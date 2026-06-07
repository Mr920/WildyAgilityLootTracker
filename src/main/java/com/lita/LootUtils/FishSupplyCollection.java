package com.lita.LootUtils;
import net.runelite.api.gameval.ItemID;

public class FishSupplyCollection extends SupplyCollection {

    public static final int ID_ANGLERFISH = ItemID.BLIGHTED_ANGLERFISH;
    public static final int ID_MANTARAY   = ItemID.BLIGHTED_MANTARAY;
    public static final int ID_KARAMBWAN  = ItemID.BLIGHTED_KARAMBWAN;


    public  SupplyItem anglerfish;
    public  SupplyItem mantaray;
    public  SupplyItem karambwan;

    public FishSupplyCollection(SupplyGroup _supplyGroup){
        super(_supplyGroup);
        this.anglerfish  = new SupplyItem(this, ID_ANGLERFISH);
        this.mantaray    = new SupplyItem(this, ID_MANTARAY);
        this.karambwan   = new SupplyItem(this, ID_KARAMBWAN);
    }

}
