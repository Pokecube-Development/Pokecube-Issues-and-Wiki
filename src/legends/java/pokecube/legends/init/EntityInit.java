package pokecube.legends.init;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import pokecube.adventures.utils.EnergyHandler;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.entity.WormholeEntity;
import thut.core.common.ThutCore;

import java.util.function.Supplier;

public class EntityInit
{
    public static final Supplier<EntityType<WormholeEntity>> WORMHOLE = PokecubeLegends.ENTITIES.register("wormhole",
            () -> EntityType.Builder.of(WormholeEntity::new, MobCategory.CREATURE).sized(2, 2));

    public static void init()
    {
        ThutCore.FORGE_BUS.addGenericListener(Entity.class, EntityInit::onEntityCapabilityAttach);
    }

    public static void onEntityCapabilityAttach(final AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getObject() instanceof WormholeEntity wormhole)
        {
            wormhole.energy = new WormholeEntity.EnergyStore();
            event.addCapability(EnergyHandler.ENERGYCAP, wormhole.energy);
        }
    }
}
