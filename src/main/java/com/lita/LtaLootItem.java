package com.lita;

import net.runelite.api.gameval.ItemID;
import net.runelite.client.util.AsyncBufferedImage;

public class LtaLootItem {
    public static final String TYPE_SUPPLY = "Supply";
    public static final String TYPE_ARMOUR = "Armour";

    public static int[] getSupplyItemIds(){
        return new int[] {
                net.runelite.api.gameval.ItemID.BLIGHTED_ANGLERFISH,
                net.runelite.api.gameval.ItemID.BLIGHTED_MANTARAY,
                net.runelite.api.gameval.ItemID.BLIGHTED_KARAMBWAN,
                net.runelite.api.gameval.ItemID.BLIGHTED_4DOSE2RESTORE
        };
    }
    public static int[] getSteelArmourItemIds(){
        return new int[] {
                net.runelite.api.gameval.ItemID.STEEL_PLATEBODY
        };
    }
    public static int[] getMithrilArmourItemIds(){
        return new int[] {
                net.runelite.api.gameval.ItemID.MITHRIL_CHAINBODY,
                net.runelite.api.gameval.ItemID.MITHRIL_PLATELEGS,
                net.runelite.api.gameval.ItemID.MITHRIL_PLATESKIRT
        };
    }
    public static int[] getAdamantArmourItemIds(){
        return new int[] {
                net.runelite.api.gameval.ItemID.ADAMANT_FULL_HELM,
                net.runelite.api.gameval.ItemID.ADAMANT_PLATEBODY,
                net.runelite.api.gameval.ItemID.ADAMANT_PLATELEGS
        };
    }
    public static int[] getRuneArmourItemIds(){
        return new int[] {
                net.runelite.api.gameval.ItemID.RUNE_MED_HELM,
                net.runelite.api.gameval.ItemID.RUNE_CHAINBODY,
                net.runelite.api.gameval.ItemID.RUNE_KITESHIELD
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
            case net.runelite.api.gameval.ItemID.BLIGHTED_ANGLERFISH:    return "Blighted anglerfish";
            case net.runelite.api.gameval.ItemID.BLIGHTED_MANTARAY:      return "Blighted manta ray";
            case net.runelite.api.gameval.ItemID.BLIGHTED_KARAMBWAN:     return "Blighted karambwan";
            case net.runelite.api.gameval.ItemID.BLIGHTED_4DOSE2RESTORE: return "Blighted super restore(4)";
            case net.runelite.api.gameval.ItemID.STEEL_PLATEBODY:        return "Steel platebody";
            case net.runelite.api.gameval.ItemID.MITHRIL_CHAINBODY:      return "Mithril chainbody";
            case net.runelite.api.gameval.ItemID.MITHRIL_PLATELEGS:      return "Mithril platelegs";
            case net.runelite.api.gameval.ItemID.MITHRIL_PLATESKIRT:     return "Mithril plateskirt";
            case net.runelite.api.gameval.ItemID.ADAMANT_FULL_HELM:      return "Adamant full helm";
            case net.runelite.api.gameval.ItemID.ADAMANT_PLATEBODY:      return "Adamant platebody";
            case net.runelite.api.gameval.ItemID.ADAMANT_PLATELEGS:      return "Adamant platelegs";
            case net.runelite.api.gameval.ItemID.RUNE_MED_HELM:          return "Rune med helm";
            case net.runelite.api.gameval.ItemID.RUNE_CHAINBODY:         return "Rune chainbody";
            case net.runelite.api.gameval.ItemID.RUNE_KITESHIELD:        return "Rune kiteshield";
            default:                                                     return "UNRECOGNIZED ITEM_ID";
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
    public static LtaLootItem[] getSupplyItems(){
        return getItemsFromIdList(getSupplyItemIds());
    }
    public static LtaLootItem[] getSteelArmourItems(){      return getItemsFromIdList(getSteelArmourItemIds());     }
    public static LtaLootItem[] getMithrilArmourItems(){    return getItemsFromIdList(getMithrilArmourItemIds());   }
    public static LtaLootItem[] getAdamantArmourItems(){    return getItemsFromIdList(getAdamantArmourItemIds());   }
    public static LtaLootItem[] getRuneArmourItems(){       return getItemsFromIdList(getRuneArmourItemIds());      }
    public static LtaLootItem[] getAllArmourItems(){        return getItemsFromIdList(getAllArmourItemIds());       }



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

    public String toDebugString(){
        return String.format("LtaLootItem { id = %d, name='%s', type='%s', price=%d, qty=%d, value=%d }", this.id, this.name, this.type, this.price, this.haveQuantity, getTotalValue());
    }

}