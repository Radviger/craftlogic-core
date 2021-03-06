package ru.craftlogic.mixin.world;

import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import ru.craftlogic.api.world.ChunkGetter;
import ru.craftlogic.api.world.WorldGetter;

@Mixin(ChunkCache.class)
public abstract class MixinChunkCache implements ChunkGetter, WorldGetter {
    @Shadow protected int chunkX, chunkZ;
    @Shadow protected abstract boolean withinBounds(int x, int z);
    @Shadow protected Chunk[][] chunkArray;
    @Shadow protected World world;

    @Override
    public Chunk getChunkAt(int chunkX, int chunkZ) {
        int cx = chunkX - this.chunkX;
        int cz = chunkZ - this.chunkZ;
        return !withinBounds(cx, cz) ? null : chunkArray[cx][cz];
    }

    @Override
    public World getWorld() {
        return world;
    }
}
