package pokecube.compat.minecraft;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import pokecube.core.PokecubeCore;
import pokecube.core.database.PokedexEntry.InteractionLogic.Interaction;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.PokemobCaps;
import pokecube.core.interfaces.capabilities.impl.PokemobSaves;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.utils.TagNames;
import thut.api.item.ItemList;

public class VanillaPokemob extends PokemobSaves implements ICapabilitySerializable<CompoundNBT>
{
    private final LazyOptional<IPokemob> holder = LazyOptional.of(() -> this);

    public VanillaPokemob()
    {
        for (final AIRoutine routine : AIRoutine.values())
            this.setRoutineState(routine, routine.getDefault());
    }

    public VanillaPokemob(final MobEntity mob)
    {
        this();
        this.setEntity(mob);
    }

    @Override
    public void deserializeNBT(final CompoundNBT nbt)
    {
        try
        {
            this.read(nbt);
        }
        catch (final Exception e)
        {
            PokecubeCore.LOGGER.error("Error Loading Pokemob", e);
        }
    }

    @Override
    public <T> LazyOptional<T> getCapability(final Capability<T> capability, final Direction facing)
    {
        return PokemobCaps.POKEMOB_CAP.orEmpty(capability, this.holder);
    }

    @Override
    public float getHeading()
    {
        if (this.getGeneralState(GeneralStates.CONTROLLED)) return this.dataSync.get(this.params.HEADINGDW);
        return this.getEntity().rotationYaw;
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        CompoundNBT tag;
        try
        {
            tag = this.write();
        }
        catch (final Exception e)
        {
            PokecubeCore.LOGGER.error("Error Saving Pokemob", e);
            tag = new CompoundNBT();
        }
        return tag;
    }

    @Override
    public void setHeading(final float heading)
    {
        if (this.getGeneralState(GeneralStates.CONTROLLED))
        {
            this.getEntity().rotationYaw = heading;
            this.dataSync.set(this.params.HEADINGDW, heading);
        }
    }

    @Override
    public boolean isSheared()
    {
        boolean sheared = this.getGeneralState(GeneralStates.SHEARED);
        if (sheared && this.getEntity().isServerWorld())
        {
            final MinecraftServer server = this.getEntity().getServer();
            final long lastShear = this.getEntity().getPersistentData().getLong(TagNames.SHEARTIME);
            final ItemStack key = new ItemStack(Items.SHEARS);
            if (this.getPokedexEntry().interact(key))
            {
                final Interaction action = this.getPokedexEntry().interactionLogic.getFor(key);
                final int timer = action.cooldown + this.rand.nextInt(1 + action.variance);
                if (lastShear < server.getTickCounter() - timer) sheared = false;
            }
            // Cannot shear this!
            else sheared = false;
            this.setGeneralState(GeneralStates.SHEARED, sheared);
        }
        return sheared;
    }

    @Override
    public void shear(final ItemStack shears)
    {
        if (this.isSheared() || !this.getEntity().isServerWorld()) return;
        final ResourceLocation WOOL = new ResourceLocation("wool");

        final ItemStack key = shears;
        if (this.getPokedexEntry().interact(key))
        {
            final MinecraftServer server = this.getEntity().getServer();
            final ArrayList<ItemStack> ret = new ArrayList<>();
            this.setGeneralState(GeneralStates.SHEARED, true);
            this.getEntity().getPersistentData().putLong(TagNames.SHEARTIME, server.getTickCounter());
            final Interaction action = this.getPokedexEntry().interactionLogic.getFor(key);
            final List<ItemStack> list = action.stacks;
            final int time = this.getHungerTime();
            this.setHungerTime(time + action.hunger);
            for (final ItemStack stack : list)
            {
                ItemStack toAdd = stack.copy();
                if (ItemList.is(WOOL, stack))
                {
                    final DyeColor colour = DyeColor.byId(this.getDyeColour());
                    final Item wool = SheepEntity.WOOL_BY_COLOR.get(colour).asItem();
                    toAdd = new ItemStack(wool, stack.getCount());
                    if (stack.hasTag()) toAdd.setTag(stack.getTag().copy());
                }
                ret.add(toAdd);
            }
            for (final ItemStack stack : ret)
                this.getEntity().entityDropItem(stack);
            this.getEntity().playSound(SoundEvents.ENTITY_SHEEP_SHEAR, 1.0F, 1.0F);
        }

    }

    @Override
    public MobEntity getEntity()
    {
        return this.entity;
    }
}
