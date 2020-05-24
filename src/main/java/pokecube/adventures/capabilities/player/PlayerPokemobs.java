package pokecube.adventures.capabilities.player;

import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import pokecube.adventures.capabilities.CapabilityHasPokemobs.DefaultPokemobs;
import pokecube.adventures.capabilities.CapabilityHasPokemobs.IHasPokemobs;
import pokecube.adventures.capabilities.CapabilityHasPokemobs.ITargetWatcher;
import pokecube.adventures.capabilities.CapabilityHasRewards.DefaultRewards;
import pokecube.adventures.capabilities.CapabilityNPCAIStates.DefaultAIStates;
import pokecube.adventures.capabilities.CapabilityNPCMessages.DefaultMessager;
import pokecube.adventures.capabilities.TrainerCaps;
import pokecube.adventures.capabilities.utils.TypeTrainer;
import pokecube.adventures.events.TrainerEventHandler;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.utils.TagNames;
import thut.api.world.mobs.data.DataSync;
import thut.core.common.world.mobs.data.DataSync_Impl;
import thut.core.common.world.mobs.data.types.Data_String;

public class PlayerPokemobs extends DefaultPokemobs
{
    public static Function<PlayerEntity, IHasPokemobs> PLAYERPOKEMOBS = (p) -> new PlayerPokemobs(p);

    public static void register(final AttachCapabilitiesEvent<Entity> event)
    {
        if (!(event.getObject() instanceof PlayerEntity)) return;
        if (event.getCapabilities().containsKey(TrainerEventHandler.POKEMOBSCAP)) return;
        final IHasPokemobs mobs = PlayerPokemobs.PLAYERPOKEMOBS.apply((PlayerEntity) event.getObject());
        event.addCapability(TrainerEventHandler.POKEMOBSCAP, mobs);
        DataSync data = TrainerEventHandler.getData(event);
        if (data == null)
        {
            data = new DataSync_Impl();
            event.addCapability(TrainerEventHandler.DATASCAP, (DataSync_Impl) data);
        }
        mobs.setDataSync(data);
        if (mobs instanceof PlayerPokemobs) ((PlayerPokemobs) mobs).holder.TYPE = data.register(new Data_String(), "");
    }

    PlayerEntity player;
    LivingEntity target = null;

    public PlayerPokemobs(final PlayerEntity player)
    {
        this.player = player;
        this.init(player, new DefaultAIStates(), new DefaultMessager(), new DefaultRewards());
    }

    @Override
    public void setPokemob(final int slot, final ItemStack cube)
    {
        // We do nothing here.
    }

    @Override
    public boolean addPokemob(final ItemStack mob)
    {
        // We also do not add pokemobs.
        return false;
    }

    @Override
    public AllowedBattle canBattle(final LivingEntity target)
    {
        return AllowedBattle.YES;
    }

    @Override
    public int getMaxPokemobCount()
    {
        return this.player.inventory.getSizeInventory();
    }

    @Override
    public ItemStack getNextPokemob()
    {
        for (int i = 0; i < this.getMaxPokemobCount(); i++)
        {
            final ItemStack stack = this.getPokemob(i);
            if (!stack.isEmpty())
            {
                final CompoundNBT pokeTag = stack.getTag().getCompound(TagNames.POKEMOB);
                final float health = pokeTag.getFloat("Health");
                if (health > 0) return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack getPokemob(final int slot)
    {
        final ItemStack stack = this.player.inventory.getStackInSlot(slot);
        if (PokecubeManager.isFilled(stack)) return stack;
        return ItemStack.EMPTY;
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT nbt = new CompoundNBT();
        if (this.getOutID() != null) nbt.putString("outPokemob", this.getOutID().toString());
        if (this.getType() != null) nbt.putString("type", this.getType().getName());
        return nbt;
    }

    @Override
    public void deserializeNBT(final CompoundNBT nbt)
    {
        this.setType(TypeTrainer.getTrainer(nbt.getString("type"), true));
        if (nbt.contains("outPokemob")) this.setOutID(UUID.fromString(nbt.getString("outPokemob")));
    }

    @Override
    public void onSetTarget(LivingEntity target)
    {
        final IHasPokemobs oldBattle = TrainerCaps.getHasPokemobs(this.target);
        if (target != null && oldBattle != null && oldBattle.getTargetRaw() == this.player && oldBattle.canBattle(
                this.player).test()) return;
        final IHasPokemobs targetmobs = TrainerCaps.getHasPokemobs(target);
        if (targetmobs == null && target != null || target == this.player) target = null;
        final Set<ITargetWatcher> watchers = this.getTargetWatchers();
        this.target = target;
        // Notify the watchers that a target was actually set.
        for (final ITargetWatcher watcher : watchers)
            watcher.onSet(target);
    }

    @Override
    public LivingEntity getTarget()
    {
        final IHasPokemobs oldBattle = TrainerCaps.getHasPokemobs(this.target);
        if (this.target != null && !this.target.isAlive()) this.target = null;
        if (oldBattle != null && oldBattle != this && oldBattle.getTargetRaw() != this.player) this.target = null;
        return this.target;
    }

    @Override
    public void resetPokemob()
    {
        // We do nothing here either.
        this.onSetTarget(null);
    }
}
