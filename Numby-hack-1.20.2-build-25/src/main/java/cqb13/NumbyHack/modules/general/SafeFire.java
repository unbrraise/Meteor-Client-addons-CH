package cqb13.NumbyHack.modules.general;

import cqb13.NumbyHack.NumbyHack;
import meteordevelopment.meteorclient.events.world.CollisionShapeEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.block.Blocks;
import net.minecraft.block.FireBlock;
import net.minecraft.block.SoulFireBlock;
import net.minecraft.util.shape.VoxelShapes;

/**
 * modified from Tanuki orignaly by walaryne
 */
public class SafeFire extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> fire = sgGeneral.add(new BoolSetting.Builder()
            .name("防止进普通火")
            .description("防止您走进火焰")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> soulFire = sgGeneral.add(new BoolSetting.Builder()
            .name("防止进灵魂火")
            .description("防止您走进灵魂之火")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> campfire = sgGeneral.add(new BoolSetting.Builder()
            .name("防进普通篝火")
            .description("防止您走进普通篝火")
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> soulCampfire = sgGeneral.add(new BoolSetting.Builder()
            .name("防进灵魂篝火")
            .description("防止您走进灵魂篝火。")
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> lava = sgGeneral.add(new BoolSetting.Builder()
            .name("防止你喝岩浆")
            .description("防止您走进岩浆")
            .defaultValue(false)
            .build()
    );

    public SafeFire() {
        super(NumbyHack.CATEGORY, "远离火", "防止您走进火焰");
    }

    @EventHandler
    public void onCollisionShape(CollisionShapeEvent event) {
        if (event.state.getBlock() instanceof FireBlock && fire.get()) {
            event.shape = VoxelShapes.fullCube();
        }

        if (event.state.getBlock() instanceof SoulFireBlock && soulFire.get()) {
            event.shape = VoxelShapes.fullCube();
        }

        if (event.state.getBlock() == Blocks.CAMPFIRE && campfire.get()) {
            event.shape = VoxelShapes.fullCube();
        }

        if (event.state.getBlock() == Blocks.SOUL_CAMPFIRE && soulCampfire.get()) {
            event.shape = VoxelShapes.fullCube();
        }

        if (event.state.getBlock() == Blocks.LAVA && lava.get()) {
            event.shape = VoxelShapes.fullCube();
        }
    }
}

