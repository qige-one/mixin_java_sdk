package one.mixin.api;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import org.apache.commons.codec.binary.Hex;

import java.security.MessageDigest;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class MixinBot {

  private static class JWTTokenGen {

    static String genToken(String uri, String body) {
      return genToken(uri, body, UUID.randomUUID().toString());
    }

    private static String genToken(String uri, String body, String jti) {
      String sig = genSig("GET", uri, body);
      long ts = System.currentTimeMillis();
      String token =
        JWT
          .create()
          .withClaim("uid", Config.APP_ID)
          .withClaim("sid", Config.SESSION_ID)
          .withIssuedAt(new Date(ts))
          .withExpiresAt(new Date(ts + 1 * 60 * 60 * 1000L))
          .withClaim("sig", sig)
          .withClaim("jti", jti)
          .sign(Algorithm.RSA512(null, Config.RSA_PRIVATE_KEY));
      return token;
    }

    private static String genSig(String method, String uri, String body) {
      try {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return Hex.encodeHexString(md.digest((method + uri + body).getBytes()));
      } catch (Exception e) {
        e.printStackTrace();
        System.exit(1);
        return null;
      }
    }
  }

  public static WebSocket connectToRemoteMixin(WebSocketListener callback) {
    String token = JWTTokenGen.genToken("/", "");
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
        toBase64(toBase64(String.format("{'user_id': '%s'}".replaceAll("'", "\""), contactId)))
      );
    return send(webSocket, Action.CREATE_MESSAGE, params);
  }

  private static String toBase64(String orig) {
    return new String(Base64.getEncoder().encode(orig.getBytes()));
  }
}
