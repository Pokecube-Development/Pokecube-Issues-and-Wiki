package pokecube.core.blocks.nests;

import java.util.HashSet;
import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fmllegacy.network.NetworkHooks;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import pokecube.core.PokecubeItems;
import pokecube.core.blocks.InteractableTile;
import pokecube.core.database.PokedexEntry;
import pokecube.core.events.EggEvent;
import pokecube.core.handlers.events.SpawnHandler;
import pokecube.core.handlers.events.SpawnHandler.ForbidReason;
import pokecube.core.handlers.events.SpawnHandler.ForbidRegion;
import pokecube.core.interfaces.IInhabitable;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityInhabitable;
import pokecube.core.interfaces.capabilities.CapabilityInhabitable.HabitatProvider;
import pokecube.core.inventory.InvWrapper;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;
import pokecube.core.items.pokemobeggs.ItemPokemobEgg;
import thut.core.common.ThutCore;

public class NestTile extends InteractableTile implements TickingBlockEntity
{
    public static int NESTSPAWNTYPES = 1;

    public static EntityPokemobEgg spawnEgg(final PokedexEntry entry, final BlockPos pos, final ServerLevel world,
            final boolean spawnNow)
    {
        final ItemStack eggItem = ItemPokemobEgg.getEggStack(entry);
        final CompoundTag nbt = eggItem.getTag();
        final CompoundTag nest = NbtUtils.writeBlockPos(pos);
        nbt.put("nestLoc", nest);
        eggItem.setTag(nbt);
        final Random rand = ThutCore.newRandom();
        final EntityPokemobEgg egg = new EntityPokemobEgg(EntityPokemobEgg.TYPE, world);
        egg.setToPos(pos.getX() + 1.5 * (0.5 - rand.nextDouble()), pos.getY() + 1, pos.getZ() + 1.5 * (0.5 - rand
                .nextDouble())).setStack(eggItem);
        final EggEvent.Lay event = new EggEvent.Lay(egg);
        MinecraftForge.EVENT_BUS.post(event);
        if (spawnNow) egg.setAge(-100);// Make it spawn after 5s
        if (!event.isCanceled())
        {
            world.addFreshEntity(egg);
            return egg;
        }
        return null;
    }

    public HashSet<IPokemob> residents = new HashSet<>();

    public CompoundTag tag = new CompoundTag();

    private final IInhabitable habitat;

    int time = 0;

    public NestTile()
    {
        this(PokecubeItems.NEST_TYPE.get());
    }

    public NestTile(final BlockEntityType<?> tileEntityTypeIn)
    {
        super(tileEntityTypeIn);
        this.habitat = this.getCapability(CapabilityInhabitable.CAPABILITY).orElse(null);
    }

    public void setWrappedHab(final IInhabitable toWrap)
    {
        if (this.habitat instanceof HabitatProvider)
        {
            this.removeForbiddenSpawningCoord();
            ((HabitatProvider) this.habitat).setWrapped(toWrap);
            this.addForbiddenSpawningCoord();
        }
    }

    public IInhabitable getWrappedHab()
    {
        if (this.habitat instanceof HabitatProvider) return ((HabitatProvider) this.habitat).getWrapped();
        return null;
    }

    public boolean isType(final ResourceLocation type)
    {
        if (this.habitat instanceof HabitatProvider)
        {
            final IInhabitable wrapped = ((HabitatProvider) this.habitat).getWrapped();
            if (wrapped.getKey() != null) return type.equals(wrapped.getKey());
        }
        return false;
    }

    public boolean addForbiddenSpawningCoord()
    {
        if (!(this.level instanceof ServerLevel)) return false;
        final BlockPos pos = this.getBlockPos();
        final IInhabitable hab = this.getWrappedHab();
        if (hab == null) return false;
        hab.setPos(pos);
        final ForbidRegion region = hab.getRepelledRegion(this, (ServerLevel) this.level);
        if (region == null) return false;
        return SpawnHandler.addForbiddenSpawningCoord(this.level, region, ForbidReason.NEST);
    }

    public void addResident(final IPokemob resident)
    {
        this.residents.add(resident);
    }

    @Override
    public InteractionResult onInteract(final BlockPos pos, final Player player, final InteractionHand hand,
            final BlockHitResult hit)
    {
        final IItemHandler handler = this.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null);
        if (handler instanceof IItemHandlerModifiable)
        {
            if (player instanceof ServerPlayer)
            {
                final ServerPlayer sendTo = (ServerPlayer) player;
                final Container wrapper = new InvWrapper((IItemHandlerModifiable) handler);
                final SimpleMenuProvider provider = new SimpleMenuProvider((i, p,
                        e) -> ChestMenu.sixRows(i, p, wrapper), new TranslatableComponent(
                                "block.pokecube.nest"));
                NetworkHooks.openGui(sendTo, provider);
            }
            return InteractionResult.SUCCESS;
        }
        return super.onInteract(pos, player, hand, hit);
    }

    /** Reads a tile entity from NBT. */
    @Override
    public void load(final BlockState state, final CompoundTag nbt)
    {
        super.load(state, nbt);
        this.time = nbt.getInt("time");
        this.tag = nbt.getCompound("_data_");
        // Ensure the repel range resets properly.
        if (this.getWrappedHab() != null) this.setWrappedHab(this.getWrappedHab());
    }

    @Override
    public void setRemoved()
    {
        super.setRemoved();
        this.removeForbiddenSpawningCoord();
    }

    public boolean removeForbiddenSpawningCoord()
    {
        if (!(this.level instanceof ServerLevel)) return false;
        final IInhabitable hab = this.getWrappedHab();
        if (hab == null || this.level.isClientSide()) return false;
        final BlockPos pos = this.getBlockPos();
        hab.setPos(pos);
        final ForbidRegion region = hab.getRepelledRegion(this, (ServerLevel) this.level);
        if (region == null) return false;
        return SpawnHandler.removeForbiddenSpawningCoord(region.getPos(), this.level);
    }

    public void removeResident(final IPokemob resident)
    {
        this.residents.remove(resident);
    }

    @Override
    public void tick()
    {
        if (this.habitat != null && this.level instanceof ServerLevel) this.habitat.onTick((ServerLevel) this.level);
        this.time++;
    }

    @Override
    public void onBroken()
    {
        if (this.habitat != null && this.level instanceof ServerLevel) this.habitat.onBroken((ServerLevel) this.level);
    }

    @Override
    public void clearRemoved()
    {
        super.clearRemoved();
        this.addForbiddenSpawningCoord();
    }

    /**
     * Writes a tile entity to NBT.
     *
     * @return
     */
    @Override
    public CompoundTag save(final CompoundTag nbt)
    {
        super.save(nbt);
        nbt.putInt("time", this.time);
        nbt.put("_data_", this.tag);
        return nbt;
    }
}
