# AntiAFK-Reconnect (Fabric) - Minecraft 1.21

What this package contains:
- A Fabric mod project skeleton that implements:
  - Anti-AFK: swings the player's hand every 40 ticks (≈2 seconds).
  - Auto-reconnect: best-effort: stores the last server address on join and attempts
    to open the Multiplayer screen 2 seconds after a disconnect.

Important notes:
1. **Server rules & ethics**: Many servers forbid AFK-bypassing. Use responsibly.
2. **Why "best-effort" for reconnect**: Minecraft/Fabric mappings and client internals
   change between builds. The reconnect code in this project uses a conservative
   approach that opens the Multiplayer screen after a 2-second delay. In many
   environments you can replace the placeholder with a direct `ConnectingScreen`
   call if your mappings support it.
3. **Building**:
   - Install JDK 17.
   - Run `./gradlew build` in the project root (on Windows use `gradlew.bat`).
   - The compiled mod jar will be in `build/libs/` (if you implement a proper Fabric
     loom build configuration and add necessary dependency versions).
4. **Tailor to your environment**:
   - You may need to update Fabric Loader / Fabric API versions in `build.gradle.kts`.
   - If you want direct reconnect (auto-join same server) you may need to modify the
     `DISCONNECT` handler to use `new ConnectingScreen(...)` or call internal client
     methods to connect to `lastServer.address`. This requires matching the exact
     Yarn/Mapping names for your Fabric/MC setup.

If you'd like, I can:
- Attempt to update the code to call `ConnectingScreen` directly for a specific Fabric API/build (tell me which Fabric/Loom versions you plan to use), or
- Produce a simpler ZIP containing only a runnable JAR (I would need an online build environment which I don't have here).
