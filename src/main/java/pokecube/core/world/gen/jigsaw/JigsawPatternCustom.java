package pokecube.core.world.gen.jigsaw;

import java.util.List;
import java.util.Random;
import java.util.function.BiPredicate;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;

import it.unimi.dsi.fastutil.objects.ObjectArrays;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern;
import net.minecraft.world.gen.feature.jigsaw.JigsawPiece;
import net.minecraft.world.gen.feature.template.TemplateManager;
import pokecube.core.PokecubeCore;
import pokecube.core.database.SpawnBiomeMatcher.SpawnCheck;
import pokecube.core.database.worldgen.WorldgenHandler.JigSawConfig;
import pokecube.core.database.worldgen.WorldgenHandler.JigSawPool;
import pokecube.core.database.worldgen.WorldgenHandler.Options;

public class JigsawPatternCustom extends JigsawPattern
{
    public final List<String> neededChildren     = Lists.newArrayList();
    public JigSawConfig       jigsaw;
    private final JigSawPool  pool;
    private boolean           processed_subpools = false;
    private int               min_value          = Integer.MIN_VALUE;

    public boolean respects_pool_spawns = true;

    // TODO see if this can check a config somehow, and then decide on if true
    // (like if it has a spawn rule)
    public BiPredicate<JigsawPiece, SpawnCheck> validator = (p, s) -> true;

    public JigsawPatternCustom(final JigSawPool part, final List<Pair<JigsawPiece, Integer>> parts)
    {
        super(new ResourceLocation(part.name), new ResourceLocation(part.target), parts);
        this.pool = part;
    }

    public JigsawPatternCustom(final JigSawPool part,
            final List<Pair<Function<JigsawPattern.PlacementBehaviour, ? extends JigsawPiece>, Integer>> parts,
            final JigsawPattern.PlacementBehaviour placementBehaviourIn)
    {
        super(new ResourceLocation(part.name), new ResourceLocation(part.target), parts, placementBehaviourIn);
        this.pool = part;
    }

    public Options getForPiece(final JigsawPiece piece)
    {
        return new Options();
    }

    public List<JigsawPiece> getPieces()
    {
        if (!this.processed_subpools)
        {
            for (final String s : this.pool.includes)
            {
                final ResourceLocation sub_pool = new ResourceLocation(s);
                final JigsawPattern sub = WorldGenRegistries.JIGSAW_POOL.getOrDefault(sub_pool);
                if (sub != null) this.jigsawPieces.addAll(sub.jigsawPieces);
                else PokecubeCore.LOGGER.error("No subpool registered by name {}", sub_pool);
            }
            this.processed_subpools = true;
        }
        return this.jigsawPieces;
    }

    public boolean isValidPos(final JigsawPiece part, final SpawnCheck check)
    {
        return this.validator.test(part, check);
    }

    @Override
    public JigsawPiece getRandomPiece(final Random rand)
    {
        return this.getPieces().get(rand.nextInt(this.getPieces().size()));
    }

    @Override
    public int getNumberOfPieces()
    {
        return this.getPieces().size();
    }

    @Override
    public List<JigsawPiece> getShuffledPieces(final Random rand)
    {
        return ImmutableList.copyOf(ObjectArrays.shuffle(this.getPieces().toArray(new JigsawPiece[0]), rand));
    }

    @Override
    public int getMaxSize(final TemplateManager templateManagerIn)
    {
        if (this.min_value == Integer.MIN_VALUE) this.min_value = this.getPieces().stream().mapToInt((p_214942_1_) ->
        {
            return p_214942_1_.getBoundingBox(templateManagerIn, BlockPos.ZERO, Rotation.NONE).getYSize();
        }).max().orElse(0);
        return this.min_value;
    }
}
