package com.lita.LootUtils;

public class SupplyGroup extends LootGroup {

    public   FishSupplyCollection    fish;
    public PotionSupplyCollection potions;

    public SupplyGroup(LootBag _bag){
        super(_bag);
        this.fish    = new FishSupplyCollection(this);
        this.potions = new PotionSupplyCollection(this);
    }
}
