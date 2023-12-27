package org.example.util.constant;

import org.example.persistence.entity.User;

public class ContextKeys {

  public static final String ROLE_KEYS = "roleKeys";
  public static final Class<User> USER_KEY = User.class;

  private ContextKeys() {
  }
}
