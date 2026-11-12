package com.tequilacat.kaleidoscope_startale.client.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import com.tequilacat.kaleidoscope_startale.config.StartaleConfig;
import com.tequilacat.kaleidoscope_startale.data.favorability.Favorability;
import com.tequilacat.kaleidoscope_startale.data.favorability.FavorabilityTracker;
import com.tequilacat.kaleidoscope_startale.data.tame.MobTaming;
import com.tequilacat.kaleidoscope_startale.data.tame.TameData;
import com.tequilacat.kaleidoscope_startale.init.KSAttachments;
import com.tequilacat.kaleidoscope_startale.init.KSItemTags;
import com.tequilacat.kaleidoscope_startale.util.PetterCooldown;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

/**
 * 驯养信息浮层。
 * <p>
 * 准星指向已驯养的生物时，在准星右侧显示一个圆角面板，
 * 自上而下依次列出版主、拍拍冷却、今日喂食、好感度与升级进度，
 * 其中喂食次数、好感度与升级进度以彩色条呈现。
 */
public final class TamedInfoOverlay implements LayeredDraw.Layer {

    /** 面板贴图。 */
    private static final ResourceLocation PANEL_TEXTURE = KaleidoscopeStartale.id("tamed_info_panel");
    /** 面板宽度。 */
    private static final int PANEL_WIDTH = 160;
    /** 面板高度。 */
    private static final int PANEL_HEIGHT = 80;
    /** 面板内边距。 */
    private static final int PADDING = 6;
    /** 标题文字相对面板顶部的偏移。 */
    private static final int TITLE_TOP = 4;
    /** 标题行高度。 */
    private static final int TITLE_HEIGHT = 13;
    /** 每行高度。 */
    private static final int ROW_HEIGHT = 11;

    /** 标签：主人。 */
    private static final Component LABEL_OWNER = Component.translatable("hud.kaleidoscope_startale.owner");
    /** 标签：拍拍冷却。 */
    private static final Component LABEL_COOLDOWN = Component.translatable("hud.kaleidoscope_startale.petter_cooldown");
    /** 标签：今日喂食。 */
    private static final Component LABEL_FEED = Component.translatable("hud.kaleidoscope_startale.feed");
    /** 标签：好感度。 */
    private static final Component LABEL_FAVOR = Component.translatable("hud.kaleidoscope_startale.favorability");
    /** 标签：升级进度。 */
    private static final Component LABEL_PROGRESS = Component.translatable("hud.kaleidoscope_startale.progress");
    /** 文本：就绪。 */
    private static final Component TEXT_READY = Component.translatable("hud.kaleidoscope_startale.ready");
    /** 文本：已满级。 */
    private static final Component TEXT_MAX_LEVEL = Component.translatable("hud.kaleidoscope_startale.max_level");
    /** 文本：未知主人。 */
    private static final Component TEXT_UNKNOWN_OWNER = Component.translatable("hud.kaleidoscope_startale.unknown_owner");

    /** 标题颜色。 */
    private static final int COLOR_TITLE = 0xFFFFD479;
    /** 标签颜色。 */
    private static final int COLOR_LABEL = 0xFFA8B0BA;
    /** 数值颜色。 */
    private static final int COLOR_VALUE = 0xFFFFFFFF;
    /** 好感度条颜色。 */
    private static final int COLOR_FAVOR = 0xFFFF7FA8;
    /** 喂食条颜色。 */
    private static final int COLOR_FEED = 0xFFFFB74D;
    /** 进度条颜色。 */
    private static final int COLOR_PROGRESS = 0xFF7FD1A6;
    /** 进度条底色。 */
    private static final int COLOR_EMPTY = 0x40FFFFFF;

    /** 当前淡入淡出程度。 */
    private static float alpha;
    /** 最近一次采样到的面板数据，用于淡出期间继续显示。 */
    private static Snapshot snapshot;

    /**
     * 面板数据快照。
     *
     * @param title        标题（生物名称）
     * @param owner        主人名称
     * @param cooldown     拍拍冷却文本
     * @param feedText     今日喂食文本
     * @param feedUsed     今日已喂食次数
     * @param feedTotal    每日喂食上限
     * @param favorText    好感度文本
     * @param favorLevel   当前等级
     * @param favorMax     最大等级
     * @param progressText 升级进度文本
     * @param progress     升级进度比例
     */
    private record Snapshot(Component title, Component owner, Component cooldown, Component feedText,
                            int feedUsed, int feedTotal, Component favorText, int favorLevel, int favorMax,
                            Component progressText, float progress) {
    }

    /**
     * 渲染面板。
     *
     * @param graphics     图形上下文
     * @param deltaTracker 帧间隔
     */
    @Override
    public void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null || minecraft.screen != null
                || minecraft.options.hideGui || minecraft.getDebugOverlay().showDebugScreen()
                || !StartaleConfig.HUD_ENABLED.get() || !wearsStrawHat(minecraft.player)) {
            alpha = 0.0F;
            return;
        }

        LivingEntity target = resolveTarget(minecraft);
        if (target != null) {
            snapshot = capture(target);
        }

        float step = Math.min(1.0F, StartaleConfig.HUD_FADE_SPEED.get().floatValue() * deltaTracker.getRealtimeDeltaTicks() / 20.0F);
        alpha = target != null ? Math.min(1.0F, alpha + step) : Math.max(0.0F, alpha - step);
        if (alpha <= 0.01F || snapshot == null) {
            return;
        }

        draw(graphics, minecraft, snapshot);
    }

    /**
     * 解析准星指向的已驯养生物。
     *
     * @param minecraft 客户端实例
     * @return 目标生物，找不到时为 null
     */
    private static LivingEntity resolveTarget(Minecraft minecraft) {
        var player = minecraft.player;
        if (player == null) {
            return null;
        }
        Vec3 start = player.getEyePosition(1.0F);
        Vec3 end = start.add(player.getViewVector(1.0F).scale(StartaleConfig.HUD_PICK_RANGE.get()));
        AABB box = player.getBoundingBox().expandTowards(end.subtract(start)).inflate(1.0D);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(player, start, end, box,
                entity -> entity instanceof LivingEntity && !entity.isSpectator() && entity.isPickable(),
                StartaleConfig.HUD_PICK_RANGE.get() * StartaleConfig.HUD_PICK_RANGE.get());
        if (hit == null || !(hit.getEntity() instanceof LivingEntity living)) {
            return null;
        }
        return MobTaming.isTamed(living) && FavorabilityTracker.hasData(living) ? living : null;
    }

    /**
     * 判断玩家头部是否佩戴了草帽。
     *
     * @param player 玩家
     * @return 佩戴草帽时为 true
     */
    private static boolean wearsStrawHat(Player player) {
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (helmet.isEmpty()) {
            return false;
        }
        return helmet.is(KSItemTags.TAMED_DATA_DISPLAY_HAT);
    }

    /**
     * 采集目标生物的面板数据。
     *
     * @param entity 目标生物
     * @return 数据快照
     */
    private static Snapshot capture(LivingEntity entity) {
        TameData tame = entity.getData(KSAttachments.TAME.get());
        Favorability favor = FavorabilityTracker.get(entity);

        Component owner;
        if (tame.isTamed() && !tame.ownerName().isEmpty()) {
            owner = Component.literal(tame.ownerName());
        } else {
            owner = TEXT_UNKNOWN_OWNER;
        }

        int remaining = PetterCooldown.remaining(entity);
        Component cooldown = remaining > 0
                ? Component.literal(String.format("%d:%02d", remaining / 20 / 60, remaining / 20 % 60))
                : TEXT_READY;

        int feedTotal = Math.max(1, StartaleConfig.FAVOR_MAX_FEEDS_PER_DAY.get());
        int feedUsed = Math.min(favor.feedCount(), feedTotal);
        Component feedText = Component.literal(feedUsed + " / " + feedTotal);

        int level = favor.level();
        Component favorText = Component.literal(level + " / " + Favorability.MAX_LEVEL);

        Component progressText;
        float progress;
        if (favor.isMaxLevel()) {
            progressText = TEXT_MAX_LEVEL;
            progress = 1.0F;
        } else {
            int perLevel = Math.max(1, Favorability.pointsPerLevel());
            int within = favor.points() - (level - 1) * perLevel;
            progress = Math.clamp(within / (float) perLevel, 0.0F, 1.0F);
            progressText = Component.literal(Math.round(progress * 100.0F) + "%");
        }

        return new Snapshot(entity.getDisplayName(), owner, cooldown, feedText, feedUsed, feedTotal,
                favorText, level, Favorability.MAX_LEVEL, progressText, progress);
    }

    /**
     * 绘制面板。
     *
     * @param graphics  图形上下文
     * @param minecraft 客户端实例
     * @param data      面板数据
     */
    private static void draw(GuiGraphics graphics, Minecraft minecraft, Snapshot data) {
        Font font = minecraft.font;

        Component[] labels = {LABEL_OWNER, LABEL_COOLDOWN, LABEL_FEED, LABEL_FAVOR, LABEL_PROGRESS};
        Component[] values = {data.owner(), data.cooldown(), data.feedText(), data.favorText(),
                data.progressText()};
        int labelWidth = 0;
        for (Component label : labels) {
            labelWidth = Math.max(labelWidth, font.width(label));
        }
        int valueWidth = 0;
        for (Component value : values) {
            valueWidth = Math.max(valueWidth, font.width(value));
        }

        int gap = 6;
        int width = PANEL_WIDTH;
        int height = PANEL_HEIGHT;

        int centerX = graphics.guiWidth() / 2;
        int centerY = graphics.guiHeight() / 2;
        int x = centerX + 10;
        if (x + width > graphics.guiWidth() - 2) {
            x = centerX - 10 - width;
        }
        int y = centerY - height / 2 + 4;

        drawPanel(graphics, x, y);

        int left = x + PADDING;
        int right = x + width - PADDING;
        int valueX = left + labelWidth + gap;
        int barX = valueX + valueWidth + 4;
        int rowY = y + PADDING;

        graphics.drawCenteredString(font, data.title(), x + width / 2, y + TITLE_TOP, fade(COLOR_TITLE, alpha));
        rowY += TITLE_HEIGHT;

        drawTextRow(graphics, font, left, valueX, rowY, LABEL_OWNER, data.owner());
        rowY += ROW_HEIGHT;
        drawTextRow(graphics, font, left, valueX, rowY, LABEL_COOLDOWN, data.cooldown());
        rowY += ROW_HEIGHT;

        drawSegmentedRow(graphics, font, left, valueX, barX, right, rowY, LABEL_FEED, data.feedText(),
                data.feedUsed() / (float) data.feedTotal(), data.feedTotal(), COLOR_FEED);
        rowY += ROW_HEIGHT;

        drawSegmentedRow(graphics, font, left, valueX, barX, right, rowY, LABEL_FAVOR, data.favorText(),
                data.favorLevel() / (float) data.favorMax(), data.favorMax(), COLOR_FAVOR);
        rowY += ROW_HEIGHT;

        drawProgressRow(graphics, font, left, valueX, barX, right, rowY, LABEL_PROGRESS,
                data.progressText(), data.progress());
    }

    /**
     * 绘制纯文本行，数值对齐到数值列。
     *
     * @param graphics 图形上下文
     * @param font     字体
     * @param left     标签左边界
     * @param valueX   数值列左边界
     * @param y        行顶坐标
     * @param label    标签
     * @param value    数值
     */
    private static void drawTextRow(GuiGraphics graphics, Font font, int left, int valueX, int y, Component label, Component value) {
        graphics.drawString(font, label, left, y + 2, fade(COLOR_LABEL, alpha), false);
        graphics.drawString(font, value, valueX, y + 2, fade(COLOR_VALUE, alpha), false);
    }

    /**
     * 绘制分段条行。
     *
     * @param graphics 图形上下文
     * @param font     字体
     * @param left     标签左边界
     * @param valueX   数值左边界
     * @param barX     进度条左边界
     * @param right    右边界
     * @param y        行顶坐标
     * @param label    标签
     * @param value    数值文本
     * @param ratio    填充比例
     * @param segments 分段数量
     * @param color    填充颜色
     */
    private static void drawSegmentedRow(GuiGraphics graphics, Font font, int left, int valueX, int barX,
                                         int right, int y, Component label, Component value, float ratio, int segments, int color) {
        graphics.drawString(font, label, left, y + 2, fade(COLOR_LABEL, alpha), false);
        graphics.drawString(font, value, valueX, y + 2, fade(COLOR_VALUE, alpha), false);

        int barWidth = right - barX;
        if (barWidth < 8) {
            return;
        }
        int filled = Math.round(ratio * segments);
        drawSegments(graphics, barX, y + 5, barWidth, 3, segments, filled, fade(color, alpha));
    }

    /**
     * 绘制连续进度条行。
     *
     * @param graphics 图形上下文
     * @param font     字体
     * @param left     标签左边界
     * @param valueX   数值左边界
     * @param barX     进度条左边界
     * @param right    右边界
     * @param y        行顶坐标
     * @param label    标签
     * @param value    数值文本
     * @param ratio    进度比例
     */
    private static void drawProgressRow(GuiGraphics graphics, Font font, int left, int valueX, int barX,
                                        int right, int y, Component label, Component value, float ratio) {
        graphics.drawString(font, label, left, y + 2, fade(COLOR_LABEL, alpha), false);
        graphics.drawString(font, value, valueX, y + 2, fade(COLOR_VALUE, alpha), false);

        int barWidth = right - barX;
        if (barWidth < 8) {
            return;
        }
        graphics.fill(barX, y + 5, barX + barWidth, y + 8, fade(COLOR_EMPTY, alpha));
        int fillWidth = Math.round(barWidth * Math.clamp(ratio, 0.0F, 1.0F));
        if (fillWidth > 0) {
            graphics.fill(barX, y + 5, barX + fillWidth, y + 8, fade(COLOR_PROGRESS, alpha));
        }
    }

    /**
     * 绘制分段条。
     *
     * @param graphics 图形上下文
     * @param x        左边界
     * @param y        顶坐标
     * @param width    总宽度
     * @param height   高度
     * @param segments 分段数量
     * @param filled   已填充段数
     * @param color    填充颜色
     */
    private static void drawSegments(GuiGraphics graphics, int x, int y, int width, int height, int segments, int filled, int color) {
        int gap = 1;
        int segmentWidth = Math.max(1, (width - gap * (segments - 1)) / segments);
        for (int i = 0; i < segments; i++) {
            int segX = x + i * (segmentWidth + gap);
            graphics.fill(segX, y, segX + segmentWidth, y + height,
                    i < filled ? color : fade(COLOR_EMPTY, alpha));
        }
    }

    /**
     * 按贴图原始尺寸绘制面板背景，不做任何缩放。
     *
     * @param graphics 图形上下文
     * @param x        左边界
     * @param y        顶坐标
     */
    private static void drawPanel(GuiGraphics graphics, int x, int y) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha * StartaleConfig.HUD_PANEL_OPACITY.get().floatValue());
        graphics.blitSprite(PANEL_TEXTURE, x, y, PANEL_WIDTH, PANEL_HEIGHT);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }

    /**
     * 按淡入淡出程度调整颜色透明度。
     *
     * @param color 原颜色
     * @param fade  透明度系数
     * @return 调整后的颜色
     */
    private static int fade(int color, float fade) {
        int baseAlpha = (color >>> 24) & 0xFF;
        int newAlpha = Math.clamp(Math.round(baseAlpha * fade), 0, 255);
        return (newAlpha << 24) | (color & 0x00FFFFFF);
    }
}
