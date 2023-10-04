package pokecube.legends.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.init.AdvCreativeTabs;
import pokecube.api.entity.pokemob.Nature;
import pokecube.api.utils.PokeType;
import pokecube.core.PokecubeCore;
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
                output.accept(BlockInit.AGED_HANGING_SIGN.get());
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
                output.accept(BlockInit.CORRUPTED_HANGING_SIGN.get());
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
                output.accept(BlockInit.DISTORTIC_HANGING_SIGN.get());
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
                output.accept(BlockInit.INVERTED_HANGING_SIGN.get());
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
                output.accept(BlockInit.MIRAGE_HANGING_SIGN.get());
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
                output.accept(BlockInit.TEMPORAL_HANGING_SIGN.get());
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
                output.accept(BlockInit.CONCRETE_HANGING_SIGN.get());
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
                output.accept(BlockInit.LARGE_OAK_CHISELED_BOOKSHELF.get());

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
                output.accept(BlockInit.METEORITE_LAYER.get());
                output.accept(BlockInit.METEORITE_MOLTEN_BLOCK.get());
                output.accept(BlockInit.METEORITE_MOLTEN_LAYER.get());
                output.accept(BlockInit.ASH_BLOCK.get());
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

                output.accept(BlockInit.LARGE_OAK_CHISELED_BOOKSHELF.get());
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
                output.accept(BlockInit.AGED_HANGING_SIGN.get());
                output.accept(BlockInit.CONCRETE_SIGN.get());
                output.accept(BlockInit.CONCRETE_DENSE_SIGN.get());
                output.accept(BlockInit.CONCRETE_HANGING_SIGN.get());
                output.accept(BlockInit.CORRUPTED_SIGN.get());
                output.accept(BlockInit.CORRUPTED_HANGING_SIGN.get());
                output.accept(BlockInit.DISTORTIC_SIGN.get());
                output.accept(BlockInit.DISTORTIC_HANGING_SIGN.get());
                output.accept(BlockInit.INVERTED_SIGN.get());
                output.accept(BlockInit.INVERTED_HANGING_SIGN.get());
                output.accept(BlockInit.MIRAGE_SIGN.get());
                output.accept(BlockInit.MIRAGE_HANGING_SIGN.get());
                output.accept(BlockInit.TEMPORAL_SIGN.get());
                output.accept(BlockInit.TEMPORAL_HANGING_SIGN.get());

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
        if (event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS && PokecubeCore.getConfig().itemsInVanillaTabs)
        {
            addAfter(event, Items.GOLDEN_CARROT, ItemInit.ICE_CARROT.get());
            addAfter(event, ItemInit.ICE_CARROT.get(), ItemInit.SHADOW_CARROT.get());

            addAfter(event, Items.CAKE, ItemInit.NULL_POKEPUFF.get());
            addAfter(event, ItemInit.NULL_POKEPUFF.get(), ItemInit.CHERI_POKEPUFF.get());
            addAfter(event, ItemInit.CHERI_POKEPUFF.get(), ItemInit.CHESTO_POKEPUFF.get());
            addAfter(event, ItemInit.CHESTO_POKEPUFF.get(), ItemInit.PECHA_POKEPUFF.get());
            addAfter(event, ItemInit.PECHA_POKEPUFF.get(), ItemInit.RAWST_POKEPUFF.get());
            addAfter(event, ItemInit.RAWST_POKEPUFF.get(), ItemInit.ASPEAR_POKEPUFF.get());
            addAfter(event, ItemInit.ASPEAR_POKEPUFF.get(), ItemInit.LEPPA_POKEPUFF.get());
            addAfter(event, ItemInit.LEPPA_POKEPUFF.get(), ItemInit.ORAN_POKEPUFF.get());
            addAfter(event, ItemInit.ORAN_POKEPUFF.get(), ItemInit.PERSIM_POKEPUFF.get());
            addAfter(event, ItemInit.PERSIM_POKEPUFF.get(), ItemInit.LUM_POKEPUFF.get());
            addAfter(event, ItemInit.LUM_POKEPUFF.get(), ItemInit.SITRUS_POKEPUFF.get());
            addAfter(event, ItemInit.SITRUS_POKEPUFF.get(), ItemInit.NANAB_POKEPUFF.get());
            addAfter(event, ItemInit.NANAB_POKEPUFF.get(), ItemInit.PINAP_POKEPUFF.get());
            addAfter(event, ItemInit.PINAP_POKEPUFF.get(), ItemInit.POMEG_POKEPUFF.get());
            addAfter(event, ItemInit.POMEG_POKEPUFF.get(), ItemInit.KELPSY_POKEPUFF.get());
            addAfter(event, ItemInit.KELPSY_POKEPUFF.get(), ItemInit.QUALOT_POKEPUFF.get());
            addAfter(event, ItemInit.QUALOT_POKEPUFF.get(), ItemInit.HONDEW_POKEPUFF.get());
            addAfter(event, ItemInit.HONDEW_POKEPUFF.get(), ItemInit.GREPA_POKEPUFF.get());
            addAfter(event, ItemInit.GREPA_POKEPUFF.get(), ItemInit.TAMATO_POKEPUFF.get());
            addAfter(event, ItemInit.TAMATO_POKEPUFF.get(), ItemInit.CORNN_POKEPUFF.get());
            addAfter(event, ItemInit.CORNN_POKEPUFF.get(), ItemInit.ENIGMA_POKEPUFF.get());
            addAfter(event, ItemInit.ENIGMA_POKEPUFF.get(), ItemInit.JABOCA_POKEPUFF.get());
            addAfter(event, ItemInit.JABOCA_POKEPUFF.get(), ItemInit.ROWAP_POKEPUFF.get());
        }

        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS && PokecubeCore.getConfig().itemsInVanillaTabs)
        {
            addAfter(event, Items.DIAMOND, ItemInit.DIAMOND_GEM.get());
            addAfter(event, ItemInit.DIAMOND_GEM.get(), ItemInit.RUBY.get());
            addAfter(event, ItemInit.RUBY.get(), ItemInit.SAPPHIRE.get());
            addAfter(event, ItemInit.SAPPHIRE.get(), ItemInit.AQUAMARINE.get());
            addAfter(event, Items.AMETHYST_SHARD, ItemInit.AQUAMARINE_SHARD.get());
            addAfter(event, ItemInit.AQUAMARINE_SHARD.get(), ItemInit.FRACTAL_SHARD.get());
            addAfter(event, ItemInit.FRACTAL_SHARD.get(), ItemInit.SPECTRUM_SHARD.get());
            addAfter(event, Items.GUNPOWDER, ItemInit.PILE_OF_ASH.get());
            addAfter(event, ItemInit.PILE_OF_ASH.get(), ItemInit.COSMIC_DUST.get());

            addBefore(event, Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE, ItemInit.RUSTED_SHIELD.get());
            addBefore(event, ItemInit.RUSTED_SHIELD.get(), ItemInit.RUSTED_SWORD.get());
            addAfter(event, Items.DISC_FRAGMENT_5, ItemInit.HEAD_MIRROR.get());
            addAfter(event, ItemInit.HEAD_MIRROR.get(), ItemInit.BODY_MIRROR.get());
            addAfter(event, ItemInit.BODY_MIRROR.get(), ItemInit.GLASS_MIRROR.get());
        }

        if (event.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS && PokecubeCore.getConfig().itemsInVanillaTabs)
        {
            addAfter(event, Items.BARREL, BlockInit.DISTORTIC_STONE_BARREL.get());
            addBefore(event, Items.REDSTONE_LAMP, BlockInit.MAGNETIC_STONE.get());
            addAfter(event, Items.CHISELED_BOOKSHELF, BlockInit.LARGE_OAK_CHISELED_BOOKSHELF.get());
        }

        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS && PokecubeCore.getConfig().itemsInVanillaTabs)
        {
            addAfter(event, PokecubeItems.DYNAMAX.get(), BlockInit.RAID_SPAWNER.get());
            addAfter(event, BlockInit.RAID_SPAWNER.get(), BlockInit.CRAMOMATIC_BLOCK.get());

            addAfter(event, Items.BARREL, BlockInit.DISTORTIC_STONE_BARREL.get());
            addAfter(event, Items.RESPAWN_ANCHOR, BlockInit.MAGNETIC_STONE.get());
            addBefore(event, Items.LIGHTNING_ROD, BlockInit.DISTORTIC_MIRROR.get());
            addAfter(event, Items.CHISELED_BOOKSHELF, BlockInit.BOOKSHELF_EMPTY.get());
            addAfter(event, Items.CHISELED_BOOKSHELF, BlockInit.LARGE_OAK_CHISELED_BOOKSHELF.get());

            addAfter(event, Items.WARPED_HANGING_SIGN, BlockInit.AGED_SIGN.get());
            addAfter(event, BlockInit.AGED_SIGN.get(), BlockInit.AGED_HANGING_SIGN.get());
            addAfter(event, BlockInit.AGED_HANGING_SIGN.get(), BlockInit.CONCRETE_SIGN.get());
            addAfter(event, BlockInit.CONCRETE_SIGN.get(), BlockInit.CONCRETE_DENSE_SIGN.get());
            addAfter(event, BlockInit.CONCRETE_DENSE_SIGN.get(), BlockInit.CONCRETE_HANGING_SIGN.get());
            addAfter(event, BlockInit.CONCRETE_HANGING_SIGN.get(), BlockInit.CORRUPTED_SIGN.get());
            addAfter(event, BlockInit.CORRUPTED_SIGN.get(), BlockInit.CORRUPTED_HANGING_SIGN.get());
            addAfter(event, BlockInit.CORRUPTED_HANGING_SIGN.get(), BlockInit.DISTORTIC_SIGN.get());
            addAfter(event, BlockInit.DISTORTIC_SIGN.get(), BlockInit.DISTORTIC_HANGING_SIGN.get());
            addAfter(event, BlockInit.DISTORTIC_HANGING_SIGN.get(), BlockInit.INVERTED_SIGN.get());
            addAfter(event, BlockInit.INVERTED_SIGN.get(), BlockInit.INVERTED_HANGING_SIGN.get());
            addAfter(event, BlockInit.INVERTED_HANGING_SIGN.get(), BlockInit.MIRAGE_SIGN.get());
            addAfter(event, BlockInit.MIRAGE_SIGN.get(), BlockInit.MIRAGE_HANGING_SIGN.get());
            addAfter(event, BlockInit.MIRAGE_HANGING_SIGN.get(), BlockInit.TEMPORAL_SIGN.get());
            addAfter(event, BlockInit.TEMPORAL_SIGN.get(), BlockInit.TEMPORAL_HANGING_SIGN.get());

            addAfter(event, Items.SOUL_TORCH, BlockInit.INFECTED_TORCH.get());
            addAfter(event, Items.SOUL_LANTERN, BlockInit.INFECTED_LANTERN.get());
            addAfter(event, Items.SOUL_CAMPFIRE, BlockInit.INFECTED_CAMPFIRE.get());
            addAfter(event, Items.GLOWSTONE, BlockInit.DISTORTIC_GLOWSTONE.get());
        }

        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES && PokecubeCore.getConfig().itemsInVanillaTabs)
        {
            addAfter(event, Items.LAVA_BUCKET, ItemInit.DISTORTIC_WATER_BUCKET.get());
            addAfter(event, Items.FLINT_AND_STEEL, ItemInit.ULTRA_KEY.get());
            addAfter(event, ItemInit.ULTRA_KEY.get(), ItemInit.GIRATINA_MIRROR.get());

            addAfter(event, Items.BAMBOO_CHEST_RAFT, PokecubeItems.getStack("pokecube_legends:aged_boat").getItem());
            addAfter(event, PokecubeItems.getStack("pokecube_legends:aged_boat").getItem(), PokecubeItems.getStack("pokecube_legends:aged_chest_boat").getItem());
            addAfter(event, PokecubeItems.getStack("pokecube_legends:aged_chest_boat").getItem(), PokecubeItems.getStack("pokecube_legends:concrete_boat").getItem());
            addAfter(event, PokecubeItems.getStack("pokecube_legends:concrete_boat").getItem(), PokecubeItems.getStack("pokecube_legends:concrete_chest_boat").getItem());
            addAfter(event, PokecubeItems.getStack("pokecube_legends:concrete_chest_boat").getItem(), PokecubeItems.getStack("pokecube_legends:corrupted_boat").getItem());
            addAfter(event, PokecubeItems.getStack("pokecube_legends:corrupted_boat").getItem(), PokecubeItems.getStack("pokecube_legends:corrupted_chest_boat").getItem());
            addAfter(event, PokecubeItems.getStack("pokecube_legends:corrupted_chest_boat").getItem(), PokecubeItems.getStack("pokecube_legends:distortic_boat").getItem());
            addAfter(event, PokecubeItems.getStack("pokecube_legends:distortic_boat").getItem(), PokecubeItems.getStack("pokecube_legends:distortic_chest_boat").getItem());
            addAfter(event, PokecubeItems.getStack("pokecube_legends:distortic_chest_boat").getItem(), PokecubeItems.getStack("pokecube_legends:inverted_boat").getItem());
            addAfter(event, PokecubeItems.getStack("pokecube_legends:inverted_boat").getItem(), PokecubeItems.getStack("pokecube_legends:inverted_chest_boat").getItem());
            addAfter(event, PokecubeItems.getStack("pokecube_legends:inverted_chest_boat").getItem(), PokecubeItems.getStack("pokecube_legends:mirage_boat").getItem());
            addAfter(event, PokecubeItems.getStack("pokecube_legends:mirage_boat").getItem(), PokecubeItems.getStack("pokecube_legends:mirage_chest_boat").getItem());
            addAfter(event, PokecubeItems.getStack("pokecube_legends:mirage_chest_boat").getItem(), PokecubeItems.getStack("pokecube_legends:temporal_boat").getItem());
            addAfter(event, PokecubeItems.getStack("pokecube_legends:temporal_boat").getItem(), PokecubeItems.getStack("pokecube_legends:temporal_chest_boat").getItem());
        }

        if (event.getTabKey() == CreativeModeTabs.COMBAT && PokecubeCore.getConfig().itemsInVanillaTabs)
        {
            addAfter(event, Items.NETHERITE_SWORD, ItemInit.COBALION_SWORD.get());
            addAfter(event, ItemInit.COBALION_SWORD.get(), ItemInit.KELDEO_SWORD.get());
            addAfter(event, ItemInit.KELDEO_SWORD.get(), ItemInit.TERRAKION_SWORD.get());
            addAfter(event, ItemInit.TERRAKION_SWORD.get(), ItemInit.VIRIZION_SWORD.get());
            addAfter(event, ItemInit.VIRIZION_SWORD.get(), ItemInit.ZACIAN_SWORD.get());
            addAfter(event, ItemInit.ZACIAN_SWORD.get(), ItemInit.RAINBOW_SWORD.get());
            addAfter(event, Items.SHIELD, ItemInit.ZAMAZENTA_SHIELD.get());

            addAfter(event, Items.NETHERITE_BOOTS, ItemInit.ULTRA_HELMET.get());
            addAfter(event, ItemInit.ULTRA_HELMET.get(), ItemInit.ULTRA_CHESTPLATE.get());
            addAfter(event, ItemInit.ULTRA_CHESTPLATE.get(), ItemInit.ULTRA_LEGGINGS.get());
            addAfter(event, ItemInit.ULTRA_LEGGINGS.get(), ItemInit.ULTRA_BOOTS.get());
        }

        if (event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS && PokecubeCore.getConfig().itemsInVanillaTabs)
        {
            addAfter(event, Items.DEEPSLATE_IRON_ORE, BlockInit.ASH_IRON_ORE.get());
            addAfter(event, Items.DEEPSLATE_DIAMOND_ORE, BlockInit.RUBY_ORE.get());
            addAfter(event, BlockInit.RUBY_ORE.get(), BlockInit.DEEPSLATE_RUBY_ORE.get());
            addAfter(event, BlockInit.DEEPSLATE_RUBY_ORE.get(), BlockInit.SAPPHIRE_ORE.get());
            addAfter(event, BlockInit.SAPPHIRE_ORE.get(), BlockInit.DEEPSLATE_SAPPHIRE_ORE.get());
            addAfter(event, BlockInit.DEEPSLATE_SAPPHIRE_ORE.get(), BlockInit.METEORITE_COSMIC_ORE.get());

            addAfter(event, Items.END_STONE, BlockInit.METEORITE_BLOCK.get());
            addAfter(event, BlockInit.METEORITE_BLOCK.get(), BlockInit.METEORITE_LAYER.get());
            addAfter(event, BlockInit.METEORITE_LAYER.get(), BlockInit.METEORITE_MOLTEN_BLOCK.get());
            addAfter(event, BlockInit.METEORITE_MOLTEN_BLOCK.get(), BlockInit.METEORITE_MOLTEN_LAYER.get());
            addAfter(event, BlockInit.METEORITE_MOLTEN_LAYER.get(), BlockInit.ASH_BLOCK.get());
            addAfter(event, BlockInit.ASH_BLOCK.get(), BlockInit.ASH.get());

            addAfter(event, Items.AMETHYST_CLUSTER, BlockInit.UNREFINED_AQUAMARINE.get());
            addAfter(event, BlockInit.UNREFINED_AQUAMARINE.get(), BlockInit.BUDDING_AQUAMARINE.get());
            addAfter(event, BlockInit.BUDDING_AQUAMARINE.get(), BlockInit.SMALL_AQUAMARINE_BUD.get());
            addAfter(event, BlockInit.SMALL_AQUAMARINE_BUD.get(), BlockInit.MEDIUM_AQUAMARINE_BUD.get());
            addAfter(event, BlockInit.MEDIUM_AQUAMARINE_BUD.get(), BlockInit.LARGE_AQUAMARINE_BUD.get());
            addAfter(event, BlockInit.LARGE_AQUAMARINE_BUD.get(), BlockInit.AQUAMARINE_CLUSTER.get());
            addAfter(event, BlockInit.AQUAMARINE_CLUSTER.get(), BlockInit.AQUAMARINE_CRYSTAL.get());
        }

        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS && PokecubeCore.getConfig().itemsInVanillaTabs)
        {
            addAfter(event, Items.STRIPPED_OAK_WOOD, BlockInit.BOOKSHELF_EMPTY.get());
            addAfter(event, Items.STRIPPED_OAK_WOOD, BlockInit.LARGE_OAK_CHISELED_BOOKSHELF.get());

            addAfter(event, Items.OAK_SLAB, BlockInit.DISTORTIC_OAK_PLANKS.get());
            addAfter(event, BlockInit.DISTORTIC_OAK_PLANKS.get(), BlockInit.DISTORTIC_OAK_STAIRS.get());
            addAfter(event, BlockInit.DISTORTIC_OAK_STAIRS.get(), BlockInit.DISTORTIC_OAK_SLAB.get());

            addAfter(event, Items.SPRUCE_SLAB, BlockInit.DISTORTIC_SPRUCE_PLANKS.get());
            addAfter(event, BlockInit.DISTORTIC_SPRUCE_PLANKS.get(), BlockInit.DISTORTIC_SPRUCE_STAIRS.get());
            addAfter(event, BlockInit.DISTORTIC_SPRUCE_STAIRS.get(), BlockInit.DISTORTIC_SPRUCE_SLAB.get());

            addAfter(event, Items.BIRCH_SLAB, BlockInit.DISTORTIC_BIRCH_PLANKS.get());
            addAfter(event, BlockInit.DISTORTIC_BIRCH_PLANKS.get(), BlockInit.DISTORTIC_BIRCH_STAIRS.get());
            addAfter(event, BlockInit.DISTORTIC_BIRCH_STAIRS.get(), BlockInit.DISTORTIC_BIRCH_SLAB.get());

            addAfter(event, Items.JUNGLE_SLAB, BlockInit.DISTORTIC_JUNGLE_PLANKS.get());
            addAfter(event, BlockInit.DISTORTIC_JUNGLE_PLANKS.get(), BlockInit.DISTORTIC_JUNGLE_STAIRS.get());
            addAfter(event, BlockInit.DISTORTIC_JUNGLE_STAIRS.get(), BlockInit.DISTORTIC_JUNGLE_SLAB.get());

            addAfter(event, Items.ACACIA_SLAB, BlockInit.DISTORTIC_ACACIA_PLANKS.get());
            addAfter(event, BlockInit.DISTORTIC_ACACIA_PLANKS.get(), BlockInit.DISTORTIC_ACACIA_STAIRS.get());
            addAfter(event, BlockInit.DISTORTIC_ACACIA_STAIRS.get(), BlockInit.DISTORTIC_ACACIA_SLAB.get());

            addAfter(event, Items.DARK_OAK_SLAB, BlockInit.DISTORTIC_DARK_OAK_PLANKS.get());
            addAfter(event, BlockInit.DISTORTIC_DARK_OAK_PLANKS.get(), BlockInit.DISTORTIC_DARK_OAK_STAIRS.get());
            addAfter(event, BlockInit.DISTORTIC_DARK_OAK_STAIRS.get(), BlockInit.DISTORTIC_DARK_OAK_SLAB.get());

            addAfter(event, Items.REINFORCED_DEEPSLATE, BlockInit.METEORITE_BLOCK.get());
            addAfter(event, BlockInit.METEORITE_BLOCK.get(), BlockInit.METEORITE_STAIRS.get());
            addAfter(event, BlockInit.METEORITE_STAIRS.get(), BlockInit.METEORITE_SLAB.get());

            addAfter(event, BlockInit.METEORITE_SLAB.get(), BlockInit.OCEAN_BRICKS.get());
            addAfter(event, BlockInit.OCEAN_BRICKS.get(), BlockInit.OCEAN_BRICK_STAIRS.get());
            addAfter(event, BlockInit.OCEAN_BRICK_STAIRS.get(), BlockInit.OCEAN_BRICK_SLAB.get());

            addAfter(event, BlockInit.OCEAN_BRICK_SLAB.get(), BlockInit.SKY_BRICKS.get());
            addAfter(event, BlockInit.SKY_BRICKS.get(), BlockInit.SKY_BRICK_STAIRS.get());
            addAfter(event, BlockInit.SKY_BRICK_STAIRS.get(), BlockInit.SKY_BRICK_SLAB.get());

            addAfter(event, BlockInit.SKY_BRICK_SLAB.get(), BlockInit.STORMY_SKY_BRICKS.get());
            addAfter(event, BlockInit.STORMY_SKY_BRICKS.get(), BlockInit.STORMY_SKY_BRICK_STAIRS.get());
            addAfter(event, BlockInit.STORMY_SKY_BRICK_STAIRS.get(), BlockInit.STORMY_SKY_BRICK_SLAB.get());

            addAfter(event, Items.RED_NETHER_BRICK_WALL, Items.MAGMA_BLOCK);
            addAfter(event, Items.MAGMA_BLOCK, BlockInit.MAGMA_BRICKS.get());
            addAfter(event, BlockInit.MAGMA_BRICKS.get(), BlockInit.MAGMA_BRICK_STAIRS.get());
            addAfter(event, BlockInit.MAGMA_BRICK_STAIRS.get(), BlockInit.MAGMA_BRICK_SLAB.get());

            addAfter(event, Items.PURPUR_SLAB, BlockInit.PURPUR_BRICKS.get());
            addAfter(event, BlockInit.PURPUR_BRICKS.get(), BlockInit.PURPUR_BRICK_STAIRS.get());
            addAfter(event, BlockInit.PURPUR_BRICK_STAIRS.get(), BlockInit.PURPUR_BRICK_SLAB.get());

            addAfter(event, Items.MUD_BRICK_WALL, BlockInit.GOLEM_STONE.get());
            addAfter(event, BlockInit.GOLEM_STONE.get(), BlockInit.TOTEM_BLOCK.get());
            addAfter(event, BlockInit.TOTEM_BLOCK.get(), BlockInit.REGICE_CORE.get());
            addAfter(event, BlockInit.REGICE_CORE.get(), BlockInit.REGIDRAGO_CORE.get());
            addAfter(event, BlockInit.REGIDRAGO_CORE.get(), BlockInit.REGIELEKI_CORE.get());
            addAfter(event, BlockInit.REGIELEKI_CORE.get(), BlockInit.REGIGIGA_CORE.get());
            addAfter(event, BlockInit.REGIGIGA_CORE.get(), BlockInit.REGIROCK_CORE.get());
            addAfter(event, BlockInit.REGIROCK_CORE.get(), BlockInit.REGISTEEL_CORE.get());

            addAfter(event, Items.NETHERITE_BLOCK, BlockInit.FRACTAL_BLOCK.get());
            addAfter(event, BlockInit.FRACTAL_BLOCK.get(), BlockInit.RUBY_BLOCK.get());
            addAfter(event, BlockInit.RUBY_BLOCK.get(), BlockInit.RUBY_STAIRS.get());
            addAfter(event, BlockInit.RUBY_STAIRS.get(), BlockInit.RUBY_SLAB.get());
            addAfter(event, BlockInit.RUBY_SLAB.get(), BlockInit.SAPPHIRE_BLOCK.get());
            addAfter(event, BlockInit.SAPPHIRE_BLOCK.get(), BlockInit.SAPPHIRE_STAIRS.get());
            addAfter(event, BlockInit.SAPPHIRE_STAIRS.get(), BlockInit.SAPPHIRE_SLAB.get());
            addAfter(event, BlockInit.SAPPHIRE_SLAB.get(), BlockInit.SPECTRUM_BLOCK.get());
            addAfter(event, BlockInit.SPECTRUM_BLOCK.get(), BlockInit.SPECTRUM_STAIRS.get());
            addAfter(event, BlockInit.SPECTRUM_STAIRS.get(), BlockInit.SPECTRUM_SLAB.get());
            addAfter(event, BlockInit.SPECTRUM_SLAB.get(), BlockInit.COSMIC_DUST_BLOCK.get());
        }

        if (event.getTabKey() == CreativeModeTabs.COLORED_BLOCKS && PokecubeCore.getConfig().itemsInVanillaTabs)
        {
            addBefore(event, Items.GLASS, BlockInit.FRAMED_DISTORTIC_MIRROR.get());
            addAfter(event, Items.PINK_STAINED_GLASS, BlockInit.SPECTRUM_GLASS.get());
            addAfter(event, BlockInit.SPECTRUM_GLASS.get(), BlockInit.MIRAGE_GLASS.get());

            addAfter(event, Items.PINK_STAINED_GLASS_PANE, BlockInit.ONE_WAY_FRAMED_MIRROR.get());
            addAfter(event, BlockInit.ONE_WAY_FRAMED_MIRROR.get(), BlockInit.ONE_WAY_GLASS.get());
            addAfter(event, BlockInit.ONE_WAY_GLASS.get(), BlockInit.ONE_WAY_GLASS_TINTED.get());
            addAfter(event, BlockInit.ONE_WAY_GLASS_TINTED.get(), BlockInit.ONE_WAY_GLASS_WHITE.get());
            addAfter(event, BlockInit.ONE_WAY_GLASS_WHITE.get(), BlockInit.ONE_WAY_GLASS_LIGHT_GRAY.get());
            addAfter(event, BlockInit.ONE_WAY_GLASS_LIGHT_GRAY.get(), BlockInit.ONE_WAY_GLASS_GRAY.get());
            addAfter(event, BlockInit.ONE_WAY_GLASS_GRAY.get(), BlockInit.ONE_WAY_GLASS_BLACK.get());
            addAfter(event, BlockInit.ONE_WAY_GLASS_BLACK.get(), BlockInit.ONE_WAY_GLASS_BROWN.get());
            addAfter(event, BlockInit.ONE_WAY_GLASS_BROWN.get(), BlockInit.ONE_WAY_GLASS_RED.get());
            addAfter(event, BlockInit.ONE_WAY_GLASS_RED.get(), BlockInit.ONE_WAY_GLASS_ORANGE.get());
            addAfter(event, BlockInit.ONE_WAY_GLASS_ORANGE.get(), BlockInit.ONE_WAY_GLASS_YELLOW.get());
            addAfter(event, BlockInit.ONE_WAY_GLASS_YELLOW.get(), BlockInit.ONE_WAY_GLASS_LIME.get());
            addAfter(event, BlockInit.ONE_WAY_GLASS_LIME.get(), BlockInit.ONE_WAY_GLASS_GREEN.get());
            addAfter(event, BlockInit.ONE_WAY_GLASS_GREEN.get(), BlockInit.ONE_WAY_GLASS_CYAN.get());
            addAfter(event, BlockInit.ONE_WAY_GLASS_CYAN.get(), BlockInit.ONE_WAY_GLASS_LIGHT_BLUE.get());
            addAfter(event, BlockInit.ONE_WAY_GLASS_LIGHT_BLUE.get(), BlockInit.ONE_WAY_GLASS_BLUE.get());
            addAfter(event, BlockInit.ONE_WAY_GLASS_BLUE.get(), BlockInit.ONE_WAY_GLASS_PURPLE.get());
            addAfter(event, BlockInit.ONE_WAY_GLASS_PURPLE.get(), BlockInit.ONE_WAY_GLASS_MAGENTA.get());
            addAfter(event, BlockInit.ONE_WAY_GLASS_MAGENTA.get(), BlockInit.ONE_WAY_GLASS_PINK.get());
            addAfter(event, BlockInit.ONE_WAY_GLASS_PINK.get(), BlockInit.ONE_WAY_GLASS_SPECTRUM.get());
            addAfter(event, BlockInit.ONE_WAY_GLASS_SPECTRUM.get(), BlockInit.ONE_WAY_GLASS_MIRAGE.get());
            addAfter(event, BlockInit.ONE_WAY_GLASS_MIRAGE.get(), BlockInit.ONE_WAY_GLASS_LAB.get());

            for (int i = 0; i < BlockInit.totemKeys.length; i++)
            {
                addBefore(event, Items.WHITE_BED, BlockInit.LELE[i].get());
            }
            addBefore(event, PokecubeItems.getStack("pokecube_legends:lele_white_totem").getItem(), BlockInit.TAPU_LELE_CORE.get());

            for (int i = 0; i < BlockInit.totemKeys.length; i++)
            {
                addBefore(event, BlockInit.TAPU_LELE_CORE.get(), BlockInit.FINI[i].get());
            }
            addBefore(event, PokecubeItems.getStack("pokecube_legends:fini_white_totem").getItem(), BlockInit.TAPU_FINI_CORE.get());

            for (int i = 0; i < BlockInit.totemKeys.length; i++)
            {
                addBefore(event, BlockInit.TAPU_FINI_CORE.get(), BlockInit.KOKO[i].get());
            }
            addBefore(event, PokecubeItems.getStack("pokecube_legends:koko_white_totem").getItem(), BlockInit.TAPU_KOKO_CORE.get());

            for (int i = 0; i < BlockInit.totemKeys.length; i++)
            {
                addBefore(event, BlockInit.TAPU_KOKO_CORE.get(), BlockInit.BULU[i].get());
            }
            addBefore(event, PokecubeItems.getStack("pokecube_legends:bulu_white_totem").getItem(), BlockInit.TAPU_BULU_CORE.get());
        }
        
        if (event.getTabKey().equals(ITEMS_TAB.getKey()))
        {
            addAfter(event, ItemInit.SPECTRUM_SHARD.get(), PokecubeItems.EMERALDSHARD.get());
        }

        if (event.getTabKey().equals(NATURAL_BLOCKS_TAB.getKey()))
        {
            addAfter(event, BlockInit.DUSK_COSMIC_ORE.get(), PokecubeItems.FOSSIL_ORE.get());
            addAfter(event, PokecubeItems.FOSSIL_ORE.get(), PokecubeItems.DEEPSLATE_FOSSIL_ORE.get());
        }
        
        if (event.getTabKey().equals(FUNCTIONAL_BLOCKS_TAB.getKey()))
        {
            addAfter(event, ItemInit.ULTRA_BOOTS.get(), PokecubeAdv.BAG.get());
            addAfter(event, PokecubeAdv.BAG.get(), PokecubeAdv.EXPSHARE.get());
            addAfter(event, PokecubeAdv.EXPSHARE.get(), PokecubeAdv.LINKER.get());
            addAfter(event, PokecubeAdv.LINKER.get(), PokecubeItems.TM.get());
            addAfter(event, PokecubeItems.TM.get(), PokecubeItems.getStack("pokecube:candy").getItem());
            addAfter(event, PokecubeItems.getStack("pokecube:candy").getItem(), PokecubeItems.getStack("pokecube:revive").getItem());
            addAfter(event, PokecubeItems.getStack("pokecube:revive").getItem(), PokecubeItems.getStack("pokecube:luckyegg").getItem());
            addAfter(event, PokecubeItems.getStack("pokecube:luckyegg").getItem(), PokecubeItems.getStack("pokecube:shiny_charm").getItem());

            addAfter(event, ItemInit.DISTORTIC_WATER_BUCKET.get(), PokecubeItems.DYNAMAX.get());
            addAfter(event, BlockInit.RAID_SPAWNER.get(), PokecubeAdv.STATUE.get());
            addAfter(event, PokecubeAdv.STATUE.get(), PokecubeItems.HEALER.get());
            addAfter(event, PokecubeItems.HEALER.get(), PokecubeItems.PC_TOP.get());
            addAfter(event, PokecubeItems.PC_TOP.get(), PokecubeItems.PC_BASE.get());
            addAfter(event, PokecubeItems.PC_BASE.get(), PokecubeItems.TRADER.get());
            addAfter(event, PokecubeItems.TRADER.get(), PokecubeItems.TM_MACHINE.get());

            addAfter(event, PokecubeItems.TM_MACHINE.get(), PokecubeAdv.CLONER.get());
            addAfter(event, PokecubeAdv.CLONER.get(), PokecubeAdv.EXTRACTOR.get());
            addAfter(event, PokecubeAdv.EXTRACTOR.get(), PokecubeAdv.SPLICER.get());
            addAfter(event, PokecubeAdv.SPLICER.get(), PokecubeAdv.SIPHON.get());

            addAfter(event, PokecubeAdv.SIPHON.get(), BlockInit.CRAMOMATIC_BLOCK.get());
            addAfter(event, BlockInit.CRAMOMATIC_BLOCK.get(), BlockInit.MIRAGE_SPOTS.get());

            addAfter(event, BlockInit.REGISTEEL_CORE.get(), PokecubeAdv.WARP_PAD.get());
            addAfter(event, PokecubeAdv.WARP_PAD.get(), PokecubeAdv.AFA.get());
            addAfter(event, PokecubeAdv.AFA.get(), PokecubeAdv.COMMANDER.get());
            addAfter(event, PokecubeAdv.COMMANDER.get(), PokecubeAdv.DAYCARE.get());

            addAfter(event, PokecubeAdv.DAYCARE.get(), PokecubeItems.NEST.get());
            addAfter(event, PokecubeItems.NEST.get(), PokecubeItems.SECRET_BASE.get());
            addAfter(event, PokecubeItems.SECRET_BASE.get(), PokecubeItems.REPEL.get());
        }

        if (event.getTabKey().equals(BERRIES_TAB.getKey()))
        {
            addAfter(event, PokecubeItems.getStack("pokecube:berry_rowap").getItem(), ItemInit.NULL_POKEPUFF.get());
            addAfter(event, ItemInit.NULL_POKEPUFF.get(), ItemInit.CHERI_POKEPUFF.get());
            addAfter(event, ItemInit.CHERI_POKEPUFF.get(), ItemInit.CHESTO_POKEPUFF.get());
            addAfter(event, ItemInit.CHESTO_POKEPUFF.get(), ItemInit.PECHA_POKEPUFF.get());
            addAfter(event, ItemInit.PECHA_POKEPUFF.get(), ItemInit.RAWST_POKEPUFF.get());
            addAfter(event, ItemInit.RAWST_POKEPUFF.get(), ItemInit.ASPEAR_POKEPUFF.get());
            addAfter(event, ItemInit.ASPEAR_POKEPUFF.get(), ItemInit.LEPPA_POKEPUFF.get());
            addAfter(event, ItemInit.LEPPA_POKEPUFF.get(), ItemInit.ORAN_POKEPUFF.get());
            addAfter(event, ItemInit.ORAN_POKEPUFF.get(), ItemInit.PERSIM_POKEPUFF.get());
            addAfter(event, ItemInit.PERSIM_POKEPUFF.get(), ItemInit.LUM_POKEPUFF.get());
            addAfter(event, ItemInit.LUM_POKEPUFF.get(), ItemInit.SITRUS_POKEPUFF.get());
            addAfter(event, ItemInit.SITRUS_POKEPUFF.get(), ItemInit.NANAB_POKEPUFF.get());
            addAfter(event, ItemInit.NANAB_POKEPUFF.get(), ItemInit.PINAP_POKEPUFF.get());
            addAfter(event, ItemInit.PINAP_POKEPUFF.get(), ItemInit.POMEG_POKEPUFF.get());
            addAfter(event, ItemInit.POMEG_POKEPUFF.get(), ItemInit.KELPSY_POKEPUFF.get());
            addAfter(event, ItemInit.KELPSY_POKEPUFF.get(), ItemInit.QUALOT_POKEPUFF.get());
            addAfter(event, ItemInit.QUALOT_POKEPUFF.get(), ItemInit.HONDEW_POKEPUFF.get());
            addAfter(event, ItemInit.HONDEW_POKEPUFF.get(), ItemInit.GREPA_POKEPUFF.get());
            addAfter(event, ItemInit.GREPA_POKEPUFF.get(), ItemInit.TAMATO_POKEPUFF.get());
            addAfter(event, ItemInit.TAMATO_POKEPUFF.get(), ItemInit.CORNN_POKEPUFF.get());
            addAfter(event, ItemInit.CORNN_POKEPUFF.get(), ItemInit.ENIGMA_POKEPUFF.get());
            addAfter(event, ItemInit.ENIGMA_POKEPUFF.get(), ItemInit.JABOCA_POKEPUFF.get());
            addAfter(event, ItemInit.JABOCA_POKEPUFF.get(), ItemInit.ROWAP_POKEPUFF.get());
        }
    }
}
