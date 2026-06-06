package com.lita;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.GameState;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.RuneLite;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.externalplugins.ExternalPluginManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.game.ItemManager;
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


*/
/*
    To-Do:
        - Make this thing respect the config
        - Session Management, complete with the ability to preserve historical information on the loot item quantities and prices from previous sessions
        - Make visual overlay configurable/switchable to Swing-based Side Panel
        - Give user's a "clear session" button
        - Detect when user enters or leaves agility arena
        - Detect logouts / logins
        - Give users the ability to override the estimated item prices in the config; This will be on a per-session basis, with the default behavior still being to pull them from the client API at the start of a new session
        - Maybe show additional statistics like the most recent bag-value-increase amount, maybe show an average rate of increase as well
*/
/*
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

            public static final int      GAME_AREA_CLIENT_TICK_EVERY =  25; // half a second
            public static final int      PLUGIN_CHECK_READY_EVERY    = 100; // about 2 seconds
            public static ItemManager    ItemManager;

            public  boolean                        firstUserLoginInit     = false;
            public  boolean                        playerFirstLoad        = false;
            public  boolean                        running                = false;
    @Inject public  Client                         client;
    @Inject public  ClientThread                   clientThread;
    @Inject public  WildyAgilityLootTrackerConfig  config;
    @Inject public  ItemManager                    itemManager;
    @Inject public  OverlayManager                 overlayManager;
    @Inject public  WildyAgilityLootTrackerOverlay overlay;
            public  WildyAgilitySession            currentSession         = null;
            public  WildyAgilityChatParser         chatParser             = null;
            public  WildyAgilityGameArea           activeGameZone         = null;
            public  WildyAgilityDebugHelper        debugHelper            = null;
            public  LtaLootItem[]                  supplies;
            public  LtaLootItem[]                  armour;
            public  String                         currentLootValStr      = null;
           private  int                            clientTickNum          = 0;


    public void startSession(){       this.currentSession.start();     }
    public void endSession(){         this.currentSession.end(); clearCurrentState();     }
    public void saveCurrentSession(){ this.currentSession.save();    }
    public void clearCurrentState(){
        this.currentSession = null;
        this.supplies       = null;
        this.armour         = null;
    }
    public void init_LootItems(){
        log.debug("Initializing LtaLootItem objects for supply and armour item tracking...");
        this.supplies   = LtaLootItem.getSupplyItems();
        this.armour     = LtaLootItem.getAllArmourItems();
        log.debug("Updating each LtaLootItem's display prop to match current config...");
        for (LtaLootItem supplyItem: supplies){
            supplyItem._detectIfConfigured(this.config);
        }
        for (LtaLootItem armourItem: armour){
            armourItem._detectIfConfigured(this.config);
        }
    }
    public void startInit(){
        this.currentSession = new WildyAgilitySession(this);
        this.chatParser     = new WildyAgilityChatParser(this);
        this.activeGameZone = new WildyAgilityGameArea(this);
        this.debugHelper    = new WildyAgilityDebugHelper(this);
        WildyAgilityLootTrackerPlugin.ItemManager = this.itemManager;

    }
    public void finishInit(){
        this.clientThread.invoke(() -> {
            init_LootItems();
            this.currentLootValStr = getTotalLootValueStr();
        });
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
    public LtaLootItem getMatchingSupplyItem(int itemId){ return getMatchingLootItem(itemId, supplies); }
    public LtaLootItem getMatchingArmourItem(int itemId){ return getMatchingLootItem(itemId, armour);   }

    public void updateLootItemPrices(LtaLootItem[] items){
        for (LtaLootItem itm: items){ itm.updateItemPrice(); }
    }
    public void updateItemPrices(){
        updateLootItemPrices(this.supplies);
        updateLootItemPrices(this.armour);
    }

    public void onDataMutation(LtaLootItem[] mutatedObjects){
        this.currentLootValStr = getTotalLootValueStr(); // by doing this here and now, we can cut down on string-formatting stuff happening in the OverlayPanel's render call
        this.overlay.onDataMutation(mutatedObjects);  // ensure OverlayPanel's component structure only gets rebuilt when an awardMessage is actually detected and parsed
    }

    @Override
    protected void startUp() throws Exception
    {
        log.debug("WildyAgilityLootTrackerPlugin started!");
        startInit();
        finishInit();
        checkReadyToRun();
    }

    @Override
    protected void shutDown() throws Exception
    {
        this.running = false;
        endSession();
        this.overlay.onShutdown();
        log.debug("WildyAgilityLootTrackerPlugin stopped!");
    }




    public boolean detectPlayerInZone(){
        return this.activeGameZone.isPlayerInZone();
    }
    public boolean isUserReady(){
        return playerFirstLoad && this.currentSession.isActive();
    }
    public void onUserReady(){
        log.debug("onUserReady()");
        startRunning();
    }

    public boolean checkReadyToRun(){
        if (! isUserReady()){ return false; }
        if (! detectPlayerInZone()){ return false; }
        onUserReady();
        return true;
    }

    public void onUserLogin(){
        log.debug("onUserLogin()");
        if (! firstUserLoginInit) {
            clientThread.invokeLater(() -> {
                if (this.client.getLocalPlayer() == null){ return false; } //dont actually need the player, just need to know client has reached this point in initialization/lifecycle
                else {
                    log.debug("User has logged in. Local player is not null. This is the first and only time this method should fire.");
                    playerFirstLoad = true;
                    return true;
                }
            });
            firstUserLoginInit = true; // since invokeLater will keep retrying, we can immediately act as if the call has been made and set it such that it won't try to call invokeLater again
        }
    }

    public void startRunning(){
        log.debug("startRunning()");
        this.running = true;
        this.debugHelper.reportRunning();
        this.overlay.onStartUp();
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged)
    {
        switch( gameStateChanged.getGameState() ){
            case CONNECTION_LOST:            log.debug("GameState.CONNECTION_LOST");                             break;
            case HOPPING:                    log.debug("GameState.HOPPING");                                     break;
            case LOADING:                    log.debug("GameState.LOADING");                                     break;
            case LOGGED_IN:                  log.debug("GameState.LOGGED_IN"); onUserLogin();                    break;
            case LOGGING_IN:                 log.debug("GameState.LOGGING_IN");                                  break;
            case LOGIN_SCREEN:               log.debug("GameState.LOGIN_SCREEN");                                break;
            case LOGIN_SCREEN_AUTHENTICATOR: log.debug("GameState.LOGIN_SCREEN_AUTHENTICATOR");                  break;
            case STARTING:                   log.debug("GameState.STARTING");                                    break;
            case UNKNOWN:                    log.debug("GameState.UNKNOWN");                                     break;
        }
    }
    @Subscribe
    public void onConfigChanged(ConfigChanged cfgEvent){
        log.debug("onConfigChanged()");
    }
    public boolean isTargetTick(final int TICK_FREQUENCY){ return ((this.clientTickNum % TICK_FREQUENCY) == 0); }
    @Subscribe
    public void onClientTick(ClientTick clientTick){
        this.clientTickNum++;
        if (isTargetTick(GAME_AREA_CLIENT_TICK_EVERY)){ this.activeGameZone.onTick(); }
        if (isTargetTick(PLUGIN_CHECK_READY_EVERY)){    if (! this.running){ checkReadyToRun(); }  }
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
        String streakS = String.valueOf(this.currentSession.streak);
        String lapCntS = String.valueOf(this.currentSession.lapNum);
        String bagValS = getTotalLootValueStr();
        return String.format("===== Streak %4s : Lap %-7s => Bag Value %s =====\r\n", streakS, lapCntS, bagValS);
    }
    public void printCheckPointBanner(){
        String bStr = getCheckpointBannerStr();
        log.info(bStr);
        client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", bStr, null);
    }

    public void updateLapCount(String msgMatchStr){
        this.currentSession.lapNum = Integer.parseInt(msgMatchStr);
        this.currentSession.lapCount++;
    }
    public void updateStreak(String msgMatchStr){
        this.currentSession.streak = Integer.parseInt(msgMatchStr);
        if ((this.currentSession.streak % 20) == 0){
            updateItemPrices();
        }
    }



    @Subscribe
    public void onChatMessage(ChatMessage cMsgEvent){
        // log.debug("WildyAgilityLootTrackerPlugin->onChatMessage()");
        if (cMsgEvent.getType() == ChatMessageType.GAMEMESSAGE){
            String  msg = cMsgEvent.getMessage();
            if (this.chatParser.checkIsLapCountMessage(msg)){ return; }
            if (this.chatParser.checkIsStreakMessage(msg)){ return; }
            if (this.chatParser.checkIsAwardMessage(msg)){ return; }
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
/* I really need a fucking flow chart....

    startUp()
        startInit()



 */