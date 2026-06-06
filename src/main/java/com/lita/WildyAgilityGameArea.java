package com.lita;

import net.runelite.api.coords.WorldArea;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;

/*  Using this helpful tool:
        https://mejrs.github.io/osrs?m=-1&z=3&p=0&x=3050&y=3943&layer=chunks&layer=labels&layer=grid
*/
public class WildyAgilityGameArea extends WorldArea {

    public static final int  NORTH   = 3968;
    public static final int  SOUTH   = 3904;
    public static final int  EAST    = 3104;
    public static final int  WEST    = 2984;
    public static final int  HEIGHT  =   64;
    public static final int  WIDTH   =  120;
    public static final int   AREA   = 7680;
    public static final int[] CENTER = { 3044, 3936 };


    public WildyAgilityLootTrackerPlugin plugin;
    public boolean                       playerInZone = false;

    public WildyAgilityGameArea(WildyAgilityLootTrackerPlugin _plugin){
        super(WEST, SOUTH, WIDTH, HEIGHT, 0);
        this.plugin = _plugin;
    }

    public boolean isPlayerInZone(){
        Client gameClient = this.plugin.client;
        Player player = gameClient.getLocalPlayer();
        if (player == null){ return false; }
        WorldPoint currentLoc = player.getWorldLocation();
        return currentLoc.isInArea2D(this);
    }

    public void checkPlayerZone(){
        boolean oldValue  = this.playerInZone;
        this.playerInZone = this.isPlayerInZone();
        if (this.playerInZone){
            if (oldValue == false){
                onZoneEnter();
            }
        }
        else {
            if (oldValue == true){
                onZoneExit();
            }
        }
    }

    public void onZoneEnter(){

    }
    public void onZoneExit(){

    }

    public void onTick(){
        checkPlayerZone();
    }
}
