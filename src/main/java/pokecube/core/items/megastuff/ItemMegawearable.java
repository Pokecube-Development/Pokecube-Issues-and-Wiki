package pokecube.core.items.megastuff;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
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
        super(new Properties().tab(PokecubeItems.POKECUBEITEMS).stacksTo(1));
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
    public void appendHoverText(ItemStack stack, @Nullable Level playerIn, List<Component> tooltip,
            TooltipFlag advanced)
    {
        if (stack.hasTag() && stack.getTag().contains("dyeColour"))
        {
            final int damage = stack.getTag().getInt("dyeColour");
            final DyeColor colour = DyeColor.byId(damage);
            tooltip.add(new TranslatableComponent(colour.getName()));
        }
    }

    @Override
    public EquipmentSlot getEquipmentSlot(ItemStack stack)
    {
        final String name = this.getRegistryName().getPath().replace("mega_", "");
        if (name.equals("megahat")) return EquipmentSlot.HEAD;
        return super.getEquipmentSlot(stack);
    }
}
