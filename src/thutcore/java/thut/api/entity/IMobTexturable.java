package thut.api.entity;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import thut.api.data.HolderProvider;
import thut.core.client.render.animation.AnimationXML.Phase;

public interface IMobTexturable
{

    public static class Defaults implements IMobTexturable
    {
        
        @Override
        public Entity getEntity()
        {
            return null;
        }

        public static IMobTexturable get(final IAttachmentHolder in)
        {
            if (in.hasData(TYPE.get())) return in.getData(TYPE.get());
            return null;
        }

        public static final HolderProvider<IMobTexturable> _REGISTRY = new HolderProvider<>();
        public static final ResourceLocation LOCSAVEABLE = ResourceLocation.parse("thutcore:mob_texture");
        public static Supplier<AttachmentType<IMobTexturable>> TYPE;
        public static void registerAttachment(DeferredRegister<AttachmentType<?>> registry)
        {
            TYPE = registry.register(LOCSAVEABLE.getPath(), () -> AttachmentType.builder(_REGISTRY::make).build());
        }
    }

    Entity getEntity();

    default String getModId()
    {
        return "minecraft";
    }

    default String getForm()
    {
        return "";
    }

    default int getRandomSeed()
    {
        return 0;
    }

    default ResourceLocation getTexture(@Nullable final String part)
    {
        if (part == null) return ResourceLocation.parse("");
        return ResourceLocation.fromNamespaceAndPath(this.getModId(), part);
    }

    default List<String> getTextureStates()
    {
        return Collections.emptyList();
    }

    default ResourceLocation preApply(final ResourceLocation in)
    {
        return in;
    }

    default void applyTexturePhase(final Phase phase)
    {
        // Most things don't care about this, pokemobs do, they use it.
    }

}
