package com.mosect.moevent;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.mosect.moevent.annotation.EventReceiver;

import java.util.LinkedList;

/**
 * Created by MoSect on 2018/2/4.
 * 事件客户端
 */

public class EventClient extends BaseEvent {

    private int maxCacheCount = Integer.MAX_VALUE; // 最大缓存数量
    private boolean offlineReceive = false; // 离线接收
    private Object from = null; // 消息来源，非此来源不接收；null表示接收所有来源
    private LinkedList<Runnable> cacheTasks; // 缓存的任务
    private Listener listener;

    private Handler handler = new Handler();
    private long handlerThreadId = Thread.currentThread().getId();

    public EventClient(@NonNull String name) {
        super(name);
    }

    @Override
    public void onReceive(@NonNull String id, @Nullable final Object src, @Nullable final Object[] args) {
        synchronized (this) {
            if (null == from || src == from) { // 筛选来源
                if (isOnline() || offlineReceive) { // 在线或者离线也接收
                    notifyReceiver(src, args);
                } else { // 不在线
                    if (maxCacheCount > 0) {
                        Runnable task = new Runnable() {
                            @Override
                            public void run() {
                                notifyReceiver(src, args);
                            }
                        };
                        if (null == cacheTasks) {
                            cacheTasks = new LinkedList<>();
                        }
                        if (cacheTasks.size() < maxCacheCount) {
                            cacheTasks.addFirst(task);
                        } else { // 添加之后超过缓存数量
                            while (cacheTasks.size() >= maxCacheCount) {
                                cacheTasks.removeLast();
                            }
                            cacheTasks.addFirst(task);
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void onlineChanged() {
        synchronized (this) {
            super.onlineChanged();
            if (isOnline()) { // 切换成在线状态
                if (null != cacheTasks) { // 执行缓存的任务
                    while (cacheTasks.size() > 0) {
                        cacheTasks.removeLast().run();
                    }
                }
            }
        }
    }

    public int getMaxCacheCount() {
        return maxCacheCount;
    }

    public EventClient setMaxCacheCount(int maxCacheCount) {
        synchronized (this) {
            this.maxCacheCount = maxCacheCount;
        }
        return this;
    }

    public boolean isOfflineReceive() {
        return offlineReceive;
    }

    public EventClient setOfflineReceive(boolean offlineReceive) {
        synchronized (this) {
            this.offlineReceive = offlineReceive;
        }
        return this;
    }

    public Object getFrom() {
        return from;
    }

    public EventClient setFrom(Object from) {
        synchronized (this) {
            this.from = from;
        }
        return this;
    }

    /**
     * 获取缓存数量（未触发的事件）
     *
     * @return 缓存数量
     */
    public int getCacheCount() {
        return null == cacheTasks ? 0 : cacheTasks.size();
    }

    /**
     * 清空缓存（未触发的事件）
     */
    public void clearCache() {
        synchronized (this) {
            if (null != cacheTasks) cacheTasks.clear();
        }
    }

    public Listener getListener() {
        return listener;
    }

    public EventClient setListener(Listener listener) {
        this.listener = listener;
        return this;
    }

    /**
     * 在创建此对象的线程接收事件
     *
     * @param src  来源
     * @param args 参数
     */
    protected void onReceive(@Nullable Object src, @Nullable Object[] args) {
    }

    /**
     * 在当前线程接收此事件，如果当前线程是创建此对象的线程，此方法不会被调用，只会调用onReceive方法
     *
     * @param src  来源
     * @param args 参数
     */
    protected void onCurrentThreadReceive(@Nullable Object src, @Nullable Object[] args) {
    }

    /**
     * 触发接收器
     *
     * @param src  来源
     * @param args 参数
     */
    private void notifyReceiver(@Nullable final Object src, @Nullable final Object[] args) {
        if (Thread.currentThread().getId() == handlerThreadId) {
            onReceive(src, args);
            if (null != listener) listener.onReceive(this, src, args);
        } else {
            onCurrentThreadReceive(src, args); // 先触发当前线程接收器
            if (null != listener) listener.onCurrentThreadReceive(this, src, args);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    onReceive(src, args);
                    if (null != listener)
                        listener.onReceive(EventClient.this, src, args);
                }
            });
        }
    }

    /**
     * 事件监听器
     */
    public interface Listener {

        /**
         * 在创建client对象的线程接收事件
         *
         * @param client 事件客户端
         * @param src    来源
         * @param args   参数
         */
        void onReceive(@NonNull EventClient client, @Nullable Object src, @Nullable Object[] args);

        /**
         * 在当前线程接收此事件，如果当前线程是创建client对象的线程，此方法不会被调用，只会调用onReceive方法
         *
         * @param client 事件客户端
         * @param src    来源
         * @param args   参数
         */
        void onCurrentThreadReceive(@NonNull EventClient client, @Nullable Object src, @Nullable Object[] args);
    }
}
