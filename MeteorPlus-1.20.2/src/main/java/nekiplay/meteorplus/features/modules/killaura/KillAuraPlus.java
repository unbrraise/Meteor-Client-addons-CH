package nekiplay.meteorplus.features.modules.killaura;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.orbit.EventHandler;
import nekiplay.meteorplus.MeteorPlus;
import nekiplay.meteorplus.features.modules.killaura.modes.LiquidBounceAura;
import nekiplay.meteorplus.utils.algoritms.Smooth;
import net.minecraft.entity.EntityType;

import java.util.Set;

public class KillAuraPlus extends Module {
	public KillAuraPlus() {
		super(MeteorPlus.CATEGORY, "杀戮光环plus", "绕过反作弊的杀戮光环");
	}

	public final SettingGroup sgGeneral = settings.getDefaultGroup();
	public final SettingGroup sgTargeting = settings.createGroup("Targeting");
	public final SettingGroup sgDelay = settings.createGroup("Delay");

	private final Setting<KillAuraPlusModes> mode = sgGeneral.add(new EnumSetting.Builder<KillAuraPlusModes>()
		.name("模式")
		.description("应用杀戮光环的方法")
		.defaultValue(KillAuraPlusModes.LiquidBounce)
		.onModuleActivated(modesSetting -> onModeChanged(modesSetting.get()))
		.onChanged(this::onModeChanged)
		.build()
	);

	public enum RotationTickSmooth
	{
		None,
		Perlin,
		Random,
		RandomPerlin,
	}

	public enum RotationRandimize
	{
		None,
		Perlin,
		Random,
		RandomPerlin,
	}

	public enum RotationMode
	{
		None,
		OnHit,
		Instant,
		LiquidBounce,
		SmoothCenter,
		Shady,
	}

	// General

	public final Setting<Boolean> randomTeleport = sgGeneral.add(new BoolSetting.Builder()
		.name("随机传送")
		.description("是否在目标周围随机传送")
		.defaultValue(false)
		.build()
	);

	public final Setting<Boolean> revertKnockback = sgGeneral.add(new BoolSetting.Builder()
		.name("无击退")
		.description("是否反转敌人的击退")
		.defaultValue(false)
		.build()
	);

	public final Setting<RotationMode> rotation = sgGeneral.add(new EnumSetting.Builder<RotationMode>()
		.name("选择(转向)")
		.description("决定何时应该旋转以面向目标")
		.defaultValue(RotationMode.LiquidBounce)
		.build()
	);

	public final Setting<Boolean> clientLook = sgGeneral.add(new BoolSetting.Builder()
		.name("客户端视角旋转")
		.description("客户端旋转")
		.defaultValue(false)
		.build()
	);

	public final Setting<Smooth.SmoothType> rotationSmooth = sgGeneral.add(new EnumSetting.Builder<Smooth.SmoothType>()
		.name("平滑旋转")
		.description("决定何时应该平滑地旋转以面向目标")
		.defaultValue(Smooth.SmoothType.None)
		.visible(() -> rotation.get() != RotationMode.Instant && rotation.get() != RotationMode.None)
		.build()
	);

	public final Setting<Integer> rotationShadySpeed = sgGeneral.add(new IntSetting.Builder()
		.name("旋转速度")
		.description("速度")
		.defaultValue(4)
		.range(1, 5)
		.sliderRange(1, 5)
		.visible(() -> rotation.get() == RotationMode.Shady)
		.build()
	);

	public final Setting<RotationRandimize> rotationRandomize = sgGeneral.add(new EnumSetting.Builder<RotationRandimize>()
		.name("随机旋转")
		.description("旋转随机化")
		.defaultValue(RotationRandimize.None)
		.visible(() -> rotationSmooth.get() != Smooth.SmoothType.None && rotationSmooth.isVisible())
		.build()
	);

	public final Setting<Integer> rotationRandomizeMultiply = sgGeneral.add(new IntSetting.Builder()
		.name("旋转随机化倍数")
		.description("速度")
		.defaultValue(4)
		.range(0, 32)
		.sliderRange(0, 32)
		.visible(() -> rotationRandomize.get() != RotationRandimize.None && rotationSmooth.isVisible())
		.build()
	);


	public final Setting<RotationTickSmooth> rotationTickSmooth = sgGeneral.add(new EnumSetting.Builder<RotationTickSmooth>()
		.name("旋转刻度平滑")
		.description("旋转随机化")
		.defaultValue(RotationTickSmooth.None)
		.visible(() -> rotationSmooth.get() != Smooth.SmoothType.None && rotationSmooth.isVisible())
		.build()
	);

	public final Setting<Integer> rotationTickSmoothMultiply = sgGeneral.add(new IntSetting.Builder()
		.name("旋转刻度平滑倍增")
		.description("速度")
		.defaultValue(2)
		.range(0, 32)
		.sliderRange(0, 32)
		.visible(() -> rotationTickSmooth.get() != RotationTickSmooth.None && rotationTickSmooth.get() != RotationTickSmooth.Random && rotationSmooth.isVisible())
		.build()
	);


	public final Setting<Double> maxRotationSpeed = sgGeneral.add(new DoubleSetting.Builder()
		.name("最大旋转速度")
		.description("最大的旋转速度")
		.defaultValue(180)
		.range(0, 180)
		.sliderRange(0, 180)
		.visible(() -> rotation.get() != RotationMode.None && rotation.get() != RotationMode.Shady && rotation.get() != RotationMode.Instant && rotationSmooth.isVisible())
		.build()
	);

	public final Setting<Double> minRotationSpeed = sgGeneral.add(new DoubleSetting.Builder()
		.name("最小旋转速度")
		.description("最小的旋转速度")
		.defaultValue(180)
		.range(0, 180)
		.sliderRange(0, 180)
		.visible(() -> rotation.get() != RotationMode.None && rotation.get() != RotationMode.Shady && rotation.get() != RotationMode.Instant && rotationSmooth.isVisible())
		.build()
	);

	public final Setting<Boolean> rayTraceRotate = sgGeneral.add(new BoolSetting.Builder()
		.name("视线内不旋转")
		.description("如果你的头看到玩家，则不旋转")
		.visible(() -> rotation.get() != RotationMode.Instant && rotation.get() != RotationMode.None && rotationSmooth.isVisible())
		.defaultValue(false)
		.build()
	);

	public final Setting<Boolean> rayTraceAttack = sgGeneral.add(new BoolSetting.Builder()
		.name("视线外不攻击")
		.description("如果你的头部看不到玩家，就不攻击")
		.visible(() -> rotation.get() != RotationMode.Instant && rotation.get() != RotationMode.None && rotationSmooth.isVisible())
		.defaultValue(false)
		.build()
	);

	public final Setting<Double> rayTraceRotateBoxStretch = sgGeneral.add(new DoubleSetting.Builder()
		.name("旋转的射线追踪框拉伸")
		.description("旋转的射线追踪框拉伸")
		.defaultValue(0.7)
		.range(-1, 1)
		.sliderRange(-1, 1)
		.visible(() -> rayTraceRotate.isVisible())
		.build()
	);

	public final Setting<Double> rayTraceAttackBoxStretch = sgGeneral.add(new DoubleSetting.Builder()
		.name("攻击的射线追踪框拉伸")
		.description("攻击的射线追踪框拉伸")
		.defaultValue(0.7)
		.range(-1, 1)
		.sliderRange(-1, 1)
		.visible(() -> rayTraceAttack.isVisible())
		.build()
	);

	public final Setting<Boolean> shieldBreaker = sgGeneral.add(new BoolSetting.Builder()
		.name("斧头破盾")
		.description("使用斧头破坏敌人的盾牌")
		.defaultValue(true)
		.build()
	);

	public final Setting<Double> hitChance = sgGeneral.add(new DoubleSetting.Builder()
		.name("击中几率")
		.description("你的攻击命中的概率")
		.defaultValue(100)
		.range(1, 100)
		.sliderRange(1, 100)
		.build()
	);

	// Targeting
	public final Setting<Set<EntityType<?>>> entities = sgTargeting.add(new EntityTypeListSetting.Builder()
		.name("实体")
		.description("要攻击的实体")
		.onlyAttackable()
		.build()
	);

	public final Setting<Double> fov = sgTargeting.add(new DoubleSetting.Builder()
		.name("视场角")
		.description("实体可以被攻击的视场角度")
		.defaultValue(360)
		.min(30)
		.max(360)
		.sliderMax(360)
		.sliderMin(30)
		.sliderRange(30, 360)
		.build()
	);

	public final Setting<Double> range = sgTargeting.add(new DoubleSetting.Builder()
		.name("范围")
		.description("实体可以被攻击的最大范围")
		.defaultValue(4.5)
		.min(0)
		.sliderMax(6)
		.build()
	);

	public final Setting<Double> wallsRange = sgTargeting.add(new DoubleSetting.Builder()
		.name("穿墙攻击范围")
		.description("通过墙壁攻击实体的最大范围")
		.defaultValue(3.5)
		.min(0)
		.sliderMax(6)
		.build()
	);

	public final Setting<SortPriority> priority = sgTargeting.add(new EnumSetting.Builder<SortPriority>()
		.name("优先级")
		.description("如何在范围内过滤目标")
		.defaultValue(SortPriority.LowestHealth)
		.build()
	);

	public final Setting<Integer> maxTargets = sgTargeting.add(new IntSetting.Builder()
		.name("最多攻击数")
		.description("一次可以攻击的实体数量")
		.defaultValue(1)
		.min(1)
		.sliderRange(1, 5)
		.build()
	);

	public final Setting<Boolean> babies = sgTargeting.add(new BoolSetting.Builder()
		.name("幼体")
		.description("是否攻击实体的婴儿变种")
		.defaultValue(true)
		.build()
	);

	public final Setting<Boolean> nametagged = sgTargeting.add(new BoolSetting.Builder()
		.name("命名")
		.description("是否攻击带有名牌的生物")
		.defaultValue(false)
		.build()
	);

	// Delay

	public final Setting<Boolean> smartDelay = sgDelay.add(new BoolSetting.Builder()
		.name("原版攻击延迟")
		.description("使用原版冷却时间来攻击实体")
		.defaultValue(true)
		.build()
	);

	public final Setting<Integer> hitDelay = sgDelay.add(new IntSetting.Builder()
		.name("攻击延迟(刻)")
		.description("你以多少刻打击实体")
		.defaultValue(0)
		.min(0)
		.sliderMax(60)
		.visible(() -> !smartDelay.get())
		.build()
	);

	public final Setting<Boolean> randomDelayEnabled = sgDelay.add(new BoolSetting.Builder()
		.name("启用随机延迟(试图绕过反作弊)")
		.description("在攻击之间添加随机延迟，以试图绕过反作弊")
		.defaultValue(false)
		.visible(() -> !smartDelay.get())
		.build()
	);

	public final Setting<Integer> randomDelayMax = sgDelay.add(new IntSetting.Builder()
		.name("随机延迟的最大值")
		.description("随机延迟的最大值")
		.defaultValue(4)
		.min(0)
		.sliderMax(20)
		.visible(() -> randomDelayEnabled.get() && !smartDelay.get())
		.build()
	);

	public final Setting<Integer> switchDelay = sgDelay.add(new IntSetting.Builder()
		.name("切换后攻击延迟")
		.description("切换热键槽后等待多少刻才能击中实体")
		.defaultValue(0)
		.min(0)
		.build()
	);


	private KillAuraPlusMode currentMode;

	private void onModeChanged(KillAuraPlusModes mode) {
		switch (mode) {
			case LiquidBounce -> {
				currentMode = new LiquidBounceAura();
			}
		}
	}
	@EventHandler
	private void onTick(TickEvent.Post event) {
		currentMode.onTick(event);
	}
	@EventHandler
	private void onSendPacket(PacketEvent.Send event) {
		currentMode.onSendPacket(event);
	}

	@Override
	public String getInfoString() {
		return currentMode.getInfoString();
	}
}
