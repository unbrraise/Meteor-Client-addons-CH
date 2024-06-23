package zgoly.meteorist.modules;

import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import zgoly.meteorist.Meteorist;

public class AutoLogin extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<String> loginCommand = sgGeneral.add(new StringSetting.Builder()
            .name("登录命令")
            .description("登录命令")
            .defaultValue("/login 1234")
            .build()
    );

    private final Setting<Boolean> serverOnly = sgGeneral.add(new BoolSetting.Builder()
            .name("仅服务器")
            .description("仅在服务器上使用自动登录")
            .defaultValue(true)
            .build()
    );

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
            .name("延迟")
            .description("发送命令后的延迟时间，以刻为单位（20刻 = 1秒）")
            .defaultValue(20)
            .min(1)
            .sliderRange(1, 40)
            .build()
    );

    boolean work;
    private int timer;

    public AutoLogin() {
        super(Meteorist.CATEGORY, "自动登录", "自动登录您的账户");
    }

    @Override
    public void onActivate() {
        timer = 0;
        work = true;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (serverOnly.get() && mc.getServer() != null && mc.getServer().isSingleplayer()) return;
        if (timer >= delay.get() && !loginCommand.get().isEmpty() && work) {
            work = false;
            ChatUtils.sendPlayerMsg(loginCommand.get());
            timer = 0;
        } else timer ++;
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        work = true;
    }
}