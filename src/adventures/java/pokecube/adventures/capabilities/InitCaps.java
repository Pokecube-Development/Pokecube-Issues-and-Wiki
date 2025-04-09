package pokecube.adventures.capabilities;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import pokecube.adventures.capabilities.CapabilityHasPokemobs.DefaultPokemobs;
import pokecube.adventures.capabilities.CapabilityHasRewards.DefaultRewards;
import pokecube.adventures.capabilities.CapabilityNPCAIStates.DefaultAIStates;
import pokecube.adventures.capabilities.CapabilityNPCMessages.DefaultMessager;
import pokecube.adventures.capabilities.CapabilityHasTrades.DefaultTrades;
import pokecube.adventures.capabilities.player.PlayerPokemobs;
import pokecube.api.entity.trainers.IHasMessages;
import pokecube.api.entity.trainers.IHasNPCAIStates;
import pokecube.api.entity.trainers.IHasPokemobs;
import pokecube.api.entity.trainers.IHasRewards;
import pokecube.api.entity.trainers.IHasTrades;
import pokecube.api.entity.trainers.TrainerCaps;
import thut.api.data.HolderProvider;

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

        var KEY_DEFAULT = ResourceLocation.fromNamespaceAndPath("pokecube_adventures", "default");
        CapabilityHasPokemobs.registerProvider(new HolderProvider.Provider<>()
        {

            @Override
            public IHasPokemobs apply(IAttachmentHolder t)
            {
                return new DefaultPokemobs();
            }

            @Override
            protected ResourceLocation key()
            {
                return KEY_DEFAULT;
            }
        });
        CapabilityNPCAIStates.registerProvider(new HolderProvider.Provider<>()
        {

            @Override
            public IHasNPCAIStates apply(IAttachmentHolder t)
            {
                return new DefaultAIStates();
            }

            @Override
            protected ResourceLocation key()
            {
                return KEY_DEFAULT;
            }
        });
        CapabilityNPCMessages.registerProvider(new HolderProvider.Provider<>()
        {

            @Override
            public IHasMessages apply(IAttachmentHolder t)
            {
                return new DefaultMessager();
            }

            @Override
            protected ResourceLocation key()
            {
                return KEY_DEFAULT;
            }
        });
        CapabilityHasRewards.registerProvider(new HolderProvider.Provider<>()
        {

            @Override
            public IHasRewards apply(IAttachmentHolder t)
            {
                return new DefaultRewards();
            }

            @Override
            protected ResourceLocation key()
            {
                return KEY_DEFAULT;
            }
        });
        CapabilityHasTrades.registerProvider(new HolderProvider.Provider<>()
        {

            @Override
            public IHasTrades apply(IAttachmentHolder t)
            {
                return new DefaultTrades();
            }

            @Override
            protected ResourceLocation key()
            {
                return KEY_DEFAULT;
            }
        });

        var KEY_PLAYER = ResourceLocation.fromNamespaceAndPath("pokecube_adventures", "player_pokemobs");
        CapabilityHasPokemobs.registerProvider(new HolderProvider.Provider<>()
        {

            @Override
            public IHasPokemobs apply(IAttachmentHolder t)
            {
                if (t instanceof Player player) return new PlayerPokemobs(player);
                return null;
            }

            @Override
            protected ResourceLocation key()
            {
                return KEY_PLAYER;
            }

            @Override
            public int getPriority()
            {
                return 50;
            }
        });
    }
}
