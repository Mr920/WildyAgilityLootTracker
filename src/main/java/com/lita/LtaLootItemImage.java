package com.lita;

import net.runelite.client.ui.overlay.components.ImageComponent;
import net.runelite.client.util.AsyncBufferedImage;

public class LtaLootItemImage extends ImageComponent {

    public LtaLootItem        targetLootItem  = null;

    public LtaLootItemImage(LtaLootItem lootItem){
        super(lootItem.getQtyImage());
        this.targetLootItem  = lootItem;
    }
}
