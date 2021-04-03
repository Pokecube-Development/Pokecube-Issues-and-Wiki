package pokecube.legends.init;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.RegistryObject;
import pokecube.adventures.utils.EnergyHandler;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.entity.WormholeEntity;

public class EntityInit
{
    public static final RegistryObject<EntityType<WormholeEntity>> WORMHOLE = PokecubeLegends.ENTITIES.register(
            "wormhole", () -> EntityType.Builder.of(WormholeEntity::new, EntityClassification.CREATURE).sized(2, 2)
                    .setCustomClientFactory((s, w) -> EntityInit.WORMHOLE.get().create(w)).build("wormhole"));

    public static void init()
    {
        MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, EntityInit::onEntityCapabilityAttach);
    }

    public static void onEntityCapabilityAttach(final AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getObject() instanceof WormholeEntity)
        {
            ((WormholeEntity) event.getObject()).energy = new WormholeEntity.EnergyStore();
            event.addCapability(EnergyHandler.ENERGYCAP, ((WormholeEntity) event.getObject()).energy);
        }
    }
}
