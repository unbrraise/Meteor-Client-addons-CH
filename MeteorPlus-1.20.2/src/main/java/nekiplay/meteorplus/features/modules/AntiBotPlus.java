package nekiplay.meteorplus.features.modules;

import meteordevelopment.meteorclient.events.entity.EntityAddedEvent;
import meteordevelopment.meteorclient.events.entity.EntityRemovedEvent;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import nekiplay.meteorplus.MeteorPlus;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import nekiplay.meteorplus.utils.ColorRemover;

import java.util.*;

public class AntiBotPlus extends Module {
	public AntiBotPlus() {
		super(MeteorPlus.CATEGORY, "反机器人检测(机翻)", "忽略杀戮光环、ESP、Tracers的机器人");
	}

	/* Thanks LiquidBounce
		https://github.com/CCBlueX/LiquidBounce/blob/legacy/src/main/java/net/ccbluex/liquidbounce/features/module/modules/misc/AntiBot.kt
	 */

	private final SettingGroup sgFilters = settings.createGroup("Filters");

	public enum TabMode {
		Equals,
		Contains,
		Contains_LowerCase;

		@Override
		public String toString() {
			return super.toString().replace('_', ' ');
		}
	}

	private final Setting<Boolean> tab = sgFilters.add(new BoolSetting.Builder()
		.name("标签")
		.description("检查标签")
		.defaultValue(true)
		.build()
	);

	private final Setting<TabMode> tabMode = sgFilters.add(new EnumSetting.Builder<TabMode>()
		.name("标签模式")
		.description("检查标签模式")
		.defaultValue(TabMode.Contains)
		.visible(tab::get)
		.build()
	);

	private final Setting<Boolean> entityID = sgFilters.add(new BoolSetting.Builder()
		.name("实体ID")
		.description("检查实体ID")
		.defaultValue(true)
		.build()
	);

	private final Setting<Boolean> color = sgFilters.add(new BoolSetting.Builder()
		.name("颜色")
		.description("检查颜色")
		.defaultValue(false)
		.build()
	);

	private final Setting<Boolean> ground = sgFilters.add(new BoolSetting.Builder()
		.name("地面")
		.description("检查地面")
		.defaultValue(true)
		.build()
	);

	private final Setting<Boolean> air = sgFilters.add(new BoolSetting.Builder()
		.name("空气中")
		.description("检查空中")
		.defaultValue(false)
		.build()
	);

	private final Setting<Boolean> InvalidGround = sgFilters.add(new BoolSetting.Builder()
		.name("无效地面")
		.description("检查无效地面状态")
		.defaultValue(true)
		.build()
	);

	private final Setting<Boolean> swing = sgFilters.add(new BoolSetting.Builder()
		.name("摆动状态")
		.description("检查摆动状态")
		.defaultValue(false)
		.build()
	);

	private final Setting<Boolean> health = sgFilters.add(new BoolSetting.Builder()
		.name("生命")
		.description("检查生命")
		.defaultValue(false)
		.build()
	);

	private final Setting<Boolean> derp = sgFilters.add(new BoolSetting.Builder()
		.name("derp")
		.description("检查derp")
		.defaultValue(true)
		.build()
	);

	private final Setting<Boolean> useHash = sgFilters.add(new BoolSetting.Builder()
		.name("记录识别的机器人")
		.description("阻止攻击和blinking ESP")
		.defaultValue(true)
		.build()
	);

	private ArrayList<Integer> hash = new ArrayList<Integer>();
	private ArrayList<Integer> swings = new ArrayList<Integer>();
	private ArrayList<Integer> grounds = new ArrayList<Integer>();
	private ArrayList<Integer> airs = new ArrayList<Integer>();
	private Map<Integer, Integer> invalidGrounds  = new HashMap<>();

	@Override
	public void onDeactivate() {
		hash.clear();
	}

	public boolean isBot(Entity entity) {
		if (entity instanceof LivingEntity living) {
			return isBot(living);
		}
		return false;
	}

	public boolean isBot(LivingEntity entity) {
		if (!(entity instanceof PlayerEntity))
			return false;
		if (!isActive())
			return false;

		if (useHash.get() && hash.contains(entity.getId())) {
			return true;
		}

		if (color.get() && entity.getDisplayName().getString().replace("§r", "").contains("§")) {
			if (useHash.get()) {
				hash.add(entity.getId());
			}
			return true;
		}

		if (ground.get() && !grounds.contains(entity.getId())) {
			if (useHash.get()) {
				hash.add(entity.getId());
			}
			return true;
		}

		if (InvalidGround.get() && invalidGrounds.getOrDefault(entity.getId(), 0) >= 10) {
			if (useHash.get()) {
				hash.add(entity.getId());
			}
			return true;
		}

		if (entityID.get() && (entity.getId() >= 1000000000 || entity.getId() <= -1)) {
			if (useHash.get()) {
				hash.add(entity.getId());
			}
			return true;
		}

		if (derp.get() && (entity.getPitch() > 90f || entity.getPitch() < -90)) {
			if (useHash.get()) {
				hash.add(entity.getId());
			}
			return true;
		}

		if (swing.get() && !swings.contains(entity.getId())) {
			if (useHash.get()) {
				hash.add(entity.getId());
			}
			return true;
		}


		if (tab.get()) {
			String targetname = ColorRemover.GetVerbatim(entity.getDisplayName().getString());
			if (mc != null && mc.getNetworkHandler() != null) {
				Collection<PlayerListEntry> entryCollection = mc.getNetworkHandler().getPlayerList();
				for (PlayerListEntry info : entryCollection) {
					if (info.getDisplayName() != null) {
						String networkName = ColorRemover.GetVerbatim(info.getDisplayName().getString());
						if (tabMode.get() == TabMode.Equals) {
							if (targetname.equals(networkName)) {
								return false;
							}
						}
						else if (tabMode.get() == TabMode.Contains_LowerCase) {
							if (targetname.toLowerCase().contains(networkName.toLowerCase())) {
								return false;
							}
						}
						else {
							if (targetname.contains(networkName)) {
								return false;
							}
						}
					}
				}
				if (useHash.get()) {
					hash.add(entity.getId());
				}
				return true;
			}
		}

		return entity.getName().getString().isEmpty() || entity.getName() == mc.player.getName();
	}

	@EventHandler
	private void onEntityAdd(EntityAddedEvent event) {
		isBot(event.entity);
	}

	@EventHandler
	private void onEntityRemove(EntityRemovedEvent event) {
		if (hash.contains(event.entity.getId())) {
			Iterator<Integer> iterator = hash.iterator();
			while (iterator.hasNext()) {
				if (iterator.next() == event.entity.getId()) {
					iterator.remove();
					return;
				}
			}
		}
	}

	@EventHandler
	private void livingEntityMove(PacketEvent.Receive event) {
		if (event.packet instanceof EntityPositionS2CPacket packet) {
			if (mc.world != null) {
				Entity entity = mc.world.getEntityById(packet.getId());
				if (entity != null) {
					if (entity.isOnGround()) {
						grounds.add(entity.getId());
					}

					if (!entity.isOnGround() && !airs.contains(entity.getId()))
						airs.add(entity.getId());

					if (entity.isOnGround()) {
						if (entity.prevY != entity.getY())
							invalidGrounds.put(entity.getId(), invalidGrounds.getOrDefault(entity.getId(), 0) + 1);
					} else {
						int currentVL = invalidGrounds.getOrDefault(entity.getId(), 0) / 2;
						if (currentVL <= 0)
							invalidGrounds.remove(entity.getId());
						else
							//invalidGrounds.put(entity.getId(), invalidGrounds.getOrDefault(entity.getId(), currentVL));
							invalidGrounds.replace(entity.getId(), currentVL);
					}
				}
			}
		}
		else if (event.packet instanceof EntityAnimationS2CPacket packet) {
			Entity entity = mc.world.getEntityById(packet.getId());
			if (entity != null) {
				if (entity instanceof LivingEntity && packet.getAnimationId() == 0 && !swings.contains(entity.getId()))
					swings.add(entity.getId());
			}
		}
	}

	@EventHandler
	public void worldEvent(GameLeftEvent event) {
		clearAll();
	}

	private void clearAll() {
		swings.clear();
		grounds.clear();
		invalidGrounds.clear();
	}
}
