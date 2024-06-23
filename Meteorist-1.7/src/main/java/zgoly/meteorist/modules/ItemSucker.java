package zgoly.meteorist.modules;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.pathing.goals.GoalGetToBlock;
import meteordevelopment.meteorclient.commands.commands.SettingCommand;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import zgoly.meteorist.Meteorist;

import java.util.List;

public class ItemSucker extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    public enum OperationMode {
        Whitelist,
        Blacklist
    }

    private final Setting<OperationMode> itemFilteringMode = sgGeneral.add(new EnumSetting.Builder<OperationMode>()
            .name("物品拾取模式")
            .description("定义物品拾取的模式")
            .defaultValue(OperationMode.Blacklist)
            .build()
    );

    private final Setting<List<Item>> itemWhitelist = sgGeneral.add(new ItemListSetting.Builder()
            .name("物品白名单")
            .description("定义物品拾取器专门拾取的物品")
            .defaultValue(Items.DIAMOND)
            .visible(() -> itemFilteringMode.get() == OperationMode.Whitelist)
            .build()
    );

    private final Setting<List<Item>> itemBlacklist = sgGeneral.add(new ItemListSetting.Builder()
            .name("物品黑名单")
            .description("定义物品吸取器专门忽略的物品")
            .defaultValue(Items.POISONOUS_POTATO)
            .visible(() -> itemFilteringMode.get() == OperationMode.Blacklist)
            .build()
    );

    private final Setting<Double> suckingRange = sgGeneral.add(new DoubleSetting.Builder()
            .name("拾取范围")
            .description("物品拾取器可以收集物品的范围，以方块为单位")
            .defaultValue(5)
            .min(1)
            .sliderRange(1, 25)
            .build()
    );

    private final Setting<Boolean> modifySpeed = sgGeneral.add(new BoolSetting.Builder()
            .name("修改移速")
            .description("是否在使用物品吸取器时改变玩家的速度")
            .defaultValue(true)
            .build()
    );

    private final Setting<Double> movementSpeed = sgGeneral.add(new DoubleSetting.Builder()
            .name("移动速度")
            .description("当“修改移速”启用时修改玩家的移动速度")
            .defaultValue(20)
            .min(1)
            .sliderRange(1, 30)
            .visible(modifySpeed::get)
            .build()
    );

    private final Setting<Boolean> returnToOrigin = sgGeneral.add(new BoolSetting.Builder()
            .name("返回原始位置")
            .description("当所有物品被拾取后，自动将玩家返回到初始位置")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> radiusWarning = sgGeneral.add(new BoolSetting.Builder()
            .name("半径警告")
            .description("当跟随半径(是BaritoneAPI中的设置)不为0时收到警告。")
            .defaultValue(true)
            .build()
    );

    public ItemSucker() {
        super(Meteorist.CATEGORY, "更好的物品拾取", "自动收集地面的物品，并具有各种可自定义的行为");
    }

    BlockPos startPos = null;
    IBaritone baritone = BaritoneAPI.getProvider().getPrimaryBaritone();

    @Override
    public void onActivate() {
        if (radiusWarning.get() && BaritoneAPI.getSettings().followRadius.value != 0) {
            info(Text.empty().append(Text.literal("物品拾取会使用 Baritone 自动寻路，最好进行相关的设置").setStyle(Style.EMPTY.withColor(Formatting.YELLOW)))
                    .append(Text.literal(BaritoneAPI.getSettings().followRadius.getName()).setStyle(Style.EMPTY.withColor(Formatting.AQUA)))
                    .append(Text.literal(" to ").setStyle(Style.EMPTY.withColor(Formatting.YELLOW)))
                    .append(Text.literal("0").setStyle(Style.EMPTY.withColor(Formatting.AQUA)))
                    .append(Text.literal(".\n\n").setStyle(Style.EMPTY.withColor(Formatting.YELLOW)))
                    .append(Text.literal("[Set " + BaritoneAPI.getSettings().followRadius.getName() + " to 0]").setStyle(Style.EMPTY.withColor(Formatting.GREEN)
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Set " + BaritoneAPI.getSettings().followRadius.getName() + " to 0.")))
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, BaritoneAPI.getSettings().prefix.value + BaritoneAPI.getSettings().followRadius.getName() + " 0")))
                    )
                    .append(Text.literal(" | ").setStyle(Style.EMPTY.withColor(Formatting.YELLOW)))
                    .append(Text.literal("[Suppress warning]").setStyle(Style.EMPTY.withColor(Formatting.RED)
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Suppress warning.")))
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, new SettingCommand() + " " + name + " " + radiusWarning.name + " " + false)))
                    )
                    .append(Text.literal("."))
            );
        }

        baritone.getFollowProcess().cancel();
        baritone.getFollowProcess().follow(entity -> entity instanceof ItemEntity
                && !((ItemEntity) entity).cannotPickup()
                && ((itemFilteringMode.get() == OperationMode.Blacklist && !itemBlacklist.get().contains((((ItemEntity) entity).getStack().getItem())))
                || (itemFilteringMode.get() == OperationMode.Whitelist && itemWhitelist.get().contains((((ItemEntity) entity).getStack().getItem()))))
                && (PlayerUtils.distanceTo(entity) <= suckingRange.get())
        );
    }

    @Override
    public void onDeactivate() {
        baritone.getFollowProcess().cancel();
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        startPos = null;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (modifySpeed.get() && (baritone.getPathingBehavior().isPathing())) {
            Vec3d vel = PlayerUtils.getHorizontalVelocity(movementSpeed.get());
            ((IVec3d) event.movement).set(vel.getX(), event.movement.y, vel.getZ());
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (baritone.getFollowProcess().isActive() && startPos == null) {
            startPos = mc.player.getBlockPos();
        } else if (!baritone.getFollowProcess().isActive() && startPos != null) {
            if (returnToOrigin.get()) baritone.getCustomGoalProcess().setGoalAndPath(new GoalGetToBlock(startPos));
            startPos = null;
        }
    }
}
