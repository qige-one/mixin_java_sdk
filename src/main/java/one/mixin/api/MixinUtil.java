package one.mixin.api;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import okio.ByteString;

public class MixinUtil {

  public static String bytesToJsonStr(ByteString bytes) {
    return new String(Gzip.gzipUncompress(bytes.toByteArray()));
  }

  static ByteString jsonStrToByteString(String json) {
    byte[] bytes = Gzip.gzipCompress(json.getBytes());
    return ByteString.of(bytes, 0, bytes.length);
  }

  private static class Gzip {
    static byte[] gzipCompress(byte[] uncompressedData) {
      byte[] result = new byte[]{};
      try (ByteArrayOutputStream bos = new ByteArrayOutputStream(uncompressedData.length);
           GZIPOutputStream gzipOS = new GZIPOutputStream(bos)) {
        gzipOS.write(uncompressedData);
        gzipOS.close();
        result = bos.toByteArray();
      } catch (IOException e) {
        e.printStackTrace();
      }
      return result;
    }

    static byte[] gzipUncompress(byte[] compressedData) {
      byte[] result = new byte[]{};
      try (ByteArrayInputStream bis = new ByteArrayInputStream(compressedData);
           ByteArrayOutputStream bos = new ByteArrayOutputStream();
           GZIPInputStream gzipIS = new GZIPInputStream(bis)) {
        byte[] buffer = new byte[1024];
        int len;
        while ((len = gzipIS.read(buffer)) != -1) {
          bos.write(buffer, 0, len);
        }
        result = bos.toByteArray();
      } catch (IOException e) {
        e.printStackTrace();
      }
      return result;
    }
  }

}
