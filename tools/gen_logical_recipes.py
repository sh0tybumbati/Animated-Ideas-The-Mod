#!/usr/bin/env python3
"""
Generate "logical crafting" recipe overrides for GTC's Animated Ideas.

Overrides vanilla recipes (placed under data/minecraft/recipe/, which a mod
datapack overrides) so they reflect the real material a block contains:

  * Stairs: 3 blocks (a triangle) -> 4 stairs, instead of vanilla's 6 -> 4.
  * Wooden trapdoors: 6 planks -> 16, instead of vanilla's 6 -> 2.
    (Iron/copper trapdoors are left alone - the thin-wood logic doesn't apply.)

Usage: gen_logical_recipes.py <vanilla_recipe_dir>
where <vanilla_recipe_dir> contains the extracted vanilla *_stairs.json /
*_trapdoor.json files, e.g. unzipped from the Minecraft client jar:
  unzip <client.jar> 'data/minecraft/recipe/*_stairs.json' \\
                     'data/minecraft/recipe/*_trapdoor.json' -d <tmp>
"""

import glob
import json
import os
import sys

ROOT = os.path.normpath(os.path.join(os.path.dirname(__file__), ".."))
OUT = os.path.join(ROOT, "src/main/resources/data/minecraft/recipe")

STAIR_PATTERN_6 = ["#  ", "## ", "###"]
STAIR_PATTERN_3 = ["#  ", "## "]
TRAPDOOR_YIELD = 16


def single_ingredient_item(recipe):
    keys = recipe.get("key", {})
    if len(keys) != 1:
        return None
    return next(iter(keys.values())).get("item")


def main():
    if len(sys.argv) != 2:
        sys.exit("usage: gen_logical_recipes.py <vanilla_recipe_dir>")
    src = sys.argv[1]
    os.makedirs(OUT, exist_ok=True)
    stairs = trapdoors = skipped = 0

    for path in sorted(glob.glob(os.path.join(src, "*_stairs.json"))):
        r = json.load(open(path))
        if r.get("type") != "minecraft:crafting_shaped" or r.get("pattern") != STAIR_PATTERN_6:
            skipped += 1
            continue
        r["pattern"] = STAIR_PATTERN_3        # 3 blocks -> existing count (4)
        json.dump(r, open(os.path.join(OUT, os.path.basename(path)), "w"), indent=2)
        stairs += 1

    for path in sorted(glob.glob(os.path.join(src, "*_trapdoor.json"))):
        r = json.load(open(path))
        item = single_ingredient_item(r)
        if not item or not item.endswith("_planks"):
            skipped += 1
            continue
        r["result"]["count"] = TRAPDOOR_YIELD
        json.dump(r, open(os.path.join(OUT, os.path.basename(path)), "w"), indent=2)
        trapdoors += 1

    print(f"stairs overridden: {stairs}, wooden trapdoors overridden: {trapdoors}, "
          f"skipped (non-matching): {skipped}")


if __name__ == "__main__":
    main()
