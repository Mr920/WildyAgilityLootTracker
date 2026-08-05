package com.lita;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WildyAgilityChatParser {
    public static final Pattern pattern_LapCount  = Pattern.compile("Wilderness Agility lap count is: .col=ff0000.([0-9]+)");
    public static final Pattern pattern_Award     = Pattern.compile("You have been awarded");
    public static final Pattern pattern_Highlight = Pattern.compile("[<]col[=]ef1020[>]([^<]+)[<].col[>]");
    public static final Pattern pattern_Item      = Pattern.compile("([0-9]+) x (.+)");
    public static final Pattern pattern_Streak    = Pattern.compile("Wilderness Agility reward streak is: .col=ff0000.([0-9]+)");
    public WildyAgilityLootTrackerPlugin plugin            = null;
    public final                   Award awardParser       = new Award(pattern_Award);
    public final             Highlighted highlightedParser = new Highlighted(pattern_Highlight);
    public final                    Item itemParser        = new Item(pattern_Item);
    public final                  Streak streakParser      = new Streak(pattern_Streak);
    public final                LapCount lapParser         = new LapCount(pattern_LapCount);
    public             WildyAgilityChatParser(WildyAgilityLootTrackerPlugin _plugin){ plugin = _plugin; }
    public     boolean checkIsAwardMessage(String chatMessage){    return  awardParser.check(chatMessage); }
    public     boolean checkIsLapCountMessage(String chatMessage){ return    lapParser.check(chatMessage); }
    public     boolean checkIsStreakMessage(String chatMessage){   return streakParser.check(chatMessage); }
    public Highlighted parseAwardMessage(){ highlightedParser.check(awardParser.parseMessage); plugin.printCheckPointBanner(); return highlightedParser; }
    public class PatternParser {
        public  Pattern pattern      = null;
        public  boolean matches      = false;
        public   String parseMessage = null;
        public  Matcher currentMatch = null;
        public   String matchedTxt   = null;
        public void onMatch(){}
        public boolean findAndSet(){ matches = currentMatch.find(); if (matches){ matchedTxt = currentMatch.group(1); } return matches; }
        public boolean check(String chatMessage){
            parseMessage = chatMessage;
            currentMatch = pattern.matcher(parseMessage);
            if (findAndSet()){ onMatch(); }
            return matches;
        }
        public PatternParser(Pattern _pattern){ pattern = _pattern; }
    }
    public class LapCount extends PatternParser {
        @Override public void onMatch(){ plugin.updateLapCount(matchedTxt); }
        public LapCount(Pattern _pattern){ super(_pattern); }
    }
    public class Streak extends PatternParser {
        @Override public void onMatch(){ plugin.updateStreak(matchedTxt); }
        public Streak(Pattern _pattern){ super(_pattern); }
    }
    public class Award extends PatternParser {
        @Override public void onMatch(){ plugin.onDataMutation(parseAwardMessage()); }
        public Award(Pattern _pattern){ super(_pattern); }
    }
    public interface LootItemGetter { LtaLootItem getMatchingItem(int itemId); }
    public class Highlighted extends PatternParser {
        public LtaLootItem updatedSupplyItem;
        public LtaLootItem updatedArmourItem;
        public boolean parseLootItem(){ return itemParser.check(matchedTxt); }
        public LtaLootItem parseLootItem(LootItemGetter itemGetter){ if (! parseLootItem()){ return null; } return updateQty(itemGetter.getMatchingItem(itemParser.ItemId)); }
        public LtaLootItem updateQty(LtaLootItem item){ item.haveQuantity += itemParser.Qty; return item; }
        public LtaLootItem parseSupplyItem(){ return parseLootItem(plugin::getMatchingSupplyItem); }
        public LtaLootItem parseArmourItem(){ return parseLootItem(plugin::getMatchingArmourItem); }
        @Override public void onMatch(){
            updatedSupplyItem = parseSupplyItem();
            if (findAndSet()){ updatedArmourItem = parseArmourItem(); }
        }
        public Highlighted(Pattern _pattern){ super(_pattern); }
    }
    public class Item extends PatternParser {
        public int Qty;
        public int ItemId;
        @Override public void onMatch(){
            Qty = Integer.parseInt(matchedTxt);
            ItemId = LtaLootItem.getItemIdFromName(currentMatch.group(2));
        }
        public Item(Pattern _pattern){ super(_pattern); }
    }
}
