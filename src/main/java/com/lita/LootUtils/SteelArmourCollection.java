package com.lita.LootUtils;

import net.runelite.api.gameval.ItemID;

public class SteelArmourCollection extends ArmourCollection {
    public static final int ID_PLATEBODY = ItemID.STEEL_PLATEBODY;


    public ArmourItem  platebody;
    public SteelArmourCollection(ArmourGroup _armourGroup){
        super(_armourGroup);
        this.platebody   = new ArmourItem(this, ID_PLATEBODY);
    }

    @Override
    public String getNameFromId(int itemID){
        switch(itemID){
            case ItemID.STEEL_PLATEBODY:        return "Steel platebody";
            default:                            return "UNRECOGNIZED ITEM_ID";
        }
    }
}
