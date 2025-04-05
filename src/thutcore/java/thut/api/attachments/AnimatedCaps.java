package thut.api.attachments;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.collect.Lists;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import thut.api.entity.IAnimated;

public class AnimatedCaps
{

    public static class Impl implements IAnimated
    {
        private final List<String> anims = Lists.newArrayList();
        private final List<String> transients = Lists.newArrayList();
        private final Map<Object, Object> particles = new HashMap<>();
        private final Object context;

        public Impl(Object context)
        {
            this.context = context;
        }

        @Override
        public List<String> getChoices()
        {
            return this.anims;
        }

        @Override
        public List<String> transientAnimations()
        {
            return transients;
        }

        @Override
        public Object getContext()
        {
            return context;
        }

        @Override
        public Map<Object, Object> activeParticles()
        {
            return particles;
        }
    }
    
    public static IAnimated makeProvider(final IAttachmentHolder in)
    {
        return new Impl(in);
    }

    public static IAnimated get(final IAttachmentHolder in)
    {
        if (in.hasData(TYPE_SAVE.get())) return in.getData(TYPE_SAVE.get());
        return null;
    }
    
    public static final ResourceLocation LOCSAVEABLE = ResourceLocation.parse("thutcore:animated_mob");

    public static Supplier<AttachmentType<IAnimated>> TYPE_SAVE;
    
    public static void registerAttachment(DeferredRegister<AttachmentType<?>> registry)
    {
        Function<IAttachmentHolder, IAnimated> func_a = AnimatedCaps::makeProvider;
        var attach_a = AttachmentType.builder(func_a).build();
        TYPE_SAVE = registry.register(LOCSAVEABLE.getPath(), () -> attach_a);
    }
}
