package pokecube.legends.items.natureedit;

//import java.util.List;
import java.util.Locale;

//import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
//import net.minecraft.util.text.ITextComponent;
//import net.minecraft.util.text.TranslationTextComponent;
//import net.minecraft.world.World;
//import net.minecraftforge.api.distmarker.Dist;
//import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.core.PokecubeItems;
import pokecube.core.interfaces.Nature;
import pokecube.legends.Reference;

public class ItemNature extends Item
{
    public static boolean isNature(final ItemStack stackIn)
    {
        return stackIn != null && stackIn.getItem() instanceof ItemNature;
    }

    public final Nature type;

    public ItemNature(final Nature type)
    {
        super(new Item.Properties().group(PokecubeItems.POKECUBEITEMS));
        final String name = type.name().equals("???") ? "unknown" : type.name();
        this.setRegistryName(Reference.ID, "mint_" + name.toLowerCase(Locale.ROOT));
        this.type = type;
    }

    //@Override
    //@OnlyIn(Dist.CLIENT)
    //public void addInformation(final ItemStack stack, final World worldIn, final List<ITextComponent> tooltip,
    //        final ITooltipFlag flagIn)
    //{
    //    final String message = Nature.getLocalizationKey(this.type);
    //    tooltip.add(new TranslationTextComponent(message));
    //}
}
