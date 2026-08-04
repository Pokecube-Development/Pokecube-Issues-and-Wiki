package pokecube.legends.init;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.entity.WormholeEntity;
import thut.api.attachments.Energy;
import thut.api.data.HolderProvider;

import java.util.function.Supplier;

public class EntityInit
{
    public static final Supplier<EntityType<WormholeEntity>> WORMHOLE = PokecubeLegends.ENTITIES.register("wormhole",
            () -> EntityType.Builder.of(WormholeEntity::new, MobCategory.CREATURE).sized(5, 5).build("wormhole"));

    public static void init(IEventBus modBus)
    {
        Energy.REGISTRY.register(new HolderProvider.Provider<>()
        {
            final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("pokecube_legends", "wormhole");

            @Override
            public EnergyStorage apply(IAttachmentHolder t)
            {
                if (t instanceof WormholeEntity)
                    return new EnergyStorage(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
                return null;
            }

            @Override
            protected ResourceLocation key()
            {
                return ID;
            }
        });
        modBus.addListener(EntityInit::AttachCaps);
    }

    private static void AttachCaps(final RegisterCapabilitiesEvent event)
    {
        event.registerEntity(Capabilities.EnergyStorage.ENTITY, WORMHOLE.get(), new ICapabilityProvider<>() {
            @Override
            public @Nullable IEnergyStorage getCapability(WormholeEntity object, Direction context)
            {
                return object.energy;
            }
        });
    }
}
