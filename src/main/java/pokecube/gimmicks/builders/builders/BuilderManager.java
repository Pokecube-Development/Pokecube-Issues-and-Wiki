package pokecube.gimmicks.builders.builders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

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

public class BuilderManager
{
    public static record BuilderClearer(IBlocksBuilder builder, IBlocksClearer clearer, String saveKey)
    {
    }

    public static record BuildContext(ServerLevel level, BlockPos origin)
    {
    }

    public static Map<String, BiFunction<List<String>, BuildContext, BuilderClearer>> providers = new HashMap<>();
    public static Map<String, Function<CompoundTag, BuilderClearer>> loaders = new HashMap<>();
    public static Map<String, Function<BuilderClearer, CompoundTag>> savers = new HashMap<>();

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

    public static BuilderClearer load(CompoundTag nbt)
    {
        String key = nbt.getString("K");
        if (!loaders.containsKey(key)) return null;
        var load = loaders.get(key);
        return load.apply(nbt);
    }

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

    public static class DefaultParser implements BiFunction<List<String>, BuildContext, BuilderClearer>
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
                if (line.startsWith("p:")) offset = line.replace("p:", "").strip();
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

            var args = offset.contains(",") ? offset.split(",") : offset.split(" ");
            int dx = 0;
            int dy = 0;
            int dz = 0;

            if (args.length == 1)
            {
                dy = Integer.parseInt(args[0]);
            }
            else if (args.length == 3)
            {
                dx = Integer.parseInt(args[0]);
                dy = Integer.parseInt(args[1]);
                dz = Integer.parseInt(args[2]);
            }

            shift = new BlockPos(dx, dy, dz);

            if (!_origin.isBlank())
            {
                args = _origin.contains(",") ? _origin.split(",") : _origin.split(" ");
                if (args.length == 3)
                {
                    dx = Integer.parseInt(args[0]);
                    dy = Integer.parseInt(args[1]);
                    dz = Integer.parseInt(args[2]);
                    origin = new BlockPos(dx, dy, dz);
                }
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

    public static BuilderClearer fromInstructions(ItemStack source, BuildContext context)
    {
        defaultInit();
        List<String> instructions = BookInstructionsParser.getInstructions(source, "build");
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
