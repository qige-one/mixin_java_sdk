package one.mixin.api;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.UUID;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class MixinBot {

  public static WebSocket connectToRemoteMixin(WebSocketListener callback) {
    String token = MixinUtil.JWTTokenGen.genToken("/", "");
    OkHttpClient client = new OkHttpClient.Builder().build();
    Request request = new Request.Builder()
      .addHeader("Sec-WebSocket-Protocol", "MixinBot-Blaze-1")
      .addHeader("Authorization", "Bearer " + token)
      .url("wss://blaze.mixin.one/")
      .build();
    return client.newWebSocket(request, callback);
  }

  private static boolean send(WebSocket webSocket, Action action, String params) {
    String rawJson =
      "{" +
        "  'id': '" + UUID.randomUUID().toString() + "'," +
        "  'action': '" + action + "'," +
        "  'params': " + params +
        "}";
    String json = rawJson.replaceAll("'", "\"");
    return webSocket.send(MixinUtil.jsonStrToByteString(json));
  }

  public static boolean sendListPendingMessages(WebSocket webSocket) {
    return send(webSocket, Action.LIST_PENDING_MESSAGES, null);
  }

  public static boolean sendMessageAck(WebSocket webSocket, String messageId) {
    String params =
      String.format(("{'message_id':'%s', 'status':'READ'}").replaceAll("'", "\"")
        , messageId);
    return send(webSocket, Action.ACKNOWLEDGE_MESSAGE_RECEIPT, params);
  }

  public static boolean sendText(
    WebSocket webSocket,
    String conversationId,
    String recipientId,
    String data) {
    String params =
      String.format(
        ("{'conversation_id':'%s', 'recipient_id':'%s', 'message_id':'%s', 'category':'%s', " +
          "'data':'%s'}").replaceAll("'", "\"")
        ,
        conversationId,
        recipientId,
        UUID.randomUUID().toString(),
        Category.PLAIN_TEXT,
        toBase64(data)
      );
    return send(webSocket, Action.CREATE_MESSAGE, params);
  }

  public static boolean sendSticker(
    WebSocket webSocket,
    String conversationId,
    String recipientId,
    String data) {
    String params =
      String.format(
        ("{'conversation_id':'%s', 'recipient_id':'%s', 'message_id':'%s', 'category':'%s', " +
          "'data':'%s'}").replaceAll("'", "\"")
        ,
        conversationId,
        recipientId,
        UUID.randomUUID().toString(),
        Category.PLAIN_STICKER,
        toBase64(data)
      );
    return send(webSocket, Action.CREATE_MESSAGE, params);
  }

  public static boolean sendContact(
    WebSocket webSocket,
    String conversationId,
    String recipientId,
    String contactId) {
    String params =
      String.format(
        ("{'conversation_id':'%s', 'recipient_id':'%s', 'message_id':'%s', 'category':'%s', " +
          "'data': '%s'}").replaceAll("'", "\""),
        conversationId,
        recipientId,
        UUID.randomUUID().toString(),
        Category.PLAIN_CONTACT,
        toBase64(String.format("{'user_id': '%s'}".replaceAll("'", "\""), contactId))
      );
    return send(webSocket, Action.CREATE_MESSAGE, params);
  }

  private static HashMap<String, String> makeHeaders(String token) {
    HashMap<String, String> headers = new HashMap<String, String>();
    headers.put("Mixin-Device-Id", Config.ADMIN_ID);
    headers.put("Content-Type", "application/json");
    headers.put("Authorization", "Bearer " + token);
    return headers;
  }

  /*
  public static String assets() {
    String token = MixinUtil.JWTTokenGen.genToken("GET", "/assets", "");
    String res = api.basic.RestAPI.get(
      "https://api.mixin.one/assets",
      makeHeaders(token)
    );
    return res;
  }
  */

  public static void transferTo(
    String assetId,
    String counterUserIid,
    double amount) throws IOException {
    String body =
      String.format(
        ("{'asset_id':'%s', 'counter_user_id':'%s', 'amount':'%s', 'memo':'hello', 'pin':'%s', " +
          "'trace_id': '%s'}").replaceAll("'", "\""),
        assetId,
        counterUserIid,
        amount,
        MixinUtil.encryptPayKey(Config.PIN, Config.PAY_KEY),
        UUID.randomUUID().toString());
    String token = MixinUtil.JWTTokenGen.genToken("POST", "/transfers", body);
    String res = MixinHttpUtil.post(
      "https://api.mixin.one/transfers",
      makeHeaders(token),
      body
    );
  }

  private static String toBase64(String orig) {
    return new String(Base64.getEncoder().encode(orig.getBytes()));
  }
}
