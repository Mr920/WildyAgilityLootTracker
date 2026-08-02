package com.lita;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;

/*  Using this helpful tool: https://mejrs.github.io/osrs?m=-1&z=3&p=0&x=3050&y=3943&layer=chunks&layer=labels&layer=grid  */
public class WildyAgilityGameArea {
    public static class CourseZone_Main {
        public final       int NORTH  = 3968;
        public final       int SOUTH  = 3904;
        public final       int EAST   = 3104;
        public final       int WEST   = 2984;
        public final       int HEIGHT =   64;
        public final       int WIDTH  =  120;
        public final       int AREA   = 7680;
        public final     int[] CENTER = { 3044, 3936 };
        public final       int PLANE  =    0;
        public final WorldArea area   = new WorldArea(WEST, SOUTH, WIDTH, HEIGHT, PLANE);
    }
    public static class CourseZone_Dungeon {
        public final       int NORTH  = 10368;
        public final       int SOUTH  = 10336;
        public final       int EAST   =  3008;
        public final       int WEST   =  2991;
        public final       int HEIGHT =    32;
        public final       int WIDTH  =    17;
        public final       int AREA   =   544;
        public final     int[] CENTER = { 2999, 10352 };
        public final       int PLANE  =    0;
        public final WorldArea area   = new WorldArea(WEST, SOUTH, WIDTH, HEIGHT, PLANE);
    }
    public final               CourseZone_Main courseMain    = new CourseZone_Main();
    public final            CourseZone_Dungeon courseDungeon = new CourseZone_Dungeon();
    public                             boolean playerInZone  = false;
    public final WildyAgilityLootTrackerPlugin plugin;
    public         WildyAgilityGameArea(WildyAgilityLootTrackerPlugin _plugin){ plugin = _plugin; }
    public    void onZoneEnter(){ plugin.onZoneEnter(); }
    public    void onZoneExit(){  plugin.onZoneExit();  }
    public boolean isInZone(WorldPoint location){ return (location.isInArea2D(courseMain.area) || location.isInArea2D(courseDungeon.area)); }
    public boolean isInZone(Player player){       return isInZone(player.getWorldLocation()); }
    public boolean isPlayerInZone(){
        Player player = plugin.client.getLocalPlayer();
        if (player == null){ return false; }
        return isInZone(player);
    }
    public    void checkPlayerZone(){
        boolean wasInZone = playerInZone;
        playerInZone = isPlayerInZone();
        if (playerInZone){ if (! wasInZone){ onZoneEnter(); } }
        else {             if (wasInZone){   onZoneExit();  } }
    }
    public    void onTick(){      checkPlayerZone();    }
}
