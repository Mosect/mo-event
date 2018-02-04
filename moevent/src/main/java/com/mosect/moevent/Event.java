package com.mosect.moevent;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by MoSect on 2018/2/4.
 * 事件
 */

public interface Event {

    /**
     * 接收事件
     *
     * @param id   事件id
     * @param src  来源
     * @param args 参数
     */
    void onReceive(@NonNull String id, @Nullable Object src, @Nullable Object[] args);

    /**
     * 获取id
     *
     * @return id，空串代表所有事件
     */
    @NonNull
    String getId();

    /**
     * 获取事件所属的管理器
     *
     * @return 管理器
     */
    @Nullable
    EventManager getManager();

    /**
     * 设置事件所属的管理器
     *
     * @param manager 管理器
     */
    void setManager(@Nullable EventManager manager);

    /**
     * 判断是否在线
     *
     * @return true，在线
     */
    boolean isOnline();

    /**
     * 设置是否在线
     *
     * @param online 在线
     */
    void setOnline(boolean online);
}
