package com.tequilacat.kaleidoscope_startale.mixin.client;

import com.tequilacat.kaleidoscope_startale.client.creative.CreativeTabSectionButton;
import com.tequilacat.kaleidoscope_startale.init.KSCreativeModeTabs;
import com.tequilacat.kaleidoscope_startale.init.KSCreativeSections;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 给创造模式物品栏加分区按钮。
 * <p>
 * 切到本模组的页签时，在界面左侧生成一排按钮；点击按钮只显示对应分区，
 * 按住 Shift 点击可以同时选中多个分区。点击后会重新构建页签内容并刷新列表。
 */
@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeSectionButtonsMixin
        extends AbstractContainerScreen<CreativeModeInventoryScreen.ItemPickerMenu> {

    /** 当前选中的创造模式物品栏。 */
    @Shadow
    private static CreativeModeTab selectedTab;

    /** 是否展示管理员物品（用于重建参数）。 */
    @Shadow
    @Final
    private boolean displayOperatorCreativeTab;

    /** 本界面创建的分区按钮。 */
    @Unique
    private final List<CreativeTabSectionButton> KS$sectionButtons = new ArrayList<>();

    /**
     * 私有构造函数。
     * <p>
     * 仅用于满足父类构造要求，Mixin 不会实际调用。
     */
    private CreativeSectionButtonsMixin() {
        super(null, null, null);
    }

    /**
     * 切换页签时重建按钮。
     *
     * @param tab 切到的物品栏
     * @param ci  回调
     */
    @Inject(method = "selectTab", at = @At("HEAD"))
    private void KS$rebuildSectionButtons(CreativeModeTab tab, CallbackInfo ci) {
        for (CreativeTabSectionButton button : this.KS$sectionButtons) {
            this.removeWidget(button);
        }
        this.KS$sectionButtons.clear();
        if (tab != KSCreativeModeTabs.STARTALE_TAB.get()) {
            return;
        }
        List<KSCreativeSections.Section> sections = KSCreativeSections.sections();
        for (int i = 0; i < sections.size(); i++) {
            int index = i;
            int x = this.leftPos - 27 - (i / 4) * 28;
            int y = this.topPos + 10 + (i % 4) * 28;
            CreativeTabSectionButton button = new CreativeTabSectionButton(x, y, index, sections.get(i),
                    pressed -> this.KS$onSectionPressed(index));
            this.KS$sectionButtons.add(button);
            this.addRenderableWidget(button);
        }
    }

    /**
     * 点击分区按钮：Shift 多选，否则只选这一个。
     *
     * @param index 分区下标
     */
    @Unique
    private void KS$onSectionPressed(int index) {
        if (Screen.hasShiftDown()) {
            KSCreativeSections.toggleSection(index);
        } else {
            KSCreativeSections.selectSection(index);
        }
        Minecraft minecraft = this.minecraft;
        LocalPlayer player = minecraft == null ? null : minecraft.player;
        if (player == null) {
            return;
        }
        CreativeModeTab.ItemDisplayParameters parameters = new CreativeModeTab.ItemDisplayParameters(
                player.connection.enabledFeatures(),
                player.canUseGameMasterBlocks() && this.displayOperatorCreativeTab,
                player.level().registryAccess());
        selectedTab.buildContents(parameters);
        ((CreativeModeInventoryScreenInvoker) this).KS$refreshCurrentTabContents(selectedTab.getDisplayItems());
    }
}
