package thut.core.common.mobs;

import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import thut.api.entity.IMobColourable;

public class DefaultColourable implements IMobColourable
{

    int[] RGBA   = { 255, 255, 255, 255 };
    int   colour = 0;

    @Override
    public int getDyeColour()
    {
        return this.colour;
    }

    @Override
    public int[] getRGBA()
    {
        return this.RGBA;
    }

    @Override
    public void setDyeColour(final int colour)
    {
        this.colour = colour;
    }

    @Override
    public void setRGBA(final int... colours)
    {
        assert this.RGBA.length == colours.length;
        this.RGBA = colours;
    }

    public static IMobColourable makeProvider(final IAttachmentHolder in)
    {
        return new DefaultColourable();
    }

    public static IMobColourable get(final IAttachmentHolder in)
    {
        if (in.hasData(TYPE_SAVE.get())) return in.getData(TYPE_SAVE.get());
        return null;
    }
    
    public static final ResourceLocation LOCSAVEABLE = ResourceLocation.parse("thutcore:colourables");

    public static Supplier<AttachmentType<IMobColourable>> TYPE_SAVE;
    
    public static void registerAttachment(DeferredRegister<AttachmentType<?>> registry)
    {
        Function<IAttachmentHolder, IMobColourable> func_a = DefaultColourable::makeProvider;
        var attach_a = AttachmentType.serializable(func_a).build();
        TYPE_SAVE = registry.register(LOCSAVEABLE.getPath(), () -> attach_a);
    }
}
