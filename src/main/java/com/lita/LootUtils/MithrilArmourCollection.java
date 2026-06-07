package com.lita.LootUtils;

import net.runelite.api.gameval.ItemID;

public class MithrilArmourCollection extends ArmourCollection {

    public static final int ID_CHAINBODY  = ItemID.MITHRIL_CHAINBODY;
    public static final int ID_PLATELEGS  = ItemID.MITHRIL_PLATELEGS;
    public static final int ID_PLATESKIRT = ItemID.MITHRIL_PLATESKIRT;


    public ArmourItem  chainbody;
    public ArmourItem  platelegs;
    public ArmourItem  plateskirt;

    public MithrilArmourCollection(ArmourGroup _armourGroup){
        super(_armourGroup);
        this.chainbody   = new ArmourItem(this, ID_CHAINBODY);
        this.platelegs   = new ArmourItem(this, ID_PLATELEGS);
        this.plateskirt  = new ArmourItem(this, ID_PLATESKIRT);
    }

    @Override
    public String getNameFromId(int itemID){
        switch(itemID){
            case ItemID.MITHRIL_CHAINBODY:      return "Mithril chainbody";
            case ItemID.MITHRIL_PLATELEGS:      return "Mithril platelegs";
            case ItemID.MITHRIL_PLATESKIRT:     return "Mithril plateskirt";
            default:                            return "UNRECOGNIZED ITEM_ID";
        }
    }
}
