# Changelog

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
