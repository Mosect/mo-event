package com.mosect.moevent;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.mosect.moevent.annotation.EventReceiver;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by MoSect on 2018/2/4.
 * 事件管理器
 */

public class EventManager extends BaseEvent {

    private static EventManager root;

    /**
     * 获取根管理器
     *
     * @return 根管理器
     */
    @NonNull
    public static EventManager getRoot() {
        if (null == root) {
            root = new EventManager();
            root.setOnline(true); // 根管理器永远在线，不在去更改其在线状态
        }
        return root;
    }

    private HashMap<String, LinkedList<Event>> eventMap;
    private final Object eventActionLock = new Object(); // 事件活动锁，增加与移除用到

    public EventManager() {
        super(""); // 事件管理器ID为空串
    }

    @Override
    public void onReceive(@NonNull String id, @Nullable Object src, @Nullable Object[] args) {
        synchronized (eventActionLock) {
            if (null != eventMap) {
                if ("".equals(id)) { // 所有事件都能收到
                    Collection<LinkedList<Event>> eventLists = eventMap.values();
                    for (LinkedList<Event> list : eventLists) { // 触发所有事件
                        notifyEventList(list, id, src, args);
                    }
                } else {
                    notifyEventList(eventMap.get(id), id, src, args); // 触发对应id事件
                    notifyEventList(eventMap.get(""), id, src, args); // 事件id为空串，表示可以接收所有事件
                }
            }
        }
    }

    /**
     * 添加事件
     *
     * @param event 事件
     */
    public void addEvent(@NonNull Event event) {
        synchronized (eventActionLock) {
            if (null == event.getManager()) {
                if (null == eventMap) eventMap = new HashMap<>();
                LinkedList<Event> list = eventMap.get(event.getId());
                if (null == list) {
                    list = new LinkedList<>();
                    eventMap.put(event.getId(), list);
                }
                list.add(event);
                event.setManager(this);
                event.setOnline(isOnline());
            } else {
                if (event.getManager() != this) { // 此事件已经绑定别的管理器了
                    throw new IllegalArgumentException(String
                            .format("Event[%s] has band a manager[%s]", event, event.getManager()));
                }
            }
        }
    }

    /**
     * 移除事件
     *
     * @param event 事件
     */
    public void removeEvent(@NonNull Event event) {
        synchronized (eventActionLock) {
            if (event.getManager() == this) {
                if (null != eventMap) { // 此处一般都为true
                    LinkedList<Event> list = eventMap.get(event.getId());
                    if (null != list) { // 此处一般都为true
                        list.remove(event);
                        if (list.size() == 0) { // 列表已被清空
                            eventMap.remove(event.getId()); // 移除此列表
                        }
                    }
                }
            }
        }
    }

    /**
     * 发送事件
     *
     * @param id   事件id
     * @param src  来源
     * @param args 参数
     */
    public void send(@NonNull String id, @Nullable Object src, @Nullable Object... args) {
        onReceive(id, src, args);
    }

    public <T> void bind(@Nullable final T target, @NonNull Class<? extends T> type, @Nullable Object... froms) {
        Method[] methods = type.getDeclaredMethods(); // 获取所有的方法
        if (null != methods && methods.length > 0) {
            for (final Method method : methods) {
                final EventReceiver eventReceiver = method.getAnnotation(EventReceiver.class); // 获取事件接收器注解
                if (null != eventReceiver) { // 存在此注解
                    Object from = null;
                    if (null != froms && eventReceiver.fromIndex() >= 0) {
                        from = froms[eventReceiver.fromIndex()];
                    }
                    EventClient client = new EventClient(eventReceiver.value()) {

                        @Override
                        protected void onReceive(@Nullable Object src, @Nullable Object[] args) {
                            if (!eventReceiver.currentThreadReceive()) {
                                notifyReceiver(src, args);
                            }
                        }

                        @Override
                        protected void onCurrentThreadReceive(@Nullable Object src, @Nullable Object[] args) {
                            if (eventReceiver.currentThreadReceive()) {
                                notifyReceiver(src, args);
                            }
                        }

                        private void notifyReceiver(@Nullable Object src, @Nullable Object[] args) {
                            Object[] methodArgs;
                            if (null != args && args.length > 0) { // 带参数
                                methodArgs = new Object[args.length + 1];
                                methodArgs[0] = src;
                                System.arraycopy(args, 0, methodArgs, 1, args.length);
                            } else { // 不带参数
                                methodArgs = new Object[]{src};
                            }
                            try {
                                method.setAccessible(true);
                                method.invoke(target, methodArgs); // 触发接收器
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                    };
                    client.setMaxCacheCount(eventReceiver.maxCacheCount());
                    client.setOfflineReceive(eventReceiver.offlineReceive());
                    client.setFrom(from);
                    addEvent(client);
                }
            }
        }
    }

    public <T> void bind(@NonNull T target, @Nullable Object... froms) {
        bind(target, target.getClass(), froms);
    }

    @Override
    protected void onlineChanged() {
        super.onlineChanged();
        synchronized (eventActionLock) {
            if (null != eventMap) {
                Collection<LinkedList<Event>> eventLists = eventMap.values();
                for (LinkedList<Event> list : eventLists) { // 触发所有事件
                    for (Event event : list) {
                        event.setOnline(isOnline());
                    }
                }
            }
        }
    }

    private void notifyEventList(LinkedList<Event> list, @NonNull String id, @Nullable Object src, @Nullable Object[] args) {
        if (null != list) {
            for (Event event : list) {
                event.onReceive(id, src, args);
            }
        }
    }
}
