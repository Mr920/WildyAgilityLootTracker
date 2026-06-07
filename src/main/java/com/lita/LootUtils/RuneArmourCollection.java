package com.lita.LootUtils;

import net.runelite.api.gameval.ItemID;

public class RuneArmourCollection extends ArmourCollection {

    public static final int ID_MEDHELM    = ItemID.RUNE_MED_HELM;
    public static final int ID_CHAINBODY  = ItemID.RUNE_CHAINBODY;
    public static final int ID_KITESHIELD = ItemID.RUNE_KITESHIELD;


    public ArmourItem  medhelm;
    public ArmourItem  chainbody;
    public ArmourItem  kiteshield;

    public RuneArmourCollection(ArmourGroup _armourGroup){
        super(_armourGroup);
        this.medhelm     = new ArmourItem(this, ID_MEDHELM);
        this.chainbody   = new ArmourItem(this, ID_CHAINBODY);
        this.kiteshield  = new ArmourItem(this, ID_KITESHIELD);
    }

    @Override
    public String getNameFromId(int itemID){
        switch(itemID){
            case ItemID.RUNE_MED_HELM:          return "Rune med helm";
            case ItemID.RUNE_CHAINBODY:         return "Rune chainbody";
            case ItemID.RUNE_KITESHIELD:        return "Rune kiteshield";
            default:                            return "UNRECOGNIZED ITEM_ID";
        }
    }
}
