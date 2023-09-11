package pokecube.legends.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
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
import pokecube.legends.Reference;

@Mod.EventBusSubscriber(modid = Reference.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class LegendsCreativeTabs {
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

                output.accept(BlockInit.TOTEM_BLOCK.get());
                output.accept(BlockInit.GOLEM_STONE.get());
                output.accept(BlockInit.REGICE_CORE.get());
                output.accept(BlockInit.REGIDRAGO_CORE.get());
                output.accept(BlockInit.REGIELEKI_CORE.get());
                output.accept(BlockInit.REGIGIGA_CORE.get());
                output.accept(BlockInit.REGIROCK_CORE.get());
                output.accept(BlockInit.REGISTEEL_CORE.get());

                output.accept(BlockInit.DISTORTIC_STONE.get());
                output.accept(BlockInit.DISTORTIC_STONE_STAIRS.get());
                output.accept(BlockInit.DISTORTIC_STONE_SLAB.get());

                output.accept(BlockInit.DISTORTIC_BARREL.get());
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

                output.accept(PokecubeAdv.LAB_GLASS.get());
                output.accept(BlockInit.SPECTRUM_GLASS.get());
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
                output.accept(BlockInit.ONE_WAY_GLASS_LAB.get());
                output.accept(BlockInit.ONE_WAY_GLASS_SPECTRUM.get());
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
            }).build());

    public static final RegistryObject<CreativeModeTab> NATURAL_BLOCKS_TAB = TABS.register("natural_blocks_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.pokecube_legends.natural_blocks"))
            .icon(() -> new ItemStack(BlockInit.DISTORTIC_GRASS_BLOCK.get()))
            .withTabsBefore(BUILDING_BLOCKS_TAB.getId())
            .displayItems((parameters, output) -> {
                output.accept(BlockInit.DISTORTIC_GRASS_BLOCK.get());
            }).build());

    public static final RegistryObject<CreativeModeTab> FUNCTIONAL_BLOCKS_TAB = TABS.register("functional_blocks_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.pokecube_legends.functional_blocks"))
            .icon(() -> new ItemStack(BlockInit.HEATRAN_BLOCK.get()))
            .withTabsBefore(NATURAL_BLOCKS_TAB.getId())
            .displayItems((parameters, output) -> {
                output.accept(ItemInit.RAINBOW_ORB.get());
                ItemInit.BOATS.get(5);
            }).build());

    public static final RegistryObject<CreativeModeTab> ITEMS_TAB = TABS.register("items_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.pokecube_legends.items"))
            .icon(() -> new ItemStack(ItemInit.RAINBOW_ORB.get()))
            .withTabsBefore(FUNCTIONAL_BLOCKS_TAB.getId())
            .displayItems((parameters, output) -> {
                output.accept(ItemInit.RAINBOW_SWORD.get());
                output.accept(ItemInit.COBALION_SWORD.get());
                output.accept(ItemInit.KELDEO_SWORD.get());
                output.accept(ItemInit.TERRAKION_SWORD.get());
                output.accept(ItemInit.VIRIZION_SWORD.get());
                output.accept(ItemInit.ZACIAN_SWORD.get());
                output.accept(ItemInit.ZAMAZENTA_SHIELD.get());

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

                output.accept(ItemInit.DIAMOND_GEM.get());
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
            event.accept(ItemInit.ICE_CARROT.get());
            event.accept(ItemInit.SHADOW_CARROT.get());
        }

        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS)
        {
            event.accept(ItemInit.RUBY.get());
            event.accept(ItemInit.SAPPHIRE.get());
            event.accept(ItemInit.AQUAMARINE.get());
            event.accept(ItemInit.AQUAMARINE_SHARD.get());
            event.accept(ItemInit.FRACTAL_SHARD.get());
            event.accept(ItemInit.SPECTRUM_SHARD.get());
            event.accept(ItemInit.DIAMOND_GEM.get());
            event.accept(ItemInit.COSMIC_DUST.get());
            event.accept(ItemInit.PILE_OF_ASH.get());

            event.accept(ItemInit.HEAD_MIRROR.get());
            event.accept(ItemInit.BODY_MIRROR.get());
            event.accept(ItemInit.GLASS_MIRROR.get());
        }

        if (event.getTabKey() == CreativeModeTabs.COMBAT)
        {
            event.accept(ItemInit.RAINBOW_SWORD.get());
            event.accept(ItemInit.COBALION_SWORD.get());
            event.accept(ItemInit.KELDEO_SWORD.get());
            event.accept(ItemInit.TERRAKION_SWORD.get());
            event.accept(ItemInit.VIRIZION_SWORD.get());
            event.accept(ItemInit.ZACIAN_SWORD.get());
            event.accept(ItemInit.ZAMAZENTA_SHIELD.get());
        }

        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS)
        {
            event.accept(BlockInit.BOOKSHELF_EMPTY.get());

            event.accept(BlockInit.DISTORTIC_OAK_PLANKS.get());
            event.accept(BlockInit.DISTORTIC_OAK_STAIRS.get());
            event.accept(BlockInit.DISTORTIC_OAK_SLAB.get());

            event.accept(BlockInit.DISTORTIC_SPRUCE_PLANKS.get());
            event.accept(BlockInit.DISTORTIC_SPRUCE_STAIRS.get());
            event.accept(BlockInit.DISTORTIC_SPRUCE_SLAB.get());

            event.accept(BlockInit.DISTORTIC_BIRCH_PLANKS.get());
            event.accept(BlockInit.DISTORTIC_BIRCH_STAIRS.get());
            event.accept(BlockInit.DISTORTIC_BIRCH_SLAB.get());

            event.accept(BlockInit.DISTORTIC_JUNGLE_PLANKS.get());
            event.accept(BlockInit.DISTORTIC_JUNGLE_STAIRS.get());
            event.accept(BlockInit.DISTORTIC_JUNGLE_SLAB.get());

            event.accept(BlockInit.DISTORTIC_ACACIA_PLANKS.get());
            event.accept(BlockInit.DISTORTIC_ACACIA_STAIRS.get());
            event.accept(BlockInit.DISTORTIC_ACACIA_SLAB.get());

            event.accept(BlockInit.DISTORTIC_DARK_OAK_PLANKS.get());
            event.accept(BlockInit.DISTORTIC_DARK_OAK_STAIRS.get());
            event.accept(BlockInit.DISTORTIC_DARK_OAK_SLAB.get());

            event.accept(BlockInit.METEORITE_BLOCK.get());
            event.accept(BlockInit.METEORITE_STAIRS.get());
            event.accept(BlockInit.METEORITE_SLAB.get());

            event.accept(BlockInit.OCEAN_BRICKS.get());
            event.accept(BlockInit.OCEAN_BRICK_STAIRS.get());
            event.accept(BlockInit.OCEAN_BRICK_SLAB.get());

            event.accept(BlockInit.SKY_BRICKS.get());
            event.accept(BlockInit.SKY_BRICK_STAIRS.get());
            event.accept(BlockInit.SKY_BRICK_SLAB.get());

            event.accept(BlockInit.STORMY_SKY_BRICKS.get());
            event.accept(BlockInit.STORMY_SKY_BRICK_STAIRS.get());
            event.accept(BlockInit.STORMY_SKY_BRICK_SLAB.get());

            event.accept(BlockInit.MAGMA_BRICKS.get());
            event.accept(BlockInit.MAGMA_BRICK_STAIRS.get());
            event.accept(BlockInit.MAGMA_BRICK_SLAB.get());

            event.accept(BlockInit.PURPUR_BRICKS.get());
            event.accept(BlockInit.PURPUR_BRICK_STAIRS.get());
            event.accept(BlockInit.PURPUR_BRICK_SLAB.get());

            event.accept(BlockInit.TOTEM_BLOCK.get());
            event.accept(BlockInit.GOLEM_STONE.get());
            event.accept(BlockInit.REGICE_CORE.get());
            event.accept(BlockInit.REGIDRAGO_CORE.get());
            event.accept(BlockInit.REGIELEKI_CORE.get());
            event.accept(BlockInit.REGIGIGA_CORE.get());
            event.accept(BlockInit.REGIROCK_CORE.get());
            event.accept(BlockInit.REGISTEEL_CORE.get());

            event.accept(BlockInit.COSMIC_DUST_BLOCK.get());
            event.accept(BlockInit.RUBY_BLOCK.get());
            event.accept(BlockInit.RUBY_STAIRS.get());
            event.accept(BlockInit.RUBY_SLAB.get());
            event.accept(BlockInit.SAPPHIRE_BLOCK.get());
            event.accept(BlockInit.SAPPHIRE_STAIRS.get());
            event.accept(BlockInit.SAPPHIRE_SLAB.get());
        }

        if (event.getTabKey() == CreativeModeTabs.COLORED_BLOCKS)
        {
            event.accept(PokecubeAdv.LAB_GLASS.get());
            event.accept(BlockInit.SPECTRUM_GLASS.get());
            event.accept(BlockInit.MIRAGE_GLASS.get());
            event.accept(BlockInit.FRAMED_DISTORTIC_MIRROR.get());

            event.accept(BlockInit.ONE_WAY_FRAMED_MIRROR.get());
            event.accept(BlockInit.ONE_WAY_GLASS.get());
            event.accept(BlockInit.ONE_WAY_GLASS_TINTED.get());
            event.accept(BlockInit.ONE_WAY_GLASS_WHITE.get());
            event.accept(BlockInit.ONE_WAY_GLASS_LIGHT_GRAY.get());
            event.accept(BlockInit.ONE_WAY_GLASS_GRAY.get());
            event.accept(BlockInit.ONE_WAY_GLASS_BLACK.get());
            event.accept(BlockInit.ONE_WAY_GLASS_BROWN.get());
            event.accept(BlockInit.ONE_WAY_GLASS_RED.get());
            event.accept(BlockInit.ONE_WAY_GLASS_ORANGE.get());
            event.accept(BlockInit.ONE_WAY_GLASS_YELLOW.get());
            event.accept(BlockInit.ONE_WAY_GLASS_LIME.get());
            event.accept(BlockInit.ONE_WAY_GLASS_GREEN.get());
            event.accept(BlockInit.ONE_WAY_GLASS_CYAN.get());
            event.accept(BlockInit.ONE_WAY_GLASS_LIGHT_BLUE.get());
            event.accept(BlockInit.ONE_WAY_GLASS_BLUE.get());
            event.accept(BlockInit.ONE_WAY_GLASS_PURPLE.get());
            event.accept(BlockInit.ONE_WAY_GLASS_MAGENTA.get());
            event.accept(BlockInit.ONE_WAY_GLASS_PINK.get());
            event.accept(BlockInit.ONE_WAY_GLASS_LAB.get());
            event.accept(BlockInit.ONE_WAY_GLASS_SPECTRUM.get());
            event.accept(BlockInit.ONE_WAY_GLASS_MIRAGE.get());
        }
    }
}
