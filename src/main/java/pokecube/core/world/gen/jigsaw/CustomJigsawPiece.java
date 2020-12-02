package pokecube.core.world.gen.jigsaw;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

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
import net.minecraft.world.gen.feature.jigsaw.IJigsawDeserializer;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern;
import net.minecraft.world.gen.feature.jigsaw.JigsawPiece;
import net.minecraft.world.gen.feature.jigsaw.SingleJigsawPiece;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.gen.feature.template.BlockIgnoreStructureProcessor;
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
import pokecube.core.world.gen.template.FillerProcessor;
import pokecube.core.world.gen.template.MarkerToAirProcessor;

public class CustomJigsawPiece extends SingleJigsawPiece
{
    public static IJigsawDeserializer<CustomJigsawPiece> TYPE;

    public static final Codec<CustomJigsawPiece> CODEC = RecordCodecBuilder.create((instance) ->
    {
        return instance.group(SingleJigsawPiece.func_236846_c_(), SingleJigsawPiece.func_236844_b_(), JigsawPiece
                .func_236848_d_(), CustomJigsawPiece.options(), CustomJigsawPiece.config()).apply(instance,
                        CustomJigsawPiece::new);
    });

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
        Set<BlockPos> poses = CustomJigsawPiece.sent_events.get(worldIn.getDimensionKey());
        if (poses == null) CustomJigsawPiece.sent_events.put(worldIn.getDimensionKey(), poses = Sets.newHashSet());
        return !poses.contains(pos.toImmutable());
    }

    private static void apply(final BlockPos pos, final World worldIn)
    {
        Set<BlockPos> poses = CustomJigsawPiece.sent_events.get(worldIn.getDimensionKey());
        if (poses == null) CustomJigsawPiece.sent_events.put(worldIn.getDimensionKey(), poses = Sets.newHashSet());
        poses.add(pos.toImmutable());
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
    public PlacementSettings func_230379_a_(final Rotation direction, final MutableBoundingBox box,
            final boolean notJigsaw)
    {
        final PlacementSettings placementsettings = new PlacementSettings();
        placementsettings.setBoundingBox(box);
        placementsettings.setRotation(direction);
        placementsettings.func_215223_c(true);
        placementsettings.setIgnoreEntities(false);
        placementsettings.func_237133_d_(true);

        if (!notJigsaw) placementsettings.addProcessor(JigsawReplacementStructureProcessor.INSTANCE);
        if (this.opts.extra.containsKey("markers_to_air")) placementsettings.addProcessor(
                MarkerToAirProcessor.PROCESSOR);
        if (this.opts.filler) placementsettings.addProcessor(FillerProcessor.PROCESSOR);
        if (!this.opts.ignoreAir || !this.opts.rigid) placementsettings.addProcessor(
                BlockIgnoreStructureProcessor.AIR_AND_STRUCTURE_BLOCK);
        else placementsettings.addProcessor(BlockIgnoreStructureProcessor.STRUCTURE_BLOCK);

        if (this.overrideList == null)
        {
            this.processors.get().func_242919_a().forEach(placementsettings::addProcessor);
            this.getPlacementBehaviour().getStructureProcessors().forEach(placementsettings::addProcessor);
        }
        else this.overrideList.func_242919_a().forEach(placementsettings::addProcessor);

        return this.toUse = placementsettings;
    }

    @Override
    public boolean func_230378_a_(final TemplateManager templates, final ISeedReader seedReader,
            final StructureManager structureManager, final ChunkGenerator chunkGenerator, final BlockPos pos1,
            final BlockPos pos2, final Rotation rotation, final MutableBoundingBox box, final Random rng,
            final boolean notJigsaw)
    {

        final Template template = this.getTemplate(templates);
        final PlacementSettings placementsettings = this.func_230379_a_(rotation, box, notJigsaw);
        if (!template.func_237146_a_(seedReader, pos1, pos2, placementsettings, rng, 18)) return false;
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
            this.maskCheck = this.mask != null && this.mask.intersectsWith(box);
            final List<BlockInfo> data = this.getDataMarkers(templates, pos1, rotation, false);
            for (final BlockInfo info : data)
            {
                final BlockPos blockpos = Template.transformedBlockPos(placementsettings, info.pos).add(pos1);
                this.handleDataMarker(seedReader, info, blockpos, rotation, rng, box);
            }

            for (final Template.BlockInfo template$blockinfo : Template.processBlockInfos(seedReader, pos1, pos2,
                    placementsettings, data, template))
                this.handleDataMarker(seedReader, template$blockinfo, pos1, rotation, rng, box);

            return true;
        }
    }

    public Template getTemplate(final TemplateManager manager)
    {
        return this.field_236839_c_.map(manager::getTemplateDefaulted, Function.identity());
    }

    @Override
    public void handleDataMarker(final IWorld worldIn, final BlockInfo info, final BlockPos pos,
            final Rotation rotationIn, final Random rand, final MutableBoundingBox box)
    {
        String function = info.nbt != null ? info.nbt.getString("metadata") : "";
        if (!function.isEmpty()) PokecubeCore.LOGGER.debug(function);

        this.isSpawn = this.isSpawn && !PokecubeSerializer.getInstance().hasPlacedProf();

        if (this.isSpawn && this.maskCheck && this.spawnReplace.equals(function))
        {
            PokecubeCore.LOGGER.debug("Overriding an entry as a professor at " + pos);
            function = PokecubeCore.getConfig().professor_override;
            PokecubeSerializer.getInstance().setPlacedProf();
        }
        if (function.startsWith("pokecube:chest:"))
        {
            final BlockPos blockpos = pos.down();
            final ResourceLocation key = new ResourceLocation(function.replaceFirst("pokecube:chest:", ""));
            if (box.isVecInside(blockpos)) LockableLootTileEntity.setLootTable(worldIn, rand, blockpos, key);
        }
        else if (function.startsWith("Chest "))
        {
            final BlockPos blockpos = pos.down();
            final ResourceLocation key = new ResourceLocation(function.replaceFirst("Chest ", ""));
            if (box.isVecInside(blockpos)) LockableLootTileEntity.setLootTable(worldIn, rand, blockpos, key);
        }
        else if (CustomJigsawPiece.shouldApply(pos, this.world))
        {
            PokecubeCore.LOGGER.info(function);
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
        return "PokecubeCustom[" + this.field_236839_c_ + "]";
    }
}
