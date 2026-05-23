package com.lita;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.game.ItemManager;
import net.runelite.client.util.AsyncBufferedImage;
import net.runelite.api.gameval.ItemID;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*  Splunk Search
```
    index=runelite* ("Wilderness Agility" OR "you have been awarded")
    | rex "You have been awarded [<]col[=]ef1020[>](?<supply_item_str>[^<]+)<.col> and [<]col[=]ef1020[>](?<loot_item_str>[^<]+)"
    | rex "Your Wilderness Agility reward streak is: <col=ff0000>(?<streak>[0-9]+)"
    | rex "Your Wilderness Agility lap count is: <col=ff0000>(?<lap_count>[0-9]+)"
    | transaction startswith="lap_count!=null()" endswith="loot_item_str!=null()"
    | eval loot_props_array=split(loot_item_str, " x ")
    | eval supply_props_array=split(supply_item_str, " x ")
    | eval loot_item=mvindex(loot_props_array, 1)
    | eval loot_item_qty=mvindex(loot_props_array, 0)
    | eval loot_item_array=split(loot_item, " ")
    | eval loot_item_category=mvindex(loot_item_array, 0)
    | eval supply_item=mvindex(supply_props_array, 1)
    | eval supply_item_qty=mvindex(supply_props_array, 0)
```
*/

/* Notes - I'm going to put notes here until I figure out what I'm doing

   Basic Idea:
        OnStartup
            Grab a list of Item Prices for all the possible wildy loot items, and setup an initial mapping between item and its price
        
        ?????????
            Detect or in some way verify that the user has entered the agility arena (should a session be continued across logins over time?)
            Start a session, using the correct session number and encoding appropriate metadata (like datetime)
            
        OnChatMessage
            Determine if its a wildy-agility type game message (regex pattern matching probably)
            Parse Out the message
            Update session structures to reflect the addition of the new loot items
                Keep track of streak and lap info?
            
*/
/*      Items we care about: (not necessarily ordered in any deeply meaningful way)
        
        net.runelite.api.gameval
            BLIGHTED_MANTARAY
            BLIGHTED_ANGLERFISH
            BLIGHTED_KARAMBWAN
            BLIGHTED_4DOSE2RESTORE
            STEEL_PLATEBODY
            MITHRIL_PLATELEGS
            MITHRIL_CHAINBODY
            MITHRIL_PLATESKIRT
            ADAMANT_FULL_HELM
            ADAMANT_PLATEBODY
            ADAMANT_PLATELEGS
            RUNE_MED_HELM
            RUNE_CHAINBODY
            RUNE_KITESHIELD
*/


class LtaLootItem {
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

@Slf4j
@PluginDescriptor(
    name = "WildyAgilityLootTracker"
)
public class WildyAgilityLootTrackerPlugin extends Plugin
{

    public LtaLootItem[] supplies;
    public LtaLootItem[] armour;

    public Pattern awardP;
    public Pattern highlightP;
    public Pattern itemP;

    public static net.runelite.client.game.ItemManager _ItemManager;

    @Inject
    private Client client;

    @Inject
    private WildyAgilityLootTrackerConfig config;



    public void debug_LogCurrentItems(){
        log.debug("===== Supplies =====");
        for (LtaLootItem supplyItem: supplies){
            log.debug(supplyItem.toDebugString());
        }
        log.debug("===== Armour   =====");
        for (LtaLootItem armourItem: armour){
            log.debug(armourItem.toDebugString());
        }
    }



    @Override
    protected void startUp() throws Exception
    {
        log.debug("WildyAgilityLootTrackerPlugin started!");
        awardP     = Pattern.compile("You have been awarded");
        highlightP = Pattern.compile("[<]col[=]ef1020[>]([^<]+)[<].col[>]");
        itemP      = Pattern.compile("([0-9]+) x (.+)");
    }

    @Override
    protected void shutDown() throws Exception
    {
        log.debug("WildyAgilityLootTrackerPlugin stopped!");
    }



    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged)
    {
        if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
        {
            client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "WildyAgilityLootTrackerPlugin says " + config.greeting(), null);
        }
    }
    
    /*
        net.runelite.api.ChatMessageType
        net.runelite.api.events.ChatMessage
    */
    @Subscribe
    public void onChatMessage(ChatMessage cMsgEvent){
        if (cMsgEvent.getType() == ChatMessageType.GAMEMESSAGE){
            String  msg = cMsgEvent.getMessage();
            Matcher  _m = awardP.matcher(msg);
            if (_m.matches()){
                Matcher _hm = highlightP.matcher(msg);
                _hm.find();
                String hStr = _hm.group(1);
                Matcher _im = itemP.matcher(hStr);
                _im.find();
                String supplyQtyS = _im.group(1);
                String supplyItemS = _im.group(2);
                _hm.find();
                hStr = _hm.group(1);
                _im  = itemP.matcher(hStr);
                _im.find();
                String armourQtyS = _im.group(1);
                String armourItemS = _im.group(2);
                
                log.debug(String.format("Parsed -> (Supply : Qty=%s , Name=%s) : (Armour : Qty=%s, Name=%s)", supplyQtyS, supplyItemS, armourQtyS, armourItemS));
            }
        }
    }

    @Provides
    WildyAgilityLootTrackerConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(WildyAgilityLootTrackerConfig.class);
    }
}