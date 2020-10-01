# Islands
A spigot plugin for creating floating island homes with different biomes.
Optimized for survival gamemode. **This is not a skyblock plugin!**
Jumping down from an island teleports player to survival "wilderness", 
that can be reset often without losing players' progress.

## Features
- Solves griefing and map resetting entirely.
- Create islands with any overworld biome
- Customizable island sizes
- Island protection
- Visiting other people's islands
  * Tip for admins: `/island name spawn` for accessing spawn via `/vi spawn`
- Customizable generation settings and speed, optimize for any hardware

### Commands

Island Managment:
- `/island create <biome> (<SMALL / NORMAL / BIG>)`
- `/island regenerate <biome> (<SMALL / NORMAL / BIG>)`
- `/island delete`
- `/island give <player>`, transfers island ownership
- `/island name <name>`, allows other players to `/vi <name>`
- `/island unname`, sets island to private

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

Replace `<Islands World Name>` with `leve-name` from `server.properties`.
For the sake of clarity you may want to set the `level-name` to something like `islands`.

Should you skip the step above, your islands will spawn in normal world instead of an empty one.

You can set a specific island as default spawn island for new players.
To do so, set `isSpawn: true` for the target island in `islands.yml`.

The plugin will generate `wilderness` world for you, which is where players will spawn when they jump off their island.
Deleting this world should be safe and players' inventories should not be reset. 
Deletion of the world specified in `level-name` will however, empty player inventories.

To further customize the plugin, check `plugins/islands/config.yml`.

## Permissions

| Permission              | Affect                            |
|-------------------------|-----------------------------------|
| `islands`               | Use /islands command              |
| `islands.create`        | Create island                     |
| `islands.create.small`  | Create / regenerate small island  |
| `islands.create.normal` | Create / regenerate normal island |
| `islands.create.big`    | Create / regenerate big island    |
| `islands.regenerate`    | Regenerate island                 |
| `islands.delete`        | Delete island                     |
| `islands.give`          | Give island                       |
| `islands.name`          | Name island                       |
| `islands.unname`        | Unname island                     |
| `islands.home`          | Use /home command                 |
| `islands.home.list`     | Use /homes command                |
| `islands.turst`         | Trust person                      |
| `islands.turst.list`    | List island's trusted players     |
| `islands.untrust`       | Untrust person                    |
| `islands.visit`         | Visit island                      |

Bypasses

| Permission                   | Affect                                                                   |
|------------------------------|--------------------------------------------------------------------------|
| `islands.bypass.islandLimit` | Ignore island create limit                                               |
| `islands.bypass.regenerate`  | Regenerate anyone's island                                               |
| `islands.bypass.delete`      | Delete anyone's island                                                   |
| `islands.bypass.give`        | Transfer any island's ownership / remove owner if no arguments provided  |
| `islands.bypass.name`        | Name anyone's island                                                     |
| `islands.bypass.unname`      | Unname anyone's island                                                   |
| `islands.bypass.trust`       | Add trusted person to anyone's island                                    |
| `islands.bypass.trust.list`  | List any island's trusted players                                        |
| `islands.bypass.untrust`     | Remove trusted person from anyone's island                               |
| `islands.bypass.protection`  | Interact with anyone's island                                            |
| `islands.bypass.home`        | Use /home from anywhere                                                  |

