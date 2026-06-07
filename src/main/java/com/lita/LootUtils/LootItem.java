package com.lita.LootUtils;

import net.runelite.client.game.ItemManager;
import net.runelite.client.util.AsyncBufferedImage;

public class LootItem {

    public int                  id;
    public String               name;
    public String               type;
    public int                  price;
    public AsyncBufferedImage   image;
    public int                  haveQuantity;
    public boolean              display;

    public LootItem(int itemID){
        this.id           = itemID;
        this.name         = getName();
        this.price        = 0;
        this.haveQuantity = 0;
    }

    public String getItemType(){ return null; }

    public LootCollection getParentCollection(){
        switch (this.getItemType()){
            case ArmourItem.ITEM_TYPE: return ((ArmourItem)this).armourCollection;
            case SupplyItem.ITEM_TYPE: return ((SupplyItem)this).supplyCollection;
            default: return null;
        }
    }

    public ItemManager getItemManager(){
        return this.getParentCollection().getLootGroup().bag.plugin.itemManager;
    }

    public int getItemPrice(){
        return this.getItemManager().getItemPrice(this.id);
    }
    public AsyncBufferedImage getImage(){
        return this.getItemManager().getImage(this.id);
    }
    public AsyncBufferedImage getQtyImage(){
        return this.getItemManager().getImage(this.id, this.haveQuantity, true);
    }
    public String getName(){ return this.getParentCollection().getNameFromId(this.id); }
}
