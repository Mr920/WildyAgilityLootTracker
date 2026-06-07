package com.lita;

public class SupplyItem extends LootItem {

    public static final String ITEM_TYPE = "SUPPLY";

    public SupplyCollection supplyCollection;
    public SupplyItem(FishSupplyCollection _supplyCollection, int itemID){
        super(itemID);
        this.supplyCollection = _supplyCollection;
    }
    public SupplyItem(PotionSupplyCollection _supplyCollection, int itemID){
        super(itemID);
        this.supplyCollection = _supplyCollection;
    }
    public SupplyItem(SupplyCollection _supplyCollection, int itemID){
        super(itemID);
        this.supplyCollection = _supplyCollection;
    }

    @Override
    public final String getItemType(){
        return ITEM_TYPE;
    }
}
