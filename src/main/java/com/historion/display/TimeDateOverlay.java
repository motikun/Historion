package com.historion.display;

import com.historion.Historion;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Historion.MOD_ID, value = Dist.CLIENT)
public class TimeDateOverlay {

    private static final int[] DAYS_IN_MONTH = { 31,28,31,30,31,30,31,31,30,31,30,31 };
    private static final String[] WEEKDAYS = { "日", "月", "火", "水", "木", "金", "土" };

    private static String getSeason(int month) {
        switch (month) {
            case 6, 7, 8, 9 -> { return "夏"; }
            case 10, 11 -> { return "秋"; }
            case 12, 1, 2, 3 -> { return "冬"; }
            case 4, 5 -> { return "春"; }
            default -> { return ""; }
        }
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null || mc.options.hideGui) return;

        long dayTime = level.getDayTime();
        int ticksInDay = (int)(level.getDayTime() % 24000L);
        int adjustedTicks = (ticksInDay + 6000) % 24000;

        // 時間計算
        int hours = (adjustedTicks * 24) / 24000;
        int minutes = ((adjustedTicks * 1440) / 24000) % 60;
        String timeStr = String.format("%02d:%02d", hours, minutes);

        // 日付計算
        int totalDays = (int)(dayTime / 24000L);
        int year = 1, month = 1, day = 1;
        int daysLeft = totalDays;

        while (true) {
            for (int i = 0; i < 12; i++) {
                if (daysLeft < DAYS_IN_MONTH[i]) {
                    month = i + 1;
                    day = daysLeft + 1;
                    break;
                } else {
                    daysLeft -= DAYS_IN_MONTH[i];
                }
            }
            if (daysLeft < 365) break;
            year++;
            daysLeft -= 365;
        }

        String weekday = WEEKDAYS[totalDays % 7];
        String dateStr = String.format("%d年目 %d月 %d日（%s）", year, month, day, weekday);

        // 描画
        GuiGraphics graphics = event.getGuiGraphics();
        PoseStack poseStack = graphics.pose();

        poseStack.pushPose();
        graphics.drawString(mc.font, "時間: " + timeStr, 5, 5, 0xFFFFFF);
        graphics.drawString(mc.font, "日付: " + dateStr, 5, 15, 0xFFFFFF);
        graphics.drawString(mc.font, "季節: " + getSeason(month), 5, 25, 0xFFFFFF);
        poseStack.popPose();
    }
}