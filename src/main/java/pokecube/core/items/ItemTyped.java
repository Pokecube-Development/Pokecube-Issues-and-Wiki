package pokecube.core.items;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
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
    public void appendHoverText(ItemStack stack, @Nullable Level playerIn, List<Component> list,
            TooltipFlag advanced)
    {
        list.add(new TextComponent(this.type));
    }
}
