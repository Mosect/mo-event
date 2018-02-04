package com.mosect.moevent;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by MoSect on 2018/2/4.
 * 基本事件，EventManager和EventClient继承此类
 */

public abstract class BaseEvent implements Event {

    private String name;
    private EventManager manager;
    private boolean online;

    public BaseEvent(@NonNull String name) {
        this.name = name;
    }

    @NonNull
    @Override
    public String getId() {
        return name;
    }

    @Nullable
    @Override
    public EventManager getManager() {
        return manager;
    }

    @Override
    public void setManager(@Nullable EventManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean isOnline() {
        return online;
    }

    @Override
    public void setOnline(boolean online) {
        if (this.online != online) {
            this.online = online;
            onlineChanged();
        }
    }

    protected void onlineChanged() {
    }
}
