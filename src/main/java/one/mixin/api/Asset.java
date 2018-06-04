package one.mixin.api;

public enum Asset {
  BTC(null), ETH(null), EOS(null), PRS(null);

  private final String uuid;

  Asset(String uuid) {
    this.uuid = uuid;
  }
}
