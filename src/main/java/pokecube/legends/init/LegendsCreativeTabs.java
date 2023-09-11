package pokecube.legends.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import pokecube.adventures.init.AdvCreativeTabs;
import pokecube.api.entity.pokemob.Nature;
import pokecube.api.utils.PokeType;
import pokecube.core.PokecubeItems;
import pokecube.legends.Reference;

@Mod.EventBusSubscriber(modid = Reference.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class LegendsCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Reference.ID);

    public static final RegistryObject<CreativeModeTab> BUILDING_BLOCKS_TAB = TABS.register("building_blocks_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.pokecube_legends.building_blocks"))
            .icon(() -> new ItemStack(BlockInit.SKY_BRICKS.get()))
            .withTabsBefore(AdvCreativeTabs.BADGES_TAB.getId())
            .displayItems((parameters, output) -> {
                output.accept(BlockInit.SKY_BRICKS.get());
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
                output.accept(ItemInit.SOUL_DEW.get());
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

                output.accept(PokecubeItems.getStack("mint_docile"));
                output.accept(PokecubeItems.getStack("mint_calm"));
                output.accept(PokecubeItems.getStack("mint_gentle"));
                output.accept(PokecubeItems.getStack("mint_sassy"));
                output.accept(PokecubeItems.getStack("mint_careful"));
                output.accept(PokecubeItems.getStack("mint_hardy"));
                output.accept(PokecubeItems.getStack("mint_lonely"));
                output.accept(PokecubeItems.getStack("mint_brave"));
                output.accept(PokecubeItems.getStack("mint_adamant"));
                output.accept(PokecubeItems.getStack("mint_naughty"));
                output.accept(PokecubeItems.getStack("mint_serious"));
                output.accept(PokecubeItems.getStack("mint_timid"));
                output.accept(PokecubeItems.getStack("mint_hasty"));
                output.accept(PokecubeItems.getStack("mint_jolly"));
                output.accept(PokecubeItems.getStack("mint_modest"));
                output.accept(PokecubeItems.getStack("mint_mild"));
                output.accept(PokecubeItems.getStack("mint_quiet"));
                output.accept(PokecubeItems.getStack("mint_rash"));
                output.accept(PokecubeItems.getStack("mint_quirky"));
                output.accept(PokecubeItems.getStack("mint_bold"));
                output.accept(PokecubeItems.getStack("mint_relaxed"));
                output.accept(PokecubeItems.getStack("mint_impish"));
                output.accept(PokecubeItems.getStack("mint_lax"));
                output.accept(PokecubeItems.getStack("mint_bashful"));

                for (final Nature type : ItemInit.mints.keySet())
                    output.accept(ItemInit.mints.get(type).get());

                output.accept(PokecubeItems.getStack("z_unknown"));
                output.accept(PokecubeItems.getStack("z_normal"));
                output.accept(PokecubeItems.getStack("z_steel"));
                output.accept(PokecubeItems.getStack("z_rock"));
                output.accept(PokecubeItems.getStack("z_dark"));
                output.accept(PokecubeItems.getStack("z_fire"));
                output.accept(PokecubeItems.getStack("z_ground"));
                output.accept(PokecubeItems.getStack("z_fighting"));
                output.accept(PokecubeItems.getStack("z_electric"));
                output.accept(PokecubeItems.getStack("z_bug"));
                output.accept(PokecubeItems.getStack("z_grass"));
                output.accept(PokecubeItems.getStack("z_ice"));
                output.accept(PokecubeItems.getStack("z_water"));
                output.accept(PokecubeItems.getStack("z_flying"));
                output.accept(PokecubeItems.getStack("z_dragon"));
                output.accept(PokecubeItems.getStack("z_poison"));
                output.accept(PokecubeItems.getStack("z_ghost"));
                output.accept(PokecubeItems.getStack("z_psychic"));
                output.accept(PokecubeItems.getStack("z_fairy"));

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
    }
}
