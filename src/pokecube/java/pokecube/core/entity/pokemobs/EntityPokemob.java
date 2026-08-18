/**
 *
 */
package pokecube.core.entity.pokemobs;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.fluids.FluidType;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.api.data.PokedexEntry.SpawnData;
import pokecube.api.data.spawns.SpawnCheck;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.CombatStates;
import pokecube.api.events.pokemobs.SpawnEvent;
import pokecube.api.events.pokemobs.SpawnEvent.SpawnContext;
import pokecube.api.events.pokemobs.SpawnEvent.Variance;
import pokecube.api.utils.PokeType;
import pokecube.api.utils.TagNames;
import pokecube.api.utils.Tools;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.tasks.idle.HungerTask;
import pokecube.core.database.Database;
import pokecube.core.entity.pokemobs.helper.PokemobRidable;
import pokecube.core.eventhandlers.SpawnHandler;
import pokecube.core.handlers.playerdata.PlayerPokemobCache;
import pokecube.core.impl.PokecubeMod;
import pokecube.core.init.Config;
import pokecube.core.init.EntityTypes;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;
import pokecube.core.items.pokemobeggs.ItemPokemobEgg;
import pokecube.core.utils.PokemobTracker;
import thut.api.ThutCaps;
import thut.api.entity.IAnimated;
import thut.api.item.ItemList;
import thut.api.maths.Vector3;

import javax.annotation.Nullable;
import java.util.UUID;

public class EntityPokemob extends PokemobRidable
{
    static ResourceLocation WALL_CLIMBERS = ResourceLocation.fromNamespaceAndPath(PokecubeMod.ID, "wall_climbing");

    private static final EntityDataAccessor<Byte> CLIMBING = SynchedEntityData.defineId(EntityPokemob.class,
            EntityDataSerializers.BYTE);

    public EntityPokemob(final EntityType<? extends TamableAnimal> type, final Level world)
    {
        super(type, world);
    }

    @Override
    protected void defineSynchedData(Builder builder)
    {
        super.defineSynchedData(builder);
        builder.define(EntityPokemob.CLIMBING, (byte) 0);
    }

    @Override
    public void setPose(Pose pose)
    {
        super.setPose(pose);
    }

    @Override
    public AgeableMob getBreedOffspring(final ServerLevel p_241840_1_, final AgeableMob ageable)
    {
        final IPokemob other = PokemobCaps.getPokemobFor(ageable);
        if (other == null) return null;
        final EntityPokemobEgg egg = EntityTypes.getEgg().create(this.level());
        egg.setStackByParents(this, other);
        return egg;
    }

    @Override
    public boolean isInWall()
    {
        return false;
    }

    @Override
    public boolean isFood(ItemStack stack)
    {
        return ItemList.is(HungerTask.FOODTAG, stack);
    }

    @Override
    public boolean canDrownInFluidType(FluidType type)
    {
        if (type == NeoForgeMod.WATER_TYPE.value())
            return !(this.getPokemob().swims() || this.getPokemob().canUseDive() || this.getPokemob()
                    .isType(PokeType.getType("water")));
        return super.canDrownInFluidType(type);
    }

    @Override
    public void die(final DamageSource cause)
    {
        super.die(cause);
        this.getPokemob().setCombatState(CombatStates.FAINTED, true);
    }

    @Override
    public void travel(Vec3 dr)
    {
        // Swimming mobs get their own treatment while swimming
        if (this.isControlledByLocalInstance() && this.isInWater() && this.getPokemob().swims())
        {
            this.moveRelative(this.getSpeed(), dr);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.8D));
        }
        // Otherwise just act like vanilla
        else super.travel(dr);
    }

    @Override
    @Nullable
    protected ResourceKey<LootTable> getDefaultLootTable()
    {
        var def = super.getDefaultLootTable();
        if (this.getPersistentData().getBoolean(TagNames.CLONED) && !PokecubeCore.getConfig().clonesDrop) return def;
        if (this.getPersistentData().getBoolean(TagNames.NODROP)) return def;
        var table = this.getPokemob().getPokedexEntry().lootTable;
        if (this.level() instanceof ServerLevel level && Config.Rules.dropLoot(level) && table != null) return table;
        else return def;
    }

    @Override
    public ItemStack getPickedResult(final HitResult target)
    {
        return ItemPokemobEgg.getEggStack(this.getPokemob());
    }

    @Override
    public SpawnGroupData finalizeSpawn(final ServerLevelAccessor worldIn, final DifficultyInstance difficultyIn,
            final MobSpawnType reason, final SpawnGroupData spawnDataIn)
    {
        final IPokemob pokemob = PokemobCaps.getPokemobFor(this);
        if (pokemob == null || !(worldIn instanceof Level))
            return super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn);
        final PokedexEntry pokeEntry = pokemob.getPokedexEntry();
        final SpawnData entry = pokeEntry.getSpawnData();
        if (entry == null) return super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn);
        final Vector3 loc = new Vector3().set(this);

        SpawnContext context = new SpawnContext(pokemob);
        SpawnCheck checker = new SpawnCheck(loc, worldIn);

        var record = entry.getMatcher(context, checker);

        final int orig_override = entry.getLevel(record);
        int overrideLevel = orig_override;
        final Variance variance = entry.getVariance(record);
        if (variance != null) overrideLevel = variance.apply(overrideLevel);

        if (pokemob != null)
        {
            final long time = System.nanoTime();
            int maxXP;
            int level;
            if (orig_override == -1) level = SpawnHandler.getSpawnLevel(context, variance, overrideLevel);
            else
            {
                final SpawnEvent.PickLevel event = new SpawnEvent.PickLevel(context, overrideLevel, variance);
                PokecubeAPI.POKEMOB_BUS.post(event);
                level = event.getLevel();
            }
            maxXP = Tools.levelToXp(pokemob.getPokedexEntry().getEvolutionMode(), level);
            pokemob.getEntity().getPersistentData().putInt(TagNames.SPAWN_EXP, maxXP);
            final double dt = (System.nanoTime() - time) / 10e3D;
            if (PokecubeCore.getConfig().debug_spawning && dt > 100)
            {
                final String toLog = "location: %1$s took: %2$sµs to spawn Init for %3$s";
                PokecubeAPI.logInfo(String.format(toLog, loc.getPos(), dt, pokemob.getDisplayName().getString()));
            }
        }
        return super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn);
    }

    @Override
    public void readSpawnData(final RegistryFriendlyByteBuf data)
    {
        this.seatCount = data.readInt();
        final FriendlyByteBuf buffer = new FriendlyByteBuf(data);
        CompoundTag tag = buffer.readNbt();
        if (!tag.isEmpty()) this.getPersistentData().put("url_model", tag);
    }

    @Override
    public boolean causeFallDamage(final float distance, final float damageMultiplier, final DamageSource source)
    {
        // TODO maybe do something here?
        // Vanilla plays sound and does damage, but only plays the sound if
        // damage occurred, maybe we should just play the sound instead?
        return super.causeFallDamage(distance, damageMultiplier, source);
    }

    @Override
    protected void checkFallDamage(final double y, final boolean onGroundIn, final BlockState state, final BlockPos pos)
    {}

    @Override
    protected SoundEvent getAmbientSound()
    {
        return this.getPokemob().getSound();
    }

    @Override
    protected float getSoundVolume()
    {
        return (float) PokecubeCore.getConfig().idleSoundVolume;
    }

    @Override
    public SoundSource getSoundSource()
    {
        return SoundSource.HOSTILE;
    }

    @Override
    public int getAmbientSoundInterval()
    {
        return PokecubeCore.getConfig().idleSoundRate;
    }

    @Override
    public void onRemovedFromLevel()
    {
        PokemobTracker.removePokemob(this.getPokemob());
        if (this.getPokemob().isPlayerOwned() && this.getPokemob().getOwnerId() != null)
            PlayerPokemobCache.UpdateCache(this.getPokemob());
        super.onRemovedFromLevel();
    }

    @Override
    public void onAddedToLevel()
    {
        PokemobTracker.addPokemob(this.getPokemob());
        if (this.getPokemob().isPlayerOwned() && this.getPokemob().getOwnerId() != null)
            PlayerPokemobCache.UpdateCache(this.getPokemob());
        super.onAddedToLevel();
    }

    @Override
    public void writeSpawnData(final RegistryFriendlyByteBuf data)
    {
        this.initSeats();
        data.writeInt(this.seatCount);
        this.getPokemob().updateHealth();
        final FriendlyByteBuf buffer = new FriendlyByteBuf(data);
        CompoundTag nbt = this.getPersistentData().getCompound("url_model");
        buffer.writeNbt(nbt);
    }

    @Override
    public boolean checkSpawnRules(final LevelAccessor worldIn, final MobSpawnType spawnReasonIn)
    {
        return true;
    }

    private int despawntimer = 0;

    @Override
    public boolean requiresCustomPersistence()
    {
        if (isPersistenceRequired() || super.requiresCustomPersistence()) return true;
        if (!(level instanceof ServerLevel level)) return true;

        final boolean despawns = Config.Rules.doDespawn(level);
        final boolean culls = Config.Rules.doCull(level);
        final boolean owned = this.getPokemob().getOwnerId() != null;

        if (owned)
        {
            this.setPersistenceRequired();
            return true;
        }
        if (this.getPersistentData().contains(TagNames.NOPOOF)) return true;
        return !(despawns || culls);
    }

    private boolean cullCheck(double distanceToClosestPlayer)
    {
        if (this.getPokemob().getOwnerId() != null || !(level instanceof ServerLevel level)) return false;
        final boolean noPoof = this.getPersistentData().getBoolean(TagNames.NOPOOF);
        if (noPoof) return false;
        distanceToClosestPlayer = Math.sqrt(distanceToClosestPlayer);
        if (Config.Rules.doCull(level, distanceToClosestPlayer)) return true;
        if (Config.Rules.doDespawn(level, distanceToClosestPlayer))
        {
            this.despawntimer--;
            return this.despawntimer <= 0;
        }
        this.despawntimer = PokecubeCore.getConfig().despawnTimer;
        return false;
    }

    @Override
    public void checkDespawn()
    {
        if (this.level.getDifficulty() == Difficulty.PEACEFUL && this.shouldDespawnInPeaceful())
        {
            this.discard();
        }
        else if (!this.isPersistenceRequired() && !this.requiresCustomPersistence()
                && level() instanceof ServerLevel level)
        {
            Entity entity = level.getNearestPlayer(this, -1.0D);
            var notDefault = EventHooks.checkMobDespawn(this);
            if (notDefault && entity != null)
            {
                double d0 = entity.distanceToSqr(this);
                if (this.removeWhenFarAway(d0))
                {
                    this.discard();
                }
            }
        }
        else
        {
            this.noActionTime = 0;
        }
    }

    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer)
    {
        return this.cullCheck(distanceToClosestPlayer);
    }

    @Override
    public void readAdditionalSaveData(final CompoundTag compound)
    {
        super.readAdditionalSaveData(compound);
        if (this.getPokemob().getCustomHolder() != null
                && this.getPokemob().getCustomHolder()._entry == Database.missingno)
            this.getPokemob().getCustomHolder().setEntry(this.getPokemob().getPokedexEntry());
        if (compound.contains("OwnerUUID")) try
        {
            final UUID id = UUID.fromString(compound.getString("OwnerUUID"));
            this.setOwnerUUID(id);
            this.setTame(true, false);
        }
        catch (final Exception e)
        {
            PokecubeAPI.LOGGER.error("Error recovering old owner!");
        }
    }

    private int climbDelay = 0;

    /**
     * Called to update the entity's position/logic.
     */
    @Override
    public void tick()
    {
        super.tick();
        if (!this.level.isClientSide)
        {
            boolean climb = this.horizontalCollision;
            if (climb && climbDelay-- < 0)
            {
                final Path p = this.getNavigation().getPath();
                climb = p.getNextNodePos().getY() >= this.getY();
            }
            else climbDelay = 5;
            this.setBesideClimbableBlock(climb);
        }
    }

    @Override
    public void kill()
    {
        if (this.isInvulnerable())
        {
            PokecubeAPI.logInfo("Not deleting {} from /kill, as is marked as invulnerable!", this);
            return;
        }
        super.kill();
    }

    /**
     * Returns true if this entity should move as if it were on a ladder (either because it's actually on a ladder, or
     * for AI reasons)
     */
    @Override
    public boolean onClimbable()
    {
        return this.isBesideClimbableBlock();
    }

    /**
     * Returns true if the WatchableObject (Byte) is 0x01 otherwise returns false. The WatchableObject is updated using
     * setBesideClimableBlock.
     */
    public boolean isBesideClimbableBlock()
    {
        return (this.entityData.get(EntityPokemob.CLIMBING) & 1) != 0;
    }

    /**
     * Updates the WatchableObject (Byte) created in entityInit(), setting it to 0x01 if par1 is true or 0x00 if it is
     * false.
     */
    public void setBesideClimbableBlock(final boolean climbing)
    {
        // Only do this if tagged accordingly
        if (!ItemList.is(EntityPokemob.WALL_CLIMBERS, this)) return;

        byte b0 = this.entityData.get(EntityPokemob.CLIMBING);
        if (climbing) b0 = (byte) (b0 | 1);
        else b0 = (byte) (b0 & -2);

        this.entityData.set(EntityPokemob.CLIMBING, b0);
    }

    private IAnimated animationHolder;
    private boolean checkedAnim = false;

    @Override
    public boolean isFlying()
    {
        if (!checkedAnim)
        {
            animationHolder = ThutCaps.getAnimated(this);
            checkedAnim = true;
        }
        if (animationHolder != null) return animationHolder.getChoices().contains("flying");
        return false;
    }
}
