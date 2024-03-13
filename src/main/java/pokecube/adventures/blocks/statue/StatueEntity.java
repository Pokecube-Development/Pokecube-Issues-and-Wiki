package pokecube.adventures.blocks.statue;

import java.util.Iterator;
import java.util.Random;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import pokecube.adventures.PokecubeAdv;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.events.pokemobs.SpawnEvent;
import pokecube.api.utils.PokeType;
import pokecube.core.PokecubeCore;
import pokecube.core.blocks.InteractableTile;
import pokecube.core.database.Database;
import pokecube.core.entity.pokemobs.PokemobType;
import thut.api.ThutCaps;
import thut.api.Tracker;
import thut.api.entity.IAnimated.IAnimationHolder;
import thut.api.entity.ICopyMob;
import thut.api.entity.IMobColourable;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;
import thut.core.common.network.TileUpdate;

public class StatueEntity extends InteractableTile
{
    private static final ResourceLocation FUELTAG = new ResourceLocation("pokecube_adventures", "statue_fuel");

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

        final ICopyMob copy = ThutCaps.getCopyMob(this);
        check:
        if (copy != null)
        {
            LivingEntity before = copy.getCopiedMob();
            copy.onBaseTick(this.level, null);
            var after = copy.getCopiedMob();
            if (after == null)
            {
                copy.setCopiedMob(before = PokecubeCore.createPokemob(Database.missingno, this.level));
                break check;
            }
            IPokemob pokemob = PokemobCaps.getPokemobFor(after);
            if (pokemob != null && pokemob.getPokedexEntry() == Database.missingno
                    && after.getType() instanceof PokemobType<?> t && t.getEntry() != Database.missingno)
            {
                pokemob.setBasePokedexEntry(t.getEntry());
                pokemob.setPokedexEntry(t.getEntry());
            }
            if (after != before)
            {
                final BlockPos pos = this.getBlockPos();
                final LivingEntity mob = after;
                IMobColourable colourable = ThutCaps.getColourable(mob);
                if (colourable != null) colourable.getRGBA();
                mob.setUUID(UUID.randomUUID());
                mob.setPos(pos.getX(), pos.getY(), pos.getZ());
                final Direction dir = this.getBlockState().getValue(HorizontalDirectionalBlock.FACING);
                switch (dir)
                {
                case EAST:
                    mob.yRot = mob.yBodyRot = mob.yRotO = mob.yBodyRotO = -90;
                    break;
                case NORTH:
                    mob.yRot = mob.yBodyRot = mob.yRotO = mob.yBodyRotO = 180;
                    break;
                case SOUTH:
                    mob.yRot = mob.yBodyRot = mob.yRotO = mob.yBodyRotO = 0;
                    break;
                case WEST:
                    mob.yRot = mob.yBodyRot = mob.yRotO = mob.yBodyRotO = 90;
                    break;
                default:
                    break;
                }
                copy.setCopiedNBT(copy.getCopiedMob().serializeNBT());
                this.requestModelDataUpdate();
            }
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket()
    {
        this.checkMob();
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag()
    {
        this.checkMob();
        return this.serializeNBT();
    }

    @Override
    public void handleUpdateTag(final CompoundTag tag)
    {
        this.deserializeNBT(tag);
        final ICopyMob copy = ThutCaps.getCopyMob(this);
        var id = copy.getCopiedID();
        var _tag = copy.getCopiedNBT();
        copy.setCopiedMob(null);
        copy.setCopiedID(id);
        copy.setCopiedNBT(_tag);
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
        final ICopyMob copy = ThutCaps.getCopyMob(this);

        if (copy == null || !(this.level instanceof ServerLevel slevel))
        {
            PokecubeAPI.POKEMOB_BUS.unregister(this);
            return;
        }

        if (!event.forSpawn || !slevel.isPositionEntityTicking(getBlockPos())) return;

        // We need to ensure that everything nearby is loaded, otherwise we can
        // have a freeze from hasNeighborSignal below.
        Iterator<Direction> iter = Direction.Plane.HORIZONTAL.iterator();
        while (iter.hasNext())
        {
            Direction d = iter.next();
            ChunkPos pos = new ChunkPos(this.getBlockPos().relative(d));
            if (slevel.getChunkSource().getChunkNow(pos.x, pos.z) == null) return;
        }

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
            float s = d1;

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
    public InteractionResult onInteract(final BlockPos pos, final Player player, final InteractionHand hand,
            final BlockHitResult hit)
    {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown() && stack.is(TagKey.create(Registry.ITEM_REGISTRY, FUELTAG)))
        {
            if (player instanceof ServerPlayer && !player.isCreative())
            {
                stack.shrink(1);
                if (fuelTimer < Tracker.instance().getTick()) fuelTimer = Tracker.instance().getTick();
                fuelTimer += PokecubeAdv.config.statueFuelDuration;
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    public static LivingEntity initMob(ICopyMob copy, CompoundTag modelTag, Runnable initMob)
    {
        String anim = null;
        String over_tex = null;
        int over_tex_a = -1;
        String id = null;
        float size = 1;
        if (modelTag.contains("id")) id = modelTag.getString("id");
        if (modelTag.contains("over_tex")) over_tex = modelTag.getString("over_tex");
        if (modelTag.contains("over_tex_a")) over_tex_a = modelTag.getInt("over_tex_a");
        if (modelTag.contains("anim")) anim = modelTag.getString("anim");
        if (modelTag.contains("size")) size = modelTag.getFloat("size");

        ResourceLocation e_id;
        // First update ID if present, and refresh the mob
        if (id != null)
        {
            copy.setCopiedMob(null);
            copy.setCopiedID(e_id = new ResourceLocation(id));
        }
        else
        {
            copy.setCopiedMob(null);
            copy.setCopiedID(e_id = new ResourceLocation("pokecube:missingno"));
        }
        initMob.run();
        var mob = copy.getCopiedMob();
        final IPokemob pokemob = PokemobCaps.getPokemobFor(mob);
        PokedexEntry entry;
        if (pokemob != null && (entry = Database.getEntry(e_id.getPath())) != pokemob.getPokedexEntry())
        {
            pokemob.setBasePokedexEntry(entry);
            pokemob.setPokedexEntry(entry);
        }
        if (copy.getCopiedMob().getType() instanceof PokemobType<?> t)
        {
            if (pokemob != null && pokemob.getPokedexEntry() == Database.missingno
                    && t.getEntry() != Database.missingno)
            {
                pokemob.setPokedexEntry(t.getEntry());
                pokemob.setBasePokedexEntry(t.getEntry());
            }
        }
        if (over_tex != null) mob.getPersistentData().putString("statue:over_tex", over_tex);
        if (over_tex_a != -1) mob.getPersistentData().putInt("statue:over_tex_a", over_tex_a);
        if (anim != null) mob.getPersistentData().putString("statue:anim", anim);
        if (pokemob != null) pokemob.setSize(size);
        final IAnimationHolder anims = ThutCaps.getAnimationHolder(mob);
        if (anim != null && anims != null)
        {
            anims.setFixed(true);
            anims.overridePlaying(anim);
        }
        return mob;
    }

    @Override
    public void load(final CompoundTag tag)
    {
        super.load(tag);
        // The stuff below only matters for when this is placed directly or nbt
        // edited. when loading normally, level is null, so we exit here.
        if (this.level == null) return;
        final ICopyMob copy = ThutCaps.getCopyMob(this);
        if (tag.contains("custom_model"))
        {
            final CompoundTag modelTag = tag.getCompound("custom_model");
            initMob(copy, modelTag, () -> this.checkMob());
        }
        // Server side send packet that it changed
        if (!this.level.isClientSide()) TileUpdate.sendUpdate(this);
        // refresh mob if changed
        this.checkMob();
        this.fuelTimer = tag.getLong("fuelTimer");
    }

    @Override
    public void saveAdditional(final CompoundTag tag)
    {
        super.saveAdditional(tag);
        tag.putLong("fuelTimer", fuelTimer);
    }
}
