package pokecube.legends.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SandBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.PlantType;
import pokecube.legends.init.ItemInit;

public class EffectBlockBaseSand extends SandBlock
{
    private final MobEffect effect;
    private final int dustColor;

    public EffectBlockBaseSand(int color, BlockBehaviour.Properties properties, final MobEffect effects)
    {
        super(color, properties);
        this.dustColor = color;
        this.effect = effects;
    }

    @Override
    public int getDustColor(BlockState p_55970_, BlockGetter p_55971_, BlockPos p_55972_) 
    {
       return this.dustColor;
    }

	@Override
	public boolean canSustainPlant(BlockState state, BlockGetter block, BlockPos pos, Direction direction, IPlantable plantable) 
	{
		final BlockPos plantPos = new BlockPos(pos.getX(), pos.getY() + 1, pos.getZ());
		final PlantType plantType = plantable.getPlantType(block, plantPos);
		if (plantType == PlantType.DESERT) 
		{
			return true;
		} else if (plantType == PlantType.WATER) 
		{
			return block.getBlockState(pos).getMaterial() == Material.WATER && block.getBlockState(pos) == defaultBlockState();
		} else if (plantType == PlantType.BEACH) 
		{
			return ((block.getBlockState(pos.east()).getBlock() == Blocks.WATER || block.getBlockState(pos.east()).hasProperty(BlockStateProperties.WATERLOGGED))
					|| (block.getBlockState(pos.west()).getBlock() == Blocks.WATER || block.getBlockState(pos.west()).hasProperty(BlockStateProperties.WATERLOGGED))
					|| (block.getBlockState(pos.north()).getBlock() == Blocks.WATER || block.getBlockState(pos.north()).hasProperty(BlockStateProperties.WATERLOGGED))
					|| (block.getBlockState(pos.south()).getBlock() == Blocks.WATER || block.getBlockState(pos.south()).hasProperty(BlockStateProperties.WATERLOGGED)));
		} else 
		{
			return false;
		}
	}

    @Override
    public void stepOn(final Level world, final BlockPos pos, final BlockState state, final Entity entity)
    {
        super.stepOn(world, pos, state, entity);
        EffectBlockBaseSand.applyEffects(entity, this.effect);
    }

    public static void applyEffects(final Entity entity, final MobEffect effects)
    {
        if (entity instanceof ServerPlayer) if (((Player) entity).getInventory().armor.get(3)
                .getItem() != new ItemStack(ItemInit.ULTRA_HELMET.get(), 1).getItem() || ((Player) entity)
                        .getInventory().armor.get(2).getItem() != new ItemStack(ItemInit.ULTRA_CHESTPLATE.get(), 1)
                                .getItem() || ((Player) entity).getInventory().armor.get(1).getItem() != new ItemStack(
                                        ItemInit.ULTRA_LEGGINGS.get(), 1).getItem() || ((Player) entity)
                                                .getInventory().armor.get(0).getItem() != new ItemStack(
                                                        ItemInit.ULTRA_BOOTS.get(), 1).getItem())
            ((LivingEntity) entity).addEffect(new MobEffectInstance(effects, 120, 1));
    }
}
