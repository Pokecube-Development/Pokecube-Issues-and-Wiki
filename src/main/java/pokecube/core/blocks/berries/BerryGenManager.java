package pokecube.core.blocks.berries;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.ProcessorRule;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import pokecube.core.PokecubeCore;
import pokecube.core.database.worldgen.WorldgenHandler.JigSawConfig;
import pokecube.core.database.worldgen.WorldgenHandler.JigSawPool;
import pokecube.core.world.gen.template.NotRuleProcessor;

public class BerryGenManager
{
    static class Berries
    {
        public List<BerryPool> pools = Lists.newArrayList();
        public List<BerryJigsaw> jigsaws = Lists.newArrayList();
    }

    static class BerryJigsaw extends JigSawConfig
    {
        List<String> trees = Lists.newArrayList();
        boolean onGrow = false;
    }

    static class BerryPool extends JigSawPool
    {}

    public static final String DATABASES = "database/berries/";

    public static final ResourceLocation REPLACETAG = new ResourceLocation("pokecube:berry_tree_replace");

    public static final ProcessorRule REPLACEABLEONLY = new ProcessorRule(AlwaysTrueTest.INSTANCE,
            new TagMatchTest(TagKey.create(Registry.BLOCK_REGISTRY, BerryGenManager.REPLACETAG)),
            Blocks.STRUCTURE_VOID.defaultBlockState());

    public static final NotRuleProcessor NOREPLACE = new NotRuleProcessor(
            ImmutableList.of(BerryGenManager.REPLACEABLEONLY));

    public static Map<Integer, TreeGrower> trees = Maps.newHashMap();

    public String MODID = PokecubeCore.MODID;
    public ResourceLocation ROOT = new ResourceLocation(PokecubeCore.MODID, "structures/");
    public Berries defaults;

    public BerryGenManager()
    {}

    public BerryGenManager(final String modid)
    {
        this.MODID = modid;
        this.ROOT = new ResourceLocation(this.MODID, "structures/");
    }

    public static interface TreeGrower
    {
        void growTree(ServerLevel world, BlockPos cropPos, int berryId);
    }

    public static void parseConfig()
    {
        // TODO Auto-generated method stub
        
    }

    public static ItemStack getRandomBerryForBiome(Level world, BlockPos blockPosition)
    {
        // TODO Auto-generated method stub
        return ItemStack.EMPTY;
    }
}
