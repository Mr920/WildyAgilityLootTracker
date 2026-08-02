package com.lita;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.util.AsyncBufferedImage;

public class LtaLootItem {
    public static final String TYPE_SUPPLY  = "Supply";
    public static final String TYPE_ARMOUR  = "Armour";
    public static final    int TYPE_STEEL   = 1;
    public static final    int TYPE_MITHRIL = 2;
    public static final    int TYPE_ADAMANT = 3;
    public static final    int TYPE_RUNE    = 4;
    public static final  int[] SUPPLY_ITEM_IDS         = { ItemID.BLIGHTED_ANGLERFISH, ItemID.BLIGHTED_MANTARAY, ItemID.BLIGHTED_KARAMBWAN, ItemID.BLIGHTED_4DOSE2RESTORE };
    public static final  int[] STEEL_ARMOUR_ITEM_IDS   = { ItemID.STEEL_PLATEBODY };
    public static final  int[] MITHRIL_ARMOUR_ITEM_IDS = { ItemID.MITHRIL_CHAINBODY,   ItemID.MITHRIL_PLATELEGS, ItemID.MITHRIL_PLATESKIRT };
    public static final  int[] ADAMANT_ARMOUR_ITEM_IDS = { ItemID.ADAMANT_FULL_HELM,   ItemID.ADAMANT_PLATEBODY, ItemID.ADAMANT_PLATELEGS  };
    public static final  int[] RUNE_ARMOUR_ITEM_IDS    = { ItemID.RUNE_MED_HELM,       ItemID.RUNE_CHAINBODY,    ItemID.RUNE_KITESHIELD    };
    public static final  int[] ALL_ARMOUR_ITEM_IDS;
    static {
        ALL_ARMOUR_ITEM_IDS = new int[ STEEL_ARMOUR_ITEM_IDS.length + MITHRIL_ARMOUR_ITEM_IDS.length + ADAMANT_ARMOUR_ITEM_IDS.length + RUNE_ARMOUR_ITEM_IDS.length ];
        int armourIdIndex   = 0;
        for (int id: STEEL_ARMOUR_ITEM_IDS){   ALL_ARMOUR_ITEM_IDS[armourIdIndex++] = id; }
        for (int id: MITHRIL_ARMOUR_ITEM_IDS){ ALL_ARMOUR_ITEM_IDS[armourIdIndex++] = id; }
        for (int id: ADAMANT_ARMOUR_ITEM_IDS){ ALL_ARMOUR_ITEM_IDS[armourIdIndex++] = id; }
        for (int id: RUNE_ARMOUR_ITEM_IDS){    ALL_ARMOUR_ITEM_IDS[armourIdIndex++] = id; }
    }
    public static final String ITEM_NAME_BLIGHTED_ANGLERFISH    = "Blighted anglerfish";
    public static final String ITEM_NAME_BLIGHTED_MANTARAY      = "Blighted manta ray";
    public static final String ITEM_NAME_BLIGHTED_KARAMBWAN     = "Blighted karambwan";
    public static final String ITEM_NAME_BLIGHTED_4DOSE2RESTORE = "Blighted super restore(4)";
    public static final String ITEM_NAME_STEEL_PLATEBODY        = "Steel platebody";
    public static final String ITEM_NAME_MITHRIL_CHAINBODY      = "Mithril chainbody";
    public static final String ITEM_NAME_MITHRIL_PLATELEGS      = "Mithril platelegs";
    public static final String ITEM_NAME_MITHRIL_PLATESKIRT     = "Mithril plateskirt";
    public static final String ITEM_NAME_ADAMANT_FULL_HELM      = "Adamant full helm";
    public static final String ITEM_NAME_ADAMANT_PLATEBODY      = "Adamant platebody";
    public static final String ITEM_NAME_ADAMANT_PLATELEGS      = "Adamant platelegs";
    public static final String ITEM_NAME_RUNE_MED_HELM          = "Rune med helm";
    public static final String ITEM_NAME_RUNE_CHAINBODY         = "Rune chainbody";
    public static final String ITEM_NAME_RUNE_KITESHIELD        = "Rune kiteshield";

    public static              int[] getSupplyItemIds(){        return SUPPLY_ITEM_IDS;         }
    public static              int[] getSteelArmourItemIds(){   return STEEL_ARMOUR_ITEM_IDS;   }
    public static              int[] getMithrilArmourItemIds(){ return MITHRIL_ARMOUR_ITEM_IDS; }
    public static              int[] getAdamantArmourItemIds(){ return ADAMANT_ARMOUR_ITEM_IDS; }
    public static              int[] getRuneArmourItemIds(){    return RUNE_ARMOUR_ITEM_IDS;    }
    public static              int[] getAllArmourItemIds(){     return ALL_ARMOUR_ITEM_IDS;     }
    public static        LtaLootItem getNewObjectFromId(int itemID){ return new LtaLootItem(itemID); }
    public static                int getItemPrice(int itemID){  return WildyAgilityLootTrackerPlugin.ItemManager.getItemPrice(itemID); }
    public static AsyncBufferedImage getImage(int itemID){      return WildyAgilityLootTrackerPlugin.ItemManager.getImage(itemID);     }
    public static             String getItemName(int itemID){
        switch(itemID){
            case ItemID.BLIGHTED_ANGLERFISH:    return ITEM_NAME_BLIGHTED_ANGLERFISH;
            case ItemID.BLIGHTED_MANTARAY:      return ITEM_NAME_BLIGHTED_MANTARAY;
            case ItemID.BLIGHTED_KARAMBWAN:     return ITEM_NAME_BLIGHTED_KARAMBWAN;
            case ItemID.BLIGHTED_4DOSE2RESTORE: return ITEM_NAME_BLIGHTED_4DOSE2RESTORE;
            case ItemID.STEEL_PLATEBODY:        return ITEM_NAME_STEEL_PLATEBODY;
            case ItemID.MITHRIL_CHAINBODY:      return ITEM_NAME_MITHRIL_CHAINBODY;
            case ItemID.MITHRIL_PLATELEGS:      return ITEM_NAME_MITHRIL_PLATELEGS;
            case ItemID.MITHRIL_PLATESKIRT:     return ITEM_NAME_MITHRIL_PLATESKIRT;
            case ItemID.ADAMANT_FULL_HELM:      return ITEM_NAME_ADAMANT_FULL_HELM;
            case ItemID.ADAMANT_PLATEBODY:      return ITEM_NAME_ADAMANT_PLATEBODY;
            case ItemID.ADAMANT_PLATELEGS:      return ITEM_NAME_ADAMANT_PLATELEGS;
            case ItemID.RUNE_MED_HELM:          return ITEM_NAME_RUNE_MED_HELM;
            case ItemID.RUNE_CHAINBODY:         return ITEM_NAME_RUNE_CHAINBODY;
            case ItemID.RUNE_KITESHIELD:        return ITEM_NAME_RUNE_KITESHIELD;
            default:                            return "UNRECOGNIZED ITEM_ID";
        }
    }
    public static                int getItemIdFromName(String itemName){
        switch(itemName){
            case ITEM_NAME_BLIGHTED_ANGLERFISH:    return ItemID.BLIGHTED_ANGLERFISH;
            case ITEM_NAME_BLIGHTED_MANTARAY:      return ItemID.BLIGHTED_MANTARAY;
            case ITEM_NAME_BLIGHTED_KARAMBWAN:     return ItemID.BLIGHTED_KARAMBWAN;
            case ITEM_NAME_BLIGHTED_4DOSE2RESTORE: return ItemID.BLIGHTED_4DOSE2RESTORE;
            case ITEM_NAME_STEEL_PLATEBODY:        return ItemID.STEEL_PLATEBODY;
            case ITEM_NAME_MITHRIL_CHAINBODY:      return ItemID.MITHRIL_CHAINBODY;
            case ITEM_NAME_MITHRIL_PLATELEGS:      return ItemID.MITHRIL_PLATELEGS;
            case ITEM_NAME_MITHRIL_PLATESKIRT:     return ItemID.MITHRIL_PLATESKIRT;
            case ITEM_NAME_ADAMANT_FULL_HELM:      return ItemID.ADAMANT_FULL_HELM;
            case ITEM_NAME_ADAMANT_PLATEBODY:      return ItemID.ADAMANT_PLATEBODY;
            case ITEM_NAME_ADAMANT_PLATELEGS:      return ItemID.ADAMANT_PLATELEGS;
            case ITEM_NAME_RUNE_MED_HELM:          return ItemID.RUNE_MED_HELM;
            case ITEM_NAME_RUNE_CHAINBODY:         return ItemID.RUNE_CHAINBODY;
            case ITEM_NAME_RUNE_KITESHIELD:        return ItemID.RUNE_KITESHIELD;
            default:                               return 1; // because this totally won't blow up in our face one day
        }
    }
    public static             String getItemType(int itemID){
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
    public static      LtaLootItem[] getItemsFromIdList(int[] idList){
        int            index = 0;
        LtaLootItem[]  items = new LtaLootItem[idList.length];
        for (int id: idList){ items[index++] = getNewObjectFromId(id); }
        return items;
    }
    public static      LtaLootItem[] getSupplyItems(){          return getItemsFromIdList(getSupplyItemIds());          }
    public static      LtaLootItem[] getSteelArmourItems(){     return getItemsFromIdList(getSteelArmourItemIds());     }
    public static      LtaLootItem[] getMithrilArmourItems(){   return getItemsFromIdList(getMithrilArmourItemIds());   }
    public static      LtaLootItem[] getAdamantArmourItems(){   return getItemsFromIdList(getAdamantArmourItemIds());   }
    public static      LtaLootItem[] getRuneArmourItems(){      return getItemsFromIdList(getRuneArmourItemIds());      }
    public static      LtaLootItem[] getAllArmourItems(){       return getItemsFromIdList(getAllArmourItemIds());       }
    public static             String TO_NUM_STR(int value){     return String.format("%,d", value);                     }
    public static             String toSmallGpStr(int value){   return String.format("%6s GP",  TO_NUM_STR(value));     }
    public static             String toBigGpStr(int value){     return String.format("%10s GP", TO_NUM_STR(value));     }
    private static           boolean IN_ID_LIST(int checkId, int[] list){
        for (int id : list){ if (id == checkId) { return true; } }
        return false;
    }

    public                int id;
    public             String name;
    public             String type;
    public                int price;
    public AsyncBufferedImage image;
    public                int haveQuantity;
    public            boolean display;

    public                    LtaLootItem(int itemID){
        id           = itemID;
        name         = getItemName(id);
        type         = getItemType(id);
        price        = getItemPrice(id);
        image        = getImage(id);
        haveQuantity = 0;
    }
    public               void updateItemPrice(){ price = getItemPrice(id);                       }
    public                int getTotalValue(){   return price * haveQuantity;                    }
    public            boolean isSteelArmour(){   return IN_ID_LIST(id, STEEL_ARMOUR_ITEM_IDS);   }
    public            boolean isMithrilArmour(){ return IN_ID_LIST(id, MITHRIL_ARMOUR_ITEM_IDS); }
    public            boolean isAdamantArmour(){ return IN_ID_LIST(id, ADAMANT_ARMOUR_ITEM_IDS); }
    public            boolean isRuneArmour(){    return IN_ID_LIST(id, RUNE_ARMOUR_ITEM_IDS);    }
    public            boolean isSupplyItem(){    return type.equals(TYPE_SUPPLY);                }
    public            boolean isArmourItem(){    return type.equals(TYPE_ARMOUR);                }
    public                int getArmourType(){
        if (isSteelArmour()){   return TYPE_STEEL;   }
        if (isMithrilArmour()){ return TYPE_MITHRIL; }
        if (isAdamantArmour()){ return TYPE_ADAMANT; }
        if (isRuneArmour()){    return TYPE_RUNE;    }
        return -1;
    }
    public               void _detectIfConfigured(WildyAgilityLootTrackerConfig configObject){
        if (isSupplyItem()){        display = configObject.getShowSupplies();      return; }
        if (isArmourItem()){
            switch(getArmourType()){
                case TYPE_STEEL:    display = configObject.getShowSteelArmour();   break;
                case TYPE_MITHRIL:  display = configObject.getShowMithrilArmour(); break;
                case TYPE_ADAMANT:  display = configObject.getShowAdamantArmour(); break;
                case TYPE_RUNE:     display = configObject.getShowRuneArmour();    break;
            }
        }
    }
    public             String getTotalGpStr(){   return toBigGpStr(getTotalValue());             }
    public             String getPriceGpStr(){   return toSmallGpStr(price);                     }
    public AsyncBufferedImage getQtyImage(){     return WildyAgilityLootTrackerPlugin.ItemManager.getImage(id, haveQuantity, true); }
}