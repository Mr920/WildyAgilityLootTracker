package com.lita;
import net.runelite.api.ChatMessageType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.chat.QueuedMessage.QueuedMessageBuilder;
import java.awt.*;

public class WildyAgilityDebugHelper {
    public WildyAgilityLootTrackerPlugin plugin;
    public WildyAgilityDebugHelper(WildyAgilityLootTrackerPlugin _plugin){ plugin = _plugin; }
    public String getRunReportMessage(){
        final     String[]     msgPieces = {"WildyAgilityLootTrackerPlugin", " is ", "running", " and ", "ready", "."};
        ChatMessageBuilder   cMsgBuilder = new ChatMessageBuilder();;
        cMsgBuilder.append(Color.RED,   msgPieces[0]);
        cMsgBuilder.append(Color.BLACK, msgPieces[1]);
        cMsgBuilder.append(Color.GREEN, msgPieces[2]);
        cMsgBuilder.append(Color.BLACK, msgPieces[3]);
        cMsgBuilder.append(Color.GREEN, msgPieces[4]);
        cMsgBuilder.append(Color.BLACK, msgPieces[5]);
        return cMsgBuilder.build();
    }
    public String getCallMessage(String callName, String message){
        final     String[]   msgPieces = {"WildyAgilityLootTrackerPlugin", " -> ", callName, "()", " : ", message};
        ChatMessageBuilder cMsgBuilder = new ChatMessageBuilder();
        cMsgBuilder.append(Color.RED,   msgPieces[0]);
        cMsgBuilder.append(Color.BLACK, msgPieces[1]);
        cMsgBuilder.append(Color.GREEN, msgPieces[2]);
        cMsgBuilder.append(Color.GREEN, msgPieces[3]);
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
    public void queueChatMessage(String message){
        plugin.clientThread.invoke(() -> { plugin.chatMessageManager.queue(getQueuedMessage(message)); });
    }
    public void queueCallMessage(String callName, String message){ queueChatMessage(getCallMessage(callName, message)); }
    public void reportRunning(){ queueChatMessage(getRunReportMessage()); }
}
