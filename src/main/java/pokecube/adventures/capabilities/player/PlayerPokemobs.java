package pokecube.adventures.capabilities.player;

import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import pokecube.adventures.capabilities.CapabilityHasPokemobs.DefaultPokemobs;
import pokecube.adventures.capabilities.CapabilityHasPokemobs.IHasPokemobs;
import pokecube.adventures.capabilities.CapabilityHasPokemobs.ITargetWatcher;
import pokecube.adventures.capabilities.CapabilityHasRewards.DefaultRewards;
import pokecube.adventures.capabilities.CapabilityNPCAIStates.DefaultAIStates;
import pokecube.adventures.capabilities.CapabilityNPCMessages.DefaultMessager;
import pokecube.adventures.capabilities.utils.TypeTrainer;
import pokecube.adventures.events.TrainerEventHandler;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.utils.TagNames;
import thut.api.world.mobs.data.DataSync;
import thut.core.common.world.mobs.data.DataSync_Impl;
import thut.core.common.world.mobs.data.types.Data_ItemStack;
import thut.core.common.world.mobs.data.types.Data_String;
import thut.wearables.EnumWearable;
import thut.wearables.ThutWearables;
import thut.wearables.inventory.PlayerWearables;

public class PlayerPokemobs extends DefaultPokemobs
{
    public static Function<Player, IHasPokemobs> PLAYERPOKEMOBS = (p) -> new PlayerPokemobs(p);

    public static void register(final AttachCapabilitiesEvent<Entity> event)
    {
        if (!(event.getObject() instanceof Player)) return;
        if (event.getCapabilities().containsKey(TrainerEventHandler.POKEMOBSCAP)) return;
        final IHasPokemobs mobs = PlayerPokemobs.PLAYERPOKEMOBS.apply((Player) event.getObject());
        event.addCapability(TrainerEventHandler.POKEMOBSCAP, mobs);
        DataSync data = TrainerEventHandler.getData(event);
        if (data == null)
        {
            data = new DataSync_Impl();
            event.addCapability(TrainerEventHandler.DATASCAP, (DataSync_Impl) data);
        }
        mobs.setDataSync(data);
        if (mobs instanceof PlayerPokemobs players)
        {
            players.holder.TYPE = data.register(new Data_String(), "");
            for (int i = 0; i < 6; i++)
                players.holder.POKEMOBS[i] = data.register(new Data_ItemStack(), ItemStack.EMPTY);
        }
    }

    Player player;
    LivingEntity target = null;

    long setTargetTime = 0;

    boolean hasBelt = false;

    public PlayerPokemobs(final Player player)
    {
        this.player = player;
        this.init(player, new DefaultAIStates(), new DefaultMessager(), new DefaultRewards());
    }

    @Override
    public void setPokemob(final int slot, final ItemStack cube)
    {
        final PlayerWearables worn = ThutWearables.getWearables(player);
        this.hasBelt = worn != null && !worn.getWearable(EnumWearable.WAIST).isEmpty();
        // We do nothing here otherwise
        if (hasBelt) super.setPokemob(slot, cube);
    }

    @Override
    public boolean addPokemob(final ItemStack mob)
    {
        final PlayerWearables worn = ThutWearables.getWearables(player);
        this.hasBelt = worn != null && !worn.getWearable(EnumWearable.WAIST).isEmpty();
        if (hasBelt) return super.addPokemob(mob);
        // We also do not add pokemobs.
        return false;
    }

    @Override
    public AllowedBattle canBattle(final LivingEntity target)
    {
        if (this.target != null && target != this.target) return AllowedBattle.NOTNOW;
        return AllowedBattle.YES;
    }

    @Override
    public int getMaxPokemobCount()
    {
        final PlayerWearables worn = ThutWearables.getWearables(player);
        this.hasBelt = worn != null && !worn.getWearable(EnumWearable.WAIST).isEmpty();
        if (hasBelt) return super.getMaxPokemobCount();
        return this.player.getInventory().getContainerSize();
    }

    @Override
    public ItemStack getNextPokemob()
    {
        ItemStack next = ItemStack.EMPTY;
        if (hasBelt) next = super.getNextPokemob();
        if (!next.isEmpty()) return next;
        for (int i = 0; i < this.player.getInventory().getContainerSize(); i++)
        {
            final ItemStack stack = this.player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.hasTag())
            {
                final CompoundTag pokeTag = stack.getTag().getCompound(TagNames.POKEMOB);
                final float health = pokeTag.getFloat("Health");
                if (health > 0) return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack getPokemob(final int slot)
    {
        final PlayerWearables worn = ThutWearables.getWearables(player);
        this.hasBelt = worn != null && !worn.getWearable(EnumWearable.WAIST).isEmpty();
        if (hasBelt) return super.getPokemob(slot);
        final ItemStack stack = this.player.getInventory().getItem(slot);
        if (PokecubeManager.isFilled(stack)) return stack;
        return ItemStack.EMPTY;
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag nbt = new CompoundTag();
        if (this.getOutID() != null) nbt.putString("outPokemob", this.getOutID().toString());
        if (this.getType() != null) nbt.putString("type", this.getType().getName());
        final ListTag ListNBT = new ListTag();
        for (int index = 0; index < this.getMaxPokemobCount(); index++)
        {
            final ItemStack i = this.getPokemob(index);
            if (i.isEmpty()) continue;
            final CompoundTag CompoundNBT = new CompoundTag();
            ListNBT.add(i.save(CompoundNBT));
        }

        nbt.put("pokemobs", ListNBT);
        return nbt;
    }

    @Override
    public void deserializeNBT(final CompoundTag nbt)
    {
        this.setType(TypeTrainer.getTrainer(nbt.getString("type"), true));
        if (nbt.contains("outPokemob")) this.setOutID(UUID.fromString(nbt.getString("outPokemob")));
        if (nbt.contains("pokemobs", 9))
        {
            if (this.clearOnLoad()) this.clearContent();
            final ListTag ListNBT = nbt.getList("pokemobs", 10);
            if (ListNBT.size() != 0) for (int i = 0; i < Math.min(ListNBT.size(), this.getMaxPokemobCount()); ++i)
                this.setPokemob(i, ItemStack.of(ListNBT.getCompound(i)));
        }
    }

    @Override
    public void onSetTarget(final LivingEntity target)
    {
        if (target != null && target.getServer() != null)
            this.setTargetTime = target.getServer().getLevel(Level.OVERWORLD).getGameTime();
        if (target == this.target) return;
        final Set<ITargetWatcher> watchers = this.getTargetWatchers();
        this.target = target;
        // Notify the watchers that a target was actually set.
        for (final ITargetWatcher watcher : watchers) watcher.onSet(target);
    }

    @Override
    public void onSetTarget(final LivingEntity target, final boolean ignoreCanBattle)
    {
        this.onSetTarget(target);
    }

    @Override
    public LivingEntity getTarget()
    {
        if (this.target != null && this.target.getServer() != null)
            if (this.target.getServer().getLevel(Level.OVERWORLD).getGameTime() - this.setTargetTime > 50)
                this.target = null;
        return this.target;
    }

    @Override
    public void resetPokemob()
    {
        // We do nothing here either.
        this.onSetTarget(null);
    }
}
