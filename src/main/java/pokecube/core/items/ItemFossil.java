package pokecube.core.items;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public class ItemFossil extends Item
{
    final String         type;
    private PokedexEntry entry;

    public ItemFossil(Properties props, String type)
    {
        super(props);
        this.setRegistryName(PokecubeCore.MODID, "fossil_" + type);
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
        list.add(new TranslatableComponent(this.entry.getUnlocalizedName()));
    }
}
