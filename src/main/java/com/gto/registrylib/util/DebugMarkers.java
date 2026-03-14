package com.gto.registrylib.util;

import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

public class DebugMarkers {

  private static Marker marker(String name) {
    return MarkerManager.getMarker("REGISTRYLIB." + name);
  }

  public static final Marker REGISTER = marker("REGISTER");
  public static final Marker DATA = marker("DATA");
}
