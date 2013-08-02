package com.ocado.plus.api.circlesync;

import java.util.List;

public class CircleInfo {
  private String id;
  private List<String> members;

  public CircleInfo(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  public List<String> getMembers() {
    return members;
  }
  public void setMembers(List<String> members) {
    this.members = members;
  }
}
