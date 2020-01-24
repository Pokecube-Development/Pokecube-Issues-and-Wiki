package thut.bling;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.IForgeRegistry;
import thut.bling.network.BagPacket;
import thut.core.common.ThutCore;
import thut.wearables.EnumWearable;
import thut.wearables.IWearable;

public class BlingItem extends Item implements IWearable
{

    public static Map<String, EnumWearable> wearables = Maps.newHashMap();
    public static Map<Item, EnumWearable>   defaults  = Maps.newHashMap();
    public static List<String>              names     = Lists.newArrayList();
    public static List<Item>                bling     = Lists.newArrayList();
    static
    {
        BlingItem.wearables.put("ring", EnumWearable.FINGER);
        BlingItem.wearables.put("neck", EnumWearable.NECK);
        BlingItem.wearables.put("wrist", EnumWearable.WRIST);
        BlingItem.wearables.put("eye", EnumWearable.EYE);
        BlingItem.wearables.put("ankle", EnumWearable.ANKLE);
        BlingItem.wearables.put("ear", EnumWearable.EAR);
        BlingItem.wearables.put("waist", EnumWearable.WAIST);
        BlingItem.wearables.put("hat", EnumWearable.HAT);
        BlingItem.wearables.put("bag", EnumWearable.BACK);
        BlingItem.wearables.put("bag_ender_vanilla", EnumWearable.BACK);
        BlingItem.wearables.put("bag_ender_large", EnumWearable.BACK);
        BlingItem.names.addAll(BlingItem.wearables.keySet());
        Collections.sort(BlingItem.names);
    }

    public static void initDefaults(final IForgeRegistry<Item> iForgeRegistry)
    {
        for (final String s : BlingItem.names)
        {
            final BlingItem bling = new BlingItem(s, BlingItem.wearables.get(s));
            bling.setRegistryName(ThutBling.MODID, "bling_" + s);
            BlingItem.bling.add(bling);
            iForgeRegistry.register(bling);
        }
    }

    public final String        name;
    private final EnumWearable slot;

    public BlingItem(final String name, final EnumWearable slot)
    {
        super(new Properties().group(ThutCore.THUTITEMS).maxStackSize(1));
        this.name = name;
        this.slot = slot;
        BlingItem.defaults.put(this, slot);
        // if (name.equals("bag_ender_large")) InventoryLarge.INVALID.add(this);
    }

    /**
     * allows items to add custom lines of information to the mouseover
     * description
     */
    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(final ItemStack stack, @Nullable final World playerIn, final List<ITextComponent> list,
            final ITooltipFlag advanced)
    {
        if (stack.hasTag() && stack.getTag().contains("dyeColour"))
        {
            final int damage = stack.getTag().getInt("dyeColour");
            final DyeColor colour = DyeColor.byId(damage);
            list.add(new TranslationTextComponent(colour.getTranslationKey()));
        }
        if (stack.hasTag() && stack.getTag().contains("gemTag"))
        {
            final ItemStack gem = ItemStack.read(stack.getTag().getCompound("gemTag"));
            if (gem != null) try
            {
                list.add(gem.getDisplayName());
            }
            catch (final Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(final World worldIn, final PlayerEntity playerIn, final Hand hand)
    {
        if (this.slot == EnumWearable.BACK)
        {
            if (!worldIn.isRemote) BagPacket.OpenBag(playerIn);
            return new ActionResult<>(ActionResultType.SUCCESS, playerIn.getHeldItem(hand));
        }
        return super.onItemRightClick(worldIn, playerIn, hand);
    }

    @Override
    public EnumWearable getSlot(final ItemStack stack)
    {
        return this.slot;
    }

    @Override
    public void renderWearable(final EnumWearable slot, final int index, final LivingEntity wearer,
            final ItemStack stack, final float partialTicks)
    {
        ThutBling.PROXY.renderWearable(slot, index, wearer, stack, partialTicks);
    }

    @Override
    public boolean dyeable(final ItemStack stack)
    {
        return true;
    }
}
