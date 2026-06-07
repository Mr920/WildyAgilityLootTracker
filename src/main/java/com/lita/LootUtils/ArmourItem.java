package com.lita.LootUtils;

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
