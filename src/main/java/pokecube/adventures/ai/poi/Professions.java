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
        Professions.HEALER = new VillagerProfession("pokecube_adventures:healer", PointsOfInterest.HEALER, ImmutableSet
                .of(), ImmutableSet.of());
        Professions.PROFESSOR = new VillagerProfession("pokecube_adventures:professor", PointsOfInterest.GENELAB,
                ImmutableSet.of(), ImmutableSet.of());
        event.getRegistry().register(Professions.HEALER.setRegistryName("pokecube_adventures:healer"));
        event.getRegistry().register(Professions.PROFESSOR.setRegistryName("pokecube_adventures:professor"));

        NpcType.HEALER.setProfession(Professions.HEALER);
        NpcType.PROFESSOR.setProfession(Professions.PROFESSOR);
    }
}
