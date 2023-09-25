package thut.bling;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import thut.api.item.ItemList;
import thut.bling.client.BlingitemRenderer;
import thut.bling.client.ClientSetupHandler;
import thut.bling.network.PacketBag;
import thut.core.common.ThutCore;
import thut.lib.RegHelper;
import thut.lib.TComponent;
import thut.wearables.EnumWearable;
import thut.wearables.IWearable;
import thut.wearables.ThutWearables;

@Mod.EventBusSubscriber(modid = ThutBling.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BlingItem extends Item implements IWearable, DyeableLeatherItem
{
    private static Set<ResourceLocation> errored = Sets.newHashSet();
    public static Map<String, EnumWearable> wearables = Maps.newHashMap();
    public static Map<String, RegistryObject<Item>> blingWearables = Maps.newHashMap();
    public static Map<Item, EnumWearable> defaults = Maps.newHashMap();
    public static List<String> names = Lists.newArrayList();
    public static List<Item> bling = Lists.newArrayList();
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

    public static void init()
    {
        for (final String type : BlingItem.names)
        {
            BlingItem.blingWearables.put(type, ThutBling.ITEMS.register("bling_" + type,
                    () -> new BlingItem(type, BlingItem.wearables.get(type))));
        }
        ThutWearables.WORNICON = () -> BlingItem.getStack("bling_hat");
    }

    public final String name;
    private final EnumWearable slot;

    public BlingItem(final String name, final EnumWearable slot)
    {
        super(new Properties().stacksTo(1));
        this.name = name;
        this.slot = slot;
        BlingItem.defaults.put(this, slot);
        BlingItem.bling.add(this);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer)
    {
        consumer.accept(new IClientItemExtensions()
        {
            private final BlockEntityWithoutLevelRenderer renderer = BlingitemRenderer.INSTANCE;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer()
            {
                return this.renderer;
            }
        });
    }

    /**
     * allows items to add custom lines of information to the mouseover
     * description
     */
    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(final ItemStack stack, @Nullable final Level playerIn, final List<Component> list,
            final TooltipFlag advanced)
    {
        if (stack.hasTag() && stack.getTag().contains("dyeColour"))
        {
            final int damage = stack.getTag().getInt("dyeColour");
            final DyeColor colour = DyeColor.byId(damage);
            list.add(TComponent.translatable(colour.getName()));
        }
        if (stack.hasTag() && stack.getTag().contains("gemTag"))
        {
            final ItemStack gem = ItemStack.of(stack.getTag().getCompound("gemTag"));
            if (gem != null) try
            {
                list.add(gem.getHoverName());
            }
            catch (final Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    public InteractionResult useOn(final UseOnContext context)
    {
        if (this.slot == EnumWearable.BACK)
        {
            final Level worldIn = context.getLevel();
            final Player playerIn = context.getPlayer();
            if (!worldIn.isClientSide) PacketBag.sendOpenPacket(playerIn, context.getItemInHand());
            return InteractionResult.SUCCESS;
        }
        return super.useOn(context);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level worldIn, final Player playerIn,
            final InteractionHand hand)
    {
        if (this.slot == EnumWearable.BACK)
        {
            if (!worldIn.isClientSide) PacketBag.sendOpenPacket(playerIn, playerIn.getItemInHand(hand));
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, playerIn.getItemInHand(hand));
        }
        return super.use(worldIn, playerIn, hand);
    }

    @Override
    public EnumWearable getSlot(final ItemStack stack)
    {
        return this.slot;
    }

    @OnlyIn(value = Dist.CLIENT)
    @Override
    public void renderWearable(final PoseStack mat, final MultiBufferSource buff, final EnumWearable slot,
            final int index, final LivingEntity wearer, final ItemStack stack, final float partialTicks,
            final int brightness, final int overlay)
    {
        ClientSetupHandler.renderWearable(mat, buff, slot, index, wearer, stack, partialTicks, brightness, overlay);
    }

    @Override
    public boolean dyeable(final ItemStack stack)
    {
        return true;
    }

    @Override
    public int getColor(ItemStack stack)
    {
        CompoundTag compoundtag = stack.getTagElement("display");

        return compoundtag != null && compoundtag.contains("color", 99) ? compoundtag.getInt("color")
                : this.slot == EnumWearable.NECK ? 0xFFFFFFFF : this.slot == EnumWearable.EYE ? 0xFF282828 : 0xFFA06540;
    }

    public static ItemStack getStack(final ResourceLocation loc)
    {
        return BlingItem.getStack(loc, true);
    }

    public static ItemStack getStack(final ResourceLocation loc, final boolean stacktrace)
    {
        final TagKey<Item> tag = TagKey.create(RegHelper.ITEM_REGISTRY, loc);
        if (tag != null)
        {
            List<Item> items = ForgeRegistries.ITEMS.tags().getTag(tag).stream().toList();
            if (!items.isEmpty())
            {
                final Item item = items.get(new Random(2).nextInt(items.size()));
                if (item != null) return new ItemStack(item);
            }
        }
        final Item item = ForgeRegistries.ITEMS.getValue(loc);
        if (item != null) return new ItemStack(item);
        if (stacktrace && BlingItem.errored.add(loc))
        {
            ThutCore.LOGGER.error(loc + " Not found in list of items.");
        }
        return ItemStack.EMPTY;
    }

    public static ItemStack getStack(final String name)
    {
        return BlingItem.getStack(name, true);
    }

    public static ItemStack getStack(final String name, final boolean stacktrace)
    {
        if (!BlingItem.stackExists(name)) return ItemStack.EMPTY;
        final ResourceLocation loc = BlingItem.toBlingResource(name);
        return BlingItem.getStack(loc, stacktrace);
    }

    public static boolean stackExists(final String name)
    {
        if (name == null) return false;
        final ResourceLocation loc = BlingItem.toBlingResource(name);
        final TagKey<Item> old = TagKey.create(RegHelper.ITEM_REGISTRY, loc);
        final Item item = ForgeRegistries.ITEMS.getValue(loc);
        return old != null || ItemList.pendingTags.containsKey(loc) || item != null;
    }

    public static ResourceLocation toBlingResource(final String name)
    {
        return toResource(name, ThutBling.MODID);
    }

    public static ResourceLocation toResource(final String name, final String modid)
    {
        ResourceLocation loc;
        if (!name.contains(":")) loc = new ResourceLocation(ThutBling.MODID, name);
        else loc = new ResourceLocation(name);
        return loc;
    }
}
