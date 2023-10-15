package pokecube.gimmicks.mega;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import pokecube.api.data.PokedexEntry;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import thut.api.item.ItemList;
import thut.lib.RegHelper;

public class MegaCapability implements ICapabilityProvider, IMegaCapability
{
    public static final ResourceLocation MEGAWORNTAG = new ResourceLocation(PokecubeCore.MODID, "mega_wearables");
    public static final ResourceLocation MEGASTONES = new ResourceLocation(PokecubeCore.MODID, "megastones");
    public static final ResourceLocation BLINGTAG = new ResourceLocation("thut_bling", "bling");

    public static final Capability<IMegaCapability> MEGA_CAP = CapabilityManager.get(new CapabilityToken<>()
    {
    });

    public static boolean matches(final ItemStack stack, final PokedexEntry entry)
    {
        final IMegaCapability cap = stack.getCapability(MegaCapability.MEGA_CAP, null).orElse(null);
        if (cap != null) return cap.isValid(stack, entry);
        return false;
    }

    protected static void onItemCaps(final AttachCapabilitiesEvent<ItemStack> event)
    {
        if (!MegaCapability.isStoneOrWearable(event.getObject())) return;
        final ResourceLocation key = new ResourceLocation("pokecube:megawearable");
        if (event.getCapabilities().containsKey(key)) return;
        event.addCapability(key, new MegaCapability(event.getObject()));
    }

    final ItemStack stack;

    private final LazyOptional<IMegaCapability> holder;

    public MegaCapability(final ItemStack itemStack)
    {
        this.stack = itemStack;
        this.holder = LazyOptional.of(
                () -> this.stack.getItem() instanceof IMegaCapability ? (IMegaCapability) this.stack.getItem() : this);
    }

    @Override
    public <T> LazyOptional<T> getCapability(final Capability<T> capability, final Direction facing)
    {
        return MegaCapability.MEGA_CAP.orEmpty(capability, this.holder);
    }

    @Override
    public PokedexEntry getEntry(final ItemStack stack)
    {
        if (stack.getItem() instanceof IMegaCapability cap) return cap.getEntry(stack);
        return MegaCapability.getForStack(stack);
    }

    @Override
    public boolean isStone(final ItemStack stack)
    {
        if (stack.getItem() instanceof IMegaCapability cap) return cap.isStone(stack);
        return ItemList.is(MegaCapability.MEGASTONES, stack);
    }

    @Override
    public boolean isValid(final ItemStack stack, final PokedexEntry entry)
    {
        if (stack.getItem() instanceof IMegaCapability cap) return cap.isValid(stack, entry);
        final PokedexEntry stacks = this.getEntry(stack);

        final boolean isStone = ItemList.is(MegaCapability.MEGASTONES, stack);
        final boolean isMegaWear = ItemList.is(MegaCapability.MEGAWORNTAG, stack);
        final boolean isBling = ItemList.is(MegaCapability.BLINGTAG, stack);

        // Bling only works if a stone is attached, so if it is bling, check if
        // it has correct entry.
        if (isStone || isBling)
        {
            return stacks == entry;
        }
        // All normal mega wearables are valid at all times
        if (isMegaWear) return true;
        return false;
    }

    protected static PokedexEntry getForStack(final ItemStack stack)
    {
        final boolean isMegaWear = ItemList.is(MegaCapability.MEGAWORNTAG, stack);
        if (isMegaWear) return Database.missingno;
        final boolean isStone = ItemList.is(MegaCapability.MEGASTONES, stack);
        if (isStone)
        {
            PokedexEntry e;
            if (stack.hasTag() && stack.getTag().contains("mega_entry"))
                e = Database.getEntry(stack.getTag().getString("mega_entry"));
            else e = Database.getEntry(RegHelper.getKey(stack).getPath());
            if (e == null) e = Database.missingno;
            return e;
        }
        final boolean isBling = ItemList.is(MegaCapability.BLINGTAG, stack);
        if (isBling && stack.hasTag() && stack.getTag().contains("gemTag"))
        {
            final ItemStack stack2 = ItemStack.of(stack.getTagElement("gemTag"));
            if (!stack2.isEmpty()) return MegaCapability.getForStack(stack2);
        }
        return Database.missingno;
    }

    protected static boolean isStoneOrWearable(final ItemStack object)
    {
        final boolean isStone = ItemList.is(MegaCapability.MEGASTONES, object);
        final boolean isMegaWear = ItemList.is(MegaCapability.MEGAWORNTAG, object);
        final boolean isBling = ItemList.is(MegaCapability.BLINGTAG, object);
        return isStone || isMegaWear || isBling;
    }
}
