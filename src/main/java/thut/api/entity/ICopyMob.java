package thut.api.entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.registries.ForgeRegistries;
import pokecube.core.utils.EntityTools;
import thut.api.entity.event.CopyUpdateEvent;

public interface ICopyMob extends INBTSerializable<CompoundNBT>
{
    ResourceLocation getCopiedID();

    LivingEntity getCopiedMob();

    CompoundNBT getCopiedNBT();

    void setCopiedID(ResourceLocation id);

    void setCopiedMob(LivingEntity mob);

    void setCopiedNBT(CompoundNBT tag);

    @Override
    default void deserializeNBT(final CompoundNBT nbt)
    {
        if (nbt.contains("id")) this.setCopiedID(new ResourceLocation(nbt.getString("id")));
        else this.setCopiedID(null);
        this.setCopiedNBT(nbt.getCompound("tag"));
    }

    @Override
    default CompoundNBT serializeNBT()
    {
        final CompoundNBT nbt = new CompoundNBT();
        if (this.getCopiedID() != null) nbt.putString("id", this.getCopiedID().toString());
        if (!this.getCopiedNBT().isEmpty()) nbt.put("tag", this.getCopiedNBT());
        return nbt;
    }

    default void onBaseTick(final @Nonnull World level, final @Nullable LivingEntity holder)
    {
        if (this.getCopiedID() == null)
        {
            if (this.getCopiedMob() != null)
            {
                this.setCopiedMob(null);
                this.setCopiedNBT(new CompoundNBT());
            }
            return;
        }
        if (this.getCopiedMob() == null)
        {
            final EntityType<?> type = ForgeRegistries.ENTITIES.getValue(this.getCopiedID());
            final Entity entity = type.create(level);
            if (entity instanceof LivingEntity)
            {
                this.setCopiedMob((LivingEntity) entity);
                try
                {
                    ((LivingEntity) entity).readAdditionalSaveData(this.getCopiedNBT());
                }
                catch (final Exception e)
                {
                    e.printStackTrace();
                }
            }
            else
            {
                this.setCopiedID(null);
                return;
            }
        }
        final LivingEntity living = this.getCopiedMob();
        if (living != null && holder != null)
        {
            living.setId(-(holder.getId() + 100));

            living.inChunk = true;
            living.baseTick();
            living.inChunk = false;

            final float eye = living.getEyeHeight(holder.getPose());
            if (eye != holder.getEyeHeight(holder.getPose())) holder.refreshDimensions();

            living.setItemInHand(Hand.MAIN_HAND, holder.getItemInHand(Hand.MAIN_HAND));
            living.setItemInHand(Hand.OFF_HAND, holder.getItemInHand(Hand.OFF_HAND));

            living.noPhysics = true;
            EntityTools.copyEntityTransforms(living, holder);
            EntityTools.copyPositions(living, holder);
            living.setLevel(holder.level);

            if (!MinecraftForge.EVENT_BUS.post(new CopyUpdateEvent(living, holder)))
            {
                living.setHealth(holder.getHealth());
                living.setAirSupply(holder.getAirSupply());
            }
        }
    }
}
