package com.mosect.moevent.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.mosect.moevent.EventManager;
import com.mosect.moevent.annotation.EventReceiver;

/**
 * Created by MoSect on 2018/2/4.
 */

public class TestActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        initViews();
        bindEvents(); // 绑定事件接收器
    }

    private void initViews() {
        findViewById(R.id.btn_a).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventManager.getRoot().send("event/a", TestActivity.this);
            }
        });
        findViewById(R.id.btn_b).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventManager.getRoot().send("event/b", TestActivity.this, "b1");
            }
        });
        findViewById(R.id.btn_c).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventManager.getRoot().send("event/c", TestActivity.this, "c1", "c2");
            }
        });
        findViewById(R.id.btn_d).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventManager.getRoot().send("event/d", TestActivity.this, "d1", "d2", "d3");
            }
        });
        findViewById(R.id.btn_new).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TestActivity.this, TestActivity.class);
                startActivity(intent);
            }
        });
    }

    @EventReceiver("event/a")
    void onAEvent(Object src) {
        System.out.println(String.format("onAEvent:src=%s", src));
    }

    @EventReceiver("event/b")
    void onBEvent(Object src, String arg1) {
        System.out.println(String.format("onBEvent:src=%s,arg1=%s", src, arg1));
    }

    @EventReceiver(value = "event/c", maxCacheCount = 2)
    void onCEvent(Object src, String arg1, String arg2) {
        System.out.println(String.format("onCEvent:src=%s,arg1=%s,arg2=%s", src, arg1, arg2));
    }

    @EventReceiver(value = "event/d", offlineReceive = true)
    void onDEvent(Object src, String arg1, String arg2, String arg3) {
        System.out.println(String.format("onDEvent:src=%s,arg1=%s,arg2=%s,arg3=%s", src, arg1, arg2, arg3));
    }
}
