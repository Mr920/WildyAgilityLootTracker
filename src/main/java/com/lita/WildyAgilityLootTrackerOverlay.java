package com.lita;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.List;
import javax.inject.Inject;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LayoutableRenderableEntity;
import net.runelite.client.ui.overlay.components.TitleComponent;
import net.runelite.client.ui.overlay.components.ComponentOrientation;
import net.runelite.client.ui.overlay.components.PanelComponent;

public class WildyAgilityLootTrackerOverlay extends OverlayPanel {
    private static final                    int STANDARD_INVENTORY_ITEM_WIDTH  = 32;
    private static final                    int STANDARD_INVENTORY_ITEM_HEIGHT = 32;
    public  static final                    int ROW_WIDTH                      = STANDARD_INVENTORY_ITEM_WIDTH * 4;
    public  static final                    int ROW_HEIGHT                     = STANDARD_INVENTORY_ITEM_HEIGHT;

    public  static Dimension getRowDimension(){ return new Dimension(ROW_WIDTH, ROW_HEIGHT); }
    public  static     Point getRowGap(){       return new Point(4, 4);                }

    private final WildyAgilityLootTrackerPlugin plugin;
    private final WildyAgilityLootTrackerConfig config;
    public                              boolean isShutDown                     = false;
    public     List<LayoutableRenderableEntity> _childCmps                     = null;
    public                       PanelComponent _supplyRow                     = null;
    public                       PanelComponent _steelRow                      = null;
    public                       PanelComponent _mithrilRow                    = null;
    public                       PanelComponent _adamantRow                    = null;
    public                       PanelComponent _runeRow                       = null;
    public                       TitleComponent _lootValTitle                  = null;

    @Inject
    private               WildyAgilityLootTrackerOverlay(WildyAgilityLootTrackerPlugin _plugin, WildyAgilityLootTrackerConfig _config){
        super(_plugin);
        plugin = _plugin;
        config = _config;
        setPosition(OverlayPosition.TOP_LEFT);
        setClearChildren(false);
    }
    public PanelComponent getNewRowComponent(){
        PanelComponent rowCmp = new PanelComponent();
        rowCmp.setOrientation(ComponentOrientation.HORIZONTAL);
        rowCmp.setPreferredSize(getRowDimension());
        rowCmp.setGap(getRowGap());
        return rowCmp;
    }
    public PanelComponent makeSupplyRow(){
        _supplyRow = getNewRowComponent();
        List<LayoutableRenderableEntity> panelChildren = _supplyRow.getChildren();
        if (config.getShowSupplies()) {
            for (LtaLootItem supplyItem : plugin.supplies) {
                panelChildren.add(new LtaLootItemImage(supplyItem));
            }
        }
        return _supplyRow;
    }
    public           void setArmourRowByType(PanelComponent _armourRow, int armourType){
        switch (armourType){
            case LtaLootItem.TYPE_STEEL:   _steelRow   = _armourRow; break;
            case LtaLootItem.TYPE_MITHRIL: _mithrilRow = _armourRow; break;
            case LtaLootItem.TYPE_ADAMANT: _adamantRow = _armourRow; break;
            case LtaLootItem.TYPE_RUNE:    _runeRow    = _armourRow; break;
        }
    }
    public PanelComponent getArmourRowByType(int armourType){
        switch (armourType){
            case LtaLootItem.TYPE_STEEL:   return _steelRow;
            case LtaLootItem.TYPE_MITHRIL: return _mithrilRow;
            case LtaLootItem.TYPE_ADAMANT: return _adamantRow;
            case LtaLootItem.TYPE_RUNE:    return _runeRow;
            default:                       return null;
        }
    }
    public PanelComponent makeArmourRow(int[] armourTypeIds, int armourType){
        PanelComponent armourRow = getNewRowComponent();
        setArmourRowByType(armourRow, armourType);
        List<LayoutableRenderableEntity> panelChildren = armourRow.getChildren();
        for (int armourTypeId: armourTypeIds){
            LtaLootItem armourItem = plugin.getMatchingArmourItem(armourTypeId);
            if (armourItem.display){ panelChildren.add(new LtaLootItemImage(armourItem)); }
        }
        return armourRow;
    }
    public TitleComponent makeLootValTitleCmp(){
        _lootValTitle = TitleComponent.builder().text(plugin.currentLootValStr).color(Color.GREEN).build();
        return _lootValTitle;
    }
    public           void configurePanelComponent(){
        panelComponent.setOrientation(ComponentOrientation.VERTICAL);
        _childCmps = panelComponent.getChildren();
        panelComponent.setPreferredSize(new Dimension(ROW_WIDTH, 0));
    }
    public           void buildChildComponents(){
        _childCmps.add(makeSupplyRow());
        _childCmps.add(makeArmourRow(LtaLootItem.getSteelArmourItemIds(),   LtaLootItem.TYPE_STEEL));
        _childCmps.add(makeArmourRow(LtaLootItem.getMithrilArmourItemIds(), LtaLootItem.TYPE_MITHRIL));
        _childCmps.add(makeArmourRow(LtaLootItem.getAdamantArmourItemIds(), LtaLootItem.TYPE_ADAMANT));
        _childCmps.add(makeArmourRow(LtaLootItem.getRuneArmourItemIds(),    LtaLootItem.TYPE_RUNE));
        _childCmps.add(makeLootValTitleCmp());
    }
    public           void buildComponents(){
        configurePanelComponent();
        _childCmps.clear();
        buildChildComponents();
    }
    public           void updateLootValueStr(){ _lootValTitle.setText(plugin.currentLootValStr); }
    public           void updateItemImage(PanelComponent itemRow, LtaLootItem mutatedItemObject){
        List<LayoutableRenderableEntity> itemImages = itemRow.getChildren();
        for (LayoutableRenderableEntity itemImage: itemImages){
            if (((LtaLootItemImage) itemImage).targetLootItem.hashCode() == mutatedItemObject.hashCode()){
                int targetIndex = itemImages.indexOf(itemImage);
                itemImages.set(targetIndex, new LtaLootItemImage(mutatedItemObject));
            }
        }
    }
    public           void updateSupplyRow(LtaLootItem mutatedSupplyObj){ updateItemImage(_supplyRow, mutatedSupplyObj); }
    public           void updateArmourRow(LtaLootItem mutatedArmourObj){
        PanelComponent armourRow = getArmourRowByType(mutatedArmourObj.getArmourType());
        if (armourRow == null){ return; } // perhaps we should be throwing an exception...
        updateItemImage(armourRow, mutatedArmourObj);
    }
    public           void updateComponents(LtaLootItem mutatedSupplyItem, LtaLootItem mutatedArmourItem){
        updateSupplyRow(mutatedSupplyItem);
        updateArmourRow(mutatedArmourItem);
        updateLootValueStr();
    }
    public           void onDataMutation(WildyAgilityChatParser.Highlighted mutatedObjects){ updateComponents(mutatedObjects.updatedSupplyItem, mutatedObjects.updatedArmourItem);     }
    public           void onStartUp(){
        if (isShutDown){ isShutDown = false; }
        buildComponents();
        plugin.overlayManager.add(this);
    }
    public           void onShutdown(){
        panelComponent.getChildren().clear();
        plugin.overlayManager.remove(this);
        isShutDown = true;
    }
    @Override
    public      Dimension render(Graphics2D graphics){ return super.render(graphics); }
    public           void rebuild(){
        panelComponent.getChildren().clear();
        buildComponents();
    }
}