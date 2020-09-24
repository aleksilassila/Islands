# Islands
A spigot plugin for creating floating island homes with different biomes.
Optimized for survival gamemode. **This is not a skyblock plugin!**
Jumping down from an island teleports player to survival "wilderness", 
that can be reset often without losing players' progress.

## Features
- Solves griefing and map resetting entirely.
- Create islands with any overworld biome
- Customizable island sizes
- Griefing protection
- Visiting other people's islands
  * Tip for admins: `/island name spawn` for accessing spawn via `/vi spawn`
- Customizable generation settings and speed, optimize for any hardware

### Commands

Island Managment:
- `/island create <biome> (<SMALL / NORMAL / BIG>)`
- `/island regenerate <biome> (<SMALL / NORMAL / BIG>)`
- `/island delete`
- `/island give`, transfers island ownership
- `/island name <name>`, allows other players to `/vi <name>`
- `/island unname`, sets island to private

Other
- `/home (<id>)`
- `/homes`, list all player's islands
- `/visit <name>`, `/vi <name>`, visit public island
- `/trust <name>`, allow player to interact with blocks and entities of your island
- `/untrust <name>`

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

![biome](screenshots/islandTypes/desert_small.png?raw=true)

![biome](screenshots/islandTypes/jungle.png?raw=true)

![biome](screenshots/islandTypes/taiga.png?raw=true)

![biome](screenshots/islandTypes/dark_woods_hills.png?raw=true)

![biome](screenshots/islandTypes/desert_night.png?raw=true)

## Permissions and Config