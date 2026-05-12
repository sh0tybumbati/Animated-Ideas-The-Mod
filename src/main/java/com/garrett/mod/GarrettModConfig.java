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
    public boolean enableLogicalStairs = true;

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
    public boolean enableLogicalTrapdoors = true;

    @ConfigEntry.Gui.Tooltip
    public boolean enableDangerousStonecutters = true;

    @ConfigEntry.Gui.Tooltip
    public boolean enablePlaceablePumpkinPie = true;

    @ConfigEntry.Gui.Tooltip
    public boolean enableMilkSplashPotion = true;

    @ConfigEntry.Gui.Tooltip
    public boolean enableTridentHitboxes = true;
}
