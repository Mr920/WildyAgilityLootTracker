package com.lita;
import net.runelite.client.config.Config;      // https://github.com/runelite/runelite/blob/master/runelite-client/src/main/java/net/runelite/client/config/Config.java
import net.runelite.client.config.ConfigGroup; // https://github.com/runelite/runelite/blob/master/runelite-client/src/main/java/net/runelite/client/config/ConfigGroup.java
import net.runelite.client.config.ConfigItem;  // https://github.com/runelite/runelite/blob/master/runelite-client/src/main/java/net/runelite/client/config/ConfigItem.java

@ConfigGroup(LtaPluginHelper.configName)
public interface WildyAgilityLootTrackerConfig extends Config {

    public static final String KEY_SHOW_SUPPLIES        = "ShowSupplies";
    public static final String KEY_SHOW_STEEL_ARMOUR    = "ShowSteelArmour";
    public static final String KEY_SHOW_MITHRIL_ARMOUR  = "ShowMithrilArmour";
    public static final String KEY_SHOW_ADAMANT_ARMOUR  = "ShowAdamantArmour";
    public static final String KEY_SHOW_RUNE_ARMOUR     = "ShowRuneArmour";

    public static final String NAME_SHOW_SUPPLIES       = "Show Supplies";
    public static final String NAME_SHOW_STEEL_ARMOUR   = "Show Steel Armour";
    public static final String NAME_SHOW_MITHRIL_ARMOUR = "Show Mithril Armour";
    public static final String NAME_SHOW_ADAMANT_ARMOUR = "Show Adamant Armour";
    public static final String NAME_SHOW_RUNE_ARMOUR    = "Show Rune Armour";

    public static final String DESC_SHOW_SUPPLIES       = "Whether or not to track and display supply items";
    public static final String DESC_SHOW_STEEL_ARMOUR   = "Whether or not to track and display steel armour items";
    public static final String DESC_SHOW_MITHRIL_ARMOUR = "Whether or not to track and display mithril armour items";
    public static final String DESC_SHOW_ADAMANT_ARMOUR = "Whether or not to track and display adamant armour items";
    public static final String DESC_SHOW_RUNE_ARMOUR    = "Whether or not to track and display rune armour items";

    public boolean _showSupplies      = true;
    public boolean _showSteelArmour   = true;
    public boolean _showMithrilArmour = true;
    public boolean _showAdamantArmour = true;
    public boolean _showRuneArmour    = true;

    @ConfigItem(keyName = KEY_SHOW_SUPPLIES,       name=NAME_SHOW_SUPPLIES,       description = DESC_SHOW_SUPPLIES)
    default boolean getShowSupplies(){       return _showSupplies;        }

    @ConfigItem(keyName = KEY_SHOW_STEEL_ARMOUR,   name=NAME_SHOW_STEEL_ARMOUR,   description = DESC_SHOW_STEEL_ARMOUR)
    default boolean getShowSteelArmour(){    return _showSteelArmour;     }

    @ConfigItem(keyName = KEY_SHOW_MITHRIL_ARMOUR, name=NAME_SHOW_MITHRIL_ARMOUR, description = DESC_SHOW_MITHRIL_ARMOUR)
    default boolean getShowMithrilArmour(){  return _showMithrilArmour;   }

    @ConfigItem(keyName = KEY_SHOW_ADAMANT_ARMOUR, name=NAME_SHOW_ADAMANT_ARMOUR, description = DESC_SHOW_ADAMANT_ARMOUR)
    default boolean getShowAdamantArmour(){  return _showAdamantArmour;   }

    @ConfigItem(keyName = KEY_SHOW_RUNE_ARMOUR,    name=NAME_SHOW_RUNE_ARMOUR,    description = DESC_SHOW_RUNE_ARMOUR)
    default boolean getShowRuneArmour(){     return _showRuneArmour;      }
}