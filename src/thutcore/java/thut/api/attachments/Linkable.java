package thut.api.attachments;

import java.util.Locale;
import java.util.UUID;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jetbrains.annotations.UnknownNullability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import io.netty.buffer.ByteBuf;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import thut.api.ThutCaps;
import thut.api.data.HolderProvider;
import thut.core.common.ThutCore;

public class Linkable
{
    public static record LinkHolder(ILinkStorage link, CompoundTag tag)
    {
        public LinkHolder(CompoundTag tag)
        {
            this(null, tag);
        }

        public LinkHolder withContext(HolderLookup.Provider context)
        {
            if (this.tag.isEmpty()) return this;
            LinkStorage contents = new LinkStorage();
            contents.deserializeNBT(context, this.tag());
            return new LinkHolder(contents, this.tag);
        }

        public LinkHolder saveHolder(HolderLookup.Provider context)
        {
            return new LinkHolder(link, link.serializeNBT(context));
        }

        public static final Codec<LinkHolder> CODEC = CompoundTag.CODEC
                .<LinkHolder>comapFlatMap(LinkHolder::read, LinkHolder::tag).stable();
        public static final StreamCodec<ByteBuf, LinkHolder> STREAM_CODEC = ByteBufCodecs.COMPOUND_TAG
                .map(LinkHolder::parse, LinkHolder::tag);

        public static DataResult<LinkHolder> read(CompoundTag tag)
        {
            try
            {
                return DataResult.success(parse(tag));
            }
            catch (ResourceLocationException resourcelocationexception)
            {
                return DataResult.error(() -> "Not a valid linkable storage tag: " + tag + " "
                        + resourcelocationexception.getMessage());
            }
        }

        public static LinkHolder parse(CompoundTag tag)
        {
            return new LinkHolder(tag);
        }
    }

    public static interface ILinkStorage extends INBTSerializable<CompoundTag>
    {
        /**
         * This gets the UUID for a mob, if this is null, then it does not have
         * a linked mob
         */
        @Nullable
        UUID getLinkedMob(Entity user);

        /**
         * This gets the UUID for a pos, if this is null, then it does not have
         * a linked pos
         */
        @Nullable
        GlobalPos getLinkedPos(Entity user);

        /**
         * This will set the linked mob, returns whether this setting actually
         * occured.
         */
        boolean setLinkedMob(@Nullable UUID mobid, @Nullable Entity user);

        /**
         * This will set the linked pos, returns whether this setting actually
         * occured.
         */
        boolean setLinkedPos(@Nullable GlobalPos pos, @Nullable Entity user);

        @Override
        default @UnknownNullability CompoundTag serializeNBT(Provider provider)
        {
            CompoundTag nbt = new CompoundTag();
            var pos = getLinkedPos(null);
            if (pos != null) nbt.put("pos", GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, pos).getOrThrow());
            var id = this.getLinkedMob(null);
            if (id != null) nbt.put("id", NbtUtils.createUUID(id));
            return nbt;
        }

        @Override
        default void deserializeNBT(Provider provider, CompoundTag nbt)
        {
            if (nbt.hasUUID("id"))
            {
                this.setLinkedMob(nbt.getUUID("id"), null);
            }
            if (nbt.contains("pos"))
            {
                var pos = GlobalPos.CODEC.decode(NbtOps.INSTANCE, nbt.get("pos")).result().get().getFirst();
                this.setLinkedPos(pos, null);
            }
        }

    }

    public static interface ILinkable extends INBTSerializable<CompoundTag>
    {
        /**
         * @param link - who to link to
         * @return whether the link connected.
         */
        boolean link(ILinkStorage link, @Nullable Entity user);

        /**
         * This will return an ILinkStorage which will link to this ILinkable.
         */
        @Nonnull
        ILinkStorage getLink(@Nullable Entity user);

        @Override
        default @UnknownNullability CompoundTag serializeNBT(Provider provider)
        {
            CompoundTag nbt = new CompoundTag();
            if (this.getLink(null) != null) nbt.put("link", nbt);
            return nbt;
        }

        @Override
        default void deserializeNBT(Provider provider, CompoundTag nbt)
        {
            if (nbt.contains("link"))
            {
                var link = new LinkStorage();
                link.deserializeNBT(provider, nbt.getCompound("link"));
                this.link(link, null);
            }
        }
    }

    public static class LinkStorage implements ILinkStorage
    {
        UUID uuid;
        GlobalPos pos;

        @Override
        public UUID getLinkedMob(final Entity user)
        {
            return this.uuid;
        }

        @Override
        public GlobalPos getLinkedPos(final Entity user)
        {
            return this.pos;
        }

        @Override
        public boolean setLinkedMob(final UUID mobid, final Entity user)
        {
            this.uuid = mobid;
            return true;
        }

        @Override
        public boolean setLinkedPos(final GlobalPos pos, final Entity user)
        {
            this.pos = pos;
            return true;
        }

    }

    public static class LinkableImpl implements ILinkable
    {
        LinkStorage store = new LinkStorage();

        @Override
        public boolean link(final ILinkStorage link, final Entity user)
        {
            this.store.setLinkedMob(link.getLinkedMob(user), user);
            this.store.setLinkedPos(link.getLinkedPos(user), user);
            return true;
        }

        @Override
        public ILinkStorage getLink(final Entity user)
        {
            return this.store;
        }
    }

    public static void setup()
    {
        ThutCore.FORGE_BUS.addListener(Linkable::linkBlock);
    }

    private static void linkBlock(final RightClickBlock event)
    {
        // Only run for items
        if (event.getItemStack().isEmpty()) return;
        // Check if stack is a linkstore
        ILinkStorage storage = null;
        var holder = event.getItemStack().get(LINK_STORE);
        if (holder == null) return;
        if (holder.link() == null)
        {
            event.getItemStack().set(LINK_STORE, holder = holder.withContext(event.getEntity().registryAccess()));
        }
        storage = holder.link();
        final BlockEntity tile = event.getLevel().getBlockEntity(event.getPos());
        ILinkable linkable = ThutCaps.getLinkable(tile, event.getFace());
        // Only run for tile entities
        if (linkable != null)
        {
            // Only run for linkable ones
            linkable.link(storage, event.getEntity());
            event.setCanceled(true);
            event.setUseBlock(TriState.FALSE);
            event.setUseItem(TriState.FALSE);
        }
        // Otherwise try to save the location instead
        else
        {
            final GlobalPos pos = GlobalPos.of(event.getEntity().level().dimension(), event.getPos());
            storage.setLinkedPos(pos, event.getEntity());
            event.setCanceled(true);
            event.setUseBlock(TriState.FALSE);
            event.setUseItem(TriState.FALSE);
            event.getItemStack().set(LINK_STORE, holder.saveHolder(event.getEntity().registryAccess()));
        }
    }

    // ITEM DATA

    public static Supplier<DataComponentType<LinkHolder>> LINK_STORE;

    public static void registerItemData(DeferredRegister<DataComponentType<?>> registry)
    {
        LINK_STORE = registry.register("link_storage", name -> new DataComponentType.Builder<LinkHolder>()
                .persistent(LinkHolder.CODEC).networkSynchronized(LinkHolder.STREAM_CODEC).build());
    }

    // ENTITY/TILE ENTITY ATTACHMENT

    @SuppressWarnings("unchecked")
    public static Supplier<AttachmentType<ILinkable>>[] TYPES = (Supplier<AttachmentType<ILinkable>>[]) new Supplier<?>[6];

    @SuppressWarnings("unchecked")
    public static final HolderProvider<ILinkable>[] REGISTRY = (HolderProvider<ILinkable>[]) new HolderProvider<?>[6];

    public static final HolderProvider<ILinkable> DEFAULT()
    {
        return REGISTRY[0];
    }

    public static ILinkable get(final IAttachmentHolder in, Direction d)
    {
        if (d == null) d = Direction.DOWN;
        var TYPE = TYPES[d.ordinal()].get();
        return in.getData(TYPE);
    }

    public static void registerAttachment(DeferredRegister<AttachmentType<?>> registry)
    {
        for (Direction d : Direction.values())
        {
            var prov = new HolderProvider<Linkable.ILinkable>();
            REGISTRY[d.ordinal()] = prov;
            var KEY = "linkable_" + d.getName().toLowerCase(Locale.ROOT);

            var type = registry.register(KEY, () -> AttachmentType.serializable(prov::make).build());
            TYPES[d.ordinal()] = type;

            var ID = ResourceLocation.fromNamespaceAndPath("thutcore", KEY);
            prov.register(new HolderProvider.Provider<Linkable.ILinkable>()
            {
                @Override
                public Linkable.ILinkable apply(IAttachmentHolder t)
                {
                    return new LinkableImpl();
                }

                @Override
                protected ResourceLocation key()
                {
                    return ID;
                }
            });
        }
    }
}
