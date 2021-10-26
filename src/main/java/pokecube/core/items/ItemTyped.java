package pokecube.core.items;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.core.interfaces.PokecubeMod;

public class ItemTyped extends Item
{
    public final String type;

    public ItemTyped(Properties props, String type)
    {
        this(props, type, true);
    }

    public ItemTyped(Properties props, String type, boolean reg)
    {
        super(props);
        this.type = type;
        if (reg) this.setRegistryName(PokecubeMod.ID, type);
    }

    /**
     * allows items to add custom lines of information to the mouseover
     * description
     */
    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, @Nullable World playerIn, List<ITextComponent> list,
            ITooltipFlag advanced)
    {
        list.add(new StringTextComponent(this.type));
    }
}
