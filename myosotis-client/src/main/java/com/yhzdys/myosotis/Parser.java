package com.yhzdys.myosotis;

@FunctionalInterface
public interface Parser<T> {

    T parse(String configValue);
}
