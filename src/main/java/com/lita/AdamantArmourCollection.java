package com.lita;

import net.runelite.api.gameval.ItemID;

public class AdamantArmourCollection extends ArmourCollection {

    public static final int ID_FULLHELM  = ItemID.ADAMANT_FULL_HELM;
    public static final int ID_PLATEBODY = ItemID.ADAMANT_PLATEBODY;
    public static final int ID_PLATELEGS = ItemID.ADAMANT_PLATELEGS;

    public ArmourItem  fullhelm;
    public ArmourItem  platebody;
    public ArmourItem  platelegs;

    public AdamantArmourCollection(ArmourGroup _armourGroup){
        super(_armourGroup);
        this.fullhelm    = new ArmourItem(this, ID_FULLHELM);
        this.platebody   = new ArmourItem(this, ID_PLATEBODY);
        this.platelegs   = new ArmourItem(this, ID_PLATELEGS);
    }

    @Override
    public String getNameFromId(int itemID){
        switch(itemID){
            case ItemID.ADAMANT_FULL_HELM:      return "Adamant full helm";
            case ItemID.ADAMANT_PLATEBODY:      return "Adamant platebody";
            case ItemID.ADAMANT_PLATELEGS:      return "Adamant platelegs";
            default:                            return "UNRECOGNIZED ITEM_ID";
        }
    }
}
