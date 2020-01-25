package pokecube.legends.blocks.plants;

import java.util.Random;

import net.minecraft.block.BlockBush;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.legends.IHasModel;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.init.BlockInit;
import pokecube.legends.init.ItemInit;

public class BlockBasePlant extends BlockBush implements IHasModel
{
    public BlockBasePlant(String name, Material material)
    {
        super(material);
        setUnlocalizedName(name);
        setRegistryName(name);
        setCreativeTab(PokecubeMod.creativeTabPokecubeBerries);
        if (material == Material.PLANTS) this.setSoundType(SoundType.PLANT);

        BlockInit.BLOCKS.add(this);
        ItemInit.ITEMS.add(new ItemBlock(this).setRegistryName(this.getRegistryName()));

    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerModels()
    {
        PokecubeLegends.proxy.registerItemRenderer(Item.getItemFromBlock(this), 0, "inventory");
    }

    public boolean generate(World worldIn, Random rand, BlockPos position)
    {
//        BlockBush flower = (BlockBush) BlockInit.ULTRA_MUSS1;
//        
//        for (int i = 0; i < 64; ++i)
//        {
//            BlockPos blockpos = position.add(rand.nextInt(8) - rand.nextInt(8), rand.nextInt(4) - rand.nextInt(4), rand.nextInt(8) - rand.nextInt(8));
//
//            if (worldIn.isAirBlock(blockpos) && (!worldIn.provider.isNether() || blockpos.getY() < 255) && flower.canBlockStay(worldIn, blockpos, flower.getDefaultState()))
//            {
//                worldIn.setBlockState(blockpos, flower.getDefaultState(), 2);
//            }
//        }

        return true;
    }
}
