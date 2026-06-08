package com.lita.MiscUtils;

public class RunResetEvent {

    private Runnable     listener;
    private Runnable     _onReset;
    private RunOnceEvent _hook;


    public RunResetEvent(){
        this._hook = new RunOnceEvent();
    }

    public void reset(){
        this._hook = new RunOnceEvent();
        this._hook.setListener(this.listener);
        if (this._onReset != null){
            this._onReset.run();
        }
    }

    public void setOnReset(Runnable onReset){
        this._onReset = onReset;
    }
    public void setListener(Runnable _listener){
        this.listener = _listener;
        this._hook.setListener(this.listener);
    }
    public boolean fire(){
        boolean eventFired = this._hook.fire();
        if (! eventFired){
            this.reset();
            //return this._hook.fire();
            return this.fire();
        }
        return eventFired;
    }

}
