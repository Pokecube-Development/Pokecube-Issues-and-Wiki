package thut.core.init;

import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.RegistryObject;
import thut.api.inventory.npc.NpcContainer;
import thut.api.particle.ParticleNoGravity;
import thut.api.particle.ParticleOrientable;
import thut.api.particle.ThutParticles;
import thut.core.common.ThutCore;

public class RegistryObjects
{
    public static final RegistryObject<MenuType<NpcContainer>> NPC_MENU;

    public static final RegistryObject<ParticleNoGravity> STRING;
    public static final RegistryObject<ParticleNoGravity> AURORA;
    public static final RegistryObject<ParticleNoGravity> MISC;
    public static final RegistryObject<ParticleNoGravity> POWDER;
    public static final RegistryObject<ParticleOrientable> LEAF;

    static
    {
        NPC_MENU = ThutCore.RegistryEvents.MENUS.register("npc",
                () -> new MenuType<>((IContainerFactory<NpcContainer>) NpcContainer::new, FeatureFlagSet.of()));

        AURORA = ThutCore.RegistryEvents.PARTICLES.register("aurora", () -> ThutParticles.AURORA);
        LEAF = ThutCore.RegistryEvents.PARTICLES.register("leaf", () -> ThutParticles.LEAF);
        MISC = ThutCore.RegistryEvents.PARTICLES.register("misc", () -> ThutParticles.MISC);
        STRING = ThutCore.RegistryEvents.PARTICLES.register("string", () -> ThutParticles.STRING);
        POWDER = ThutCore.RegistryEvents.PARTICLES.register("powder", () -> ThutParticles.POWDER);

    }

    public static void init()
    {}
}
