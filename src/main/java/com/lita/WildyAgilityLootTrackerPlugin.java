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
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.externalplugins.ExternalPluginManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.game.ItemManager;
import net.runelite.client.util.AsyncBufferedImage;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.ui.overlay.OverlayManager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.time.LocalDateTime;



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



    To-Do:
        - Figure out why the client occasionally crashes, I suspect this still has something to do with trying to call that init_LootItems() in the Plugin's main startUp()
        - Make this thing respect the config
        - Session Management, complete with the ability to preserve historical information on the loot item quantities and prices from previous sessions
        - Make the visual Overlay
        - Give user's a "clear session" button
        - Detect when user enters or leaves agility arena
        - Detect logouts / logins
        - Give users the ability to override the estimated item prices in the config; This will be on a per-session basis, with the default behavior still being to pull them from the client API at the start of a new session
        - Maybe show additional statistics like the most recent bag-value-increase amount, maybe show an average rate of increase as well

    Client Events we should probably be listening to:
        ClientShutdown
        ConfigChanged
        ConfigSync
        RuneScapeProfileChanged
        SessionOpen
        SessionClose


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
    public Pattern rewardStreakP;
    public Pattern lapCountP;
    public boolean saidHi = false;

    public static ItemManager _ItemManager;

    @Inject
    private Client client;
    @Inject
    private ClientThread clientThread;
    @Inject
    private WildyAgilityLootTrackerConfig config;
    @Inject
    private ItemManager __ItemManager;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private WildyAgilityLootTrackerOverlay overlay;

    public           int currentSessionNum      = 0; // this is for the future, but won't be reliable until later
    public LocalDateTime currentSessionStartDt  = null;
    public LocalDateTime currentSessionEndDt    = null;
    public           int currentStreak          = 0;
    public           int currentLap             = 0;
    public        String currentLootValStr      = null;


    public void startSession(){
        this.currentSessionNum++;
        this.currentSessionStartDt = LocalDateTime.now();
        log.info("Started Session " + Integer.toString(this.currentSessionNum, 10));
    }
    public void endSession(){
        log.info("Ending session...");
        this.currentSessionEndDt = LocalDateTime.now();
        saveCurrentSession();
        clearCurrentState();
    }
    public void saveCurrentSession(){
        // to-do
        log.info("To-Do : Implement saveCurrentSession()");
    }
    public void clearCurrentState(){
        this.currentStreak = 0;
        this.currentLap    = 0;
        this.supplies      = null;
        this.armour        = null;
        //init_LootItems();
        //startSession();
    }
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

    public void init_LootItems(){
        log.info("Initializing LtaLootItem objects for supply and armour item tracking...");
        supplies   = LtaLootItem.getSupplyItems();
        armour     = LtaLootItem.getAllArmourItems();
        log.info("Updating each LtaLootItem's display prop to match current config...");
        for (LtaLootItem supplyItem: supplies){
            supplyItem._detectIfConfigured(this.config);
        }
        for (LtaLootItem armourItem: armour){
            armourItem._detectIfConfigured(this.config);
        }
    }
    public LtaLootItem getMatchingLootItem(int itemId, LtaLootItem[] searchList){
                int index         = 0;
        LtaLootItem matchedObject = null;
        while ((index < searchList.length) && (matchedObject == null)){
            if (searchList[index].id == itemId){ matchedObject = searchList[index]; }
            index++;
        }
        return matchedObject;
    }
    public LtaLootItem getMatchingSupplyItem(int itemId){
        return getMatchingLootItem(itemId, supplies);
    }
    public LtaLootItem getMatchingArmourItem(int itemId){
        return getMatchingLootItem(itemId, armour);
    }

    public void onDataMutation(LtaLootItem[] mutatedObjects){

        this.currentLootValStr = getTotalLootValueStr(); // by doing this here and now, we can cut down on string-formatting stuff happening in the OverlayPanel's render call

        this.overlay.onDataMutation(mutatedObjects);  // ensure OverlayPanel's component structure only gets rebuilt when an awardMessage is actually detected and parsed
    }

    @Override
    protected void startUp() throws Exception
    {
        log.info("WildyAgilityLootTrackerPlugin started!");
        awardP        = Pattern.compile("You have been awarded");
        highlightP    = Pattern.compile("[<]col[=]ef1020[>]([^<]+)[<].col[>]");
        itemP         = Pattern.compile("([0-9]+) x (.+)");
        rewardStreakP = Pattern.compile("Wilderness Agility reward streak is: .col=ff0000.([0-9]+)");
        lapCountP     = Pattern.compile("Wilderness Agility lap count is: .col=ff0000.([0-9]+)");
        WildyAgilityLootTrackerPlugin._ItemManager = this.__ItemManager;
        if (this.__ItemManager == null){
            log.info("__ItemManager is null");
        }
        else {
            log.info(String.format("Got ItemManager@%s", Integer.toHexString(this.__ItemManager.hashCode()))); // toString() method uses reflection, which runelite dev practices discourage, so we do this instead
        }

        /* It doesn't feel right to throw this part in startup, given the io costs and such, but until I better learn the plugin event lifecycle, this will just have to do
           also if this is only called once, yet the user has a habit of logging in and out over and over again over time without relaunching runelite (*cough*, like you) then
           the price data could get stale, we should think about detecting price changes and config changes that occur after startup
        */
        log.info("Deferring further initialization to run on the clientThread at a later time");
        clientThread.invokeLater(() -> {
            //init_LootItems(); // let's defer this until even further in the lifecycle, after the first successful user login...
            startSession();
            // overlayManager.add(overlay); // let's defer this until even further in the lifecycle, after the first successful user login...
        });
    }

    @Override
    protected void shutDown() throws Exception
    {
        endSession();
        log.info("WildyAgilityLootTrackerPlugin stopped!");
    }

    public void reportRunning(){
        String reportMessage = "WildyAgilityLootTrackerPlugin is running and ready.";
        log.info(reportMessage);
        log.info("deferring sending game message to occur at a later time on the client thread");
        clientThread.invokeLater(() -> {
            client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", reportMessage, null);
        });
    }
    public void finishInit(){
        init_LootItems();
        this.currentLootValStr = getTotalLootValueStr();
        overlayManager.add(overlay);
        reportRunning();
    }

    public void onUserLogin(){
        log.info("onUserLogin() => called");
        if (! saidHi) {
            clientThread.invokeLater(() -> {
                if (this.client.getLocalPlayer() == null){ return false; }
                else {
                    log.info("User has logged in. Local player is not null. This is the first and only time this method should fire.");
                    finishInit();
                    return true;
                }
            });
            saidHi = true; // since invokeLater will keep retrying, we can immediately act as if the call has been made and set it such that it won't try to call invokeLater again
        }
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged)
    {
        if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
        {
            onUserLogin();
        }
    }

    public int[] parseOut_LootItem(Matcher highlightMatcher){
        if (! highlightMatcher.find()) { log.info("Failed to match highlight pattern"); return new int[] {1, 0}; } // i know this will bite me in the ass, I'll fix it later
        String hStr = highlightMatcher.group(1);
        Matcher itemMatcher = itemP.matcher(hStr);
        if (! itemMatcher.find()) { log.info("Failed to match item pattern"); return new int[] {1, 0}; }           // i know this will bite me in the ass, I'll fix it later
        String QtyS  = itemMatcher.group(1);
        String ItemS = itemMatcher.group(2);
        int    ItemId = LtaLootItem.getItemIdFromName(ItemS);
        int       Qty = Integer.parseInt(QtyS);
        return new int[] { ItemId, Qty };
    }
    public LtaLootItem parseOut_SupplyItem(Matcher highlightMatcher){
              int[] parsedValues = parseOut_LootItem(highlightMatcher);
                int itemId       = parsedValues[0];
                int Qty          = parsedValues[1];
        LtaLootItem supplyItem   = getMatchingSupplyItem(itemId);
        supplyItem.haveQuantity += Qty;
        return supplyItem;
    }
    public LtaLootItem parseOut_ArmourItem(Matcher highlightMatcher){
              int[] parsedValues = parseOut_LootItem(highlightMatcher);
                int itemId       = parsedValues[0];
                int Qty          = parsedValues[1];
        LtaLootItem armourItem   = getMatchingArmourItem(itemId);
        armourItem.haveQuantity += Qty;
        return armourItem;
    }
    public LtaLootItem[] parseAwardMessage(String msg){
        Matcher _hm = highlightP.matcher(msg);
        LtaLootItem updatedSupplyItem = parseOut_SupplyItem(_hm);
        LtaLootItem updatedArmourItem = parseOut_ArmourItem(_hm);
        //log.info(String.format("Parsed -> (Supply : Qty=%s , Name=%s) : (Armour : Qty=%s, Name=%s)", supplyQtyS, supplyItemS, armourQtyS, armourItemS));
        log.info(String.format("Parsed & Updated [S] -> %s", updatedSupplyItem.toDebugString()));
        log.info(String.format("Parsed & Updated [A] -> %s", updatedArmourItem.toDebugString()));
        printCheckPointBanner();
        return new LtaLootItem[] {
                updatedSupplyItem,
                updatedArmourItem
        };
    }
    public int getTotalLootValue(){
        int total = 0;
        for (LtaLootItem sItem: supplies){
            total += sItem.getTotalValue();
        }
        for (LtaLootItem aItem: armour){
            total += aItem.getTotalValue();
        }
        return total;
    }
    public String getTotalLootValueStr(){
        return String.format("%14s", String.format("%,d GP", getTotalLootValue()));
    }
    public String getCheckpointBannerStr(){
        String streakS = String.valueOf(this.currentStreak);
        String lapCntS = String.valueOf(this.currentLap);
        String bagValS = getTotalLootValueStr();
        return String.format("===== Streak %4s : Lap %-7s => Bag Value %s =====\r\n", streakS, lapCntS, bagValS);
    }
    public void printCheckPointBanner(){
        String bStr = getCheckpointBannerStr();
        log.info(bStr);
        client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", bStr, null);
    }
    public boolean checkIsAwardMessage(String chatMessage){
        Matcher  _m = awardP.matcher(chatMessage);
        if (! _m.find()){
            //log.info("Game Message does not match the award text pattern");
            //log.info(String.format("Game Message was: '%s'", msg));
            return false;
        }
        log.info("Received Game Message matching the award format pattern, proceeding to parse it out.");
        LtaLootItem[] mutatedObjects = parseAwardMessage(chatMessage);
        onDataMutation(mutatedObjects);
        return true;
    }
    public void updateLapCount(String msgMatchStr){
        this.currentLap = Integer.parseInt(msgMatchStr);
    }
    public void updateStreak(String msgMatchStr){
        this.currentStreak = Integer.parseInt(msgMatchStr);
    }

    public boolean checkIsLapCountMessage(String chatMessage){
        Matcher _matcher = lapCountP.matcher(chatMessage);
        if (! _matcher.find()){ return false; }
        updateLapCount(_matcher.group(1));
        return true;
    }
    public boolean checkIsStreakMessage(String chatMessage){
        Matcher _matcher = rewardStreakP.matcher(chatMessage);
        if (! _matcher.find()){ return false; }
        updateStreak(_matcher.group(1));
        return true;
    }

    /*
        net.runelite.api.ChatMessageType
        net.runelite.api.events.ChatMessage
    */
    @Subscribe
    public void onChatMessage(ChatMessage cMsgEvent){
        // log.info("WildyAgilityLootTrackerPlugin->onChatMessage()");
        if (cMsgEvent.getType() == ChatMessageType.GAMEMESSAGE){
            String  msg = cMsgEvent.getMessage();
            if (checkIsLapCountMessage(msg)){ return; }
            if (checkIsStreakMessage(msg)){ return; }
            if (checkIsAwardMessage(msg)){ return; }
            // additional checks
        }
    }

    @Provides
    WildyAgilityLootTrackerConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(WildyAgilityLootTrackerConfig.class);
    }




    public static void main(String[] args) throws Exception {
        ExternalPluginManager.loadBuiltin(WildyAgilityLootTrackerPlugin.class);
        RuneLite.main(args);
    }
}

/*  Execution (because my dumbass forgets everything)

        .\gradlew.bat runMain --info

    later we can add a gradle exec task to run the shadowJar:

        java -ea -jar "C:\runelite-plugin-devel\Wildy_Agility_Loot_Tracker\build\libs\WildyAgilityLootTracker-1.0.0-all.jar" --developer-mode --debug

*/