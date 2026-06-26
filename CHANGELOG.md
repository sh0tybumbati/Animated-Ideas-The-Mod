# Changelog

## v1.4.3
- **Banner patterns on sleeping bags** — right-click a placed sleeping bag with a banner to stamp its design onto the bedroll (consumes the banner, kept in creative). Uses real banner-pattern compositing across the whole 2-block bag, cropped so the head pillow still shows.
- Breaking a patterned bag **preserves the design** on the dropped item (and restores it on placement), like a banner.
- Sleeping bag hitbox is now a flat slab matching the model instead of the tall vanilla bed shape.

## v1.4.2
- **Trumpets** now play real trumpet notes (C/E/G for low/mid/high) instead of placeholder note-block sounds, sourced from the University of Iowa Electronic Music Studios sample library (free, unrestricted use).
- Updated the trumpet item texture.

## v1.4.1
- **Sleeping Bags** now come in all **16 dye colors** (like beds), each crafted from 3 matching wool + 3 string. They use the vanilla bed top textures per color — head half with the pillow, foot half plain — so a placed bag reads as a bed laid flat.

## v1.4.0
**Part 3 begins**

**Cheese**
- Right-click an empty cauldron with a **milk bucket** to fill it; after ~10s the milk curdles into a **Cheese Block** that pops out above.
- Uncraft a Cheese Block into **9 edible cheese slices** (and craft 9 back into a block).

**Cooking Eggs**
- Cook a vanilla **egg** in a furnace, smoker, air fryer, or campfire into an edible **Fried Egg**.

**Sleeping Bags**
- A bed-like block crafted from **3 wool + 3 string** that you can sleep in to pass the night **without resetting your spawn point**.

**Trumpets**
- A brass **Trumpet** crafted from gold that plays low/mid/high notes based on where you're looking (placeholder note sounds for now).

**Frying Pan**
- A two-and-two… er, **four-slot** campfire-style cooker that **floats on top of a lit campfire** and cooks at **2× campfire speed**; finished food pops off above.
- Uses the vanilla **campfire-cooking** recipe set; cooks only while the campfire below is lit, and pops off (dropping its food) if that campfire is removed.
- Raw eggs render as a little **3D sunny-side-up egg** while frying — both on the pan and on campfires.
- Also wieldable as a **melee weapon**: a light, slow, heavy bonk with a metallic clang and extra knockback, held sword-style.

## v1.3.5
- **Splash Potion of Milk** now uses the vanilla splash-potion model (glass bottle + liquid) tinted **white**, as both the item and the thrown projectile.

## v1.3.4
- Parrots perch on the **side matching the shoulder** they came from (persisted per-parrot).
- Doodle book's text "Clear" button replaced with the cream **Clear icon**.

## v1.3.3
- Perched parrots now **ride the armor stand** (no longer fall off) and sit on a **shoulder**, with the offset tracking the stand's facing.
- **One parrot per stand** — placing another swaps the existing one back onto your shoulder.
- **Click a perched parrot** to take it back onto your shoulder.

## v1.3.2
- Doodle tool icons **recolored to warm cream** for contrast on the dark buttons.
- Tool buttons moved to a **top toolbar above the canvas** (MS-Paint style), with Clear at the right end.

## v1.3.1
- Replaced the doodle tool-button letters with **brush / fill / eyedropper / line** icons.

## v1.3.0
**Doodle Books (finished)**
- Right-click opens a **64×64 multi-page sketchbook** (up to 16 pages), rendered as a single uploaded texture.
- Brush, **fill (bucket), eyedropper, and line** tools, plus a 16-color palette, eraser, and clear.
- Page turning (`<` / `>`; adds a new page on the last one); each page saves on turn/close.
- Crafted from **book + feather + dye**.
- Custom dim background so the Blur mod doesn't wash out the canvas.

**Canvases**
- Place on **all six faces** — floors and ceilings, not just walls.
- Attach to **any collidable block** (fences, walls, slabs) for easel setups.
- Painting stays correctly aligned on every face.

**Pumpkin carving**
- Bumped to **16×16** per face (from 8×8).

**Compatibility**
- Minecraft dependency tightened to `>=1.21 <1.21.2` (the mod relies on 1.21.0 APIs that changed in 1.21.2).

## v1.2.0
- **Custom Pumpkin Carving**: carve an 8×8 grid into each of a pumpkin's four faces with a sword; light it with flint &amp; steel for a jack-o'-lantern glow. Breaking drops a carving-preserving item.

## v1.1.0
- **Physical Trident Hitboxes** reworked: only **wall-stuck** tridents are solid, with a thin, end-rod-shaped collision box (parkour-friendly); ground-stuck tridents no longer block others from landing.
- Wooden trapdoor recipes now yield **24**.
- README refreshed to the current feature set.

## v1.0.x — Part 2 sprint
- Fixed the build (migrated to the Minecraft 1.21 Data Components API).
- **Sandwiches** expanded to (almost) every edible food, with **inherited effects** — spider eye poisons, golden apples buff, chorus teleports, honey clears poison, etc.
- **Air Fryer**: a food-only cooker — twice as fast as a smoker but burns fuel twice as fast.
- **Logical Stairs & Trapdoors** reworked as real recipe overrides (stairs 3→4 across all vanilla types; wooden trapdoors boosted).
- **Creative tab** added for all mod items.
- Crash/render fixes: canvas break, trident mixin, thrown milk-potion renderer; canvas model rewrite (no z-fighting); gunpowder now renders like redstone wire (darker tint).
- Made per-feature config toggles actually work; added missing translations.

## v1.0.x — Initial release
**Part 1:** Sandwiches, Paintable Canvases (16 colors + transparent, brush/dye, eyedropper, honeycomb wax), Placeable Gunpowder, Placeable Milk, Repairable Anvils, Co-op Mining, Logical Stairs &amp; Trapdoors.

**Standalone:** Parrots on Armor Stands, Gunpowder explodes in furnaces.

**Part 2 (initial):** Placeable Pumpkin Pie, Splash Potion of Milk, Dangerous Stonecutters, Physical Trident Hitboxes.
