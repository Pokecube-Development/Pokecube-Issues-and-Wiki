package thut.bling;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraftforge.client.IItemRenderProperties;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import thut.api.ThutCaps;
import thut.bling.bag.small.SmallInventory;
import thut.bling.bag.small.SmallManager;
import thut.bling.client.BlingitemRenderer;
import thut.bling.client.ClientSetupHandler;
import thut.bling.network.PacketBag;
import thut.core.common.ThutCore;
import thut.lib.TComponent;
import thut.wearables.EnumWearable;
import thut.wearables.IWearable;

public class BlingItem extends Item implements IWearable, DyeableLeatherItem
{

    public static Map<String, EnumWearable> wearables = Maps.newHashMap();
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
        for (final String s : BlingItem.names)
        {
            ThutBling.ITEMS.register("bling_" + s, () -> new BlingItem(s, BlingItem.wearables.get(s)));
        }
        ThutCore.FORGE_BUS.addGenericListener(ItemStack.class, BlingItem::onItemCaps);
    }

    private static void onItemCaps(final AttachCapabilitiesEvent<ItemStack> event)
    {
        if (event.getObject().getItem() instanceof BlingItem bling && bling.localInventory)
        {
            var stack = event.getObject();
            ICapabilityProvider provider = new ICapabilityProvider()
            {
                final LazyOptional<IItemHandler> holder = LazyOptional.of(() -> this.getWrapped());

                private InvWrapper wrapped = null;

                protected IItemHandler getWrapped()
                {
                    if (wrapped != null) return wrapped;
                    UUID id = UUID.randomUUID();
                    if (!stack.hasTag()) stack.setTag(new CompoundTag());
                    final CompoundTag tag = stack.getTag();
                    if (tag.hasUUID("bag_id")) id = tag.getUUID("bag_id");
                    else tag.putUUID("bag_id", id);
                    final SmallInventory inv = SmallManager.INSTANCE.get(id);
                    wrapped = new InvWrapper(inv);
                    return wrapped;
                }

                @Override
                public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side)
                {
                    return ThutCaps.ITEM_HANDLER.orEmpty(cap, holder);
                }
            };
            event.addCapability(new ResourceLocation(ThutBling.MODID, "bag_inventory"), provider);
        }
    }

    public final String name;
    private final EnumWearable slot;
    private boolean localInventory = false;

    public BlingItem(final String name, final EnumWearable slot)
    {
        super(new Properties().tab(ThutCore.THUTITEMS).stacksTo(1));
        this.name = name;
        this.slot = slot;
        BlingItem.defaults.put(this, slot);
        BlingItem.bling.add(this);
        localInventory = name.equals("bag");
    }

    @Override
    public void initializeClient(Consumer<IItemRenderProperties> consumer)
    {
        consumer.accept(new IItemRenderProperties()
        {
            private final BlockEntityWithoutLevelRenderer renderer = BlingitemRenderer.INSTANCE;

            @Override
            public BlockEntityWithoutLevelRenderer getItemStackRenderer()
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
}
