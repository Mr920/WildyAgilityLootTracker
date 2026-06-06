package com.lita;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
public class WildyAgilitySession {

   private static int                 _ID     = 0;
    public static WildyAgilitySession current = null;
    public static WildyAgilitySession last    = null;

    public static WildyAgilitySession getCurrent(WildyAgilityLootTrackerPlugin _plugin){
        if (current == null){
            current = new WildyAgilitySession(_plugin);
        }
        return current;
    }
    public static WildyAgilitySession getLast(WildyAgilityLootTrackerPlugin _plugin){
        if (last == null){
            last = getCurrent(_plugin);
        }
        return last;
    }

    public static WildyAgilitySession getNew(WildyAgilityLootTrackerPlugin _plugin){ return new WildyAgilitySession(_plugin); }
    public static int getNewID(){
        return _ID++;
    }

    public WildyAgilityLootTrackerPlugin plugin;

    public           int num      = 1; // this is for the future, but won't be reliable until later
    public LocalDateTime startDt  = null;
    public LocalDateTime endDt    = null;
    public           int streak   = 0;
    public           int lapNum   = 0;
    public           int lapCount = 0;

    public WildyAgilitySession(WildyAgilityLootTrackerPlugin _plugin){
        this.plugin = _plugin;
        this.num = getNewID();
        if (WildyAgilitySession.current == null){
            WildyAgilitySession.current = this;
        }
        last = this;
    }
    public void start(){
        this.startDt = LocalDateTime.now();
        log.debug("Started Session " + Integer.toString(this.num, 10));
    }
    public void end(){
        log.debug("Ending session...");
        this.endDt = LocalDateTime.now();
        save();
        //clear();
    }
    public void save(){
        log.debug("To-Do : Implement save()");
    }
    public boolean isActive(){
        return ((this.startDt != null) && (this.endDt == null));
    }

    /* Is this even needed?
    public void clear(){
        log.debug("To-Do : Implement clear()");
    }
    */

}
