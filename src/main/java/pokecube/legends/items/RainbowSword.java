package pokecube.legends.items;

import com.google.common.collect.ImmutableMultimap;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.item.IItemTier;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.legends.items.tools.ToolSword;

public class RainbowSword extends SwordItem
{
    public final IItemTier tier;
    public RainbowSword(final IItemTier material, final int bonusDamage, final float attackSpeed, final ItemGroup group)
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
