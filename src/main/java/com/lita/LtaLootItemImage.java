package com.lita;
import net.runelite.client.ui.overlay.components.ImageComponent;

public class LtaLootItemImage extends ImageComponent {
    public LtaLootItem targetLootItem = null;
    public LtaLootItemImage(LtaLootItem lootItem){
        super(lootItem.getQtyImage());
        this.targetLootItem = lootItem;
    }
}
