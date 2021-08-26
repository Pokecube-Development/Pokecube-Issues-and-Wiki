package pokecube.core.blocks.nests;

import java.util.HashSet;
import java.util.Random;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.network.NetworkHooks;
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

public class NestTile extends InteractableTile implements ITickableTileEntity
{
    public static int NESTSPAWNTYPES = 1;

    public static EntityPokemobEgg spawnEgg(final PokedexEntry entry, final BlockPos pos, final ServerWorld world,
            final boolean spawnNow)
    {
        final ItemStack eggItem = ItemPokemobEgg.getEggStack(entry);
        final CompoundNBT nbt = eggItem.getTag();
        final CompoundNBT nest = NBTUtil.writeBlockPos(pos);
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

    public CompoundNBT tag = new CompoundNBT();

    private final IInhabitable habitat;

    int time = 0;

    public NestTile()
    {
        this(PokecubeItems.NEST_TYPE.get());
    }

    public NestTile(final TileEntityType<?> tileEntityTypeIn)
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
        if (!(this.level instanceof ServerWorld)) return false;
        final BlockPos pos = this.getBlockPos();
        final IInhabitable hab = this.getWrappedHab();
        if (hab == null) return false;
        hab.setPos(pos);
        final ForbidRegion region = hab.getRepelledRegion(this, (ServerWorld) this.level);
        if (region == null) return false;
        return SpawnHandler.addForbiddenSpawningCoord(this.level, region, ForbidReason.NEST);
    }

    public void addResident(final IPokemob resident)
    {
        this.residents.add(resident);
    }

    @Override
    public ActionResultType onInteract(final BlockPos pos, final PlayerEntity player, final Hand hand,
            final BlockRayTraceResult hit)
    {
        final IItemHandler handler = this.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null);
        if (handler instanceof IItemHandlerModifiable)
        {
            if (player instanceof ServerPlayerEntity)
            {
                final ServerPlayerEntity sendTo = (ServerPlayerEntity) player;
                final IInventory wrapper = new InvWrapper((IItemHandlerModifiable) handler);
                final SimpleNamedContainerProvider provider = new SimpleNamedContainerProvider((i, p,
                        e) -> ChestContainer.sixRows(i, p, wrapper), new TranslationTextComponent(
                                "block.pokecube.nest"));
                NetworkHooks.openGui(sendTo, provider);
            }
            return ActionResultType.SUCCESS;
        }
        return super.onInteract(pos, player, hand, hit);
    }

    /** Reads a tile entity from NBT. */
    @Override
    public void load(final BlockState state, final CompoundNBT nbt)
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
        if (!(this.level instanceof ServerWorld)) return false;
        final IInhabitable hab = this.getWrappedHab();
        if (hab == null || this.level.isClientSide()) return false;
        final BlockPos pos = this.getBlockPos();
        hab.setPos(pos);
        final ForbidRegion region = hab.getRepelledRegion(this, (ServerWorld) this.level);
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
        if (this.habitat != null && this.level instanceof ServerWorld) this.habitat.onTick((ServerWorld) this.level);
        this.time++;
    }

    @Override
    public void onBroken()
    {
        if (this.habitat != null && this.level instanceof ServerWorld) this.habitat.onBroken((ServerWorld) this.level);
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
    public CompoundNBT save(final CompoundNBT nbt)
    {
        super.save(nbt);
        nbt.putInt("time", this.time);
        nbt.put("_data_", this.tag);
        return nbt;
    }
}
