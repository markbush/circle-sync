package com.ocado.plus.api.circlesync;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Config {
  public static List<CircleMapping> loadConfig(String filename) throws IOException {
    List<CircleMapping> circles = new ArrayList<CircleMapping>();

    String line;
    BufferedReader reader = null;
    try {
      FileInputStream in = new FileInputStream(filename);
      reader = new BufferedReader(new InputStreamReader(in));
      while ((line = reader.readLine()) != null) {
        String[] parts = line.split(":");
        if (parts.length == 2) {
          CircleMapping mapping = new CircleMapping(parts[0], parts[1]);
          circles.add(mapping);
        }
      }
    } finally {
      if (reader != null) {
        reader.close();
      }
    }

    return circles;
  }
}
