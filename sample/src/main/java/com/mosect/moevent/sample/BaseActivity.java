package com.mosect.moevent.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.mosect.moevent.EventManager;

/**
 * Created by MoSect on 2018/2/4.
 * 基本activity
 */

public class BaseActivity extends AppCompatActivity {

    private EventManager eventManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventManager.getRoot().send("activityCreateEvent", this); // 发送Activity创建事件

        eventManager = new EventManager();
        EventManager.getRoot().addEvent(eventManager);
        eventManager.bind(this, BaseActivity.class);
    }

    @Override
    protected void onPause() {
        super.onPause();
        eventManager.setOnline(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        eventManager.setOnline(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventManager.getRoot().removeEvent(eventManager);
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    /**
     * 绑定事件
     *
     * @param froms 来源
     */
    public void bindEvents(Object... froms) {
        eventManager.bind(this, froms);
    }
}
