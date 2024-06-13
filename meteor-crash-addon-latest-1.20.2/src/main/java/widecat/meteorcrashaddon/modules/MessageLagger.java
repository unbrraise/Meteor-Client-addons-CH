package widecat.meteorcrashaddon.modules;

import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import widecat.meteorcrashaddon.CrashAddon;

import java.util.List;
import java.util.Random;

public class MessageLagger extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> messageLength = sgGeneral.add(new IntSetting.Builder()
        .name("消息长度")
        .description("消息的长度")
        .defaultValue(200)
        .min(1)
        .sliderMin(1)
        .sliderMax(1000)
        .build());

    private final Setting<Boolean> keepSending = sgGeneral.add(new BoolSetting.Builder()
        .name("持续发送")
        .description("不断重复发送滞后消息")
        .defaultValue(false)
        .build());

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("延迟")
        .description("持续发送消息之间的延迟（以刻度为单位）")
        .defaultValue(100)
        .min(0)
        .sliderMax(1000)
        .visible(keepSending::get)
        .build());

    private final Setting<Boolean> whisper = sgGeneral.add(new BoolSetting.Builder()
        .name("私聊")
        .description("向服务器上的随机人员私聊延迟消息")
        .defaultValue(false)
        .build());

    private final Setting<Boolean> autoDisable = sgGeneral.add(new BoolSetting.Builder()
        .name("踢出关闭")
        .description("在被踢出时禁用模块")
        .defaultValue(true)
        .build());

    public MessageLagger() {
        super(CrashAddon.CATEGORY, "消息崩溃", "发送密集消息，导致服务器上其他玩家延迟");
    }

    private int timer;

    @Override
    public void onActivate() {
        if (Utils.canUpdate() && !keepSending.get()) {
            if (!whisper.get()) {
                sendLagMessage();
            }
            else {
                sendLagWhisper();
            }
            toggle();
        }
        if (keepSending.get()) timer = delay.get();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (timer <= 0) {
            if (Utils.canUpdate() && keepSending.get()) {
                if (!whisper.get()) {
                    sendLagMessage();
                }
                else {
                    sendLagWhisper();
                }
            }
            timer = delay.get();
        }
        else {
            timer--;
        }
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        if (autoDisable.get() && isActive()) toggle();
    }

    private void sendLagMessage() {
        String message = generateLagMessage();
         ChatUtils.sendPlayerMsg(message);
    }

    private void sendLagWhisper() {
        List<AbstractClientPlayerEntity> players = mc.world.getPlayers();
        PlayerEntity player = players.get(new Random().nextInt(players.size()));
        String message = generateLagMessage();

         ChatUtils.sendPlayerMsg("/msg " + player.getGameProfile().getName() + " " + message);
    }

    private String generateLagMessage() {
        String message = null;
        for (int i = 0; i < messageLength.get(); i++) {
            message += (char) (Math.floor(Math.random() * 0x1D300) + 0x800);
        }
        return message;
    }
}
