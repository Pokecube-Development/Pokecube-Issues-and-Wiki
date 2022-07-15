package pokecube.world.gen.structures.pool_elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.GravityProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.JigsawReplacementProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.Event.Result;
import pokecube.core.PokecubeCore;
import pokecube.core.events.StructureEvent;
import pokecube.core.handlers.events.EventsHandler;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.world.gen.structures.PokecubeStructures;
import pokecube.world.gen.structures.processors.MarkerToAirProcessor;
import pokecube.world.gen.structures.processors.NoWaterlogProcessor;
import pokecube.world.gen.structures.utils.ExpandedJigsawPacement;

public class ExpandedJigsawPiece extends SinglePoolElement
{
    public static Codec<ExpandedJigsawPiece> makeCodec()
    {
        return RecordCodecBuilder.create((instance) -> {
            return instance.group(SinglePoolElement.templateCodec(), SinglePoolElement.processorsCodec(),
                    StructurePoolElement.projectionCodec(),
                    Ints.CODEC.fieldOf("int_config").orElse(Ints.DEFAULT).forGetter(s -> s.int_config),
                    Bools.CODEC.fieldOf("bool_config").orElse(Bools.DEFAULT).forGetter(s -> s.bool_config),
                    Codec.STRING.fieldOf("biome_type").orElse("none").forGetter(s -> s.biome_type),
                    Codec.STRING.fieldOf("name").orElse("none").forGetter(s -> s.biome_type),
                    Codec.STRING.fieldOf("flags").orElse("").forGetter(s -> s.flags), ResourceLocation.CODEC.listOf()
                            .fieldOf("extra_pools").orElse(new ArrayList<>()).forGetter(s -> s.extra_pools))
                    .apply(instance, ExpandedJigsawPiece::new);
        });
    }

    public static final Map<ResourceKey<Level>, Set<BlockPos>> sent_events = Maps.newConcurrentMap();

    private static boolean shouldApply(final BlockPos pos, final Level worldIn)
    {
        final Set<BlockPos> poses = ExpandedJigsawPiece.sent_events.get(worldIn.dimension());
        if (poses == null) return true;
        return !poses.contains(pos.immutable());
    }

    private static void apply(final BlockPos pos, final Level worldIn)
    {
        Set<BlockPos> poses = ExpandedJigsawPiece.sent_events.get(worldIn.dimension());
        if (poses == null) ExpandedJigsawPiece.sent_events.put(worldIn.dimension(), poses = Sets.newHashSet());
        poses.add(pos.immutable());
    }

    public static class Ints
    {
        public static final Ints DEFAULT = new Ints(-1, 10, 0, -1, -1, 100);

        public static final Codec<Ints> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance
                    .group(Codec.INT.fieldOf("y_offset").orElse(-1).forGetter(s -> s.y_offset),
                            Codec.INT.fieldOf("space_below").orElse(10).forGetter(s -> s.space_below),
                            Codec.INT.fieldOf("extra_child_depth").orElse(0).forGetter(s -> s.extra_child_depth),
                            Codec.INT.fieldOf("h_clearance").orElse(-1).forGetter(s -> s.h_clearance),
                            Codec.INT.fieldOf("v_clearance").orElse(-1).forGetter(s -> s.v_clearance),
                            Codec.INT.fieldOf("priority").orElse(100).forGetter(s -> s.priority))
                    .apply(instance, Ints::new);
        });

        public final int y_offset;
        public final int space_below;
        public final int extra_child_depth;
        public final int h_clearance;
        public final int v_clearance;
        public final int priority;

        public Ints(int y_offset, int space_below, int extra_child_depth, int h_clearance, int v_clearance,
                int priority)
        {
            this.h_clearance = h_clearance;
            this.v_clearance = v_clearance;
            this.y_offset = y_offset;
            this.space_below = space_below;
            this.extra_child_depth = extra_child_depth;
            this.priority = priority;
        }
    }

    public static class Bools
    {
        public static final Bools DEFAULT = new Bools(false, false, true, false, false);

        public static final Codec<Bools> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance
                    .group(Codec.BOOL.fieldOf("ignore_air").orElse(false).forGetter(s -> s.ignore_air),
                            Codec.BOOL.fieldOf("water_terrain_match").orElse(false)
                                    .forGetter(s -> s.water_terrain_match),
                            Codec.BOOL.fieldOf("markers_to_air").orElse(true).forGetter(s -> s.markers_to_air),
                            Codec.BOOL.fieldOf("only_once").orElse(false).forGetter(s -> s.only_once),
                            Codec.BOOL.fieldOf("no_affect_noise").orElse(false).forGetter(s -> s.no_affect_noise))
                    .apply(instance, Bools::new);
        });

        public final boolean ignore_air;
        public final boolean water_terrain_match;
        public final boolean markers_to_air;
        public final boolean only_once;
        public final boolean no_affect_noise;

        public Bools(boolean ignore_air, boolean water_terrain_match, boolean markers_to_air, boolean only_once,
                boolean no_affect_noise)
        {
            this.ignore_air = ignore_air;
            this.water_terrain_match = water_terrain_match;
            this.markers_to_air = markers_to_air;
            this.only_once = only_once;
            this.no_affect_noise = no_affect_noise;
        }
    }

    public Level world;

    public final String biome_type;
    public final String name;
    public final String flags;
    public final List<ResourceLocation> extra_pools;
    public final Ints int_config;
    public final Bools bool_config;

    public final String[] _flags;

    public boolean isSpawn;
    public String spawnReplace;
    public BlockPos spawnPos;
    public BlockPos profPos;
    public boolean placedSpawn = false;

    boolean maskCheck;

    public ExpandedJigsawPiece(final Either<ResourceLocation, StructureTemplate> template,
            final Holder<StructureProcessorList> processors, final StructureTemplatePool.Projection behaviour,
            final Ints int_config, final Bools bool_config, final String biome_type, final String name,
            final String flags, List<ResourceLocation> extra_pools)
    {
        super(template, processors, behaviour);
        this.biome_type = biome_type;
        this.name = name;
        this.flags = flags;
        this.extra_pools = extra_pools;
        this._flags = flags.split(",");
        this.int_config = int_config;
        this.bool_config = bool_config;
    }

    @Override
    public int getGroundLevelDelta()
    {
        // Negative y_offset, as this is the shift of the ground, not the shift
        // of the structure!
        return -this.int_config.y_offset;
    }

    @Override
    public StructurePlaceSettings getSettings(final Rotation direction, final BoundingBox box, final boolean notJigsaw)
    {
        final StructurePlaceSettings placementsettings = new StructurePlaceSettings();
        placementsettings.setBoundingBox(box);
        placementsettings.setRotation(direction);
        placementsettings.setKnownShape(true);
        placementsettings.setIgnoreEntities(false);
        placementsettings.setFinalizeEntities(true);

        // First do the jigsaw -> whatever it turns into
        if (!notJigsaw) placementsettings.addProcessor(JigsawReplacementProcessor.INSTANCE);

        // Then add custom processors
        this.processors.value().list().forEach(placementsettings::addProcessor);

        // Then add structure block handling
        if (bool_config.markers_to_air) placementsettings.addProcessor(MarkerToAirProcessor.PROCESSOR);
        else placementsettings.addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);

        // And finally add the terrain matching processors
        if (bool_config.water_terrain_match)
            placementsettings.addProcessor(new GravityProcessor(Types.OCEAN_FLOOR_WG, -1));
        else this.getProjection().getProcessors().forEach(placementsettings::addProcessor);

        return placementsettings;
    }

    public void checkWaterlogging(final WorldGenLevel level, StructureTemplate template,
            StructurePlaceSettings placementsettings, final BlockPos pos1, final BlockPos pos2, final Rotation rotation,
            final BoundingBox box, final Random rng, Map<BlockPos, BlockState> unWaterlog)
    {
        List<StructureTemplate.StructureBlockInfo> list = placementsettings.getRandomPalette(template.palettes, pos1)
                .blocks();

        for (StructureTemplate.StructureBlockInfo structuretemplate$structureblockinfo : StructureTemplate
                .processBlockInfos(level, pos1, pos2, placementsettings, list, template))
        {
            BlockPos blockpos = structuretemplate$structureblockinfo.pos;
            if (box == null || box.isInside(blockpos))
            {
                FluidState old = level.getFluidState(blockpos);
                @SuppressWarnings("deprecation")
                BlockState to_place = structuretemplate$structureblockinfo.state.mirror(placementsettings.getMirror())
                        .rotate(placementsettings.getRotation());
                if (old.is(Fluids.WATER) && to_place.getFluidState().isEmpty()
                        && to_place.getBlock() instanceof LiquidBlockContainer)
                {
                    unWaterlog.put(blockpos, level.getBlockState(blockpos));
                }
            }
        }
    }

    @Override
    public boolean place(final StructureManager templates, final WorldGenLevel level,
            final StructureFeatureManager structureManager, final ChunkGenerator chunkGenerator, final BlockPos pos1,
            final BlockPos pos2, final Rotation rotation, final BoundingBox box, final Random rng,
            final boolean notJigsaw)
    {
        final StructureTemplate template = this.getTemplate(templates);
        final StructurePlaceSettings placementsettings = this.getSettings(rotation, box, notJigsaw);
        final int placeFlags = 18;

        boolean placed = false;

        try
        {
            Map<BlockPos, BlockState> unWaterlog = Maps.newHashMap();
            boolean checkWaterlog = this.processors.value().list().contains(NoWaterlogProcessor.PROCESSOR);
            if (checkWaterlog)
            {
                checkWaterlogging(level, template, placementsettings, pos1, pos2, rotation, box, rng, unWaterlog);
            }
            placed = template.placeInWorld(level, pos1, pos2, placementsettings, rng, placeFlags);
            if (checkWaterlog && placed)
            {
                unWaterlog.forEach((pos, state) -> {
                    BlockState newState = level.getBlockState(pos);
                    if (newState.getBlock() instanceof LiquidBlockContainer cont)
                    {
                        boolean worked = cont.placeLiquid(level, pos, newState, Fluids.EMPTY.defaultFluidState());
                        if (!worked && newState.hasProperty(BlockStateProperties.WATERLOGGED))
                        {
                            worked = level.setBlock(pos, newState.setValue(BlockStateProperties.WATERLOGGED, false),
                                    placeFlags & -2 | 16);
                        }
                    }
                });
            }
        }
        catch (final Exception e)
        {
            PokecubeCore.LOGGER.error("Error with part of structure: {}", this.name);
            PokecubeCore.LOGGER.error(e);
        }

        if (!placed) return false;
        else
        {
            if (this.world == null) this.world = ExpandedJigsawPacement.getForGen(chunkGenerator);
            if (!"none".equals(this.biome_type))
            {
                final BoundingBox realBox = this.getBoundingBox(templates, pos1, rotation);
                final StructureEvent.BuildStructure event = new StructureEvent.BuildStructure(realBox, this.world,
                        this.name, placementsettings);
                event.setBiomeType(this.biome_type);
                MinecraftForge.EVENT_BUS.post(event);
            }

            // Check if we need to undo any waterlogging which may have
            // occurred, we also process data markers here as to not duplicate
            // loop later, as this operation is expensive enough anyway.
            if (bool_config.markers_to_air)
            {
                final List<StructureTemplate.StructureBlockInfo> list = placementsettings
                        .getRandomPalette(template.palettes, pos1).blocks();
                for (final StructureBlockInfo info : list)
                {
                    final boolean isDataMarker = info.state.getBlock() == Blocks.STRUCTURE_BLOCK && info.nbt != null
                            && StructureMode.valueOf(info.nbt.getString("mode")) == StructureMode.DATA;
                    if (isDataMarker)
                    {
                        final BlockPos blockpos = StructureTemplate
                                .calculateRelativePosition(placementsettings, info.pos).offset(pos1);
                        this.handleDataMarker(level, info, blockpos, rotation, rng, box);
                    }
                }
            }
            else
            {
                // The false is if to return transformed position
                final List<StructureBlockInfo> data = this.getDataMarkers(templates, pos1, rotation, false);
                for (final StructureBlockInfo info : data)
                {
                    final BlockPos blockpos = StructureTemplate.calculateRelativePosition(placementsettings, info.pos)
                            .offset(pos1);
                    this.handleDataMarker(level, info, blockpos, rotation, rng, box);
                }
            }
            return true;
        }
    }

    @Override
    public void handleDataMarker(final LevelAccessor worldIn, final StructureBlockInfo info, final BlockPos pos,
            final Rotation rotationIn, final Random rand, final BoundingBox box)
    {
        String function = info.nbt != null ? info.nbt.getString("metadata") : "";

        final boolean toPlaceProf = this.isSpawn && !PokecubeSerializer.getInstance().hasPlacedProf();
        final boolean toPlaceSpawn = this.isSpawn && !this.placedSpawn;

        if (toPlaceProf && info.pos.equals(this.profPos))
        {
            PokecubeCore.LOGGER.info("Overriding an entry as a professor at " + pos);
            function = PokecubeCore.getConfig().professor_override;
            PokecubeSerializer.getInstance().setPlacedProf();
        }
        if (toPlaceSpawn && info.pos.equals(this.spawnPos))
        {
            PokecubeCore.LOGGER.info("Overriding world spawn to " + pos);
            EventsHandler.Schedule(this.world, w -> {
                ((ServerLevel) w).setDefaultSpawnPos(pos, 0);
                return true;
            });
            this.placedSpawn = true;
        }
        if (function.startsWith("pokecube:chest:"))
        {
            final BlockPos blockpos = pos.below();
            final ResourceLocation key = new ResourceLocation(function.replaceFirst("pokecube:chest:", ""));
            if (box.isInside(blockpos)) RandomizableContainerBlockEntity.setLootTable(worldIn, rand, blockpos, key);
        }
        else if (function.startsWith("Chest "))
        {
            final BlockPos blockpos = pos.below();
            final ResourceLocation key = new ResourceLocation(function.replaceFirst("Chest ", ""));
            if (box.isInside(blockpos)) RandomizableContainerBlockEntity.setLootTable(worldIn, rand, blockpos, key);
        }
        else if (ExpandedJigsawPiece.shouldApply(pos, this.world))
        {
            final Event event = new StructureEvent.ReadTag(function.trim(), pos, worldIn, (ServerLevel) this.world,
                    rand, box);
            MinecraftForge.EVENT_BUS.post(event);
            if (event.getResult() == Result.ALLOW) ExpandedJigsawPiece.apply(pos, this.world);
        }
        super.handleDataMarker(worldIn, info, pos, rotationIn, rand, box);
    }

    @Override
    public StructurePoolElementType<?> getType()
    {
        return PokecubeStructures.EXPANDED_POOL_ELEMENT.get();
    }

    @Override
    public String toString()
    {
        return "PokecubeCustom[" + this.template + "]";
    }
}
