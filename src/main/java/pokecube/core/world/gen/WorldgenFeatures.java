package pokecube.core.world.gen;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;

import net.minecraft.core.Holder;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.data.worldgen.ProcessorLists;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.carver.CanyonCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CaveCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.structure.pools.EmptyPoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool.Projection;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import pokecube.core.PokecubeCore;
import pokecube.core.blocks.berries.BerryGenManager;
import pokecube.core.database.worldgen.WorldgenHandler;
import pokecube.core.database.worldgen.WorldgenHandler.JigSawConfig;
import pokecube.core.database.worldgen.WorldgenHandler.JigSawPool;
import pokecube.core.database.worldgen.WorldgenHandler.Options;
import pokecube.core.world.gen.carver.CanyonCarver;
import pokecube.core.world.gen.carver.CaveCarver;
import pokecube.core.world.gen.jigsaw.CustomJigsawPiece;
import pokecube.core.world.gen.template.LadderToGround;
import pokecube.core.world.gen.template.PokecubeStructureProcessor;

public class WorldgenFeatures
{
    public static final DeferredRegister<WorldCarver<?>> CARVERS = DeferredRegister
            .create(ForgeRegistries.WORLD_CARVERS, PokecubeCore.MODID);

    public static final RegistryObject<WorldCarver<?>> CAVE = WorldgenFeatures.CARVERS.register("cave",
            () -> new CaveCarver(CaveCarverConfiguration.CODEC));
    public static final RegistryObject<WorldCarver<?>> CANYON = WorldgenFeatures.CARVERS.register("canyon",
            () -> new CanyonCarver(CanyonCarverConfiguration.CODEC));
    public static final RegistryObject<WorldCarver<?>> OCEAN_CAVE = WorldgenFeatures.CARVERS.register("ocean_cave",
            () -> new CaveCarver(CaveCarverConfiguration.CODEC));

    public static final List<StructureProcessor> BERRYRULES = ImmutableList.of(BerryGenManager.NOREPLACE);
    public static final List<StructureProcessor> GENERICRULES = Lists
            .newArrayList(ProcessorLists.STREET_PLAINS.value().list());
    public static final List<StructureProcessor> LADDERRULES = Lists.newArrayList();

    public static final StructureProcessorList BERRYLIST;
    public static final Holder<StructureProcessorList> GENERICLIST;
    public static final Holder<StructureProcessorList> LADDERFIX;

    private static final Map<ResourceLocation, StructureProcessorList> procLists = Maps.newHashMap();

    static
    {
        WorldgenFeatures.GENERICRULES.add(0, PokecubeStructureProcessor.PROCESSOR);
        LADDERRULES.add(LadderToGround.PROCESSOR);
        // TODO find out why it hates the "berry_gen" list...
        BERRYLIST = new StructureProcessorList(WorldgenFeatures.BERRYRULES);
        GENERICLIST = WorldgenFeatures.register("generic", WorldgenFeatures.GENERICRULES);
        LADDERFIX = WorldgenFeatures.register("ladders_to_ground", WorldgenFeatures.LADDERRULES);

        WorldgenFeatures.procLists.put(new ResourceLocation("pokecube", "berry_gen"), WorldgenFeatures.BERRYLIST);
    }

    public static void init(final IEventBus bus)
    {
        WorldgenFeatures.CARVERS.register(bus);
    }

    public static Holder<StructureProcessorList> getProcList(final String value)
    {
        StructureProcessorList listToUse = null;
        final ResourceLocation key = new ResourceLocation(value);
        if (BuiltinRegistries.PROCESSOR_LIST.keySet().contains(key))
            listToUse = BuiltinRegistries.PROCESSOR_LIST.get(key);
        else listToUse = WorldgenFeatures.procLists.getOrDefault(key, null);
        return Holder.direct(listToUse);
    }

    public static Holder<StructureTemplatePool> register(final JigSawPool pool,
            final Holder<StructureProcessorList> default_list)
    {
        final StructureTemplatePool.Projection placement = pool.rigid ? StructureTemplatePool.Projection.RIGID
                : StructureTemplatePool.Projection.TERRAIN_MATCHING;
        final List<Pair<Function<Projection, ? extends StructurePoolElement>, Integer>> pairs = Lists.newArrayList();
        int size = 0;
        final Holder<StructureProcessorList> listToUse = default_list;

        if (pool.base_override) WorldgenHandler.BASE_OVERRIDES.add(pool);

        for (final String option : pool.options)
        {
            int second = 1;
            final String[] args = option.split(";");
            Options opts = new Options();
            if (args.length > 1)
            {
                opts = Options.deserialize(args[1]);
                second = opts.weight;
            }
            if (!opts.override)
            {
                opts.filler = pool.filler;
                opts.ignoreAir = pool.ignoreAir;
                opts.rigid = pool.rigid;
                opts.water = pool.water;
                opts.proc_list = pool.proc_list;
                opts.base_override = pool.base_override;
            }
            final Pair<Function<Projection, ? extends StructurePoolElement>, Integer> pair = Pair
                    .of(WorldgenFeatures.makePiece(args[0], listToUse, opts), second);
            size += second;
            pairs.add(pair);
        }

        // Now add values from the included pools
        for (final String s : pool.includes)
        {
            final ResourceLocation incl = new ResourceLocation(s);
            if (!BuiltinRegistries.TEMPLATE_POOL.keySet().contains(incl))
            {
                PokecubeCore.LOGGER.error("Warning, No pool by name {} was found!", s);
                continue;
            }
            final StructureTemplatePool toInclude = BuiltinRegistries.TEMPLATE_POOL.get(incl);
            PokecubeCore.LOGGER.debug("Adding parts from {} to {}", s, pool.name);
            toInclude.rawTemplates.forEach(p -> {
                final Pair<Function<Projection, ? extends StructurePoolElement>, Integer> pair = Pair
                        .of(t -> p.getFirst(), p.getSecond());
                pairs.add(pair);
            });
        }

        final StructureTemplatePool pattern = new StructureTemplatePool(new ResourceLocation(pool.name),
                new ResourceLocation(pool.target), pairs, placement);
        PokecubeCore.LOGGER.debug("Registered Pattern/Pool: {}, with target: {}, of size: {}({},{})", pool.name,
                pool.target, size, pairs.size(), pattern.size());
        return Pools.register(pattern);
    }

    public static Holder<StructureProcessorList> register(final String name, final List<StructureProcessor> list)
    {
        final ResourceLocation resourcelocation = new ResourceLocation("pokecube", name);
        final StructureProcessorList structureprocessorlist = new StructureProcessorList(list);
        return BuiltinRegistries.register(BuiltinRegistries.PROCESSOR_LIST, resourcelocation, structureprocessorlist);
    }

    public static Function<StructureTemplatePool.Projection, StructurePoolElement> makePiece(final String name,
            final Holder<StructureProcessorList> list, final Options opts)
    {
        final ResourceLocation resource = new ResourceLocation(name);
        if (resource.getPath().equals("empty")) return (placement) -> {
            return EmptyPoolElement.INSTANCE;
        };
        return (placement) -> {
            return new CustomJigsawPiece(Either.left(resource), list, placement, opts, new JigSawConfig());
        };
    }
}
