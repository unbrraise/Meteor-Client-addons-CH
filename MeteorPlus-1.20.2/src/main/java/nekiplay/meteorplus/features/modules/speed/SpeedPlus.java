package nekiplay.meteorplus.features.modules.speed;

import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import nekiplay.meteorplus.MeteorPlus;
import nekiplay.meteorplus.features.modules.speed.modes.*;
import nekiplay.meteorplus.features.modules.speed.modes.matrix.Matrix;
import nekiplay.meteorplus.features.modules.speed.modes.matrix.Matrix6_7_0;
import nekiplay.meteorplus.features.modules.speed.modes.matrix.MatrixExploit;
import nekiplay.meteorplus.features.modules.speed.modes.matrix.MatrixExploit2;

public class SpeedPlus extends Module {
	public SpeedPlus() {
		super(MeteorPlus.CATEGORY, "速度+", "绕过反作弊的地面加速");
		onSpeedModeChanged(speedMode.get());
	}
	private final SettingGroup sgGeneral = settings.getDefaultGroup();


	public final Setting<SpeedModes> speedMode = sgGeneral.add(new EnumSetting.Builder<SpeedModes>()
		.name("模式")
		.description("应用速度的方法")
		.defaultValue(SpeedModes.Matrix_Exploit)
		.onModuleActivated(spiderModesSetting -> onSpeedModeChanged(spiderModesSetting.get()))
		.onChanged(this::onSpeedModeChanged)
		.build()
	);

	public final Setting<Double> speedMatrix = sgGeneral.add(new DoubleSetting.Builder()
		.name("速度")
		.description("速度")
		.defaultValue(4)
		.visible(() -> speedMode.get() == SpeedModes.Matrix_Exploit || speedMode.get() == SpeedModes.Matrix_Exploit_2)
		.build()
	);

	public final Setting<Double> speedVulcanef2 = sgGeneral.add(new DoubleSetting.Builder()
		.name("速度效果2")
		.description("速度效果2")
		.defaultValue(45)
		.max(75)
		.sliderRange(0, 75)
		.visible(() -> speedMode.get() == SpeedModes.Vulcan)
		.build()
	);

	public final Setting<Double> speedVulcanef1 = sgGeneral.add(new DoubleSetting.Builder()
		.name("速度效果1")
		.description("速度效果1")
		.defaultValue(45)
		.max(75)
		.sliderRange(0, 75)
		.visible(() -> speedMode.get() == SpeedModes.Vulcan)
		.build()
	);

	public final Setting<Double> speedVulcanef0 = sgGeneral.add(new DoubleSetting.Builder()
		.name("速度效果0")
		.description("速度效果0")
		.defaultValue(35)
		.max(75)
		.sliderRange(0, 75)
		.visible(() -> speedMode.get() == SpeedModes.Vulcan)
		.build()
	);


	public final Setting<Boolean> autoSwapVulcan = sgGeneral.add(new BoolSetting.Builder()
		.name("自动切换")
		.description("自动切换")
		.defaultValue(true)
		.visible(() -> speedMode.get() == SpeedModes.Vulcan)
		.build()
	);

	private SpeedMode currentMode;

	@Override
	public void onActivate() {
		currentMode.onActivate();
	}

	@Override
	public void onDeactivate() {
		currentMode.onDeactivate();
	}

	@EventHandler
	private void onPreTick(TickEvent.Pre event) {
		currentMode.onTickEventPre(event);
	}

	@EventHandler
	private void onPostTick(TickEvent.Post event) {
		currentMode.onTickEventPost(event);
	}
	@EventHandler
	public void onSendPacket(PacketEvent.Send event) {
		currentMode.onSendPacket(event);
	}
	@EventHandler
	public void onSentPacket(PacketEvent.Sent event) {
		currentMode.onSentPacket(event);
	}

	@EventHandler
	private void onPlayerMoveEvent(PlayerMoveEvent event) {
		currentMode.onPlayerMoveEvent(event);
	}


	private void onSpeedModeChanged(SpeedModes mode) {
		switch (mode) {
			case Matrix_Exploit_2 -> currentMode = new MatrixExploit2();
			case Matrix_Exploit -> currentMode = new MatrixExploit();
			case Matrix_6_7_0 -> currentMode = new Matrix6_7_0();
			case Matrix -> currentMode = new Matrix();
			case AAC_Hop_4_3_8 -> currentMode = new AACHop438();
			case Vulcan -> currentMode = new Vulcan();
			case NCP_Hop -> currentMode = new NCPHop();
		}
	}
}
