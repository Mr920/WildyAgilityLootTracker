package com.lita.MiscUtils;

public class RunOnceEvent {
    private Runnable listener = null;
    private boolean  hasFired = false;

    public RunOnceEvent(){
    }
    public RunOnceEvent(Runnable _listener){
        this.setListener(_listener);
    }

    private void dispatch(){
        if (this.listener != null){
            this.listener.run();
        }
    }

    public void setListener(Runnable _listener){
        this.listener = _listener;
        if (this.hasFired){
            this.dispatch();
        }
    }
    public boolean fire(){
        if (! this.hasFired){
            this.dispatch();
            this.hasFired = true;
            return true;
        }
        return false;
    }
}
