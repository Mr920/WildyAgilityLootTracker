package com.lita;

public class ArmourCollection extends LootCollection {

    public ArmourGroup armourGroup;

    public ArmourCollection(ArmourGroup parentGroup){
        this.armourGroup = parentGroup;
    }

    @Override
    public String getNameFromId(int itemID){ return null; }

    @Override
    public ArmourGroup getLootGroup(){
        return this.armourGroup;
    }
}
