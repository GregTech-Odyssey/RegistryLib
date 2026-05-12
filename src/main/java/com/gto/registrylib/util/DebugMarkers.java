package com.gto.registrylib.util;

import lombok.experimental.UtilityClass;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

@UtilityClass
public class DebugMarkers {

    private Marker marker(String name) {
        return MarkerManager.getMarker("REGISTRYLIB." + name);
    }

    public static final Marker REGISTER = marker("REGISTER");
    public static final Marker DATA = marker("DATA");

    public static Marker register() {
        return REGISTER;
    }

    public static Marker data() {
        return DATA;
    }
}
