package com.lita;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;

@Slf4j
public class WildyAgilityDebugHelper {

    public WildyAgilityLootTrackerPlugin plugin;

    public WildyAgilityDebugHelper(WildyAgilityLootTrackerPlugin _plugin){
        this.plugin = _plugin;
    }

    public void LogCurrentItems(){
        log.info("===== Supplies =====");
        for (LtaLootItem supplyItem: this.plugin.supplies){
            log.info(supplyItem.toDebugString());
        }
        log.info("===== Armour   =====");
        for (LtaLootItem armourItem: this.plugin.armour){
            log.info(armourItem.toDebugString());
        }
    }

    public void reportRunning(){
        String reportMessage = "WildyAgilityLootTrackerPlugin is running and ready.";
        log.debug(reportMessage);
        log.debug("deferring sending game message to occur at a later time on the client thread");
        this.plugin.clientThread.invokeLater(() -> {
            this.plugin.client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", reportMessage, null);
        });
    }

}
