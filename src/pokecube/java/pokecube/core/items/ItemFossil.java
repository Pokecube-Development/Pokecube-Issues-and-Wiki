package pokecube.core.items;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import pokecube.api.data.PokedexEntry;
import pokecube.core.database.Database;

public class ItemFossil extends Item
{
    final String type;
    private PokedexEntry entry;

    public ItemFossil(Properties props, String type)
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
        if (this.entry == null) this.entry = Database.getEntry(this.type);
        tooltipComponents.add(Component.translatable(this.entry.getUnlocalizedName()).withStyle(ChatFormatting.GOLD));
    }
}
