package com.lita;

import net.runelite.api.gameval.ItemID;

public class ArmourItem extends LootItem {

    public static final String ITEM_TYPE = "ARMOUR";

    public ArmourCollection armourCollection;

    public ArmourItem(ArmourCollection _armourCollection, int itemID){
        super(itemID);
        this.armourCollection = _armourCollection;
    }

    @Override
    public final String getItemType(){
        return ITEM_TYPE;
    }

}
