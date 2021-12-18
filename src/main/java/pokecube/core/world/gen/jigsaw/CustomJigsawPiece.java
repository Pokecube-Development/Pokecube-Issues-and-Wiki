package pokecube.core.world.gen.jigsaw;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.feature.structures.SinglePoolElement;
import net.minecraft.world.level.levelgen.feature.structures.StructurePoolElement;
import net.minecraft.world.level.levelgen.feature.structures.StructurePoolElementType;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.GravityProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.JigsawReplacementProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.Event.Result;
import pokecube.core.PokecubeCore;
import pokecube.core.database.worldgen.WorldgenHandler.JigSawConfig;
import pokecube.core.database.worldgen.WorldgenHandler.Options;
import pokecube.core.events.StructureEvent;
import pokecube.core.handlers.events.EventsHandler;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.core.world.gen.WorldgenFeatures;
import pokecube.core.world.gen.template.FillerProcessor;
import pokecube.core.world.gen.template.MarkerToAirProcessor;

public class CustomJigsawPiece extends SinglePoolElement
{
    public static StructurePoolElementType<CustomJigsawPiece> TYPE;

    public static Codec<CustomJigsawPiece> makeCodec()
    {
        return RecordCodecBuilder.create((instance) -> {
            return instance.group(SinglePoolElement.templateCodec(), SinglePoolElement.processorsCodec(),
                    StructurePoolElement.projectionCodec(), CustomJigsawPiece.options(), CustomJigsawPiece.config())
                    .apply(instance, CustomJigsawPiece::new);
        });
    }

    protected static <E extends CustomJigsawPiece> RecordCodecBuilder<E, Options> options()
    {
        return Options.CODEC.fieldOf("opts").forGetter((o) -> {
            return o.opts;
        });
    }

    protected static <E extends CustomJigsawPiece> RecordCodecBuilder<E, JigSawConfig> config()
    {
        return JigSawConfig.CODEC.fieldOf("config").forGetter((o) -> {
            return o.config;
        });
    }

    public static final Map<ResourceKey<Level>, Set<BlockPos>> sent_events = Maps.newConcurrentMap();

    private static boolean shouldApply(final BlockPos pos, final Level worldIn)
    {
        final Set<BlockPos> poses = CustomJigsawPiece.sent_events.get(worldIn.dimension());
        if (poses == null) return true;
        return !poses.contains(pos.immutable());
    }

    private static void apply(final BlockPos pos, final Level worldIn)
    {
        Set<BlockPos> poses = CustomJigsawPiece.sent_events.get(worldIn.dimension());
        if (poses == null) CustomJigsawPiece.sent_events.put(worldIn.dimension(), poses = Sets.newHashSet());
        poses.add(pos.immutable());
    }

    public final Options opts;

    // This gets re-set by the assembler
    public JigSawConfig config;

    public Level world;

    public boolean isSpawn;
    public String spawnReplace;
    public BlockPos spawnPos;
    public BlockPos profPos;
    public boolean placedSpawn = false;

    public StructurePlaceSettings toUse;

    public StructureProcessorList overrideList = null;

    boolean maskCheck;

    public CustomJigsawPiece(final Either<ResourceLocation, StructureTemplate> template,
            final Supplier<StructureProcessorList> processors, final StructureTemplatePool.Projection behaviour,
            final Options opts, final JigSawConfig config)
    {
        super(template, processors, behaviour);
        this.opts = opts;
        this.config = config;
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

        if (!notJigsaw) placementsettings.addProcessor(JigsawReplacementProcessor.INSTANCE);
        if (this.opts.extra.containsKey("markers_to_air"))
            placementsettings.addProcessor(MarkerToAirProcessor.PROCESSOR);
        if (this.opts.filler) placementsettings.addProcessor(FillerProcessor.PROCESSOR);

        final boolean shouldIgnoreAire = this.opts.ignoreAir || !this.opts.rigid;

        if (shouldIgnoreAire) placementsettings.addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR);
        else placementsettings.addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);

        final boolean wasNull = this.overrideList == null;
        if (wasNull && !this.opts.proc_list.isEmpty())
            this.overrideList = WorldgenFeatures.getProcList(this.opts.proc_list);

        if (this.overrideList == null) this.processors.get().list().forEach(placementsettings::addProcessor);
        else
        {
            this.overrideList.list().forEach(placementsettings::addProcessor);
            if (wasNull) this.overrideList = null;
        }
        final boolean water_terrain_match = !this.opts.rigid && this.opts.water;
        if (water_terrain_match) placementsettings.addProcessor(new GravityProcessor(Types.OCEAN_FLOOR_WG, -1));
        else this.getProjection().getProcessors().forEach(placementsettings::addProcessor);

        if (!config.proc_list.isEmpty())
            WorldgenFeatures.getProcList(config.proc_list).list().forEach(placementsettings::addProcessor);

        return this.toUse = placementsettings;
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
            placed = template.placeInWorld(level, pos1, pos2, placementsettings, rng, placeFlags);
        }
        catch (final Exception e)
        {
            PokecubeCore.LOGGER.error("Error with part of structure: {}", this.config.serialize());
            PokecubeCore.LOGGER.error(e);
        }

        if (!placed) return false;
        else
        {
            if (this.world == null) this.world = JigsawAssmbler.getForGen(chunkGenerator);
            if (this.config.name != null)
            {
                final BoundingBox realBox = this.getBoundingBox(templates, pos1, rotation);
                final StructureEvent.BuildStructure event = new StructureEvent.BuildStructure(realBox, this.world,
                        this.config.name, placementsettings);
                event.setBiomeType(this.config.biomeType);
                MinecraftForge.EVENT_BUS.post(event);
            }

            // Check if we need to undo any waterlogging which may have
            // occurred, we also process data markers here as to not duplicate
            // loop later, as this operation is expensive enough anyway.
            if (this.opts.extra.containsKey("markers_to_air"))
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
                    else if (info.state.hasProperty(BlockStateProperties.WATERLOGGED))
                    {
                        final BlockPos blockpos = StructureTemplate
                                .calculateRelativePosition(placementsettings, info.pos).offset(pos1);
                        final BlockState blockstate = info.state.mirror(placementsettings.getMirror()).rotate(level,
                                blockpos, placementsettings.getRotation());
                        level.setBlock(blockpos, blockstate.setValue(BlockStateProperties.WATERLOGGED, false),
                                placeFlags);
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
        else if (CustomJigsawPiece.shouldApply(pos, this.world))
        {
            final Event event = new StructureEvent.ReadTag(function.trim(), pos, worldIn, (ServerLevel) this.world,
                    rand, box);
            MinecraftForge.EVENT_BUS.post(event);
            if (event.getResult() == Result.ALLOW) CustomJigsawPiece.apply(pos, this.world);
        }
        super.handleDataMarker(worldIn, info, pos, rotationIn, rand, box);
    }

    @Override
    public StructurePoolElementType<?> getType()
    {
        return CustomJigsawPiece.TYPE;
    }

    @Override
    public String toString()
    {
        return "PokecubeCustom[" + this.template + "]";
    }
}
