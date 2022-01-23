package pokecube.legends.worldgen;

import java.util.Map;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;

import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.SurfaceRules.SurfaceRule;
import net.minecraft.world.level.levelgen.SurfaceSystem;

public class UltraSpaceSurfaceRules
{
    public interface UltraSpaceRuleSource extends Function<SurfaceRules.Context, SurfaceRules.SurfaceRule>
    {
        public static void init()
        {
            Registry.register(Registry.RULE, "pokecube_legends:azure_bandlands", UltraSpaceSurfaceRules.Bandlands.CODEC);
        }

        Codec<? extends UltraSpaceSurfaceRules.UltraSpaceRuleSource> codec();
    }

    private static Map<Block, Block> TERRACOTTA_MAP = Maps.newConcurrentMap();

    public static void init()
    {
        TERRACOTTA_MAP.put(Blocks.TERRACOTTA, Blocks.BLUE_TERRACOTTA);
        TERRACOTTA_MAP.put(Blocks.BROWN_TERRACOTTA, Blocks.PINK_TERRACOTTA);
        TERRACOTTA_MAP.put(Blocks.ORANGE_TERRACOTTA, Blocks.PURPLE_TERRACOTTA);
        TERRACOTTA_MAP.put(Blocks.YELLOW_TERRACOTTA, Blocks.MAGENTA_TERRACOTTA);
        TERRACOTTA_MAP.put(Blocks.RED_TERRACOTTA, Blocks.PURPLE_TERRACOTTA);
        TERRACOTTA_MAP.put(Blocks.WHITE_TERRACOTTA, Blocks.LIGHT_BLUE_TERRACOTTA);
        TERRACOTTA_MAP.put(Blocks.LIGHT_GRAY_TERRACOTTA, Blocks.MAGENTA_TERRACOTTA);
    }

    private static BlockState replaceTerracotta(BlockState state)
    {
        return TERRACOTTA_MAP.containsKey(state.getBlock()) ? TERRACOTTA_MAP.get(state.getBlock()).defaultBlockState() : state;
    }

    public static class TerracottaReplaceRule implements SurfaceRule
    {
        SurfaceSystem system;

        public TerracottaReplaceRule(SurfaceSystem system)
        {
            this.system = system;
        }

        @Nullable
        public BlockState tryApply(int x, int y, int z)
        {
            return replaceTerracotta(system.getBand(x, y, z));
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
            TerracottaReplaceRule rule = new TerracottaReplaceRule(context.system);
            return rule::tryApply;
        }
    }

}
