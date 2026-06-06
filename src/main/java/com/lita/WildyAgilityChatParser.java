package com.lita;

import lombok.extern.slf4j.Slf4j;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class WildyAgilityChatParser {

    public WildyAgilityLootTrackerPlugin plugin;

    public Pattern awardP;
    public Pattern highlightP;
    public Pattern itemP;
    public Pattern rewardStreakP;
    public Pattern lapCountP;

    public String  parseMessage;
    public Matcher currentMatch;

    public WildyAgilityChatParser(WildyAgilityLootTrackerPlugin _plugin){
        plugin        = _plugin;
        awardP        = Pattern.compile("You have been awarded");
        highlightP    = Pattern.compile("[<]col[=]ef1020[>]([^<]+)[<].col[>]");
        itemP         = Pattern.compile("([0-9]+) x (.+)");
        rewardStreakP = Pattern.compile("Wilderness Agility reward streak is: .col=ff0000.([0-9]+)");
        lapCountP     = Pattern.compile("Wilderness Agility lap count is: .col=ff0000.([0-9]+)");
    }

    public int[] badIDs(){ return new int[] {1, 0}; }

    public boolean checkIsAwardMessage(String chatMessage){
        this.parseMessage = chatMessage;
        this.currentMatch = awardP.matcher(chatMessage);
        if (! this.currentMatch.find()){
            //log.debug("Game Message does not match the award text pattern");
            //log.debug(String.format("Game Message was: '%s'", msg));
            return false;
        }
        log.debug("Received Game Message matching the award format pattern, proceeding to parse it out.");
        LtaLootItem[] mutatedObjects = parseAwardMessage(chatMessage);
        this.plugin.onDataMutation(mutatedObjects);
        return true;
    }
    public boolean checkIsLapCountMessage(String chatMessage){
        this.parseMessage = chatMessage;
        this.currentMatch = lapCountP.matcher(chatMessage);
        if (! this.currentMatch.find()){ return false; }
        this.plugin.updateLapCount(this.currentMatch.group(1));
        return true;
    }
    public boolean checkIsStreakMessage(String chatMessage){
        this.parseMessage = chatMessage;
        this.currentMatch = rewardStreakP.matcher(chatMessage);
        if (! this.currentMatch.find()){ return false; }
        this.plugin.updateStreak(this.currentMatch.group(1));
        return true;
    }

    public LtaLootItem[] parseAwardMessage(String msg){
        this.parseMessage = msg;
        this.currentMatch = highlightP.matcher(msg);
        LtaLootItem updatedSupplyItem = parseOut_SupplyItem(this.currentMatch);
        LtaLootItem updatedArmourItem = parseOut_ArmourItem(this.currentMatch);
        //log.debug(String.format("Parsed -> (Supply : Qty=%s , Name=%s) : (Armour : Qty=%s, Name=%s)", supplyQtyS, supplyItemS, armourQtyS, armourItemS));
        log.info(String.format("Parsed & Updated [S] -> %s", updatedSupplyItem.toDebugString()));
        log.info(String.format("Parsed & Updated [A] -> %s", updatedArmourItem.toDebugString()));
        this.plugin.printCheckPointBanner();
        return new LtaLootItem[] {
                updatedSupplyItem,
                updatedArmourItem
        };
    }
    public int[]       parseOut_LootItem(Matcher highlightMatcher){
        if (! highlightMatcher.find()) { log.debug("Failed to match highlight pattern"); return badIDs(); } // i know this will bite me in the ass, I'll fix it later
        String         hStr = highlightMatcher.group(1);
        Matcher itemMatcher = itemP.matcher(hStr);
        if (! itemMatcher.find()) { log.debug("Failed to match item pattern"); return badIDs(); }           // i know this will bite me in the ass, I'll fix it later
        String         QtyS = itemMatcher.group(1);
        String        ItemS = itemMatcher.group(2);
        int          ItemId = LtaLootItem.getItemIdFromName(ItemS);
        int             Qty = Integer.parseInt(QtyS);
        return new int[] { ItemId, Qty };
    }
    public LtaLootItem parseOut_SupplyItem(Matcher highlightMatcher){
        int[]       parsedValues = parseOut_LootItem(highlightMatcher);
        int               itemId = parsedValues[0];
        int                  Qty = parsedValues[1];
        LtaLootItem supplyItem   = this.plugin.getMatchingSupplyItem(itemId);
        supplyItem.haveQuantity += Qty;
        return supplyItem;
    }
    public LtaLootItem parseOut_ArmourItem(Matcher highlightMatcher){
        int[]       parsedValues = parseOut_LootItem(highlightMatcher);
        int               itemId = parsedValues[0];
        int                  Qty = parsedValues[1];
        LtaLootItem   armourItem = this.plugin.getMatchingArmourItem(itemId);
        armourItem.haveQuantity += Qty;
        return armourItem;
    }

}
