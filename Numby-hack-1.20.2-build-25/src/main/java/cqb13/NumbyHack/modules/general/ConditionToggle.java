package cqb13.NumbyHack.modules.general;

import cqb13.NumbyHack.NumbyHack;
import meteordevelopment.meteorclient.events.entity.DamageEvent;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ModuleListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.DeathMessageS2CPacket;

import java.util.List;

public class ConditionToggle extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> death = sgGeneral.add(new BoolSetting.Builder()
            .name("死亡切换")
            .description("死亡时切换模块")
            .defaultValue(false)
            .build()
    );

    private final Setting<List<Module>> deathOnToggleModules = sgGeneral.add(new ModuleListSetting.Builder()
            .name("死亡时开启模块")
            .description("死亡时要激活哪些模块")
            .visible(death::get)
            .build()
    );

    private final Setting<List<Module>> deathOffToggleModules = sgGeneral.add(new ModuleListSetting.Builder()
            .name("死亡时关闭模块")
            .description("死亡时要关闭哪些模块")
            .visible(death::get)
            .build()
    );

    private final Setting<Boolean> logout = sgGeneral.add(new BoolSetting.Builder()
            .name("登出切换")
            .description("登出时切换模块")
            .defaultValue(false)
            .build()
    );

    private final Setting<List<Module>> logoutOnToggleModules = sgGeneral.add(new ModuleListSetting.Builder()
            .name("登出时开启模块")
            .description("注销时要激活哪些模块")
            .visible(logout::get)
            .build()
    );

    private final Setting<List<Module>> logoutOffToggleModules = sgGeneral.add(new ModuleListSetting.Builder()
            .name("登出时关闭模块")
            .description("注销时要关闭哪些模块")
            .visible(logout::get)
            .build()
    );

    private final Setting<Boolean> damage = sgGeneral.add(new BoolSetting.Builder()
            .name("伤害切换")
            .description("受到伤害时切换模块")
            .defaultValue(false)
            .build()
    );

    private final Setting<List<Module>> damageOnToggleModules = sgGeneral.add(new ModuleListSetting.Builder()
            .name("受伤时开启")
            .description("受伤时要激活哪些模块")
            .visible(damage::get)
            .build()
    );

    private final Setting<List<Module>> damageOffToggleModules = sgGeneral.add(new ModuleListSetting.Builder()
            .name("受伤时关闭")
            .description("受伤时要关闭哪些模块")
            .visible(damage::get)
            .build()
    );

    private final Setting<Boolean> player = sgGeneral.add(new BoolSetting.Builder()
            .name("玩家切换")
            .description("当玩家进入您的渲染距离时切换模块")
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> ignoreFriends = sgGeneral.add(new BoolSetting.Builder()
            .name("忽略好友")
            .description("忽略好友进入您的渲染距离")
            .defaultValue(true)
            .visible(player::get)
            .build()
    );

    private final Setting<List<Module>> playerOnToggleModules = sgGeneral.add(new ModuleListSetting.Builder()
            .name("玩家进入时切换")
            .description("玩家进入时要激活哪些模块")
            .visible(player::get)
            .build()
    );

    private final Setting<List<Module>> playerOffToggleModules = sgGeneral.add(new ModuleListSetting.Builder()
            .name("玩家离开时切换")
            .description("玩家离开时要关闭哪些模块")
            .visible(player::get)
            .build()
    );

    public ConditionToggle() {
        super(NumbyHack.CATEGORY, "模块切换", "根据条件切换模块");
    }

    //death toggle
    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event)  {
        if (event.packet instanceof DeathMessageS2CPacket packet) {
            Entity entity = mc.world.getEntityById(packet.getEntityId());
            if (entity == mc.player && death.get()) {
                toggleModules(deathOnToggleModules.get(), deathOffToggleModules.get());
            }
        }
    }

    //damage toggle
    @EventHandler
    private void onDamage(DamageEvent event) {
        if (event.entity.getUuid() == null) return;
        if (!event.entity.getUuid().equals(mc.player.getUuid())) return;

        if (damage.get()) {
            toggleModules(damageOnToggleModules.get(), damageOffToggleModules.get());
        }
    }

    //logout toggle
    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        if (logout.get()) {
            toggleModules(logoutOffToggleModules.get(), logoutOnToggleModules.get());
        }
    }

    //player toggle
    @EventHandler
    private void onTick(TickEvent.Post event) {
        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof PlayerEntity) {
                if (entity.getUuid() != mc.player.getUuid()) {
                    if (!ignoreFriends.get() && entity != mc.player) {
                        if (player.get()) {
                            toggleModules(playerOnToggleModules.get(), playerOffToggleModules.get());
                        }
                    } else if (ignoreFriends.get() && !Friends.get().isFriend((PlayerEntity) entity)) {
                        if (player.get()) {
                            toggleModules(playerOnToggleModules.get(), playerOffToggleModules.get());
                        }
                    }
                }
            }
        }
    }
    private void toggleModules(List<Module> onModules, List<Module> offModules) {
        for (Module module : offModules) {
            if (module.isActive()) {
                module.toggle();
            }
        }
        for (Module module : onModules) {
            if (!module.isActive()) {
                module.toggle();
            }
        }
    }
}
