package com.lita;

public class LootBag {

    public WildyAgilityLootTrackerPlugin plugin;
    public SupplyGroup supplies;
    public ArmourGroup armour;

    public LootBag(WildyAgilityLootTrackerPlugin _plugin){
        this.plugin = _plugin;
        this.supplies = new SupplyGroup(this);
        this.armour   = new ArmourGroup(this);
    }

}


/*  Format Notes (rough draft)


Loot_Bag {
	Supply_Group {
		Fish_Supply_C {
			  anglers Supply_Item {}
			    manta Supply_Item {}
			karambwan Supply_Item {}
		}
		Potion_Supply_C {
			restores Supply_Item {}
		}
	}
	Armour_Group {
		Steel_Armour_C {
			platebody Armour_Item {}
		}
		Mithril_Armour_C {
			 chainbody Armour_Item {}
			 platelegs Armour_Item {}
			plateskirt Armour_Item {}
		}
		Adamant_Armour_C {
			 fullhelm Armour_Item {}
			platebody Armour_Item {}
			platelegs Armour_Item {}
		}
		Rune_Armour_C {
			   medhelm Armour_Item {}
			 chainbody Armour_Item {}
			kiteshield Armour_Item {}
		}
	}
}

Loot_Bag

LootItem
	Supply_Item
	Armour_Item

LootGroup
	Supply_Group
	Armour_Group

LootCollection
	Supply_C
		Fish_Supply_C
		Potion_Supply_C

	Armour_C
		Steel_Armour_C
		Mithril_Armour_C
		Adamant_Armour_C
		Rune_Armour_C

 */