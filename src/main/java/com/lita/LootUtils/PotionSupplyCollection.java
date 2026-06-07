package com.lita.LootUtils;

import net.runelite.api.gameval.ItemID;

public class PotionSupplyCollection extends SupplyCollection {

    public static final int ID_RESTORE = ItemID.BLIGHTED_4DOSE2RESTORE;

    public SupplyItem  restores;

    public PotionSupplyCollection(SupplyGroup _supplyGroup){
        super(_supplyGroup);
        this.restores    = new SupplyItem(this, ID_RESTORE);
    }
}
