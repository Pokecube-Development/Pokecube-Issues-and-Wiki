package pokecube.adventures.blocks.statue;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.energy.IEnergyStorage;
import pokecube.adventures.PokecubeAdv;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.events.pokemobs.SpawnEvent;
import pokecube.api.utils.PokeType;
import pokecube.core.PokecubeCore;
import pokecube.core.blocks.InteractableTile;
import pokecube.core.entity.genetics.GeneticsManager;
import pokecube.core.entity.genetics.genes.SizeGene;
import thut.api.ThutCaps;
import thut.api.Tracker;
import thut.api.attachments.CopyMob;
import thut.api.attachments.CopyMob.CopyInfo;
import thut.api.entity.IMobColourable;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;
import thut.core.common.network.TileUpdate;

import java.util.Random;
import java.util.UUID;

public class StatueEntity extends InteractableTile implements IEnergyStorage
{
    private static final ResourceLocation FUELTAG = ResourceLocation.fromNamespaceAndPath("pokecube_adventures",
            "statue_fuel");

    public static CopyInfo unpackStatue(CopyInfo info, Level level)
    {
        if (info == null) return null;
        var copy = info.copy();
        if (copy == null || copy.getCopiedID() == null)
        {
            info = info.withContext(level.registryAccess());
            if (info.copy() != null)
            {
                copy = info.copy();
            }
            else return info;
        }
        if (copy.recreateMob(level))
        {
            info = new CopyMob.CopyInfo(copy, info.tag());
        }
        return info;
    }

    public static CopyInfo unpackStatue(ItemStack statue, Level level)
    {
        if (statue.has(CopyMob.COPY_STORE))
        {
            var info = statue.get(CopyMob.COPY_STORE);
            var info2 = unpackStatue(info, level);
            if (info != info2) statue.set(CopyMob.COPY_STORE, info2);
            return info2;
        }
        return null;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static CopyInfo unpackStatue(StatueEntity entity)
    {
        var comps = entity.components();
        var orig = comps.get(CopyMob.COPY_STORE.get());
        boolean madeNew = orig == null;
        if (madeNew)
        {
            var tag = new CompoundTag();
            tag.putString("id", "pokecube:missingno");
            orig = new CopyInfo(tag);
        }

        var level = entity.getLevel();
        var copy = orig.copy();
        if (copy == null || copy.getCopiedID() == null)
        {
            orig = orig.withContext(level.registryAccess());
            if (orig.copy() != null)
            {
                copy = orig.copy();
            }
            madeNew = true;
        }
        if (copy.recreateMob(level))
        {
            var tag = orig.tag();
            var after = copy.getCopiedMob();
            if (after == null) after = EntityType.PIG.create(level);
            IMobColourable colourable = ThutCaps.getColourable(after);
            if (colourable != null) colourable.getRGBA();
            after.setUUID(UUID.randomUUID());
            var pos = entity.getBlockPos();
            after.setPos(pos.getX(), pos.getY(), pos.getZ());
            final Direction dir = entity.getBlockState().getValue(HorizontalDirectionalBlock.FACING);
            float rot = 0f;
            switch (dir)
            {
            case EAST:
                rot = -90;
                break;
            case NORTH:
                rot = 180;
                break;
            case SOUTH:
                rot = 0;
                break;
            case WEST:
                rot = 90;
                break;
            default:
                break;
            }
            if (tag.contains("rotation")) rot += tag.getFloat("rotation");
            after.setYRot(after.yBodyRot = after.yRotO = after.yBodyRotO = rot);
            float size = tag.contains("size") ? tag.getFloat("size") : 1;
            IPokemob pokemob = PokemobCaps.getPokemobFor(after);
            if (pokemob != null)
            {
                SizeGene.setScale(pokemob, size);
                pokemob.getGenes().getAlleles(GeneticsManager.SIZEGENE).getExpressed().onUpdateTick(after);
            }
            after.setYHeadRot(after.yBodyRot);
            copy.setCopiedMob(after);
            orig = new CopyMob.CopyInfo(copy, tag.copy());
            madeNew = true;
        }

        var loaded = unpackStatue(orig, entity.getLevel());
        // Update the component
        if (loaded != orig || madeNew)
        {
            var builder = DataComponentMap.builder();
            comps.keySet().forEach(t -> {
                var v = comps.get(t);
                if (t != CopyMob.COPY_STORE.get())
                {
                    DataComponentType _t = t;
                    builder.set(_t, v);
                }
            });
            builder.set(CopyMob.COPY_STORE, loaded);
            entity.setComponents(builder.build());
        }
        if (loaded == null || loaded.copy() == null) return null;
        return loaded;
    }

    public int ticks = 0;

    private long lastParticleTick = -1;
    private long fuelTimer = -1;

    public StatueEntity(final BlockEntityType<?> type, final BlockPos pos, final BlockState state)
    {
        super(type, pos, state);
    }

    public StatueEntity(final BlockPos pos, final BlockState state)
    {
        this(PokecubeAdv.STATUE_TYPE.get(), pos, state);
    }

    public void checkMob()
    {
        // No checking mob if not in world! This can happen during certain types
        // of worldgen, passed in via the block.getShape
        if (!this.hasLevel()) return;

        var info = unpackStatue(this);
        if (info != null)
        {
            var copy = info.copy();
            copy.onBaseTick(this.level, null);
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket()
    {
        this.checkMob();
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(Provider provider)
    {
        this.checkMob();
        return this.saveWithoutMetadata(provider);
    }

    @Override
    public void handleUpdateTag(final CompoundTag tag, Provider provider)
    {
        this.loadWithComponents(tag, provider);
        this.checkMob();
    }

    @Override
    public void onLoad()
    {
        super.onLoad();
        if (!level.isClientSide)
        {
            PokecubeAPI.POKEMOB_BUS.register(this);
        }
    }

    @Override
    public void onChunkUnloaded()
    {
        super.onChunkUnloaded();
        if (!level.isClientSide)
        {
            PokecubeAPI.POKEMOB_BUS.unregister(this);
        }
    }

    @SubscribeEvent
    public void onSpawnEventRate(SpawnEvent.Check.Rate event)
    {
        var info = unpackStatue(this);
        if (info == null || !(this.level instanceof ServerLevel slevel))
        {
            PokecubeAPI.POKEMOB_BUS.unregister(this);
            return;
        }

        if (!event.forSpawn || !slevel.isPositionEntityTicking(getBlockPos())) return;

        // We need to ensure that everything nearby is loaded, otherwise we can
        // have a freeze from hasNeighborSignal below.
        for (Direction d : Direction.Plane.HORIZONTAL)
        {
            ChunkPos pos = new ChunkPos(this.getBlockPos().relative(d));
            if (slevel.getChunkSource().getChunkNow(pos.x, pos.z) == null) return;
        }

        var copy = info.copy();
        mob_check:
        if (copy.getCopiedMob() != null)
        {
            final IPokemob pokemob = PokemobCaps.getPokemobFor(copy.getCopiedMob());

            boolean powered = level.hasNeighborSignal(getBlockPos());
            double d = PokecubeCore.getConfig().maxSpawnRadius;

            if (!(pokemob != null && powered && event.location().distToSq(new Vector3().set(this)) < d * d))
                break mob_check;

            double size = pokemob.getMobSizes().mag();

            int x = this.getBlockPos().getX();
            int y = this.getBlockPos().getY();
            int z = this.getBlockPos().getZ();

            long tick = Tracker.instance().getTick();
            if (fuelTimer < tick)
            {
                if (lastParticleTick < tick)
                {
                    Random r = ThutCore.newRandom();
                    for (int l = 0; l < 10; l++)
                    {
                        double i = r.nextGaussian() * size;
                        double j = r.nextGaussian() * size;
                        double k = r.nextGaussian() * size;
                        slevel.sendParticles(ParticleTypes.ANGRY_VILLAGER, x + i, y + j, z + k, 1, 0, 0, 0, 0);
                    }
                    lastParticleTick = tick + 20;
                }
                break mob_check;
            }

            PokedexEntry entry = pokemob.getPokedexEntry();
            float r0 = event.getRate();
            float r1 = r0 > 1 ? r0 : 1;
            r0 = r0 > 1 ? 1 : r0;

            float d1 = (1 - r0);
            float s;

            boolean sameType1 = pokemob.getType1() != PokeType.unknown && event.entry().isType(pokemob.getType1());
            boolean sameType2 = pokemob.getType2() != PokeType.unknown && event.entry().isType(pokemob.getType2());

            int n = 1;
            if (sameType1) n++;
            if (sameType2) n++;
            if (entry == event.entry())
            {
                n = 5;
            }

            s = (float) Math.pow(d1, n);
            if (n == 1)
            {
                event.setRate(r1 * r0 / 2);
            }
            else if (s < 1) event.setRate(r1 * (1 - s));
            if (lastParticleTick < tick)
            {
                Random r = ThutCore.newRandom();
                for (int l = 0; l < 10; l++)
                {
                    double i = r.nextGaussian() * size;
                    double j = r.nextGaussian() * size;
                    double k = r.nextGaussian() * size;
                    slevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, x + i, y + j, z + k, 1, 0, 0, 0, 0);
                }
                lastParticleTick = tick + 20;
            }
        }
    }

    @Override
    public ItemInteractionResult useItemOn(ItemStack stack, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hitResult)
    {
        if (!player.isShiftKeyDown() && stack.is(TagKey.create(Registries.ITEM, FUELTAG)))
        {
            if (player instanceof ServerPlayer && !player.isCreative()) stack.shrink(1);
            if (fuelTimer < Tracker.instance().getTick()) fuelTimer = Tracker.instance().getTick();
            fuelTimer += PokecubeAdv.config.statueFuelDuration;
            return ItemInteractionResult.CONSUME;
        }
        return super.useItemOn(stack, pos, player, hand, hitResult);
    }

    @Override
    public void loadAdditional(final CompoundTag compound, Provider provider)
    {
        super.loadAdditional(compound, provider);
        // The stuff below only matters for when this is placed directly or nbt
        // edited. when loading normally, level is null, so we exit here.
        if (this.level == null) return;
        // Ensure we are unpacked before sending update
        unpackStatue(this);
        // Server side send packet that it changed
        if (!this.level.isClientSide()) TileUpdate.sendUpdate(this);
        // refresh mob if changed
        this.checkMob();
        this.fuelTimer = compound.getLong("fuelTimer");
    }

    @Override
    public void saveAdditional(final CompoundTag compound, Provider provider)
    {
        super.saveAdditional(compound, provider);
        compound.putLong("fuelTimer", fuelTimer);
    }

    @Override
    public int receiveEnergy(int toReceive, boolean simulate)
    {
        if(PokecubeAdv.config.statueEnergyPerTick < 0) return 0;
        if(toReceive < PokecubeAdv.config.statueEnergyPerTick) return 0;
        toReceive = toReceive - PokecubeAdv.config.statueEnergyPerTick;
        if(!simulate)
        {
            long tick = Tracker.instance().getTick();
            this.fuelTimer = tick + toReceive;
        }
        return toReceive;
    }

    @Override
    public int extractEnergy(final int maxExtract, final boolean simulate)
    {
        return 0;
    }

    @Override
    public int getEnergyStored()
    {
        return Math.max(0, (int)(this.fuelTimer - Tracker.instance().getTick()));
    }

    @Override
    public int getMaxEnergyStored()
    {
        return PokecubeAdv.config.statueEnergyPerTick;
    }

    @Override
    public boolean canExtract()
    {
        return false;
    }

    @Override
    public boolean canReceive()
    {
        return true;
    }
}
