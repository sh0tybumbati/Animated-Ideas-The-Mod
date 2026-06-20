# GTC's Animated Ideas

A Minecraft Fabric mod (1.21) implementing ideas from [GarrettTheCarrot](https://www.youtube.com/@GarrettTheCarrot)'s *"Animating your Minecraft Ideas"* YouTube series.

Released episode by episode. See **[MODPAGE.md](MODPAGE.md)** for the full feature list and roadmap.

## Features

**Part 1**
- **Sandwiches** — 2 bread + (almost) any food item makes its sandwich. Hunger/saturation stack on top of the filling, and effect-bearing fillings carry their effect (spider eye poisons you, golden apple gives regen + absorption, chorus teleports, honey clears poison, …).
- **Air Fryer** — a food-only cooker: 2× faster than a smoker, but burns fuel twice as fast.
- **Paintable Canvases** — wall-mounted 16×16 canvases in all 16 dye colors plus a transparent variant. Paint with a brush (main hand) + dye (offhand), sample colors by pick-blocking, seal with honeycomb.
- **Placeable Gunpowder** — place gunpowder as ground fuses (rendered like redstone wire) and light it with flint and steel.
- **Placeable Milk** — pour a milk bucket into the world as a flowing fluid that clears effects on contact.
- **Repairable Anvils** — repair a chipped/damaged anvil one stage with an iron ingot, or fully with an iron block.
- **Co-op Mining** — blocks break faster when multiple players mine them together.

**Part 2**
- **Placeable Pumpkin Pie** — place it like a cake and eat it slice by slice.
- **Splash Potion of Milk** — throwable potion that clears status effects in an area.
- **Dangerous Stonecutters** — hurt entities that stand on them.
- **Physical Trident Hitboxes** — tridents stuck in a *wall* become thin, standable pegs.

**Standalone**
- **Parrots on Armor Stands** — perch a shoulder parrot on an armor stand.
- **Gunpowder in Furnaces** — smelting gunpowder explodes.
- **Logical Stairs & Trapdoors** — recipes reflect real material: 3 blocks make 4 stairs; 6 planks make 24 wooden trapdoors.

## Recipes

| Item | Recipe |
|------|--------|
| Colored canvas | Carpet (center) + 4 sticks in a cross |
| Transparent canvas | Glass pane + 4 sticks in a cross |
| Air Fryer | Furnace (center) ringed by iron ingots, redstone beneath |
| Sandwich (any) | 2× Bread + any supported food item (shapeless) |
| Stairs | 3 matching blocks in a corner → 4 stairs |
| Wooden trapdoor | 6 planks → 24 trapdoors |

## Configuration

Most features can be toggled individually via the in-game config screen (requires [Mod Menu](https://modrinth.com/mod/modmenu) + [Cloth Config](https://modrinth.com/mod/cloth-config)). The Logical Stairs & Trapdoors changes are recipe overrides and are always on.

## Requirements

- Minecraft 1.21
- [Fabric Loader](https://fabricmc.net/) ≥ 0.15.11
- [Fabric API](https://modrinth.com/mod/fabric-api)
- [Cloth Config](https://modrinth.com/mod/cloth-config)
- [Mod Menu](https://modrinth.com/mod/modmenu)

## License

MIT
