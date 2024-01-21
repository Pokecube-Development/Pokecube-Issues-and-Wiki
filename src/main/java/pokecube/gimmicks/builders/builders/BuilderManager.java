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

import net.minecraft.ResourceLocationException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
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
import thut.api.level.structures.NamedVolumes.INamedPart;
import thut.api.level.structures.StructureManager;
import thut.core.common.ThutCore;
import thut.lib.TComponent;

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
    public static record BuilderClearer(@Nullable IBlocksBuilder builder, @Nullable IBlocksClearer clearer,
            @Nonnull String saveKey)
    {
    }

    /**
     * Context for starting a build. It includes the involved server level, as
     * well as the blockpos to consider origin for the build.
     */
    public static record BuildContext(ServerLevel level, BlockPos origin, @Nullable ServerPlayer player)
    {
        public BuildContext(ServerLevel level, BlockPos origin)
        {
            this(level, origin, null);
        }
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
        public static final DefaultSaver INSTANCE = new DefaultSaver();

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
        private static final DefaultParser INSTANCE = new DefaultParser();

        @Override
        public BuilderClearer apply(List<String> lines, BuildContext bcontext)
        {
            if (lines.size() < 2) return null;

            var level = bcontext.level();
            var origin = bcontext.origin();

            ResourceLocation toMake = null;
            BlockPos shift = new BlockPos(0, 0, 0);

            String type = lines.get(0).replace("build:", "").strip();
            boolean loadSaved = false;

            if (type.equals("save"))
            {
                if (bcontext.player() == null) return null;
                boolean trySave = saveStructureForLocation(lines, bcontext);
                if (!trySave) return null;
                return new BuilderClearer(null, null, type);
            }

            if (!(type.equals("jigsaw") || type.equals("building") || (loadSaved = type.equals("saved")))) return null;

            if (type.equals("saved") && bcontext.player() == null) return null;

            var name = lines.get(1);
            if (loadSaved)
            {
                if (name.contains(":"))
                {
                    var loc = new ResourceLocation(name);
                    name = loc.getPath();
                }
                toMake = new ResourceLocation(bcontext.player().getStringUUID(), name);
            }
            else toMake = new ResourceLocation(name);

            String rotation = "NONE";
            String offset = "0 0 0";
            String mirror = "NONE";
            String _origin = "";
            int jigsawDepth = 4;
            boolean noClear = false;

            for (int i = 2; i < lines.size(); i++)
            {
                String line = lines.get(i);
                if (line.startsWith("s:")) offset = line.replace("s:", "").strip();
                if (line.startsWith("r:")) rotation = line.replace("r:", "").strip();
                if (line.startsWith("m:")) rotation = line.replace("m:", "").strip();
                if (line.startsWith("d:")) jigsawDepth = Integer.parseInt(line.replace("d:", "").strip());
                if (line.startsWith("o:")) _origin = line.replace("o:", "").strip();
                if (line.startsWith("no_clear")) noClear = true;
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
                if (type.equals("building") || type.equals("saved"))
                {
                    var builder = new StructureBuilder(origin.offset(shift), rot, mir);
                    builder.toMake = toMake;
                    if (noClear) return new BuilderClearer(builder, null, "builder");
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
                        if (noClear) return new BuilderClearer(jigsaw, null, "jigsaw");
                        return new BuilderClearer(jigsaw, jigsaw, "jigsaw");
                    }
                }
            }

            return null;
        }
    }

    public static boolean saveStructureForLocation(List<String> lines, BuildContext bcontext)
    {
        if (bcontext.player() == null) return false;
        if (lines.size() < 2) return false;

        var level = bcontext.level();
        var origin = bcontext.origin();

        ResourceLocation toMake = null;
        BlockPos size = new BlockPos(0, 0, 0);

        String type = lines.get(0).replace("build:", "").strip();

        if (!type.equals("save")) return false;

        var name = lines.get(1);
        if (name.contains(":"))
        {
            var loc = new ResourceLocation(name);
            name = loc.getPath();
        }

        toMake = new ResourceLocation(bcontext.player().getStringUUID(), name);

        String _size = "0 0 0";
        String _origin = "";

        for (int i = 2; i < lines.size(); i++)
        {
            String line = lines.get(i);
            if (line.startsWith("s:")) _size = line.replace("s:", "").strip();
            if (line.startsWith("o:")) _origin = line.replace("o:", "").strip();
        }

        var tmp = BookInstructionsParser.blockPosFromInstruction(_size);
        if (tmp != null) size = tmp;

        if (!_origin.isBlank())
        {
            tmp = BookInstructionsParser.blockPosFromInstruction(_origin);
            if (tmp != null) origin = tmp;
        }

        int volume = size.getX() * size.getY() * size.getZ();
        if (volume > 32 * 32 * 32) return false;

        var structuremanager = level.getStructureManager();

        StructureTemplate structuretemplate;
        try
        {
            structuretemplate = structuremanager.getOrCreate(toMake);
        }
        catch (ResourceLocationException resourcelocationexception1)
        {
            return false;
        }

        structuretemplate.fillFromWorld(level, origin, size, false, Blocks.AIR);
        structuretemplate.setAuthor(bcontext.player().getName().getString());
        try
        {
            return structuremanager.save(toMake);
        }
        catch (ResourceLocationException resourcelocationexception)
        {
            return false;
        }
    }

    public static boolean generateBuildingBookForLocation(BuildContext context, ItemStack book)
    {
        var level = context.level();
        var pos = context.origin();
        if (book.getItem() != Items.WRITABLE_BOOK) return false;
        var structs = StructureManager.getFor(level.dimension(), pos, false);
        for (var s : structs)
        {
            INamedPart inside = null;
            for (var p : s.getParts())
            {
                if (p.getBounds().isInside(pos))
                {
                    inside = p;
                    break;
                }
            }
            if (inside != null && inside.getWrapped() instanceof PoolElementStructurePiece pooled)
            {
                CompoundTag nbt = new CompoundTag();
                ListTag pages = new ListTag();
                nbt.put("pages", pages);
                String msg = "build:building\n";

                var contx = StructurePieceSerializationContext.fromLevel(level);;
                var pooled_tag = pooled.createTag(contx);
                var element_tag = pooled_tag.getCompound("pool_element");
                msg += element_tag.getString("location") + "\n";
                if (pooled_tag.contains("rotation")) msg += "r: " + pooled_tag.getString("rotation") + "\n";

                int dy = pooled_tag.getInt("ground_level_delta");
                if (element_tag.contains("int_config"))
                {
                    dy += element_tag.getCompound("int_config").getInt("y_offset");
                }

                if (dy != 0) msg += "s: 0 " + dy + " 0\n";
                if (pooled_tag.contains("PosX")) msg += "o: " + pooled_tag.getInt("PosX") + " "
                        + pooled_tag.getInt("PosY") + " " + pooled_tag.getInt("PosZ") + "\n";
                pages.add(StringTag.valueOf(msg));

                book.setTag(nbt);
                book.setHoverName(TComponent.literal("Blueprint"));
                return true;
            }
        }
        return false;
    }

    /**
     * Populates providers, savers and loaders with some defaults if they are
     * empty.
     */
    public static void defaultInit()
    {
        if (providers.isEmpty())
        {
            var parser = DefaultParser.INSTANCE;
            providers.put("jigsaw", parser);
            providers.put("building", parser);
            providers.put("save", parser);
            providers.put("saved", parser);
        }
        if (savers.isEmpty())
        {
            var saver = DefaultSaver.INSTANCE;
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
