package pokecube.adventures.ai.poi;

import com.google.common.collect.ImmutableSet;

import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraftforge.event.RegistryEvent.Register;
import pokecube.core.entity.npc.NpcType;

public class Professions
{
    public static VillagerProfession HEALER;
    public static VillagerProfession PROFESSOR;

    public static void register(final Register<VillagerProfession> event)
    {
        Professions.HEALER = VillagerProfession.register("pokecube_adventures:healer", PointsOfInterest.HEALER.get(),
                ImmutableSet.of(), ImmutableSet.of(), null);
        Professions.PROFESSOR = VillagerProfession.register("pokecube_adventures:professor", PointsOfInterest.GENELAB
                .get(), ImmutableSet.of(), ImmutableSet.of(), null);
        NpcType.byType("healer").setProfession(Professions.HEALER);
        NpcType.byType("professor").setProfession(Professions.PROFESSOR);
    }
}
