package pokecube.gimmicks.mega;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.buffer.ByteBuf;
import net.minecraft.ResourceLocationException;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import pokecube.api.data.PokedexEntry;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import thut.bling.ThutBling;
import thut.lib.TCodecs;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class MegaCapability implements IMegaCapability
{
    public static class MegaStone
    {
        private String entry;
        private int[] colours;

        public MegaStone()
        {
            // Exposed for json use. it is used by the codec.
        }

        public int[] colours() {return colours;}

        public String entry() {return entry;}

        public PokedexEntry getEntry()
        {
            return Database.getEntry(entry);
        }

        public static final Codec<MegaStone> CODEC = TCodecs.jsonCodec(MegaStone.class);
        public static final StreamCodec<ByteBuf, MegaStone> STREAM_CODEC = TCodecs.jsonStreamCodec(MegaStone.class);

        @Override
        public int hashCode()
        {
            return entry.hashCode() ^ Arrays.hashCode(colours);
        }

        @Override
        public boolean equals(Object o)
        {
            if (!(o instanceof MegaStone megaStone)) return false;
            return Objects.equals(entry, megaStone.entry) && Objects.deepEquals(colours, megaStone.colours);
        }
    }

    public static record MegaWearable(IMegaCapability details, ResourceLocation key)
    {
        public MegaWearable(ResourceLocation key)
        {
            this(null, key);
        }

        public MegaWearable withItem(ItemStack stack)
        {
            return new MegaWearable(REGISTRY.get(key).apply(stack), key);
        }

        public static final Codec<MegaWearable> CODEC = ResourceLocation.CODEC.comapFlatMap(MegaWearable::read,
                MegaWearable::key).stable();

        public static final StreamCodec<ByteBuf, MegaWearable> STREAM_CODEC = ResourceLocation.STREAM_CODEC.map(
                MegaWearable::parse, MegaWearable::key);

        public static DataResult<MegaWearable> read(ResourceLocation tag)
        {
            try
            {
                return DataResult.success(parse(tag));
            }
            catch (ResourceLocationException resourcelocationexception)
            {
                return DataResult.error(
                        () -> "Not a valid pokemob tag: " + tag + " " + resourcelocationexception.getMessage());
            }
        }

        public static MegaWearable parse(ResourceLocation location)
        {
            return new MegaWearable(location);
        }
    }

    private static final Map<ResourceLocation, Function<ItemStack, IMegaCapability>> REGISTRY = new HashMap<>();

    public static void RegisterMegaType(ResourceLocation key, Function<ItemStack, IMegaCapability> provider)
    {
        REGISTRY.put(key, provider);
    }

    public static boolean matches(final ItemStack stack, final PokedexEntry entry)
    {
        return stack.has(MegaEvolveHelper.MEGA_WEARABLE) && stack.get(MegaEvolveHelper.MEGA_WEARABLE).withItem(stack)
                .details().isValid(stack, entry);
    }

    final ItemStack stack;

    public MegaCapability(final ItemStack itemStack)
    {
        this.stack = itemStack;
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
        return stack.has(MegaEvolveHelper.MEGA_STONE);
    }

    @Override
    public boolean isValid(final ItemStack stack, final PokedexEntry entry)
    {
        if (stack.getItem() instanceof IMegaCapability cap) return cap.isValid(stack, entry);
        final PokedexEntry stacks = this.getEntry(stack);

        final boolean isStone = stack.has(MegaEvolveHelper.MEGA_STONE);
        final boolean isMegaWear = stack.has(MegaEvolveHelper.MEGA_WEARABLE);
        final boolean isBling = stack.has(ThutBling.BLING_GEM_DATA);

        // Bling only works if a stone is attached, so if it is bling, check if
        // it has correct entry.
        if (isStone || isBling)
        {
            return stacks == entry;
        }
        // All normal mega wearables are valid at all times
        return isMegaWear;
    }

    protected static PokedexEntry getForStack(final ItemStack stack)
    {
        final boolean isMegaWear = stack.has(MegaEvolveHelper.MEGA_WEARABLE);
        if (isMegaWear) return Database.missingno;
        final boolean isStone = stack.has(MegaEvolveHelper.MEGA_STONE);
        if (isStone)
        {
            PokedexEntry e = stack.get(MegaEvolveHelper.MEGA_STONE).getEntry();
            if (e == null) e = Database.missingno;
            return e;
        }
        final boolean isBling = stack.has(ThutBling.BLING_GEM_DATA);
        if (isBling)
        {
            var gem = stack.get(ThutBling.BLING_GEM_DATA);
            final ItemStack stack2 = ItemStack.parseOptional(PokecubeCore.proxy.getRegistries(), gem.gemTag());
            if (!stack2.isEmpty()) return MegaCapability.getForStack(stack2);
        }
        return Database.missingno;
    }

    protected static boolean isStoneOrWearable(final ItemStack stack)
    {
        final boolean isStone = stack.has(MegaEvolveHelper.MEGA_STONE);
        final boolean isMegaWear = stack.has(MegaEvolveHelper.MEGA_WEARABLE);
        final boolean isBling = stack.has(ThutBling.BLING_GEM_DATA);
        return isStone || isMegaWear || isBling;
    }
}
