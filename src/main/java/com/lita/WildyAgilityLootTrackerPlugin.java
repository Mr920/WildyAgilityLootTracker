package com.lita;
import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.game.ItemManager;

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
                    name = LtaPluginHelper.pluginMainName,
              configName = LtaPluginHelper.configName,
             description = LtaPluginHelper.description,
                    tags = { LtaPluginHelper.TAG_1, LtaPluginHelper.TAG_2, LtaPluginHelper.TAG_3, LtaPluginHelper.TAG_4, LtaPluginHelper.TAG_5 },
               conflicts = {},
        enabledByDefault = LtaPluginHelper.enabledByDefault,
                  hidden = LtaPluginHelper.hidden,
         developerPlugin = LtaPluginHelper.developerPlugin,
          loadInSafeMode = LtaPluginHelper.loadInSafeMode
)
public class WildyAgilityLootTrackerPlugin extends Plugin {
            public static final                                  int GAME_AREA_CLIENT_TICK_EVERY =  25; // half a second
            public static final                               String CONFIG_GROUP_NAME           = LtaPluginHelper.configName;
            public static final Class<WildyAgilityLootTrackerConfig> CONFIG_CLS                  = WildyAgilityLootTrackerConfig.class;
    @Inject public                          Client client;
    @Inject public                    ClientThread clientThread;
    @Inject public                   ConfigManager configManager;
    @Inject public   WildyAgilityLootTrackerConfig config;
    @Inject public              ChatMessageManager chatMessageManager;
    @Inject public                  OverlayManager overlayManager;
    @Inject public  WildyAgilityLootTrackerOverlay overlay;
    @Inject public                     ItemManager itemManager;
            public             WildyAgilitySession currentSession         = null;
            public          WildyAgilityChatParser chatParser             = null;
            public            WildyAgilityGameArea activeGameZone         = null;
            public         WildyAgilityDebugHelper debugHelper            = null;
            public                   LtaLootItem[] supplies               = null;
            public                   LtaLootItem[] armour                 = null;
            public                          String currentLootValStr      = null;
           private                             int clientTickNum          = 0;
           private                         boolean running                = false;
    public        void startSession(){
        if ((currentSession != null) && currentSession.isActive()){ currentSession.end(); startSession(); }
        else {
            currentSession = new WildyAgilitySession(this);
            currentSession.start();
        }
    }
    public        void updateItemDisplayConfig(LtaLootItem[] items){ for (LtaLootItem item: items){ item._detectIfConfigured(config); } }
    public        void updateItemDisplayConfigs(){
        updateItemDisplayConfig(supplies);
        updateItemDisplayConfig(armour);
    }
    public        void init_LootItems(){
        LtaLootItem.ITEM_MANAGER = itemManager;
        supplies = LtaLootItem.getSupplyItems();
        armour   = LtaLootItem.getAllArmourItems();
        updateItemDisplayConfigs();
        currentLootValStr = getTotalLootValueStr();
    }
    public        void _init(){
        chatParser     = new WildyAgilityChatParser(this);
        activeGameZone = new WildyAgilityGameArea(this);
        debugHelper    = new WildyAgilityDebugHelper(this);
        clientThread.invokeLater(this::init_LootItems);
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
    public        void onDataMutation(WildyAgilityChatParser.Highlighted mutatedObjects){
        currentLootValStr = getTotalLootValueStr();
        overlay.onDataMutation(mutatedObjects);
    }
    @Override
    protected     void startUp()  throws Exception { _init(); }
    @Override
    protected     void shutDown() throws Exception { stopRunning(); }
    public        void startRunning(){ if (! running){ running = true; startSession(); debugHelper.reportRunning(); overlay.onStartUp(); } }
    public        void stopRunning(){  if (running){   running = false; currentSession.end(); overlay.onShutdown(); } }
    public        void onZoneEnter(){
        debugHelper.queueCallMessage("onZoneEnter", "user in active zone");
        startRunning();
    }
    public        void onZoneExit(){
        debugHelper.queueCallMessage("onZoneExit", "stopping the run...");
        stopRunning();
    }
  /*@Subscribe
    public void onConfigChanged(ConfigChanged cfgEvent){
        String     groupName = cfgEvent.getGroup();
        if ( groupName.equals(CONFIG_GROUP_NAME) ){  }
    }*/
    public     boolean isTargetTick(final int TICK_FREQUENCY){ return ((clientTickNum % TICK_FREQUENCY) == 0); }
    public        void onTick_GameArea(){ activeGameZone.onTick(); }
    @Subscribe
    public        void onClientTick(ClientTick clientTick){ clientTickNum++; if (isTargetTick(GAME_AREA_CLIENT_TICK_EVERY)) { onTick_GameArea(); } }
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
        if (running) {
            if (cMsgEvent.getType() == ChatMessageType.GAMEMESSAGE) {
                String msg = cMsgEvent.getMessage();
                if (chatParser.checkIsLapCountMessage(msg)) {
                    return;
                }
                if (chatParser.checkIsStreakMessage(msg)) {
                    return;
                }
                chatParser.checkIsAwardMessage(msg);
            }
        }
    }
    WildyAgilityLootTrackerConfig getConfig(){ return configManager.getConfig(CONFIG_CLS); }
    @Provides WildyAgilityLootTrackerConfig provideConfig(){ return getConfig(); }
    @Subscribe public void onConfigChanged(ConfigChanged event){
        if (!event.getGroup().equals(CONFIG_GROUP_NAME)){ return; }
        log.debug(LtaPluginHelper.toString(event));
        int[] itemIds;
        String itemType;
        switch(event.getKey()){
            case WildyAgilityLootTrackerConfig.KEY_SHOW_SUPPLIES:       itemType = LtaLootItem.TYPE_SUPPLY; itemIds = LtaLootItem.getSupplyItemIds();        break;
            case WildyAgilityLootTrackerConfig.KEY_SHOW_STEEL_ARMOUR:   itemType = LtaLootItem.TYPE_ARMOUR; itemIds = LtaLootItem.getSteelArmourItemIds();   break;
            case WildyAgilityLootTrackerConfig.KEY_SHOW_MITHRIL_ARMOUR: itemType = LtaLootItem.TYPE_ARMOUR; itemIds = LtaLootItem.getMithrilArmourItemIds(); break;
            case WildyAgilityLootTrackerConfig.KEY_SHOW_ADAMANT_ARMOUR: itemType = LtaLootItem.TYPE_ARMOUR; itemIds = LtaLootItem.getAdamantArmourItemIds(); break;
            case WildyAgilityLootTrackerConfig.KEY_SHOW_RUNE_ARMOUR:    itemType = LtaLootItem.TYPE_ARMOUR; itemIds = LtaLootItem.getRuneArmourItemIds();    break;
            default: itemType = ""; itemIds = new int[]{}; break;
        }
        switch(itemType){
            case LtaLootItem.TYPE_SUPPLY:
                for (int supplyItemId: itemIds){
                    LtaLootItem supplyItem = getMatchingSupplyItem(supplyItemId);
                    supplyItem._detectIfConfigured(config);
                }
                break;
            case LtaLootItem.TYPE_ARMOUR:
                for (int armourItemId: itemIds){
                    LtaLootItem armourItem = getMatchingArmourItem(armourItemId);
                    armourItem._detectIfConfigured(config);
                }
                break;
        }
        overlay.rebuild();
    }
}