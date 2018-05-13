package one.mixin.api;

import com.google.gson.JsonObject;

public enum Action {

  LIST_PENDING_MESSAGES,
  CREATE_MESSAGE,
  ACKNOWLEDGE_MESSAGE_RECEIPT,
  ERROR,
  NOT_IMPLEMENTED_BY_THIS_SDK_YET;

  public static Action parseFrom(JsonObject obj) {
    return parseFrom(obj.get("action").getAsString());
  }

  public static Action parseFrom(String value) {
    if (value == null) {
      throw new IllegalArgumentException("the value to parse cannot be null");
    } else if (value.length() == 0) {
      return null;
    } else {
      try {
        return Action.valueOf(value);
      } catch (Exception e) {
        return NOT_IMPLEMENTED_BY_THIS_SDK_YET;
      }
    }
  }
}
