package pokecube.legends.init;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.WaterlilyBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.fmllegacy.RegistryObject;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.blocks.FlowerBase;
import pokecube.legends.blocks.MushroomBase;
import pokecube.legends.blocks.plants.BlossomLilyPadBlock;
import pokecube.legends.blocks.plants.LilyPadBlock;

public class PlantsInit
{

    // Plants
    public static RegistryObject<Block> COMPRECED_MUSHROOM;
    public static RegistryObject<Block> DISTORCED_MUSHROOM;
    public static RegistryObject<Block> GOLDEN_POPPY;
    public static RegistryObject<Block> INVERTED_ORCHID;
    public static RegistryObject<Block> PINK_TAINTED_LILY_PAD;
    public static RegistryObject<Block> TAINTED_LILY_PAD;

    static
    {
        PlantsInit.COMPRECED_MUSHROOM = PokecubeLegends.DIMENSIONS_TAB.register("compreced_mushroom", () -> new MushroomBase(BlockBehaviour.Properties
                .of(Material.PLANT, MaterialColor.COLOR_PURPLE).noCollission().randomTicks().instabreak().sound(SoundType.GRASS)
                .lightLevel((i) -> {return 1;})));

        PlantsInit.DISTORCED_MUSHROOM = PokecubeLegends.DIMENSIONS_TAB.register("distorced_mushroom", () -> new MushroomBase(BlockBehaviour.Properties
                .of(Material.PLANT, MaterialColor.COLOR_PURPLE).noCollission().randomTicks().instabreak().sound(SoundType.GRASS)
                .lightLevel((i) -> {return 1;})));

        PlantsInit.GOLDEN_POPPY = PokecubeLegends.DIMENSIONS_TAB.register("golden_poppy", () -> new FlowerBase(MobEffects.ABSORPTION, 10,
        		BlockBehaviour.Properties.of(Material.PLANT, MaterialColor.GOLD).noCollission().instabreak().sound(SoundType.CORAL_BLOCK)));

        PlantsInit.INVERTED_ORCHID = PokecubeLegends.DIMENSIONS_TAB.register("inverted_orchid", () -> new FlowerBase(MobEffects.HEAL, 10,
        		BlockBehaviour.Properties.of(Material.PLANT, MaterialColor.COLOR_PINK).noCollission().instabreak().sound(SoundType.BAMBOO_SAPLING)));

        PlantsInit.PINK_TAINTED_LILY_PAD = PokecubeLegends.DIMENSIONS_TAB.register("pink_blossom_tainted_lily_pad", () -> new BlossomLilyPadBlock(
        		BlockBehaviour.Properties.of(Material.PLANT, MaterialColor.COLOR_PURPLE).instabreak().sound(SoundType.LILY_PAD).noOcclusion()));

        PlantsInit.TAINTED_LILY_PAD = PokecubeLegends.DIMENSIONS_TAB.register("tainted_lily_pad", () -> new LilyPadBlock(
        		BlockBehaviour.Properties.of(Material.PLANT, MaterialColor.COLOR_PURPLE).instabreak().sound(SoundType.LILY_PAD).noOcclusion()));
    }

    public static void registry() {

    }
}
