package thut.api.attachments;

import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.collect.Sets;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import thut.api.ThutCaps;
import thut.api.entity.IAnimated.IAnimationHolder;
import thut.api.entity.ICopyMob;
import thut.api.entity.animation.CapabilityAnimation.DefaultImpl;
import thut.core.common.ThutCore;
import thut.lib.RegHelper;

public class CopyMob
{
    public static record CopyInfo(ICopyMob copy, CompoundTag tag)
    {

        public CopyInfo(CompoundTag tag)
        {
            this(null, tag);
        }

        public CopyInfo withContext(HolderLookup.Provider context)
        {
            Impl contents = new Impl();
            contents.deserializeNBT(context, this.tag());
            contents.setCopiedNBT(this.tag());
            return new CopyInfo(contents, this.tag);
        }

        public CopyInfo saveHolder(HolderLookup.Provider context)
        {
            return new CopyInfo(copy, copy.serializeNBT(context));
        }

        public static CopyInfo copyOf(LivingEntity mob)
        {
            ICopyMob copy = new CopyMob.Impl();
            copy.setCopiedMob(mob);
            return new CopyInfo(copy, copy.serializeNBT(mob.registryAccess()));
        }

        public static CopyInfo copyOf(EntityType<?> type)
        {
            CompoundTag tag = new CompoundTag();
            tag.putString("id", RegHelper.getKey(type) + "");
            return new CopyInfo(null, tag);
        }

        public static final Codec<CopyInfo> CODEC = CompoundTag.CODEC.<CopyInfo>comapFlatMap(CopyInfo::read,
                CopyInfo::tag).stable();
        public static final StreamCodec<ByteBuf, CopyInfo> STREAM_CODEC = ByteBufCodecs.COMPOUND_TAG.map(
                CopyInfo::parse, CopyInfo::tag);

        public static DataResult<CopyInfo> read(CompoundTag tag)
        {
            try
            {
                return DataResult.success(parse(tag).withContext(ThutCore.proxy.getRegistries()));
            }
            catch (ResourceLocationException resourcelocationexception)
            {
                return DataResult.error(
                        () -> "Not a valid itemholder tag: " + tag + " " + resourcelocationexception.getMessage());
            }
        }

        public static CopyInfo parse(CompoundTag tag)
        {
            return new CopyInfo(tag);
        }
    }

    // ITEM DATA

    public static Supplier<DataComponentType<CopyInfo>> COPY_STORE;

    public static void registerItemData(DeferredRegister<DataComponentType<?>> registry)
    {
        COPY_STORE = registry.register("copy_mob",
                name -> new DataComponentType.Builder<CopyInfo>().persistent(CopyInfo.CODEC)
                        .networkSynchronized(CopyInfo.STREAM_CODEC).build());
    }

    // ENTITY/TILE ENTITY ATTACHMENT

    public static class Impl implements ICopyMob, TrackedAttachment
    {
        public ResourceLocation copiedID = null;
        public LivingEntity copiedMob = null;
        public CompoundTag copiedNBT = new CompoundTag();

        @Override
        public ResourceLocation getCopiedID()
        {
            return this.copiedID;
        }

        @Override
        public LivingEntity getCopiedMob()
        {
            return this.copiedMob;
        }

        @Override
        public CompoundTag getCopiedNBT()
        {
            return this.copiedNBT;
        }

        @Override
        public void setCopiedID(final ResourceLocation id)
        {
            this.copiedID = id;
            this.markDirty();
        }

        @Override
        public void setCopiedMob(final LivingEntity mob)
        {
            this.copiedMob = mob;
            if (mob != null)
            {
                this.setCopiedID(RegHelper.getKey(mob));
                CompoundTag ret = new CompoundTag();
                String id = mob.getEncodeId();
                if (id != null)
                {
                    ret.putString("id", id);
                }
                this.setCopiedNBT(mob.saveWithoutId(ret));
            }
            else
            {
                this.setCopiedID(null);
            }
            this.markDirty();
        }

        @Override
        public void setCopiedNBT(final CompoundTag tag)
        {
            this.copiedNBT = tag;
            this.markDirty();
        }

        private boolean isDirty = false;

        @Override
        public void markDirty()
        {
            this.isDirty = true;
        }

        @Override
        public void markClean()
        {
            this.isDirty = false;
        }

        @Override
        public boolean isDirty()
        {
            return isDirty;
        }
    }

    public static final ResourceLocation LOC = ResourceLocation.parse("thutcore:copymob");
    public static final ResourceLocation ANIM = ResourceLocation.parse("thutcore:animations");

    public static Supplier<AttachmentType<ICopyMob>> TYPE_COPY;
    public static Supplier<AttachmentType<IAnimationHolder>> TYPE_ANIM;

    private static final Set<ResourceLocation> ATTACH_TO = Sets.newHashSet();

    public static ICopyMob getCopyHolder(final IAttachmentHolder in)
    {
        if (!(in instanceof Entity e)) return null;
        if (!CopyMob.ATTACH_TO.contains(RegHelper.getKey(e.getType()))) return null;
        return in.getData(TYPE_COPY.get());
    }

    public static IAnimationHolder getAnimHolder(final IAttachmentHolder in)
    {
        return in.getData(TYPE_ANIM.get());
    }

    public static void registerAttachment(DeferredRegister<AttachmentType<?>> registry)
    {
        Function<IAttachmentHolder, ICopyMob> func_a = holder -> new Impl();
        var attach_a = AttachmentType.serializable(func_a).copyOnDeath().build();
        TYPE_COPY = registry.register(LOC.getPath(), () -> attach_a);

        Function<IAttachmentHolder, IAnimationHolder> func_b = holder -> new DefaultImpl();
        var attach_b = AttachmentType.serializable(func_b).copyOnDeath().build();
        TYPE_ANIM = registry.register(ANIM.getPath(), () -> attach_b);
    }

    private static void onEntitySizeSet(final EntityEvent.Size event)
    {
        final ICopyMob copyMob = ThutCaps.getCopyMob(event.getEntity());
        if (copyMob == null || copyMob.getCopiedMob() == null) return;
        final LivingEntity copied = copyMob.getCopiedMob();
        final Pose pose = event.getEntity().getPose();
        final EntityDimensions dims = copied.getDimensions(pose);
        final float height = dims.height();
        final float width = dims.width();
        final float eye = copied.getEyeHeight(pose);
        event.setNewSize(EntityDimensions.fixed(width, height).withEyeHeight(eye));
    }

    private static void onLivingUpdate(final EntityTickEvent.Post event)
    {
        final ICopyMob copyMob = ThutCaps.getCopyMob(event.getEntity());
        if (copyMob == null) return;
        if (event.getEntity() instanceof LivingEntity entity) copyMob.onBaseTick(event.getEntity().level(), entity);
    }

    public static void setup()
    {
        ThutCore.FORGE_BUS.addListener(CopyMob::onEntitySizeSet);
        ThutCore.FORGE_BUS.addListener(CopyMob::onLivingUpdate);

        // Lets make this one default.
        CopyMob.register(EntityType.PLAYER);
    }

    public static void register(final EntityType<?> type)
    {
        synchronized (CopyMob.ATTACH_TO)
        {
            CopyMob.ATTACH_TO.add(RegHelper.getKey(type));
        }
    }
}
