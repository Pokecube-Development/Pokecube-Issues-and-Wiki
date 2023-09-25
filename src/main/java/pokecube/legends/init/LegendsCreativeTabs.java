package pokecube.legends.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.init.AdvCreativeTabs;
import pokecube.api.entity.pokemob.Nature;
import pokecube.api.utils.PokeType;
import pokecube.core.PokecubeItems;
import pokecube.core.init.CoreCreativeTabs;
import pokecube.legends.Reference;

@Mod.EventBusSubscriber(modid = Reference.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class LegendsCreativeTabs extends CoreCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Reference.ID);

    public static final RegistryObject<CreativeModeTab> BUILDING_BLOCKS_TAB = TABS.register("building_blocks_tab", () -> CreativeModeTab.builder()
            .withBackgroundLocation(new ResourceLocation(Reference.ID, "textures/gui/container/tab_item_search.png"))
            .title(Component.translatable("itemGroup.pokecube_legends.building_blocks"))
            .icon(() -> new ItemStack(BlockInit.DUSK_DOLERITE_BRICKS.get()))
            .withTabsBefore(AdvCreativeTabs.BADGES_TAB.getId())
            .withSearchBar(71)
            .displayItems((parameters, output) -> {
                output.accept(BlockInit.AGED_LOG.get());
                output.accept(BlockInit.AGED_WOOD.get());
                output.accept(BlockInit.STRIP_AGED_LOG.get());
                output.accept(BlockInit.STRIP_AGED_WOOD.get());
                output.accept(BlockInit.AGED_BARREL.get());
                output.accept(BlockInit.AGED_BOOKSHELF.get());
                output.accept(BlockInit.AGED_BOOKSHELF_EMPTY.get());
                output.accept(BlockInit.AGED_PLANKS.get());
                output.accept(BlockInit.AGED_STAIRS.get());
                output.accept(BlockInit.AGED_SLAB.get());
                output.accept(BlockInit.AGED_FENCE.get());
                output.accept(BlockInit.AGED_FENCE_GATE.get());
                output.accept(BlockInit.AGED_SIGN.get());
                output.accept(BlockInit.AGED_DOOR.get());
                output.accept(BlockInit.AGED_TRAPDOOR.get());
                output.accept(BlockInit.AGED_PR_PLATE.get());
                output.accept(BlockInit.AGED_BUTTON.get());

                output.accept(BlockInit.CORRUPTED_LOG.get());
                output.accept(BlockInit.CORRUPTED_WOOD.get());
                output.accept(BlockInit.STRIP_CORRUPTED_LOG.get());
                output.accept(BlockInit.STRIP_CORRUPTED_WOOD.get());
                output.accept(BlockInit.CORRUPTED_BARREL.get());
                output.accept(BlockInit.CORRUPTED_BOOKSHELF.get());
                output.accept(BlockInit.CORRUPTED_BOOKSHELF_EMPTY.get());
                output.accept(BlockInit.CORRUPTED_PLANKS.get());
                output.accept(BlockInit.CORRUPTED_STAIRS.get());
                output.accept(BlockInit.CORRUPTED_SLAB.get());
                output.accept(BlockInit.CORRUPTED_FENCE.get());
                output.accept(BlockInit.CORRUPTED_FENCE_GATE.get());
                output.accept(BlockInit.CORRUPTED_SIGN.get());
                output.accept(BlockInit.CORRUPTED_DOOR.get());
                output.accept(BlockInit.CORRUPTED_TRAPDOOR.get());
                output.accept(BlockInit.CORRUPTED_PR_PLATE.get());
                output.accept(BlockInit.CORRUPTED_BUTTON.get());

                output.accept(BlockInit.DISTORTIC_LOG.get());
                output.accept(BlockInit.DISTORTIC_WOOD.get());
                output.accept(BlockInit.STRIP_DISTORTIC_LOG.get());
                output.accept(BlockInit.STRIP_DISTORTIC_WOOD.get());
                output.accept(BlockInit.DISTORTIC_BARREL.get());
                output.accept(BlockInit.DISTORTIC_BOOKSHELF.get());
                output.accept(BlockInit.DISTORTIC_BOOKSHELF_EMPTY.get());
                output.accept(BlockInit.DISTORTIC_PLANKS.get());
                output.accept(BlockInit.DISTORTIC_STAIRS.get());
                output.accept(BlockInit.DISTORTIC_SLAB.get());
                output.accept(BlockInit.DISTORTIC_FENCE.get());
                output.accept(BlockInit.DISTORTIC_FENCE_GATE.get());
                output.accept(BlockInit.DISTORTIC_SIGN.get());
                output.accept(BlockInit.DISTORTIC_DOOR.get());
                output.accept(BlockInit.DISTORTIC_TRAPDOOR.get());
                output.accept(BlockInit.DISTORTIC_PR_PLATE.get());
                output.accept(BlockInit.DISTORTIC_BUTTON.get());

                output.accept(BlockInit.INVERTED_LOG.get());
                output.accept(BlockInit.INVERTED_WOOD.get());
                output.accept(BlockInit.STRIP_INVERTED_LOG.get());
                output.accept(BlockInit.STRIP_INVERTED_WOOD.get());
                output.accept(BlockInit.INVERTED_BARREL.get());
                output.accept(BlockInit.INVERTED_BOOKSHELF.get());
                output.accept(BlockInit.INVERTED_BOOKSHELF_EMPTY.get());
                output.accept(BlockInit.INVERTED_PLANKS.get());
                output.accept(BlockInit.INVERTED_STAIRS.get());
                output.accept(BlockInit.INVERTED_SLAB.get());
                output.accept(BlockInit.INVERTED_FENCE.get());
                output.accept(BlockInit.INVERTED_FENCE_GATE.get());
                output.accept(BlockInit.INVERTED_SIGN.get());
                output.accept(BlockInit.INVERTED_DOOR.get());
                output.accept(BlockInit.INVERTED_TRAPDOOR.get());
                output.accept(BlockInit.INVERTED_PR_PLATE.get());
                output.accept(BlockInit.INVERTED_BUTTON.get());

                output.accept(BlockInit.MIRAGE_LOG.get());
                output.accept(BlockInit.MIRAGE_WOOD.get());
                output.accept(BlockInit.STRIP_MIRAGE_LOG.get());
                output.accept(BlockInit.STRIP_MIRAGE_WOOD.get());
                output.accept(BlockInit.MIRAGE_BARREL.get());
                output.accept(BlockInit.MIRAGE_BOOKSHELF.get());
                output.accept(BlockInit.MIRAGE_BOOKSHELF_EMPTY.get());
                output.accept(BlockInit.MIRAGE_PLANKS.get());
                output.accept(BlockInit.MIRAGE_STAIRS.get());
                output.accept(BlockInit.MIRAGE_SLAB.get());
                output.accept(BlockInit.MIRAGE_FENCE.get());
                output.accept(BlockInit.MIRAGE_FENCE_GATE.get());
                output.accept(BlockInit.MIRAGE_SIGN.get());
                output.accept(BlockInit.MIRAGE_DOOR.get());
                output.accept(BlockInit.MIRAGE_TRAPDOOR.get());
                output.accept(BlockInit.MIRAGE_PR_PLATE.get());
                output.accept(BlockInit.MIRAGE_BUTTON.get());

                output.accept(BlockInit.TEMPORAL_LOG.get());
                output.accept(BlockInit.TEMPORAL_WOOD.get());
                output.accept(BlockInit.STRIP_TEMPORAL_LOG.get());
                output.accept(BlockInit.STRIP_TEMPORAL_WOOD.get());
                output.accept(BlockInit.TEMPORAL_BARREL.get());
                output.accept(BlockInit.TEMPORAL_BOOKSHELF.get());
                output.accept(BlockInit.TEMPORAL_BOOKSHELF_EMPTY.get());
                output.accept(BlockInit.TEMPORAL_PLANKS.get());
                output.accept(BlockInit.TEMPORAL_STAIRS.get());
                output.accept(BlockInit.TEMPORAL_SLAB.get());
                output.accept(BlockInit.TEMPORAL_FENCE.get());
                output.accept(BlockInit.TEMPORAL_FENCE_GATE.get());
                output.accept(BlockInit.TEMPORAL_SIGN.get());
                output.accept(BlockInit.TEMPORAL_DOOR.get());
                output.accept(BlockInit.TEMPORAL_TRAPDOOR.get());
                output.accept(BlockInit.TEMPORAL_PR_PLATE.get());
                output.accept(BlockInit.TEMPORAL_BUTTON.get());

                output.accept(BlockInit.CONCRETE_LOG.get());
                output.accept(BlockInit.CONCRETE_WOOD.get());
                output.accept(BlockInit.STRIP_CONCRETE_LOG.get());
                output.accept(BlockInit.STRIP_CONCRETE_WOOD.get());
                output.accept(BlockInit.CONCRETE_BARREL.get());
                output.accept(BlockInit.CONCRETE_BOOKSHELF.get());
                output.accept(BlockInit.CONCRETE_BOOKSHELF_EMPTY.get());
                output.accept(BlockInit.CONCRETE_PLANKS.get());
                output.accept(BlockInit.CONCRETE_STAIRS.get());
                output.accept(BlockInit.CONCRETE_SLAB.get());
                output.accept(BlockInit.CONCRETE_FENCE.get());
                output.accept(BlockInit.CONCRETE_FENCE_GATE.get());
                output.accept(BlockInit.CONCRETE_SIGN.get());
                output.accept(BlockInit.CONCRETE_DOOR.get());
                output.accept(BlockInit.CONCRETE_TRAPDOOR.get());
                output.accept(BlockInit.CONCRETE_PR_PLATE.get());
                output.accept(BlockInit.CONCRETE_BUTTON.get());

                output.accept(BlockInit.CONCRETE_DENSE_BARREL.get());
                output.accept(BlockInit.CONCRETE_DENSE_BOOKSHELF.get());
                output.accept(BlockInit.CONCRETE_DENSE_BOOKSHELF_EMPTY.get());
                output.accept(BlockInit.CONCRETE_DENSE_PLANKS.get());
                output.accept(BlockInit.CONCRETE_DENSE_STAIRS.get());
                output.accept(BlockInit.CONCRETE_DENSE_SLAB.get());
                output.accept(BlockInit.CONCRETE_DENSE_WALL.get());
                output.accept(BlockInit.CONCRETE_DENSE_WALL_GATE.get());
                output.accept(BlockInit.CONCRETE_DENSE_SIGN.get());
                output.accept(BlockInit.CONCRETE_DENSE_PR_PLATE.get());
                output.accept(BlockInit.CONCRETE_DENSE_BUTTON.get());

                output.accept(BlockInit.BOOKSHELF_EMPTY.get());

                output.accept(BlockInit.DISTORTIC_OAK_PLANKS.get());
                output.accept(BlockInit.DISTORTIC_OAK_STAIRS.get());
                output.accept(BlockInit.DISTORTIC_OAK_SLAB.get());

                output.accept(BlockInit.DISTORTIC_SPRUCE_PLANKS.get());
                output.accept(BlockInit.DISTORTIC_SPRUCE_STAIRS.get());
                output.accept(BlockInit.DISTORTIC_SPRUCE_SLAB.get());

                output.accept(BlockInit.DISTORTIC_BIRCH_PLANKS.get());
                output.accept(BlockInit.DISTORTIC_BIRCH_STAIRS.get());
                output.accept(BlockInit.DISTORTIC_BIRCH_SLAB.get());

                output.accept(BlockInit.DISTORTIC_JUNGLE_PLANKS.get());
                output.accept(BlockInit.DISTORTIC_JUNGLE_STAIRS.get());
                output.accept(BlockInit.DISTORTIC_JUNGLE_SLAB.get());

                output.accept(BlockInit.DISTORTIC_ACACIA_PLANKS.get());
                output.accept(BlockInit.DISTORTIC_ACACIA_STAIRS.get());
                output.accept(BlockInit.DISTORTIC_ACACIA_SLAB.get());

                output.accept(BlockInit.DISTORTIC_DARK_OAK_PLANKS.get());
                output.accept(BlockInit.DISTORTIC_DARK_OAK_STAIRS.get());
                output.accept(BlockInit.DISTORTIC_DARK_OAK_SLAB.get());

                output.accept(BlockInit.ULTRA_STONE.get());
                output.accept(BlockInit.ULTRA_STONE_STAIRS.get());
                output.accept(BlockInit.ULTRA_STONE_SLAB.get());
                output.accept(BlockInit.ULTRA_STONE_PR_PLATE.get());
                output.accept(BlockInit.ULTRA_STONE_BUTTON.get());

                output.accept(BlockInit.ULTRA_COBBLESTONE.get());
                output.accept(BlockInit.ULTRA_COBBLESTONE_STAIRS.get());
                output.accept(BlockInit.ULTRA_COBBLESTONE_SLAB.get());

                output.accept(BlockInit.ULTRA_STONE_BRICKS.get());
                output.accept(BlockInit.ULTRA_STONE_BRICK_STAIRS.get());
                output.accept(BlockInit.ULTRA_STONE_BRICK_SLAB.get());

                output.accept(BlockInit.ULTRA_DARKSTONE.get());
                output.accept(BlockInit.ULTRA_DARKSTONE_STAIRS.get());
                output.accept(BlockInit.ULTRA_DARKSTONE_SLAB.get());
                output.accept(BlockInit.ULTRA_DARKSTONE_PR_PLATE.get());
                output.accept(BlockInit.ULTRA_DARKSTONE_BUTTON.get());

                output.accept(BlockInit.ULTRA_DARK_COBBLESTONE.get());
                output.accept(BlockInit.ULTRA_DARK_COBBLESTONE_STAIRS.get());
                output.accept(BlockInit.ULTRA_DARK_COBBLESTONE_SLAB.get());

                output.accept(BlockInit.ULTRA_DARKSTONE_BRICKS.get());
                output.accept(BlockInit.ULTRA_DARKSTONE_BRICK_STAIRS.get());
                output.accept(BlockInit.ULTRA_DARKSTONE_BRICK_SLAB.get());

                output.accept(BlockInit.DUSK_DOLERITE.get());
                output.accept(BlockInit.DUSK_DOLERITE_STAIRS.get());
                output.accept(BlockInit.DUSK_DOLERITE_SLAB.get());
                output.accept(BlockInit.DUSK_DOLERITE_PR_PLATE.get());
                output.accept(BlockInit.DUSK_DOLERITE_BUTTON.get());

                output.accept(BlockInit.COBBLED_DUSK_DOLERITE.get());
                output.accept(BlockInit.COBBLED_DUSK_DOLERITE_STAIRS.get());
                output.accept(BlockInit.COBBLED_DUSK_DOLERITE_SLAB.get());

                output.accept(BlockInit.DUSK_DOLERITE_BRICKS.get());
                output.accept(BlockInit.DUSK_DOLERITE_BRICK_STAIRS.get());
                output.accept(BlockInit.DUSK_DOLERITE_BRICK_SLAB.get());

                output.accept(BlockInit.METEORITE_BLOCK.get());
                output.accept(BlockInit.METEORITE_STAIRS.get());
                output.accept(BlockInit.METEORITE_SLAB.get());

                output.accept(BlockInit.AZURE_SANDSTONE.get());
                output.accept(BlockInit.AZURE_SANDSTONE_STAIRS.get());
                output.accept(BlockInit.AZURE_SANDSTONE_SLAB.get());
                output.accept(BlockInit.AZURE_SANDSTONE_PR_PLATE.get());
                output.accept(BlockInit.AZURE_SANDSTONE_BUTTON.get());

                output.accept(BlockInit.SMOOTH_AZURE_SANDSTONE.get());
                output.accept(BlockInit.SMOOTH_AZURE_SANDSTONE_STAIRS.get());
                output.accept(BlockInit.SMOOTH_AZURE_SANDSTONE_SLAB.get());

                output.accept(BlockInit.AZURE_SANDSTONE_BRICKS.get());
                output.accept(BlockInit.AZURE_SANDSTONE_BRICK_STAIRS.get());
                output.accept(BlockInit.AZURE_SANDSTONE_BRICK_SLAB.get());

                output.accept(BlockInit.BLACKENED_SANDSTONE.get());
                output.accept(BlockInit.BLACKENED_SANDSTONE_STAIRS.get());
                output.accept(BlockInit.BLACKENED_SANDSTONE_SLAB.get());
                output.accept(BlockInit.BLACKENED_SANDSTONE_PR_PLATE.get());
                output.accept(BlockInit.BLACKENED_SANDSTONE_BUTTON.get());

                output.accept(BlockInit.SMOOTH_BLACKENED_SANDSTONE.get());
                output.accept(BlockInit.SMOOTH_BLACKENED_SANDSTONE_STAIRS.get());
                output.accept(BlockInit.SMOOTH_BLACKENED_SANDSTONE_SLAB.get());

                output.accept(BlockInit.BLACKENED_SANDSTONE_BRICKS.get());
                output.accept(BlockInit.BLACKENED_SANDSTONE_BRICK_STAIRS.get());
                output.accept(BlockInit.BLACKENED_SANDSTONE_BRICK_SLAB.get());

                output.accept(BlockInit.CRYSTALLIZED_SANDSTONE.get());
                output.accept(BlockInit.CRYS_SANDSTONE_STAIRS.get());
                output.accept(BlockInit.CRYS_SANDSTONE_SLAB.get());
                output.accept(BlockInit.CRYS_SANDSTONE_PR_PLATE.get());
                output.accept(BlockInit.CRYS_SANDSTONE_BUTTON.get());

                output.accept(BlockInit.SMOOTH_CRYS_SANDSTONE.get());
                output.accept(BlockInit.SMOOTH_CRYS_SANDSTONE_STAIRS.get());
                output.accept(BlockInit.SMOOTH_CRYS_SANDSTONE_SLAB.get());

                output.accept(BlockInit.CRYS_SANDSTONE_BRICKS.get());
                output.accept(BlockInit.CRYS_SANDSTONE_BRICK_STAIRS.get());
                output.accept(BlockInit.CRYS_SANDSTONE_BRICK_SLAB.get());

                output.accept(BlockInit.UNREFINED_AQUAMARINE.get());
                output.accept(BlockInit.UNREFINED_AQUAMARINE_STAIRS.get());
                output.accept(BlockInit.UNREFINED_AQUAMARINE_SLAB.get());

                output.accept(BlockInit.AQUAMARINE_BLOCK.get());
                output.accept(BlockInit.AQUAMARINE_STAIRS.get());
                output.accept(BlockInit.AQUAMARINE_SLAB.get());
                output.accept(BlockInit.AQUAMARINE_PR_PLATE.get());
                output.accept(BlockInit.AQUAMARINE_BUTTON.get());

                output.accept(BlockInit.AQUAMARINE_BRICKS.get());
                output.accept(BlockInit.AQUAMARINE_BRICK_STAIRS.get());
                output.accept(BlockInit.AQUAMARINE_BRICK_SLAB.get());

                output.accept(BlockInit.OCEAN_BRICKS.get());
                output.accept(BlockInit.OCEAN_BRICK_STAIRS.get());
                output.accept(BlockInit.OCEAN_BRICK_SLAB.get());

                output.accept(BlockInit.SKY_BRICKS.get());
                output.accept(BlockInit.SKY_BRICK_STAIRS.get());
                output.accept(BlockInit.SKY_BRICK_SLAB.get());

                output.accept(BlockInit.STORMY_SKY_BRICKS.get());
                output.accept(BlockInit.STORMY_SKY_BRICK_STAIRS.get());
                output.accept(BlockInit.STORMY_SKY_BRICK_SLAB.get());

                output.accept(BlockInit.DISTORTIC_TERRACOTTA.get());
                output.accept(BlockInit.DISTORTIC_TERRACOTTA_STAIRS.get());
                output.accept(BlockInit.DISTORTIC_TERRACOTTA_SLAB.get());

                output.accept(BlockInit.MAGMA_BRICKS.get());
                output.accept(BlockInit.MAGMA_BRICK_STAIRS.get());
                output.accept(BlockInit.MAGMA_BRICK_SLAB.get());

                output.accept(BlockInit.PURPUR_BRICKS.get());
                output.accept(BlockInit.PURPUR_BRICK_STAIRS.get());
                output.accept(BlockInit.PURPUR_BRICK_SLAB.get());

                output.accept(BlockInit.GOLEM_STONE.get());
                output.accept(BlockInit.TOTEM_BLOCK.get());
                output.accept(BlockInit.REGICE_CORE.get());
                output.accept(BlockInit.REGIDRAGO_CORE.get());
                output.accept(BlockInit.REGIELEKI_CORE.get());
                output.accept(BlockInit.REGIGIGA_CORE.get());
                output.accept(BlockInit.REGIROCK_CORE.get());
                output.accept(BlockInit.REGISTEEL_CORE.get());

                output.accept(BlockInit.DISTORTIC_STONE.get());
                output.accept(BlockInit.DISTORTIC_STONE_STAIRS.get());
                output.accept(BlockInit.DISTORTIC_STONE_SLAB.get());

                output.accept(BlockInit.DISTORTIC_STONE_BARREL.get());
                output.accept(BlockInit.DISTORTIC_STONE_BRICKS.get());
                output.accept(BlockInit.DISTORTIC_STONE_BRICK_STAIRS.get());
                output.accept(BlockInit.DISTORTIC_STONE_BRICK_SLAB.get());

                output.accept(BlockInit.CHISELED_DISTORTIC_STONE.get());
                output.accept(BlockInit.CHISELED_DISTORTIC_STONE_STAIRS.get());
                output.accept(BlockInit.CHISELED_DISTORTIC_STONE_SLAB.get());

                for (int i = 0; i < BlockInit.unowns.length; i++) {
                    output.accept(BlockInit.UNOWN_STONES[i].get());
                }

                output.accept(BlockInit.MAGNETIC_STONE.get());

                output.accept(BlockInit.SPECTRUM_GLASS.get());
                output.accept(PokecubeAdv.LAB_GLASS.get());
                output.accept(BlockInit.MIRAGE_GLASS.get());
                output.accept(BlockInit.FRAMED_DISTORTIC_MIRROR.get());

                output.accept(BlockInit.ONE_WAY_FRAMED_MIRROR.get());
                output.accept(BlockInit.ONE_WAY_GLASS.get());
                output.accept(BlockInit.ONE_WAY_GLASS_TINTED.get());
                output.accept(BlockInit.ONE_WAY_GLASS_WHITE.get());
                output.accept(BlockInit.ONE_WAY_GLASS_LIGHT_GRAY.get());
                output.accept(BlockInit.ONE_WAY_GLASS_GRAY.get());
                output.accept(BlockInit.ONE_WAY_GLASS_BLACK.get());
                output.accept(BlockInit.ONE_WAY_GLASS_BROWN.get());
                output.accept(BlockInit.ONE_WAY_GLASS_RED.get());
                output.accept(BlockInit.ONE_WAY_GLASS_ORANGE.get());
                output.accept(BlockInit.ONE_WAY_GLASS_YELLOW.get());
                output.accept(BlockInit.ONE_WAY_GLASS_LIME.get());
                output.accept(BlockInit.ONE_WAY_GLASS_GREEN.get());
                output.accept(BlockInit.ONE_WAY_GLASS_CYAN.get());
                output.accept(BlockInit.ONE_WAY_GLASS_LIGHT_BLUE.get());
                output.accept(BlockInit.ONE_WAY_GLASS_BLUE.get());
                output.accept(BlockInit.ONE_WAY_GLASS_PURPLE.get());
                output.accept(BlockInit.ONE_WAY_GLASS_MAGENTA.get());
                output.accept(BlockInit.ONE_WAY_GLASS_PINK.get());
                output.accept(BlockInit.ONE_WAY_GLASS_SPECTRUM.get());
                output.accept(BlockInit.ONE_WAY_GLASS_LAB.get());
                output.accept(BlockInit.ONE_WAY_GLASS_MIRAGE.get());

                output.accept(BlockInit.DISTORTIC_MIRROR.get());
                output.accept(BlockInit.CHISELED_DISTORTIC_MIRROR.get());

                output.accept(BlockInit.COSMIC_DUST_BLOCK.get());
                output.accept(BlockInit.FRACTAL_BLOCK.get());
                output.accept(BlockInit.RUBY_BLOCK.get());
                output.accept(BlockInit.RUBY_STAIRS.get());
                output.accept(BlockInit.RUBY_SLAB.get());
                output.accept(BlockInit.SAPPHIRE_BLOCK.get());
                output.accept(BlockInit.SAPPHIRE_STAIRS.get());
                output.accept(BlockInit.SAPPHIRE_SLAB.get());
                output.accept(BlockInit.SPECTRUM_BLOCK.get());
                output.accept(BlockInit.SPECTRUM_STAIRS.get());
                output.accept(BlockInit.SPECTRUM_SLAB.get());
                output.accept(BlockInit.ULTRA_METAL.get());
                output.accept(BlockInit.ULTRA_METAL_STAIRS.get());
                output.accept(BlockInit.ULTRA_METAL_SLAB.get());
                output.accept(BlockInit.ULTRA_METAL_PR_PLATE.get());
                output.accept(BlockInit.ULTRA_METAL_BUTTON.get());

                output.accept(PokecubeAdv.STATUE.get());

                output.accept(BlockInit.INFECTED_TORCH.get());
                output.accept(BlockInit.INFECTED_LANTERN.get());
                output.accept(BlockInit.INFECTED_CAMPFIRE.get());
            }).build());

    public static final RegistryObject<CreativeModeTab> NATURAL_BLOCKS_TAB = TABS.register("natural_blocks_tab", () -> CreativeModeTab.builder()
            .withBackgroundLocation(new ResourceLocation(Reference.ID, "textures/gui/container/tab_item_search.png"))
            .title(Component.translatable("itemGroup.pokecube_legends.natural_blocks"))
            .icon(() -> new ItemStack(BlockInit.DISTORTIC_GRASS_BLOCK.get()))
            .withTabsBefore(BUILDING_BLOCKS_TAB.getId())
            .withSearchBar(71)
            .displayItems((parameters, output) -> {
                output.accept(BlockInit.AGED_GRASS_BLOCK.get());
                output.accept(BlockInit.AGED_PODZOL.get());
                output.accept(BlockInit.AGED_DIRT.get());
                output.accept(BlockInit.AGED_COARSE_DIRT.get());
                output.accept(BlockInit.AZURE_GRASS_BLOCK.get());
                output.accept(BlockInit.AZURE_DIRT.get());
                output.accept(BlockInit.AZURE_COARSE_DIRT.get());
                output.accept(BlockInit.CORRUPTED_GRASS_BLOCK.get());
                output.accept(BlockInit.CORRUPTED_DIRT.get());
                output.accept(BlockInit.CORRUPTED_COARSE_DIRT.get());
                output.accept(BlockInit.ROOTED_CORRUPTED_DIRT.get());
                output.accept(BlockInit.JUNGLE_GRASS_BLOCK.get());
                output.accept(BlockInit.JUNGLE_PODZOL.get());
                output.accept(BlockInit.JUNGLE_DIRT.get());
                output.accept(BlockInit.JUNGLE_COARSE_DIRT.get());
                output.accept(BlockInit.MUSHROOM_GRASS_BLOCK.get());
                output.accept(BlockInit.FUNGAL_NYLIUM.get());
                output.accept(BlockInit.MUSHROOM_DIRT.get());
                output.accept(BlockInit.MUSHROOM_COARSE_DIRT.get());
                output.accept(BlockInit.ROOTED_MUSHROOM_DIRT.get());


                output.accept(BlockInit.ULTRA_STONE.get());
                output.accept(BlockInit.ULTRA_DARKSTONE.get());
                output.accept(BlockInit.DUSK_DOLERITE.get());
                output.accept(BlockInit.DISTORTIC_STONE.get());
                output.accept(BlockInit.DISTORTIC_GRASS_BLOCK.get());
                output.accept(BlockInit.CRACKED_DISTORTIC_STONE.get());
                output.accept(BlockInit.DISTORTIC_MIRROR.get());
                output.accept(BlockInit.DISTORTIC_GLOWSTONE.get());
                output.accept(BlockInit.METEORITE_BLOCK.get());
                output.accept(BlockInit.METEORITE_MOLTEN_BLOCK.get());
                output.accept(BlockInit.ASH_BLOCK.get());
                output.accept(BlockInit.METEORITE_LAYER.get());
                output.accept(BlockInit.METEORITE_MOLTEN_LAYER.get());
                output.accept(BlockInit.ASH.get());

                output.accept(BlockInit.TURQUOISE_GRAVEL.get());
                output.accept(BlockInit.AZURE_SAND.get());
                output.accept(BlockInit.AZURE_SANDSTONE.get());
                output.accept(BlockInit.BLACKENED_SAND.get());
                output.accept(BlockInit.BLACKENED_SANDSTONE.get());
                output.accept(BlockInit.CRYSTALLIZED_SAND.get());
                output.accept(BlockInit.CRYSTALLIZED_SANDSTONE.get());

                output.accept(BlockInit.ULTRA_COAL_ORE.get());
                output.accept(BlockInit.DUSK_COAL_ORE.get());
                output.accept(BlockInit.ASH_IRON_ORE.get());
                output.accept(BlockInit.ULTRA_IRON_ORE.get());
                output.accept(BlockInit.DUSK_IRON_ORE.get());
                output.accept(BlockInit.ULTRA_COPPER_ORE.get());
                output.accept(BlockInit.DUSK_COPPER_ORE.get());
                output.accept(BlockInit.ULTRA_GOLD_ORE.get());
                output.accept(BlockInit.DUSK_GOLD_ORE.get());
                output.accept(BlockInit.ULTRA_REDSTONE_ORE.get());
                output.accept(BlockInit.DUSK_REDSTONE_ORE.get());
                output.accept(BlockInit.ULTRA_LAPIS_ORE.get());
                output.accept(BlockInit.DUSK_LAPIS_ORE.get());
                output.accept(BlockInit.ULTRA_EMERALD_ORE.get());
                output.accept(BlockInit.DUSK_EMERALD_ORE.get());
                output.accept(BlockInit.ULTRA_DIAMOND_ORE.get());
                output.accept(BlockInit.DUSK_DIAMOND_ORE.get());
                output.accept(BlockInit.RUBY_ORE.get());
                output.accept(BlockInit.DEEPSLATE_RUBY_ORE.get());
                output.accept(BlockInit.ULTRA_RUBY_ORE.get());
                output.accept(BlockInit.DUSK_RUBY_ORE.get());
                output.accept(BlockInit.SAPPHIRE_ORE.get());
                output.accept(BlockInit.DEEPSLATE_SAPPHIRE_ORE.get());
                output.accept(BlockInit.ULTRA_SAPPHIRE_ORE.get());
                output.accept(BlockInit.DUSK_SAPPHIRE_ORE.get());
                output.accept(BlockInit.SPECTRUM_ORE.get());
                output.accept(BlockInit.DUSK_SPECTRUM_ORE.get());
                output.accept(BlockInit.FRACTAL_ORE.get());
                output.accept(BlockInit.METEORITE_COSMIC_ORE.get());
                output.accept(BlockInit.ULTRA_COSMIC_ORE.get());
                output.accept(BlockInit.DUSK_COSMIC_ORE.get());
                output.accept(BlockInit.ULTRA_FOSSIL_ORE.get());
                output.accept(BlockInit.DUSK_FOSSIL_ORE.get());

                output.accept(BlockInit.UNREFINED_AQUAMARINE.get());
                output.accept(BlockInit.BUDDING_AQUAMARINE.get());
                output.accept(BlockInit.AQUAMARINE_CRYSTAL.get());
                output.accept(BlockInit.AQUAMARINE_CLUSTER.get());
                output.accept(BlockInit.LARGE_AQUAMARINE_BUD.get());
                output.accept(BlockInit.MEDIUM_AQUAMARINE_BUD.get());
                output.accept(BlockInit.SMALL_AQUAMARINE_BUD.get());

                output.accept(BlockInit.AGED_LOG.get());
                output.accept(BlockInit.CORRUPTED_LOG.get());
                output.accept(BlockInit.DISTORTIC_LOG.get());
                output.accept(BlockInit.INVERTED_LOG.get());
                output.accept(BlockInit.MIRAGE_LOG.get());
                output.accept(BlockInit.TEMPORAL_LOG.get());

                output.accept(BlockInit.AGED_LEAVES.get());
                output.accept(BlockInit.CORRUPTED_LEAVES.get());
                output.accept(BlockInit.DISTORTIC_LEAVES.get());
                output.accept(BlockInit.INVERTED_LEAVES.get());
                output.accept(BlockInit.MIRAGE_LEAVES.get());
                output.accept(BlockInit.TEMPORAL_LEAVES.get());
                output.accept(BlockInit.DYNA_LEAVES_RED.get());
                output.accept(BlockInit.DYNA_LEAVES_PINK.get());
                output.accept(BlockInit.DYNA_LEAVES_PASTEL_PINK.get());

                output.accept(BlockInit.AGED_SAPLING.get());
                output.accept(BlockInit.CORRUPTED_SAPLING.get());
                output.accept(BlockInit.DISTORTIC_SAPLING.get());
                output.accept(BlockInit.INVERTED_SAPLING.get());
                output.accept(BlockInit.MIRAGE_SAPLING.get());
                output.accept(BlockInit.TEMPORAL_SAPLING.get());
                output.accept(BlockInit.DYNA_SHRUB.get());

                output.accept(BlockInit.CRYSTALLIZED_CACTUS.get());
                output.accept(BlockInit.TALL_CRYSTALLIZED_BUSH.get());
                output.accept(BlockInit.CRYSTALLIZED_BUSH.get());
                output.accept(PlantsInit.AZURE_COLEUS.get());
                output.accept(PlantsInit.COMPRECED_MUSHROOM.get());
                output.accept(PlantsInit.DISTORCED_MUSHROOM.get());
                output.accept(PlantsInit.INVERTED_ORCHID.get());
                output.accept(BlockInit.BIG_CONTAMINATED_DRIPLEAF.get());
                output.accept(BlockInit.SMALL_CONTAMINATED_DRIPLEAF.get());
                output.accept(BlockInit.POLLUTING_BLOSSOM.get());
                output.accept(PlantsInit.TALL_CORRUPTED_GRASS.get());
                output.accept(PlantsInit.CORRUPTED_GRASS.get());
                output.accept(PlantsInit.HANGING_TENDRILS.get());
                output.accept(PlantsInit.PURPLE_WISTERIA_VINES.get());
                output.accept(PlantsInit.TAINTED_ROOTS.get());
                output.accept(PlantsInit.TALL_TAINTED_SEAGRASS.get());
                output.accept(PlantsInit.TAINTED_SEAGRASS.get());
                output.accept(PlantsInit.TAINTED_KELP.get());
                output.accept(PlantsInit.TAINTED_LILY_PAD.get());
                output.accept(PlantsInit.PINK_TAINTED_LILY_PAD.get());
                output.accept(PlantsInit.DISTORTIC_GRASS.get());
                output.accept(PlantsInit.DISTORTIC_VINES.get());
                output.accept(PlantsInit.TEMPORAL_BAMBOO.get());
                output.accept(BlockInit.STRING_OF_PEARLS.get());

                output.accept(PlantsInit.TALL_GOLDEN_GRASS.get());
                output.accept(PlantsInit.GOLDEN_GRASS.get());
                output.accept(PlantsInit.LARGE_GOLDEN_FERN.get());
                output.accept(PlantsInit.GOLDEN_FERN.get());
                output.accept(PlantsInit.GOLDEN_SHROOM_PLANT.get());
                output.accept(PlantsInit.GOLDEN_DANDELION.get());
                output.accept(PlantsInit.GOLDEN_POPPY.get());
                output.accept(PlantsInit.GOLDEN_ORCHID.get());
                output.accept(PlantsInit.GOLDEN_ALLIUM.get());
                output.accept(PlantsInit.GOLDEN_AZURE_BLUET.get());
                output.accept(PlantsInit.GOLDEN_TULIP.get());
                output.accept(PlantsInit.GOLDEN_OXEYE_DAISY.get());
                output.accept(PlantsInit.GOLDEN_CORNFLOWER.get());
                output.accept(PlantsInit.GOLDEN_LILY_VALLEY.get());
                output.accept(PlantsInit.GOLDEN_SWEET_BERRY_BUSH.get());
            }).build());

    public static final RegistryObject<CreativeModeTab> FUNCTIONAL_BLOCKS_TAB = TABS.register("functional_blocks_tab", () -> CreativeModeTab.builder()
            .withBackgroundLocation(new ResourceLocation(Reference.ID, "textures/gui/container/tab_item_search_short.png"))
            .title(Component.translatable("itemGroup.pokecube_legends.functional_blocks"))
            .icon(() -> new ItemStack(ItemInit.GIRATINA_MIRROR.get()))
            .withTabsBefore(NATURAL_BLOCKS_TAB.getId())
            .withSearchBar(53)
            .displayItems((parameters, output) -> {
                output.accept(ItemInit.ULTRA_KEY.get());
                output.accept(ItemInit.GIRATINA_MIRROR.get());

                output.accept(ItemInit.RAINBOW_SWORD.get());
                output.accept(ItemInit.COBALION_SWORD.get());
                output.accept(ItemInit.KELDEO_SWORD.get());
                output.accept(ItemInit.TERRAKION_SWORD.get());
                output.accept(ItemInit.VIRIZION_SWORD.get());
                output.accept(ItemInit.ZACIAN_SWORD.get());
                output.accept(ItemInit.ZAMAZENTA_SHIELD.get());

                output.accept(ItemInit.ULTRA_HELMET.get());
                output.accept(ItemInit.ULTRA_CHESTPLATE.get());
                output.accept(ItemInit.ULTRA_LEGGINGS.get());
                output.accept(ItemInit.ULTRA_BOOTS.get());

                output.accept(ItemInit.DISTORTIC_WATER_BUCKET.get());

                output.accept(BlockInit.RAID_SPAWNER.get());

                output.accept(BlockInit.VICTINI_CORE.get());
                output.accept(BlockInit.TROUGH_BLOCK.get());
                output.accept(BlockInit.TAO_BLOCK.get());
                output.accept(BlockInit.XERNEAS_CORE.get());
                output.accept(BlockInit.NATURE_CORE.get());
                output.accept(BlockInit.TIMESPACE_CORE.get());
                output.accept(BlockInit.KELDEO_CORE.get());
                output.accept(BlockInit.YVELTAL_CORE.get());
                output.accept(BlockInit.TAPU_BULU_CORE.get());
                output.accept(BlockInit.TAPU_KOKO_CORE.get());
                output.accept(BlockInit.TAPU_FINI_CORE.get());
                output.accept(BlockInit.TAPU_LELE_CORE.get());
                output.accept(BlockInit.HEATRAN_BLOCK.get());
                output.accept(BlockInit.LEGENDARY_SPAWN.get());

                output.accept(BlockInit.GOLEM_STONE.get());
                output.accept(BlockInit.TOTEM_BLOCK.get());
                output.accept(BlockInit.REGICE_CORE.get());
                output.accept(BlockInit.REGIDRAGO_CORE.get());
                output.accept(BlockInit.REGIELEKI_CORE.get());
                output.accept(BlockInit.REGIGIGA_CORE.get());
                output.accept(BlockInit.REGIROCK_CORE.get());
                output.accept(BlockInit.REGISTEEL_CORE.get());

                output.accept(BlockInit.DISTORTIC_MIRROR.get());
                output.accept(BlockInit.MAGNETIC_STONE.get());

                output.accept(BlockInit.AGED_BARREL.get());
                output.accept(BlockInit.CONCRETE_BARREL.get());
                output.accept(BlockInit.CONCRETE_DENSE_BARREL.get());
                output.accept(BlockInit.CORRUPTED_BARREL.get());
                output.accept(BlockInit.DISTORTIC_BARREL.get());
                output.accept(BlockInit.INVERTED_BARREL.get());
                output.accept(BlockInit.MIRAGE_BARREL.get());
                output.accept(BlockInit.TEMPORAL_BARREL.get());
                output.accept(BlockInit.DISTORTIC_STONE_BARREL.get());

                output.accept(BlockInit.BOOKSHELF_EMPTY.get());
                output.accept(BlockInit.AGED_BOOKSHELF.get());
                output.accept(BlockInit.AGED_BOOKSHELF_EMPTY.get());
                output.accept(BlockInit.CONCRETE_BOOKSHELF.get());
                output.accept(BlockInit.CONCRETE_BOOKSHELF_EMPTY.get());
                output.accept(BlockInit.CONCRETE_DENSE_BOOKSHELF.get());
                output.accept(BlockInit.CONCRETE_DENSE_BOOKSHELF_EMPTY.get());
                output.accept(BlockInit.CORRUPTED_BOOKSHELF.get());
                output.accept(BlockInit.CORRUPTED_BOOKSHELF_EMPTY.get());
                output.accept(BlockInit.DISTORTIC_BOOKSHELF.get());
                output.accept(BlockInit.DISTORTIC_BOOKSHELF_EMPTY.get());
                output.accept(BlockInit.INVERTED_BOOKSHELF.get());
                output.accept(BlockInit.INVERTED_BOOKSHELF_EMPTY.get());
                output.accept(BlockInit.MIRAGE_BOOKSHELF.get());
                output.accept(BlockInit.MIRAGE_BOOKSHELF_EMPTY.get());
                output.accept(BlockInit.TEMPORAL_BOOKSHELF.get());
                output.accept(BlockInit.TEMPORAL_BOOKSHELF_EMPTY.get());

                output.accept(BlockInit.INFECTED_TORCH.get());
                output.accept(BlockInit.INFECTED_LANTERN.get());
                output.accept(BlockInit.INFECTED_CAMPFIRE.get());

                output.accept(BlockInit.AGED_SIGN.get());
                output.accept(BlockInit.CONCRETE_SIGN.get());
                output.accept(BlockInit.CONCRETE_DENSE_SIGN.get());
                output.accept(BlockInit.CORRUPTED_SIGN.get());
                output.accept(BlockInit.DISTORTIC_SIGN.get());
                output.accept(BlockInit.INVERTED_SIGN.get());
                output.accept(BlockInit.MIRAGE_SIGN.get());
                output.accept(BlockInit.TEMPORAL_SIGN.get());

                output.accept(PokecubeItems.getStack("pokecube_legends:aged_boat"));
                output.accept(PokecubeItems.getStack("pokecube_legends:aged_chest_boat"));
                output.accept(PokecubeItems.getStack("pokecube_legends:concrete_boat"));
                output.accept(PokecubeItems.getStack("pokecube_legends:concrete_chest_boat"));
                output.accept(PokecubeItems.getStack("pokecube_legends:corrupted_boat"));
                output.accept(PokecubeItems.getStack("pokecube_legends:corrupted_chest_boat"));
                output.accept(PokecubeItems.getStack("pokecube_legends:distortic_boat"));
                output.accept(PokecubeItems.getStack("pokecube_legends:distortic_chest_boat"));
                output.accept(PokecubeItems.getStack("pokecube_legends:inverted_boat"));
                output.accept(PokecubeItems.getStack("pokecube_legends:inverted_chest_boat"));
                output.accept(PokecubeItems.getStack("pokecube_legends:mirage_boat"));
                output.accept(PokecubeItems.getStack("pokecube_legends:mirage_chest_boat"));
                output.accept(PokecubeItems.getStack("pokecube_legends:temporal_boat"));
                output.accept(PokecubeItems.getStack("pokecube_legends:temporal_chest_boat"));

                for (int i = 0; i < BlockInit.totemKeys.length; i++)
                {
                    output.accept(BlockInit.BULU[i].get());
                }

                for (int i = 0; i < BlockInit.totemKeys.length; i++)
                {
                    output.accept(BlockInit.KOKO[i].get());
                }

                for (int i = 0; i < BlockInit.totemKeys.length; i++)
                {
                    output.accept(BlockInit.FINI[i].get());
                }

                for (int i = 0; i < BlockInit.totemKeys.length; i++)
                {
                    output.accept(BlockInit.LELE[i].get());
                }
            }).build());

    public static final RegistryObject<CreativeModeTab> ITEMS_TAB = TABS.register("items_tab", () -> CreativeModeTab.builder()
            .withBackgroundLocation(new ResourceLocation(Reference.ID, "textures/gui/container/tab_item_search.png"))
            .title(Component.translatable("itemGroup.pokecube_legends.items"))
            .icon(() -> new ItemStack(ItemInit.RAINBOW_ORB.get()))
            .withTabsBefore(FUNCTIONAL_BLOCKS_TAB.getId())
            .withSearchBar(71)
            .displayItems((parameters, output) -> {
                output.accept(ItemInit.DIAMOND_GEM.get());
                output.accept(ItemInit.RUBY.get());
                output.accept(ItemInit.SAPPHIRE.get());
                output.accept(ItemInit.AQUAMARINE.get());
                output.accept(ItemInit.AQUAMARINE_SHARD.get());
                output.accept(ItemInit.FRACTAL_SHARD.get());
                output.accept(ItemInit.SPECTRUM_SHARD.get());
                output.accept(ItemInit.COSMIC_DUST.get());
                output.accept(ItemInit.PILE_OF_ASH.get());

                output.accept(ItemInit.HEAD_MIRROR.get());
                output.accept(ItemInit.BODY_MIRROR.get());
                output.accept(ItemInit.GLASS_MIRROR.get());

                output.accept(ItemInit.WISHING_PIECE.get());

                output.accept(ItemInit.RUSTED_SWORD.get());
                output.accept(ItemInit.RUSTED_SHIELD.get());
                output.accept(ItemInit.IMPRISIONMENT_HELMET.get());
                output.accept(ItemInit.WOODEN_CROWN.get());

                output.accept(ItemInit.ORANGE_RUNE.get());
                output.accept(ItemInit.GREEN_RUNE.get());
                output.accept(ItemInit.BLUE_RUNE.get());
                output.accept(ItemInit.LELE_ORB.get());
                output.accept(ItemInit.BULU_ORB.get());
                output.accept(ItemInit.KOKO_ORB.get());
                output.accept(ItemInit.FINI_ORB.get());
                output.accept(ItemInit.LIGHT_STONE.get());
                output.accept(ItemInit.DARK_STONE.get());
                output.accept(ItemInit.LIFE_ORB.get());
                output.accept(ItemInit.LUSTROUS_ORB.get());
                output.accept(ItemInit.GRAY_ORB.get());
                output.accept(ItemInit.DESTRUCT_ORB.get());
                output.accept(ItemInit.RED_ORB.get());
                output.accept(ItemInit.FLAME_GEM.get());
                output.accept(ItemInit.THUNDER_GEM.get());
                output.accept(ItemInit.GREEN_ORB.get());
                output.accept(ItemInit.REGIS_ORB.get());
                output.accept(ItemInit.WATER_GEM.get());
                output.accept(ItemInit.COSMIC_ORB.get());
                output.accept(ItemInit.BLUE_ORB.get());
                output.accept(ItemInit.OCEAN_ORB.get());
                output.accept(ItemInit.SOUL_DEW.get());
                output.accept(ItemInit.RAINBOW_ORB.get());

                output.accept(ItemInit.ICE_CARROT.get());
                output.accept(ItemInit.SHADOW_CARROT.get());
                output.accept(ItemInit.DARK_FIRE_WING.get());
                output.accept(ItemInit.FIRE_WING.get());
                output.accept(ItemInit.STATIC_WING.get());
                output.accept(ItemInit.ELECTRIC_WING.get());
                output.accept(ItemInit.LUNAR_WING.get());
                output.accept(ItemInit.SILVER_WING.get());
                output.accept(ItemInit.ICE_WING.get());
                output.accept(ItemInit.ICE_DARK_WING.get());
                output.accept(ItemInit.RAINBOW_WING.get());

                output.accept(ItemInit.DNA_SPLICER_A.get());
                output.accept(ItemInit.DNA_SPLICER_B.get());
                output.accept(ItemInit.N_LUNARIZER.get());
                output.accept(ItemInit.N_SOLARIZER.get());
                output.accept(ItemInit.GRACIDEA.get());
                output.accept(ItemInit.METEORITE.get());
                output.accept(ItemInit.METEOR_SHARD.get());
                output.accept(ItemInit.CHIPPED_POT.get());
                output.accept(ItemInit.CRACKED_POT.get());
                output.accept(ItemInit.GALARCUFF.get());
                output.accept(ItemInit.GALARWREATH.get());
                output.accept(ItemInit.EMBLEM.get());
                output.accept(ItemInit.GIGANTIC_SHARD.get());
                output.accept(ItemInit.NIGHTMARE_BOOK.get());
                output.accept(ItemInit.PARCHMENT_DARK.get());
                output.accept(ItemInit.PARCHMENT_WATER.get());
                output.accept(ItemInit.REINS_UNITY.get());

                output.accept(ItemInit.SOUL_HEART.get());
                output.accept(ItemInit.ADAMANT_ORB.get());
                output.accept(ItemInit.GRISEOUS_ORB.get());
                output.accept(ItemInit.AZELF_GEM.get());
                output.accept(ItemInit.MESPRIT_GEM.get());
                output.accept(ItemInit.UXIE_GEM.get());
                output.accept(ItemInit.ANCIENT_STONE.get());
                output.accept(ItemInit.KYUREM_CORE.get());
                output.accept(ItemInit.ROCK_CORE.get());
                output.accept(ItemInit.DRAGO_CORE.get());
                output.accept(ItemInit.ICE_CORE.get());
                output.accept(ItemInit.MAGMA_CORE.get());
                output.accept(ItemInit.STAR_CORE.get());
                output.accept(ItemInit.STEAM_CORE.get());
                output.accept(ItemInit.STEEL_CORE.get());
                output.accept(ItemInit.THUNDER_CORE.get());
                output.accept(ItemInit.AZURE_FLUTE.get());
                output.accept(ItemInit.KUBFU_SCARF.get());
                output.accept(ItemInit.LIGHTING_CRYSTAL.get());
                output.accept(ItemInit.MANAPHY_NECKLACE.get());
                output.accept(ItemInit.MELOETTA_OCARINA.get());
                output.accept(ItemInit.ZYGARDE_CUBE.get());

                output.accept(PokecubeItems.getStack("pokecube_legends:mint_docile"));
                output.accept(PokecubeItems.getStack("pokecube_legends:mint_calm"));
                output.accept(PokecubeItems.getStack("pokecube_legends:mint_gentle"));
                output.accept(PokecubeItems.getStack("pokecube_legends:mint_sassy"));
                output.accept(PokecubeItems.getStack("pokecube_legends:mint_careful"));
                output.accept(PokecubeItems.getStack("pokecube_legends:mint_hardy"));
                output.accept(PokecubeItems.getStack("pokecube_legends:mint_lonely"));
                output.accept(PokecubeItems.getStack("pokecube_legends:mint_brave"));
                output.accept(PokecubeItems.getStack("pokecube_legends:mint_adamant"));
                output.accept(PokecubeItems.getStack("pokecube_legends:mint_naughty"));
                output.accept(PokecubeItems.getStack("pokecube_legends:mint_serious"));
                output.accept(PokecubeItems.getStack("pokecube_legends:mint_timid"));
                output.accept(PokecubeItems.getStack("pokecube_legends:mint_hasty"));
                output.accept(PokecubeItems.getStack("pokecube_legends:mint_jolly"));
                output.accept(PokecubeItems.getStack("pokecube_legends:mint_naive"));
                output.accept(PokecubeItems.getStack("pokecube_legends:mint_modest"));
                output.accept(PokecubeItems.getStack("pokecube_legends:mint_mild"));
                output.accept(PokecubeItems.getStack("pokecube_legends:mint_quiet"));
                output.accept(PokecubeItems.getStack("pokecube_legends:mint_rash"));
                output.accept(PokecubeItems.getStack("pokecube_legends:mint_quirky"));
                output.accept(PokecubeItems.getStack("pokecube_legends:mint_bold"));
                output.accept(PokecubeItems.getStack("pokecube_legends:mint_relaxed"));
                output.accept(PokecubeItems.getStack("pokecube_legends:mint_impish"));
                output.accept(PokecubeItems.getStack("pokecube_legends:mint_lax"));
                output.accept(PokecubeItems.getStack("pokecube_legends:mint_bashful"));

                for (final Nature type : ItemInit.mints.keySet())
                    output.accept(ItemInit.mints.get(type).get());

                output.accept(PokecubeItems.getStack("pokecube_legends:z_unknown"));
                output.accept(PokecubeItems.getStack("pokecube_legends:z_normal"));
                output.accept(PokecubeItems.getStack("pokecube_legends:z_steel"));
                output.accept(PokecubeItems.getStack("pokecube_legends:z_rock"));
                output.accept(PokecubeItems.getStack("pokecube_legends:z_dark"));
                output.accept(PokecubeItems.getStack("pokecube_legends:z_fire"));
                output.accept(PokecubeItems.getStack("pokecube_legends:z_ground"));
                output.accept(PokecubeItems.getStack("pokecube_legends:z_fighting"));
                output.accept(PokecubeItems.getStack("pokecube_legends:z_electric"));
                output.accept(PokecubeItems.getStack("pokecube_legends:z_bug"));
                output.accept(PokecubeItems.getStack("pokecube_legends:z_grass"));
                output.accept(PokecubeItems.getStack("pokecube_legends:z_dragon"));
                output.accept(PokecubeItems.getStack("pokecube_legends:z_ice"));
                output.accept(PokecubeItems.getStack("pokecube_legends:z_water"));
                output.accept(PokecubeItems.getStack("pokecube_legends:z_flying"));
                output.accept(PokecubeItems.getStack("pokecube_legends:z_poison"));
                output.accept(PokecubeItems.getStack("pokecube_legends:z_ghost"));
                output.accept(PokecubeItems.getStack("pokecube_legends:z_psychic"));
                output.accept(PokecubeItems.getStack("pokecube_legends:z_fairy"));

                for (final PokeType type : ItemInit.zCrystals.keySet())
                    output.accept(ItemInit.zCrystals.get(type).get());
            }).build());

    @SubscribeEvent
    public static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS)
        {
            add(event, Items.GOLDEN_CARROT, ItemInit.ICE_CARROT.get());
            add(event, ItemInit.ICE_CARROT.get(), ItemInit.SHADOW_CARROT.get());

            add(event, Items.CAKE, ItemInit.NULL_POKEPUFF.get());
            add(event, ItemInit.NULL_POKEPUFF.get(), ItemInit.CHERI_POKEPUFF.get());
            add(event, ItemInit.CHERI_POKEPUFF.get(), ItemInit.CHESTO_POKEPUFF.get());
            add(event, ItemInit.CHESTO_POKEPUFF.get(), ItemInit.PECHA_POKEPUFF.get());
            add(event, ItemInit.PECHA_POKEPUFF.get(), ItemInit.RAWST_POKEPUFF.get());
            add(event, ItemInit.RAWST_POKEPUFF.get(), ItemInit.ASPEAR_POKEPUFF.get());
            add(event, ItemInit.ASPEAR_POKEPUFF.get(), ItemInit.LEPPA_POKEPUFF.get());
            add(event, ItemInit.LEPPA_POKEPUFF.get(), ItemInit.ORAN_POKEPUFF.get());
            add(event, ItemInit.ORAN_POKEPUFF.get(), ItemInit.PERSIM_POKEPUFF.get());
            add(event, ItemInit.PERSIM_POKEPUFF.get(), ItemInit.LUM_POKEPUFF.get());
            add(event, ItemInit.LUM_POKEPUFF.get(), ItemInit.SITRUS_POKEPUFF.get());
            add(event, ItemInit.SITRUS_POKEPUFF.get(), ItemInit.NANAB_POKEPUFF.get());
            add(event, ItemInit.NANAB_POKEPUFF.get(), ItemInit.PINAP_POKEPUFF.get());
            add(event, ItemInit.PINAP_POKEPUFF.get(), ItemInit.POMEG_POKEPUFF.get());
            add(event, ItemInit.POMEG_POKEPUFF.get(), ItemInit.KELPSY_POKEPUFF.get());
            add(event, ItemInit.KELPSY_POKEPUFF.get(), ItemInit.QUALOT_POKEPUFF.get());
            add(event, ItemInit.QUALOT_POKEPUFF.get(), ItemInit.HONDEW_POKEPUFF.get());
            add(event, ItemInit.HONDEW_POKEPUFF.get(), ItemInit.GREPA_POKEPUFF.get());
            add(event, ItemInit.GREPA_POKEPUFF.get(), ItemInit.TAMATO_POKEPUFF.get());
            add(event, ItemInit.TAMATO_POKEPUFF.get(), ItemInit.CORNN_POKEPUFF.get());
            add(event, ItemInit.CORNN_POKEPUFF.get(), ItemInit.ENIGMA_POKEPUFF.get());
            add(event, ItemInit.ENIGMA_POKEPUFF.get(), ItemInit.JABOCA_POKEPUFF.get());
            add(event, ItemInit.JABOCA_POKEPUFF.get(), ItemInit.ROWAP_POKEPUFF.get());
        }

        if (event.getTabKey().equals(BERRIES_TAB.getKey()))
        {
            add(event, ItemInit.NULL_POKEPUFF.get());
            add(event, ItemInit.CHERI_POKEPUFF.get());
            add(event, ItemInit.CHESTO_POKEPUFF.get());
            add(event, ItemInit.PECHA_POKEPUFF.get());
            add(event, ItemInit.RAWST_POKEPUFF.get());
            add(event, ItemInit.ASPEAR_POKEPUFF.get());
            add(event, ItemInit.LEPPA_POKEPUFF.get());
            add(event, ItemInit.ORAN_POKEPUFF.get());
            add(event, ItemInit.PERSIM_POKEPUFF.get());
            add(event, ItemInit.LUM_POKEPUFF.get());
            add(event, ItemInit.SITRUS_POKEPUFF.get());
            add(event, ItemInit.NANAB_POKEPUFF.get());
            add(event, ItemInit.PINAP_POKEPUFF.get());
            add(event, ItemInit.POMEG_POKEPUFF.get());
            add(event, ItemInit.KELPSY_POKEPUFF.get());
            add(event, ItemInit.QUALOT_POKEPUFF.get());
            add(event, ItemInit.HONDEW_POKEPUFF.get());
            add(event, ItemInit.GREPA_POKEPUFF.get());
            add(event, ItemInit.TAMATO_POKEPUFF.get());
            add(event, ItemInit.CORNN_POKEPUFF.get());
            add(event, ItemInit.ENIGMA_POKEPUFF.get());
            add(event, ItemInit.JABOCA_POKEPUFF.get());
            add(event, ItemInit.ROWAP_POKEPUFF.get());
        }

        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS)
        {
            add(event, ItemInit.DIAMOND_GEM.get());
            add(event, ItemInit.RUBY.get());
            add(event, ItemInit.SAPPHIRE.get());
            add(event, ItemInit.AQUAMARINE.get());
            add(event, ItemInit.AQUAMARINE_SHARD.get());
            add(event, ItemInit.FRACTAL_SHARD.get());
            add(event, ItemInit.SPECTRUM_SHARD.get());
            add(event, ItemInit.COSMIC_DUST.get());
            add(event, ItemInit.PILE_OF_ASH.get());

            add(event, ItemInit.HEAD_MIRROR.get());
            add(event, ItemInit.BODY_MIRROR.get());
            add(event, ItemInit.GLASS_MIRROR.get());
        }

        if (event.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS)
        {
            add(event, PokecubeAdv.WARP_PAD.get());
            add(event, BlockInit.DISTORTIC_STONE_BARREL.get());
        }

        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS)
        {
            add(event, BlockInit.RAID_SPAWNER.get());
            add(event, PokecubeAdv.CLONER.get());
            add(event, PokecubeAdv.EXTRACTOR.get());
            add(event, PokecubeAdv.SPLICER.get());
            add(event, PokecubeAdv.SIPHON.get());
            add(event, BlockInit.CRAMOMATIC_BLOCK.get());

            add(event, PokecubeAdv.WARP_PAD.get());
            add(event, PokecubeAdv.AFA.get());
            add(event, PokecubeAdv.COMMANDER.get());
            add(event, PokecubeAdv.DAYCARE.get());
            add(event, BlockInit.DISTORTIC_STONE_BARREL.get());
            add(event, BlockInit.MAGNETIC_STONE.get());
            add(event, BlockInit.DISTORTIC_MIRROR.get());

            add(event, BlockInit.AGED_SIGN.get());
            add(event, BlockInit.CONCRETE_SIGN.get());
            add(event, BlockInit.CONCRETE_DENSE_SIGN.get());
            add(event, BlockInit.CORRUPTED_SIGN.get());
            add(event, BlockInit.DISTORTIC_SIGN.get());
            add(event, BlockInit.INVERTED_SIGN.get());
            add(event, BlockInit.MIRAGE_SIGN.get());
            add(event, BlockInit.TEMPORAL_SIGN.get());

            add(event, BlockInit.INFECTED_TORCH.get());
            add(event, BlockInit.INFECTED_LANTERN.get());
            add(event, BlockInit.INFECTED_CAMPFIRE.get());

            add(event, PokecubeAdv.STATUE.get());
        }

        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES)
        {
            add(event, ItemInit.ULTRA_KEY.get());
            add(event, ItemInit.GIRATINA_MIRROR.get());
            add(event, ItemInit.DISTORTIC_WATER_BUCKET.get());
            add(event, PokecubeItems.getStack("pokecube_legends:aged_boat"));
            add(event, PokecubeItems.getStack("pokecube_legends:aged_chest_boat"));
            add(event, PokecubeItems.getStack("pokecube_legends:concrete_boat"));
            add(event, PokecubeItems.getStack("pokecube_legends:concrete_chest_boat"));
            add(event, PokecubeItems.getStack("pokecube_legends:corrupted_boat"));
            add(event, PokecubeItems.getStack("pokecube_legends:corrupted_chest_boat"));
            add(event, PokecubeItems.getStack("pokecube_legends:distortic_boat"));
            add(event, PokecubeItems.getStack("pokecube_legends:distortic_chest_boat"));
            add(event, PokecubeItems.getStack("pokecube_legends:inverted_boat"));
            add(event, PokecubeItems.getStack("pokecube_legends:inverted_chest_boat"));
            add(event, PokecubeItems.getStack("pokecube_legends:mirage_boat"));
            add(event, PokecubeItems.getStack("pokecube_legends:mirage_chest_boat"));
            add(event, PokecubeItems.getStack("pokecube_legends:temporal_boat"));
            add(event, PokecubeItems.getStack("pokecube_legends:temporal_chest_boat"));
        }

        if (event.getTabKey() == CreativeModeTabs.COMBAT)
        {
            add(event, ItemInit.RAINBOW_SWORD.get());
            add(event, ItemInit.COBALION_SWORD.get());
            add(event, ItemInit.KELDEO_SWORD.get());
            add(event, ItemInit.TERRAKION_SWORD.get());
            add(event, ItemInit.VIRIZION_SWORD.get());
            add(event, ItemInit.ZACIAN_SWORD.get());
            add(event, ItemInit.ZAMAZENTA_SHIELD.get());

            add(event, ItemInit.ULTRA_HELMET.get());
            add(event, ItemInit.ULTRA_CHESTPLATE.get());
            add(event, ItemInit.ULTRA_LEGGINGS.get());
            add(event, ItemInit.ULTRA_BOOTS.get());
        }

        if (event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS)
        {
            add(event, BlockInit.ASH_IRON_ORE.get());
            add(event, BlockInit.RUBY_ORE.get());
            add(event, BlockInit.DEEPSLATE_RUBY_ORE.get());
            add(event, BlockInit.SAPPHIRE_ORE.get());
            add(event, BlockInit.DEEPSLATE_SAPPHIRE_ORE.get());
            add(event, BlockInit.METEORITE_COSMIC_ORE.get());
            add(event, BlockInit.METEORITE_BLOCK.get());
            add(event, BlockInit.METEORITE_MOLTEN_BLOCK.get());
            add(event, BlockInit.ASH_BLOCK.get());
            add(event, BlockInit.METEORITE_LAYER.get());
            add(event, BlockInit.METEORITE_MOLTEN_LAYER.get());
            add(event, BlockInit.ASH.get());
        }

        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS)
        {
            add(event, BlockInit.BOOKSHELF_EMPTY.get());

            add(event, BlockInit.DISTORTIC_OAK_PLANKS.get());
            add(event, BlockInit.DISTORTIC_OAK_STAIRS.get());
            add(event, BlockInit.DISTORTIC_OAK_SLAB.get());

            add(event, BlockInit.DISTORTIC_SPRUCE_PLANKS.get());
            add(event, BlockInit.DISTORTIC_SPRUCE_STAIRS.get());
            add(event, BlockInit.DISTORTIC_SPRUCE_SLAB.get());

            add(event, BlockInit.DISTORTIC_BIRCH_PLANKS.get());
            add(event, BlockInit.DISTORTIC_BIRCH_STAIRS.get());
            add(event, BlockInit.DISTORTIC_BIRCH_SLAB.get());

            add(event, BlockInit.DISTORTIC_JUNGLE_PLANKS.get());
            add(event, BlockInit.DISTORTIC_JUNGLE_STAIRS.get());
            add(event, BlockInit.DISTORTIC_JUNGLE_SLAB.get());

            add(event, BlockInit.DISTORTIC_ACACIA_PLANKS.get());
            add(event, BlockInit.DISTORTIC_ACACIA_STAIRS.get());
            add(event, BlockInit.DISTORTIC_ACACIA_SLAB.get());

            add(event, BlockInit.DISTORTIC_DARK_OAK_PLANKS.get());
            add(event, BlockInit.DISTORTIC_DARK_OAK_STAIRS.get());
            add(event, BlockInit.DISTORTIC_DARK_OAK_SLAB.get());

            add(event, BlockInit.METEORITE_BLOCK.get());
            add(event, BlockInit.METEORITE_STAIRS.get());
            add(event, BlockInit.METEORITE_SLAB.get());

            add(event, BlockInit.OCEAN_BRICKS.get());
            add(event, BlockInit.OCEAN_BRICK_STAIRS.get());
            add(event, BlockInit.OCEAN_BRICK_SLAB.get());

            add(event, BlockInit.SKY_BRICKS.get());
            add(event, BlockInit.SKY_BRICK_STAIRS.get());
            add(event, BlockInit.SKY_BRICK_SLAB.get());

            add(event, BlockInit.STORMY_SKY_BRICKS.get());
            add(event, BlockInit.STORMY_SKY_BRICK_STAIRS.get());
            add(event, BlockInit.STORMY_SKY_BRICK_SLAB.get());

            add(event, BlockInit.MAGMA_BRICKS.get());
            add(event, BlockInit.MAGMA_BRICK_STAIRS.get());
            add(event, BlockInit.MAGMA_BRICK_SLAB.get());

            add(event, BlockInit.PURPUR_BRICKS.get());
            add(event, BlockInit.PURPUR_BRICK_STAIRS.get());
            add(event, BlockInit.PURPUR_BRICK_SLAB.get());

            add(event, BlockInit.TOTEM_BLOCK.get());
            add(event, BlockInit.GOLEM_STONE.get());
            add(event, BlockInit.REGICE_CORE.get());
            add(event, BlockInit.REGIDRAGO_CORE.get());
            add(event, BlockInit.REGIELEKI_CORE.get());
            add(event, BlockInit.REGIGIGA_CORE.get());
            add(event, BlockInit.REGIROCK_CORE.get());
            add(event, BlockInit.REGISTEEL_CORE.get());

            add(event, BlockInit.COSMIC_DUST_BLOCK.get());
            add(event, BlockInit.RUBY_BLOCK.get());
            add(event, BlockInit.RUBY_STAIRS.get());
            add(event, BlockInit.RUBY_SLAB.get());
            add(event, BlockInit.SAPPHIRE_BLOCK.get());
            add(event, BlockInit.SAPPHIRE_STAIRS.get());
            add(event, BlockInit.SAPPHIRE_SLAB.get());
        }

        if (event.getTabKey() == CreativeModeTabs.COLORED_BLOCKS)
        {
            add(event, BlockInit.SPECTRUM_GLASS.get());
            add(event, PokecubeAdv.LAB_GLASS.get());
            add(event, BlockInit.MIRAGE_GLASS.get());
            add(event, BlockInit.FRAMED_DISTORTIC_MIRROR.get());

            add(event, BlockInit.ONE_WAY_FRAMED_MIRROR.get());
            add(event, BlockInit.ONE_WAY_GLASS.get());
            add(event, BlockInit.ONE_WAY_GLASS_TINTED.get());
            add(event, BlockInit.ONE_WAY_GLASS_WHITE.get());
            add(event, BlockInit.ONE_WAY_GLASS_LIGHT_GRAY.get());
            add(event, BlockInit.ONE_WAY_GLASS_GRAY.get());
            add(event, BlockInit.ONE_WAY_GLASS_BLACK.get());
            add(event, BlockInit.ONE_WAY_GLASS_BROWN.get());
            add(event, BlockInit.ONE_WAY_GLASS_RED.get());
            add(event, BlockInit.ONE_WAY_GLASS_ORANGE.get());
            add(event, BlockInit.ONE_WAY_GLASS_YELLOW.get());
            add(event, BlockInit.ONE_WAY_GLASS_LIME.get());
            add(event, BlockInit.ONE_WAY_GLASS_GREEN.get());
            add(event, BlockInit.ONE_WAY_GLASS_CYAN.get());
            add(event, BlockInit.ONE_WAY_GLASS_LIGHT_BLUE.get());
            add(event, BlockInit.ONE_WAY_GLASS_BLUE.get());
            add(event, BlockInit.ONE_WAY_GLASS_PURPLE.get());
            add(event, BlockInit.ONE_WAY_GLASS_MAGENTA.get());
            add(event, BlockInit.ONE_WAY_GLASS_PINK.get());
            add(event, BlockInit.ONE_WAY_GLASS_SPECTRUM.get());
            add(event, BlockInit.ONE_WAY_GLASS_LAB.get());
            add(event, BlockInit.ONE_WAY_GLASS_MIRAGE.get());

            for (int i = 0; i < BlockInit.totemKeys.length; i++)
            {
                add(event, BlockInit.BULU[i].get());
            }

            for (int i = 0; i < BlockInit.totemKeys.length; i++)
            {
                add(event, BlockInit.KOKO[i].get());
            }

            for (int i = 0; i < BlockInit.totemKeys.length; i++)
            {
                add(event, BlockInit.FINI[i].get());
            }

            for (int i = 0; i < BlockInit.totemKeys.length; i++)
            {
                add(event, BlockInit.LELE[i].get());
            }
        }
        
        if (event.getTabKey().equals(ITEMS_TAB.getKey()))
        {
            add(event, ItemInit.SPECTRUM_SHARD.get(), PokecubeItems.EMERALDSHARD.get());
        }

        if (event.getTabKey().equals(NATURAL_BLOCKS_TAB.getKey()))
        {
            add(event, BlockInit.DUSK_COSMIC_ORE.get(), PokecubeItems.FOSSIL_ORE.get());
            add(event, PokecubeItems.FOSSIL_ORE.get(), PokecubeItems.DEEPSLATE_FOSSIL_ORE.get());
        }
        
        if (event.getTabKey().equals(FUNCTIONAL_BLOCKS_TAB.getKey()))
        {
            add(event, PokecubeAdv.BAG.get());
            add(event, PokecubeAdv.EXPSHARE.get());
            add(event, PokecubeAdv.LINKER.get());
            add(event, PokecubeItems.TM.get());
            add(event, PokecubeItems.getStack("pokecube:candy"));
            add(event, PokecubeItems.getStack("pokecube:revive"));
            add(event, PokecubeItems.getStack("pokecube:luckyegg"));
            add(event, PokecubeItems.getStack("pokecube:shiny_charm"));

            add(event, PokecubeItems.DYNAMAX.get());
            add(event, PokecubeAdv.STATUE.get());
            add(event, PokecubeItems.HEALER.get());
            add(event, PokecubeItems.PC_TOP.get());
            add(event, PokecubeItems.PC_BASE.get());
            add(event, PokecubeItems.TRADER.get());
            add(event, PokecubeItems.TM_MACHINE.get());

            add(event, PokecubeAdv.CLONER.get());
            add(event, PokecubeAdv.EXTRACTOR.get());
            add(event, PokecubeAdv.SPLICER.get());
            add(event, PokecubeAdv.SIPHON.get());

            add(event, PokecubeAdv.WARP_PAD.get());
            add(event, PokecubeAdv.AFA.get());
            add(event, PokecubeAdv.COMMANDER.get());
            add(event, PokecubeAdv.DAYCARE.get());

            add(event, PokecubeItems.NEST.get());
            add(event, PokecubeItems.SECRET_BASE.get());
            add(event, PokecubeItems.REPEL.get());

            add(event, BlockInit.CRAMOMATIC_BLOCK.get());
            add(event, BlockInit.MIRAGE_SPOTS.get());
        }
    }
}
