package pokecube.core.items;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import thut.lib.TComponent;

public class ItemTyped extends Item
{
    public final String type;

    public ItemTyped(Properties props, String type)
    {
        super(props);
        this.type = type;
    }

    /**
     * allows items to add custom lines of information to the mouseover
     * description
     */
    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents,
            TooltipFlag tooltipFlag)
    {
        tooltipComponents.add(TComponent.literal(this.type).withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.ITALIC));
    }
}
