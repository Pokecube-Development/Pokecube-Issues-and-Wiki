package pokecube.gimmicks.builders;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.ai.AIRoutine;
import pokecube.api.events.pokemobs.InitAIEvent;
import pokecube.api.events.pokemobs.InitAIEvent.Init.Type;
import pokecube.core.PokecubeCore;
import pokecube.gimmicks.builders.tasks.DoBuild;
import pokecube.gimmicks.builders.tasks.ManageBuild;

/**
 * This class handles mobs building structures based on instructions in a book
 * in their offhand slot
 *
 */
@EventBusSubscriber(modid = PokecubeCore.MODID)
public class BuilderTasks
{
    public static AIRoutine BUILD = new AIRoutine("BUILD", true, e -> true);

    public static BuilderConfig config = BuilderConfig.loadConfig();

    /**
     * Setup and register stuff.
     */
    @SubscribeEvent
    public static void init(FMLLoadCompleteEvent event)
    {
        PokecubeAPI.POKEMOB_BUS.addListener(BuilderTasks::onAIInit);

        List<String> keys = new ArrayList<>();
        BuiltInRegistries.BLOCK_ENTITY_TYPE.keySet().forEach(key -> keys.add(key.toString()));
        keys.sort(null);
        config.known_ids = keys;
        BuilderConfig.saveConfig(config);
    }

    private static void onAIInit(InitAIEvent.Init event)
    {
        if (event.type != Type.UTILITY) return;
        // Add manager for building
        event.add(new ManageBuild());
        // Add worker for building
        event.add(new DoBuild());
    }
}
