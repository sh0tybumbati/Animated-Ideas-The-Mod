#!/usr/bin/env python3
"""
Generate sandwich assets (recipes, item models, lang entries, tinted textures)
for GTC's Animated Ideas.

Source of truth for sandwich *stats/effects* is Sandwiches.java; this script owns
the static *assets*. The two must agree on the sandwich names.

Textures are drawn procedurally: a simple bun + a filling band tinted to each
filling's colour. Replace the drawing in `make_texture` (or swap in a real base
image + mask) when proper art is ready, then re-run to regenerate every variant.

Existing hand-made assets are never overwritten: any recipe/model/texture file
that already exists is left untouched (this preserves the original 7 sandwiches).
"""

import json
import os
from PIL import Image

ROOT = os.path.normpath(os.path.join(os.path.dirname(__file__), ".."))
ASSETS = os.path.join(ROOT, "src/main/resources/assets/gtcai")
DATA = os.path.join(ROOT, "src/main/resources/data/gtcai")
LANG = os.path.join(ASSETS, "lang/en_us.json")

# name -> (filling item id, tint colour, display-name override or None)
SANDWICHES = {
    # cooked meats (existing art preserved)
    "beef":    ("cooked_beef", (139, 75, 47), None),
    "pork":    ("cooked_porkchop", (217, 140, 140), None),
    "chicken": ("cooked_chicken", (217, 191, 140), None),
    "mutton":  ("cooked_mutton", (181, 97, 63), None),
    "rabbit":  ("cooked_rabbit", (200, 155, 106), None),
    "cod":     ("cooked_cod", (199, 184, 154), None),
    "salmon":  ("cooked_salmon", (224, 138, 90), None),
    # raw meats & fish
    "raw_beef":      ("beef", (200, 90, 90), None),
    "raw_pork":      ("porkchop", (230, 150, 150), None),
    "raw_chicken":   ("chicken", (235, 200, 150), None),
    "raw_mutton":    ("mutton", (210, 120, 110), None),
    "raw_rabbit":    ("rabbit", (215, 160, 120), None),
    "raw_cod":       ("cod", (200, 190, 160), None),
    "raw_salmon":    ("salmon", (235, 140, 90), None),
    "tropical_fish": ("tropical_fish", (240, 140, 60), None),
    "pufferfish":    ("pufferfish", (235, 210, 80), None),
    # vegetables
    "carrot":           ("carrot", (237, 142, 43), None),
    "golden_carrot":    ("golden_carrot", (255, 210, 74), None),
    "potato":           ("potato", (201, 162, 107), None),
    "baked_potato":     ("baked_potato", (176, 125, 60), None),
    "poisonous_potato": ("poisonous_potato", (120, 150, 70), None),
    "beetroot":         ("beetroot", (140, 30, 60), None),
    # fruits
    "apple":                  ("apple", (216, 53, 42), None),
    "golden_apple":           ("golden_apple", (255, 216, 74), None),
    "enchanted_golden_apple": ("enchanted_golden_apple", (230, 120, 255), None),
    "melon_slice":            ("melon_slice", (76, 175, 80), "Melon Sandwich"),
    "sweet_berries":          ("sweet_berries", (179, 32, 58), "Sweet Berry Sandwich"),
    "glow_berries":           ("glow_berries", (240, 169, 59), "Glow Berry Sandwich"),
    "chorus":                 ("chorus_fruit", (130, 80, 140), "Chorus Fruit Sandwich"),
    # other foods
    "bread":        ("bread", (200, 150, 75), None),
    "cookie":       ("cookie", (169, 105, 46), None),
    "dried_kelp":   ("dried_kelp", (59, 90, 43), None),
    "pumpkin_pie":  ("pumpkin_pie", (217, 139, 43), None),
    "rotten_flesh": ("rotten_flesh", (111, 90, 58), None),
    "spider_eye":   ("spider_eye", (90, 70, 70), None),
    "honey":        ("honey_bottle", (240, 169, 59), "Honey Sandwich"),
}

BUN_TOP = (226, 182, 104, 255)
BUN_BOTTOM = (201, 156, 84, 255)
OUTLINE = (94, 62, 33, 255)
CLEAR = (0, 0, 0, 0)


def display_name(name, override):
    if override:
        return override
    return " ".join(w.capitalize() for w in name.split("_")) + " Sandwich"


def make_texture(path, tint):
    """A 16x16 sandwich: top bun, tinted filling band, bottom bun, with an outline."""
    img = Image.new("RGBA", (16, 16), CLEAR)
    px = img.load()
    x0, x1 = 2, 13          # inclusive horizontal span
    y0, y1 = 3, 12          # inclusive vertical span
    fill_color = (tint[0], tint[1], tint[2], 255)
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            border = x in (x0, x1) or y in (y0, y1)
            if border:
                px[x, y] = OUTLINE
            elif y <= 5:
                px[x, y] = BUN_TOP
            elif y <= 9:
                px[x, y] = fill_color
            else:
                px[x, y] = BUN_BOTTOM
    img.save(path)


def write_if_absent(path, content):
    if os.path.exists(path):
        return False
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        f.write(content)
    return True


def main():
    recipes_made = models_made = textures_made = 0
    lang = json.load(open(LANG))

    for name, (filling, tint, override) in SANDWICHES.items():
        sid = f"{name}_sandwich"
        rid = f"gtcai:{sid}"

        recipe = {
            "fabric:load_conditions": [
                {"condition": "fabric:registry_contains",
                 "registry": "minecraft:item", "values": [rid]}
            ],
            "type": "minecraft:crafting_shapeless",
            "ingredients": [
                {"item": "minecraft:bread"},
                {"item": "minecraft:bread"},
                {"item": f"minecraft:{filling}"},
            ],
            "result": {"id": rid, "count": 1},
        }
        if write_if_absent(os.path.join(DATA, "recipe", f"{sid}.json"),
                           json.dumps(recipe, indent=2) + "\n"):
            recipes_made += 1

        model = {"parent": "minecraft:item/generated",
                 "textures": {"layer0": f"gtcai:item/{sid}"}}
        if write_if_absent(os.path.join(ASSETS, "models/item", f"{sid}.json"),
                           json.dumps(model) + "\n"):
            models_made += 1

        tex_path = os.path.join(ASSETS, "textures/item", f"{sid}.png")
        if not os.path.exists(tex_path):
            os.makedirs(os.path.dirname(tex_path), exist_ok=True)
            make_texture(tex_path, tint)
            textures_made += 1

        lang.setdefault(f"item.gtcai.{sid}", display_name(name, override))

    with open(LANG, "w") as f:
        json.dump(lang, f, indent=2, ensure_ascii=False)
        f.write("\n")

    print(f"recipes: +{recipes_made}, models: +{models_made}, "
          f"textures: +{textures_made}, total sandwiches: {len(SANDWICHES)}")


if __name__ == "__main__":
    main()
