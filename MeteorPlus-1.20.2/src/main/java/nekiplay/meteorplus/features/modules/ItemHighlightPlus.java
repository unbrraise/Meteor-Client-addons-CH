package nekiplay.meteorplus.features.modules;

import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.GenericSetting;
import meteordevelopment.meteorclient.settings.ItemListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import nekiplay.meteorplus.MeteorPlus;
import nekiplay.meteorplus.settings.items.ESPItemData;
import nekiplay.meteorplus.settings.items.HiglightItemData;
import nekiplay.meteorplus.settings.items.ItemDataSetting;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

import java.util.List;
import java.util.Map;

public class ItemHighlightPlus extends Module {
	public ItemHighlightPlus() {
		super(MeteorPlus.CATEGORY, "高亮物品+", "在物品栏和GUI中高亮选定的物品");
	}

	public final SettingGroup sgGeneral = settings.getDefaultGroup();
	public final Setting<List<Item>> whitelist = sgGeneral.add(new ItemListSetting.Builder()
		.name("物品")
		.description("要高亮的物品列表")
		.defaultValue(
			Items.ELYTRA
		)
		.build()
	);

	public final Setting<HiglightItemData> defaultBlockConfig = sgGeneral.add(new GenericSetting.Builder<HiglightItemData>()
		.name("默认物品配置")
		.description("默认的物品配置")
		.defaultValue(
			new HiglightItemData(
				new SettingColor(0, 255, 200, 25)
			)
		)
		.build()
	);
	public final Setting<Map<Item, HiglightItemData>> itemsConfigs = sgGeneral.add(new ItemDataSetting.Builder<HiglightItemData>()
		.name("物品配置")
		.description("每个物品的配置")
		.defaultData(defaultBlockConfig)
		.build()
	);
}
