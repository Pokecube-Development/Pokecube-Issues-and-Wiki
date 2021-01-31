package pokecube.core.blocks.nests;

import java.util.HashSet;
import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.nbt.StringNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import pokecube.core.PokecubeItems;
import pokecube.core.blocks.InteractableTile;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokedexEntry.SpawnData;
import pokecube.core.database.SpawnBiomeMatcher;
import pokecube.core.events.EggEvent;
import pokecube.core.handlers.events.SpawnHandler;
import pokecube.core.handlers.events.SpawnHandler.ForbidReason;
import pokecube.core.interfaces.IInhabitable;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityInhabitable;
import pokecube.core.interfaces.capabilities.CapabilityInhabitable.HabitatProvider;
import pokecube.core.inventory.InvWrapper;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;
import pokecube.core.items.pokemobeggs.ItemPokemobEgg;
import thut.api.maths.Vector3;

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
        final Random rand = new Random();
        final EntityPokemobEgg egg = new EntityPokemobEgg(EntityPokemobEgg.TYPE, world);
        egg.setPos(pos.getX() + 1.5 * (0.5 - rand.nextDouble()), pos.getY() + 1, pos.getZ() + 1.5 * (0.5 - rand
                .nextDouble())).setStack(eggItem);
        final EggEvent.Lay event = new EggEvent.Lay(egg);
        MinecraftForge.EVENT_BUS.post(event);
        if (spawnNow) egg.setGrowingAge(-100);// Make it spawn after 5s
        if (!event.isCanceled())
        {
            world.addEntity(egg);
            return egg;
        }
        return null;
    }

    public HashSet<IPokemob>  residents = new HashSet<>();
    public List<PokedexEntry> spawns    = Lists.newArrayList();

    public CompoundNBT tag = new CompoundNBT();

    public final IInhabitable habitat;

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

    public boolean isType(final ResourceLocation type)
    {
        if (this.habitat instanceof HabitatProvider)
        {
            final IInhabitable wrapped = ((HabitatProvider) this.habitat).getWrapped();
            if (wrapped.getKey() != null) return type.equals(wrapped.getKey());
            final IInhabitable made = CapabilityInhabitable.make(type);
            if (made != null)
            {
                ((HabitatProvider) this.habitat).setWrapped(made);
                return true;
            }
        }
        return false;
    }

    public boolean addForbiddenSpawningCoord()
    {
        final BlockPos pos = this.getPos();
        return SpawnHandler.addForbiddenSpawningCoord(pos.getX(), pos.getY(), pos.getZ(), this.world, 32,
                ForbidReason.NEST);
    }

    public void addResident(final IPokemob resident)
    {
        this.residents.add(resident);
    }

    public void init()
    {
        final Vector3 pos = Vector3.getNewVector().set(this);
        for (int i = 0; i < NestTile.NESTSPAWNTYPES; i++)
        {
            int tries = 0;
            PokedexEntry entry = SpawnHandler.getSpawnForLoc(this.getWorld(), pos);
            while (entry == null && tries++ < 10)
                entry = SpawnHandler.getSpawnForLoc(this.getWorld(), pos);
            if (entry != null) this.spawns.add(entry);
        }
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
                        e) -> ChestContainer.createGeneric9X6(i, p, wrapper), new TranslationTextComponent(
                                "block.pokecube.nest"));
                NetworkHooks.openGui(sendTo, provider);
            }
            return ActionResultType.SUCCESS;
        }
        return super.onInteract(pos, player, hand, hit);
    }

    /** Reads a tile entity from NBT. */
    @Override
    public void read(final BlockState state, final CompoundNBT nbt)
    {
        super.read(state, nbt);
        this.spawns.clear();
        if (nbt.contains("spawns"))
        {
            final ListNBT spawnsTag = (ListNBT) nbt.get("spawns");
            for (int i = 0; i < spawnsTag.size(); i++)
            {
                final String name = spawnsTag.getString(i);
                final PokedexEntry entry = Database.getEntry(name);
                if (entry != null) this.spawns.add(entry);
            }
        }
        this.time = nbt.getInt("time");
        this.tag = nbt.getCompound("_data_");
    }

    @Override
    public void remove()
    {
        super.remove();
        this.removeForbiddenSpawningCoord();
    }

    public boolean removeForbiddenSpawningCoord()
    {
        return SpawnHandler.removeForbiddenSpawningCoord(this.getPos(), this.world);
    }

    public void removeResident(final IPokemob resident)
    {
        this.residents.remove(resident);
    }

    @Override
    public void tick()
    {
        if (this.habitat != null && this.world instanceof ServerWorld) this.habitat.onTick((ServerWorld) this.world);
        this.time++;
        final int power = this.world.getRedstonePower(this.getPos(), Direction.DOWN);
        if (!(this.world instanceof ServerWorld) || this.world.getDifficulty() == Difficulty.PEACEFUL && power == 0)
            return;
        if (this.spawns.isEmpty() && this.time >= 200)
        {
            this.time = 0;
            this.init();
        }
        if (this.spawns.isEmpty() || this.time < 200 + this.world.rand.nextInt(2000)) return;
        this.time = 0;
        int num = 3;
        final PokedexEntry entry = this.spawns.get(this.world.rand.nextInt(this.spawns.size()));
        final SpawnData data = entry.getSpawnData();
        if (data != null)
        {
            final Vector3 here = Vector3.getNewVector().set(this);
            final SpawnBiomeMatcher matcher = data.getMatcher(this.world, here);
            final int min = data.getMin(matcher);
            final int max = data.getMax(matcher);
            final int diff = Math.max(1, max - min);
            num = min + this.world.rand.nextInt(diff);
            if (this.residents.size() < num) NestTile.spawnEgg(entry, this.getPos(), (ServerWorld) this.world, true);
        }
    }

    @Override
    public void onBroken()
    {
        if (this.habitat != null && this.world instanceof ServerWorld) this.habitat.onBroken((ServerWorld) this.world);
    }

    @Override
    public void validate()
    {
        super.validate();
        this.addForbiddenSpawningCoord();
    }

    /**
     * Writes a tile entity to NBT.
     *
     * @return
     */
    @Override
    public CompoundNBT write(final CompoundNBT nbt)
    {
        super.write(nbt);
        final ListNBT spawnsTag = new ListNBT();
        for (final PokedexEntry entry : this.spawns)
            spawnsTag.add(StringNBT.valueOf(entry.getTrimmedName()));
        nbt.put("spawns", spawnsTag);
        nbt.putInt("time", this.time);
        nbt.put("_data_", this.tag);
        return nbt;
    }
}
