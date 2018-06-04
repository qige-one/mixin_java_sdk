package one.mixin.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MixinHttpUtil {

  private static final OkHttpClient client = new OkHttpClient();

  public static String get(String url) throws IOException {
    Request request = new Request.Builder().url(url).build();
    Response response = client.newCall(request).execute();
    if (!response.isSuccessful()) {
      throw new IOException("Unexpected code " + response);
    }
    /*
    Headers responseHeaders = response.headers();
    for (int i = 0; i < responseHeaders.size(); i++) {
      System.out.println(responseHeaders.name(i) + ": " + responseHeaders.value(i));
    }
    */
    return response.body().string();
  }

  private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

  public static String post(
    String url, HashMap<String, String> headers, String body) throws IOException {
    Request.Builder builder = new Request.Builder().url(url).post(RequestBody.create(JSON, body));
    if (headers.size() > 0) {
      for (Map.Entry<String, String> entry : headers.entrySet()) {
        builder.addHeader(entry.getKey(), entry.getValue());
      }
    }
    Request request = builder.build();
    Response response = client.newCall(request).execute();
    if (!response.isSuccessful()) {
      throw new IOException("Unexpected code " + response);
    }
    return response.body().string();
  }
}
