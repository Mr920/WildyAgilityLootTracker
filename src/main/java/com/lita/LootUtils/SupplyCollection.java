package com.lita.LootUtils;

import net.runelite.api.gameval.ItemID;

public class SupplyCollection extends LootCollection {

    public SupplyGroup supplyGroup;

    public SupplyCollection(SupplyGroup _supplyGroup){
        this.supplyGroup = _supplyGroup;
    }

    @Override
    public String getNameFromId(int itemID){
        switch(itemID){
            case ItemID.BLIGHTED_ANGLERFISH:    return "Blighted anglerfish";
            case ItemID.BLIGHTED_MANTARAY:      return "Blighted manta ray";
            case ItemID.BLIGHTED_KARAMBWAN:     return "Blighted karambwan";
            case ItemID.BLIGHTED_4DOSE2RESTORE: return "Blighted super restore(4)";
            default:                            return "UNRECOGNIZED ITEM_ID";
        }
    }

    @Override
    public SupplyGroup getLootGroup(){
        return this.supplyGroup;
    }
}
