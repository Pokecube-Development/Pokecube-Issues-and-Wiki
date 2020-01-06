package pokecube.core.items.megastuff;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.core.PokecubeItems;
import pokecube.core.interfaces.PokecubeMod;

public class ItemMegawearable extends Item
{
    private static Map<String, String> wearables = Maps.newHashMap();

    static
    {
        ItemMegawearable.registerWearable("ring", "FINGER");
        ItemMegawearable.registerWearable("belt", "WAIST");
        ItemMegawearable.registerWearable("hat", "HAT");
    }

    public static String getSlot(String name)
    {
        return ItemMegawearable.wearables.get(name);
    }

    public static Collection<String> getWearables()
    {
        return ItemMegawearable.wearables.keySet();
    }

    public static void registerWearable(String name, String slot)
    {
        ItemMegawearable.wearables.put(name, slot);
    }

    public final String name;
    public final String slot;

    public ItemMegawearable(String name, String slot)
    {
        super(new Properties().group(PokecubeItems.POKECUBEITEMS).maxStackSize(1));
        this.name = name;
        this.slot = slot;
        this.setRegistryName(PokecubeMod.ID, "mega_" + name);

    }

    /**
     * allows items to add custom lines of information to the mouseover
     * description
     */
    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World playerIn, List<ITextComponent> tooltip,
            ITooltipFlag advanced)
    {
        if (stack.hasTag() && stack.getTag().contains("dyeColour"))
        {
            final int damage = stack.getTag().getInt("dyeColour");
            final DyeColor colour = DyeColor.byId(damage);
            tooltip.add(new TranslationTextComponent(colour.getTranslationKey()));
        }
    }

    @Override
    public EquipmentSlotType getEquipmentSlot(ItemStack stack)
    {
        final String name = this.getRegistryName().getPath().replace("mega_", "");
        if (name.equals("megahat")) return EquipmentSlotType.HEAD;
        return super.getEquipmentSlot(stack);
    }
}
