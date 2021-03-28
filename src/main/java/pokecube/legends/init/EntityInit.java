package pokecube.legends.init;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraftforge.fml.RegistryObject;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.entity.WormholeEntity;

public class EntityInit
{
    public static final RegistryObject<EntityType<WormholeEntity>> WORMHOLE = PokecubeLegends.ENTITIES.register(
            "wormhole", () -> EntityType.Builder.of(WormholeEntity::new, EntityClassification.CREATURE).sized(2, 2)
                    .setCustomClientFactory((s, w) -> EntityInit.WORMHOLE.get().create(w)).build("wormhole"));

    public static void init()
    {
    }

}
