package pokecube.legends.legacy;

import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries.Keys;
import net.minecraftforge.registries.MissingMappingsEvent;
import net.minecraftforge.registries.MissingMappingsEvent.Mapping;
import pokecube.legends.init.BlockInit;

public class LegendsRegistryChangeFixer
{
    private static final Set<ResourceLocation> distortic_framed_mirror =
            Sets.newHashSet(new ResourceLocation("pokecube_legends:framed_distortic_mirror"));
    private static final Set<ResourceLocation> one_way_framed_mirror =
            Sets.newHashSet(new ResourceLocation("pokecube_legends:distortic_one_way_framed_mirror"));
    private static final Set<ResourceLocation> one_way_glass =
            Sets.newHashSet(new ResourceLocation("pokecube_legends:distortic_one_way_glass"));
    private static final Set<ResourceLocation> one_way_tinted_glass =
            Sets.newHashSet(new ResourceLocation("pokecube_legends:distortic_one_way_tinted_glass"));
    private static final Set<ResourceLocation> one_way_white_stained_glass =
            Sets.newHashSet(new ResourceLocation("pokecube_legends:distortic_one_way_white_stained_glass"));
    private static final Set<ResourceLocation> one_way_light_gray_stained_glass =
            Sets.newHashSet(new ResourceLocation("pokecube_legends:distortic_one_way_light_gray_stained_glass"));
    private static final Set<ResourceLocation> one_way_gray_stained_glass =
            Sets.newHashSet(new ResourceLocation("pokecube_legends:distortic_one_way_gray_stained_glass"));
    private static final Set<ResourceLocation> one_way_black_stained_glass =
            Sets.newHashSet(new ResourceLocation("pokecube_legends:distortic_one_way_black_stained_glass"));
    private static final Set<ResourceLocation> one_way_brown_stained_glass =
            Sets.newHashSet(new ResourceLocation("pokecube_legends:distortic_one_way_brown_stained_glass"));
    private static final Set<ResourceLocation> one_way_red_stained_glass =
            Sets.newHashSet(new ResourceLocation("pokecube_legends:distortic_one_way_red_stained_glass"));
    private static final Set<ResourceLocation> one_way_orange_stained_glass =
            Sets.newHashSet(new ResourceLocation("pokecube_legends:distortic_one_way_orange_stained_glass"));
    private static final Set<ResourceLocation> one_way_yellow_stained_glass =
            Sets.newHashSet(new ResourceLocation("pokecube_legends:distortic_one_way_yellow_stained_glass"));
    private static final Set<ResourceLocation> one_way_lime_stained_glass =
            Sets.newHashSet(new ResourceLocation("pokecube_legends:distortic_one_way_lime_stained_glass"));
    private static final Set<ResourceLocation> one_way_green_stained_glass =
            Sets.newHashSet(new ResourceLocation("pokecube_legends:distortic_one_way_green_stained_glass"));
    private static final Set<ResourceLocation> one_way_cyan_stained_glass =
            Sets.newHashSet(new ResourceLocation("pokecube_legends:distortic_one_way_cyan_stained_glass"));
    private static final Set<ResourceLocation> one_way_light_blue_stained_glass =
            Sets.newHashSet(new ResourceLocation("pokecube_legends:distortic_one_way_light_blue_stained_glass"));
    private static final Set<ResourceLocation> one_way_blue_stained_glass =
            Sets.newHashSet(new ResourceLocation("pokecube_legends:distortic_one_way_blue_stained_glass"));
    private static final Set<ResourceLocation> one_way_purple_stained_glass =
            Sets.newHashSet(new ResourceLocation("pokecube_legends:distortic_one_way_purple_stained_glass"));
    private static final Set<ResourceLocation> one_way_magenta_stained_glass =
            Sets.newHashSet(new ResourceLocation("pokecube_legends:distortic_one_way_magenta_stained_glass"));
    private static final Set<ResourceLocation> one_way_pink_stained_glass =
            Sets.newHashSet(new ResourceLocation("pokecube_legends:distortic_one_way_pink_stained_glass"));
    private static final Set<ResourceLocation> one_way_spectrum_glass =
            Sets.newHashSet(new ResourceLocation("pokecube_legends:distortic_one_way_spectrum_glass"));
    private static final Set<ResourceLocation> one_way_mirage_glass =
            Sets.newHashSet(new ResourceLocation("pokecube_legends:distortic_one_way_mirage_glass"));
    private static final Set<ResourceLocation> one_way_laboratory_glass =
            Sets.newHashSet(new ResourceLocation("pokecube_legends:distortic_one_way_laboratory_glass"));

    @SubscribeEvent
    public static void onRegistryMissingEvent(MissingMappingsEvent event)
    {
        // Remap the One-Way Glass Blocks.
        if (event.getKey().equals(Keys.BLOCKS))
        {
            List<Mapping<Block>> mappings = event.getAllMappings(Keys.BLOCKS);
            mappings.forEach(m -> {
                if (distortic_framed_mirror.contains(m.getKey())) m.remap(BlockInit.DISTORTIC_FRAMED_MIRROR.get());
                if (one_way_glass.contains(m.getKey())) m.remap(BlockInit.ONE_WAY_GLASS.get());
                if (one_way_tinted_glass.contains(m.getKey())) m.remap(BlockInit.ONE_WAY_GLASS_TINTED.get());
                if (one_way_white_stained_glass.contains(m.getKey())) m.remap(BlockInit.ONE_WAY_GLASS_WHITE.get());
                if (one_way_light_gray_stained_glass.contains(m.getKey())) m.remap(BlockInit.ONE_WAY_GLASS_LIGHT_GRAY.get());
                if (one_way_gray_stained_glass.contains(m.getKey())) m.remap(BlockInit.ONE_WAY_GLASS_GRAY.get());
                if (one_way_black_stained_glass.contains(m.getKey())) m.remap(BlockInit.ONE_WAY_GLASS_BLACK.get());
                if (one_way_brown_stained_glass.contains(m.getKey())) m.remap(BlockInit.ONE_WAY_GLASS_BROWN.get());
                if (one_way_red_stained_glass.contains(m.getKey())) m.remap(BlockInit.ONE_WAY_GLASS_RED.get());
                if (one_way_orange_stained_glass.contains(m.getKey())) m.remap(BlockInit.ONE_WAY_GLASS_ORANGE.get());
                if (one_way_yellow_stained_glass.contains(m.getKey())) m.remap(BlockInit.ONE_WAY_GLASS_YELLOW.get());
                if (one_way_lime_stained_glass.contains(m.getKey())) m.remap(BlockInit.ONE_WAY_GLASS_LIME.get());
                if (one_way_green_stained_glass.contains(m.getKey())) m.remap(BlockInit.ONE_WAY_GLASS_GREEN.get());
                if (one_way_cyan_stained_glass.contains(m.getKey())) m.remap(BlockInit.ONE_WAY_GLASS_CYAN.get());
                if (one_way_light_blue_stained_glass.contains(m.getKey())) m.remap(BlockInit.ONE_WAY_GLASS_LIGHT_BLUE.get());
                if (one_way_blue_stained_glass.contains(m.getKey())) m.remap(BlockInit.ONE_WAY_GLASS_BLUE.get());
                if (one_way_purple_stained_glass.contains(m.getKey())) m.remap(BlockInit.ONE_WAY_GLASS_PURPLE.get());
                if (one_way_magenta_stained_glass.contains(m.getKey())) m.remap(BlockInit.ONE_WAY_GLASS_MAGENTA.get());
                if (one_way_pink_stained_glass.contains(m.getKey())) m.remap(BlockInit.ONE_WAY_GLASS_PINK.get());
                if (one_way_spectrum_glass.contains(m.getKey())) m.remap(BlockInit.ONE_WAY_GLASS_SPECTRUM.get());
                if (one_way_mirage_glass.contains(m.getKey())) m.remap(BlockInit.ONE_WAY_GLASS_MIRAGE.get());
                if (one_way_laboratory_glass.contains(m.getKey())) m.remap(BlockInit.ONE_WAY_GLASS_LAB.get());
            });
        }

        // Remap the One-Way Glass Block items.
        if (event.getKey().equals(Keys.ITEMS))
        {
            List<Mapping<Item>> mappings = event.getAllMappings(Keys.ITEMS);
            mappings.forEach(m -> {
                if (distortic_framed_mirror.contains(m.getKey())) m.remap(BlockInit.DISTORTIC_FRAMED_MIRROR.get().asItem());
                if (one_way_glass.contains(m.getKey())) m.remap(BlockInit.ONE_WAY_GLASS.get().asItem());
                if (one_way_tinted_glass.contains(m.getKey())) m.remap(BlockInit.ONE_WAY_GLASS_TINTED.get().asItem());
                if (one_way_white_stained_glass.contains(m.getKey())) m.remap(BlockInit.ONE_WAY_GLASS_WHITE.get().asItem());
                if (one_way_light_gray_stained_glass.contains(m.getKey())) m.remap(BlockInit.ONE_WAY_GLASS_LIGHT_GRAY.get().asItem());
                if (one_way_gray_stained_glass.contains(m.getKey())) m.remap(BlockInit.ONE_WAY_GLASS_GRAY.get().asItem());
                if (one_way_black_stained_glass.contains(m.getKey())) m.remap(BlockInit.ONE_WAY_GLASS_BLACK.get().asItem());
                if (one_way_brown_stained_glass.contains(m.getKey())) m.remap(BlockInit.ONE_WAY_GLASS_BROWN.get().asItem());
                if (one_way_red_stained_glass.contains(m.getKey())) m.remap(BlockInit.ONE_WAY_GLASS_RED.get().asItem());
                if (one_way_orange_stained_glass.contains(m.getKey())) m.remap(BlockInit.ONE_WAY_GLASS_ORANGE.get().asItem());
                if (one_way_yellow_stained_glass.contains(m.getKey())) m.remap(BlockInit.ONE_WAY_GLASS_YELLOW.get().asItem());
                if (one_way_lime_stained_glass.contains(m.getKey())) m.remap(BlockInit.ONE_WAY_GLASS_LIME.get().asItem());
                if (one_way_green_stained_glass.contains(m.getKey())) m.remap(BlockInit.ONE_WAY_GLASS_GREEN.get().asItem());
                if (one_way_cyan_stained_glass.contains(m.getKey())) m.remap(BlockInit.ONE_WAY_GLASS_CYAN.get().asItem());
                if (one_way_light_blue_stained_glass.contains(m.getKey())) m.remap(BlockInit.ONE_WAY_GLASS_LIGHT_BLUE.get().asItem());
                if (one_way_blue_stained_glass.contains(m.getKey())) m.remap(BlockInit.ONE_WAY_GLASS_BLUE.get().asItem());
                if (one_way_purple_stained_glass.contains(m.getKey())) m.remap(BlockInit.ONE_WAY_GLASS_PURPLE.get().asItem());
                if (one_way_magenta_stained_glass.contains(m.getKey())) m.remap(BlockInit.ONE_WAY_GLASS_MAGENTA.get().asItem());
                if (one_way_pink_stained_glass.contains(m.getKey())) m.remap(BlockInit.ONE_WAY_GLASS_PINK.get().asItem());
                if (one_way_spectrum_glass.contains(m.getKey())) m.remap(BlockInit.ONE_WAY_GLASS_SPECTRUM.get().asItem());
                if (one_way_mirage_glass.contains(m.getKey())) m.remap(BlockInit.ONE_WAY_GLASS_MIRAGE.get().asItem());
                if (one_way_laboratory_glass.contains(m.getKey())) m.remap(BlockInit.ONE_WAY_GLASS_LAB.get().asItem());
            });
        }
    }
}
