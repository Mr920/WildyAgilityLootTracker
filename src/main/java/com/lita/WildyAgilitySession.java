package com.lita;
import java.time.LocalDateTime;

public class WildyAgilitySession {
   private static int _ID = 0;
    public static int getNewID(){ return _ID++; }

    public WildyAgilityLootTrackerPlugin plugin;
    public                           int num;
    public                 LocalDateTime startDt  = null;
    public                 LocalDateTime endDt    = null;
    public                           int streak   = 0;
    public                           int lapNum   = 0;
    public                           int lapCount = 0;

    public WildyAgilitySession(WildyAgilityLootTrackerPlugin _plugin){
        plugin = _plugin;
        num = getNewID();
    }
    public void start(){
        if (! hasStarted()) {
            startDt = LocalDateTime.now();
            plugin.debugHelper.queueChatMessage("Started Session " + Integer.toString(num, 10));
        }
        else {
            if (! hasFinished()){ end(); }
            /* save(); */
            plugin.currentSession = new WildyAgilitySession(plugin);
            plugin.currentSession.start();
        }
    }
    public void end(){
        this.endDt = LocalDateTime.now();
        /* save(); */
        for (LtaLootItem sItm: plugin.supplies){ sItm.haveQuantity = 0; }
        for (LtaLootItem aItm: plugin.armour){   aItm.haveQuantity = 0; }
    }
  //public void save(){} // I will come back and implement this later
    public boolean hasStarted(){  return (this.startDt != null); }
    public boolean hasFinished(){ return (this.endDt != null);   }
    public boolean isActive(){    return (hasStarted() && (! hasFinished())); }
}
