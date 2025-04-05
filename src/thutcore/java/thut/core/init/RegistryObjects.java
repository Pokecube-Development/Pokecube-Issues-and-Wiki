package thut.core.init;

import java.util.function.Supplier;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredHolder;
import thut.api.inventory.npc.NpcContainer;
import thut.api.particle.ParticleNoGravity;
import thut.api.particle.ParticleOrientable;
import thut.api.particle.ThutParticles;
import thut.core.common.ThutCore;

public class RegistryObjects
{
    public static final Supplier<MenuType<NpcContainer>> NPC_MENU;

    public static final Supplier<ParticleNoGravity> STRING;
    public static final Supplier<ParticleNoGravity> AURORA;
    public static final Supplier<ParticleNoGravity> MISC;
    public static final Supplier<ParticleNoGravity> POWDER;
    public static final Supplier<ParticleOrientable> LEAF;

    public static final DeferredHolder<Attribute,Attribute> MOB_SIZE_SCALE;

    static
    {
        NPC_MENU = ThutCore.RegistryEvents.MENUS.register("npc",
                () -> new MenuType<>((IContainerFactory<NpcContainer>) NpcContainer::new, FeatureFlagSet.of()));

        AURORA = ThutCore.RegistryEvents.PARTICLES.register("aurora", () -> ThutParticles.AURORA);
        LEAF = ThutCore.RegistryEvents.PARTICLES.register("leaf", () -> ThutParticles.LEAF);
        MISC = ThutCore.RegistryEvents.PARTICLES.register("misc", () -> ThutParticles.MISC);
        STRING = ThutCore.RegistryEvents.PARTICLES.register("string", () -> ThutParticles.STRING);
        POWDER = ThutCore.RegistryEvents.PARTICLES.register("powder", () -> ThutParticles.POWDER);

        MOB_SIZE_SCALE = ThutCore.RegistryEvents.ATTRIBUTES.register("thutcore.mob_size_scale",
                () -> new RangedAttribute("thutcore.mob_size_scale", 1, 1e-3, 1e3).setSyncable(true));
    }

    public static void init()
    {}
}
