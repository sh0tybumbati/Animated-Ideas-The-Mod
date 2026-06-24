package com.garrett.mod;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "gtcai")
public class GarrettModConfig implements ConfigData {
    @ConfigEntry.Gui.Tooltip
    public boolean enableParrotArmorStands = true;

    @ConfigEntry.Gui.Tooltip
    public boolean enableGunpowderExplosions = true;

    @ConfigEntry.Gui.Tooltip
    public boolean enablePlaceableGunpowder = true;

    @ConfigEntry.Gui.Tooltip
    public boolean enableRepairableAnvils = true;

    @ConfigEntry.Gui.Tooltip
    public boolean enablePlaceableMilk = true;

    @ConfigEntry.Gui.Tooltip
    public boolean enableSandwiches = true;

    @ConfigEntry.Gui.Tooltip
    public boolean enableCoopMining = true;

    @ConfigEntry.Gui.Tooltip
    public boolean enableCanvases = true;

    @ConfigEntry.Gui.Tooltip
    public boolean enableDangerousStonecutters = true;

    @ConfigEntry.Gui.Tooltip
    public boolean enablePlaceablePumpkinPie = true;

    @ConfigEntry.Gui.Tooltip
    public boolean enableMilkSplashPotion = true;

    @ConfigEntry.Gui.Tooltip
    public boolean enableTridentHitboxes = true;

    @ConfigEntry.Gui.Tooltip
    public boolean enableAirFryer = true;

    @ConfigEntry.Gui.Tooltip
    public boolean enableCustomPumpkinCarving = true;

    @ConfigEntry.Gui.Tooltip
    public boolean enableDoodleBooks = true;

    @ConfigEntry.Gui.Tooltip
    public boolean enableCheese = true;

    @ConfigEntry.Gui.Tooltip
    public boolean enableCookingEggs = true;

    @ConfigEntry.Gui.Tooltip
    public boolean enableSleepingBags = true;

    @ConfigEntry.Gui.Tooltip
    public boolean enableTrumpets = true;

    @ConfigEntry.Gui.Tooltip
    public boolean enableFryingPan = true;
}
