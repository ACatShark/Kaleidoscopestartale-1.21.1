package com.tequilacat.kaleidoscope_startale.client.creative;

import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import com.tequilacat.kaleidoscope_startale.init.KSCreativeSections;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * 创造模式分区按钮。
 * <p>
 * 用于在创造模式物品栏中跳转到指定分区：按钮上绘制分区图标，
 * 并根据分区是否处于激活状态切换底图，同时提供含操作提示的悬停文本。
 */
public class CreativeTabSectionButton extends Button {

    /** 按钮边长。 */
    public static final int SIZE = 24;

    /**
     * 未选中状态的按钮底图。
     */
    private static final ResourceLocation TEXTURE = KaleidoscopeStartale.id("textures/gui/creative_inventory/section_button.png");

    /**
     * 选中状态的按钮底图。
     */
    private static final ResourceLocation TEXTURE_SELECTED = KaleidoscopeStartale.id("textures/gui/creative_inventory/section_button_selected.png");

    /** 分区。 */
    private final KSCreativeSections.Section section;

    /** 分区下标。 */
    private final int index;

    /**
     * 构造一个分区按钮。
     *
     * @param x       屏幕 x 坐标
     * @param y       屏幕 y 坐标
     * @param index   分区下标
     * @param section 分区
     * @param onPress 点击回调
     */
    public CreativeTabSectionButton(int x, int y, int index, KSCreativeSections.Section section, OnPress onPress) {
        super(x, y, SIZE, SIZE, Component.empty(), onPress, Button.DEFAULT_NARRATION);
        this.index = index;
        this.section = section;
        this.setTooltip(Tooltip.create(Component.empty()
                .append(section.name())
                .append("\n")
                .append(Component.translatable("creative_tab." + KaleidoscopeStartale.MODID + ".shift_hint")
                        .withStyle(ChatFormatting.GRAY))));
    }

    /**
     * 判断该分区是否处于激活状态。
     *
     * @return 分区是否处于激活状态
     */
    public boolean isSectionSelected() {
        return KSCreativeSections.isActive(this.index);
    }

    /**
     * 渲染按钮。
     * <p>
     * 根据分区激活状态绘制对应底图，并在按钮内绘制分区图标。
     *
     * @param graphics    绘制上下文
     * @param mouseX      鼠标 x 坐标
     * @param mouseY      鼠标 y 坐标
     * @param partialTick 插值刻
     */
    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ResourceLocation texture = this.isSectionSelected() ? TEXTURE_SELECTED : TEXTURE;
        graphics.blit(texture, this.getX(), this.getY(), 0, 0.0F, 0.0F, SIZE, SIZE, SIZE, SIZE);
        graphics.renderItem(this.section.icon().get(), this.getX() + 4, this.getY() + 4);
    }
}