package com.lita;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WildyAgilityChatParser {
    public WildyAgilityLootTrackerPlugin plugin;
    public                       Pattern awardP;
    public                       Pattern highlightP;
    public                       Pattern itemP;
    public                       Pattern rewardStreakP;
    public                       Pattern lapCountP;
    public                        String parseMessage;
    public                       Matcher currentMatch;
    public WildyAgilityChatParser(WildyAgilityLootTrackerPlugin _plugin){
        plugin        = _plugin;
        awardP        = Pattern.compile("You have been awarded");
        highlightP    = Pattern.compile("[<]col[=]ef1020[>]([^<]+)[<].col[>]");
        itemP         = Pattern.compile("([0-9]+) x (.+)");
        rewardStreakP = Pattern.compile("Wilderness Agility reward streak is: .col=ff0000.([0-9]+)");
        lapCountP     = Pattern.compile("Wilderness Agility lap count is: .col=ff0000.([0-9]+)");
    }
    public int[] badIDs(){ return new int[] {1, 0}; }
    private boolean checkMessageMatches(String chatMessage, Pattern p){
        parseMessage = chatMessage;
        currentMatch = p.matcher(parseMessage);
        return currentMatch.find();
    }
    public boolean checkIsAwardMessage(String chatMessage){
        if (checkMessageMatches(chatMessage, awardP)) {        plugin.onDataMutation(parseAwardMessage());   return true; } else { return false; }
    }
    public boolean checkIsLapCountMessage(String chatMessage){
        if (checkMessageMatches(chatMessage, lapCountP)) {     plugin.updateLapCount(currentMatch.group(1)); return true; } else { return false; }
    }
    public boolean checkIsStreakMessage(String chatMessage){
        if (checkMessageMatches(chatMessage, rewardStreakP)) { plugin.updateStreak(currentMatch.group(1));   return true; } else { return false; }
    }
    public LtaLootItem[] parseAwardMessage(){
        LtaLootItem updatedSupplyItem = null;
        LtaLootItem updatedArmourItem = null;
        currentMatch = highlightP.matcher(parseMessage);
        if (currentMatch.find()) { updatedSupplyItem = parseOut_SupplyItem(); }
        if (currentMatch.find()) { updatedArmourItem = parseOut_ArmourItem(); }
        plugin.printCheckPointBanner();
        return new LtaLootItem[]{updatedSupplyItem, updatedArmourItem};
    }
    public int[]       parseOut_LootItem(){
        Matcher itemMatcher = itemP.matcher(currentMatch.group(1));
        if (! itemMatcher.find()) { return badIDs(); } // i know this will bite me in the ass, I'll fix it later
        int             Qty = Integer.parseInt(itemMatcher.group(1));
        int          ItemId = LtaLootItem.getItemIdFromName(itemMatcher.group(2));
        return new int[] { ItemId, Qty };
    }
    public LtaLootItem parseOut_SupplyItem(){
        int[]       parsedValues = parseOut_LootItem();
        int               itemId = parsedValues[0];
        int                  Qty = parsedValues[1];
        LtaLootItem   supplyItem = plugin.getMatchingSupplyItem(itemId);
        supplyItem.haveQuantity += Qty;
        return supplyItem;
    }
    public LtaLootItem parseOut_ArmourItem(){
        int[]       parsedValues = parseOut_LootItem();
        int               itemId = parsedValues[0];
        int                  Qty = parsedValues[1];
        LtaLootItem   armourItem = plugin.getMatchingArmourItem(itemId);
        armourItem.haveQuantity += Qty;
        return armourItem;
    }
}
