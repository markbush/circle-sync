package com.ocado.plus.api.circlesync;

import java.util.List;

public interface SourceLoader {
  public List<String> getMembersForGroup(String groupName) throws Exception;
}
