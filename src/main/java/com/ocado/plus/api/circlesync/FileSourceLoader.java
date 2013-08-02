package com.ocado.plus.api.circlesync;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class FileSourceLoader implements SourceLoader {

  @Override
  public List<String> getMembersForGroup(String groupName) throws Exception {
    List<String> members = new ArrayList<String>();
    String line;
    BufferedReader reader = null;
    try {
      FileInputStream in = new FileInputStream(groupName);
      reader = new BufferedReader(new InputStreamReader(in));
      while ((line = reader.readLine()) != null) {
        members.add(line);
      }
    } finally {
      if (reader != null) {
        reader.close();
      }
    }
    return members;
  }
}
