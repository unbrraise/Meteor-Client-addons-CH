package zgoly.meteorist.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.ItemStack;
import zgoly.meteorist.Meteorist;

public class AutoFix extends Module {
    public enum Mode {
        Default,
        Percentage
    }
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<String> fixCommand = sgGeneral.add(new StringSetting.Builder()
            .name("修复命令")
            .description("修复物品的命令")
            .defaultValue("/fix all")
            .build()
    );

    private final Setting<AutoFix.Mode> mode = sgGeneral.add(new EnumSetting.Builder<AutoFix.Mode>()
            .name("模式")
            .description("百分比 - 计算物品耐久度百分比， 默认 - 以数字形式计算物品耐久度")
            .defaultValue(Mode.Default)
            .build()
    );

    private final Setting<Integer> minDurability = sgGeneral.add(new IntSetting.Builder()
            .name("最小耐久")
            .description("发送命令的耐久度数字")
            .defaultValue(10)
            .min(1)
            .sliderRange(1, 1000)
            .visible(() -> mode.get() == Mode.Default)
            .build()
    );

    private final Setting<Integer> minDurabilityPercentage = sgGeneral.add(new IntSetting.Builder()
            .name("最小耐久度%")
            .description("发送命令的耐久度百分比")
            .defaultValue(10)
            .min(1)
            .sliderRange(1, 100)
            .visible(() -> mode.get() == Mode.Percentage)
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

    private int timer;

    public AutoFix() {
        super(Meteorist.CATEGORY, "物损坏发送命令", "当物品接近损坏时在聊天中写入命令");
    }

    @Override
    public void onActivate() {
        timer = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        boolean work = false;

        if (timer >= delay.get()) {
            for (ItemStack item : mc.player.getItemsEquipped()) {
                if (item.getDamage() > 0 && item.getMaxDamage() > 0) {
                    if ((mode.get() == Mode.Default && item.getMaxDamage() - item.getDamage() >= minDurability.get()) ||
                            (mode.get() == Mode.Percentage && (((item.getMaxDamage() - item.getDamage()) * 100) / item.getMaxDamage()) >= minDurabilityPercentage.get())) {
                        work = true;
                    }
                }
            }

            if (work) {
                ChatUtils.sendPlayerMsg(fixCommand.get());
                timer = 0;
            }
        } else {
            timer++;
        }
    }
}