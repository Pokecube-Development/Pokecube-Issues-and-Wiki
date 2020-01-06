package pokecube.core.items;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
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
    public void addInformation(ItemStack stack, @Nullable World playerIn, List<ITextComponent> list,
            ITooltipFlag advanced)
    {
        if (this.entry == null) this.entry = Database.getEntry(this.type);
        list.add(new TranslationTextComponent(this.entry.getUnlocalizedName()));
    }
}
