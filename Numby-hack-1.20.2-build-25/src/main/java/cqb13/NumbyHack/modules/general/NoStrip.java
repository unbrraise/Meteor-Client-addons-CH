package cqb13.NumbyHack.modules.general;

import cqb13.NumbyHack.NumbyHack;
import meteordevelopment.meteorclient.events.entity.player.InteractBlockEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Names;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
/**
 * made by cqb13
 */
public class NoStrip extends Module {
    private final SettingGroup sgBlocks = settings.createGroup("Blocks");

    private final Setting<Boolean> swingHand = sgBlocks.add(new BoolSetting.Builder()
            .name("挥动手臂")
            .description("渲染挥动手臂动画")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> chatFeedback = sgBlocks.add(new BoolSetting.Builder()
            .name("通知")
            .description("当您尝试扒木头衣服时，在聊天中通知您")
            .defaultValue(false)
            .build()
    );

    public NoStrip() {
        super(NumbyHack.CATEGORY, "防止削木头(没用)", "防止给木头衣服扒了");
    }

    @EventHandler
    private void onInteractBlock(InteractBlockEvent event) {
        if (!shouldInteractBlock(event.result)) event.cancel();
    }

    private boolean shouldInteractBlock(BlockHitResult hitResult) {
        if(mc.player.getMainHandStack().getItem().toString().contains("axe")){
            if (mc.crosshairTarget.getType() == HitResult.Type.BLOCK) {
                BlockPos pos = ((BlockHitResult) mc.crosshairTarget).getBlockPos();
                String result = Names.get(mc.world.getBlockState(pos).getBlock());
                if (result.contains("Log")){
                    if (swingHand.get()) mc.player.swingHand(mc.player.getActiveHand());
                    if (chatFeedback.get()) info("木头：不要扒我衣服啊！");
                    return false;
                }
            }
        }
        return true;
    }
}