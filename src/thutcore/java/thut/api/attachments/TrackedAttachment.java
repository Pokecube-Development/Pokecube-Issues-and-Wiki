package thut.api.attachments;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;

import java.util.List;

public interface TrackedAttachment
{
    void markDirty();
    void markClean();
    boolean isDirty();
    default void preSyncClientSide(){}
    default void postSyncClientSide(){}

    public static interface ITrackedAttachmentHolder
    {
        List<Tuple<ResourceLocation, TrackedAttachment>> thutcore$getTracked();
    }
}
