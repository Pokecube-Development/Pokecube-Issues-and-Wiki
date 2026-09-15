package thut.mixin.entity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.neoforged.neoforge.attachment.AttachmentHolder;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import thut.api.attachments.TrackedAttachment;
import thut.api.attachments.TrackedAttachment.ITrackedAttachmentHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mixin(AttachmentHolder.class)
public abstract class EntityAttachments implements ITrackedAttachmentHolder
{
    @Shadow
    abstract Map<AttachmentType<?>, Object> getAttachmentMap();

    @Unique
    List<Tuple<ResourceLocation, TrackedAttachment>> thutcore$trackedAttachments = new ArrayList<>();

    @Override
    public List<Tuple<ResourceLocation, TrackedAttachment>> thutcore$getTracked()
    {
        var map = this.getAttachmentMap();
        if (map.size() != thutcore$trackedAttachments.size())
        {
            thutcore$trackedAttachments = new ArrayList<>();
            for (var entry : map.entrySet())
            {
                var key = NeoForgeRegistries.ATTACHMENT_TYPES.getKey(entry.getKey());
                var value = entry.getValue();
                if (value instanceof TrackedAttachment tracked)
                    thutcore$trackedAttachments.add(new Tuple<>(key, tracked));
            }
        }
        return thutcore$trackedAttachments;
    }
}
