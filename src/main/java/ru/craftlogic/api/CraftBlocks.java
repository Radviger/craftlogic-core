package ru.craftlogic.api;

import com.google.common.base.Optional;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import ru.craftlogic.CraftConfig;
import ru.craftlogic.api.block.BlockBase;
import ru.craftlogic.api.item.ItemBlockBase;
import ru.craftlogic.common.block.*;
import ru.craftlogic.common.item.ItemRock;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static ru.craftlogic.api.CraftItems.registerItem;

public class CraftBlocks {
    public static Block BARREL_WOOD, BARREL_STONE;
    public static Block DRYING_RACK;
    public static BlockGourd MELON, PUMPKIN;
    public static Block BEE_HIVE;
    public static BiMap<BlockPlanks.EnumType, Block> BEE_HOUSE = HashBiMap.create(BlockPlanks.EnumType.values().length);
    public static Block MUSHROOM_GRASS;
    public static Block ROCK;

    static void init(Side side) {
        if (CraftConfig.blocks.enableBarrels) {
            BARREL_WOOD = registerBlockWithItem(new BlockBarrelWood());
            BARREL_STONE = registerBlockWithItem(new BlockBarrelStone());
        }

        DRYING_RACK = registerBlockWithItem(new BlockDryingRack());

        if (CraftConfig.tweaks.enableFancyGourd) {
            PUMPKIN = registerBlock(new BlockGourd(BlockGourd.GourdVariant.PUMPKIN));
            MELON = registerBlock(new BlockGourd(BlockGourd.GourdVariant.MELON));
        }

        BEE_HIVE = registerBlockWithItem(new BlockBeeHive());

        for (BlockPlanks.EnumType plankType : BlockPlanks.EnumType.values()) {
            BEE_HOUSE.put(plankType, registerBlockWithItem(new BlockBeeHouse(plankType)));
        }

        MUSHROOM_GRASS = registerBlock(new BlockMushroomGrass());

        if (CraftConfig.items.enableRocks) {
            ROCK = registerBlockWithItem(new BlockRock(), ItemRock::new);
        }
    }

    public static <B extends Block> B registerBlock(@Nonnull B block) {
        GameRegistry.findRegistry(Block.class).register(block);
        return block;
    }

    public static <B extends BlockBase> B registerBlockWithItem(@Nonnull B block) {
        return registerBlockWithItem(block, ItemBlockBase::new);
    }

    public static <B extends Block> B registerBlockWithItem(@Nonnull B block, Function<B, Item> itemBlockMaker) {
        B result = registerBlock(block);
        Item itemBlock = itemBlockMaker.apply(block).setRegistryName(block.getRegistryName());
        registerItem(itemBlock);
        return result;
    }

    public static IBlockState parseBlockState(String string) {
        int firstBracket = string.indexOf('[');
        boolean hasBracket = firstBracket >= 0;
        String id = hasBracket ? string.substring(0, firstBracket) : string;
        Block block = Block.REGISTRY.getObject(new ResourceLocation(id));
        IBlockState state = block.getDefaultState();
        if (hasBracket) {
            String args = string.substring(firstBracket + 1, string.length() - 1);
            Map<String, IProperty<?>> properties = new HashMap<>();
            for (IProperty<?> property : state.getPropertyKeys()) {
                properties.put(property.getName(), property);
            }
            if (!args.trim().isEmpty()) {
                for (String kv : args.split(",")) {
                    String[] parts = kv.split("=");
                    if (parts.length == 2) {
                        String k = parts[0];
                        String v = parts[1];
                        if (properties.containsKey(k)) {
                            IProperty property = properties.get(k);
                            Optional<?> value = property.parseValue(v);
                            if (value.isPresent()) {
                                state = state.withProperty(property, (Comparable)value.get());
                            } else {
                                throw new IllegalArgumentException("Invalid property value: " + k + "=" + v + " for block " + id);
                            }
                        } else {
                            throw new IllegalArgumentException("Invalid property " + k + " for block " + id);
                        }
                    } else {
                        throw new IllegalArgumentException("Invalid property=value pair: " + Arrays.toString(parts) + " for block " + id);
                    }
                }
            }
        }
        return state;
    }
}
