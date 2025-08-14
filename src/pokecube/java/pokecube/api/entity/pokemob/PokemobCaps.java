package pokecube.api.entity.pokemob;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.buffer.ByteBuf;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import pokecube.api.PokecubeAPI;
import pokecube.api.ai.IInhabitor;
import pokecube.api.blocks.IInhabitable.IHabitat;
import pokecube.api.entity.CapabilityAffected;
import pokecube.api.entity.CapabilityInhabitable;
import pokecube.api.entity.CapabilityInhabitor;
import pokecube.api.entity.IOngoingAffected;
import pokecube.api.items.EggInfo;
import pokecube.api.items.IPokemobUseable;
import pokecube.api.items.PokecubeContents;
import pokecube.api.items.PokesealContents;
import pokecube.core.utils.EntityTools;
import thut.api.attachments.Breedable;
import thut.api.data.HolderProvider;
import thut.api.entity.IBreedingMob;
import thut.core.common.genetics.DefaultGenetics;
import thut.core.common.network.SyncAttachments;

import java.util.function.Supplier;

public class PokemobCaps
{
    public static Supplier<AttachmentType<IOngoingAffected>> ONGOING_AFFECTED;
    public static Supplier<AttachmentType<IPokemob>> POKEMOB;
    public static Supplier<AttachmentType<IHabitat>> INHABITABLE;
    public static Supplier<AttachmentType<IInhabitor>> INHABITOR;

    public static Supplier<DataComponentType<UsableItem>> USABLE_DATA;
    public static Supplier<DataComponentType<PokecubeContents>> POKECUBE_DATA;
    public static Supplier<DataComponentType<EggInfo>> POKEEGG_DATA;
    public static Supplier<DataComponentType<PokesealContents>> POKESEAL_DATA;

    public static IHabitat getHabitatFor(IAttachmentHolder entityIn)
    {
        if (entityIn == null) return null;
        if (entityIn.hasData(INHABITABLE)) return entityIn.getData(INHABITABLE);
        return null;
    }

    public static IInhabitor getInhabitorFor(IAttachmentHolder entityIn)
    {
        if (entityIn == null) return null;
        if (entityIn.hasData(INHABITOR) || CapabilityInhabitor._REGISTRY.make(entityIn) != null)
        {
            return entityIn.getData(INHABITOR);
        }
        return null;
    }

    public static IPokemob getPokemobFor(IAttachmentHolder entityIn)
    {
        if (entityIn == null) return null;
        if (entityIn instanceof Entity entity) entityIn = EntityTools.getCoreEntity(entity);
        if (entityIn.hasData(POKEMOB)) return entityIn.getData(POKEMOB);
        return null;
    }

    public static IOngoingAffected getAffected(final IAttachmentHolder in)
    {
        if (in == null) return null;
        return in.getData(ONGOING_AFFECTED);
    }

    public static EggInfo getEggContents(final ItemStack in)
    {
        return in.get(POKEEGG_DATA);
    }

    public static PokesealContents getPokeseal(final ItemStack in)
    {
        return in.get(POKESEAL_DATA);
    }

    public static void updatePokeseal(ItemStack in, PokesealContents contents)
    {
        in.set(POKESEAL_DATA, contents);
    }

    public static void removePokeseal(ItemStack in)
    {
        in.remove(POKESEAL_DATA);
    }

    public static IPokemobUseable getPokemobUsable(final ItemStack in)
    {
        if (in == null) return null;
        if (in.getItem() instanceof IPokemobUseable) return (IPokemobUseable) in.getItem();
        var data = in.get(USABLE_DATA);
        if (data == null) return null;
        return data.effect();
    }

    public static PokecubeContents getPokemobIn(ItemStack stack, Level level)
    {
        return getPokemobIn(stack, level, false);
    }

    public static PokecubeContents getPokemobIn(ItemStack stack, Level level, boolean forceNew)
    {
        PokecubeContents contents = stack.get(POKECUBE_DATA);
        if (contents == null) return null;
        if ((contents.pokemob() == null || forceNew) && level != null && contents.tag().contains("I"))
        {
            // We stripped the genes off the mob's tag when we saved it, so re-add them now.
            if (stack.has(DefaultGenetics.GENE_STORE))
            {
                var genesStored = stack.get(DefaultGenetics.GENE_STORE);
                contents.tag().getCompound("M").getCompound("neoforge:attachments")
                        .put("thutcore:genetics", genesStored.tag().get("P"));
            }
            LivingEntity mob = loadContents(contents, level, forceNew);
            IPokemob pokemob = getPokemobFor(mob);
            if (pokemob != null)
            {
                if (mob instanceof Mob m) pokemob.setEntity(m);
                contents = new PokecubeContents(pokemob, pokemob.getEntity(), contents.tag());
            }
            else if (mob != null) contents = new PokecubeContents(null, mob, contents.tag());
            else
            {
                var tag = contents.tag().copy();
                tag.remove("I");
                contents = new PokecubeContents(tag);
            }
            stack.set(POKECUBE_DATA, contents);
        }
        return contents;
    }

    public static PokecubeContents getPokemobIn(ItemStack stack)
    {
        return getPokemobIn(stack, null);
    }

    public static void removePokemob(ItemStack in)
    {
        in.remove(POKECUBE_DATA);
    }

    public static void updatePokecube(ItemStack in, PokecubeContents contents)
    {
        in.set(POKECUBE_DATA, contents);
    }

    public static boolean isFilled(final ItemStack stack)
    {
        return stack.has(POKECUBE_DATA);
    }

    public static LivingEntity loadContents(PokecubeContents contents, Level level, boolean forceNew)
    {
        if (!forceNew && contents.pokemob() != null && contents.pokemob().getEntity() != null)
            return contents.pokemob().getEntity();

        if (level == null)
        {
            PokecubeAPI.LOGGER.catching(new NullPointerException("World null when loadContents!"));
            return null;
        }
        CompoundTag tag = contents.tag().getCompound("M");
        if (tag.isEmpty()) return null;
        var made = EntityType.create(tag, level);
        if (made.isPresent())
        {
            var e = made.get();
            return e instanceof LivingEntity e1 ? e1 : null;
        }
        return null;
    }

    public static record UsableItem(ResourceLocation key, IPokemobUseable effect)
    {
        public UsableItem(ResourceLocation key)
        {
            this(key, null);
        }

        public UsableItem withEffect(IPokemobUseable effect)
        {
            return new UsableItem(this.key, effect);
        }

        @Override
        public String toString()
        {
            return key.toString();
        }

        public static final Codec<UsableItem> CODEC = Codec.STRING.comapFlatMap(UsableItem::read, UsableItem::toString)
                .stable();

        public static final StreamCodec<ByteBuf, UsableItem> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(
                UsableItem::parse, UsableItem::toString);

        public static DataResult<UsableItem> read(String location)
        {
            try
            {
                return DataResult.success(parse(location));
            }
            catch (ResourceLocationException resourcelocationexception)
            {
                return DataResult.error(() -> "Not a valid resource location: " + location + " "
                        + resourcelocationexception.getMessage());
            }
        }

        public static UsableItem parse(String location)
        {
            ResourceLocation loc = ResourceLocation.parse(location);
            return new UsableItem(loc);
        }
    }

    public static final HolderProvider<IPokemob> _REGISTRY = new HolderProvider<>(
            ResourceLocation.parse("pokecube:pokemob"));

    public static void registerAttachment(DeferredRegister<AttachmentType<?>> registry)
    {
        POKEMOB = registry.register("pokemob",
                () -> AttachmentType.serializable(PokemobCaps._REGISTRY::make).copyOnDeath().build());

        ONGOING_AFFECTED = registry.register("ongoing_affected",
                () -> AttachmentType.builder(CapabilityAffected::makeProvider).build());

        INHABITABLE = registry.register("habitat",
                () -> AttachmentType.serializable(CapabilityInhabitable::makeProvider).build());

        INHABITOR = registry.register("inhabitor",
                () -> AttachmentType.builder(CapabilityInhabitor._REGISTRY::make).build());

        SyncAttachments.SYNCED.add(ResourceLocation.parse("pokecube:pokemob"));
        SyncAttachments.SYNCED.add(ResourceLocation.parse("pokecube:ongoing_affected"));
        SyncAttachments.UNCHECKED_SYNC.add(ResourceLocation.parse("pokecube:pokemob"));

        Breedable._REGISTRY.register(new HolderProvider.Provider<>()
        {
            @Override
            public IBreedingMob apply(IAttachmentHolder t)
            {
                if (t.hasData(POKEMOB) && t.getData(POKEMOB) instanceof IBreedingMob breeds) return breeds;
                return null;
            }

            @Override
            protected ResourceLocation key()
            {
                return ResourceLocation.parse("pokecube:pokemob");
            }
        });
    }

    public static void registerComponents(DeferredRegister<DataComponentType<?>> registry)
    {
        USABLE_DATA = registry.register("mob_usable",
                name -> new DataComponentType.Builder<UsableItem>().persistent(UsableItem.CODEC)
                        .networkSynchronized(UsableItem.STREAM_CODEC).build());
        POKECUBE_DATA = registry.register("pokecube",
                name -> new DataComponentType.Builder<PokecubeContents>().persistent(PokecubeContents.CODEC)
                        .networkSynchronized(PokecubeContents.STREAM_CODEC).build());
        POKEEGG_DATA = registry.register("pokeegg",
                name -> new DataComponentType.Builder<EggInfo>().persistent(EggInfo.CODEC)
                        .networkSynchronized(EggInfo.STREAM_CODEC).build());
        POKESEAL_DATA = registry.register("pokeseal",
                name -> new DataComponentType.Builder<PokesealContents>().persistent(PokesealContents.CODEC)
                        .networkSynchronized(PokesealContents.STREAM_CODEC).build());
    }
}
