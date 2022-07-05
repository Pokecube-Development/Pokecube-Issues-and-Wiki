package pokecube.world.gen.structures.pool_elements;

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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.Event.Result;
import pokecube.core.PokecubeCore;
import pokecube.core.events.StructureEvent;
import pokecube.core.handlers.events.EventsHandler;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.world.gen.structures.PokecubeStructures;
import pokecube.world.gen.structures.utils.ExpandedJigsawPacement;

public class ExpandedJigsawPiece extends SinglePoolElement
{
    public static Codec<ExpandedJigsawPiece> makeCodec()
    {
        return RecordCodecBuilder.create((instance) -> {
            return instance.group(SinglePoolElement.templateCodec(), SinglePoolElement.processorsCodec(),
                    StructurePoolElement.projectionCodec(),
                    Codec.BOOL.fieldOf("ignore_air").orElse(false).forGetter(structure -> structure.ignore_air),
                    Codec.BOOL.fieldOf("water_terrain_match").orElse(false)
                            .forGetter(structure -> structure.water_terrain_match),
                    Codec.BOOL.fieldOf("markers_to_air").orElse(true).forGetter(structure -> structure.markers_to_air),
                    Codec.STRING.fieldOf("biome_type").orElse("none").forGetter(structure -> structure.biome_type),
                    Codec.STRING.fieldOf("name").orElse("none").forGetter(structure -> structure.biome_type),
                    Codec.STRING.fieldOf("flags").orElse("").forGetter(structure -> structure.flags))
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

    public Level world;

    boolean ignore_air = false;
    boolean water_terrain_match = false;
    boolean markers_to_air = true;
    String biome_type = "none";
    String name = "none";
    String flags = "";

    public boolean isSpawn;
    public String spawnReplace;
    public BlockPos spawnPos;
    public BlockPos profPos;
    public boolean placedSpawn = false;

    public StructurePlaceSettings toUse;

    public StructureProcessorList overrideList = null;

    boolean maskCheck;

    public ExpandedJigsawPiece(final Either<ResourceLocation, StructureTemplate> template,
            final Holder<StructureProcessorList> processors, final StructureTemplatePool.Projection behaviour,
            final boolean ignoreAir, final boolean water_terrain_match, final boolean markers_to_air,
            final String biome_type, final String name, final String flags)
    {
        super(template, processors, behaviour);
        this.ignore_air = ignoreAir;
        this.water_terrain_match = water_terrain_match;
        this.biome_type = biome_type;
        this.name = name;
        this.markers_to_air = markers_to_air;
        this.flags = flags;
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

        final boolean shouldIgnoreAire = this.ignore_air;

        if (shouldIgnoreAire) placementsettings.addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR);
        else placementsettings.addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);

        this.processors.value().list().forEach(placementsettings::addProcessor);

        if (water_terrain_match) placementsettings.addProcessor(new GravityProcessor(Types.OCEAN_FLOOR_WG, -1));
        else this.getProjection().getProcessors().forEach(placementsettings::addProcessor);

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
            PokecubeCore.LOGGER.error("Error with part of structure: {}", this.name);
            PokecubeCore.LOGGER.error(e);
        }

        if (!placed) return false;
        else
        {
            if (this.world == null) this.world = ExpandedJigsawPacement.getForGen(chunkGenerator);
            if (!"none".equals(this.name))
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
            if (this.markers_to_air)
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
