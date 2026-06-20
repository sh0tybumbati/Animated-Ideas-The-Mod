package com.garrett.mod;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Defines every sandwich variant. A sandwich is 2 bread + one filling:
 *   nutrition  = 10 (two bread) + filling nutrition
 *   saturation = nutrition-weighted blend of the ingredients' saturation modifiers
 *   effects    = inherited from the filling
 *
 * This class is the runtime source of truth for sandwich stats/effects.
 * The matching recipes, lang entries, item models and textures are produced by
 * tools/gen_sandwiches.py, which must stay in sync on the sandwich names below.
 */
public final class Sandwiches {
    private Sandwiches() {}

    private static final int BREAD_NUTRITION = 10;       // 2 bread
    private static final float BREAD_SAT_SUM = 6.0f;     // 2 * (5 nutrition * 0.6 modifier)

    /** A status effect applied on eating, with a probability in [0,1]. */
    public record EffectSpec(Holder<MobEffect> effect, int seconds, int amplifier, float probability) {
        MobEffectInstance build() { return new MobEffectInstance(effect, seconds * 20, amplifier); }
    }

    /**
     * @param name     sandwich id stem ({@code name + "_sandwich"})
     * @param nutrition the filling's own nutrition
     * @param satMod    the filling's own saturation modifier
     * @param effects   effects inherited from the filling
     * @param factory   custom Item factory for special behaviour, or null for a plain food Item
     */
    public record Filling(String name, int nutrition, float satMod,
                          List<EffectSpec> effects, Function<Item.Properties, Item> factory) {}

    public static final List<Filling> FILLINGS = new ArrayList<>();
    public static final Map<String, Item> ITEMS = new LinkedHashMap<>();

    private static void add(String name, int nutrition, float satMod, EffectSpec... effects) {
        FILLINGS.add(new Filling(name, nutrition, satMod, List.of(effects), null));
    }

    private static void add(String name, int nutrition, float satMod,
                            Function<Item.Properties, Item> factory, EffectSpec... effects) {
        FILLINGS.add(new Filling(name, nutrition, satMod, List.of(effects), factory));
    }

    private static EffectSpec eff(Holder<MobEffect> e, int seconds, int amplifier, float probability) {
        return new EffectSpec(e, seconds, amplifier, probability);
    }

    static {
        // --- Cooked meats (the original seven) ---
        add("beef",    8, 0.8f);
        add("pork",    8, 0.8f);
        add("chicken", 6, 0.6f);
        add("mutton",  6, 0.8f);
        add("rabbit",  5, 0.6f);
        add("cod",     5, 0.6f);
        add("salmon",  6, 0.8f);

        // --- Raw meats & fish ---
        add("raw_beef",      3, 0.3f);
        add("raw_pork",      3, 0.3f);
        add("raw_chicken",   2, 0.3f, eff(MobEffects.HUNGER, 30, 0, 0.3f));
        add("raw_mutton",    2, 0.3f);
        add("raw_rabbit",    3, 0.3f);
        add("raw_cod",       2, 0.1f);
        add("raw_salmon",    2, 0.1f);
        add("tropical_fish", 1, 0.1f);
        add("pufferfish",    1, 0.1f,
            eff(MobEffects.POISON,   60, 3, 1.0f),
            eff(MobEffects.HUNGER,   15, 2, 1.0f),
            eff(MobEffects.CONFUSION, 15, 0, 1.0f));

        // --- Vegetables ---
        add("carrot",           3, 0.6f);
        add("golden_carrot",    6, 1.2f);
        add("potato",           1, 0.3f);
        add("baked_potato",     5, 0.6f);
        add("poisonous_potato", 2, 0.3f, eff(MobEffects.POISON, 5, 0, 0.6f));
        add("beetroot",         1, 0.6f);

        // --- Fruits ---
        add("apple", 4, 0.3f);
        add("golden_apple", 4, 1.2f,
            eff(MobEffects.REGENERATION, 5, 1, 1.0f),
            eff(MobEffects.ABSORPTION, 120, 0, 1.0f));
        add("enchanted_golden_apple", 4, 1.2f,
            eff(MobEffects.REGENERATION, 30, 1, 1.0f),
            eff(MobEffects.ABSORPTION, 120, 3, 1.0f),
            eff(MobEffects.DAMAGE_RESISTANCE, 300, 0, 1.0f),
            eff(MobEffects.FIRE_RESISTANCE, 300, 0, 1.0f));
        add("melon_slice",  2, 0.3f);
        add("sweet_berries", 2, 0.1f);
        add("glow_berries",  2, 0.1f);
        add("chorus", 4, 0.3f, ChorusSandwichItem::new); // teleports the eater

        // --- Other foods ---
        add("bread",       5, 0.6f);
        add("cookie",      2, 0.1f);
        add("dried_kelp",  1, 0.6f);
        add("pumpkin_pie", 8, 0.3f);
        add("rotten_flesh", 4, 0.1f, eff(MobEffects.HUNGER, 30, 0, 0.8f));
        add("spider_eye",   2, 0.8f, eff(MobEffects.POISON, 5, 0, 1.0f));
        add("honey", 6, 0.1f, HoneySandwichItem::new); // clears poison
    }

    /** Builds and registers every sandwich item. Call only when sandwiches are enabled. */
    public static void registerAll() {
        for (Filling f : FILLINGS) {
            int nutrition = BREAD_NUTRITION + f.nutrition();
            float satMod = (BREAD_SAT_SUM + f.nutrition() * f.satMod()) / nutrition;

            FoodProperties.Builder food = new FoodProperties.Builder()
                .nutrition(nutrition).saturationModifier(satMod);
            for (EffectSpec e : f.effects()) {
                food.effect(e.build(), e.probability());
            }

            Item.Properties props = new Item.Properties().food(food.build());
            Item item = f.factory() != null ? f.factory().apply(props) : new Item(props);
            ITEMS.put(f.name() + "_sandwich", item);
        }
        for (Map.Entry<String, Item> e : ITEMS.entrySet()) {
            Registry.register(BuiltInRegistries.ITEM,
                ResourceLocation.fromNamespaceAndPath(GarrettMod.MOD_ID, e.getKey()), e.getValue());
        }
    }
}
