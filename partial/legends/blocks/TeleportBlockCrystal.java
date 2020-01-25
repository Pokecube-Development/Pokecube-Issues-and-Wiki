/*package pokecube.legends.blocks;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.legends.init.DimensionInit;
import pokecube.legends.worldgen.dimensions.TeleportFuncionCrystal;

public class TeleportBlockCrystal extends BlockBase
{	
	EnumParticleTypes EnumParticlesTypes;
	public TeleportBlockCrystal(String name, Material material) 
	{
		super(name, material);
		setSoundType(SoundType.GLASS);
		setHardness(5.0F);
		setResistance(15.0F);
		setHarvestLevel("pickace", 2);
		setLightLevel(0.8F);
		setLightOpacity(5);
	}
	
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return Item.getByNameOrId("minecraft:glass");
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state)
	{
		return false;
	}
	
	@Override
	public boolean isFullCube(IBlockState state) 
	{
		return false;
	}
	
	@Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
    }
	
    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer playerIn,
    	EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!playerIn.isRiding() && !playerIn.isBeingRidden() && playerIn.isNonBoss()) {
            if (!playerIn.world.isRemote && playerIn instanceof EntityPlayerMP) {
                EntityPlayerMP player = (EntityPlayerMP) playerIn;
                MinecraftServer minecraftServer = player.getServer();
                int dimensionId = world.provider.getDimension();
                int dimensionIn;

                if (player.timeUntilPortal > 0) {
                    player.timeUntilPortal = 10;
                } else {
                    player.isInvulnerableDimensionChange();
                    if (dimensionId == DimensionInit.TEMPORAL_PLACE.getId()) {
                        dimensionIn = 0;
                    } else {
                        dimensionIn = DimensionInit.TEMPORAL_PLACE.getId();
                    }

                    player.timeUntilPortal = 10;
                    minecraftServer.getPlayerList().transferPlayerToDimension(player, dimensionIn, new TeleportFuncionCrystal(minecraftServer.getWorld(dimensionIn)));
                }
            }
        }
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
        if (rand.nextInt(100) == 0) {
            world.playSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.AMBIENT_CAVE, SoundCategory.BLOCKS, 0.5F, rand.nextFloat() * 0.4F + 0.8F, false);
        }
    }
}*/
