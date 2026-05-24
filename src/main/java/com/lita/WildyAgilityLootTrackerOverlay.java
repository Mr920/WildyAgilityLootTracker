package com.lita;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.List;
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

public class WildyAgilityLootTrackerOverlay extends OverlayPanel
{
    private final WildyAgilityLootTrackerPlugin plugin;
    private final WildyAgilityLootTrackerConfig config;
    private final int STANDARD_INVENTORY_ITEM_WIDTH  = 32;
    private final int STANDARD_INVENTORY_ITEM_HEIGHT = 32;

    @Inject
    private WildyAgilityLootTrackerOverlay(WildyAgilityLootTrackerPlugin _plugin, WildyAgilityLootTrackerConfig _config){
        super(_plugin);
        setPosition(OverlayPosition.TOP_LEFT);
        this.plugin = _plugin;
        this.config = _config;
        addMenuEntry(RUNELITE_OVERLAY_CONFIG, OPTION_CONFIGURE, "WildyAgilityLootTracker overlay");
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

    @Override
    public Dimension render(Graphics2D graphics){
        panelComponent.setOrientation(ComponentOrientation.VERTICAL);
        panelComponent.getChildren().clear();
        List<LayoutableRenderableEntity> panelChildren = panelComponent.getChildren();
        panelChildren.add(makeSupplyRow());
        panelChildren.add(LineComponent.builder().build());
        // This may seem convoluted, but we need to lookup the existing objects by id, so that we are using the existing objects and not creating new ones
        panelChildren.add(makeArmourRow(LtaLootItem.getSteelArmourItemIds()));
        panelChildren.add(LineComponent.builder().build());
        panelChildren.add(makeArmourRow(LtaLootItem.getMithrilArmourItemIds()));
        panelChildren.add(LineComponent.builder().build());
        panelChildren.add(makeArmourRow(LtaLootItem.getAdamantArmourItemIds()));
        panelChildren.add(LineComponent.builder().build());
        panelChildren.add(makeArmourRow(LtaLootItem.getRuneArmourItemIds()));
        panelChildren.add(LineComponent.builder().build());

        panelComponent.setPreferredSize(new Dimension((STANDARD_INVENTORY_ITEM_WIDTH * 4), (STANDARD_INVENTORY_ITEM_HEIGHT * 5)));
        panelComponent.setGap(new Point(0, 12));


        return super.render(graphics);
    }
}
