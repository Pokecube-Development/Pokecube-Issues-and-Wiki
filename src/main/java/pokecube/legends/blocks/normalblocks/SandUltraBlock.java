package pokecube.legends.blocks.normalblocks;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import pokecube.legends.blocks.BlockBase;
import pokecube.legends.init.ItemInit;

public class SandUltraBlock extends BlockBase
{
    public SandUltraBlock(final String name, final Material material, MaterialColor color)
    {
        super(name, Properties.create(material, color).sound(SoundType.SAND).hardnessAndResistance(2, 6).harvestTool(
                ToolType.SHOVEL).harvestLevel(1));
    }

    @Override
    public boolean matchesBlock(Block block) {
        return this == Blocks.SAND || this == block;
    }

    @Override
    public void onEntityWalk(final World world, final BlockPos pos, final Entity entity)
    {
        super.onEntityWalk(world, pos, entity);
        {
            final java.util.HashMap<String, Object> $_dependencies = new java.util.HashMap<>();
            $_dependencies.put("entity", entity);
            SandUltraBlock.executeProcedure($_dependencies);
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
        if (entity instanceof ServerPlayerEntity) if (((PlayerEntity) entity).inventory.armorInventory.get(3).getItem() != new ItemStack(ItemInit.ULTRA_HELMET.get(), 1).getItem() ||
                ((PlayerEntity) entity).inventory.armorInventory.get(2).getItem() != new ItemStack(ItemInit.ULTRA_CHESTPLATE.get(), 1).getItem() ||
                ((PlayerEntity) entity).inventory.armorInventory.get(1).getItem() != new ItemStack(ItemInit.ULTRA_LEGGINGS.get(), 1).getItem() ||
                ((PlayerEntity) entity).inventory.armorInventory.get(0).getItem() != new ItemStack(ItemInit.ULTRA_BOOTS.get(), 1).getItem()) ((LivingEntity) entity).addPotionEffect(new EffectInstance(Effects.LEVITATION, 120, 1));
    }
}
