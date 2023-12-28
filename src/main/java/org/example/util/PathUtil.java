package org.example.util;

public class PathUtil {

  private PathUtil() {
  }

  public static Long getNamespaceId(String path) {
    String[] split = path.split("/");
    return Long.parseLong(split[3]);
  }
}
