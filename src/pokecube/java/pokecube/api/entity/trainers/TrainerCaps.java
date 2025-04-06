package pokecube.api.entity.trainers;

import java.util.function.Supplier;

import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;

public class TrainerCaps
{
    public static Supplier<AttachmentType<IHasNPCAIStates>> AISTATES;
    public static Supplier<AttachmentType<IHasPokemobs>> TRAINER;
    public static Supplier<AttachmentType<IHasMessages>> MESSAGES;
    public static Supplier<AttachmentType<IHasRewards>> REWARDS;
    public static Supplier<AttachmentType<IHasTrades>> TRADES;

    public static IHasPokemobs getHasPokemobs(final IAttachmentHolder entityIn)
    {
        if (entityIn == null) return null;
        return entityIn.hasData(TRAINER) ? entityIn.getData(TRAINER) : null;
    }

    public static IHasRewards getHasRewards(final IAttachmentHolder entityIn)
    {
        if (entityIn == null) return null;
        return entityIn.hasData(REWARDS) ? entityIn.getData(REWARDS) : null;
    }

    public static IHasTrades getHasTrades(final IAttachmentHolder entityIn)
    {
        if (entityIn == null) return null;
        return entityIn.hasData(TRADES) ? entityIn.getData(TRADES) : null;
    }

    public static IHasMessages getMessages(final IAttachmentHolder entityIn)
    {
        if (entityIn == null) return null;
        return entityIn.hasData(MESSAGES) ? entityIn.getData(MESSAGES) : null;
    }

    public static IHasNPCAIStates getNPCAIStates(final IAttachmentHolder entityIn)
    {
        if (entityIn == null) return null;
        return entityIn.hasData(AISTATES) ? entityIn.getData(AISTATES) : null;
    }
}
