package pokecube.core.world.gen.jigsaw;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.BiConsumer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import com.mojang.datafixers.util.Pair;

import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.StructureMode;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.DimensionType;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.jigsaw.EmptyJigsawPiece;
import net.minecraft.world.gen.feature.jigsaw.IJigsawDeserializer;
import net.minecraft.world.gen.feature.jigsaw.JigsawManager;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern.PlacementBehaviour;
import net.minecraft.world.gen.feature.jigsaw.JigsawPiece;
import net.minecraft.world.gen.feature.jigsaw.SingleJigsawPiece;
import net.minecraft.world.gen.feature.structure.AbstractVillagePiece;
import net.minecraft.world.gen.feature.structure.IStructurePieceType;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.structure.Structures;
import net.minecraft.world.gen.feature.template.AlwaysTrueRuleTest;
import net.minecraft.world.gen.feature.template.BlockIgnoreStructureProcessor;
import net.minecraft.world.gen.feature.template.BlockMatchRuleTest;
import net.minecraft.world.gen.feature.template.JigsawReplacementStructureProcessor;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.RandomBlockMatchRuleTest;
import net.minecraft.world.gen.feature.template.RuleEntry;
import net.minecraft.world.gen.feature.template.RuleStructureProcessor;
import net.minecraft.world.gen.feature.template.StructureProcessor;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.Event.Result;
import pokecube.core.PokecubeCore;
import pokecube.core.database.PokedexEntryLoader;
import pokecube.core.database.PokedexEntryLoader.SpawnRule;
import pokecube.core.database.SpawnBiomeMatcher;
import pokecube.core.database.SpawnBiomeMatcher.SpawnCheck;
import pokecube.core.database.worldgen.WorldgenHandler.JigSawConfig;
import pokecube.core.database.worldgen.WorldgenHandler.JigSawPool;
import pokecube.core.events.StructureEvent;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.core.world.gen.template.ExtendedRuleProcessor;
import pokecube.core.world.gen.template.FillerProcessor;
import pokecube.core.world.gen.template.PokecubeStructureProcessor;

public class JigsawPieces
{
    public static final IStructurePieceType CSP = CustomJigsawPiece::new;

    public static final RuleEntry PATHTOOAK    = new RuleEntry(new BlockMatchRuleTest(Blocks.GRASS_PATH),
            new BlockMatchRuleTest(Blocks.WATER), Blocks.OAK_PLANKS.getDefaultState());
    public static final RuleEntry PATHTOGRASS  = new RuleEntry(new RandomBlockMatchRuleTest(Blocks.GRASS_PATH, 0.05F),
            AlwaysTrueRuleTest.INSTANCE, Blocks.GRASS_BLOCK.getDefaultState());
    public static final RuleEntry GRASSTOWATER = new RuleEntry(new BlockMatchRuleTest(Blocks.GRASS_BLOCK),
            new BlockMatchRuleTest(Blocks.WATER), Blocks.WATER.getDefaultState());
    public static final RuleEntry DIRTTOWATER  = new RuleEntry(new BlockMatchRuleTest(Blocks.DIRT),
            new BlockMatchRuleTest(Blocks.WATER), Blocks.WATER.getDefaultState());

    public static final RuleStructureProcessor RULES = new ExtendedRuleProcessor(ImmutableList.of(
            JigsawPieces.PATHTOOAK, JigsawPieces.GRASSTOWATER, JigsawPieces.DIRTTOWATER, JigsawPieces.PATHTOGRASS));

    public static final Map<String, JigSawPool>          pools    = Maps.newHashMap();
    public static final Map<String, JigsawPatternCustom> patterns = Maps.newHashMap();

    private static final Set<JigSawConfig> toInitialze = Sets.newConcurrentHashSet();

    public static final Map<DimensionType, Set<BlockPos>> sent_events = Maps.newConcurrentMap();

    private static boolean shouldApply(final BlockPos pos, final IWorld worldIn)
    {
        Set<BlockPos> poses = JigsawPieces.sent_events.get(worldIn.getDimension().getType());
        if (poses == null) JigsawPieces.sent_events.put(worldIn.getDimension().getType(), poses = Sets.newHashSet());
        return !poses.contains(pos.toImmutable());
    }

    private static void apply(final BlockPos pos, final IWorld worldIn)
    {
        Set<BlockPos> poses = JigsawPieces.sent_events.get(worldIn.getDimension().getType());
        if (poses == null) JigsawPieces.sent_events.put(worldIn.getDimension().getType(), poses = Sets.newHashSet());
        poses.add(pos.toImmutable());
    }

    public static void initPool(final JigSawPool pool)
    {
        JigsawPieces.initPool(pool, (p, t) ->
        { // Do nothing by default.
        }, ImmutableList.of(PokecubeStructureProcessor.PROCESSOR, JigsawPieces.RULES));
    }

    public static void initPool(final JigSawPool pool, final BiConsumer<SingleOffsetPiece, JsonObject> proc,
            final List<StructureProcessor> rules)
    {
        JigsawPieces.pools.put(pool.name, pool);
        JigsawPieces.patterns.put(pool.name, JigsawPieces.registerPart(pool, proc, rules));
    }

    public static void initStructure(final ChunkGenerator<?> chunk_gen, final TemplateManager templateManagerIn,
            final BlockPos pos, final List<StructurePiece> parts, final SharedSeedRandom rand,
            final JigSawConfig struct, final Biome biome)
    {
        Structures.init();
        final ResourceLocation key = new ResourceLocation(struct.root);
        final JigsawAssmbler assembler = new JigsawAssmbler();
        boolean built = assembler.build(key, struct.size, CustomJigsawPiece::new, chunk_gen, templateManagerIn, pos,
                parts, rand, biome);
        int n = 0;
        while (!built && n++ < 20)
        {
            parts.clear();
            built = assembler.build(key, struct.size, CustomJigsawPiece::new, chunk_gen, templateManagerIn, pos, parts,
                    rand, biome);
        }
        if (!built) PokecubeCore.LOGGER.warn("Failed to complete a structure at " + pos);

    }

    private static JigsawPatternCustom registerPart(final JigSawPool part,
            final BiConsumer<SingleOffsetPiece, JsonObject> proc, final List<StructureProcessor> rules)
    {
        final PlacementBehaviour behaviour = part.rigid ? PlacementBehaviour.RIGID
                : PlacementBehaviour.TERRAIN_MATCHING;
        final List<Pair<JigsawPiece, Integer>> parts = Lists.newArrayList();
        final String subbiome = part.biomeType;
        for (String option : part.options)
        {
            final String[] args = option.split(";");
            Integer second = 1;
            PlacementBehaviour place = behaviour;
            JsonObject thing = null;
            if (args.length > 1) try
            {
                thing = PokedexEntryLoader.gson.fromJson(args[1], JsonObject.class);
                if (thing.has("weight")) second = thing.get("weight").getAsInt();
                if (thing.has("rigid")) place = thing.get("rigid").getAsBoolean() ? PlacementBehaviour.RIGID
                        : PlacementBehaviour.TERRAIN_MATCHING;
            }
            catch (final Exception e)
            {
                PokecubeCore.LOGGER.error("Error parsing json for {}", args[1]);
                PokecubeCore.LOGGER.error(e);
            }
            option = args[0];
            if (option.equals("empty")) parts.add(Pair.of(EmptyJigsawPiece.INSTANCE, second));
            else parts.add(Pair.of(new SingleOffsetPiece(part, option, rules, place, subbiome).process(thing, proc),
                    second));
        }
        final JigsawPatternCustom pattern = new JigsawPatternCustom(part, parts, behaviour);
        // Register the buildings
        JigsawManager.REGISTRY.register(pattern);
        return pattern;
    }

    public static void registerJigsaw(final JigSawConfig jigsaw)
    {
        JigsawPieces.toInitialze.add(jigsaw);
    }

    public static void finializeJigsaws()
    {
        for (final JigSawConfig jigsaw : JigsawPieces.toInitialze)
        {
            final JigsawPatternCustom pattern = JigsawPieces.patterns.get(jigsaw.root);
            if (pattern != null)
            {
                pattern.neededChildren.addAll(jigsaw.needed_once);
                pattern.jigsaw = jigsaw;
            }
            else PokecubeCore.LOGGER.error("Attempting to register a jigsaw with an un-known root: {}", jigsaw.root);
        }
    }

    public static class SingleOffsetPiece extends SingleJigsawPiece
    {
        public static IJigsawDeserializer PIECETYPE = IJigsawDeserializer.register("pokecube:sop",
                SingleOffsetPiece::new);

        public final JigSawPool pool;
        public int              offset = 1;
        public int              dy     = 0;
        private boolean         ignoreAir;

        public String flag = "";

        public String  subbiome;
        public String  spawnReplace = "";
        public boolean isSpawn;

        private SpawnBiomeMatcher _spawn = null;
        public MutableBoundingBox mask   = null;
        public PlacementSettings  last_used;

        private boolean maskCheck = false;

        public SingleOffsetPiece(final Dynamic<?> dyn)
        {
            super(dyn);
            final String poolstr = dyn.get("pool").asString("");
            this.pool = PokedexEntryLoader.gson.fromJson(poolstr, JigSawPool.class);
            this.subbiome = dyn.get("subbiome").asString("");
            this.flag = dyn.get("flag").asString("");
            this.offset = dyn.get("offset").asInt(0);
            this.dy = dyn.get("dy").asInt(0);
            this.ignoreAir = dyn.get("ignoreAir").asBoolean(false);
        }

        public SingleOffsetPiece(final JigSawPool pool, final String location,
                final List<StructureProcessor> processors, final PlacementBehaviour type, final String subbiome)
        {
            super(location, processors, type);
            this.subbiome = subbiome;
            this.ignoreAir = pool.ignoreAir;
            this.pool = pool;
        }

        @Override
        public <T> Dynamic<T> serialize0(final DynamicOps<T> ops)
        {
            final Map<T, T> map = Maps.newHashMap();
            map.put(ops.createString("pool"), ops.createString(PokedexEntryLoader.gson.toJson(this.pool)));
            map.put(ops.createString("subbiome"), ops.createString(this.subbiome));
            map.put(ops.createString("flag"), ops.createString(this.flag));
            map.put(ops.createString("location"), ops.createString(this.location.toString()));
            map.put(ops.createString("ignoreAir"), ops.createBoolean(this.ignoreAir));
            map.put(ops.createString("dy"), ops.createInt(this.dy));
            map.put(ops.createString("offset"), ops.createInt(this.offset));
            map.put(ops.createString("processors"), ops.createList(this.processors.stream().map((proc) ->
            {
                return proc.serialize(ops).getValue();
            })));
            return new Dynamic<>(ops, ops.createMap(ImmutableMap.copyOf(map)));
        }

        @Override
        public IJigsawDeserializer getType()
        {
            return SingleOffsetPiece.PIECETYPE;
        }

        private SpawnBiomeMatcher fromJson(final JsonElement rule)
        {
            try
            {
                return new SpawnBiomeMatcher(PokedexEntryLoader.gson.fromJson(rule, SpawnRule.class));
            }
            catch (final Exception e)
            {
                PokecubeCore.LOGGER.error("Error parsing spawn for {}", rule);
                PokecubeCore.LOGGER.error(e);
                return null;
            }
        }

        public SingleOffsetPiece process(final JsonObject thing, final BiConsumer<SingleOffsetPiece, JsonObject> proc)
        {
            if (thing != null) try
            {
                if (thing.has("flag")) this.flag = thing.get("flag").getAsString();
                if (thing.has("ignoreAir")) this.ignoreAir = thing.get("ignoreAir").getAsBoolean();
                if (thing.has("subbiome")) this.subbiome = thing.get("subbiome").getAsString();
                if (thing.has("spawn")) this._spawn = this.fromJson(thing.get("spawn"));
                if (thing.has("dy")) this.dy = thing.get("dy").getAsInt();
                proc.accept(this, thing);
            }
            catch (final Exception e)
            {
                PokecubeCore.LOGGER.error("Error parsing values for {}", thing);
                PokecubeCore.LOGGER.error(e);
            }
            return this;
        }

        public boolean isValidPos(final SpawnCheck check)
        {
            if (this._spawn != null)
            {
                final boolean valid = this._spawn.matches(check);
                return valid;
            }
            return true;
        }

        @Override
        public int getGroundLevelDelta()
        {
            // This is the ground level delta.
            return this.offset + this.dy;
        }

        @Override
        public PlacementSettings createPlacementSettings(final Rotation p_214860_1_,
                final MutableBoundingBox p_214860_2_)
        {
            this.last_used = new PlacementSettings();
            this.last_used.setBoundingBox(p_214860_2_);
            this.last_used.setRotation(p_214860_1_);
            this.last_used.func_215223_c(true);
            this.last_used.setIgnoreEntities(false);
            if (this.pool.filler) this.last_used.addProcessor(FillerProcessor.PROCESSOR);
            if (this.ignoreAir) this.last_used.addProcessor(BlockIgnoreStructureProcessor.AIR_AND_STRUCTURE_BLOCK);
            else this.last_used.addProcessor(BlockIgnoreStructureProcessor.STRUCTURE_BLOCK);
            this.last_used.addProcessor(JigsawReplacementStructureProcessor.INSTANCE);
            this.processors.forEach(this.last_used::addProcessor);
            this.getPlacementBehaviour().getStructureProcessors().forEach(this.last_used::addProcessor);
            return this.last_used;
        }

        public Template getTemplate(final TemplateManager manager)
        {
            return manager.getTemplateDefaulted(this.location);
        }

        @Override
        public boolean place(final TemplateManager manager, final IWorld worldIn, final ChunkGenerator<?> p_225575_3_,
                final BlockPos pos, final Rotation rotation, final MutableBoundingBox box, final Random rand)
        {

            final Template template = this.getTemplate(manager);
            final PlacementSettings placementsettings = this.createPlacementSettings(rotation, box);

            if (!template.addBlocksToWorld(worldIn, pos, placementsettings, 18)) return false;
            else
            {
                final StructureEvent.BuildStructure event = new StructureEvent.BuildStructure(box, worldIn,
                        this.location.toString(), placementsettings);
                event.setBiomeType(this.subbiome);
                MinecraftForge.EVENT_BUS.post(event);

                this.maskCheck = this.mask != null && this.mask.intersectsWith(box);

                // This section is added what is modifed in, it copies the
                // structure block processing from the template structures, so
                // that we can also handle metadata on marker blocks.
                for (final Template.BlockInfo template$blockinfo : template.func_215381_a(pos, placementsettings,
                        Blocks.STRUCTURE_BLOCK))
                    if (template$blockinfo.nbt != null)
                    {
                        final StructureMode structuremode = StructureMode.valueOf(template$blockinfo.nbt.getString(
                                "mode"));
                        if (structuremode == StructureMode.DATA) this.handleDataMarker(template$blockinfo.nbt.getString(
                                "metadata"), template$blockinfo.pos, worldIn, rand, box);
                    }
                // Back to the stuff that the superclass does.
                for (final Template.BlockInfo template$blockinfo : Template.processBlockInfos(template, worldIn, pos,
                        placementsettings, this.getDataMarkers(manager, pos, rotation, false)))
                    this.handleDataMarker(worldIn, template$blockinfo, pos, rotation, rand, box);
                return true;
            }
        }

        protected void handleDataMarker(String function, final BlockPos pos, final IWorld worldIn, final Random rand,
                final MutableBoundingBox sbb)
        {
            this.isSpawn = this.isSpawn && !PokecubeSerializer.getInstance().hasPlacedProf();
            if (this.isSpawn && this.maskCheck && this.spawnReplace.equals(function))
            {
                PokecubeCore.LOGGER.info("Overriding an entry as a professor at " + pos);
                function = PokecubeCore.getConfig().professor_override;
                PokecubeSerializer.getInstance().setPlacedProf();
            }
            if (function.startsWith("pokecube:chest:"))
            {
                final BlockPos blockpos = pos.down();
                worldIn.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
                final ResourceLocation key = new ResourceLocation(function.replaceFirst("pokecube:chest:", ""));
                if (sbb.isVecInside(blockpos)) LockableLootTileEntity.setLootTable(worldIn, rand, blockpos, key);
            }
            else if (function.startsWith("Chest "))
            {
                final BlockPos blockpos = pos.down();
                worldIn.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
                final ResourceLocation key = new ResourceLocation(function.replaceFirst("Chest ", ""));
                if (sbb.isVecInside(blockpos)) LockableLootTileEntity.setLootTable(worldIn, rand, blockpos, key);
            }
            else if (JigsawPieces.shouldApply(pos, worldIn))
            {
                PokecubeCore.LOGGER.info(function);
                final Event event = new StructureEvent.ReadTag(function.trim(), pos, worldIn, rand, sbb);
                MinecraftForge.EVENT_BUS.post(event);
                if (event.getResult() == Result.ALLOW) JigsawPieces.apply(pos, worldIn);
            }
        }
    }

    public static class CustomJigsawPiece extends AbstractVillagePiece
    {
        public CustomJigsawPiece(final TemplateManager manager, final JigsawPiece piece, final BlockPos pos,
                final int groundLevelDelta, final Rotation dir, final MutableBoundingBox box)
        {
            super(JigsawPieces.CSP, manager, piece, pos, groundLevelDelta, dir, box);
        }

        public CustomJigsawPiece(final TemplateManager manager, final CompoundNBT tag)
        {
            super(manager, tag, JigsawPieces.CSP);
        }
    }
}
