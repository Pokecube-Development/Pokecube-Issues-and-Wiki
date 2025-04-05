package pokecube.adventures.capabilities;

import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import pokecube.api.entity.trainers.TrainerCaps;

public class InitCaps
{
    public static void registerAttachment(DeferredRegister<AttachmentType<?>> registry)
    {
        TrainerCaps.AISTATES = registry.register("trainer_ai_state",
                () -> AttachmentType.serializable(CapabilityNPCAIStates::make).build());
        TrainerCaps.TRAINER = registry.register("trainer_pokemobs",
                () -> AttachmentType.serializable(CapabilityHasPokemobs::make).build());
        TrainerCaps.MESSAGES = registry.register("trainer_messages",
                () -> AttachmentType.serializable(CapabilityNPCMessages::make).build());
        TrainerCaps.REWARDS = registry.register("trainer_rewards",
                () -> AttachmentType.serializable(CapabilityHasRewards::make).build());
        TrainerCaps.TRADES = registry.register("trainer_trades",
                () -> AttachmentType.serializable(CapabilityHasTrades::make).build());
    }
}
