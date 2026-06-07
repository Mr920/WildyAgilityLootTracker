package com.lita;

public class ArmourGroup extends LootGroup {

    public   SteelArmourCollection   steelCollection;
    public MithrilArmourCollection mithrilCollection;
    public AdamantArmourCollection adamantCollection;
    public    RuneArmourCollection    runeCollection;

    public ArmourGroup(LootBag _bag){
        super(_bag);
        this.steelCollection   = new SteelArmourCollection(this);
        this.mithrilCollection = new MithrilArmourCollection(this);
        this.adamantCollection = new AdamantArmourCollection(this);
        this.runeCollection    = new RuneArmourCollection(this);
    }
}
