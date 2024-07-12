package nekiplay.meteorplus.settings.items;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.gui.utils.IScreenFactory;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.utils.misc.IChangeable;
import meteordevelopment.meteorclient.utils.misc.ICopyable;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;

public class ESPItemData implements ICopyable<ESPItemData>, ISerializable<ESPItemData>, IChangeable, IItemData<ESPItemData>, IScreenFactory {
	public ShapeMode shapeMode;
	public SettingColor lineColor;
	public SettingColor sideColor;

	public boolean tracer;
	public SettingColor tracerColor;

	private boolean changed;

	public ESPItemData(ShapeMode shapeMode, SettingColor lineColor, SettingColor sideColor, boolean tracer, SettingColor tracerColor) {
		this.shapeMode = shapeMode;
		this.lineColor = lineColor;
		this.sideColor = sideColor;

		this.tracer = tracer;
		this.tracerColor = tracerColor;
	}

	@Override
	public WidgetScreen createScreen(GuiTheme theme, Item block, ItemDataSetting<ESPItemData> setting) {
		return new ESPItemDataScreen(theme, this, block, setting);
	}

	@Override
	public WidgetScreen createScreen(GuiTheme theme) {
		return new ESPItemDataScreen(theme, this, null, null);
	}

	@Override
	public boolean isChanged() {
		return changed;
	}

	public void changed() {
		changed = true;
	}

	public void tickRainbow() {
		lineColor.update();
		sideColor.update();
		tracerColor.update();
	}

	@Override
	public ESPItemData set(ESPItemData value) {
		shapeMode = value.shapeMode;
		lineColor.set(value.lineColor);
		sideColor.set(value.sideColor);

		tracer = value.tracer;
		tracerColor.set(value.tracerColor);

		changed = value.changed;

		return this;
	}

	@Override
	public ESPItemData copy() {
		return new ESPItemData(shapeMode, new SettingColor(lineColor), new SettingColor(sideColor), tracer, new SettingColor(tracerColor));
	}

	@Override
	public NbtCompound toTag() {
		NbtCompound tag = new NbtCompound();

		tag.putString("shapeMode", shapeMode.name());
		tag.put("lineColor", lineColor.toTag());
		tag.put("sideColor", sideColor.toTag());

		tag.putBoolean("tracer", tracer);
		tag.put("tracerColor", tracerColor.toTag());

		tag.putBoolean("changed", changed);

		return tag;
	}

	@Override
	public ESPItemData fromTag(NbtCompound tag) {
		shapeMode = ShapeMode.valueOf(tag.getString("shapeMode"));
		lineColor.fromTag(tag.getCompound("lineColor"));
		sideColor.fromTag(tag.getCompound("sideColor"));

		tracer = tag.getBoolean("tracer");
		tracerColor.fromTag(tag.getCompound("tracerColor"));

		changed = tag.getBoolean("changed");

		return this;
	}
}
