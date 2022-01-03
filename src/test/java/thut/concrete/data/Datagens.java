package thut.concrete.data;

import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.compress.utils.Lists;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.registries.RegistryObject;
import thut.api.block.flowing.FlowingBlock;
import thut.concrete.Concrete;

public class Datagens
{
    public static class BStateProvider extends BlockStateProvider
    {
        public BStateProvider(DataGenerator gen, ExistingFileHelper exFileHelper)
        {
            super(gen, Concrete.MODID, exFileHelper);
        }

        @Override
        protected void registerStatesAndModels()
        {
            List<Block> simple_blocks = Lists.newArrayList();

            simple_blocks.add(Concrete.DUST_BLOCK.get());
            simple_blocks.add(Concrete.MOLTEN_BLOCK.get());
            simple_blocks.add(Concrete.SOLID_BLOCK.get());
            for (RegistryObject<FlowingBlock> reg : Concrete.DRY_BLOCK) simple_blocks.add(reg.get());
            for (RegistryObject<Block> reg : Concrete.REF_BLOCK) simple_blocks.add(reg.get());

            for (Block b : simple_blocks) simpleBlock(b);
        }
    }

    public static class BTagsProvider extends BlockTagsProvider
    {

        public BTagsProvider(DataGenerator p_126511_, ExistingFileHelper existingFileHelper)
        {
            super(p_126511_, Concrete.MODID, existingFileHelper);
        }

        @Override
        protected void addTags()
        {
            List<Block> pickaxe_mine = Lists.newArrayList();
            List<Block> iron_mine = Lists.newArrayList();
            pickaxe_mine.add(Concrete.SOLID_BLOCK.get());
            pickaxe_mine.add(Concrete.SOLID_LAYER.get());

            for (RegistryObject<FlowingBlock> reg : Concrete.DRY_LAYER) pickaxe_mine.add(reg.get());
            for (RegistryObject<FlowingBlock> reg : Concrete.DRY_BLOCK) pickaxe_mine.add(reg.get());

            for (RegistryObject<Block> reg : Concrete.REF_LAYER)
            {
                pickaxe_mine.add(reg.get());
                iron_mine.add(reg.get());
            }
            for (RegistryObject<Block> reg : Concrete.REF_BLOCK)
            {
                pickaxe_mine.add(reg.get());
                iron_mine.add(reg.get());
            }

            pickaxe_mine.add(Concrete.REBAR_BLOCK.get());

            TagAppender<Block> tagger = tag(BlockTags.MINEABLE_WITH_PICKAXE);
            tagger.add(pickaxe_mine.toArray(new Block[0]));
            tagger = tag(BlockTags.NEEDS_IRON_TOOL);
            tagger.add(iron_mine.toArray(new Block[0]));
        }
    }

    public static class ITagsProvider extends ItemTagsProvider
    {

        public ITagsProvider(DataGenerator generator, BlockTagsProvider blockTags, ExistingFileHelper helper)
        {
            super(generator, blockTags, Concrete.MODID, helper);
        }

        @Override
        protected void addTags()
        {

        }

        @Override
        public String getName()
        {
            return "Concrete Tags";
        }
    }

    public static class ConcRecipes extends RecipeProvider
    {

        public ConcRecipes(DataGenerator generatorIn)
        {
            super(generatorIn);
        }

        @Override
        protected void buildCraftingRecipes(Consumer<FinishedRecipe> consumer)
        {}
    }

    public static class LangsProvider extends LanguageProvider
    {

        public LangsProvider(DataGenerator gen, String locale)
        {
            super(gen, Concrete.MODID, locale);
        }

        @Override
        protected void addTranslations()
        {
            add(Concrete.VOLCANO.get(), "Volcano source");

            add(Concrete.DUST_LAYER.get(), "Dust layer");
            add(Concrete.DUST_BLOCK.get(), "Dust block");

            add(Concrete.MOLTEN_LAYER.get(), "Lava layer");
            add(Concrete.MOLTEN_BLOCK.get(), "Lava block");

            add(Concrete.SOLID_LAYER.get(), "Solidified lava layer");
            add(Concrete.SOLID_BLOCK.get(), "Solidified lava block");

            add(Concrete.WET_LAYER.get(), "Liquid concrete layer");
            add(Concrete.WET_BLOCK.get(), "Liquid concrete block");

            add(Concrete.REBAR_BLOCK.get(), "Rebar");

            for (int i = 0; i < DyeColor.values().length; i++)
            {
                DyeColor c = DyeColor.values()[i];
                String colour = c.getName().substring(0, 1).toUpperCase() + c.getName().substring(1);

                add(Concrete.DRY_LAYER[i].get(), colour + " concrete layer");
                add(Concrete.DRY_BLOCK[i].get(), colour + " concrete block");

                add(Concrete.REF_LAYER[i].get(), colour + " reinforced concrete layer");
                add(Concrete.REF_BLOCK[i].get(), colour + " reinforced concrete block");
            }

            // Now items
            for (int i = 0; i < DyeColor.values().length; i++)
            {
                DyeColor c = DyeColor.values()[i];
                String colour = c.getName().substring(0, 1).toUpperCase() + c.getName().substring(1);
                add(Concrete.BRUSHES[i].get(), colour + " paint brush");
            }
            add(Concrete.BRUSHES[DyeColor.values().length + 1].get(), "Paint brush");
            add(Concrete.BUCKET.get(), "Bucket of Liquid Concrete");
            add(Concrete.SMOOTHER.get(), "Rock smoother");

            add(Concrete.DUST_ITEM.get(), "Rock dust");
            add(Concrete.CEMENT_ITEM.get(), "Cement");

            add(Concrete.CAO_ITEM.get(), "Calcium Oxide");
            add(Concrete.CACO3_ITEM.get(), "Calcium Carbonate");
        }
    }

    public static class LootTablesProv extends BaseLootTableProvider
    {

        public LootTablesProv(DataGenerator dataGeneratorIn)
        {
            super(dataGeneratorIn);
        }

        @Override
        protected void addTables()
        {
            lootTables.put(Concrete.DUST_BLOCK.get(), createSimpleTable("dust_block", Concrete.DUST_BLOCK.get()));
            lootTables.put(Concrete.SOLID_BLOCK.get(), createSimpleTable("solid_block", Concrete.SOLID_BLOCK.get()));
            lootTables.put(Concrete.REBAR_BLOCK.get(), createSimpleTable("rebar", Concrete.REBAR_BLOCK.get()));

            addSilkyLayer(Concrete.DUST_LAYER.get(), Concrete.DUST_ITEM.get(), "dust_layer");
            addSilkyLayer(Concrete.SOLID_LAYER.get(), Concrete.DUST_ITEM.get(), "solid_layer");

            for (int i = 0; i < DyeColor.values().length; i++)
            {
                DyeColor c = DyeColor.values()[i];
                String colour = c.getName().substring(0, 1).toUpperCase() + c.getName().substring(1);

                addSilkyLayer(Concrete.DRY_LAYER[i].get(), Concrete.DUST_ITEM.get(), colour + " concrete_layer");
                addSimple(Concrete.DRY_BLOCK[i].get(), colour + " concrete_block");

                addSimple(Concrete.REF_LAYER[i].get(), colour + " reinforced_concrete_layer");
                addSimple(Concrete.REF_BLOCK[i].get(), colour + " reinforced_concrete_block");
            }
        }

        private void addSimple(Block block, String string)
        {
            lootTables.put(block, createSimpleTable(string, block));
        }

        private void addSilkyLayer(Block block, Item dust, String string)
        {
            lootTables.put(block, createSilkTouchTableDust(string, block, dust));
        }

    }
}
