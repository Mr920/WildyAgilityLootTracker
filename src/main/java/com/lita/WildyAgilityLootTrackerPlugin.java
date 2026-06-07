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
import net.runelite.client.chat.ChatMessageManager;
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
            public static final int      PLUGIN_CHECK_READY_EVERY    =  50; // about 1 seconds
            public static final String   CONFIG_GROUP_NAME           = "WildyAgilityLootTracker";
            public static ItemManager    ItemManager;

            /* Since I suck at flow control management, I need a series of flip-once toggles to help keep track of where we are and what has fired
               the goal of this is more fined-tuning state checking and the ability to stop execution in its tracks if it seems we are doing something redundant without first having
               reinitialized everything properly

               Yes I know how redundant and ridiculous this is, but until I can get things under control this is how we're doing it
            */
            public  boolean                        _s_startedPlugin       = false;
            public  boolean                        _s_stoppedPlugin       = false;
            public  boolean                        _s_initialized         = false;
            public  boolean                        _s_initLoot            = false;
            public  boolean                        _s_startedSession      = false;
            public  boolean                        _s_finishedSession     = false;
            public  boolean                        _s_userReady           = false;
            public  boolean                        _s_startedRun          = false;
            public  boolean                        _s_finishedRun         = false;
            public  boolean                        _s_enteredZone         = false;
            public  boolean                        _s_exitedZone          = false;
            public  boolean                        _s_inZone              = false;
            public  boolean                        _s_firstPlayerLogin    = false;
            public  boolean                        _s_firstPlayerLoad     = false;
            public  boolean                        _s_sessionReady        = false;
            public  boolean                        _s_zoneReady           = false;
            public  boolean                        _s_readyToRun          = false;

            public  boolean                        _s_GS_STARTING         = false;
            public  boolean                        _s_GS_LOGIN_SCREEN     = false;
            public  boolean                        _s_GS_LOGGING_IN       = false;
            public  boolean                        _s_GS_LOGGED_IN        = false;
            public  boolean                        _s_GS_LOADING          = false;

            public  boolean                        _autoCheckRunState     = true;
            public  boolean                        _autoMonitorGameZone   = true;

    @Inject public  Client                         client;
    @Inject public  ClientThread                   clientThread;
    @Inject public  WildyAgilityLootTrackerConfig  config;
    @Inject public  ItemManager                    itemManager;
    @Inject public  ChatMessageManager             chatMessageManager;
    @Inject public  OverlayManager                 overlayManager;
    @Inject public  WildyAgilityLootTrackerOverlay overlay;

            public  WildyAgilitySession            currentSession         = null;
            public  WildyAgilityChatParser         chatParser             = null;
            public  WildyAgilityGameArea           activeGameZone         = null;
            public  WildyAgilityDebugHelper        debugHelper            = null;
            public  LtaLootItem[]                  supplies               = null;
            public  LtaLootItem[]                  armour                 = null;
            public  String                         currentLootValStr      = null;
           private  int                            clientTickNum          = 0;

   /* be careful with this one...
      it is likely to cause bugs, should really only be used in a hard-kill type situation in conjunction with other cleanup functions
    */
    public void RESET_ALL_STATE_FLAGS(){
           this._s_startedPlugin = false;   this._s_stoppedPlugin = false;     this._s_initialized = false;
                this._s_initLoot = false;  this._s_startedSession = false; this._s_finishedSession = false;
               this._s_userReady = false;      this._s_startedRun = false;     this._s_finishedRun = false;
             this._s_enteredZone = false;      this._s_exitedZone = false;          this._s_inZone = false;
        this._s_firstPlayerLogin = false; this._s_firstPlayerLoad = false;      this._s_readyToRun = false;
        this._s_sessionReady     = false;       this._s_zoneReady = false;
    }
    public void PREPARE_FLAG_FOR_NEXT_RUN(){
        this._s_stoppedPlugin   = false;
      //this._s_initLoot        = false;
        this._s_startedSession  = false;
        this._s_finishedSession = false;
        this._s_startedRun      = false;
        this._s_finishedRun     = false;
        this._s_enteredZone     = false;
        this._s_exitedZone      = false;
        this._s_inZone          = false;
        this._s_readyToRun      = false;
        this._s_sessionReady    = false;
        this._s_zoneReady       = false;
    }
    public void startSession(){
        if (! _s_startedSession){
            this.currentSession.start();
             this._s_startedSession = true;
            this._s_finishedSession = false;
        }
        else {
            if (this.currentSession.isActive()){
                endSession();
                startSession();
            }
            else {
                this.currentSession = new WildyAgilitySession(this);
                this.currentSession.start();
                this._s_startedSession  = true;
                this._s_finishedSession = false;
            }
        }
    }
    public void endSession(){
        if (_s_startedSession && (! _s_finishedSession)){
            this.currentSession.end();
            this._s_finishedSession = true; /* clearCurrentState(); */
        }
    }
    public void saveCurrentSession(){ this.currentSession.save();    }
    /*
    public void clearCurrentState(){
        this.currentSession = null;
        this.chatParser     = null;
        this.activeGameZone = null;
        this.debugHelper    = null;
        this.supplies       = null;
        this.armour         = null;
    }
    */
    public void updateItemDisplayConfig(LtaLootItem[] items){
        for (LtaLootItem item: items){
            item._detectIfConfigured(this.config);
        }
    }
    public void updateItemDisplayConfigs(){
        log.debug("updateItemDisplayConfig(this.supplies)");
        updateItemDisplayConfig(this.supplies);
        log.debug("updateItemDisplayConfig(this.armour)");
        updateItemDisplayConfig(this.armour);
    }
    public void init_LootItems(){
        if (! _s_initLoot) {
            this._s_initLoot = true;
            log.debug("Initializing : this.supplies -> LtaLootItem.getSupplyItems()");
            this.supplies = LtaLootItem.getSupplyItems();
            log.debug("Initializing : this.armour   -> LtaLootItem.getAllArmourItems()");
            this.armour   = LtaLootItem.getAllArmourItems();
            updateItemDisplayConfigs();
        }
    }
    public void _init(){
        if (! _s_initialized) {
            this._s_initialized = true;
            log.debug("_init()");
            this.currentSession = new WildyAgilitySession(this);
            this.chatParser     = new WildyAgilityChatParser(this);
            this.activeGameZone = new WildyAgilityGameArea(this);
            this.debugHelper    = new WildyAgilityDebugHelper(this);
            WildyAgilityLootTrackerPlugin.ItemManager = this.itemManager; // this was seriously the stupidest design decision ever, PLEASE refactor this to not be necessary anymore
            this.clientThread.invoke(() -> {
                init_LootItems();
                this.currentLootValStr = getTotalLootValueStr();
            });
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
    protected void startUp() throws Exception {
        if (! _s_startedPlugin) {
            this._s_startedPlugin = true;
            this._s_stoppedPlugin = false;
            log.debug("startUp():START");
            _init();
            //this.running = true;
            //checkReadyToRun();
            log.debug("startUp():END");
        }
    }

    @Override
    protected void shutDown() throws Exception {
        if (! _s_stoppedPlugin) {
            this._s_stoppedPlugin = true;
            log.debug("shutDown():START");
            //this.running = false;
            endSession();
            this.overlay.onShutdown();
            log.debug("shutDown():END");
        }
    }



    /* activeGameZone is doing its own monitoring, I don't think this method is really required....
    public boolean detectPlayerInZone(){
        this._s_inZone = this.activeGameZone.isPlayerInZone();
        return this._s_inZone;
    }
    */
    public void onReadyToRun(){
        this._autoCheckRunState = false;
        log.debug("onReadyToRun() -> startRunning()");
        startRunning();
    }

    public boolean checkReadyToRun(){
        if (! _s_readyToRun){
            if (! checkGameStateReady()){  return false; }
            if (! checkUserLoginScreen()){ return false; }
            if (! checkUserLoaded()){      return false; }
            if (! checkUserReady()){       return false; }
            if (! checkSessionReady()){    return false; }
            if (! checkZoneReady()){       return false; }
            this._s_readyToRun = true;
        }
        // consider doing any final conditional checks
        return this._s_readyToRun;
    }
    public boolean checkGameStateReady(){
        if (! this._s_GS_LOGGED_IN){
            // no conditional checks to be done here, as this is managed by the onGameStateChanged() listener
            debugLog_statusCall("checkGameStateReady()", "_s_GS_LOGGED_IN", "false"); return false;
        }
        else {
            debugLog_statusCall("checkGameStateReady()", "_s_GS_LOGGED_IN", "true");
        }
        return this._s_GS_LOGGED_IN;
    }
    public boolean checkUserLoginScreen(){
        if (! _s_firstPlayerLogin) {
            if (! _s_GS_LOGGED_IN) { debugLog_statusCall("checkUserLoginScreen()", "_s_GS_LOGGED_IN", "false"); return false; }
            else {
                this._s_firstPlayerLogin = true;
                debugLog_statusCall("checkUserLoginScreen()", "_s_firstPlayerLogin", "true");
            }
        }
        return _s_firstPlayerLogin;
    }
    public boolean checkUserLoaded(){
        if (! _s_firstPlayerLoad){
            if (this.client.getLocalPlayer() == null){
                debugLog_statusCall("checkUserLoaded()", "_s_firstPlayerLoad", "false");
                return false;
            }
            else {
                this._s_firstPlayerLoad = true;
                debugLog_statusCall("checkUserLoaded()", "_s_firstPlayerLoad", "true");
            }
        }
        return this._s_firstPlayerLoad;
    }
    public boolean checkSessionReady(){
        if (! this._s_sessionReady){
            if (this.currentSession.isActive()){ debugLog_statusCall("checkSessionReady()", "currentSession.isActive()", "true"); return false; }
            else {
                this._s_sessionReady = true;
                debugLog_statusCall("checkSessionReady()", "_s_sessionReady", "true");
            }
        }
        return this._s_sessionReady;
    }
    public boolean checkUserReady(){
        if (! _s_userReady) {
            if (! _s_firstPlayerLoad){   debugLog_statusCall("checkUserReady()", "_s_firstPlayerLoad", "false"); return false; }
            else {
                this._s_userReady = true;
                debugLog_statusCall("checkUserReady()", "_s_userReady", "true");
            }
        }
        return this._s_userReady; // even though nothing executed, still return correct state
    }
    public boolean checkZoneReady(){
        if (! _s_zoneReady){
            //if (! detectPlayerInZone()){ log.debug("checkZoneReady() -> detectPlayerInZone => false"); return false; } // this is expensive and not really required since I have the GameArea doing its own monitoring....
            if (! _s_inZone){            debugLog_statusCall("checkZoneReady()", "_s_inZone", "false"); return false; }
            else {
                this._s_zoneReady = true;
                debugLog_statusCall("checkZoneReady()", "_s_zoneReady", "true");
            }
        }
        return this._s_zoneReady;
    }
    public void checkAllReady(){
        if (checkReadyToRun()) {
            log.debug("checkAllReady() -> onReadyToRun()");
            onReadyToRun();
        }
    }

    public void debugLog_statusCall(String callName, String varName, String varValue){
        String fmt_str = String.format("%25s -> %25s => %s", callName, varName, varValue);
        log.debug(fmt_str);
    }

    public void turnAutoChecksAndMonitorsOn(){
        log.debug("turnAutoChecksAndMonitorsOn()");
        if (! this._autoCheckRunState){ this._autoCheckRunState = true; }
        if (! this._autoMonitorGameZone) { this._autoMonitorGameZone = true; }
        if (this._s_finishedRun){ this.prepareNextRun(); }
    }

    public void prepareNextRun(){
        this.PREPARE_FLAG_FOR_NEXT_RUN();

    }

    public void startRunning(){
        if (! _s_startedRun) {
            log.debug("startRunning()");
            this._s_startedRun  = true;
            this._s_finishedRun = false;
            startSession();
            this.debugHelper.reportRunning();
            this.overlay.onStartUp();
        }
    }
    public void stopRunning(){
        if (! this._s_finishedRun) {
            this._s_finishedRun = true;
            log.debug("stopRunning()");
            this.currentSession.end();
            this.turnAutoChecksAndMonitorsOn();
        }
    }

    public void onZoneEnter(){
        if (! this._s_inZone) {
            log.debug("onZoneEnter()");
            this._s_enteredZone = true;
            this._s_inZone      = true;
            this.debugHelper.queueCallMessage("onZoneEnter", "user in active zone");
            //startRunning();
        }
    }
    public void onZoneExit(){
        if (this._s_inZone) {
            log.debug("onZoneExit()");
            this._s_exitedZone  = true;
            this._s_inZone      = false;
            this.debugHelper.queueCallMessage("onZoneExit", "stopping the run...");
            stopRunning();
            this.overlay.onShutdown();
        }
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged)
    {
        switch( gameStateChanged.getGameState() ){
            case CONNECTION_LOST:            log.debug("GameState.CONNECTION_LOST");                             break;
            case HOPPING:                    log.debug("GameState.HOPPING");                                     break;
            case LOADING:
                if (! this._s_GS_LOADING){
                    this._s_GS_LOADING = true;
                }
                log.debug("GameState.LOADING"); // go ahead and continue to display every time anyway
                break;
            case LOGGED_IN:
                if (! this._s_GS_LOGGED_IN){
                    this._s_GS_LOGGED_IN = true;
                    log.debug("GameState.LOGGED_IN");
                    //onUserLogin();
                }
                break;
            case LOGGING_IN:
                if (! this._s_GS_LOGGING_IN){
                    this._s_GS_LOGGING_IN = true;
                    log.debug("GameState.LOGGING_IN");
                }
                break;
            case LOGIN_SCREEN:
                if (! this._s_GS_LOGIN_SCREEN){
                    this._s_GS_LOGIN_SCREEN = true;
                    log.debug("GameState.LOGIN_SCREEN");
                }
                break;
            case LOGIN_SCREEN_AUTHENTICATOR:
                log.debug("GameState.LOGIN_SCREEN_AUTHENTICATOR");
                break;
            case STARTING:
                if (! this._s_GS_STARTING){
                    this._s_GS_STARTING = true;
                    log.debug("GameState.STARTING");
                }
                break;
            case UNKNOWN:                    log.debug("GameState.UNKNOWN");                                     break;
        }
    }
    @Subscribe
    public void onConfigChanged(ConfigChanged cfgEvent){
        //log.debug("onConfigChanged()");
        String     groupName = cfgEvent.getGroup();
        String qualifiedName = null;
        String     debugText = null;
        if ( groupName.equals(CONFIG_GROUP_NAME) ){
            qualifiedName = String.format("onConfigChanged(%s::%s)", cfgEvent.getGroup(), cfgEvent.getKey());
                debugText = String.format("%45s : %6s => %s", qualifiedName, cfgEvent.getOldValue(), cfgEvent.getNewValue());
                log.debug(debugText);
        }
    }
    public boolean isTargetTick(final int TICK_FREQUENCY){ return ((this.clientTickNum % TICK_FREQUENCY) == 0); }

    public void onTick_GameArea(){
        this.activeGameZone.onTick();
    }
    public void onTick_PluginCheckReady(){
        checkAllReady();
    }

    @Subscribe
    public void onClientTick(ClientTick clientTick){

        this.clientTickNum++;
        if (this._autoMonitorGameZone) {
            if (isTargetTick(GAME_AREA_CLIENT_TICK_EVERY)) {
                onTick_GameArea();
            }
        }
        if (this._autoCheckRunState) {
            if (isTargetTick(PLUGIN_CHECK_READY_EVERY)) {
                onTick_PluginCheckReady();
            }
        }

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
    WildyAgilityLootTrackerConfig provideConfig(ConfigManager configManager){
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


Flows:

    Normal Login, within area

    Normal Login, outside of area





 */