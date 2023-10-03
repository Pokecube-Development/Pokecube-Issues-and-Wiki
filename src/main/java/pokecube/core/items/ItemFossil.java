package pokecube.core.items;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.api.data.PokedexEntry;
import pokecube.core.database.Database;
import thut.lib.TComponent;

public class ItemFossil extends Item
{
    final String         type;
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
    public void appendHoverText(ItemStack stack, @Nullable Level playerIn, List<Component> list,
            TooltipFlag advanced)
    {
        if (this.entry == null) this.entry = Database.getEntry(this.type);
        list.add(TComponent.translatable(this.entry.getUnlocalizedName()).withStyle(ChatFormatting.GOLD));
    }
}
