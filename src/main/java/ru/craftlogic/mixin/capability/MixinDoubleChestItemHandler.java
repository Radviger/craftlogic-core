package ru.craftlogic.mixin.capability;

import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.VanillaDoubleChestItemHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import ru.craftlogic.api.world.TileEntities;
import ru.craftlogic.common.block.ChestPart;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;

import static ru.craftlogic.common.block.ChestProperties.PART;

@Mixin(VanillaDoubleChestItemHandler.class)
public abstract class MixinDoubleChestItemHandler extends WeakReference<TileEntityChest> implements IItemHandlerModifiable {
    @Shadow(remap = false) @Final
    public static VanillaDoubleChestItemHandler NO_ADJACENT_CHESTS_INSTANCE;

    public MixinDoubleChestItemHandler(TileEntityChest referent) {
        super(referent);
    }

    /**
     * @author Radviger
     * @reason Separable chests
     */
    @Overwrite(remap = false)
    @Nullable
    public static VanillaDoubleChestItemHandler get(TileEntityChest chest) {
        World world = chest.getWorld();
        BlockPos pos = chest.getPos();
        if (world != null && pos != null && world.isBlockLoaded(pos)) {
            IBlockState state = world.getBlockState(pos);
            if (state.getPropertyKeys().contains(PART)) {
                ChestPart part = state.getValue(PART);
                if (part != ChestPart.SINGLE) {
                    Block blockType = chest.getBlockType();
                    BlockPos offsetPos = pos.offset(part.rotate(state.getValue(BlockChest.FACING)));
                    IBlockState offsetState = world.getBlockState(offsetPos);
                    if (offsetState.getBlock() == blockType && offsetState.getValue(PART) == part) {
                        TileEntityChest otherChest = TileEntities.getTileEntity(world, offsetPos, TileEntityChest.class);
                        if (otherChest != null) {
                            return new VanillaDoubleChestItemHandler(chest, otherChest, part == ChestPart.RIGHT);
                        }
                    }
                }
            } else {
                Block blockType = chest.getBlockType();
                EnumFacing[] horizontals = EnumFacing.HORIZONTALS;

                for(int i = horizontals.length - 1; i >= 0; --i) {
                    EnumFacing side = horizontals[i];
                    BlockPos p = pos.offset(side);
                    Block block = world.getBlockState(p).getBlock();
                    if (block == blockType) {
                        TileEntity otherTE = world.getTileEntity(p);
                        if (otherTE instanceof TileEntityChest) {
                            TileEntityChest otherChest = (TileEntityChest)otherTE;
                            return new VanillaDoubleChestItemHandler(chest, otherChest, side != EnumFacing.WEST && side != EnumFacing.NORTH);
                        }
                    }
                }
            }

            return NO_ADJACENT_CHESTS_INSTANCE;
        } else {
            return null;
        }
    }
}
