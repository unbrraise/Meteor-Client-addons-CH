package zgoly.meteorist.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import zgoly.meteorist.Meteorist;

public class AutoHeal extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<String> healCommand = sgGeneral.add(new StringSetting.Builder()
            .name("补血命令")
            .description("补满生命值条的命令")
            .defaultValue("/heal")
            .build()
    );

    private final Setting<Integer> healthLevel = sgGeneral.add(new IntSetting.Builder()
            .name("生命值")
            .description("发送命令的生命值")
            .defaultValue(10)
            .min(1)
            .sliderRange(1, 20)
            .build()
    );

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
            .name("延迟")
            .description("发送命令后的延迟时间，以刻为单位（20刻 = 1秒)")
            .defaultValue(20)
            .min(1)
            .sliderRange(1, 40)
            .build()
    );

    private int timer;

    public AutoHeal() {
        super(Meteorist.CATEGORY, "生命低发送命令", "当生命值较低时在聊天中写入命令");
    }

    @Override
    public void onActivate() {
        timer = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (timer >= delay.get() && mc.player.getHealth() <= healthLevel.get()) {
            ChatUtils.sendPlayerMsg(healCommand.get());
            timer = 0;
        } else timer ++;
    }
}