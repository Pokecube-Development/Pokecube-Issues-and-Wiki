package pokecube.core.ai.tasks.burrows.burrow;

import thut.api.Tracker;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.INBTSerializable;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.tasks.burrows.BurrowTasks;
import pokecube.core.blocks.nests.NestTile;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokedexEntry.EvolutionData;
import pokecube.core.database.PokedexEntry.SpawnData;
import pokecube.core.entity.pokemobs.genetics.genes.SpeciesGene;
import pokecube.core.entity.pokemobs.genetics.genes.SpeciesGene.SpeciesInfo;
import pokecube.core.handlers.events.SpawnHandler.AABBRegion;
import pokecube.core.handlers.events.SpawnHandler.ForbidRegion;
import pokecube.core.interfaces.IInhabitable;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;
import pokecube.core.world.IWorldTickListener;

public class BurrowHab implements IInhabitable, INBTSerializable<CompoundNBT>, IWorldTickListener
{
    public static BurrowHab makeFor(final IPokemob pokemob, final BlockPos pos)
    {
        final BurrowHab hab = new BurrowHab();
        hab.setMaker(pokemob.getPokedexEntry());
        if (hab.related.isEmpty()) return null;
        hab.setPos(pos);
        final Predicate<MobEntity> filter = mob -> hab.canEnterHabitat(mob);
        final List<MobEntity> mobs = pokemob.getEntity().getCommandSenderWorld().getEntitiesOfClass(MobEntity.class,
                hab.burrow.getOutBounds().inflate(10), filter);
        for (final MobEntity mob : mobs)
            if (!hab.mobs.contains(mob.getUUID())) hab.mobs.add(mob.getUUID());
        return hab;
    }

    private PokedexEntry maker;

    public Room burrow;

    ForbidRegion repelled = null;

    final Set<PokedexEntry> related = Sets.newHashSet();

    final Set<PokedexEntry> mutations = Sets.newHashSet();

    Predicate<PokedexEntry> valid = e ->
    {
        return this.related.contains(e) || this.mutations.contains(e);
    };

    public Set<UUID> eggs = Sets.newHashSet();
    public Set<UUID> mobs = Sets.newHashSet();

    public PokedexEntry getMaker()
    {
        return this.maker;
    }

    @Override
    public void updateRepelledRegion(final TileEntity tile, final ServerWorld world)
    {
        final AxisAlignedBB box = this.burrow.getOutBounds().inflate(16, 0, 16);
        this.repelled = new AABBRegion(box);
    }

    @Override
    public ForbidRegion getRepelledRegion(final TileEntity tile, final ServerWorld world)
    {
        if (this.repelled == null) this.updateRepelledRegion(tile, world);
        return this.repelled;
    }

    private void addRelations(final PokedexEntry parent, final Set<PokedexEntry> related)
    {
        if (related.contains(parent)) return;
        related.add(parent);
        if (!related.contains(parent.getChild())) this.addRelations(parent.getChild(), related);
        for (final EvolutionData d : parent.evolutions)
            this.addRelations(d.evolution, related);
    }

    public void setMaker(final PokedexEntry maker)
    {
        this.maker = maker;
        this.mutations.clear();
        this.related.clear();
        this.addRelations(maker, this.related);
    }

    @Override
    public void setPos(final BlockPos pos)
    {
        if (this.burrow == null)
        {
            // In this case, this is called from the very initial setting of the
            // burrow, so we will try to make a burrow, and then adjust the
            // location so that the nest is on the floor of the burrow made.
            // Pick a room size based on the biggest pokemob in the related
            // list.
            if (this.related.isEmpty()) throw new IllegalStateException("No related mobs? " + pos + " " + this.maker);

            final float height = this.related.stream().max((o1, o2) -> Float.compare(o1.height, o2.height))
                    .get().height;
            float size = height * 2 + 1;
            size = Math.max(3, size);
            final float direction = new Random().nextInt(360);
            this.burrow = new Room(direction, size);
            this.burrow.setCenter(pos.below((int) Math.ceil(size + 2)), size, direction);
            this.burrow.started = true;
        }
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT nbt = new CompoundNBT();
        nbt.putString("maker", this.maker.getTrimmedName());
        nbt.put("burrow", this.burrow.serializeNBT());
        final ListNBT eggs = new ListNBT();
        this.eggs.forEach(uuid ->
        {
            final CompoundNBT tag = new CompoundNBT();
            tag.putUUID("id", uuid);
            eggs.add(tag);
        });
        nbt.put("eggs", eggs);
        final ListNBT mobs = new ListNBT();
        this.mobs.forEach(uuid ->
        {
            final CompoundNBT tag = new CompoundNBT();
            tag.putUUID("id", uuid);
            mobs.add(tag);
        });
        nbt.put("mobs", mobs);
        final ListNBT muts = new ListNBT();
        this.mutations.forEach(entry ->
        {
            muts.add(StringNBT.valueOf(entry.getTrimmedName()));
        });
        nbt.put("mutated", muts);
        return nbt;
    }

    @Override
    public void onTickEnd(final ServerWorld world)
    {
        IWorldTickListener.super.onTickEnd(world);
    }

    @Override
    public void onBroken(final ServerWorld world)
    {
        IInhabitable.super.onBroken(world);
    }

    private List<IPokemob> cleanAndCollectMobs(final ServerWorld world)
    {
        final List<IPokemob> pokemobs = Lists.newArrayList();
        this.mobs.removeIf(uuid ->
        {
            final Entity mob = world.getEntity(uuid);
            if (mob == null || !(mob instanceof MobEntity)) return true;
            if (!this.canEnterHabitat((MobEntity) mob)) return true;
            pokemobs.add(CapabilityPokemob.getPokemobFor(mob));
            return false;
        });
        return pokemobs;
    }

    @Override
    public void onTick(final ServerWorld world)
    {
        final long time = Tracker.instance().getTick();

        int x, y, z;
        x = this.burrow.getCenter().getX();
        y = this.burrow.getCenter().getY();
        z = this.burrow.getCenter().getZ();

        final boolean playerNear = !world.getPlayers(p -> p.distanceToSqr(x, y, z) < PokecubeCore
                .getConfig().cullDistance).isEmpty();

        final Random rng = new Random();
        // Lets make the eggs not hatch for now,
        // This also removes hatched/removed eggs
        this.eggs.removeIf(uuid ->
        {
            final Entity mob = world.getEntity(uuid);
            if (!(mob instanceof EntityPokemobEgg) || !mob.isAddedToWorld()) return true;
            final EntityPokemobEgg egg = (EntityPokemobEgg) mob;
            if (this.mobs.size() > PokecubeCore.getConfig().nestMobNumber || !playerNear) egg.setAge(-100);
            else if (egg.getAge() < -100) egg.setAge(-rng.nextInt(100));
            return false;
        });

        if (!playerNear) return;

        if (time % 20 == 0)
        {
            final List<IPokemob> pokemobs = this.cleanAndCollectMobs(world);
            if (this.burrow.shouldDig(time))
            {
                final CompoundNBT tag = new CompoundNBT();
                tag.putBoolean("dig", true);
                for (final IPokemob pokemob : pokemobs)
                {
                    final Brain<?> brain = pokemob.getEntity().getBrain();
                    if (!brain.hasMemoryValue(BurrowTasks.JOB_INFO)) brain.setMemory(BurrowTasks.JOB_INFO, tag);
                }
            }

            final TileEntity tile = world.getBlockEntity(this.burrow.getCenter());

            if (tile instanceof NestTile)
            {
                ((NestTile) tile).residents.removeIf(p ->
                {
                    if (p == null) return true;
                    if (!p.getEntity().isAlive()) return true;
                    if (!p.getEntity().inChunk) return true;
                    return false;
                });
                ((NestTile) tile).residents.forEach(p -> this.mobs.add(p.getEntity().getUUID()));
            }

            // Here we cleanup the burrow, and see if any other mobs of similar
            // sizes will fit.
            if (this.mobs.isEmpty() && this.eggs.isEmpty())
            {
                final List<EntityType<?>> types = Lists.newArrayList(EntityTypeTags.bind(IMoveConstants.BURROWS
                        .toString()).getValues());
                Collections.shuffle(types);
                final Biome b = world.getBiome(this.burrow.getCenter());
                selection:
                for (final EntityType<?> t : types)
                {
                    final IPokemob pokemob = CapabilityPokemob.getPokemobFor(t.create(world));
                    if (pokemob != null)
                    {
                        final PokedexEntry entry = pokemob.getPokedexEntry();

                        final SpawnData spawn = entry.getSpawnData();
                        if (spawn == null) continue;
                        if (!spawn.isValid(b)) continue;

                        if (this.related.contains(entry))
                        {
                            this.setMaker(entry);
                            break selection;
                        }
                        final Set<PokedexEntry> relations = Sets.newHashSet();
                        this.addRelations(entry, relations);
                        final float height = relations.stream().max((o1, o2) -> Float.compare(o1.height, o2.height))
                                .get().height;
                        float size = height * 2 + 1;
                        size = Math.max(3, size);
                        if (size <= this.burrow.getSize())
                        {
                            this.setMaker(entry);
                            break selection;
                        }
                    }
                }
            }

            // If we have less than 3 eggs, make one from one of the random
            // related mobs.
            if (this.eggs.size() < 3 && rng.nextDouble() < PokecubeCore.getConfig().nestEggRate)
            {
                final List<PokedexEntry> entries = Lists.newArrayList(this.related);
                Collections.shuffle(entries);
                PokedexEntry entry = entries.get(0).getChild();
                if (entries.size() > 1)
                {
                    final PokedexEntry other = entries.get(1);
                    final SpeciesGene geneA = new SpeciesGene();
                    SpeciesInfo info = geneA.getValue();
                    info.entry = entry;
                    final SpeciesGene geneB = new SpeciesGene();
                    info = geneB.getValue();
                    info.entry = other;
                    final SpeciesGene newGene = (SpeciesGene) geneA.interpolate(geneB);
                    info = newGene.getValue();
                    entry = info.entry;
                    if (!this.related.contains(entry)) this.addRelations(entry, this.mutations);
                }
                final BlockPos pos = this.burrow.getCenter();
                if (world.isEmptyBlock(pos.above()))
                {
                    final EntityPokemobEgg egg = NestTile.spawnEgg(entry, pos, world, false);
                    if (egg != null) this.eggs.add(egg.getUUID());
                }
            }
        }
    }

    @Override
    public void deserializeNBT(final CompoundNBT nbt)
    {
        this.setMaker(Database.getEntry(nbt.getString("maker")));
        this.burrow = new Room();
        this.burrow.deserializeNBT(nbt.getCompound("burrow"));
        this.eggs.clear();
        this.mobs.clear();
        this.mutations.clear();
        final int compoundId = 10;
        final ListNBT eggs = nbt.getList("eggs", compoundId);
        for (int i = 0; i < eggs.size(); ++i)
        {
            final CompoundNBT tag = eggs.getCompound(i);
            this.eggs.add(tag.getUUID("id"));
        }
        final ListNBT mobs = nbt.getList("mobs", compoundId);
        for (int i = 0; i < mobs.size(); ++i)
        {
            final CompoundNBT tag = mobs.getCompound(i);
            this.mobs.add(tag.getUUID("id"));
        }
        final int stringId = 8;
        final ListNBT muts = nbt.getList("mutated", stringId);
        for (int i = 0; i < muts.size(); ++i)
            this.mutations.add(Database.getEntry(muts.getString(i)));
    }

    @Override
    public void onExitHabitat(final MobEntity mob)
    {
    }

    @Override
    public boolean onEnterHabitat(final MobEntity mob)
    {
        if (this.canEnterHabitat(mob))
        {
            this.mobs.add(mob.getUUID());
            mob.restrictTo(this.burrow.getCenter(), 64);
        }
        return false;
    }

    @Override
    public boolean canEnterHabitat(final MobEntity mob)
    {
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
        if (pokemob == null) return false;
        return this.valid.test(pokemob.getPokedexEntry());
    }

    @Override
    public ResourceLocation getKey()
    {
        return BurrowTasks.BURROWLOC;
    }

}
