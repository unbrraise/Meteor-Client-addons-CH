package zgoly.meteorist.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;
import zgoly.meteorist.Meteorist;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class EntityUse extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Set<EntityType<?>>> entities = sgGeneral.add(new EntityTypeListSetting.Builder()
            .name("实体")
            .description("用物品右键的实体")
            .defaultValue(EntityType.SHEEP)
            .onlyAttackable()
            .build()
    );

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
            .name("范围")
            .description("范围")
            .min(0)
            .defaultValue(4.5)
            .build()
    );

    private final Setting<Hand> hand = sgGeneral.add(new EnumSetting.Builder<Hand>()
            .name("手")
            .description("要使用的手")
            .defaultValue(Hand.MAIN_HAND)
            .build()
    );

    private final Setting<Boolean> swingHand = sgGeneral.add(new BoolSetting.Builder()
            .name("挥动手臂")
            .description("在客户端挥动手臂")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> ignoreBabies = sgGeneral.add(new BoolSetting.Builder()
            .name("忽略幼体")
            .description("忽略幼年实体")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> oneTime = sgGeneral.add(new BoolSetting.Builder()
            .name("仅一次")
            .description("仅对每个实体使用物品一次")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
            .name("发旋转数据包")
            .description("在点击实体时向服务器发送旋转数据包")
            .defaultValue(true)
            .build()
    );

    public EntityUse() {
        super(Meteorist.CATEGORY, "右键点击", "手持物品时右键点击实体");
    }

    private final List<Entity> used = new ArrayList<>();

    @Override
    public void onActivate() {
        used.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof LivingEntity)
                    || !(entities.get().contains(entity.getType()))
                    || mc.player.getMainHandStack().isEmpty()
                    || oneTime.get() && used.contains(entity)
                    || mc.player.distanceTo(entity) > range.get()
                    || ignoreBabies.get() && ((LivingEntity) entity).isBaby()) continue;

            Rotations.rotate(Rotations.getYaw(entity), Rotations.getPitch(entity), -100, null);
            if (rotate.get()) mc.interactionManager.interactEntity(mc.player, entity, hand.get());
            if (swingHand.get()) mc.player.swingHand(hand.get());
            if (oneTime.get()) used.add(entity);

            return;
        }
    }
}