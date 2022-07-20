package com.yhzdys.myosotis.misc;

import org.slf4j.Logger;

public final class LoggerFactory {

    public static Logger getLogger() {
        return org.slf4j.LoggerFactory.getLogger("myosotis");
    }

}
