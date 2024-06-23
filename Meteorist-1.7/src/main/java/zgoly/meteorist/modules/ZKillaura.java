package zgoly.meteorist.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Tameable;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import zgoly.meteorist.Meteorist;
import zgoly.meteorist.utils.Utils;

import java.util.Set;

public class ZKillaura extends Module {
    public enum OnFallMode {
        None,
        Value,
        RandomValue
    }

    public enum HitSpeedMode {
        None,
        Value,
        RandomValue
    }

    private final SettingGroup sgFilter = settings.createGroup("Filter");
    private final SettingGroup sgAttack = settings.createGroup("Attack");
    private final SettingGroup sgVisual = settings.createGroup("Visual");

    private final Setting<Set<EntityType<?>>> entities = sgFilter.add(new EntityTypeListSetting.Builder()
            .name("实体")
            .description("指定用于攻击的实体类型")
            .onlyAttackable()
            .defaultValue(EntityType.PLAYER)
            .build()
    );

    private final Setting<Double> range = sgFilter.add(new DoubleSetting.Builder()
            .name("范围")
            .description("定义攻击目标实体的最大范围")
            .defaultValue(4.5)
            .min(0)
            .sliderMax(6)
            .build()
    );

    private final Setting<Boolean> ignoreBabies = sgFilter.add(new BoolSetting.Builder()
            .name("忽略幼体")
            .description("防止攻击动物的幼崽变体")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> ignoreNamed = sgFilter.add(new BoolSetting.Builder()
            .name("忽略命名")
            .description("防止攻击已命名的生物")
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> ignorePassive = sgFilter.add(new BoolSetting.Builder()
            .name("被动杀戮")
            .description("只有实体攻击你时才攻击他")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> ignoreTamed = sgFilter.add(new BoolSetting.Builder()
            .name("忽略驯服")
            .description("防止攻击已驯服的生物")
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> ignoreFriends = sgFilter.add(new BoolSetting.Builder()
            .name("忽略好友")
            .description("防止攻击您好友列表中的玩家")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> ignoreWalls = sgFilter.add(new BoolSetting.Builder()
            .name("穿墙攻击!")
            .description("允许穿墙攻击")
            .defaultValue(false)
            .build()
    );

    private final Setting<OnFallMode> onFallMode = sgAttack.add(new EnumSetting.Builder<OnFallMode>()
            .name("下落时的攻击模式")
            .description("选择一个攻击策略，以在下落时最大化暴击伤害")
            .defaultValue(OnFallMode.Value)
            .build()
    );

    private final Setting<Double> onFallValue = sgAttack.add(new DoubleSetting.Builder()
            .name("下落攻击为特定数值")
            .description("定义在下落时攻击的特定数值")
            .min(0)
            .defaultValue(0.25)
            .sliderMax(1)
            .visible(() -> onFallMode.get() == OnFallMode.Value)
            .build()
    );

    private final Setting<Double> onFallMinRandomValue = sgAttack.add(new DoubleSetting.Builder()
            .name("下落攻击时最小随机值")
            .description("指定在下落时攻击时的最小随机化数值")
            .min(0)
            .defaultValue(0.2)
            .sliderMax(1)
            .visible(() -> onFallMode.get() == OnFallMode.RandomValue)
            .build()
    );

    private final Setting<Double> onFallMaxRandomValue = sgAttack.add(new DoubleSetting.Builder()
            .name("下落时攻击时最大随机值")
            .description("指定在下落时攻击时的最大随机化数值")
            .min(0)
            .defaultValue(0.4)
            .sliderMax(1)
            .visible(() -> onFallMode.get() == OnFallMode.RandomValue)
            .build()
    );

    private final Setting<HitSpeedMode> hitSpeedMode = sgAttack.add(new EnumSetting.Builder<HitSpeedMode>()
            .name("攻击速度模式")
            .description("选择攻击的打击速度模式")
            .defaultValue(HitSpeedMode.Value)
            .build()
    );

    private final Setting<Double> hitSpeedValue = sgAttack.add(new DoubleSetting.Builder()
            .name("命中速度值")
            .description("定义攻击时的特定命中速度值。")
            .defaultValue(0)
            .sliderRange(-10, 10)
            .visible(() -> hitSpeedMode.get() == HitSpeedMode.Value)
            .build()
    );

    private final Setting<Double> hitSpeedMinRandomValue = sgAttack.add(new DoubleSetting.Builder()
            .name("最小随机命中速度值")
            .description("指定最小随机命中速度值")
            .defaultValue(0)
            .sliderRange(-10, 10)
            .visible(() -> hitSpeedMode.get() == HitSpeedMode.RandomValue)
            .build()
    );

    private final Setting<Double> hitSpeedMaxRandomValue = sgAttack.add(new DoubleSetting.Builder()
            .name("最大随机命中速度值")
            .description("指定最大随机命中速度值")
            .defaultValue(0)
            .sliderRange(-10, 10)
            .visible(() -> hitSpeedMode.get() == HitSpeedMode.RandomValue)
            .build()
    );

    private final Setting<Boolean> swingHand = sgVisual.add(new BoolSetting.Builder()
            .name("手臂摆动")
            .description("使手部摆动在客户端可见")
            .defaultValue(true)
            .build()
    );

    public ZKillaura() {
        super(Meteorist.CATEGORY, "瞄准杀戮光环", "只有在瞄准目标时才攻击的杀戮光环");
    }

    float randomOnFallFloat = 0;
    float randomHitSpeedFloat = 0;

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player.isDead() || mc.world == null) return;

        OnFallMode currOnFallMode = onFallMode.get();
        if (currOnFallMode != OnFallMode.None) {
            float onFall = currOnFallMode == OnFallMode.Value ? onFallValue.get().floatValue() : randomOnFallFloat;
            if (!(mc.player.fallDistance > onFall)) return;
        }

        HitSpeedMode currHitSpeedMode = hitSpeedMode.get();
        float hitSpeed = currHitSpeedMode == HitSpeedMode.Value ? hitSpeedValue.get().floatValue() : randomHitSpeedFloat;
        if (currHitSpeedMode != HitSpeedMode.None && (mc.player.getAttackCooldownProgress(hitSpeed) * 17.0F) < 16) return;

        HitResult hitResult = Utils.getCrosshairTarget(mc.player, range.get(), ignoreWalls.get(), (e -> !e.isSpectator()
                && e.canHit()
                && entities.get().contains(e.getType())
                && !(ignoreBabies.get() && (e instanceof AnimalEntity && (((AnimalEntity) e).isBaby())))
                && !(ignoreNamed.get() && e.hasCustomName())
                && !(ignorePassive.get() && (e instanceof Tameable && ((Tameable) e).getOwnerUuid() != null && !((Tameable) e).getOwnerUuid().equals(mc.player.getUuid())))
                && !(ignoreTamed.get() && (e instanceof PlayerEntity && !Friends.get().shouldAttack((PlayerEntity) e)))
                && !(ignoreFriends.get() && (e instanceof PlayerEntity && !Friends.get().shouldAttack((PlayerEntity) e)))
        ));

        if (hitResult == null || hitResult.getType() != HitResult.Type.ENTITY) return;
        Entity entity = ((EntityHitResult) hitResult).getEntity();

        LivingEntity livingEntity = (LivingEntity) entity;
        if (livingEntity.getHealth() > 0) {
            mc.interactionManager.attackEntity(mc.player, livingEntity);

            if (swingHand.get()) mc.player.swingHand(Hand.MAIN_HAND);

            if (currOnFallMode == OnFallMode.RandomValue) {
                float min = Math.min(onFallMinRandomValue.get().floatValue(), onFallMaxRandomValue.get().floatValue());
                float max = Math.max(onFallMinRandomValue.get().floatValue(), onFallMaxRandomValue.get().floatValue());
                randomOnFallFloat = min + mc.world.random.nextFloat() * (max - min);
            }

            if (currHitSpeedMode == HitSpeedMode.RandomValue) {
                float min = Math.min(hitSpeedMinRandomValue.get().floatValue(), hitSpeedMaxRandomValue.get().floatValue());
                float max = Math.max(hitSpeedMinRandomValue.get().floatValue(), hitSpeedMaxRandomValue.get().floatValue());
                randomHitSpeedFloat = min + mc.world.random.nextFloat() * (max - min);
            }
        }
    }
}