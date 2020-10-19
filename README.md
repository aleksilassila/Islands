# Islands
A spigot plugin for creating floating island homes with different biomes.
Optimized for survival gamemode. **This is not a skyblock plugin!**
Jumping down from an island teleports player to survival "wilderness", 
that can be reset often without losing players' progress.

If you want to experience the plugin in game, you can hop on blucraft.mc.gg! (1.16.3)

## Features
"Okay cool, islands. What's the point?" Here are some problems that this plugin solves.

**Griefing.**
Every island is protected by default.
The owner can `/trust` other players so that they can also interact with blocks and entities. 

**"Base managment."**
Want to start a new base? Just create a new island. Tired of creating a new world
multiple times to find the biome of your liking? Well, this plugin lets you chooose that too.
Personally I just like the idea of placing individual creations of mine on their own islands.
It also makes saving them in a schematic file a lot easier. (Also I might add this as a feature later)

**Resources.**
Everyone hates monuments that are already raided by another player.
This plugin allows resetting the survival world whenever you feel like it without losing your progress.
Your islands and inventory are saved in a separate world. 

**Community.** Players can easily visit each other's islands with `/visit` command.
The plugin has gui menu for browsing public islands. It is also possible to set up
"official" islands like spawn island that are owned by the server.
*Tip for admins: `/island name spawn` for accessing spawn via `/vi spawn`*

The plugin has customizable generation settings, so the plugin can be optimized for any hardware.
**To set up the plugin, check out [Wiki](https://github.com/aleksilassila/Islands/wiki).
You can also find extensive list of permissions, commands and configuration there.**

I'd recommend admins to set up daily or weekly reset of wilderness world and possibly nether and end too. 
This ensures that players have fresh resources available at all times and encourages players to build on islands as intended.
You can google more about how to schedule reboots and resets.

## Screenshots
### Functionality
Available biomes are fetched from `islandsSource` world. 
The search are can be adjusted to match server performance.
Supports all overworld biomes.

![Biomes](screenshots/functionality/biomes.png?raw=true)

Queue system ensures that generating islands does not cause lag.
Generation speed can also be adjusted.

![Queue](screenshots/functionality/queue2.png?raw=true)

Jumping down from an island teleport player to survival "wilderness",
where damage is enabled. Wilderness can be reset often without resetting players' progression.

![Wilderness](screenshots/functionality/wilderness.png?raw=true)

![Wilderness](screenshots/functionality/wilderness2.png?raw=true)

Islands are protected by default. No more "I forgot to protect my base and got griefed".
Players can trust other players to play together.

![Island Protection](screenshots/functionality/protection.png?raw=true)

Comes with a handy gui that players can use to visit other public islands!

![Island Protection](screenshots/functionality/visiting_gui.png?raw=true)

### Example Islands

![biome](screenshots/islandTypes/badlands.png?raw=true)

![biome](screenshots/islandTypes/desert_small.png?raw=true)

![biome](screenshots/islandTypes/jungle.png?raw=true)

![biome](screenshots/islandTypes/taiga.png?raw=true)

![biome](screenshots/islandTypes/dark_woods_hills.png?raw=true)

![biome](screenshots/islandTypes/desert_night.png?raw=true)
