package pokecube.core.world.gen.jigsaw;

import java.util.function.Supplier;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.gen.feature.jigsaw.IJigsawDeserializer;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern;
import net.minecraft.world.gen.feature.jigsaw.JigsawPiece;
import net.minecraft.world.gen.feature.jigsaw.SingleJigsawPiece;
import net.minecraft.world.gen.feature.template.BlockIgnoreStructureProcessor;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.StructureProcessorList;
import net.minecraft.world.gen.feature.template.Template;
import pokecube.core.database.worldgen.WorldgenHandler.JigSawConfig;
import pokecube.core.database.worldgen.WorldgenHandler.Options;

public class CustomJigsawPiece extends SingleJigsawPiece
{
    public static IJigsawDeserializer<CustomJigsawPiece> TYPE;

    public static final Codec<CustomJigsawPiece> CODEC = RecordCodecBuilder.create((instance) ->
    {
        return instance.group(SingleJigsawPiece.func_236846_c_(), SingleJigsawPiece.func_236844_b_(), JigsawPiece
                .func_236848_d_(), CustomJigsawPiece.config(), CustomJigsawPiece.options()).apply(instance,
                        CustomJigsawPiece::new);
    });

    protected static <E extends CustomJigsawPiece> RecordCodecBuilder<E, JigSawConfig> config()
    {
        return JigSawConfig.CODEC.fieldOf("struct_config").forGetter((o) ->
        {
            return o.struct_config;
        });
    }

    protected static <E extends CustomJigsawPiece> RecordCodecBuilder<E, Options> options()
    {
        return Options.CODEC.fieldOf("opts").forGetter((o) ->
        {
            return o.opts;
        });
    }

    public JigSawConfig struct_config;
    public Options      opts;

    protected CustomJigsawPiece(final Either<ResourceLocation, Template> template,
            final Supplier<StructureProcessorList> processors, final JigsawPattern.PlacementBehaviour behaviour,
            final JigSawConfig config, final Options opts)
    {
        super(template, processors, behaviour);
        this.struct_config = config;
        this.opts = opts;
    }

    @Override
    protected PlacementSettings func_230379_a_(final Rotation p_230379_1_, final MutableBoundingBox p_230379_2_,
            final boolean p_230379_3_)
    {
        final PlacementSettings placementsettings = super.func_230379_a_(p_230379_1_, p_230379_2_, p_230379_3_);
        placementsettings.removeProcessor(BlockIgnoreStructureProcessor.STRUCTURE_BLOCK);
        placementsettings.addProcessor(BlockIgnoreStructureProcessor.AIR_AND_STRUCTURE_BLOCK);
        return placementsettings;
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
