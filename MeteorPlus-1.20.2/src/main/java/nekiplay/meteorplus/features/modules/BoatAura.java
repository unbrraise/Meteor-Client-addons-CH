package nekiplay.meteorplus.features.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import nekiplay.meteorplus.MeteorPlus;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Hand;

public class BoatAura extends Module {
	public BoatAura() {
		super(MeteorPlus.CATEGORY, "上船光环", "自动坐船");
	}

	private final SettingGroup boatAuraSettings = settings.createGroup("Boat Aura Settings");

	private final Setting<Boolean> singleUse = boatAuraSettings.add(new BoolSetting.Builder()
		.name("仅用一次")
		.description("第一次使用后禁用模块")
		.defaultValue(false)
		.build()
	);

	private final Setting<Boolean> onlyInAir = boatAuraSettings.add(new BoolSetting.Builder()
		.name("仅空中")
		.description("仅在空中与船互动")
		.defaultValue(false)
		.build()
	);

	private final Setting<Boolean> onlyIfFallDamade = boatAuraSettings.add(new BoolSetting.Builder()
		.name("防摔伤")
		.description("防止跌落伤害")
		.defaultValue(false)
		.visible(onlyInAir::get)
		.build()
	);


	private final Setting<Integer> delay = boatAuraSettings.add(new IntSetting.Builder()
		.name("延迟")
		.description("移动延迟")
		.defaultValue(8)
		.min(0)
		.visible(() -> !singleUse.get())
		.sliderRange(0, 80)
		.build()
	);

	long mils = 0;
	@Override
	public void onActivate() {

	}

	@EventHandler
	private void onTickEvent(TickEvent.Pre event) {
		assert mc.world != null;
		for (Entity entity : mc.world.getEntities()) {
			if (mc.player != null && entity.getType() == EntityType.BOAT) {
				if (mc.interactionManager != null && mils == 0) {
					if (onlyInAir.get() && mc.player.isOnGround()) return;
					if (onlyInAir.get() && onlyIfFallDamade.get() && mc.player.isOnGround() && mc.player.fallDistance <= 2) return;

					mc.player.teleport(entity.getBlockPos().getX(), entity.getBlockPos().getX() + 1, entity.getBlockPos().getZ());
					mc.interactionManager.interactEntity(mc.player, entity, Hand.MAIN_HAND);

					if (singleUse.get()) {
						toggle();
						info("Disabled");
						return;
					}

					mils = delay.get();
				}
				else {
					mils--;
				}
			}
		}
	}
}
