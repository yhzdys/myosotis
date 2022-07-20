package com.yhzdys.myosotis.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class ObjectUtil {

    private static final AtomicInteger objectIds = new AtomicInteger(0);
    private static final Map<Object, Integer> objectIdMap = new ConcurrentHashMap<>(0);

    public static Integer getId(Object object) {
        return objectIdMap.computeIfAbsent(object, o -> objectIds.incrementAndGet());
    }

}
