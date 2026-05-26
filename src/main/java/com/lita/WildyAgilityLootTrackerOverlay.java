package com.lita;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.List;
import java.time.Instant;
import javax.inject.Inject;
import static net.runelite.api.MenuAction.RUNELITE_OVERLAY_CONFIG;
import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LayoutableRenderableEntity;
import net.runelite.client.ui.overlay.components.TitleComponent;
import net.runelite.client.ui.overlay.components.ImageComponent;
import net.runelite.client.ui.overlay.components.ComponentOrientation;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.api.gameval.ItemID;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WildyAgilityLootTrackerOverlay extends OverlayPanel
{
    private final WildyAgilityLootTrackerPlugin plugin;
    private final WildyAgilityLootTrackerConfig config;
    private final int STANDARD_INVENTORY_ITEM_WIDTH  = 32;
    private final int STANDARD_INVENTORY_ITEM_HEIGHT = 32;

    public long reRenderScheduledTime  = 0;
    public long lastComponentBuildTime = 0;
    public long lastFrameDrawTime      = 0;
    public int  componentsBuildNum     = 0;

    public boolean shouldReRender      = false;

    public List<LayoutableRenderableEntity> _childCmps = null;

    public void logRenderTiming(){
        long buildElapsed  = lastComponentBuildTime - reRenderScheduledTime;
        long renderElapsed = lastFrameDrawTime - lastComponentBuildTime;
        log.info(String.format("Component Build completed %d ms after re-render scheduled, the render took an additional %d ms", buildElapsed, renderElapsed));
    }
    public long getCurrentTimeMs(){
        return Instant.now().toEpochMilli();
    }
    public void scheduleReRender(){
        this.shouldReRender = true;
        this.reRenderScheduledTime = getCurrentTimeMs();
        log.info("Scheduled Re-Render");
    }
    public void markRenderComplete(){
        this.shouldReRender = false;
        this.lastFrameDrawTime = getCurrentTimeMs();
        logRenderTiming();
    }
    public void onDataMutation(){
        log.info("Data Mutation Detected");
        scheduleReRender();
    }

    @Inject
    private WildyAgilityLootTrackerOverlay(WildyAgilityLootTrackerPlugin _plugin, WildyAgilityLootTrackerConfig _config){
        super(_plugin);
        setPosition(OverlayPosition.TOP_LEFT);
        /*
            setClearChildren(false) is necessary for giving this thing object-caching abilities which will help cut down
            on how many calls we make in our render() method

            if setClearChildren(true), what happens is that the panel is drawn, then the children are removed
            then on the very next frame, if you don't re-add all the children, then everything is lost and the panel
            will disappear

            by doing setClearChildren(false), we can make it so that the object structure persists between render() calls
            Which means we only have to clear the children and rebuild the UI components when something changes in the
            underlying data, rather than having to rebuild everything on every single frame

            it looks like this is saving about 23 ms of real-world execution time PER FRAME in my little test
        */
        setClearChildren(false);
        this.plugin = _plugin;
        this.config = _config;
        scheduleReRender();
        addMenuEntry(RUNELITE_OVERLAY_CONFIG, OPTION_CONFIGURE, "WildyAgilityLootTrackerPlugin overlay");
    }

    public PanelComponent makeSupplyRow(){
        PanelComponent supplyRow = new PanelComponent();
        supplyRow.setOrientation(ComponentOrientation.HORIZONTAL);
        supplyRow.setPreferredSize(new Dimension((STANDARD_INVENTORY_ITEM_WIDTH * 4), STANDARD_INVENTORY_ITEM_HEIGHT));
        supplyRow.setGap(new Point(4, 4));
        List<LayoutableRenderableEntity> panelChildren = supplyRow.getChildren();
        if (this.config.getShowSupplies()) {
            for (LtaLootItem supplyItem : this.plugin.supplies) {
                panelChildren.add(new ImageComponent(supplyItem.getQtyImage()));
            }
        }
        //supplyRow.setBackgroundColor(new Color(204, 94, 66));
        return supplyRow;
    }
    public PanelComponent makeArmourRow(int[] armourTypeIds){
        PanelComponent armourRow = new PanelComponent();
        armourRow.setOrientation(ComponentOrientation.HORIZONTAL);
        armourRow.setPreferredSize(new Dimension((STANDARD_INVENTORY_ITEM_WIDTH * 4), STANDARD_INVENTORY_ITEM_HEIGHT));
        armourRow.setGap(new Point(4, 4));
        List<LayoutableRenderableEntity> panelChildren = armourRow.getChildren();
        for (int armourTypeId: armourTypeIds){
            LtaLootItem armourItem = this.plugin.getMatchingArmourItem(armourTypeId);
            if (armourItem.display){
                panelChildren.add(new ImageComponent(armourItem.getQtyImage()));
            }
        }
        return armourRow;
    }

    public void configurePanelComponent(){
        panelComponent.setOrientation(ComponentOrientation.VERTICAL);
        this._childCmps = panelComponent.getChildren();
        panelComponent.setPreferredSize(new Dimension((STANDARD_INVENTORY_ITEM_WIDTH * 4), 0));
        //panelComponent.setGap(new Point(0, 4));
    }
    public void buildChildComponents(){

        this._childCmps.add(makeSupplyRow());
        //this._childCmps.add(LineComponent.builder().build());

        // This may seem convoluted, but we need to lookup the existing objects by id, so that we are using the existing objects and not creating new ones
        this._childCmps.add(makeArmourRow(LtaLootItem.getSteelArmourItemIds()));
        //this._childCmps.add(LineComponent.builder().build());
        this._childCmps.add(makeArmourRow(LtaLootItem.getMithrilArmourItemIds()));
        //this._childCmps.add(LineComponent.builder().build());
        this._childCmps.add(makeArmourRow(LtaLootItem.getAdamantArmourItemIds()));
        //this._childCmps.add(LineComponent.builder().build());
        this._childCmps.add(makeArmourRow(LtaLootItem.getRuneArmourItemIds()));
        //this._childCmps.add(LineComponent.builder().build());

        this._childCmps.add(TitleComponent.builder().text(this.plugin.currentLootValStr).color(Color.GREEN).build());
    }
    public void buildComponents(){
        log.info("Building Components...");
        configurePanelComponent();
        this._childCmps.clear();
        buildChildComponents();
        this.componentsBuildNum++;
    }
    public void rebuildComponents(){
        if (this.componentsBuildNum > 0){ log.info("Re-creating component structures"); }
        buildComponents();
        this.lastComponentBuildTime = getCurrentTimeMs();
    }

    /* Eventually we should move away from rebuilding UI object structures even when the data mutates
       There's no need to rebuild everything. We could get away with merely replacing the updated object sprites
       and replacing the final text component's text.

       But this will require logic to determine which sprite changed so that we only replace individual sprites
       rather than redrawing entire subpanels full of multiple sprites
    */
    @Override
    public Dimension render(Graphics2D graphics){
        if (this.shouldReRender){
            rebuildComponents();
            Dimension renderedDimension = super.render(graphics);
            markRenderComplete();
            return renderedDimension;
        }
        else {
            return super.render(graphics);
        }
    }


}
