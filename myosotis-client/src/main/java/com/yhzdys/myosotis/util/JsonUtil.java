package com.yhzdys.myosotis.util;

import com.alibaba.fastjson2.JSON;

import java.util.List;

public final class JsonUtil {

    public static <T> T toObject(String json, Class<T> target) {
        return JSON.parseObject(json).to(target);
    }

    public static <T> List<T> toList(String json, Class<T> target) {
        return JSON.parseArray(json).toList(target);
    }

    public static String toString(Object target) {
        return JSON.toJSONString(target);
    }

}
