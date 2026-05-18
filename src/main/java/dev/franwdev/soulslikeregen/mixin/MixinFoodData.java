package dev.franwdev.soulslikeregen.mixin;

import dev.franwdev.soulslikeregen.capability.RegenCapProvider;
import dev.franwdev.soulslikeregen.feedback.FeedbackHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FoodData.class)
public abstract class MixinFoodData {

    /**
     * Intercepts the moment just before player.heal() would be called.
     *
     * When ci.cancel() is called here, FoodData.tick() returns immediately, which means:
     * - player.heal() is not called (the inject fires instead)
     * - this.addExhaustion() which comes AFTER in the same block is also skipped
     * - Result: neither healing nor food consumption occurs (UHC-style behavior)
     *
     * This is correct: there is no point spending food on a heal that will never happen.
     */
    @Inject(
        method = "tick(Lnet/minecraft/world/entity/player/Player;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;heal(F)V"
        ),
        cancellable = true
    )
    private void soulslikeregen$interceptHeal(Player tickPlayer, CallbackInfo ci) {
        // Use the method parameter directly — no @Shadow needed
        RegenCapProvider.get(tickPlayer).ifPresent(cap -> {
            if (cap.isExhausted()) {
                // Block the heal — food/saturation cost already applied by FoodData above.
                ci.cancel();

                // Throttle the "exhausted" feedback message (send at most once every 5 seconds)
                if (cap.getExhaustedMessageCooldown() <= 0) {
                    FeedbackHelper.sendExhaustedFeedback((ServerPlayer) tickPlayer);
                    cap.setExhaustedMessageCooldown(100); // 100 ticks = 5 seconds
                }
            }
            // If not exhausted, the heal proceeds normally.
            // The fatigue accumulation from the heal is handled in a separate
            // PlayerTickEvent that detects health changes.
        });
    }
}
