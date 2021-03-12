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

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.StructureMode;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.Heightmap.Type;
import net.minecraft.world.gen.feature.jigsaw.IJigsawDeserializer;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern;
import net.minecraft.world.gen.feature.jigsaw.JigsawPiece;
import net.minecraft.world.gen.feature.jigsaw.SingleJigsawPiece;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.gen.feature.template.BlockIgnoreStructureProcessor;
import net.minecraft.world.gen.feature.template.GravityStructureProcessor;
import net.minecraft.world.gen.feature.template.JigsawReplacementStructureProcessor;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.StructureProcessorList;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.Event.Result;
import pokecube.core.PokecubeCore;
import pokecube.core.database.worldgen.WorldgenHandler.JigSawConfig;
import pokecube.core.database.worldgen.WorldgenHandler.Options;
import pokecube.core.events.StructureEvent;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.core.world.gen.WorldgenFeatures;
import pokecube.core.world.gen.template.FillerProcessor;
import pokecube.core.world.gen.template.MarkerToAirProcessor;

public class CustomJigsawPiece extends SingleJigsawPiece
{
    public static IJigsawDeserializer<CustomJigsawPiece> TYPE;

    public static Codec<CustomJigsawPiece> makeCodec()
    {
        return RecordCodecBuilder.create((instance) ->
        {
            return instance.group(SingleJigsawPiece.templateCodec(), SingleJigsawPiece.processorsCodec(), JigsawPiece
                    .projectionCodec(), CustomJigsawPiece.options(), CustomJigsawPiece.config()).apply(instance,
                            CustomJigsawPiece::new);
        });
    }

    protected static <E extends CustomJigsawPiece> RecordCodecBuilder<E, Options> options()
    {
        return Options.CODEC.fieldOf("opts").forGetter((o) ->
        {
            return o.opts;
        });
    }

    protected static <E extends CustomJigsawPiece> RecordCodecBuilder<E, JigSawConfig> config()
    {
        return JigSawConfig.CODEC.fieldOf("config").forGetter((o) ->
        {
            return o.config;
        });
    }

    public static final Map<RegistryKey<World>, Set<BlockPos>> sent_events = Maps.newConcurrentMap();

    private static boolean shouldApply(final BlockPos pos, final World worldIn)
    {
        final Set<BlockPos> poses = CustomJigsawPiece.sent_events.get(worldIn.dimension());
        if (poses == null) return true;
        return !poses.contains(pos.immutable());
    }

    private static void apply(final BlockPos pos, final World worldIn)
    {
        Set<BlockPos> poses = CustomJigsawPiece.sent_events.get(worldIn.dimension());
        if (poses == null) CustomJigsawPiece.sent_events.put(worldIn.dimension(), poses = Sets.newHashSet());
        poses.add(pos.immutable());
    }

    public final Options opts;

    // This gets re-set by the assembler
    public JigSawConfig config;

    public World world;

    public boolean            isSpawn;
    public String             spawnReplace;
    public MutableBoundingBox mask;

    public PlacementSettings toUse;

    public StructureProcessorList overrideList = null;

    boolean maskCheck;

    public CustomJigsawPiece(final Either<ResourceLocation, Template> template,
            final Supplier<StructureProcessorList> processors, final JigsawPattern.PlacementBehaviour behaviour,
            final Options opts, final JigSawConfig config)
    {
        super(template, processors, behaviour);
        this.opts = opts;
        this.config = config;
    }

    @Override
    public PlacementSettings getSettings(final Rotation direction, final MutableBoundingBox box,
            final boolean notJigsaw)
    {
        final PlacementSettings placementsettings = new PlacementSettings();
        placementsettings.setBoundingBox(box);
        placementsettings.setRotation(direction);
        placementsettings.setKnownShape(true);
        placementsettings.setIgnoreEntities(false);
        placementsettings.setFinalizeEntities(true);

        if (!notJigsaw) placementsettings.addProcessor(JigsawReplacementStructureProcessor.INSTANCE);
        if (this.opts.extra.containsKey("markers_to_air")) placementsettings.addProcessor(
                MarkerToAirProcessor.PROCESSOR);
        if (this.opts.filler) placementsettings.addProcessor(FillerProcessor.PROCESSOR);

        final boolean shouldIgnoreAire = this.opts.ignoreAir || !this.opts.rigid;

        if (shouldIgnoreAire) placementsettings.addProcessor(BlockIgnoreStructureProcessor.STRUCTURE_AND_AIR);
        else placementsettings.addProcessor(BlockIgnoreStructureProcessor.STRUCTURE_BLOCK);

        final boolean wasNull = this.overrideList == null;
        if (wasNull && !this.opts.proc_list.isEmpty()) this.overrideList = WorldgenFeatures.getProcList(
                this.opts.proc_list);

        if (this.overrideList == null) this.processors.get().list().forEach(placementsettings::addProcessor);
        else
        {
            this.overrideList.list().forEach(placementsettings::addProcessor);
            if (wasNull) this.overrideList = null;
        }

        final boolean water_terrain_match = !this.opts.rigid && this.opts.water;
        if (water_terrain_match) placementsettings.addProcessor(new GravityStructureProcessor(Type.OCEAN_FLOOR_WG, -1));
        else this.getProjection().getProcessors().forEach(placementsettings::addProcessor);

        return this.toUse = placementsettings;
    }

    @Override
    public boolean place(final TemplateManager templates, final ISeedReader seedReader,
            final StructureManager structureManager, final ChunkGenerator chunkGenerator, final BlockPos pos1,
            final BlockPos pos2, final Rotation rotation, final MutableBoundingBox box, final Random rng,
            final boolean notJigsaw)
    {

        final Template template = this.getTemplate(templates);
        final PlacementSettings placementsettings = this.getSettings(rotation, box, notJigsaw);
        final int placeFlags = 18;
        if (!template.placeInWorld(seedReader, pos1, pos2, placementsettings, rng, placeFlags)) return false;
        else
        {
            if (this.world == null) this.world = JigsawAssmbler.getForGen(chunkGenerator);
            if (this.config.name != null)
            {
                final StructureEvent.BuildStructure event = new StructureEvent.BuildStructure(box, this.world,
                        this.config.name, placementsettings);
                event.setBiomeType(this.config.biomeType);
                MinecraftForge.EVENT_BUS.post(event);
            }
            this.maskCheck = this.mask != null && this.mask.intersects(box);

            // Check if we need to undo any waterlogging which may have
            // occurred, we also process data markers here as to not duplicate
            // loop later, as this operation is expensive enough anyway.
            if (this.opts.extra.containsKey("markers_to_air"))
            {
                final List<Template.BlockInfo> list = placementsettings.getRandomPalette(template.palettes, pos1)
                        .blocks();
                for (final BlockInfo info : list)
                {
                    final boolean isDataMarker = info.state.getBlock() == Blocks.STRUCTURE_BLOCK && info.nbt != null
                            && StructureMode.valueOf(info.nbt.getString("mode")) == StructureMode.DATA;
                    if (isDataMarker)
                    {
                        final BlockPos blockpos = Template.calculateRelativePosition(placementsettings, info.pos)
                                .offset(pos1);
                        this.handleDataMarker(seedReader, info, blockpos, rotation, rng, box);
                    }
                    else if (info.state.hasProperty(BlockStateProperties.WATERLOGGED))
                    {
                        final BlockPos blockpos = Template.calculateRelativePosition(placementsettings, info.pos)
                                .offset(pos1);
                        final BlockState blockstate = info.state.mirror(placementsettings.getMirror()).rotate(
                                seedReader, blockpos, placementsettings.getRotation());
                        seedReader.setBlock(blockpos, blockstate.setValue(BlockStateProperties.WATERLOGGED, false),
                                placeFlags);
                    }
                }
            }
            else
            {
                // The false is if to return transformed position
                final List<BlockInfo> data = this.getDataMarkers(templates, pos1, rotation, false);
                for (final BlockInfo info : data)
                {
                    final BlockPos blockpos = Template.calculateRelativePosition(placementsettings, info.pos).offset(
                            pos1);
                    this.handleDataMarker(seedReader, info, blockpos, rotation, rng, box);
                }
            }
            return true;
        }
    }

    @Override
    public void handleDataMarker(final IWorld worldIn, final BlockInfo info, final BlockPos pos,
            final Rotation rotationIn, final Random rand, final MutableBoundingBox box)
    {
        String function = info.nbt != null ? info.nbt.getString("metadata") : "";

        this.isSpawn = this.isSpawn && !PokecubeSerializer.getInstance().hasPlacedProf();

        if (this.isSpawn && this.maskCheck && this.spawnReplace.equals(function))
        {
            PokecubeCore.LOGGER.debug("Overriding an entry as a professor at " + pos);
            function = PokecubeCore.getConfig().professor_override;
            PokecubeSerializer.getInstance().setPlacedProf();
        }
        if (function.startsWith("pokecube:chest:"))
        {
            final BlockPos blockpos = pos.below();
            final ResourceLocation key = new ResourceLocation(function.replaceFirst("pokecube:chest:", ""));
            if (box.isInside(blockpos)) LockableLootTileEntity.setLootTable(worldIn, rand, blockpos, key);
        }
        else if (function.startsWith("Chest "))
        {
            final BlockPos blockpos = pos.below();
            final ResourceLocation key = new ResourceLocation(function.replaceFirst("Chest ", ""));
            if (box.isInside(blockpos)) LockableLootTileEntity.setLootTable(worldIn, rand, blockpos, key);
        }
        else if (CustomJigsawPiece.shouldApply(pos, this.world))
        {
            final Event event = new StructureEvent.ReadTag(function.trim(), pos, worldIn, (ServerWorld) this.world,
                    rand, box);
            MinecraftForge.EVENT_BUS.post(event);
            if (event.getResult() == Result.ALLOW) CustomJigsawPiece.apply(pos, this.world);
        }
        super.handleDataMarker(worldIn, info, pos, rotationIn, rand, box);
    }

    @Override
    public IJigsawDeserializer<?> getType()
    {
        return CustomJigsawPiece.TYPE;
    }

    @Override
    public String toString()
    {
        return "PokecubeCustom[" + this.template + "]";
    }
}
