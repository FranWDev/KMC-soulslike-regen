package dev.franwdev.soulslikeregen.client;

import dev.franwdev.soulslikeregen.SoulslikeRegen;
import dev.franwdev.soulslikeregen.config.SoulslikeRegenClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import java.util.HashMap;
import java.util.Map;

public class FatigueHudOverlay {

    private static final int EYE_FRAME_COUNT = 12;
    private static final ResourceLocation[] EYE_FRAMES = new ResourceLocation[EYE_FRAME_COUNT];
    private static final Map<ResourceLocation, Boolean> TEXTURE_EXISTS_CACHE = new HashMap<>();
    private static final HudColorInterpolator COLOR_INTERPOLATOR = new HudColorInterpolator();

    static {
        for (int i = 0; i < EYE_FRAME_COUNT; i++) {
            EYE_FRAMES[i] = new ResourceLocation(SoulslikeRegen.MODID, "textures/hud/eye_" + i + ".png");
        }
    }

    public static final IGuiOverlay HUD = (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.player == null) {
            return;
        }

        float current = FatigueClientData.getCurrentFatigue();
        float max = FatigueClientData.getMaxCap();
        boolean exhausted = FatigueClientData.isExhausted();

        if (current == 0f && !exhausted) {
            return;
        }

        // Fetch configured offsets
        int xOffset = SoulslikeRegenClientConfig.HUD_X_OFFSET.get();
        int yOffset = SoulslikeRegenClientConfig.HUD_Y_OFFSET.get();

        int x = screenWidth / 2 + xOffset;
        int y = screenHeight + yOffset;

        int frame = getEyeFrame(current, max, exhausted);
        ResourceLocation texture = EYE_FRAMES[frame];

        // Check texture existence (with cache)
        boolean textureExists = TEXTURE_EXISTS_CACHE.computeIfAbsent(texture, tex -> 
            mc.getResourceManager().getResource(tex).isPresent()
        );

        // Blit eye texture only if it exists
        if (textureExists) {
            guiGraphics.blit(texture, x - 8, y, 0, 0, 16, 16, 16, 16);
        }

        // Draw text
        Font font = mc.font;
        Component text = Component.literal((int) Math.round(current) + " / " + (int) Math.round(max));

        // Get interpolated color based on exhausted state and recovery state
        int color = COLOR_INTERPOLATOR.getColor(exhausted, FatigueClientData.getRecoveryType());

        int textWidth = font.width(text);
        // Draw text centered below the eye icon
        guiGraphics.drawString(font, text, x - (textWidth / 2), y + 17, color, true);
    };

    private static int getEyeFrame(float fatigue, float maxCap, boolean exhausted) {
        if (exhausted) {
            return EYE_FRAME_COUNT - 1;
        }
        if (maxCap <= 0f) {
            return 0;
        }
        float ratio = Math.min(1f, Math.max(0f, fatigue / maxCap));
        return Math.round(ratio * (EYE_FRAME_COUNT - 1));
    }
}
