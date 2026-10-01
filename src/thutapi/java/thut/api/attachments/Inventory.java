package thut.api.attachments;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.buffer.ByteBuf;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import thut.api.data.HolderProvider;
import thut.api.inventory.InvHelper.ItemCap;

import java.util.function.Supplier;

public class Inventory
{
    public static record ItemsHolder(ItemCap items, CompoundTag tag)
    {
        public ItemsHolder(CompoundTag tag)
        {
            this(null, tag);
        }

        public ItemsHolder withContext(HolderLookup.Provider context)
        {
            ItemCap contents = new ItemCap(0, 0);
            contents.deserializeNBT(context, this.tag());
            return new ItemsHolder(contents, this.tag);
        }

        public static final Codec<ItemsHolder> CODEC = CompoundTag.CODEC.comapFlatMap(ItemsHolder::read,
                ItemsHolder::tag).stable();
        public static final StreamCodec<ByteBuf, ItemsHolder> STREAM_CODEC = ByteBufCodecs.COMPOUND_TAG.map(
                ItemsHolder::parse, ItemsHolder::tag);

        public static DataResult<ItemsHolder> read(CompoundTag tag)
        {
            try
            {
                return DataResult.success(parse(tag));
            }
            catch (ResourceLocationException resourcelocationexception)
            {
                return DataResult.error(
                        () -> "Not a valid itemholder tag: " + tag + " " + resourcelocationexception.getMessage());
            }
        }

        public static ItemsHolder parse(CompoundTag tag)
        {
            return new ItemsHolder(tag);
        }
    }

    // ITEM DATA

    public static Supplier<DataComponentType<ItemsHolder>> INVENTORY_STORE;

    public static void registerItemData(DeferredRegister<DataComponentType<?>> registry)
    {
        INVENTORY_STORE = registry.register("item_storage",
                name -> new DataComponentType.Builder<ItemsHolder>().persistent(ItemsHolder.CODEC)
                        .networkSynchronized(ItemsHolder.STREAM_CODEC).build());
    }

    // ENTITY/TILE ENTITY ATTACHMENT

    public static Supplier<AttachmentType<ItemCap>> TYPE;

    public static final HolderProvider<ItemCap> REGISTRY = new HolderProvider<>(
            ResourceLocation.parse("thutcore:inventory"));

    public static boolean has(final IAttachmentHolder in)
    {
        return in.hasData(TYPE);
    }

    public static ItemCap get(final IAttachmentHolder in)
    {
        return in.getData(TYPE);
    }

    public static void registerAttachment(DeferredRegister<AttachmentType<?>> registry)
    {
        TYPE = registry.register("inventory", () -> AttachmentType.serializable(REGISTRY::make).build());
    }

}
