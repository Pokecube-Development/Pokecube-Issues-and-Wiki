package pokecube.mobs.moves.world;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import pokecube.core.PokecubeCore;
import pokecube.core.database.resources.PackFinder;
import pokecube.core.database.util.DataHelpers;
import pokecube.core.database.util.DataHelpers.ResourceData;
import pokecube.core.handlers.events.MoveEventsHandler;
import pokecube.core.interfaces.IMoveAction;
import pokecube.core.interfaces.IPokemob;
import thut.api.Tracker;
import thut.api.item.ItemList;
import thut.api.maths.Vector3;
import thut.api.terrain.BiomeDatabase;
import thut.api.util.JsonUtil;

public class ActionNaturePower implements IMoveAction
{
    private static final List<ConfigChanger> CHANGERS = Lists.newArrayList();

    public static final NatureData INSTANCE = new NatureData("database/nature_power/");

    public static class NatureData extends ResourceData
    {
        private final String tagPath;

        public boolean validLoad = false;

        public NatureData(String key)
        {
            super(key);
            this.tagPath = key;
            DataHelpers.addDataType(this);
        }

        @Override
        public void reload(AtomicBoolean valid)
        {
            this.validLoad = false;
            final String path = new ResourceLocation(this.tagPath).getPath();
            final Collection<ResourceLocation> resources = PackFinder.getJsonResources(path);
            this.validLoad = !resources.isEmpty();
            CHANGERS.clear();
            preLoad();
            resources.forEach(l -> this.loadFile(l));
            CHANGERS.sort(Comparator.comparingInt(c -> c.priority));
            if (this.validLoad)
            {
                PokecubeCore.LOGGER.debug("Loaded Nature Power effects.");
                valid.set(true);
            }
        }

        private void loadFile(final ResourceLocation l)
        {
            try
            {
                // This one we just take the first resourcelocation. If someone
                // wants to edit an existing one, it means they are most likely
                // trying to remove default behaviour. They can add new things
                // by
                // just adding another json file to the correct package.
                InputStream res = PackFinder.getStream(l);
                final Reader reader = new InputStreamReader(res);

                final ConfigChanger temp = JsonUtil.gson.fromJson(reader, ConfigChanger.class);
                if (!confirmNew(temp, l))
                {
                    reader.close();
                    return;
                }
                CHANGERS.add(temp);
                reader.close();
            }
            catch (final Exception e)
            {
                // Might not be valid, so log and skip in that case.
                PokecubeCore.LOGGER.error("Error with resources in {}", l);
                PokecubeCore.LOGGER.error(e);
            }
        }
    }

    public static class ConfigChanger implements IBiomeChanger
    {
        public static class Column
        {
            int dy = 0;
            float threshold = 0.5f;
            List<String> ids = Lists.newArrayList();
            List<Float> weights = Lists.newArrayList();

            float _max_ = -1;

            List<ResourceLocation> _ids_ = Lists.newArrayList();

            public double max()
            {
                if (_max_ < 0)
                {
                    _max_ = 0;
                    for (float f : weights) _max_ += f;
                }
                return _max_;
            }

            public float apply(BlockPos pos, ServerLevel world)
            {
                if (_ids_.isEmpty()) ids.forEach(s -> _ids_.add(new ResourceLocation(s)));

                float m = 0;
                for (int i = 0; i < _ids_.size(); i++)
                {
                    int y = i + dy;
                    BlockPos p2 = pos.above(y);
                    boolean matches = ItemList.is(_ids_.get(i), world.getBlockState(p2));
                    Float weight = weights.get(i);
                    if (matches) m += weight;
                    else if (weight >= 1) return 0;
                }
                return m;
            }
        }

        public int priority = 100;
        public String biome;
        public List<Column> columns = Lists.newArrayList();
        public List<String> required = Lists.newArrayList();

        ResourceLocation _biome_;
        List<ResourceLocation> _required_ = Lists.newArrayList();
        ServerLevel _level_;

        final Predicate<BlockPos> _predicate_ = t -> {

            final ResourceKey<Biome> here = BiomeDatabase.getKey(_level_.getBiome(t));
            // Already the same biome, no apply!
            if (here.location().equals(_biome_)) return false;

            for (Column c : columns)
            {
                float weight = c.apply(t, _level_);
                if (weight >= c.max() * c.threshold)
                {
                    return true;
                }
            }
            return false;
        };

        final Predicate<PointChecker> _has_required_ = c -> {
            if (_required_.size() == 0) return true;

            for (Vector3 v : c.blocks)
            {
                BlockPos p = v.getPos();
                BlockState s = _level_.getBlockState(p);
                for (ResourceLocation r : _required_) if (ItemList.is(r, s)) return true;
                p = p.above();
                s = _level_.getBlockState(p);
                for (ResourceLocation r : _required_) if (ItemList.is(r, s)) return true;
            }
            return false;
        };

        @Override
        public boolean apply(BlockPos pos, ServerLevel world)
        {
            this._level_ = world;
            if (_biome_ == null)
            {
                _biome_ = new ResourceLocation(biome);
                required.forEach(s -> _required_.add(new ResourceLocation(s)));
            }
            ResourceKey<Biome> KEY = ResourceKey.create(Registry.BIOME_REGISTRY, _biome_);
            final PointChecker checker = new PointChecker(world, Vector3.getNewVector().set(pos), _predicate_);
            checker.checkPoints();
            System.out.println("Checking for " + _biome_);
            if (!_has_required_.test(checker))
            {
                System.out.println("failed required for " + _biome_);
                return false;
            }
            return ActionNaturePower.applyChecker(checker, world, KEY);
        }
    }

    /**
     * Implementers of this interface must have a public constructor that takes
     * no arguments.
     */
    public abstract interface IBiomeChanger
    {
        /**
         * This method should check whether it should apply a biome change, and
         * if it should, it should do so, then return true. It should return
         * false if it does not change anything. Only the first of these to
         * return true will be used, so if you need to re-order things, reorder
         * ActionNaturePower.changer_classes accordingly.
         */
        public boolean apply(BlockPos pos, ServerLevel world);
    }

    /**
     * Very basic tree finder, it finds all connected blocks that match the
     * validCheck predicate.
     */
    public static class PointChecker
    {
        Level world;
        Vector3 centre;
        // we use lists here for faster iteration, sets are faster lookups for
        // contains, but lists iterate more GC friendly.
        List<Vector3> blocks = new LinkedList<>();
        List<Vector3> checked = new LinkedList<>();
        List<BlockState> states = Lists.newArrayList();
        final Predicate<BlockPos> validCheck;
        boolean yaxis = false;
        int maxRSq = 8 * 8;

        public PointChecker(final Level world, final Vector3 pos, final Predicate<BlockPos> validator)
        {
            this.world = world;
            this.centre = pos;
            this.validCheck = validator;
        }

        public void checkPoints()
        {
            this.populateList(this.centre);
        }

        public void clear()
        {
            this.blocks.clear();
            this.checked.clear();
        }

        private boolean nextPoint(final Vector3 prev, final List<Vector3> tempList)
        {
            boolean ret = false;
            final Vector3 temp = Vector3.getNewVector();
            // Check the connected blocks, see if they match predicate, if they
            // do, add them to the list. This also checks diagonally connected
            // blocks.
            for (int i = -1; i <= 1; i++) for (int j = -1; j <= 1; j++)
                // If yaxis, also check vertical connections, for
                // naturepower, we usually only care about horizontal.
                if (this.yaxis) for (int k = -1; k <= 1; k++)
            {
                temp.set(prev).addTo(i, k, j);
                if (this.validCheck.test(temp.getPos())) if (temp.distToSq(this.centre) <= this.maxRSq)
                {
                    tempList.add(temp.copy());
                    this.states.add(temp.getBlockState(this.world));
                    ret = true;
                }
            }
                else
            {
                temp.set(prev).addTo(i, 0, j);
                if (this.validCheck.test(temp.getPos())) if (temp.distToSq(this.centre) <= this.maxRSq)
                {
                    tempList.add(temp.copy());
                    this.states.add(temp.getBlockState(this.world));
                    ret = true;
                }
            }
            this.checked.add(prev);
            return ret;
        }

        private void populateList(final Vector3 base)
        {
            // Add the initial block.
            this.blocks.add(base);
            // Loop untill no new blocks have been added.
            while (this.checked.size() < this.blocks.size())
            {
                final List<Vector3> toAdd = new ArrayList<>();
                // Add all connecting blocks that match, unless they have
                // already been checked.
                for (final Vector3 v : this.blocks) if (!this.checked.contains(v)) this.nextPoint(v, toAdd);
                // Add any blocks that are new to the list.
                for (final Vector3 v : toAdd) if (!this.blocks.contains(v)) this.blocks.add(v);
            }
        }
    }

    /**
     * This class will reset the biomes back to whatever worldgen says they
     * should be, it goes out 8 blocks, and checks what the biome is, what it
     * should be, and sets it back. It must be used on a diamond block.
     */
    public static class ResetChanger implements IBiomeChanger
    {
        public ResetChanger()
        {}

        @Override
        public boolean apply(final BlockPos pos, final ServerLevel world)
        {
            if (world.getBlockState(pos).getBlock() != Blocks.DIAMOND_BLOCK) return false;
            boolean mod = false;
            final Vector3 vec = Vector3.getNewVector().set(pos);

            ChunkGenerator generator = world.getChunkSource().getGenerator();
            Climate.Sampler sampler = generator.climateSampler();

            for (int i = -8; i <= 8; i++) for (int j = -8; j <= 8; j++) for (int k = -8; k <= 8; k++)
            {
                vec.set(pos).addTo(i, j, k);
                final Biome here = vec.getBiome(world);
                int qx = QuartPos.fromBlock(vec.intX());
                int qy = QuartPos.fromBlock(vec.intY());
                int qz = QuartPos.fromBlock(vec.intZ());
                final Biome natural = world.getChunkSource().getGenerator().getBiomeSource().getNoiseBiome(qx, qy, qz,
                        sampler);
                if (natural != here)
                {
                    vec.setBiome(natural, world);
                    mod = true;
                }
            }
            final ServerLevel sWorld = world;
            sWorld.getChunkSource().blockChanged(pos);
            return mod;
        }
    }

    public static boolean applyChecker(final PointChecker checker, final Level world, final ResourceKey<Biome> key)
    {
        // Check if > 1 as it will always at least contain the center.
        if (checker.blocks.size() > 1)
        {
            final Set<ChunkAccess> affected = Sets.newHashSet();
            final Biome biome = BiomeDatabase.getBiome(key);
            final ServerLevel sWorld = (ServerLevel) world;
            sWorld.getServer().getPlayerList();
            // This needs to use the chunk manager and send the chunkto watching
            // players.
            int minY = Integer.MAX_VALUE;
            int maxY = Integer.MIN_VALUE;
            // Apply the biome to all the locations.
            for (final Vector3 loc : checker.blocks)
            {
                loc.setBiome(biome, world);
                affected.add(world.getChunk(loc.getPos()));
                sWorld.getChunkSource().blockChanged(loc.getPos());
                minY = Math.min(minY, loc.intY() / 16);
                maxY = Math.max(maxY, loc.intY() / 16);
            }
            return true;
        }
        return false;
    }

    /**
     * This is filled with new instances of whatever is in changer_classes. It
     * will have same ordering as changer_classes, and the first of these to
     * return true for a location is the only one that will be used.
     */
    private final List<IBiomeChanger> changers = Lists.newArrayList();

    private final IBiomeChanger reset = new ResetChanger();

    public ActionNaturePower()
    {}

    @Override
    public boolean applyEffect(final IPokemob attacker, final Vector3 location)
    {
        if (attacker.inCombat()) return false;
        if (!(attacker.getOwner() instanceof ServerPlayer)) return false;
        if (!(attacker.getEntity().getLevel() instanceof ServerLevel level)) return false;
        if (!MoveEventsHandler.canAffectBlock(attacker, location, this.getMoveName())) return false;
        final long time = attacker.getEntity().getPersistentData().getLong("lastAttackTick");
        final long now = Tracker.instance().getTick();
        if (time + 20 * 3 > now) return false;
        final BlockPos pos = location.getPos();
        if (this.changers.isEmpty()) this.init();
        // Check the changers in order, and apply the first one that returns
        // true. TODO hunger cost added here.
        for (final IBiomeChanger changer : this.changers) if (changer.apply(pos, level)) return true;
        return false;
    }

    @Override
    public String getMoveName()
    {
        return "naturepower";
    }

    @Override
    public void init()
    {
        changers.addAll(CHANGERS);
        changers.add(reset);
    }
}
