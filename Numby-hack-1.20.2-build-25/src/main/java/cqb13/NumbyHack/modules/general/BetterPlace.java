package cqb13.NumbyHack.modules.general;

import cqb13.NumbyHack.NumbyHack;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

public class BetterPlace extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRange = settings.createGroup("Range");

    // General

    private final Setting<Boolean> render = sgGeneral.add(new BoolSetting.Builder()
            .name("渲染")
            .description("渲染一个方块覆盖层显示方块将被放置的位置")
            .defaultValue(true)
            .build()
    );

    private final Setting<ShapeMode> shapeMode = sgGeneral.add(new EnumSetting.Builder<ShapeMode>()
            .name("渲染模式")
            .description("形状如何被渲染")
            .defaultValue(ShapeMode.Both)
            .build()
    );

    private final Setting<SettingColor> sideColor = sgGeneral.add(new ColorSetting.Builder()
            .name("侧面颜色")
            .description("被渲染方块四周的颜色")
            .defaultValue(new SettingColor(146,188,98, 75))
            .build()
    );

    private final Setting<SettingColor> lineColor = sgGeneral.add(new ColorSetting.Builder()
            .name("线框颜色")
            .description("被渲染方块线条的颜色")
            .defaultValue(new SettingColor(146,188,98, 255))
            .build()
    );

    // Range

    private final Setting<Boolean> customRange = sgRange.add(new BoolSetting.Builder()
            .name("自定义范围")
            .description("使用自定义范围进行更好的放置")
            .defaultValue(false)
            .build()
    );

    private final Setting<Double> range = sgRange.add(new DoubleSetting.Builder()
            .name("范围")
            .description("选取合适的范围")
            .visible(customRange::get)
            .defaultValue(5)
            .min(0)
            .sliderMax(6)
            .build()
    );

    private HitResult hitResult;

    public BetterPlace() {
        super(NumbyHack.CATEGORY, "更好的放置", "帮助您在通常无法放置的位置放置方块");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        setHitResult();
        if (hitResult instanceof BlockHitResult && mc.player.getMainHandStack().getItem() instanceof BlockItem && mc.options.useKey.isPressed()) {
            BlockUtils.place(((BlockHitResult) hitResult).getBlockPos(), Hand.MAIN_HAND, mc.player.getInventory().selectedSlot, false, 0, true, true, false);
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (!(hitResult instanceof BlockHitResult)
                || !mc.world.getBlockState(((BlockHitResult) hitResult).getBlockPos()).isReplaceable()
                || !(mc.player.getMainHandStack().getItem() instanceof BlockItem)
                || !render.get()) return;

        event.renderer.box(((BlockHitResult) hitResult).getBlockPos(), sideColor.get(), lineColor.get(), shapeMode.get(), 0);
    }

    private void setHitResult() {
        final double r = customRange.get() ? range.get() : mc.interactionManager.getReachDistance();
        for (int i = (int) r; i > 0; i -= 1D) {
            hitResult = mc.getCameraEntity().raycast(Math.min(r, i), 0, false);
            if (hitResult instanceof BlockHitResult && isValid(((BlockHitResult) hitResult).getBlockPos())) return;
        }
        hitResult = null;
    }

    private boolean isValid(BlockPos pos) {
        return !pos.equals(mc.player.getBlockPos()) && BlockUtils.getPlaceSide(pos) != null;
    }
}
