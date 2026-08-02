package com.lita;
import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.OverlayManager;

/* To-Do:
        [ ] Make this thing respect the config
        [ ] Session Management, complete with the ability to preserve historical information on the loot item quantities and prices from previous sessions
        [ ] Make visual overlay configurable/switchable to Swing-based Side Panel
        [ ] Give users a "clear session" button
        [x] Detect when user enters or leaves agility arena
        [ ] Detect logouts / logins
        [ ] Give users the ability to override the estimated item prices in the config; This will be on a per-session basis, with the default behavior still being to pull them from the client API at the start of a new session
        [ ] Maybe show additional statistics like the most recent bag-value-increase amount, maybe show an average rate of increase as well
*/
/* Don't forget about:
        https://static.runelite.net/runelite-client/apidocs/net/runelite/client/events/package-summary.html */


@Slf4j
@PluginDescriptor(
    name = "WildyAgilityLootTracker"
)
public class WildyAgilityLootTrackerPlugin extends Plugin {
            public static final                int GAME_AREA_CLIENT_TICK_EVERY =  25; // half a second
            public static final                int PLUGIN_CHECK_READY_EVERY    =  50; // about 1 seconds
            public static final             String CONFIG_GROUP_NAME           = "WildyAgilityLootTracker";
            public static              ItemManager ItemManager;
    @Inject public                          Client client;
    @Inject public                    ClientThread clientThread;
    @Inject public   WildyAgilityLootTrackerConfig config;
    @Inject public                     ItemManager itemManager;
    @Inject public              ChatMessageManager chatMessageManager;
    @Inject public                  OverlayManager overlayManager;
    @Inject public  WildyAgilityLootTrackerOverlay overlay;
            public             WildyAgilitySession currentSession         = null;
            public          WildyAgilityChatParser chatParser             = null;
            public            WildyAgilityGameArea activeGameZone         = null;
            public         WildyAgilityDebugHelper debugHelper            = null;
            public                   LtaLootItem[] supplies               = null;
            public                   LtaLootItem[] armour                 = null;
            public                          String currentLootValStr      = null;
           private                             int clientTickNum          = 0;

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

    public        void PREPARE_FLAG_FOR_NEXT_RUN(){
        _s_stoppedPlugin   = false;
        _s_startedSession  = false;
        _s_finishedSession = false;
        _s_startedRun      = false;
        _s_finishedRun     = false;
        _s_enteredZone     = false;
        _s_exitedZone      = false;
        _s_inZone          = false;
        _s_readyToRun      = false;
        _s_sessionReady    = false;
        _s_zoneReady       = false;
    }
    public        void startSession(){
        if (! _s_startedSession){
            currentSession.start();
            _s_startedSession = true;
            _s_finishedSession = false;
        }
        else {
            if (currentSession.isActive()){
                endSession();
                startSession();
            }
            else {
                currentSession = new WildyAgilitySession(this);
                currentSession.start();
                _s_startedSession  = true;
                _s_finishedSession = false;
            }
        }
    }
    public        void endSession(){
        if (_s_startedSession && (! _s_finishedSession)){
            currentSession.end();
            _s_finishedSession = true;
        }
    }
    public        void updateItemDisplayConfig(LtaLootItem[] items){ for (LtaLootItem item: items){ item._detectIfConfigured(config); } }
    public        void updateItemDisplayConfigs(){
        updateItemDisplayConfig(supplies);
        updateItemDisplayConfig(armour);
    }
    public        void init_LootItems(){
        if (! _s_initLoot) {
            _s_initLoot = true;
            supplies = LtaLootItem.getSupplyItems();
            armour   = LtaLootItem.getAllArmourItems();
            updateItemDisplayConfigs();
        }
    }
    public        void _init(){
        if (! _s_initialized) {
            _s_initialized = true;
            currentSession = new WildyAgilitySession(this);
            chatParser     = new WildyAgilityChatParser(this);
            activeGameZone = new WildyAgilityGameArea(this);
            debugHelper    = new WildyAgilityDebugHelper(this);
            WildyAgilityLootTrackerPlugin.ItemManager = itemManager; // this was seriously the stupidest design decision ever, PLEASE refactor this to not be necessary anymore
            clientThread.invoke(() -> {
                init_LootItems();
                currentLootValStr = getTotalLootValueStr();
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
    public        void updateLootItemPrices(LtaLootItem[] items){ for (LtaLootItem itm: items){ itm.updateItemPrice(); } }
    public        void updateItemPrices(){
        updateLootItemPrices(supplies);
        updateLootItemPrices(armour);
    }
    public        void onDataMutation(LtaLootItem[] mutatedObjects){
        currentLootValStr = getTotalLootValueStr();
        overlay.onDataMutation(mutatedObjects);
    }
    @Override
    protected     void startUp() throws Exception {
        if (! _s_startedPlugin) {
            _s_startedPlugin = true;
            _s_stoppedPlugin = false;
            _init();
        }
    }
    @Override
    protected     void shutDown() throws Exception {
        if (! _s_stoppedPlugin) {
            _s_stoppedPlugin = true;
            _s_startedPlugin = false;
            endSession();
            overlay.onShutdown();
        }
    }
    public        void onReadyToRun(){ _autoCheckRunState = false; startRunning(); }
    public     boolean checkReadyToRun(){
        if (! _s_readyToRun){
            if (! checkGameStateReady()){  return false; }
            if (! checkUserLoginScreen()){ return false; }
            if (! checkUserLoaded()){      return false; }
            if (! checkUserReady()){       return false; }
            if (! checkSessionReady()){    return false; }
            if (! checkZoneReady()){       return false; }
            _s_readyToRun = true;
        }
        return _s_readyToRun;
    }
    public     boolean checkGameStateReady(){ return this._s_GS_LOGGED_IN; }
    public     boolean checkUserLoginScreen(){
        if (! _s_firstPlayerLogin) {
            if (! _s_GS_LOGGED_IN) { return false; }
            else { _s_firstPlayerLogin = true;     }
        }
        return _s_firstPlayerLogin;
    }
    public     boolean checkUserLoaded(){
        if (! _s_firstPlayerLoad){
            if (client.getLocalPlayer() == null){ return false; }
            else { _s_firstPlayerLoad = true; }
        }
        return _s_firstPlayerLoad;
    }
    public     boolean checkSessionReady(){
        if (! _s_sessionReady){
            if (currentSession.isActive()){ return false; }
            else { _s_sessionReady = true; }
        }
        return _s_sessionReady;
    }
    public     boolean checkUserReady(){
        if (! _s_userReady) {
            if (! _s_firstPlayerLoad){ return false; }
            else { _s_userReady = true; }
        }
        return _s_userReady;
    }
    public     boolean checkZoneReady(){
        if (! _s_zoneReady){
            if (! _s_inZone){ return false; }
            else { _s_zoneReady = true;     }
        }
        return _s_zoneReady;
    }
    public        void checkAllReady(){ if (checkReadyToRun()) { onReadyToRun(); } }
    public        void turnAutoChecksAndMonitorsOn(){
        if (! _autoCheckRunState){    _autoCheckRunState   = true; }
        if (! _autoMonitorGameZone) { _autoMonitorGameZone = true; }
        if (_s_finishedRun){ PREPARE_FLAG_FOR_NEXT_RUN(); }
    }
    public        void startRunning(){
        if (! _s_startedRun) {
            _s_startedRun  = true;
            _s_finishedRun = false;
            startSession();
            debugHelper.reportRunning();
            overlay.onStartUp();
        }
    }
    public        void stopRunning(){
        if (! _s_finishedRun) {
            _s_finishedRun = true;
            currentSession.end();
            turnAutoChecksAndMonitorsOn();
        }
    }
    public        void onZoneEnter(){
        if (! _s_inZone) {
            _s_enteredZone = true;
            _s_inZone      = true;
            debugHelper.queueCallMessage("onZoneEnter", "user in active zone");
            //startRunning();
        }
    }
    public        void onZoneExit(){
        if (_s_inZone) {
            _s_exitedZone  = true;
            _s_inZone      = false;
            debugHelper.queueCallMessage("onZoneExit", "stopping the run...");
            stopRunning();
            overlay.onShutdown();
        }
    }
    @Subscribe
    public        void onGameStateChanged(GameStateChanged gameStateChanged) {
        switch( gameStateChanged.getGameState() ){
            case LOADING:      if (! _s_GS_LOADING){      _s_GS_LOADING      = true; }                      break;
            case LOGGED_IN:    if (! _s_GS_LOGGED_IN){    _s_GS_LOGGED_IN    = true; /* onUserLogin(); */ } break;
            case LOGGING_IN:   if (! _s_GS_LOGGING_IN){   _s_GS_LOGGING_IN   = true; }                      break;
            case LOGIN_SCREEN: if (! _s_GS_LOGIN_SCREEN){ _s_GS_LOGIN_SCREEN = true; }                      break;
            case STARTING:     if (! _s_GS_STARTING){     _s_GS_STARTING     = true; }                      break;
        }
    }
  /*@Subscribe
    public void onConfigChanged(ConfigChanged cfgEvent){
        String     groupName = cfgEvent.getGroup();
        if ( groupName.equals(CONFIG_GROUP_NAME) ){  }
    }*/
    public     boolean isTargetTick(final int TICK_FREQUENCY){ return ((clientTickNum % TICK_FREQUENCY) == 0); }
    public        void onTick_GameArea(){ activeGameZone.onTick(); }
    public        void onTick_PluginCheckReady(){ checkAllReady(); }
    @Subscribe
    public        void onClientTick(ClientTick clientTick){
        clientTickNum++;
        if (_autoMonitorGameZone && isTargetTick(GAME_AREA_CLIENT_TICK_EVERY)) { onTick_GameArea();         }
        if (_autoCheckRunState   && isTargetTick(PLUGIN_CHECK_READY_EVERY))    { onTick_PluginCheckReady(); }
    }
    public         int getTotalLootValue(){
        int total = 0;
        for (LtaLootItem sItem: supplies){ total += sItem.getTotalValue(); }
        for (LtaLootItem aItem: armour){   total += aItem.getTotalValue(); }
        return total;
    }
    public      String getTotalLootValueStr(){ return String.format("%14s", String.format("%,d GP", getTotalLootValue())); }
    public      String getCheckpointBannerStr(){
        return String.format("===== Streak %4s : Lap %-7s => Bag Value %s =====\r\n", String.valueOf(currentSession.streak), String.valueOf(currentSession.lapNum), getTotalLootValueStr());
    }
    public        void printCheckPointBanner(){ client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", getCheckpointBannerStr(), null); }
    public        void updateLapCount(String msgMatchStr){
        currentSession.lapNum = Integer.parseInt(msgMatchStr);
        currentSession.lapCount++;
    }
    public        void updateStreak(String msgMatchStr){
        currentSession.streak = Integer.parseInt(msgMatchStr);
        if ((currentSession.streak % 20) == 0){ updateItemPrices(); }
    }
    @Subscribe
    public        void onChatMessage(ChatMessage cMsgEvent){
        if (cMsgEvent.getType() == ChatMessageType.GAMEMESSAGE){
            String  msg = cMsgEvent.getMessage();
            if (chatParser.checkIsLapCountMessage(msg)){ return; }
            if (chatParser.checkIsStreakMessage(msg)){ return; }
            chatParser.checkIsAwardMessage(msg);
        }
    }
    @Provides
    WildyAgilityLootTrackerConfig provideConfig(ConfigManager configManager){ return configManager.getConfig(WildyAgilityLootTrackerConfig.class); }
}