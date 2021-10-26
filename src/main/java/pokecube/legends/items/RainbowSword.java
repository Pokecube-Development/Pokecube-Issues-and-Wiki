package pokecube.legends.items;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class RainbowSword extends SwordItem
{
    public final Tier tier;
    public RainbowSword(final Tier material, final int bonusDamage, final float attackSpeed, final CreativeModeTab group)
    {
        super(material, bonusDamage, attackSpeed, new Properties().tab(group));
        this.tier = material;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean isFoil(final ItemStack itemstack)
    {
        return true;
    }

    @Override
    public boolean isValidRepairItem(ItemStack item, ItemStack repair) {
        return this.tier.getRepairIngredient().test(repair) || super.isValidRepairItem(item, repair);
    }
}
