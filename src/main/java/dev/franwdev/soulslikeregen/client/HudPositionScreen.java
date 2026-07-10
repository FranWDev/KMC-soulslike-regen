package dev.franwdev.soulslikeregen.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import dev.franwdev.soulslikeregen.config.SoulslikeRegenClientConfig;
import dev.franwdev.soulslikeregen.SoulslikeRegen;
import org.lwjgl.glfw.GLFW;

public class HudPositionScreen extends Screen {

    private static final ResourceLocation EYE_TEXTURE = new ResourceLocation(SoulslikeRegen.MODID, "textures/hud/eye_0.png");

    private boolean dragging = false;
    private double dragStartX = 0;
    private double dragStartY = 0;
    private int configStartX = 0;
    private int configStartY = 0;

    public HudPositionScreen() {
        super(Component.literal("Adjust Fatigue HUD Position"));
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Draw dark background overlay
        this.renderBackground(guiGraphics);

        int screenWidth = this.width;
        int screenHeight = this.height;

        int xOffset = SoulslikeRegenClientConfig.HUD_X_OFFSET.get();
        int yOffset = SoulslikeRegenClientConfig.HUD_Y_OFFSET.get();

        int hudX = screenWidth / 2 + xOffset;
        int hudY = screenHeight + yOffset;

        // Bounding box for selection/hover feedback
        int boxX1 = hudX - 25;
        int boxY1 = hudY - 4;
        int boxX2 = hudX + 25;
        int boxY2 = hudY + 28;

        boolean hovered = mouseX >= boxX1 && mouseX <= boxX2 && mouseY >= boxY1 && mouseY <= boxY2;

        // Draw dotted/solid border around the HUD
        int borderColor = dragging ? 0xFF00FFFF : (hovered ? 0xFFFFFF00 : 0x88FFFFFF);
        guiGraphics.fill(boxX1, boxY1, boxX2, boxY1 + 1, borderColor); // Top
        guiGraphics.fill(boxX1, boxY2 - 1, boxX2, boxY2, borderColor); // Bottom
        guiGraphics.fill(boxX1, boxY1, boxX1 + 1, boxY2, borderColor); // Left
        guiGraphics.fill(boxX2 - 1, boxY1, boxX2, boxY2, borderColor); // Right

        // Draw a light background inside the box to show target area
        int bgFill = dragging ? 0x4400FFFF : (hovered ? 0x44FFFF00 : 0x22FFFFFF);
        guiGraphics.fill(boxX1 + 1, boxY1 + 1, boxX2 - 1, boxY2 - 1, bgFill);

        // Draw mock eye texture
        guiGraphics.blit(EYE_TEXTURE, hudX - 8, hudY, 0, 0, 16, 16, 16, 16);

        // Draw mock text
        Component mockText = Component.literal("40 / 80");
        int textWidth = this.font.width(mockText);
        guiGraphics.drawString(this.font, mockText, hudX - (textWidth / 2), hudY + 17, 0xFFFFFFFF, true);

        // Draw instructions screen-centered
        guiGraphics.drawCenteredString(this.font, "HUD Position Editor", screenWidth / 2, 20, 0xFFFFFF00);
        guiGraphics.drawCenteredString(this.font, "• Drag with MOUSE to reposition", screenWidth / 2, 40, 0xFFFFFFFF);
        guiGraphics.drawCenteredString(this.font, "• Use ARROW KEYS for precise adjustments", screenWidth / 2, 55, 0xFFFFFFFF);
        guiGraphics.drawCenteredString(this.font, "• Press R to Reset to Default position", screenWidth / 2, 70, 0xFFFFFFFF);
        guiGraphics.drawCenteredString(this.font, "• Press ESC to Save and Exit", screenWidth / 2, 85, 0xFFFFFFFF);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) { // Left click
            int screenWidth = this.width;
            int screenHeight = this.height;

            int xOffset = SoulslikeRegenClientConfig.HUD_X_OFFSET.get();
            int yOffset = SoulslikeRegenClientConfig.HUD_Y_OFFSET.get();

            int hudX = screenWidth / 2 + xOffset;
            int hudY = screenHeight + yOffset;

            int boxX1 = hudX - 25;
            int boxY1 = hudY - 4;
            int boxX2 = hudX + 25;
            int boxY2 = hudY + 28;

            if (mouseX >= boxX1 && mouseX <= boxX2 && mouseY >= boxY1 && mouseY <= boxY2) {
                dragging = true;
                dragStartX = mouseX;
                dragStartY = mouseY;
                configStartX = xOffset;
                configStartY = yOffset;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            dragging = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (dragging && button == 0) {
            double deltaX = mouseX - dragStartX;
            double deltaY = mouseY - dragStartY;

            int newXOffset = configStartX + (int) Math.round(deltaX);
            int newYOffset = configStartY + (int) Math.round(deltaY);

            // Clamp offsets to keep the HUD within reasonable bounds
            newXOffset = Math.min(this.width / 2, Math.max(-this.width / 2, newXOffset));
            newYOffset = Math.min(0, Math.max(-this.height, newYOffset));

            SoulslikeRegenClientConfig.HUD_X_OFFSET.set(newXOffset);
            SoulslikeRegenClientConfig.HUD_Y_OFFSET.set(newYOffset);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        int step = Screen.hasShiftDown() ? 10 : 1;
        if (keyCode == GLFW.GLFW_KEY_UP) {
            SoulslikeRegenClientConfig.HUD_Y_OFFSET.set(SoulslikeRegenClientConfig.HUD_Y_OFFSET.get() - step);
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_DOWN) {
            SoulslikeRegenClientConfig.HUD_Y_OFFSET.set(Math.min(0, SoulslikeRegenClientConfig.HUD_Y_OFFSET.get() + step));
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_LEFT) {
            SoulslikeRegenClientConfig.HUD_X_OFFSET.set(SoulslikeRegenClientConfig.HUD_X_OFFSET.get() - step);
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_RIGHT) {
            SoulslikeRegenClientConfig.HUD_X_OFFSET.set(SoulslikeRegenClientConfig.HUD_X_OFFSET.get() + step);
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_R) {
            // Reset to defaults
            SoulslikeRegenClientConfig.HUD_X_OFFSET.set(0);
            SoulslikeRegenClientConfig.HUD_Y_OFFSET.set(-59);
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            // Save client config
            SoulslikeRegenClientConfig.SPEC.save();
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
