package pokecube.gimmicks.nests.tasks.birds.nest;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.util.INBTSerializable;
import pokecube.api.blocks.IInhabitable;
import pokecube.api.blocks.IRepelledVolume;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.core.database.Database;
import pokecube.core.entity.genetics.genes.SpeciesGene;
import pokecube.core.eventhandlers.SpawnHandler;
import pokecube.core.init.Config;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;
import pokecube.gimmicks.nests.NestTasks;
import pokecube.gimmicks.nests.blocks.NestTile;
import pokecube.gimmicks.nests.tasks.birds.BirdTasks;
import thut.api.ThutAPI;
import thut.api.Tracker;
import thut.api.world.IWorldTickListener;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

public class BirdHab implements IInhabitable, INBTSerializable<CompoundTag>, IWorldTickListener
{
    public static BirdHab makeFor(final IPokemob pokemob, final BlockPos pos)
    {
        final BirdHab hab = new BirdHab(null);
        hab.setMaker(pokemob.getPokedexEntry());
        if (hab.related.isEmpty()) return null;
        hab.setPos(pos);
        final Predicate<Mob> filter = hab::canEnterHabitat;
        final List<Mob> mobs = pokemob.getEntity().level()
                .getEntitiesOfClass(Mob.class, AABB.ofSize(pos.getCenter(), 16, 16, 16), filter);
        for (final Mob mob : mobs) hab.mobs.add(mob.getUUID());
        return hab;
    }

    private PokedexEntry maker;

    IRepelledVolume repelled = null;

    final Set<PokedexEntry> related = Sets.newHashSet();

    final Set<PokedexEntry> mutations = Sets.newHashSet();

    Predicate<PokedexEntry> valid = e -> this.related.contains(e) || this.mutations.contains(e);

    public Set<UUID> eggs = Sets.newHashSet();
    public Set<UUID> mobs = Sets.newHashSet();
    public BlockPos pos;
    public boolean unocuppied = false;

    public PokedexEntry getMaker()
    {
        return this.maker;
    }

    public BirdHab(BlockEntity block)
    {
        if (block != null) this.pos = block.getBlockPos();
    }

    @Override
    public void updateRepelledRegion(final BlockEntity tile, final ServerLevel world)
    {
        this.pos = tile.getBlockPos();
        final AABB box = AABB.ofSize(tile.getBlockPos().getCenter(), 8, 8, 8);
        this.repelled = new SpawnHandler.AABBRegion(box, IRepelledVolume.ForbidReason.NEST);
    }

    @Override
    public IRepelledVolume getRepelledRegion(final BlockEntity tile, final ServerLevel world)
    {
        if (this.unocuppied) return this.repelled = null;
        if (this.repelled == null) this.updateRepelledRegion(tile, world);
        return this.repelled;
    }

    private void addRelations(final PokedexEntry parent, final Set<PokedexEntry> related)
    {
        if (related.contains(parent)) return;
        related.add(parent);
        if (!related.contains(parent.getChild())) this.addRelations(parent.getChild(), related);
        for (final PokedexEntry.EvolutionData d : parent.evolutions) this.addRelations(d.evolution, related);
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
        this.pos = pos;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider)
    {
        final CompoundTag nbt = new CompoundTag();
        nbt.putString("maker", this.maker.getTrimmedName());
        final ListTag eggs = new ListTag();
        this.eggs.forEach(uuid -> {
            final CompoundTag tag = new CompoundTag();
            tag.putUUID("id", uuid);
            eggs.add(tag);
        });
        nbt.put("eggs", eggs);
        final ListTag mobs = new ListTag();
        this.mobs.forEach(uuid -> {
            final CompoundTag tag = new CompoundTag();
            tag.putUUID("id", uuid);
            mobs.add(tag);
        });
        nbt.put("mobs", mobs);
        final ListTag muts = new ListTag();
        this.mutations.forEach(entry -> muts.add(StringTag.valueOf(entry.getTrimmedName())));
        nbt.put("mutated", muts);
        return nbt;
    }

    @Override
    public void onTickEnd(final ServerLevel world)
    {
        boolean canTick = world.isLoaded(this.pos);
        if (!canTick) return;
        IWorldTickListener.super.onTickEnd(world);
    }

    @Override
    public void onBroken(final ServerLevel world)
    {
        IInhabitable.super.onBroken(world);
    }

    private List<IPokemob> cleanAndCollectMobs(final ServerLevel world)
    {
        final List<IPokemob> pokemobs = Lists.newArrayList();
        this.mobs.removeIf(uuid -> {
            final Entity entity = world.getEntity(uuid);
            if (!(entity instanceof Mob mob)) return true;
            if (!this.canEnterHabitat(mob)) return true;
            pokemobs.add(PokemobCaps.getPokemobFor(mob));
            return false;
        });
        return pokemobs;
    }

    @Override
    public void onTick(final ServerLevel world)
    {
        boolean canTick = world.isLoaded(this.pos);
        if (!canTick) return;

        final long time = Tracker.instance().getTick();

        int x, y, z;
        x = this.pos.getX();
        y = this.pos.getY();
        z = this.pos.getZ();

        final boolean playerNear = !world
                .getPlayers(p -> p.distanceToSqr(x, y, z) < Config.Rules.despawnDistance(world)).isEmpty();

        final Random rng = ThutAPI.newRandom();
        // Lets make the eggs not hatch for now,
        // This also removes hatched/removed eggs
        this.eggs.removeIf(uuid -> {
            final Entity mob = world.getEntity(uuid);
            if (!(mob instanceof EntityPokemobEgg egg) || !mob.isAddedToLevel()) return true;
            if (this.mobs.size() > NestTasks.config.nestMobNumber || !playerNear) egg.setAge(-100);
            else if (egg.getAge() < -100) egg.setAge(-rng.nextInt(100));
            return false;
        });

        if (!playerNear) return;

        if (time % 20 == 0)
        {
            final BlockEntity tile = world.getBlockEntity(this.pos);
            if (tile instanceof NestTile nest)
            {
                nest.residents.forEach(p -> this.mobs.add(p.getEntity().getUUID()));
            }

            // Here we cleanup the burrow, and see if any other mobs of similar
            // sizes will fit.
            this.unocuppied = this.mobs.isEmpty() && this.eggs.isEmpty();

            // If we have less than 3 eggs, make one from one of the random
            // related mobs.
            if (!unocuppied && this.eggs.size() < 3 && rng.nextDouble() < NestTasks.config.nestEggRate)
            {
                final List<PokedexEntry> entries = Lists.newArrayList(this.related);
                Collections.shuffle(entries);
                PokedexEntry entry = entries.get(0).getChild();
                if (entries.size() > 1)
                {
                    final PokedexEntry other = entries.get(1);
                    final SpeciesGene geneA = new SpeciesGene();
                    SpeciesGene.SpeciesInfo info = geneA.getValue();
                    info.setEntry(entry);
                    final SpeciesGene geneB = new SpeciesGene();
                    info = geneB.getValue();
                    info.setEntry(other);
                    final SpeciesGene newGene = (SpeciesGene) geneA.interpolate(geneB);
                    info = newGene.getValue();
                    entry = info.getEntry();
                    if (!this.related.contains(entry)) this.addRelations(entry, this.mutations);
                }
                final BlockPos pos = this.pos;
                if (world.isEmptyBlock(pos))
                {
                    final EntityPokemobEgg egg = NestTile.spawnEgg(entry, pos, world, false);
                    if (egg != null) this.eggs.add(egg.getUUID());
                }
            }
            else if(this.unocuppied){
                // Make a smaller repelled region to apply?
            }
        }
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, final CompoundTag nbt)
    {
        this.setMaker(Database.getEntry(nbt.getString("maker")));
        this.eggs.clear();
        this.mobs.clear();
        this.mutations.clear();
        final int compoundId = 10;
        final ListTag eggs = nbt.getList("eggs", compoundId);
        for (int i = 0; i < eggs.size(); ++i)
        {
            final CompoundTag tag = eggs.getCompound(i);
            this.eggs.add(tag.getUUID("id"));
        }
        final ListTag mobs = nbt.getList("mobs", compoundId);
        for (int i = 0; i < mobs.size(); ++i)
        {
            final CompoundTag tag = mobs.getCompound(i);
            this.mobs.add(tag.getUUID("id"));
        }
        final int stringId = 8;
        final ListTag muts = nbt.getList("mutated", stringId);
        for (int i = 0; i < muts.size(); ++i) this.mutations.add(Database.getEntry(muts.getString(i)));
    }

    @Override
    public void onExitHabitat(final Mob mob)
    {}

    @Override
    public boolean onEnterHabitat(final Mob mob)
    {
        if (this.canEnterHabitat(mob))
        {
            this.mobs.add(mob.getUUID());
            mob.restrictTo(this.pos, 64);
        }
        return false;
    }

    @Override
    public boolean canEnterHabitat(final Mob mob)
    {
        final IPokemob pokemob = PokemobCaps.getPokemobFor(mob);
        if (pokemob == null) return false;
        return this.valid.test(pokemob.getPokedexEntry());
    }

    @Override
    public ResourceLocation getKey()
    {
        return BirdTasks.NESTLOC;
    }

}
