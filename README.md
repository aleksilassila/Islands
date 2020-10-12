# Islands
A spigot plugin for creating floating island homes with different biomes.
Optimized for survival gamemode. **This is not a skyblock plugin!**
Jumping down from an island teleports player to survival "wilderness", 
that can be reset often without losing players' progress.

If you want to see the plugin in action, you can visit 46.101.169.26 (1.16.2)

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
*Tip for admins: `/island name spawn` for accessing spawn via `/vi spawn`*

The plugin has customizable generation settings, so the plugin can be optimized for any hardware

### Commands

Island Managment:
- `/island create <biome> (<SMALL / NORMAL / BIG>)`
- `/island regenerate <biome> (<SMALL / NORMAL / BIG>)`
- `/island delete`
- `/island give <player>`, transfers island ownership
- `/island name <name>`, allows other players to `/vi <name>`
- `/island unname`, sets island to private
- `/island setspawn`, set island spawnpoint


Other
- `/home (<id>)`
- `/homes`, list all player's islands
- `/visit <name>`, `/vi <name>`, visit public island
- `/trust <player>`, allow player to interact with blocks and entities of your island
- `/untrust <player>`

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

## Setting up

To set up the plugin, add following lines to your `bukkit.yml` file.
You will also have to install VoidGenerator plugin.

```
worlds:
  <Islands World Name>:
    generator: VoidGenerator:PLAINS
```

Replace `<Islands World Name>` with `islandsWorldName` from `plugins/Islands/config.yml`.
You may want to also set `level-name` in `server.properties` as `islandsWorldName`.
This is because the world specified in `level-name` holds player inventory data. If you set up automatical
`wilderness` (see below) wipes, make sure you are not using the world specified in `level-name` as wilderness. 

**Should you not set the world generator for `islands` world, your islands will spawn in normal world instead of an empty one.**

### Optional

You can set a specific island as default spawn island for new players.
To do so, set `isSpawn: true` for the target island in `islands.yml`.

The plugin will generate `wilderness` world for you, which is where players will spawn when they jump off their island.
You can change the world name with `wildernessWorldName` in the plugin config.
*Unless `wildernessWorldName` is the same as `level-name`, deleting this world should not wipe players' inventories.*

To further customize the plugin, check `plugins/islands/config.yml`.

## Permissions

| Permission                      | Affect                                  |
|---------------------------------|-----------------------------------------|
| `islands.command`               | Use /islands command                    |
| `islands.command.create`        | Create island                           |
| `islands.command.create.small`  | Create / regenerate small island        |
| `islands.command.create.normal` | Create / regenerate normal island       |
| `islands.command.create.big`    | Create / regenerate big island          |
| `islands.command.create.custom` | Create / regenerate custom sized island |
| `islands.command.regenerate`    | Regenerate island                       |
| `islands.command.delete`        | Delete island                           |
| `islands.command.give`          | Give island                             |
| `islands.command.name`          | Name island                             |
| `islands.command.unname`        | Unname island                           |
| `islands.command.home`          | Use /home command                       |
| `islands.command.home.list`     | Use /homes command                      |
| `islands.command.turst`         | Trust person                            |
| `islands.command.turst.list`    | List island's trusted players           |
| `islands.command.untrust`       | Untrust person                          |
| `islands.command.visit`         | Visit island                            |


Bypasses

| Permission                   | Affect                                                                  |
|------------------------------|-------------------------------------------------------------------------|
| `islands.bypass.islandLimit` | Ignore island create limit                                              |
| `islands.bypass.regenerate`  | Regenerate anyone's island                                              |
| `islands.bypass.delete`      | Delete anyone's island                                                  |
| `islands.bypass.give`        | Transfer any island's ownership / remove owner if no arguments provided |
| `islands.bypass.name`        | Name anyone's island                                                    |
| `islands.bypass.unname`      | Unname anyone's island                                                  |
| `islands.bypass.trust`       | Add trusted person to anyone's island                                   |
| `islands.bypass.trust.list`  | List any island's trusted players                                       |
| `islands.bypass.untrust`     | Remove trusted person from anyone's island                              |
| `islands.bypass.protection`  | Interact with anyone's island                                           |
| `islands.bypass.home`        | Use /home from anywhere                                                 |
