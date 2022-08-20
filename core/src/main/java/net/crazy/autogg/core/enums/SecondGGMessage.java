package net.crazy.autogg.core.enums;

public enum SecondGGMessage {
  HEART("<3"),
  GOOD_DAY("Have a good day!"),
  AUTOGG_FOR_LABYMOD("AutoGG for LabyMod"),
  AUTOGG_BY_SK1ER("AutoGG by Sk1er!");

  private final String message;

  SecondGGMessage(String message) {
    this.message = message;
  }

  @Override
  public String toString() {
    return message;
  }
}
