package widecat.meteorcrashaddon.modules;

import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.world.PlaySoundEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.network.packet.c2s.play.BoatPaddleStateC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import widecat.meteorcrashaddon.CrashAddon;

public class EntityCrash extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Modes> mode = sgGeneral.add(new EnumSetting.Builder<Modes>()
        .name("模式")
        .description("模式")
        .defaultValue(Modes.Position)
        .build());

    private final Setting<Double> speed = sgGeneral.add(new DoubleSetting.Builder()
        .name("速度")
        .description("每秒钟移动的方块数")
        .defaultValue(1337)
        .sliderRange(50, 10000)
        .visible(() -> mode.get() == Modes.Movement)
        .build());

    private final Setting<Integer> amount = sgGeneral.add(new IntSetting.Builder()
        .name("发包数量")
        .description("每刻发送的数据包数")
        .defaultValue(2000)
        .sliderRange(100, 10000)
        .build());

    private final Setting<Boolean> noSound = sgGeneral.add(new BoolSetting.Builder()
        .name("关闭声音")
        .description("阻止桨的声音")
        .defaultValue(false)
        .visible(() -> mode.get() == Modes.Boat)
        .build());

    private final Setting<Boolean> autoDisable = sgGeneral.add(new BoolSetting.Builder()
        .name("踢出关闭")
        .description("在被踢出时禁用模块")
        .defaultValue(true)
        .build());

    public EntityCrash() {
        super(CrashAddon.CATEGORY, "实体崩溃", "当你骑乘一个实体时，会发生一些有趣的事情");
    }

    @EventHandler
    public void onTick(TickEvent.Post event) {
        Entity vehicle = mc.player.getVehicle();
        if (vehicle == null) {
            error("你必须正在骑乘一个实体，切换中");
            toggle();
            return;
        }

        switch (mode.get()) {
            case Boat -> {
                if (!(vehicle instanceof BoatEntity)) {
                    error("你必须在船上，切换中");
                    toggle();
                }
                for (int i = 0; i < amount.get(); i++) {
                    mc.getNetworkHandler().sendPacket(new BoatPaddleStateC2SPacket(true, true));
                }
            }
            case Movement -> {
                for (int i = 0; i < amount.get(); i++) {
                    Vec3d v = vehicle.getPos();
                    vehicle.setPos(v.x, v.y + speed.get(), v.z);
                    mc.getNetworkHandler().sendPacket(new VehicleMoveC2SPacket(vehicle));
                }
            }
            case Position -> {
                BlockPos start = mc.player.getBlockPos();
                Vec3d end = new Vec3d(start.getX() + .5, start.getY() + 1, start.getZ() + .5);
                vehicle.updatePosition(end.x, end.y - 1, end.z);
                for (int i = 0; i < amount.get(); i++) {
                    mc.getNetworkHandler().sendPacket(new VehicleMoveC2SPacket(vehicle));
                }
            }
        }
    }

    @EventHandler
    private void onPlaySound(PlaySoundEvent event) {
        if (noSound.get() && event.sound.getId().toString().equals("minecraft:entity.boat.paddle_land") || event.sound.getId().toString().equals("minecraft:entity.boat.paddle_water")) {
            event.cancel();
        }
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        if (autoDisable.get()) toggle();
    }

    public enum Modes {
        Boat, Position, Movement
    }
}
