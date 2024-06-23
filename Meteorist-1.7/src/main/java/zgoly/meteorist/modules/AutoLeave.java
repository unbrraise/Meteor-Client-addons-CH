package zgoly.meteorist.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.text.Text;
import zgoly.meteorist.Meteorist;

import java.util.List;

public class AutoLeave extends Module {
    public enum Mode {
        Logout,
        Commands
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
            .name("模式")
            .description("所使用的模式")
            .defaultValue(Mode.Logout)
            .build()
    );

    private final Setting<List<String>> commands = sgGeneral.add(new StringListSetting.Builder()
            .name("离开命令")
            .description("发送的命令")
            .defaultValue("/spawn")
            .visible(() -> mode.get() == Mode.Commands)
            .build()
    );

    private final Setting<Integer> range = sgGeneral.add(new IntSetting.Builder()
            .name("范围")
            .description("如果玩家在范围内则离开")
            .defaultValue(5)
            .min(1)
            .sliderRange(1, 10)
            .build()
    );

    private final Setting<Boolean> ignoreFriends = sgGeneral.add(new BoolSetting.Builder()
            .name("忽视好友")
            .description("不对被添加为好友的玩家做出反应")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> toggleOff = sgGeneral.add(new BoolSetting.Builder()
            .name("自动关闭")
            .description("在使用后禁用自动离开模块")
            .defaultValue(false)
            .build()
    );

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
            .name("延迟")
            .description("发送命令后的延迟时间，以刻为单位（20刻 = 1秒）")
            .defaultValue(20)
            .min(1)
            .sliderRange(1, 40)
            .build()
    );

    private int timer;
    private boolean work;

    public AutoLeave() {
        super(Meteorist.CATEGORY, "自动离开", "如果玩家在范围内，自动离开");
    }

    @Override
    public void onActivate() {
        timer = 0;
        work = true;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof PlayerEntity) {
                if (entity.getUuid() != mc.player.getUuid() && mc.player.distanceTo(entity) < range.get()) {
                    if (ignoreFriends.get() && Friends.get().isFriend((PlayerEntity) entity)) return;
                    if (work) {
                        if (mode.get() == Mode.Logout) {
                            mc.player.networkHandler.onDisconnect(new DisconnectS2CPacket(Text.of("[Auto Leave] Found player in radius.")));
                        } else if (mode.get() == Mode.Commands && !commands.get().isEmpty()) {
                            for (String command : commands.get()) ChatUtils.sendPlayerMsg(command);
                        }
                        work = !work;
                    }
                    if (!work && timer >= delay.get()) {
                        work = true;
                        timer = 0;
                    } else if (!work) timer ++;
                    if (toggleOff.get()) this.toggle();
                }
            }
        }
    }
}
