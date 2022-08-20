package net.crazy.autogg.core.listener;

import com.google.inject.Inject;
import net.crazy.autogg.core.AutoGG;
import net.crazy.autogg.core.enums.TriggerType;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.chat.ChatReceiveEvent;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TriggerListener {
  private final AutoGG addon;
  private boolean invokedGG = false;

  private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(5);

  @Inject
  private TriggerListener(AutoGG addon) {
    this.addon = addon;
  }


  @Subscribe
  public void onMessageReceived(ChatReceiveEvent event) {
    if (addon.canNotMatch())
      return;

    String message = PlainTextComponentSerializer.plainText().serialize(event.message());
    message = message.replaceAll("&[mnkolr]", "");

    if (this.addon.matchAnti(message, TriggerType.ANTI_KARMA)) {
      event.setCancelled(true);
      return;
    }

    if (this.addon.matchAnti(message, TriggerType.ANTI_GG)) {
      event.setCancelled(true);
      return;
    }

    if (invokedGG)
      return;

    if (this.addon.configuration().getCasualGG().get() && this.addon.matchCasual(message)) {
      this.sendGGMessage(true);
      return;
    }

    if (this.addon.configuration().enabled().get() && this.addon.match(message))
      this.sendGGMessage(false);
  }

  private void sendGGMessage(boolean casual) {
    this.invokedGG = true;
    this.executorService.schedule(() -> {
      String messageAddition = this.addon.getMessageAddition();

      addon.sendMessage(messageAddition + this.addon.configuration().getGgMessage().get().toString());

      if (this.addon.configuration().getSecondMessageSettings().getEnabled().get()
          && (!casual || this.addon.configuration().getSecondMessageSettings().getSendOnCasual().get())) {
        this.executorService.schedule(() -> {
          addon.sendMessage(messageAddition +
              this.addon.configuration().getSecondMessageSettings().getMessage().get().toString());
        }, this.addon.configuration().getSecondMessageSettings().getMessageDelay().get(),
            TimeUnit.MILLISECONDS);
      }

      try {
        Thread.sleep(2000);
      } catch (InterruptedException exception) {
        addon.logger().error(exception.getMessage());
      }

      this.invokedGG = false;
    }, this.addon.configuration().getMessageDelay().get(), TimeUnit.MILLISECONDS);
  }
}
