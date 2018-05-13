package one.mixin.api;

import com.google.gson.JsonObject;

public enum Category {
  PLAIN_TEXT,
  PLAIN_DATA,
  PLAIN_IMAGE,
  PLAIN_STICKER,
  PLAIN_CONTACT,

  SYSTEM_CONVERSATION,
  SYSTEM_ACCOUNT_SNAPSHOT,
  BAD_OR_NOT_IMPLEMENTED_BY_THIS_SDK_YET;

  public static Category parseFrom(JsonObject obj) {
    return parseFrom(obj.get("data").getAsJsonObject().get("category").getAsString());
  }

  public static Category parseFrom(String value) {
    if (value == null) {
      throw new IllegalArgumentException("the value to parse cannot be null");
    } else if (value.length() == 0) {
      return null;
    } else {
      try {
        return Category.valueOf(value);
      } catch (Exception e) {
        return BAD_OR_NOT_IMPLEMENTED_BY_THIS_SDK_YET;
      }
    }
  }
}
