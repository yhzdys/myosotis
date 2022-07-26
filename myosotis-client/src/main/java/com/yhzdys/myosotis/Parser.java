package com.yhzdys.myosotis;

/**
 * config value parser functionalInterface
 */
@FunctionalInterface
public interface Parser<T> {

    T parse(String configValue);

}
