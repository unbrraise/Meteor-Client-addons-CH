package cqb13.NumbyHack.modules.general;

import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.util.math.Vec3d;
import cqb13.NumbyHack.NumbyHack;

/**
 * from Tanuki
 */
public class Confetti extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<SettingColor> colorOne = sgGeneral.add(new ColorSetting.Builder()
            .name("颜色1")
            .description("要更改的第一种彩带颜色")
            .defaultValue(new SettingColor(73, 107, 190, 255))
            .build()
    );

    private final Setting<SettingColor> colorTwo = sgGeneral.add(new ColorSetting.Builder()
            .name("颜色2")
            .description("要更改的第二种彩带颜色")
            .defaultValue(new SettingColor(73, 107, 190, 255))
            .build()
    );

    public Confetti() {
        super(NumbyHack.CATEGORY, "图腾粒子", "更改不死图腾弹出粒子的颜色");
    }

    public Vec3d getColorOne() {
        return getDoubleVectorColor(colorOne);
    }

    public Vec3d getColorTwo() {
        return getDoubleVectorColor(colorTwo);
    }

    public Vec3d getDoubleVectorColor(Setting<SettingColor> colorSetting) {
        return new Vec3d((double) colorSetting.get().r / 255, (double) colorSetting.get().g / 255, (double) colorSetting.get().b / 255);
    }
}
