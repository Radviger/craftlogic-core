package ru.craftlogic.api.block;

import net.minecraft.util.EnumFacing;

public interface HeatAcceptor {
    int getTemperature();
    int getMaxTemperature();
    int acceptHeat(EnumFacing side, int amount);
}
