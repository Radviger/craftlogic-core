package ru.craftlogic.mixin.client.renderer;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import org.spongepowered.asm.mixin.Mixin;

import static ru.craftlogic.CraftLogic.MODID;

@Mixin(targets = "net/minecraft/client/renderer/BlockModelShapes$3")
public class MixinBlockModelShapes$3 extends StateMapperBase {
    @Override
    protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
        String propString = getPropertyString(state.getProperties());
        return new ModelResourceLocation(MODID + ":gourd_stem", propString);
    }
}
