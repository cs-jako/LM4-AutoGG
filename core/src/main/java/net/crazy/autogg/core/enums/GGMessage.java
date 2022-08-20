package net.crazy.autogg.core.enums;

public enum GGMessage {
  GG_UPPER("GG"),
  GG_LOWER("gg"),
  GF("gf"),
  GOOD_GAME("Good Game"),
  GOOD_FIGHT("Good Fight"),
  GOOD_ROUND("Good Round! :D");

  private final String message;
  GGMessage(String message) {
    this.message = message;
  }


  @Override
  public String toString() {
    return this.message;
  }
}
