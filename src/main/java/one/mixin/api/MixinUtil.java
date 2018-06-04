package one.mixin.api;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import org.apache.commons.codec.binary.Hex;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.spec.MGF1ParameterSpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.Random;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import javax.crypto.spec.SecretKeySpec;

import okio.ByteString;

public class MixinUtil {

  public static class JWTTokenGen {

    static String genToken(String uri, String body) {
      return genToken("GET", uri, body, UUID.randomUUID().toString());
    }

    static String genToken(String method, String uri, String body) {
      return genToken(method, uri, body, UUID.randomUUID().toString());
    }

    private static String genToken(String method, String uri, String body, String jti) {
      String sig = genSig(method, uri, body);
      long ts = System.currentTimeMillis();
      String token =
        JWT
          .create()
          .withClaim("uid", Config.APP_ID)
          .withClaim("sid", Config.SESSION_ID)
          .withIssuedAt(new Date(ts))
          .withExpiresAt(new Date(ts + 12 * 60 * 60 * 1000L))
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

  //解密 Key：私钥
  public static byte[] decrypt(Key key, String encryptedString, String sessionId) {
    try {
      Cipher c = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
      c.init(Cipher.DECRYPT_MODE, key,
        new OAEPParameterSpec(
          "SHA-256", "MGF1", MGF1ParameterSpec.SHA256, new PSource.PSpecified(sessionId.getBytes())));
      byte[] decodedBytes;
      byte[] in = Base64.getDecoder().decode(encryptedString);
      decodedBytes = c.doFinal(in);
      return decodedBytes;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  private static void copyOfRange(byte[] from, byte[] to, int index) {
    for (int i = 0; i < from.length; i++) {
      to[index + i] = from[i];
    }
  }

  public static String encryptPayKey(String pin, byte[] PAY_KEY) {
    int ts = (int) (System.currentTimeMillis() / 1000L);
    byte[] pinBytes = pin.getBytes();
    byte[] tsBytes =
      new byte[]{
        (byte) (ts % 0x100),
        (byte) ((ts % 0x10000) >> 8),
        (byte) ((ts % 0x1000000) >> 16),
        (byte) (ts >> 24),
        0, 0, 0, 0};
    byte[] paddedBytes = new byte[6 + 8 + 8 + 10];
    Arrays.fill(paddedBytes, (byte) 10);
    copyOfRange(pinBytes, paddedBytes, 0);
    copyOfRange(tsBytes, paddedBytes, 6);
    copyOfRange(tsBytes, paddedBytes, 6 + 8);
    byte[] ivv = new byte[16];
    new Random().nextBytes(ivv);
    String encryptedPayKey = encrypt(PAY_KEY, ivv, paddedBytes);
    return encryptedPayKey;
  }

  private static String encrypt(byte[] key, byte[] ivv, byte[] toEnc) {
    try {
      IvParameterSpec iv = new IvParameterSpec(ivv);
      SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");

      Cipher cipher = Cipher.getInstance("AES/CBC/NOPADDING");
      cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

      byte[] encrypted = cipher.doFinal(toEnc);
      byte[] all = new byte[ivv.length + encrypted.length];
      copyOfRange(ivv, all, 0);
      copyOfRange(encrypted, all, ivv.length);
      String sss = Base64.getEncoder().encodeToString(all);
      // System.out.println("encrypted string: " + sss);
      return sss;
    } catch (Exception ex) {
      ex.printStackTrace();
    }

    return null;
  }

}
