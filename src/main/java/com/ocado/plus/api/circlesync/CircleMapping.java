package com.ocado.plus.api.circlesync;

import java.util.ArrayList;
import java.util.List;

public class CircleMapping {
  /**
   * The name of the group in the source system.
   */
  private String sourceName;
  /**
   * The name of the Circle to sync with.
   */
  private String circleName;
  /**
   * The list of user IDs that should be in this Circle.
   */
  private List<String> users = new ArrayList<String>();

  public CircleMapping(String sourceName, String circleName) {
    this.sourceName = sourceName;
    this.circleName = circleName;
  }

  public String getSourceName() {
    return sourceName;
  }
  public void setSourceName(String sourceName) {
    this.sourceName = sourceName;
  }

  public String getCircleName() {
    return circleName;
  }
  public void setCircleName(String circleName) {
    this.circleName = circleName;
  }

  public List<String> getUsers() {
    return users;
  }
  public void setUsers(List<String> users) {
    this.users = users;
  }
}
