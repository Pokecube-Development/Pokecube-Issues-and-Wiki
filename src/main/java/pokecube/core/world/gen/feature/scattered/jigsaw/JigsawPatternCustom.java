package pokecube.core.world.gen.feature.scattered.jigsaw;

import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern;
import net.minecraft.world.gen.feature.jigsaw.JigsawPiece;

public class JigsawPatternCustom extends JigsawPattern
{
    private final List<JigsawPiece> guarenteed;

    public JigsawPatternCustom(final ResourceLocation nameIn, final ResourceLocation target,
            final List<Pair<JigsawPiece, Integer>> pieces, final PlacementBehaviour placementBehaviourIn)
    {
        super(nameIn, target, pieces, placementBehaviourIn);
        this.guarenteed = Lists.newArrayList();
        for (final Pair<JigsawPiece, Integer> pair : pieces)
            if (pair.getSecond() < 0) this.guarenteed.add(pair.getFirst().setPlacementBehaviour(placementBehaviourIn));
    }

    @Override
    public List<JigsawPiece> getShuffledPieces(final Random rand)
    {
        final List<JigsawPiece> ret = Lists.newArrayList(super.getShuffledPieces(rand));
        for (final JigsawPiece part : this.guarenteed)
            ret.add(0, part);
        return ret;
    }

    @Override
    public int getNumberOfPieces()
    {
        return super.getNumberOfPieces() + this.guarenteed.size();
    }

}
