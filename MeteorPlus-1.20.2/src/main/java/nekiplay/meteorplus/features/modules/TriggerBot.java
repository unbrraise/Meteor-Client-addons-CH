package nekiplay.meteorplus.features.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import nekiplay.meteorplus.MeteorPlus;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Tameable;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.world.GameMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TriggerBot extends Module {
	public TriggerBot() {
		super(MeteorPlus.CATEGORY, "杀戮光环", "攻击你周围的指定实体");
	}

	@Override
	public void onDeactivate() {
		hitDelayTimer = 0;
		targets.clear();
	}

	private final SettingGroup sgGeneral = settings.getDefaultGroup();

	private final Setting<Set<EntityType<?>>> entities = sgGeneral.add(new EntityTypeListSetting.Builder()
		.name("实体")
		.description("要攻击的实体列表")
		.onlyAttackable()
		.build()
	);

	private final Setting<Boolean> babies = sgGeneral.add(new BoolSetting.Builder()
		.name("忽略幼体")
		.description("是否攻击实体的幼年变种")
		.defaultValue(true)
		.build()
	);

	private final Setting<Boolean> smartDelay = sgGeneral.add(new BoolSetting.Builder()
		.name("原版攻击间隔")
		.description("使用原版的冷却时间来攻击实体")
		.defaultValue(true)
		.build()
	);

	private final Setting<Integer> hitDelay = sgGeneral.add(new IntSetting.Builder()
		.name("攻击延迟")
		.description("你击打实体的速度（以tick为单位）")
		.defaultValue(0)
		.min(0)
		.sliderMax(60)
		.visible(() -> !smartDelay.get())
		.build()
	);

	private final Setting<Boolean> randomDelayEnabled = sgGeneral.add(new BoolSetting.Builder()
		.name("随机延迟(尝试绕过反作弊)")
		.description("在击打之间添加随机延迟以试图绕过反作弊")
		.defaultValue(false)
		.visible(() -> !smartDelay.get())
		.build()
	);

	private final Setting<Integer> randomDelayMax = sgGeneral.add(new IntSetting.Builder()
		.name("最大随机延迟")
		.description("随机延迟的最大值")
		.defaultValue(4)
		.min(0)
		.sliderMax(20)
		.visible(() -> randomDelayEnabled.get() && !smartDelay.get())
		.build()
	);

	private final List<Entity> targets = new ArrayList<>();

	private int hitDelayTimer;

	private boolean entityCheck(Entity entity) {
		if (entity.equals(mc.player) || entity.equals(mc.cameraEntity)) return false;
		if ((entity instanceof LivingEntity && ((LivingEntity) entity).isDead()) || !entity.isAlive()) return false;
		if (!entities.get().contains(entity.getType())) return false;
		if (entity instanceof Tameable tameable
			&& tameable.getOwnerUuid() != null
			&& tameable.getOwnerUuid().equals(mc.player.getUuid())) return false;
		if (entity instanceof PlayerEntity) {
			if (((PlayerEntity) entity).isCreative()) return false;
			if (!Friends.get().shouldAttack((PlayerEntity) entity)) return false;
		}
		return !(entity instanceof AnimalEntity) || babies.get() || !((AnimalEntity) entity).isBaby();
	}

	private boolean delayCheck() {
		if (smartDelay.get()) return mc.player.getAttackCooldownProgress(0.5f) >= 1;


		if (hitDelayTimer > 0) {
			hitDelayTimer--;
			return false;
		} else {
			hitDelayTimer = hitDelay.get();
			if (randomDelayEnabled.get()) hitDelayTimer += Math.round(Math.random() * randomDelayMax.get());
			return true;
		}
	}

	@EventHandler
	private void onTick(TickEvent.Pre event) {
		if (!mc.player.isAlive() || PlayerUtils.getGameMode() == GameMode.SPECTATOR) return;
		if (mc.targetedEntity == null) return;
		MultiTasks multiTasks = Modules.get().get(MultiTasks.class);
		if (!multiTasks.isActive() && (mc.player.isUsingItem() || mc.interactionManager.isBreakingBlock())) return;

		if (delayCheck()) hitEntity(mc.targetedEntity);
	}

	private void hitEntity(Entity target) {
		mc.interactionManager.attackEntity(mc.player, target);
		mc.player.swingHand(Hand.MAIN_HAND);
	}
}
