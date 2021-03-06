package ru.craftlogic.common.entity.projectile;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class EntitySpiderSpit extends Entity implements IProjectile {
    public EntitySpider owner;
    private NBTTagCompound ownerNbt;

    public EntitySpiderSpit(World world) {
        super(world);
    }

    public EntitySpiderSpit(World world, EntitySpider owner) {
        super(world);
        this.owner = owner;
        this.setPosition(owner.posX - (double)(owner.width + 1F) * 0.5D * (double)MathHelper.sin(owner.renderYawOffset * 0.017453292F), owner.posY + (double)owner.getEyeHeight() - 0.10000000149011612D, owner.posZ + (double)(owner.width + 1F) * 0.5D * (double)MathHelper.cos(owner.renderYawOffset * 0.017453292F));
        this.setSize(0.25F, 0.25F);
    }

    @SideOnly(Side.CLIENT)
    public EntitySpiderSpit(World world, double x, double y, double z, double mx, double my, double mz) {
        super(world);
        this.setPosition(x, y, z);

        for(int i = 0; i < 7; ++i) {
            double d0 = 0.4D + 0.1D * (double)i;
            world.spawnParticle(EnumParticleTypes.SPIT, x, y, z, mx * d0, my, mz * d0);
        }

        this.motionX = mx;
        this.motionY = my;
        this.motionZ = mz;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (this.ownerNbt != null) {
            this.restoreOwnerFromSave();
        }

        Vec3d start = new Vec3d(this.posX, this.posY, this.posZ);
        Vec3d end = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
        RayTraceResult target = this.world.rayTraceBlocks(start, end);
        start = new Vec3d(this.posX, this.posY, this.posZ);
        end = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
        if (target != null) {
            end = new Vec3d(target.hitVec.x, target.hitVec.y, target.hitVec.z);
        }

        Entity entity = this.getHitEntity(start, end);
        if (entity != null) {
            target = new RayTraceResult(entity);
        }

        if (target != null && !ForgeEventFactory.onProjectileImpact(this, target)) {
            this.onHit(target);
        }

        this.posX += this.motionX;
        this.posY += this.motionY;
        this.posZ += this.motionZ;
        float f = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
        this.rotationYaw = (float)(MathHelper.atan2(this.motionX, this.motionZ) * 57.29577951308232D);

        this.rotationPitch = (float)(MathHelper.atan2(this.motionY, (double)f) * 57.29577951308232D);

        while (this.rotationPitch - this.prevRotationPitch < -180F) {
            this.prevRotationPitch -= 360F;
        }

        while(this.rotationPitch - this.prevRotationPitch >= 180F) {
            this.prevRotationPitch += 360F;
        }

        while(this.rotationYaw - this.prevRotationYaw < -180F) {
            this.prevRotationYaw -= 360F;
        }

        while(this.rotationYaw - this.prevRotationYaw >= 180F) {
            this.prevRotationYaw += 360F;
        }

        this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
        this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;

        if (!this.world.isMaterialInBB(this.getEntityBoundingBox(), Material.AIR)) {
            this.setDead();
        } else if (this.isInWater()) {
            this.setDead();
        } else {
            this.motionX *= 0.9900000095367432D;
            this.motionY *= 0.9900000095367432D;
            this.motionZ *= 0.9900000095367432D;
            if (!this.hasNoGravity()) {
                this.motionY -= 0.05999999865889549D;
            }

            this.setPosition(this.posX, this.posY, this.posZ);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void setVelocity(double mx, double my, double mz) {
        this.motionX = mx;
        this.motionY = my;
        this.motionZ = mz;
        if (this.prevRotationPitch == 0F && this.prevRotationYaw == 0F) {
            float f = MathHelper.sqrt(mx * mx + mz * mz);
            this.rotationPitch = (float)(Math.toDegrees(MathHelper.atan2(my, (double)f)));
            this.rotationYaw = (float)(Math.toDegrees(MathHelper.atan2(mx, mz)));
            this.prevRotationPitch = this.rotationPitch;
            this.prevRotationYaw = this.rotationYaw;
            this.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
        }
    }

    @Nullable
    private Entity getHitEntity(Vec3d start, Vec3d end) {
        List<Entity> entities = this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().expand(this.motionX, this.motionY, this.motionZ).grow(1.0D));

        double distance = Double.MAX_VALUE;
        Entity nearestEntity = null;

        for (Entity e : entities) {
            if (e != this.owner) {
                AxisAlignedBB box = e.getEntityBoundingBox().grow(0.30000001192092896D);
                RayTraceResult target = box.calculateIntercept(start, end);
                if (target != null) {
                    double d = start.squareDistanceTo(target.hitVec);
                    if (d < distance) {
                        distance = d;
                        nearestEntity = e;
                    }
                }
            }
        }

        return nearestEntity;
    }

    @Override
    public void shoot(double targetX, double targetY, double targetZ, float p_shoot_7_, float p_shoot_8_) {
        float f = MathHelper.sqrt(targetX * targetX + targetY * targetY + targetZ * targetZ);
        targetX /= (double)f;
        targetY /= (double)f;
        targetZ /= (double)f;
        targetX += this.rand.nextGaussian() * 0.007499999832361937D * (double)p_shoot_8_;
        targetY += this.rand.nextGaussian() * 0.007499999832361937D * (double)p_shoot_8_;
        targetZ += this.rand.nextGaussian() * 0.007499999832361937D * (double)p_shoot_8_;
        targetX *= (double)p_shoot_7_;
        targetY *= (double)p_shoot_7_;
        targetZ *= (double)p_shoot_7_;
        this.motionX = targetX;
        this.motionY = targetY;
        this.motionZ = targetZ;
        float f1 = MathHelper.sqrt(targetX * targetX + targetZ * targetZ);
        this.rotationYaw = (float)(Math.toDegrees(MathHelper.atan2(targetX, targetZ)));
        this.rotationPitch = (float)(Math.toDegrees(MathHelper.atan2(targetY, (double)f1)));
        this.prevRotationYaw = this.rotationYaw;
        this.prevRotationPitch = this.rotationPitch;
    }

    public void onHit(RayTraceResult target) {
        Entity victim = target.entityHit;
        if (victim != null && this.owner != null) {
            victim.attackEntityFrom(DamageSource.causeIndirectDamage(this, this.owner).setProjectile(), 1F);
            if (this.rand.nextInt(3) == 0) {
                BlockPos pos = target.entityHit.getPosition();
                if (this.world.isAirBlock(pos)) {
                    this.world.setBlockState(pos, Blocks.WEB.getDefaultState());
                }
            }
        }

        if (!this.world.isRemote) {
            this.setDead();
        }
    }

    @Override
    protected void entityInit() {}

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        if (compound.hasKey("Owner", 10)) {
            this.ownerNbt = compound.getCompoundTag("Owner");
        }
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        if (this.owner != null) {
            NBTTagCompound owner = new NBTTagCompound();
            UUID uuid = this.owner.getUniqueID();
            owner.setUniqueId("OwnerUUID", uuid);
            compound.setTag("Owner", owner);
        }
    }

    private void restoreOwnerFromSave() {
        if (this.ownerNbt != null && this.ownerNbt.hasUniqueId("OwnerUUID")) {
            UUID uuid = this.ownerNbt.getUniqueId("OwnerUUID");

            for (EntitySpider spider : this.world.getEntitiesWithinAABB(EntitySpider.class, this.getEntityBoundingBox().grow(15D))) {
                if (spider.getUniqueID().equals(uuid)) {
                    this.owner = spider;
                    break;
                }
            }
        }

        this.ownerNbt = null;
    }
}
