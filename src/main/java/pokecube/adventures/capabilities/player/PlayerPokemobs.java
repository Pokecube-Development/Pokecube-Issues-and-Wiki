package pokecube.adventures.capabilities.player;

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
import pokecube.adventures.capabilities.CapabilityHasRewards.DefaultRewards;
import pokecube.adventures.capabilities.CapabilityNPCAIStates.DefaultAIStates;
import pokecube.adventures.capabilities.CapabilityNPCMessages.DefaultMessager;
import pokecube.adventures.capabilities.utils.TypeTrainer;
import pokecube.adventures.events.TrainerEventHandler;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.utils.TagNames;

public class PlayerPokemobs extends DefaultPokemobs
{
    public static Function<PlayerEntity, IHasPokemobs> PLAYERPOKEMOBS = (p) -> new PlayerPokemobs(p);

    public static void register(final AttachCapabilitiesEvent<Entity> event)
    {
        if (!(event.getObject() instanceof PlayerEntity)) return;
        if (event.getCapabilities().containsKey(TrainerEventHandler.POKEMOBSCAP)) return;
        final IHasPokemobs mobs = PlayerPokemobs.PLAYERPOKEMOBS.apply((PlayerEntity) event.getObject());
        event.addCapability(TrainerEventHandler.POKEMOBSCAP, mobs);
    }

    PlayerEntity player;

    public PlayerPokemobs(final PlayerEntity player)
    {
        this.player = player;
        this.init(player, new DefaultAIStates(), new DefaultMessager(), new DefaultRewards());
    }

    @Override
    public boolean canBattle(final LivingEntity target)
    {
        return true;
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
        this.setType(TypeTrainer.getTrainer(nbt.getString("type")));
        if (nbt.contains("outPokemob")) this.setOutID(UUID.fromString(nbt.getString("outPokemob")));
    }
}
