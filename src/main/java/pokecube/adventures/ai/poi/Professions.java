package pokecube.adventures.ai.poi;

import com.google.common.collect.ImmutableSet;

import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraftforge.event.RegistryEvent.Register;
import pokecube.core.entity.npc.NpcType;

public class Professions
{
    public static VillagerProfession HEALER;
    public static VillagerProfession PROFESSOR;
    public static VillagerProfession MERCHANT;

    public static void register(final Register<VillagerProfession> event)
    {
        Professions.HEALER = new VillagerProfession("pokecube_adventures:healer",
                pokecube.core.ai.poi.PointsOfInterest.HEALER.get(), ImmutableSet.of(), ImmutableSet.of(), null)
                        .setRegistryName("pokecube_adventures:healer");

        Professions.PROFESSOR = new VillagerProfession("pokecube_adventures:professor", PointsOfInterest.GENELAB.get(),
                ImmutableSet.of(), ImmutableSet.of(), null).setRegistryName("pokecube_adventures:professor");
        Professions.MERCHANT = new VillagerProfession("pokecube_adventures:trader", PointsOfInterest.TRADER.get(),
                ImmutableSet.of(), ImmutableSet.of(), null).setRegistryName("pokecube_adventures:trader");

        event.getRegistry().register(HEALER);
        event.getRegistry().register(PROFESSOR);
        event.getRegistry().register(MERCHANT);

        NpcType.byType("healer").setProfession(Professions.HEALER);
        NpcType.byType("professor").setProfession(Professions.PROFESSOR);
        NpcType.byType("trader").setProfession(Professions.MERCHANT);
    }
}
