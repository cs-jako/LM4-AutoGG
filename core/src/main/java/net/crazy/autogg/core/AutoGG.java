package net.crazy.autogg.core;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Singleton;
import jdk.internal.access.JavaNetHttpCookieAccess;
import net.crazy.autogg.core.enums.GGMessage;
import net.crazy.autogg.core.enums.SecondGGMessage;
import net.crazy.autogg.core.enums.TriggerType;
import net.crazy.autogg.core.listener.GeneralListener;
import net.crazy.autogg.core.listener.TriggerListener;
import net.labymod.api.addon.LabyAddon;
import net.labymod.api.client.network.server.ServerData;
import net.labymod.api.models.addon.annotation.AddonListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

@Singleton
@AddonListener
public class AutoGG extends LabyAddon<AutoGGConfiguration> {
  private final HashMap<Pattern, HashMap<TriggerType, HashSet<Pattern>>> triggers = new HashMap<>();
  private final HashMap<Pattern, HashMap<TriggerType, Pattern>> antiTriggers = new HashMap<>();
  private final HashMap<Pattern, String> messageAdditions = new HashMap<>();

  public final ExecutorService executorService = Executors.newFixedThreadPool(1);

  private Pattern currentServerPattern;

  @Override
  protected void enable() {
    this.registerSettingCategory();

    this.executorService.execute(this::loadRegex);

    Runtime.getRuntime().addShutdownHook(new Thread(this.executorService::shutdown));

    this.registerListener(GeneralListener.class);
    this.registerListener(TriggerListener.class);

    this.logger().info("[AutoGG] Addon enabled.");
  }

  @Override
  protected Class<AutoGGConfiguration> configurationClass() {
    return AutoGGConfiguration.class;
  }

  public void updateServer(ServerData serverData) {
    if (serverData == null) {
      this.currentServerPattern = null;
      return;
    }
    String serverIP = serverData.address().toString()
        .replaceAll("^(.*):\\d{1,5}$", "$1").toLowerCase(Locale.ENGLISH);

    for (Pattern keyPattern : this.triggers.keySet()) {
      if (keyPattern.matcher(serverIP).matches()) {
        this.currentServerPattern = keyPattern;
        return;
      }
    }

    this.currentServerPattern = null;
  }

  public boolean matchAnti(String message, TriggerType antiType) {
    boolean matchPattern = this.antiTriggers.get(this.currentServerPattern).get(antiType).matcher(message).matches();

    if (antiType == TriggerType.ANTI_KARMA) {
      return (this.configuration().getHideKarmaMessage().get() && matchPattern);
    }

    if (!this.configuration().getHideGGMessage().get())
      return false;

    if (matchPattern)
      return true;

    String[] messageSplits = message.split(" ");

    for (String antiTrigger : getAntiTriggers())
      for (String messageSplit : messageSplits)
        if (messageSplit.equals(antiTrigger))
          return true;

    return false;
  }

  public boolean canNotMatch() {
    return (this.currentServerPattern == null);
  }

  public boolean matchCasual(String message) {
    for (Pattern casualPattern : this.triggers.get(this.currentServerPattern).get(TriggerType.CASUAL))
      if (casualPattern.matcher(message).matches())
        return true;

    return false;
  }

  public boolean match(String message) {
    for (Pattern pattern : this.triggers.get(this.currentServerPattern).get(TriggerType.NORMAL))
      if (pattern.matcher(message).matches())
        return true;

    return false;
  }

  public String getMessageAddition() {
    return this.messageAdditions.get(this.currentServerPattern);
  }

  public void loadRegex() {
    JsonObject requestResult = this.downloadTriggerJson();

    if (requestResult == null) {
      displayMessage("§cError while fetching AutoGG data.");
      logger().error("Error while fetching AutoGG data.");
      return;
    }

    this.triggers.clear();
    this.antiTriggers.clear();
    this.messageAdditions.clear();

    for (Map.Entry<String, JsonElement> entry : requestResult.entrySet()) {
      //General for every trigger
      Pattern serverPattern = Pattern.compile(entry.getKey().replaceAll("\\\\{2}", "\\\\"));

      HashMap<TriggerType, HashSet<Pattern>> serverTriggers = new HashMap<>();
      JsonObject triggerObject = (JsonObject) entry.getValue();

      //Adding gg triggers
      JsonObject ggTriggerObject = triggerObject.getAsJsonObject("gg_triggers");

      HashSet<Pattern> normalTriggers = new HashSet<>();
      for (JsonElement triggerEntry : ggTriggerObject.getAsJsonArray("triggers")) {
        normalTriggers.add(
            Pattern.compile(triggerEntry.getAsString().replaceAll("\\\\{2}", "\\\\")));
      }
      serverTriggers.put(TriggerType.NORMAL, normalTriggers);

      HashSet<Pattern> casualTriggers = new HashSet<>();
      for (JsonElement triggerEntry : ggTriggerObject.getAsJsonArray("casual_triggers")) {
        casualTriggers.add(
            Pattern.compile(triggerEntry.getAsString().replaceAll("\\\\{2}", "\\\\")));
      }
      serverTriggers.put(TriggerType.CASUAL, casualTriggers);

      this.triggers.put(serverPattern, serverTriggers);

      //Adding anti Triggers
      JsonObject antiObject = triggerObject.getAsJsonObject("other_patterns");

      HashMap<TriggerType, Pattern> antiTriggers = new HashMap<>();
      antiTriggers.put(TriggerType.ANTI_GG,

          Pattern.compile(reformatAntiString(antiObject.get("antigg")
              .getAsString()).replaceAll("(?<!\\\\)\\$\\{antigg_strings}",
              String.join("|", this.getAntiTriggers()))));

      antiTriggers.put(TriggerType.ANTI_KARMA,
          Pattern.compile(reformatAntiString(antiObject.get("anti_karma").getAsString())));

      this.antiTriggers.put(serverPattern, antiTriggers);

      //Adding additional message
      this.messageAdditions.put(serverPattern,
          triggerObject.getAsJsonObject("other").get("msg").getAsString());
    }
    logger().info("[AutoGG] Updated Regex");
  }


  private String reformatAntiString(String stringToReformat) {
    return stringToReformat.substring(1, stringToReformat.length() - 1)
        .replaceAll("\\\\{2}", "\\\\");
  }

  @SuppressWarnings("deprecation")
  private JsonObject downloadTriggerJson() {
    JsonObject requestResult;

    try {
      //Http Request
      HttpURLConnection connection = (HttpURLConnection) new URL(
          "https://static.sk1er.club/autogg/regex_triggers_new.json").openConnection();
      connection.addRequestProperty("User-Agent",
          "java 8 HttpURLConnection (LabyMod AutoGG Addon by CrazySchnetzler & MineFlash07)");
      connection.setConnectTimeout(20000);
      connection.setReadTimeout(20000);
      connection.setDoOutput(true);
      connection.setUseCaches(false);
      connection.setRequestMethod("GET");

      StringBuilder resultBuilder = new StringBuilder();

      //Transform to string
      try (BufferedReader reader = new BufferedReader(
          new InputStreamReader(connection.getInputStream()))) {
        String line;
        while ((line = reader.readLine()) != null) {
          resultBuilder.append(line);
        }
      }

      requestResult = new JsonParser().parse(resultBuilder.toString()).getAsJsonObject();
    } catch (IOException | ClassCastException exception) {
      return null;
    }

    return requestResult.getAsJsonObject("servers");
  }

  private String[] getAntiTriggers() {
    HashSet<String> messages = new HashSet<>();

    for (GGMessage ggMessage : GGMessage.values()) {
      messages.add(ggMessage.name());
    }

    for (SecondGGMessage secondGGMessage : SecondGGMessage.values()) {
      messages.add(secondGGMessage.toString());
    }

    String[] result = new String[messages.size()];
    messages.toArray(result);

    return result;
  }
}
