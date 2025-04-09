package thut.api.attachments;

public interface TrackedAttachment
{
    void markDirty();
    void markClean();
    boolean isDirty();
}
