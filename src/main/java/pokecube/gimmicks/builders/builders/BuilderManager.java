package pokecube.gimmicks.builders.builders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Predicates;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraftforge.common.util.INBTSerializable;
import pokecube.api.PokecubeAPI;
import pokecube.api.utils.BookInstructionsParser;
import pokecube.gimmicks.builders.builders.events.BuilderInstructionsEvent;
import pokecube.world.gen.structures.configs.ExpandedJigsawConfiguration;
import pokecube.world.gen.structures.configs.ExpandedJigsawConfiguration.AvoidanceSettings;
import pokecube.world.gen.structures.configs.ExpandedJigsawConfiguration.ClearanceSettings;
import pokecube.world.gen.structures.configs.ExpandedJigsawConfiguration.YSettings;
import pokecube.world.gen.structures.pieces.ExpandedPoolElementStructurePiece;
import pokecube.world.gen.structures.utils.ExpandedJigsawPacement;
import thut.core.common.ThutCore;

/**
 * This class manages organising IBlocksBuilder and IBlocksClearer, and
 * generating them from instructions in books, as well as saving/loading of them
 * to/from nbt.
 */
public class BuilderManager
{
    /**
     * Record for holding the IBlocksBuilder, IBlocksClearer for a mob to work
     * with. It also holds a String, saveKey, which is what is used to lookup
     * the serializer/deserializer.
     */
    public static record BuilderClearer(IBlocksBuilder builder, IBlocksClearer clearer, String saveKey)
    {
    }

    /**
     * Context for starting a build. It includes the involved server level, as
     * well as the blockpos to consider origin for the build.
     */
    public static record BuildContext(ServerLevel level, BlockPos origin)
    {
    }

    /**
     * Map of saveKey - generator for the BuilderClearer. The saveKey is looked
     * up based on the instructions, as the text after build: in the
     * instructions book.<br>
     * <br>
     * The List&ltString&gt is the instructions loaded from the book.
     */
    public static Map<String, BiFunction<List<String>, BuildContext, BuilderClearer>> providers = new HashMap<>();
    /**
     * Map of saveKey - load function.
     */
    public static Map<String, Function<CompoundTag, BuilderClearer>> loaders = new HashMap<>();
    /**
     * Map of saveKey - save function.
     */
    public static Map<String, Function<BuilderClearer, CompoundTag>> savers = new HashMap<>();

    /**
     * Attempts to save the builder
     * 
     * @param build
     * @return nbt containing the saved builder, or empty nbt if no saver.
     */
    @Nonnull
    public static CompoundTag save(BuilderClearer build)
    {
        CompoundTag nbt = new CompoundTag();
        if (build == null) return nbt;
        String key = build.saveKey();
        if (!savers.containsKey(key)) return nbt;
        nbt.putString("K", key);
        nbt.put("V", savers.get(key).apply(build));
        return nbt;
    }

    /**
     * Attemps to load a builder
     * 
     * @param nbt
     * @return either the loaded builder or null
     */
    @Nullable
    public static BuilderClearer load(CompoundTag nbt)
    {
        String key = nbt.getString("K");
        if (!loaders.containsKey(key)) return null;
        var load = loaders.get(key);
        return load.apply(nbt);
    }

    /**
     * Default implementation of a saver, this can save any generic
     * IBlocksBuilder (key of "b") and IBlocksClearer (key of "c") that also
     * implement INBTSerializable <br>
     * <br>
     * In the case that they are the same object, it only saves it as the
     * builder, and includes a boolean saved under the key "s"
     */
    public static class DefaultSaver implements Function<BuilderClearer, CompoundTag>
    {

        @Override
        public CompoundTag apply(BuilderClearer build)
        {
            CompoundTag tag = new CompoundTag();
            if (build.builder() instanceof INBTSerializable<?> ser) tag.put("b", ser.serializeNBT());
            if (build.builder() == build.clearer()) tag.putBoolean("s", true);
            else if (build.clearer() instanceof INBTSerializable<?> ser) tag.put("c", ser.serializeNBT());
            return tag;
        }

    }

    /**
     * Default parser for "jigsaw" and "building" keys, class is private as this
     * is the hardcoded implementation.
     */
    private static class DefaultParser implements BiFunction<List<String>, BuildContext, BuilderClearer>
    {
        @Override
        public BuilderClearer apply(List<String> lines, BuildContext bcontext)
        {
            if (lines.size() < 2) return null;

            var level = bcontext.level();
            var origin = bcontext.origin();

            ResourceLocation toMake = null;
            BlockPos shift = new BlockPos(0, 0, 0);

            String type = lines.get(0).replace("build:", "").strip();

            if (!(type.equals("jigsaw") || type.equals("building"))) return null;

            toMake = new ResourceLocation(lines.get(1));
            String rotation = "NONE";
            String offset = "0 0 0";
            String mirror = "NONE";
            String _origin = "";
            int jigsawDepth = 4;

            for (int i = 2; i < lines.size(); i++)
            {
                String line = lines.get(i);
                if (line.startsWith("s:")) offset = line.replace("s:", "").strip();
                if (line.startsWith("r:")) rotation = line.replace("r:", "").strip();
                if (line.startsWith("m:")) rotation = line.replace("m:", "").strip();
                if (line.startsWith("d:")) jigsawDepth = Integer.parseInt(line.replace("d:", "").strip());
                if (line.startsWith("o:")) _origin = line.replace("o:", "").strip();
            }

            Rotation rot = Rotation.NONE;
            try
            {
                rot = Rotation.valueOf(rotation.toUpperCase(Locale.ROOT));
            }
            catch (Exception e)
            {
                PokecubeAPI.LOGGER.error(e);
            }

            Mirror mir = Mirror.NONE;
            try
            {
                mir = Mirror.valueOf(mirror.toUpperCase(Locale.ROOT));
            }
            catch (Exception e)
            {
                PokecubeAPI.LOGGER.error(e);
            }

            var tmp = BookInstructionsParser.blockPosFromInstruction(offset);
            if (tmp != null) shift = tmp;

            if (!_origin.isBlank())
            {
                tmp = BookInstructionsParser.blockPosFromInstruction(_origin);
                if (tmp != null) origin = tmp;
            }

            if (toMake != null)
            {
                if (type.equals("building"))
                {
                    var builder = new StructureBuilder(origin.offset(shift), rot, mir);
                    builder.toMake = toMake;
                    return new BuilderClearer(builder, builder, "builder");
                }
                else
                {
                    var poolHolder = level.registryAccess().registryOrThrow(Registry.TEMPLATE_POOL_REGISTRY)
                            .getHolderOrThrow(ResourceKey.create(Registry.TEMPLATE_POOL_REGISTRY, toMake));
                    ExpandedJigsawConfiguration config = new ExpandedJigsawConfiguration(poolHolder, jigsawDepth,
                            YSettings.DEFAULT, ClearanceSettings.DEFAULT, new ArrayList<>(), "", "", "none",
                            Heightmap.Types.WORLD_SURFACE_WG, new ArrayList<>(), 0, 0, AvoidanceSettings.DEFAULT);
                    var context = new PieceGeneratorSupplier.Context<ExpandedJigsawConfiguration>(
                            level.getChunkSource().getGenerator(),
                            level.getChunkSource().getGenerator().getBiomeSource(), level.getSeed(),
                            new ChunkPos(origin), config, level, Predicates.alwaysTrue(), level.getStructureManager(),
                            level.registryAccess());
                    WorldgenRandom worldgenrandom = new WorldgenRandom(new LegacyRandomSource(0L));
                    worldgenrandom.setLargeFeatureSeed(context.seed(), context.chunkPos().x, context.chunkPos().z);
                    var make = ExpandedJigsawPacement.addPieces(context, ExpandedPoolElementStructurePiece::new, origin,
                            false, false, worldgenrandom, rot);
                    if (make.isPresent())
                    {
                        StructurePiecesBuilder structurepiecesbuilder = new StructurePiecesBuilder();
                        worldgenrandom = new WorldgenRandom(new LegacyRandomSource(0L));
                        worldgenrandom.setLargeFeatureSeed(context.seed(), context.chunkPos().x, context.chunkPos().z);
                        var buildContext = new PieceGenerator.Context<ExpandedJigsawConfiguration>(config,
                                level.getChunkSource().getGenerator(), level.getStructureManager(),
                                new ChunkPos(origin), level, worldgenrandom, level.getSeed());
                        make.get().generatePieces(structurepiecesbuilder, buildContext);

                        var jigsaw = new JigsawBuilder(structurepiecesbuilder, shift, level);
                        return new BuilderClearer(jigsaw, jigsaw, "jigsaw");
                    }
                }
            }

            return null;
        }
    }

    /**
     * Populates providers, savers and loaders with some defaults if they are
     * empty.
     */
    public static void defaultInit()
    {
        if (providers.isEmpty())
        {
            var parser = new DefaultParser();
            providers.put("jigsaw", parser);
            providers.put("building", parser);
        }
        if (savers.isEmpty())
        {
            var saver = new DefaultSaver();
            savers.put("jigsaw", saver);
            savers.put("building", saver);
        }
        if (loaders.isEmpty())
        {
            // Our default loader assumes it was saved with the default saver,
            // so if you edited the saver, ensure to change here too!
            loaders.put("jigsaw", (tag) -> {
                if (tag.contains("b", Tag.TAG_COMPOUND))
                {
                    var jig = new JigsawBuilder();
                    jig.deserializeNBT(tag.getCompound("b"));
                    return new BuilderClearer(jig, jig, "jigsaw");
                }
                return null;
            });
            loaders.put("building", (tag) -> {
                if (tag.contains("b", Tag.TAG_COMPOUND))
                {
                    var jig = new StructureBuilder();
                    jig.deserializeNBT(tag.getCompound("b"));
                    return new BuilderClearer(jig, jig, "building");
                }
                return null;
            });
        }
    }

    /**
     * Attempts to generate a {@link BuilderClearer} from instructions found in
     * the given itemstack.
     * 
     * @param source  - instructions to read
     * @param context - context for the build
     * @return generated {@link BuilderClearer} or null if not valid
     *         instructions.
     */
    @Nullable
    public static BuilderClearer fromInstructions(ItemStack source, BuildContext context)
    {
        defaultInit();
        List<String> instructions = BookInstructionsParser.getInstructions(source, "build", true);
        try
        {
            if (instructions.isEmpty()) return null;
            String type = instructions.get(0).replace("build:", "").strip();
            BuilderClearer initial = null;
            if (providers.containsKey(type)) initial = providers.get(type).apply(instructions, context);
            var event = new BuilderInstructionsEvent(instructions, initial);
            ThutCore.FORGE_BUS.post(event);
            return event.getResults();
        }
        catch (Exception e)
        {
            PokecubeAPI.LOGGER.error("Error loading building instructions", e);
        }
        return null;
    }
}
