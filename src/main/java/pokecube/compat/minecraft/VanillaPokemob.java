package pokecube.compat.minecraft;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry.InteractionLogic.Interaction;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.api.utils.TagNames;
import pokecube.core.impl.capabilities.impl.PokemobSaves;
import thut.api.Tracker;
import thut.api.item.ItemList;

public class VanillaPokemob extends PokemobSaves implements ICapabilitySerializable<CompoundTag>
{
    private final LazyOptional<IPokemob> holder = LazyOptional.of(() -> this);

    public VanillaPokemob()
    {
        for (final AIRoutine routine : AIRoutine.values())
            this.setRoutineState(routine, routine.getDefault());
    }

    public VanillaPokemob(final Mob mob)
    {
        this();
        this.setEntity(mob);
    }

    @Override
    public void deserializeNBT(final CompoundTag nbt)
    {
        try
        {
            this.read(nbt);
        }
        catch (final Exception e)
        {
            PokecubeAPI.LOGGER.error("Error Loading Pokemob", e);
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
        return this.getEntity().yRot;
    }

    @Override
    public CompoundTag serializeNBT()
    {
        CompoundTag tag;
        try
        {
            tag = this.write();
        }
        catch (final Exception e)
        {
            PokecubeAPI.LOGGER.error("Error Saving Pokemob", e);
            tag = new CompoundTag();
        }
        return tag;
    }

    @Override
    public void setHeading(final float heading)
    {
        if (this.getGeneralState(GeneralStates.CONTROLLED))
        {
            this.getEntity().yRot = heading;
            this.dataSync.set(this.params.HEADINGDW, heading);
        }
    }

    @Override
    public boolean isSheared()
    {
        boolean sheared = this.getGeneralState(GeneralStates.SHEARED);
        if (sheared && this.getEntity().isEffectiveAi())
        {
            final long lastShear = this.getEntity().getPersistentData().getLong(TagNames.SHEARTIME);
            final ItemStack key = new ItemStack(Items.SHEARS);
            if (this.getPokedexEntry().interact(key))
            {
                final Interaction action = this.getPokedexEntry().interactionLogic.getFor(key);
                final int timer = action.cooldown + this.rand.nextInt(1 + action.variance);
                if (lastShear < Tracker.instance().getTick() - timer) sheared = false;
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
        if (this.isSheared() || !this.getEntity().isEffectiveAi()) return;
        final ResourceLocation WOOL = new ResourceLocation("wool");

        final ItemStack key = shears;
        if (this.getPokedexEntry().interact(key))
        {
            final ArrayList<ItemStack> ret = new ArrayList<>();
            this.setGeneralState(GeneralStates.SHEARED, true);
            this.getEntity().getPersistentData().putLong(TagNames.SHEARTIME, Tracker.instance().getTick());
            final Interaction action = this.getPokedexEntry().interactionLogic.getFor(key);
            final List<ItemStack> list = action.stacks;
            this.applyHunger(action.hunger);
            for (final ItemStack stack : list)
            {
                ItemStack toAdd = stack.copy();
                if (ItemList.is(WOOL, stack))
                {
                    final DyeColor colour = DyeColor.byId(this.getDyeColour());
                    final Item wool = Sheep.ITEM_BY_DYE.get(colour).asItem();
                    toAdd = new ItemStack(wool, stack.getCount());
                    if (stack.hasTag()) toAdd.setTag(stack.getTag().copy());
                }
                ret.add(toAdd);
            }
            for (final ItemStack stack : ret)
                this.getEntity().spawnAtLocation(stack);
            this.getEntity().playSound(SoundEvents.SHEEP_SHEAR, 1.0F, 1.0F);
        }

    }

    @Override
    public Mob getEntity()
    {
        return this.entity;
    }
}
