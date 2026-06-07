package com.lita;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.chat.QueuedMessage.QueuedMessageBuilder;
import java.awt.*;


@Slf4j
public class WildyAgilityDebugHelper {

    public WildyAgilityLootTrackerPlugin plugin;

    public WildyAgilityDebugHelper(WildyAgilityLootTrackerPlugin _plugin){
        this.plugin = _plugin;
        log.debug("Instantiating");
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

    public String getRunReportMessage(Boolean formatted){
        final     String[]     msgPieces = {"WildyAgilityLootTrackerPlugin", " is ", "running", " and ", "ready", "."};
                    String reportMessage = null;
        ChatMessageBuilder   cMsgBuilder = null;

        if (! formatted){
            reportMessage = String.join("", msgPieces);
        }
        else {
            cMsgBuilder = new ChatMessageBuilder();
            cMsgBuilder.append(Color.RED,   msgPieces[0]);
            cMsgBuilder.append(Color.BLACK, msgPieces[1]);
            cMsgBuilder.append(Color.GREEN, msgPieces[2]);
            cMsgBuilder.append(Color.BLACK, msgPieces[3]);
            cMsgBuilder.append(Color.GREEN, msgPieces[4]);
            cMsgBuilder.append(Color.BLACK, msgPieces[5]);
            reportMessage = cMsgBuilder.build();
        }

        return reportMessage;
    }
    public String getCallMessage(String callName, String message){
        final String[] msgPieces = {"WildyAgilityLootTrackerPlugin", " -> ", callName, "()", " : ", message};
        ChatMessageBuilder cMsgBuilder = new ChatMessageBuilder();
        cMsgBuilder.append(Color.RED,   msgPieces[0]);
        cMsgBuilder.append(Color.BLACK, msgPieces[1]);
        cMsgBuilder.append(Color.GREEN,  msgPieces[2]);
        cMsgBuilder.append(Color.GREEN,  msgPieces[3]);
        cMsgBuilder.append(Color.BLACK, msgPieces[4]);
        cMsgBuilder.append(Color.BLUE,  msgPieces[5]);
        return cMsgBuilder.build();
    }

    public QueuedMessage getQueuedMessage(String message){
        QueuedMessageBuilder qMsgBuilder = QueuedMessage.builder();
        qMsgBuilder.type(ChatMessageType.GAMEMESSAGE);
        qMsgBuilder.name("");
        qMsgBuilder.value(message);
        return qMsgBuilder.build();
    }
    public void addChatMessage(String message){
        this.plugin.clientThread.invokeLater(() -> {
            this.plugin.client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", message, null);
        });
    }
    public void queueChatMessage(String message){
        this.plugin.clientThread.invoke(() -> {
            this.plugin.chatMessageManager.queue(getQueuedMessage(message));
        });
    }
    public void queueCallMessage(String callName, String message){
        this.queueChatMessage(this.getCallMessage(callName, message));
    }
    public void reportRunning(){
        String reportMsgPlain     = getRunReportMessage(false);
        String reportMsgFormatted = getRunReportMessage(true);
        log.debug(reportMsgPlain);
      //addChatMessage(reportMsgFormatted);
        queueChatMessage(reportMsgFormatted);
    }

}
