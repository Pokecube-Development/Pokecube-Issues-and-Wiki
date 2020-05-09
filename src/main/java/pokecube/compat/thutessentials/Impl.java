package pokecube.compat.thutessentials;

import java.util.Collection;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.database.SpawnBiomeMatcher;
import pokecube.core.database.SpawnBiomeMatcher.MatchResult;
import pokecube.core.database.SpawnBiomeMatcher.SpawnCheck;
import pokecube.core.database.SpawnBiomeMatcher.StructureMatcher;
import pokecube.core.events.pokemob.SpawnCheckEvent;
import thut.essentials.util.world.IHasStructures;
import thut.essentials.util.world.WorldStructures;

public class Impl
{
    private static class StructChecker implements StructureMatcher
    {
        @Override
        public MatchResult structuresMatch(final SpawnBiomeMatcher matcher, final SpawnCheck checker)
        {
            if (matcher._validStructures.isEmpty()) return MatchResult.PASS;
            if (checker.world instanceof ServerWorld)
            {
                final LazyOptional<IHasStructures> opt = ((ServerWorld) checker.world).getCapability(
                        WorldStructures.CAPABILITY);
                if (opt.isPresent())
                {
                    final IHasStructures holder = opt.orElseGet(null);
                    final Collection<ResourceLocation> hits = holder.getStructures(checker.location.getPos());
                    for (final ResourceLocation hit : hits)
                        if (matcher._validStructures.contains(hit.toString())) return MatchResult.SUCCEED;
                }
            }
            return MatchResult.FAIL;
        }
    }

    public static void register()
    {
        PokecubeCore.LOGGER.debug("Registering ThutEssentials Support");
        MinecraftForge.EVENT_BUS.register(Impl.class);
    }

    @SubscribeEvent
    public static void initMatcher(final SpawnCheckEvent.Init event)
    {
        event.matcher._structs = StructureMatcher.or(new StructChecker(), event.matcher._structs);
    }

}
