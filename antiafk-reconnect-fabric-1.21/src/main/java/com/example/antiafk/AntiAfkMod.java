package com.example.antiafk;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.network.ClientConnection;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.Nullable;

public class AntiAfkMod implements ClientModInitializer {
    private static final int TICKS_PER_SWING = 40; // ~2 seconds (20 ticks = 1s)
    private int tickCounter = 0;
    private @Nullable ServerAddressHolder lastServer = null;

    @Override
    public void onInitializeClient() {
        // Anti-AFK: swing hand every TICKS_PER_SWING ticks
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) {
                tickCounter = 0;
                return;
            }
            tickCounter++;
            if (tickCounter >= TICKS_PER_SWING) {
                tickCounter = 0;
                try {
                    client.player.swingHand(Hand.MAIN_HAND);
                } catch (Exception e) {
                    // best-effort; ignore
                }
            }
        });

        // Track joining server to store server info for reconnect
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            try {
                if (client.getCurrentServerEntry() != null) {
                    ServerInfo info = client.getCurrentServerEntry();
                    lastServer = new ServerAddressHolder(info.address, info.name);
                } else {
                    lastServer = null;
                }
            } catch (Throwable t) {
                // some Fabric/MC mappings differ; fallback to null
                lastServer = null;
            }
        });

        // On disconnect, attempt reconnect after 2000ms (2s)
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            if (lastServer == null) return;
            final String address = lastServer.address;
            final String name = lastServer.name;
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    // Attempt to reconnect on the client thread
                    MinecraftClient.getInstance().execute(() -> {
                        try {
                            Screen parent = MinecraftClient.getInstance().currentScreen;
                            // Connecting screen constructor/methods vary between versions/mappings.
                            // We use a best-effort approach: open the multiplayer screen then open the connecting screen.
                            MinecraftClient.getInstance().setScreen(new MultiplayerScreen(parent));
                            // After opening multiplayer screen, user can select server; many mappings require more advanced usage.
                            // If connecting directly is supported in your environment, replace with a ConnectingScreen call.
                        } catch (Exception e) {
                            // ignore errors
                        }
                    });
                } catch (InterruptedException e) {
                    // ignore
                }
            }).start();
        });
    }

    private static class ServerAddressHolder {
        public final String address;
        public final String name;
        public ServerAddressHolder(String address, String name) {
            this.address = address;
            this.name = name;
        }
    }
}
