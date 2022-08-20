package net.crazy.autogg.core.settings;

import net.crazy.autogg.core.enums.SecondGGMessage;
import net.labymod.api.client.gui.screen.widget.widgets.input.SliderWidget.SliderSetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.SwitchWidget.SwitchSetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.dropdown.DropdownWidget.DropdownSetting;
import net.labymod.api.configuration.loader.Config;
import net.labymod.api.configuration.loader.annotation.ParentSwitch;
import net.labymod.api.configuration.loader.annotation.SpriteSlot;
import net.labymod.api.configuration.loader.property.ConfigProperty;
import net.labymod.api.util.MethodOrder;

public class SecondMessageSettings extends Config {

  @ParentSwitch
  @SwitchSetting
  private ConfigProperty<Boolean> enabled = new ConfigProperty<>(false);

  @MethodOrder(after = "enabled")
  @SliderSetting(min = 0f, max = 5000, steps = 5f)
  @SpriteSlot(x = 3)
  private final ConfigProperty<Integer> messageDelay = new ConfigProperty<>(1000);

  @MethodOrder(after = "messageDelay")
  @DropdownSetting
  @SpriteSlot(x = 4)
  private final ConfigProperty<SecondGGMessage> message = new ConfigProperty<>(SecondGGMessage.HEART);

  @MethodOrder(after = "message")
  @SwitchSetting
  @SpriteSlot(x = 2)
  private ConfigProperty<Boolean> sendOnCasual = new ConfigProperty<>(true);

  public ConfigProperty<Boolean> getEnabled() {
    return enabled;
  }

  public ConfigProperty<Integer> getMessageDelay() {
    return messageDelay;
  }

  public ConfigProperty<SecondGGMessage> getMessage() {
    return message;
  }

  public ConfigProperty<Boolean> getSendOnCasual() {
    return sendOnCasual;
  }
}
