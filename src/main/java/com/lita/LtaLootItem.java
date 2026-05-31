package com.lita;

import net.runelite.api.gameval.ItemID;
import net.runelite.client.util.AsyncBufferedImage;

public class LtaLootItem {

    // @@@@@ STATIC STUFF FIRST @@@@@

    public static final String TYPE_SUPPLY  = "Supply";
    public static final String TYPE_ARMOUR  = "Armour";
    public static final int    TYPE_STEEL   = 1;
    public static final int    TYPE_MITHRIL = 2;
    public static final int    TYPE_ADAMANT = 3;
    public static final int    TYPE_RUNE    = 4;


    public static int[] getSupplyItemIds(){
        return new int[] {
                ItemID.BLIGHTED_ANGLERFISH,
                ItemID.BLIGHTED_MANTARAY,
                ItemID.BLIGHTED_KARAMBWAN,
                ItemID.BLIGHTED_4DOSE2RESTORE
        };
    }
    public static int[] getSteelArmourItemIds(){
        return new int[] {
                ItemID.STEEL_PLATEBODY
        };
    }
    public static int[] getMithrilArmourItemIds(){
        return new int[] {
                ItemID.MITHRIL_CHAINBODY,
                ItemID.MITHRIL_PLATELEGS,
                ItemID.MITHRIL_PLATESKIRT
        };
    }
    public static int[] getAdamantArmourItemIds(){
        return new int[] {
                ItemID.ADAMANT_FULL_HELM,
                ItemID.ADAMANT_PLATEBODY,
                ItemID.ADAMANT_PLATELEGS
        };
    }
    public static int[] getRuneArmourItemIds(){
        return new int[] {
                ItemID.RUNE_MED_HELM,
                ItemID.RUNE_CHAINBODY,
                ItemID.RUNE_KITESHIELD
        };
    }
    public static int[] getAllArmourItemIds(){
        int[] steelIds   = getSteelArmourItemIds();
        int[] mithrilIds = getMithrilArmourItemIds();
        int[] adamantIds = getAdamantArmourItemIds();
        int[] runeIds    = getRuneArmourItemIds();
        int   index      = 0;
        int   totalIdCnt = steelIds.length + mithrilIds.length + adamantIds.length + runeIds.length;
        int[] allIds     = new int[ totalIdCnt ];

        for (int id: steelIds){   allIds[index++] = id; }
        for (int id: mithrilIds){ allIds[index++] = id; }
        for (int id: adamantIds){ allIds[index++] = id; }
        for (int id: runeIds){    allIds[index++] = id; }

        return allIds;
    }

    /* Doing some method wrappers for my own convenience */
    public static int getItemPrice(int itemID){
        return WildyAgilityLootTrackerPlugin._ItemManager.getItemPrice(itemID);
    }
    public static AsyncBufferedImage getImage(int itemID){
        return WildyAgilityLootTrackerPlugin._ItemManager.getImage(itemID);
    }

    public static String getItemName(int itemID){
        switch(itemID){
            case ItemID.BLIGHTED_ANGLERFISH:    return "Blighted anglerfish";
            case ItemID.BLIGHTED_MANTARAY:      return "Blighted manta ray";
            case ItemID.BLIGHTED_KARAMBWAN:     return "Blighted karambwan";
            case ItemID.BLIGHTED_4DOSE2RESTORE: return "Blighted super restore(4)";
            case ItemID.STEEL_PLATEBODY:        return "Steel platebody";
            case ItemID.MITHRIL_CHAINBODY:      return "Mithril chainbody";
            case ItemID.MITHRIL_PLATELEGS:      return "Mithril platelegs";
            case ItemID.MITHRIL_PLATESKIRT:     return "Mithril plateskirt";
            case ItemID.ADAMANT_FULL_HELM:      return "Adamant full helm";
            case ItemID.ADAMANT_PLATEBODY:      return "Adamant platebody";
            case ItemID.ADAMANT_PLATELEGS:      return "Adamant platelegs";
            case ItemID.RUNE_MED_HELM:          return "Rune med helm";
            case ItemID.RUNE_CHAINBODY:         return "Rune chainbody";
            case ItemID.RUNE_KITESHIELD:        return "Rune kiteshield";
            default:                            return "UNRECOGNIZED ITEM_ID";
        }
    }
    public static int getItemIdFromName(String itemName){
        switch(itemName){
            case "Blighted anglerfish":         return ItemID.BLIGHTED_ANGLERFISH;
            case "Blighted manta ray":          return ItemID.BLIGHTED_MANTARAY;
            case "Blighted karambwan":          return ItemID.BLIGHTED_KARAMBWAN;
            case "Blighted super restore(4)":   return ItemID.BLIGHTED_4DOSE2RESTORE;
            case "Steel platebody":             return ItemID.STEEL_PLATEBODY;
            case "Mithril chainbody":           return ItemID.MITHRIL_CHAINBODY;
            case "Mithril platelegs":           return ItemID.MITHRIL_PLATELEGS;
            case "Mithril plateskirt":          return ItemID.MITHRIL_PLATESKIRT;
            case "Adamant full helm":           return ItemID.ADAMANT_FULL_HELM;
            case "Adamant platebody":           return ItemID.ADAMANT_PLATEBODY;
            case "Adamant platelegs":           return ItemID.ADAMANT_PLATELEGS;
            case "Rune med helm":               return ItemID.RUNE_MED_HELM;
            case "Rune chainbody":              return ItemID.RUNE_CHAINBODY;
            case "Rune kiteshield":             return ItemID.RUNE_KITESHIELD;
            default:                            return 1; // because this totally won't blow up in our face one day
        }
    }
    public static String getItemType(int itemID){
        switch(itemID){
            case ItemID.BLIGHTED_ANGLERFISH:
            case ItemID.BLIGHTED_MANTARAY:
            case ItemID.BLIGHTED_KARAMBWAN:
            case ItemID.BLIGHTED_4DOSE2RESTORE:
                return TYPE_SUPPLY;
            case ItemID.STEEL_PLATEBODY:
            case ItemID.MITHRIL_CHAINBODY:
            case ItemID.MITHRIL_PLATELEGS:
            case ItemID.MITHRIL_PLATESKIRT:
            case ItemID.ADAMANT_FULL_HELM:
            case ItemID.ADAMANT_PLATEBODY:
            case ItemID.ADAMANT_PLATELEGS:
            case ItemID.RUNE_MED_HELM:
            case ItemID.RUNE_CHAINBODY:
            case ItemID.RUNE_KITESHIELD:
                return TYPE_ARMOUR;
            default:
                return "UNRECOGNIZED_TYPE";
        }
    }

    public static LtaLootItem getNewObjectFromId(int itemID){
        return new LtaLootItem(itemID);
    }
    public static LtaLootItem[] getItemsFromIdList(int[] idList){
        int            index = 0;
        LtaLootItem[]  items = new LtaLootItem[idList.length];
        for (int id: idList){
            items[index++] = getNewObjectFromId(id);
        }
        return items;
    }
    public static LtaLootItem[] getSupplyItems(){           return getItemsFromIdList(getSupplyItemIds());          }
    public static LtaLootItem[] getSteelArmourItems(){      return getItemsFromIdList(getSteelArmourItemIds());     }
    public static LtaLootItem[] getMithrilArmourItems(){    return getItemsFromIdList(getMithrilArmourItemIds());   }
    public static LtaLootItem[] getAdamantArmourItems(){    return getItemsFromIdList(getAdamantArmourItemIds());   }
    public static LtaLootItem[] getRuneArmourItems(){       return getItemsFromIdList(getRuneArmourItemIds());      }
    public static LtaLootItem[] getAllArmourItems(){        return getItemsFromIdList(getAllArmourItemIds());       }

    // @@@@@ INSTANCE STUFF @@@@@

    public int                  id;
    public String               name;
    public String               type;
    public int                  price;
    public AsyncBufferedImage   image;
    public int                  haveQuantity;
    public boolean              display;

    public LtaLootItem(int itemID){
        this.id           = itemID;
        this.name         = getItemName(this.id);
        this.type         = getItemType(this.id);
        this.price        = getItemPrice(this.id);
        this.image        = getImage(this.id);
        this.haveQuantity = 0;
    }

    public int getTotalValue(){
        return this.price * this.haveQuantity;
    }
    public boolean isSteelArmour(){
        return (this.id == ItemID.STEEL_PLATEBODY);
    }
    public boolean isMithrilArmour(){
        switch(this.id){
            case ItemID.MITHRIL_CHAINBODY:
            case ItemID.MITHRIL_PLATELEGS:
            case ItemID.MITHRIL_PLATESKIRT:
                return true;
            default:
                return false;
        }
    }
    public boolean isAdamantArmour(){
        switch(this.id){
            case ItemID.ADAMANT_FULL_HELM:
            case ItemID.ADAMANT_PLATEBODY:
            case ItemID.ADAMANT_PLATELEGS:
                return true;
            default:
                return false;
        }
    }
    public boolean isRuneArmour(){
        switch(this.id){
            case ItemID.RUNE_MED_HELM:
            case ItemID.RUNE_CHAINBODY:
            case ItemID.RUNE_KITESHIELD:
                return true;
            default:
                return false;
        }
    }
    public boolean isSupplyItem(){
        return getItemType(this.id).equals(TYPE_SUPPLY);
    }
    public boolean isArmourItem(){
        return getItemType(this.id).equals(TYPE_ARMOUR);
    }
    public void _detectIfConfigured(WildyAgilityLootTrackerConfig configObject){
        if (this.isSupplyItem()){
                                         this.display = configObject.getShowSupplies();      return;
        }
        if (this.isArmourItem()){
            if (this.isSteelArmour()){   this.display = configObject.getShowSteelArmour();   return; }
            if (this.isMithrilArmour()){ this.display = configObject.getShowMithrilArmour(); return; }
            if (this.isAdamantArmour()){ this.display = configObject.getShowAdamantArmour(); return; }
            if (this.isRuneArmour()){    this.display = configObject.getShowRuneArmour();    return; }
        }
    }

    public void updateItemPrice(){
        this.price = getItemPrice(this.id);
    }

    public String toSmallGpStr(int value){
        String numStr = String.format("%,d", value);
        return String.format("%6s GP", numStr);
    }
    public String toBigGpStr(int value){
        String numStr = String.format("%,d", value);
        return String.format("%10s GP", numStr);
    }
    public String getTotalGpStr(){
        return toBigGpStr(getTotalValue());
    }
    public String getPriceGpStr(){
        return toSmallGpStr(this.price);
    }
    public String toDebugString(){
        return String.format("LtaLootItem[id=%06d]{ T: %s | N: %-27s | %4d * %s | %s }", this.id, this.type, this.name, this.haveQuantity, getPriceGpStr(), getTotalGpStr());
    }
    public AsyncBufferedImage getQtyImage(){
        return WildyAgilityLootTrackerPlugin._ItemManager.getImage(this.id, this.haveQuantity, true);
    }

    public int getArmourType(){
        if (isSteelArmour()){   return LtaLootItem.TYPE_STEEL;   }
        if (isMithrilArmour()){ return LtaLootItem.TYPE_MITHRIL; }
        if (isAdamantArmour()){ return LtaLootItem.TYPE_ADAMANT; }
        if (isRuneArmour()){    return LtaLootItem.TYPE_RUNE;    }
        return -1;
    }

}