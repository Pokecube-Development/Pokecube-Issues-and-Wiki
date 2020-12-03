package pokecube.legends.blocks.normalblocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.ToolType;
import pokecube.legends.blocks.BlockBase;
import pokecube.legends.init.ItemInit;

public class SandDistorBlock extends BlockBase
{
    public SandDistorBlock(final String name, final Material material)
    {
        super(name, Properties.create(material).sound(SoundType.SCAFFOLDING).hardnessAndResistance(4, 5).harvestTool(
                ToolType.SHOVEL).harvestLevel(2));
    }
    
    @Override
    public boolean canSustainPlant(final BlockState state, final IBlockReader world, final BlockPos pos,
            final Direction direction, final IPlantable plantable)
    {
        return true;
    }

    @Override
    public void onEntityWalk(final World world, final BlockPos pos, final Entity entity)
    {
        super.onEntityWalk(world, pos, entity);
        {
            final java.util.HashMap<String, Object> $_dependencies = new java.util.HashMap<>();
            $_dependencies.put("entity", entity);
            SandDistorBlock.executeProcedure($_dependencies);
        }
    }

    public static void executeProcedure(final java.util.HashMap<String, Object> dependencies)
    {
        if (dependencies.get("entity") == null)
        {
            System.err.println("Failed to WalkEffect!");
            return;
        }
        final Entity entity = (Entity) dependencies.get("entity");
        if (entity instanceof ServerPlayerEntity) {
        	if ((((PlayerEntity) entity).inventory.armorInventory.get(3).getItem() != new ItemStack(ItemInit.ULTRA_HELMET.get(), 1).getItem()) ||
                    (((PlayerEntity) entity).inventory.armorInventory.get(2).getItem() != new ItemStack(ItemInit.ULTRA_CHESTPLATE.get(), 1).getItem()) ||
                    (((PlayerEntity) entity).inventory.armorInventory.get(1).getItem() != new ItemStack(ItemInit.ULTRA_LEGGINGS.get(), 1).getItem()) || 
                    (((PlayerEntity) entity).inventory.armorInventory.get(0).getItem() != new ItemStack(ItemInit.ULTRA_BOOTS.get(), 1).getItem())) 
                {
        	((LivingEntity) entity).addPotionEffect(new EffectInstance(Effects.MINING_FATIGUE, 120, 1));
                }
        }
    }
}
