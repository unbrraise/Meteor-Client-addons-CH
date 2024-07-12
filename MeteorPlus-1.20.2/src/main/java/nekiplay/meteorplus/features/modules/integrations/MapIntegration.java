package nekiplay.meteorplus.features.modules.integrations;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import nekiplay.meteorplus.MeteorPlus;
import nekiplay.meteorplus.MixinPlugin;

public class MapIntegration extends Module {
	public MapIntegration() {
		super(MeteorPlus.CATEGORYMODS, "Mini地图+(太酷啦)", "改进Mini地图上的模块");
	}

	private final SettingGroup baritoneIntegration = settings.createGroup("Baritone");

	public final Setting<Boolean> baritoneGoto = baritoneIntegration.add(new BoolSetting.Builder()
		.name("Baritone寻路")
		.description("在选定位置使用Baritone移动")
		.defaultValue(true)
		.build()
	);

	public final Setting<Boolean> baritoneElytra = baritoneIntegration.add(new BoolSetting.Builder()
		.name("Baritoneq飞行")
		.description("使用鞘翅自动寻路飞行,但是你要设置地图种子,只能在地狱使用")
		.defaultValue(true)
		.visible(() -> {
			boolean allow = false;
			for (IBaritone baritone : BaritoneAPI.getProvider().getAllBaritones()) {
				if (!baritone.getCommandManager().getRegistry().stream().filter((a) -> a.getNames().get(0).equalsIgnoreCase("elytra")).findAny().isEmpty()) {
					allow = true;
					break;
				}
			}
			if (allow) {
				allow = baritoneGoto.get();
			}
			return allow;
		})
		.build()
	);

	private final SettingGroup fullMap = settings.createGroup("Full map");

	public final Setting<Boolean> showBlock = fullMap.add(new BoolSetting.Builder()
		.name("显示方块名称")
		.description("显示点击位置的方块名称")
		.visible(() -> MixinPlugin.isXaeroWorldMapresent)
		.defaultValue(true)
		.build()
	);
}
