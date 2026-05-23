package com.lita;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.externalplugins.ExternalPluginManager;
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
    public boolean saidHi = false;

    public static ItemManager _ItemManager;

    @Inject
    private Client client;

    @Inject
    private WildyAgilityLootTrackerConfig config;



    public void debug_LogCurrentItems(){
        log.info("===== Supplies =====");
        for (LtaLootItem supplyItem: supplies){
            log.info(supplyItem.toDebugString());
        }
        log.info("===== Armour   =====");
        for (LtaLootItem armourItem: armour){
            log.info(armourItem.toDebugString());
        }
    }



    @Override
    protected void startUp() throws Exception
    {
        log.info("WildyAgilityLootTrackerPlugin started!");
        awardP     = Pattern.compile("You have been awarded");
        highlightP = Pattern.compile("[<]col[=]ef1020[>]([^<]+)[<].col[>]");
        itemP      = Pattern.compile("([0-9]+) x (.+)");
    }

    @Override
    protected void shutDown() throws Exception
    {
        log.info("WildyAgilityLootTrackerPlugin stopped!");
    }



    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged)
    {
        if ((gameStateChanged.getGameState() == GameState.LOGGED_IN) && (! saidHi) && (this.client.getLocalPlayer() != null))
        {
            client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "WildyAgilityLootTrackerPlugin says " + config.greeting(), null);
            saidHi = true;
        }
    }
    
    /*
        net.runelite.api.ChatMessageType
        net.runelite.api.events.ChatMessage
    */
    @Subscribe
    public void onChatMessage(ChatMessage cMsgEvent){
        log.info("WildyAgilityLootTrackerPlugin->onChatMessage()");
        if (cMsgEvent.getType() == ChatMessageType.GAMEMESSAGE){
            String  msg = cMsgEvent.getMessage();
            Matcher  _m = awardP.matcher(msg);
            if (_m.matches()){
                Matcher _hm = highlightP.matcher(msg);
                if (! _hm.find()) { log.info("Award Pattern Matched, but not the highlight pattern"); return; }
                String hStr = _hm.group(1);
                Matcher _im = itemP.matcher(hStr);
                if (! _im.find()) { log.info("Highlight Pattern Matched, but not the item pattern"); return; }
                String supplyQtyS  = _im.group(1);
                String supplyItemS = _im.group(2);
                if (! _hm.find()){ log.info("Failed to find 2nd highlight match"); return; };
                hStr = _hm.group(1);
                _im  = itemP.matcher(hStr);
                if (! _im.find()){ log.info("Highlight Pattern Matched, but not the item pattern (2nd call)"); return; };
                String armourQtyS = _im.group(1);
                String armourItemS = _im.group(2);
                log.info(String.format("Parsed -> (Supply : Qty=%s , Name=%s) : (Armour : Qty=%s, Name=%s)", supplyQtyS, supplyItemS, armourQtyS, armourItemS));
            }
            else {
                log.info("Game Message does not match the award text pattern");
            }
        }
    }

    @Provides
    WildyAgilityLootTrackerConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(WildyAgilityLootTrackerConfig.class);
    }

    public static void main(String[] args) throws Exception {
        //log.debug("This plugin isn't really meant to be called in standalone fashion...");
        ExternalPluginManager.loadBuiltin(WildyAgilityLootTrackerPlugin.class);
        RuneLite.main(args);
    }
}

// java -ea -jar "C:\runelite-plugin-devel\Wildy_Agility_Loot_Tracker\build\libs\WildyAgilityLootTracker-unspecified-all.jar" --developer-mode --debug
// java -ea -jar "C:\runelite-plugin-devel\Wildy_Agility_Loot_Tracker\build\libs\WildyAgilityLootTracker-unspecified-all.jar" --developer-mode