package net.crazy.autogg.core;

import net.crazy.autogg.core.enums.GGMessage;
import net.crazy.autogg.core.settings.SecondMessageSettings;
import net.labymod.api.addon.AddonConfig;
import net.labymod.api.client.gui.screen.widget.widgets.input.ButtonWidget.ButtonSetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.SliderWidget.SliderSetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.SwitchWidget.SwitchSetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.dropdown.DropdownWidget.DropdownSetting;
import net.labymod.api.configuration.loader.annotation.ConfigName;
import net.labymod.api.configuration.loader.annotation.SpriteSlot;
import net.labymod.api.configuration.loader.annotation.SpriteTexture;
import net.labymod.api.configuration.loader.property.ConfigProperty;
import net.labymod.api.configuration.settings.Setting;
import net.labymod.api.configuration.settings.annotation.SettingSection;
import net.labymod.api.inject.LabyGuice;
import net.labymod.api.util.MethodOrder;

@SuppressWarnings("FieldMayBeFinal")
@ConfigName("settings")
@SpriteTexture("settings.png")
public class AutoGGConfiguration extends AddonConfig {

  /**
   * GENERAL
   */
  @SettingSection("general")
  @SwitchSetting
  @SpriteSlot(x = 1)
  private final ConfigProperty<Boolean> enabled = new ConfigProperty<>(true);

  @ButtonSetting(translation = "autogg.settings.refreshCache.button")
  @MethodOrder(after = "enabled")
  @SpriteSlot(x = 0)
  public void refreshCache(Setting setting) {
    AutoGG addon = LabyGuice.getInstance(AutoGG.class);
    addon.loadRegex();
  }

  @MethodOrder(after = "refreshCache")
  @SwitchSetting
  @SpriteSlot(x = 2)
  private final ConfigProperty<Boolean> casualGG = new ConfigProperty<>(false);

  @MethodOrder(after = "casualGG")
  @SliderSetting(min = 0f, max = 5000, steps = 5f)
  @SpriteSlot(x = 3)
  private final ConfigProperty<Integer> messageDelay = new ConfigProperty<>(1000);

  @MethodOrder(after = "messageDelay")
  @DropdownSetting
  @SpriteSlot(x = 4)
  private final ConfigProperty<GGMessage> ggMessage = new ConfigProperty<>(GGMessage.GG_UPPER);

  /**
   * Second message
   */

  @SettingSection("second_message")
  @MethodOrder(after = "messageDelay")
  @SpriteSlot(x = 5)
  private SecondMessageSettings secondMessageSettings = new SecondMessageSettings();

  /**
   * Hidden Messages
   */
  @SettingSection("hidden_messages")
  @MethodOrder(after = "secondMessageSettings")
  @SwitchSetting
  @SpriteSlot(x = 6)
  private final ConfigProperty<Boolean> hideGGMessage = new ConfigProperty<>(false);

  @MethodOrder(after = "hideGGMessage")
  @SwitchSetting
  @SpriteSlot(x = 7)
  private final ConfigProperty<Boolean> hideKarmaMessage = new ConfigProperty<>(false);


  @Override
  public ConfigProperty<Boolean> enabled() {
    return this.enabled;
  }

  public ConfigProperty<Boolean> getCasualGG() {
    return casualGG;
  }

  public ConfigProperty<Integer> getMessageDelay() {
    return messageDelay;
  }

  public ConfigProperty<GGMessage> getGgMessage() {
    return ggMessage;
  }

  public SecondMessageSettings getSecondMessageSettings() {
    return secondMessageSettings;
  }

  public ConfigProperty<Boolean> getHideGGMessage() {
    return hideGGMessage;
  }

  public ConfigProperty<Boolean> getHideKarmaMessage() {
    return hideKarmaMessage;
  }
}
