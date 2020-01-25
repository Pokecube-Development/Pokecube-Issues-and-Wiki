package pokecube.legends.items.zmove;

import java.util.List;
import java.util.Locale;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.core.PokecubeItems;
import pokecube.core.utils.PokeType;
import pokecube.legends.Reference;

public class ItemZCrystal extends Item
{
    public static boolean isZCrystal(final ItemStack stackIn)
    {
        return stackIn != null && stackIn.getItem() instanceof ItemZCrystal;
    }

    public final PokeType type;

    public ItemZCrystal(final PokeType type)
    {
        super(new Item.Properties().group(PokecubeItems.POKECUBEITEMS));
        final String name = type.name.equals("???") ? "unknown" : type.name;
        this.setRegistryName(Reference.ID, "z_" + name.toLowerCase(Locale.ROOT));
        this.type = type;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(final ItemStack stack, final World worldIn, final List<ITextComponent> tooltip,
            final ITooltipFlag flagIn)
    {
        final String message = PokeType.getTranslatedName(this.type);
        tooltip.add(new TranslationTextComponent(message));
    }

}
