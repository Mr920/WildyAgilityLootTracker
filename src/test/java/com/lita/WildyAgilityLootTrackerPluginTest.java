package com.lita;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class WildyAgilityLootTrackerPluginTest
{
    public static void main(String[] args) throws Exception
    {
        ExternalPluginManager.loadBuiltin(WildyAgilityLootTrackerPlugin.class);
        RuneLite.main(args);
    }
}