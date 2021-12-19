package pokecube.legends.worldgen;

import java.util.function.Function;
import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.WorldGenerationContext;

public class UltraSpaceSurfaceRules
{
    public interface UltraSpaceRuleSource extends Function<UltraSpaceSurfaceRules.Context2, UltraSpaceSurfaceRules.UltraSpaceSurfaceRule>
    {

        public static void init()
        {
            Registry.register(Registry.RULE, "pokecube_legends:azure_bandlands", UltraSpaceSurfaceRules.Bandlands.CODEC);
        }

        Codec<? extends UltraSpaceSurfaceRules.UltraSpaceRuleSource> codec();
    }

    public static final class Context2
    {
        long lastUpdateXZ = -9223372036854775807L;
        long lastUpdateY = -9223372036854775807L;
        int blockX;
        int blockZ;
        int surfaceDepth;
        public final UltraSpaceSurfaceSystem system;

        public Context2(UltraSpaceSurfaceSystem system, ChunkAccess chunk, NoiseChunk noise, Function<BlockPos, Biome> posBiome,
                Registry<Biome> biome, WorldGenerationContext genContext)
        {
            this.system = system;
        }

        protected void updateXZ(int p_189570_, int p_189571_)
        {
            ++this.lastUpdateXZ;
            ++this.lastUpdateY;
            this.blockX = p_189570_;
            this.blockZ = p_189571_;
            this.surfaceDepth = this.system.getSurfaceDepth(p_189570_, p_189571_);
        }
    }

    public static enum Bandlands implements SurfaceRules.RuleSource
    {
        INSTANCE;

        static final Codec<UltraSpaceSurfaceRules.Bandlands> CODEC = Codec.unit(INSTANCE);

        public Codec<? extends SurfaceRules.RuleSource> codec()
        {
            return CODEC;
        }

        public SurfaceRules.SurfaceRule apply(SurfaceRules.Context context)
        {
            return context.system::getBand;
        }
    }

    public interface UltraSpaceSurfaceRule
    {
        @Nullable
        BlockState tryApply(int x, int y, int z);
    }

}
