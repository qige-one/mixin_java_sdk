package one.mixin.api;

import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;

public class Config {

  // 修改为你自己的 APP_ID
  public static final String APP_ID = "04955bc2-9c3e-4cb4-868c-749dbab599bf";
  // 修改为在 developers.mixin.one/dashboard 上获取到的 SECRET
  public static final String SECRET = "4fa82c5c40389d3dd021c00f7d173388f60286573edb7fb46320fcdd3b0e20a4";
  // 修改为在 developers.mixin.one/dashboard 上获取到的 PIN
  public static final String PIN = "993570";
  // 修改为在 developers.mixin.one/dashboard 上获取到的 SESSION_ID
  public static final String SESSION_ID = "f68a2e9c-3106-4e1c-b895-4208a83c9f34";
  // 修改为在 developers.mixin.one/dashboard 上获取到的 TOKEN
  public static final String TOKEN = "lAv9w9MytOM+6b7zGwu+VjB0Sy+db0wN0bt5Dqfwg57U/BwIBAURkKH9MKUhMXuzIa7X762rWxwd2VguZq4yYwsByRJ2Gz/ynTP8L8Vmb9FIUWjoFy1fBxjnR4V+o+fZoigszhcqxlR12WnVdBjZKaAlxM/3ea6CHdDrLJTyuHo=";
  // 修改为你自己（即 APP 作者）的 Mixin 账号的 UUID
  public static final String ADMIN_ID = "0c21b607-5e5b-461b-963f-95708346c21d";

  private static RSAPrivateKey loadPrivateKey() {
    try {
      PrivateKey key =
        new PrivateKeyReader(Config.class.getResourceAsStream("src/main/resources/rsa_private_key.txt"))
          .getPrivateKey();
      return (RSAPrivateKey) key;
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
      return null;
    }
  }

  public static final RSAPrivateKey RSA_PRIVATE_KEY = loadPrivateKey();
  
  public static final byte[] PAY_KEY = MixinUtil.decrypt(RSA_PRIVATE_KEY, TOKEN, SESSION_ID);
}
