package com.mosect.moevent.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by MoSect on 2018/2/4.
 * 事件接收器注解，用此注解表示的方法，将被当成事件接收器，使用EventManager.bind方法可以将此接收器
 * 绑定给事件管理器。
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EventReceiver {

    /**
     * 事件id，表示此接收器可以接收的事件
     *
     * @return 事件id，默认为空串，表示可以接收所有事件
     */
    public String value() default "";

    /**
     * 来源的下标（EventManager.bind中froms参数的下标），指定接收器来源，非此来源不接收
     *
     * @return 来源下标，默认为-1，表示没有设置来源，可以接收所有来源的事件
     */
    public int fromIndex() default -1;

    /**
     * 最大的离线缓存数量
     *
     * @return 缓存数量，默认为Integer.MAX_VALUE
     */
    public int maxCacheCount() default Integer.MAX_VALUE;

    /**
     * 离线是否可以接收事件
     *
     * @return 默认为false, 离线不可以接收事件
     */
    public boolean offlineReceive() default false;

    /**
     * 当前线程接收，如果设置了当前线程接收，表示该接收器只有在事件发送的线程接收，创建该接收器的线程不接收
     *
     * @return 是否是当前线程接收，默认为false
     */
    public boolean currentThreadReceive() default false;
}
