package zgoly.meteorist.modules;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.Target;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Tameable;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import zgoly.meteorist.Meteorist;

import java.util.Set;

public class ZAimbot extends Module {
    private final SettingGroup sgFilter = settings.createGroup("Filter");
    private final SettingGroup sgAim = settings.createGroup("Aim");
    private final SettingGroup sgVisibility = settings.createGroup("Visibility");

    private final Setting<Set<EntityType<?>>> entities = sgFilter.add(new EntityTypeListSetting.Builder()
            .name("实体")
            .description("指定要瞄准的实体类型")
            .onlyAttackable()
            .defaultValue(EntityType.PLAYER)
            .build()
    );

    private final Setting<Double> range = sgFilter.add(new DoubleSetting.Builder()
            .name("范围")
            .description("目标实体的最大距离")
            .min(0)
            .defaultValue(4.5)
            .build()
    );

    private final Setting<SortPriority> priority = sgFilter.add(new EnumSetting.Builder<SortPriority>()
            .name("优先级")
            .description("用于在范围内优先选择目标的排序方法")
            .defaultValue(SortPriority.ClosestAngle)
            .build()
    );

    private final Setting<Boolean> ignoreBabies = sgFilter.add(new BoolSetting.Builder()
            .name("忽略幼体")
            .description("防止瞄准动物的幼崽变体")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> ignoreNamed = sgFilter.add(new BoolSetting.Builder()
            .name("忽略命名")
            .description("防止瞄准已命名的生物")
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> ignorePassive = sgFilter.add(new BoolSetting.Builder()
            .name("忽略被动")
            .description("仅允许瞄准瞄准你的生物")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> ignoreTamed = sgFilter.add(new BoolSetting.Builder()
            .name("忽略驯服")
            .description("防止瞄准已驯服的生物")
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> ignoreFriends = sgFilter.add(new BoolSetting.Builder()
            .name("忽略好友")
            .description("防止瞄准在您好友列表中的玩家")
            .defaultValue(true)
            .build()
    );

    private final Setting<Target> bodyTarget = sgAim.add(new EnumSetting.Builder<Target>()
            .name("瞄准部位")
            .description("瞄准目标实体的身体部位")
            .defaultValue(Target.Head)
            .build()
    );

    private final Setting<Boolean> instantAim = sgAim.add(new BoolSetting.Builder()
            .name("瞬喵QAQ")
            .description("瞬间瞄准目标实体")
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> syncSpeedWithCooldown = sgAim.add(new BoolSetting.Builder()
            .name("瞄准速度与攻击冷却进度同步")
            .description("将瞄准速度与攻击冷却进度同步")
            .defaultValue(false)
            .visible(() -> !instantAim.get())
            .build()
    );

    private final Setting<Double> speed = sgAim.add(new DoubleSetting.Builder()
            .name("速度")
            .description("调整瞄准速度")
            .min(0)
            .defaultValue(1)
            .sliderRange(0.1, 10)
            .visible(() -> !instantAim.get())
            .build()
    );

    public final Setting<Double> targetMovementPrediction = sgAim.add(new DoubleSetting.Builder()
            .name("预测目标偏移量")
            .description("瞄准时预测目标移动的量")
            .min(0.0F)
            .sliderMax(20.0F)
            .defaultValue(0.0F)
            .build()
    );

    private final Setting<Boolean> useFovRange = sgVisibility.add(new BoolSetting.Builder()
            .name("瞄准的视野范围")
            .description("将瞄准限制在指定视野范围内的实体")
            .defaultValue(true)
            .build()
    );

    private final Setting<Double> fovRange = sgVisibility.add(new DoubleSetting.Builder()
            .name("视野范围")
            .description("瞄准实体的最大视野范围")
            .sliderRange(0, 180)
            .defaultValue(90)
            .visible(useFovRange::get)
            .build()
    );

    private final Setting<Boolean> ignoreWalls = sgVisibility.add(new BoolSetting.Builder()
            .name("穿墙瞄准!")
            .description("允许穿墙瞄准")
            .defaultValue(false)
            .build()
    );

    public ZAimbot() {
        super(Meteorist.CATEGORY, "自瞄", "智能瞄准器会在瞄准时拥有更多设置");
    }

    @EventHandler
    private void renderTick(Render3DEvent event) {
        if (mc.player == null || mc.world == null) return;
        LivingEntity player = mc.player;

        // filter entities
        Entity target = TargetUtils.get(e -> !e.equals(player)
                && e.isAlive()
                && entities.get().contains(e.getType())
                && !(ignoreBabies.get() && (e instanceof AnimalEntity && (((AnimalEntity) e).isBaby())))
                && !(ignoreNamed.get() && e.hasCustomName())
                && !(ignorePassive.get() && (e instanceof PassiveEntity && ((PassiveEntity) e).isAttacking()))
                && !(ignoreTamed.get() && (e instanceof Tameable && ((Tameable) e).getOwnerUuid() != null && !((Tameable) e).getOwnerUuid().equals(player.getUuid())))
                && !(ignoreFriends.get() && (e instanceof PlayerEntity && !Friends.get().shouldAttack((PlayerEntity) e)))
                && PlayerUtils.isWithin(e, range.get())
                && (!useFovRange.get() || calculateFov(player, e) <= fovRange.get())
                && (ignoreWalls.get() || PlayerUtils.canSeeEntity(e)), priority.get()
        );

        if (target == null) return;
        aim(player, target);
    }

    private float calculateFov(LivingEntity player, Entity target) {
        Vec3d lookDirection = player.getRotationVec(1.0F);
        Vec3d targetDirection = target.getPos().subtract(player.getPos()).normalize();
        return (float) Math.toDegrees(Math.acos(lookDirection.dotProduct(targetDirection)));
    }

    private void aim(LivingEntity player, Entity target) {
        float targetYaw = (float) Rotations.getYaw(target.getPos().add(target.getVelocity().multiply(targetMovementPrediction.get())));
        float targetPitch = (float) Rotations.getPitch(target, bodyTarget.get());

        float yawDifference = MathHelper.wrapDegrees(targetYaw - player.getYaw());
        float pitchDifference = MathHelper.wrapDegrees(targetPitch - player.getPitch());

        if (instantAim.get()) {
            player.setYaw(targetYaw);
            player.setPitch(targetPitch);
        } else {
            float cooldownProgress = syncSpeedWithCooldown.get() ? mc.player.getAttackCooldownProgress(0) : 1;
            player.setYaw(player.getYaw() + yawDifference * cooldownProgress * speed.get().floatValue() / 10);
            player.setPitch(player.getPitch() + pitchDifference * cooldownProgress * speed.get().floatValue() / 10);
        }
    }
}
