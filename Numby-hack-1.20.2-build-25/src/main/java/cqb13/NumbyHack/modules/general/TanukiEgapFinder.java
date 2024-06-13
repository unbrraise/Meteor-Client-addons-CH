package cqb13.NumbyHack.modules.general;

import cqb13.NumbyHack.NumbyHack;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * from Tanuki
 */
// https://gitlab.com/Walaryne/tanuki/-/blob/master/src/main/java/minegame159/meteorclient/modules/misc/EgapFinder.java

public class TanukiEgapFinder extends Module {
    private final SettingGroup sgDefault = settings.getDefaultGroup();

    private final Setting<Boolean> coords = sgDefault.add(new BoolSetting.Builder()
            .name("发送坐标信息")
            .description("发送坐标消息，以防您懒得查看您的.minecraft文件夹")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> debug = sgDefault.add(new BoolSetting.Builder()
            .name("debug")
            .description("没用,只是打印出在您的渲染距离内定位到的每个箱子的信息，会大量刷屏。")
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> playSound = sgDefault.add(new BoolSetting.Builder()
            .name("播放提示音")
            .description("当您发现箱子时播放声音")
            .defaultValue(false)
            .build()
    );

    private boolean check;
    private boolean lock;
    private int stage = 0;
    private int checkDelay;
    private int comparatorHold = 0;
    private BlockPos chest;
    private BlockPos prevChest;

    public TanukiEgapFinder() {
        super(NumbyHack.CATEGORY, "空隙查找器(机翻)应该是找箱子", "在单人世界中找箱子，并创建一个名为\"egap-coords.txt\"的文件");
    }

    private static void writeToFile(String coords) {
        try (FileWriter fw = new FileWriter("egap-coords.txt", true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(coords);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void onActivate() {
        stage = 0;
        checkDelay = 0;
        lock = true;
        if (debug.get()) ChatUtils.info("STARTING");
    }

    @Override
    public void onDeactivate() {
        if (debug.get()) ChatUtils.info("STOPPING");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        check = false;
        for (BlockEntity blockEntity : Utils.blockEntities()) {
            if (blockEntity instanceof ChestBlockEntity) {
                if (blockEntity.isRemoved()) continue;
                chest = blockEntity.getPos();

                check = true;
                lock = false;
            }
        }

        if (!check) {
            checkDelay++;
        } else {
            checkDelay = 0;
        }
        if (checkDelay >= 2) {
            lock = true;
            checkDelay = 0;
            stage = 1;
        }

        if (!lock) {
            if (stage == 0) {
                stage = 1;
            }
            switch (stage) {
                case 1: {
                    int adjacent = chest.getX() - 1;
                    Block block = mc.world.getBlockState(chest.add(-1, 0, 0)).getBlock();
                    if (block != Blocks.COMPARATOR) {;
                        ChatUtils.sendPlayerMsg("/setblock " + adjacent + " " + chest.getY() + " " + chest.getZ() + " minecraft:comparator[facing=east]");
                    }
                    stage++;
                    break;
                }
                case 2: {
                    int xAdjacent = chest.getX() - 1;
                    int yAdjacent = chest.getY() + 1;
                    Block block = mc.world.getBlockState(chest.add(-1, +1, 0)).getBlock();
                    if (block != Blocks.ACACIA_LEAVES) {
                        ChatUtils.sendPlayerMsg("/setblock " + xAdjacent + " " + yAdjacent + " " + chest.getZ() + " minecraft:acacia_leaves");
                    }
                    comparatorHold++;
                    if (comparatorHold == 3) {
                        stage++;
                        comparatorHold = 0;
                    }
                    break;
                }
                case 3: {
                    Block block = mc.world.getBlockState(chest).getBlock();
                    if (block == Blocks.CHEST) {
                        ChatUtils.sendPlayerMsg("/execute if data block " + chest.getX() + " " + chest.getY() + " " + chest.getZ() + " Items[{id:\"minecraft:enchanted_golden_apple\"}] as @p run setblock " + chest.getX() + " " + chest.getY() + " " + chest.getZ() + " minecraft:diamond_block");
                    }
                    prevChest = chest;
                    stage++;
                    break;
                }
                case 4: {
                    Block diamondPos = mc.world.getBlockState(prevChest).getBlock();
                    if (diamondPos == Blocks.DIAMOND_BLOCK) {
                        if (playSound.get()) mc.player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                        ChatUtils.info((!coords.get() ? Formatting.GREEN + "Found an egap! Wrote coords to file." : Formatting.GREEN + "Found an egap! Wrote coords to file. " + prevChest.getX() + " " + prevChest.getY() + " " + prevChest.getZ()));
                        writeToFile(prevChest.getX() + " " + prevChest.getY() + " " + prevChest.getZ());
                    } else
                        ChatUtils.sendPlayerMsg("/setblock " + chest.getX() + " " + chest.getY() + " " + chest.getZ() + " minecraft:air");
                    stage++;
                    break;
                }
            }
            if (stage == 5) {
                stage = 1;
            }
        }
    }
}