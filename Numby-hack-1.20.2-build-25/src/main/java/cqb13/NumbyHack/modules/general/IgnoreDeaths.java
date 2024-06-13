package cqb13.NumbyHack.modules.general;

import cqb13.NumbyHack.NumbyHack;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringListSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.text.Text;

import java.util.List;

public class IgnoreDeaths extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<String>> names = sgGeneral.add(new StringListSetting.Builder()
            .name("玩家姓名")
            .description("您希望隐藏其死亡消息的玩家名称")
            .defaultValue(List.of())
            .build()
    );

    private final Setting<Boolean> mustContainWords = sgGeneral.add(new BoolSetting.Builder()
            .name("必须包含的词语")
            .description("仅当消息包含指定的单词和玩家名称时才会忽略该消息")
            .defaultValue(false)
            .build()
    );

    private final Setting<List<String>> blockedWords = sgGeneral.add(new StringListSetting.Builder()
            .name("屏蔽词")
            .description("将导致消息被阻止的词语列表")
            .defaultValue(List.of())
            .visible(mustContainWords::get)
            .build()
    );

    public IgnoreDeaths() {
        super(NumbyHack.CATEGORY, "忽略死亡信息", "从聊天中删除包含死亡发送者名称的消息");
    }

    @EventHandler
    private void onMessageReceive(ReceiveMessageEvent event) {
        Text message = event.getMessage();

        if (message == null) return;

        message = Text.of(message.getString().toLowerCase());

        if (mustContainWords.get()) {
            for (String name : names.get()) {
                for (String word : blockedWords.get()) {
                    if (message.getString().contains(name) && message.getString().contains(word.toLowerCase())) {
                        event.cancel();
                    }
                }
            }
        } else {
            for (String name : names.get()) {
                if (message.getString().contains(name)) {
                    event.cancel();
                }
            }
        }

        event.setMessage(message);
    }
}