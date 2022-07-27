package com.yhzdys.myosotis;

/**
 * config value parser functionalInterface
 */
@FunctionalInterface
public interface Parser<T> {

    /**
     * @param configValue configValue
     * @return parsed value
     */
    T parse(String configValue);
}
