package com.yhzdys.myosotis.support;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class LockStore {

    private static final Map<String, Lock> lockMap = new ConcurrentHashMap<>(2);

    public static Lock get(String key) {
        return lockMap.computeIfAbsent(key, k -> new Lock());
    }

    private static class Lock {
    }
}
