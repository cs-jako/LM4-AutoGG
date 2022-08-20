package net.crazy.autogg.core.listener;

import com.google.inject.Inject;
import net.crazy.autogg.core.AutoGG;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.network.server.NetworkDisconnectEvent;
import net.labymod.api.event.client.network.server.NetworkLoginEvent;

public class GeneralListener {
  private final AutoGG addon;

  @Inject
  public GeneralListener(AutoGG addon) {
    this.addon = addon;
  }

  @Subscribe
  public void onJoin(NetworkLoginEvent event) {
    addon.updateServer(event.serverData());
  }

  @Subscribe
  public void onDisconnect(NetworkDisconnectEvent event) {
    addon.updateServer(null);
  }
}
