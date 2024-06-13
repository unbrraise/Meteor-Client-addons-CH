package widecat.meteorcrashaddon.modules;

import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import widecat.meteorcrashaddon.CrashAddon;

public class PacketSpammer extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> amount = sgGeneral.add(new IntSetting.Builder()
        .name("发包数量")
        .description("每个刻度向服务器发送多少个数据包")
        .defaultValue(100)
        .min(1)
        .sliderMax(1000)
        .build());

    private final Setting<Boolean> autoDisable = sgGeneral.add(new BoolSetting.Builder()
        .name("踢出关闭")
        .description("在被踢出时禁用模块")
        .defaultValue(true)
        .build());

    public PacketSpammer() {
        super(CrashAddon.CATEGORY, "封包垃圾邮件", "发送随机数据包（tmd毫无用处）");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        for (int i = 0; i < amount.get(); i++) {
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(Math.random() >= 0.5));
            mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        }
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        if (autoDisable.get()) toggle();
    }
}
