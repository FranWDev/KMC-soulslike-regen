package dev.franwdev.soulslikeregen.level;

import java.util.List;
import net.minecraft.server.level.ServerPlayer;

import dev.franwdev.soulslikeregen.capability.IRegenCap;
import dev.franwdev.soulslikeregen.config.RegenConfig;
import dev.franwdev.soulslikeregen.feedback.FeedbackHelper;

public class LevelUpHandler {

    public static void checkLevelUp(ServerPlayer player, IRegenCap cap) {
        float spent = cap.getTotalFatigueSpent();
        int curLevel = cap.getCurrentLevel();
        List<RegenConfig.LevelDefinition> defs = RegenConfig.LEVELS;

        boolean leveledUp = false;
        float originalMaxCap = cap.getMaxCap();
        float newMaxCap = originalMaxCap;
        int nextLevel = curLevel + 1;

        while (nextLevel - 1 < defs.size()) {
            RegenConfig.LevelDefinition def = defs.get(nextLevel - 1);
            if (spent >= def.fatigueThreshold()) {
                newMaxCap += def.capacityIncrease();
                cap.setCurrentLevel(nextLevel);
                leveledUp = true;
                nextLevel++;
            } else {
                break;
            }
        }

        if (leveledUp) {
            cap.setMaxCap(newMaxCap);
            FeedbackHelper.sendLevelUp(player, cap.getCurrentLevel(), newMaxCap, newMaxCap - originalMaxCap);
        }
    }
}
