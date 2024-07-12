package nekiplay.meteorplus.features.modules;

import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import nekiplay.meteorplus.MeteorPlus;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.text.Text;
import meteordevelopment.meteorclient.events.entity.EntityAddedEvent;
import meteordevelopment.orbit.EventHandler;
import java.util.Objects;

public class AutoLeave extends Module {
	public AutoLeave() {
		super(MeteorPlus.CATEGORY, "自动登出", "在有玩家进入你的渲染距离后自动登出");
	}
	private final SettingGroup ALSettings = settings.createGroup("Auto Leave Settings");
	private final Setting<Boolean> visualRangeIgnoreFriends = ALSettings.add(new BoolSetting.Builder()
		.name("忽略好友")
		.description("忽略你的好友")
		.defaultValue(true)
		.build()
	);

	private final Setting<Boolean> AutoDisable = ALSettings.add(new BoolSetting.Builder()
		.name("自动关闭")
		.description("检测到玩家后禁用该功能")
		.defaultValue(true)
		.build()
	);

	private final Setting<Boolean> Command = ALSettings.add(new BoolSetting.Builder()
		.name("不登出")
		.description("发送聊天框命令或者是聊天,而不是登出")
		.defaultValue(false)
		.build()
	);

	private final Setting<String> command_str = ALSettings.add(new StringSetting.Builder()
		.name("命令:")
		.description("聊天框命令")
		.defaultValue("/spawn")
		.visible(Command::get)
		.build()
	);

	@EventHandler
	public void onEntityAdded(EntityAddedEvent event) {
		if (mc.player == null) return;
		if (visualRangeIgnoreFriends.get()) {
			if (event.entity.isPlayer() && !Friends.get().isFriend((PlayerEntity) event.entity) && !Objects.equals(event.entity.getEntityName(), mc.player.getEntityName()) && !Objects.equals(event.entity.getEntityName(), "FreeCamera")) {
				if (Command.get()) {
					ChatUtils.sendPlayerMsg(command_str.get());
					info((String.format("player §c%s§r was detected", event.entity.getEntityName())));
				} else {
					mc.world.disconnect();
					mc.player.networkHandler.onDisconnect(new DisconnectS2CPacket(Text.literal(String.format("[§dAuto Leaeve§r] player %s was detected", event.entity.getEntityName()))));
				}
			if (AutoDisable.get()) this.toggle();
			}
		}
		else if (event.entity.isPlayer()){
				mc.player.networkHandler.onDisconnect(new DisconnectS2CPacket(Text.literal(String.format("[§dAuto Leaeve§r] player %s was detected", event.entity.getEntityName()))));
				if (AutoDisable.get()) this.toggle();
		}
	}
}
