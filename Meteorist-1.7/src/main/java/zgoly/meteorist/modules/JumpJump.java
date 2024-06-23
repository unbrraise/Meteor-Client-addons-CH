package zgoly.meteorist.modules;

import meteordevelopment.meteorclient.events.meteor.KeyEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.orbit.EventHandler;
import zgoly.meteorist.Meteorist;

public class JumpJump extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> multiplier = sgGeneral.add(new IntSetting.Builder()
            .name("跳跃次数")
            .description("跳跃的次数")
            .defaultValue(2)
            .range(1, 1200)
            .sliderRange(1, 10)
            .onChanged(a -> onActivate())
            .build()
    );

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
            .name("延迟")
            .description("发送命令后的延迟时间，以刻为单位（20刻 = 1秒）")
            .defaultValue(3)
            .range(1, 1200)
            .sliderRange(1, 10)
            .build()
    );

    int mult;
    int timer;

    public JumpJump() {
        super(Meteorist.CATEGORY, "左脚踩右脚", "使用多次跳跃使您跳得比正常更高");
    }

    @Override
    public void onActivate() {
        timer = 0;
        mult = multiplier.get();
    }

    @EventHandler
    private void onKey(KeyEvent event) {
        if (event.action != KeyAction.Press) return;
        if (mc.options.jumpKey.matchesKey(event.key, 0)) mult = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mult < multiplier.get()) {
            if (timer >= delay.get()) {
                mult ++;
                mc.player.fallDistance = 0;
                mc.player.jump();
                timer = 0;
            } else timer ++;
        }
    }
}