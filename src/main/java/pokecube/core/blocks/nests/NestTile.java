package pokecube.core.blocks.nests;

import java.util.HashSet;
import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.network.NetworkHooks;
import pokecube.api.blocks.IInhabitable;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.CapabilityInhabitable.HabitatProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.events.EggEvent;
import pokecube.core.PokecubeItems;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.blocks.InteractableTile;
import pokecube.core.eventhandlers.SpawnHandler;
import pokecube.core.eventhandlers.SpawnHandler.ForbidReason;
import pokecube.core.eventhandlers.SpawnHandler.ForbidRegion;
import pokecube.core.init.EntityTypes;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;
import pokecube.core.items.pokemobeggs.ItemPokemobEgg;
import pokecube.core.utils.CapHolders;
import thut.api.ThutCaps;
import thut.api.block.ITickTile;
import thut.api.inventory.InvWrapper;
import thut.core.common.ThutCore;
import thut.lib.TComponent;

public class NestTile extends InteractableTile implements ITickTile
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
        final EntityPokemobEgg egg = new EntityPokemobEgg(EntityTypes.getEgg(), world);
        egg.setToPos(pos.getX() + 1.5 * (0.5 - rand.nextDouble()), pos.getY() + 1,
                pos.getZ() + 1.5 * (0.5 - rand.nextDouble())).setStack(eggItem);
        final EggEvent.Lay event = new EggEvent.Lay(egg);
        ThutCore.FORGE_BUS.post(event);
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

    public NestTile(final BlockPos pos, final BlockState state)
    {
        this(PokecubeItems.NEST_TYPE.get(), pos, state);
    }

    public NestTile(final BlockEntityType<?> tileEntityTypeIn, final BlockPos pos, final BlockState state)
    {
        super(tileEntityTypeIn, pos, state);
        this.habitat = CapHolders.getInhabitable(this);
    }

    public void setWrappedHab(final IInhabitable toWrap)
    {
        if (this.habitat instanceof HabitatProvider hab)
        {
            this.removeForbiddenSpawningCoord();
            hab.setWrapped(toWrap);
            this.addForbiddenSpawningCoord();
        }
    }

    public IInhabitable getWrappedHab()
    {
        if (this.habitat instanceof HabitatProvider hab) return hab.getWrapped();
        return null;
    }

    public boolean isType(final ResourceLocation type)
    {
        if (this.habitat instanceof HabitatProvider hab)
        {
            final IInhabitable wrapped = hab.getWrapped();
            if (wrapped.getKey() != null) return type.equals(wrapped.getKey());
        }
        return false;
    }

    public boolean addForbiddenSpawningCoord()
    {
        if (!(this.level instanceof ServerLevel level)) return false;
        final BlockPos pos = this.getBlockPos();
        final IInhabitable hab = this.getWrappedHab();
        if (hab == null) return false;
        hab.setPos(pos);
        final ForbidRegion region = hab.getRepelledRegion(this, level);
        if (region == null) return false;
        return SpawnHandler.addForbiddenSpawningCoord(this.level, region, ForbidReason.NEST);
    }

    public void addResident(final IPokemob resident)
    {
        this.residents.add(resident);
        final IInhabitable hab = this.getWrappedHab();
        if (resident.getEntity().getBrain().checkMemory(MemoryModules.NEST_POS.get(), MemoryStatus.REGISTERED))
        {
            resident.getEntity().getBrain().setMemory(MemoryModules.NEST_POS.get(),
                    GlobalPos.of(level.dimension(), getBlockPos()));
        }
        if (hab != null) hab.addResident(resident.getEntity());
    }

    @Override
    public InteractionResult onInteract(final BlockPos pos, final Player player, final InteractionHand hand,
            final BlockHitResult hit)
    {
        final IItemHandler handler = ThutCaps.getInventory(this);
        if (handler instanceof IItemHandlerModifiable mhandler)
        {
            if (player instanceof ServerPlayer sendTo)
            {
                final InvWrapper wrapper = new InvWrapper(mhandler);
                wrapper.addListener(c -> {
                    this.getLevel().getChunk(getBlockPos()).setUnsaved(true);
                });
                final SimpleMenuProvider provider = new SimpleMenuProvider(
                        (i, p, e) -> ChestMenu.sixRows(i, p, wrapper), TComponent.translatable("block.pokecube.nest"));
                NetworkHooks.openGui(sendTo, provider);
            }
            return InteractionResult.SUCCESS;
        }
        return super.onInteract(pos, player, hand, hit);
    }

    /** Reads a tile entity from NBT. */
    @Override
    public void load(final CompoundTag nbt)
    {
        super.load(nbt);
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
        if (!(this.level instanceof ServerLevel level)) return false;
        final IInhabitable hab = this.getWrappedHab();
        if (hab == null || this.level.isClientSide()) return false;
        final BlockPos pos = this.getBlockPos();
        hab.setPos(pos);
        final ForbidRegion region = hab.getRepelledRegion(this, level);
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
        if (this.habitat != null && this.level instanceof ServerLevel level) this.habitat.onTick(level);
        this.time++;
    }

    @Override
    public void onBroken()
    {
        if (this.habitat != null && this.level instanceof ServerLevel level) this.habitat.onBroken(level);
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
    public void saveAdditional(final CompoundTag nbt)
    {
        super.saveAdditional(nbt);
        nbt.putInt("time", this.time);
        nbt.put("_data_", this.tag);
    }
}
