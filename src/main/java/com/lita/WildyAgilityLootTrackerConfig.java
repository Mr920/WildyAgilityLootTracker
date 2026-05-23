package com.lita;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("WildyAgilityLootTracker")
public interface WildyAgilityLootTrackerConfig extends Config
{
    public boolean _showSupplies      = true;
    public boolean _showSteelArmour   = true;
    public boolean _showMithrilArmour = true;
    public boolean _showAdamantArmour = true;
    public boolean _showRuneArmour    = true;


    @ConfigItem(
        keyName = "greeting",
        name = "Welcome Greeting",
        description = "The message to show to the user when they login"
    )
    default String greeting()
    {
        return "Hello";
    }

    @ConfigItem(keyName = "ShowSupplies",      name="Show Supplies",       description = "Whether or not to track and display supply items")
    default boolean getShowSupplies(){
        return _showSupplies;
    }
    @ConfigItem(keyName = "ShowSteelArmour",   name="Show Steel Armour",   description = "Whether or not to track and display steel armour items")
    default boolean getShowSteelArmour(){
        return _showSteelArmour;
    }
    @ConfigItem(keyName = "ShowMithrilArmour", name="Show Mithril Armour", description = "Whether or not to track and display mithril armour items")
    default boolean getShowMithrilArmour(){
        return _showMithrilArmour;
    }
    @ConfigItem(keyName = "ShowAdamantArmour", name="Show Adamant Armour", description = "Whether or not to track and display adamant armour items")
    default boolean getShowAdamantArmour(){
        return _showAdamantArmour;
    }
    @ConfigItem(keyName = "ShowRuneArmour",    name="Show Rune Armour",    description = "Whether or not to track and display rune armour items")
    default boolean getShowRuneArmour(){
        return _showRuneArmour;
    }

}