package pokecube.gimmicks.builders;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.ai.AIRoutine;
import pokecube.api.events.pokemobs.InitAIEvent;
import pokecube.api.events.pokemobs.InitAIEvent.Init.Type;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.tasks.utility.StoreTask;
import pokecube.gimmicks.builders.tasks.DoBuild;
import pokecube.gimmicks.builders.tasks.ManageBuild;

/**
 * This class handles mobs building structures based on instructions in a book
 * in their offhand slot
 *
 */
@EventBusSubscriber(bus = Bus.MOD, modid = PokecubeCore.MODID)
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
        var task = event.namedTasks.get(StoreTask.KEY);
        StoreTask storage = task instanceof StoreTask store ? store : null;
        if (storage == null) return;
        IPokemob pokemob = event.getPokemob();
        // Add manager for building
        event.add(new ManageBuild(pokemob, storage));
        // Add worker for building
        event.add(new DoBuild(pokemob, storage));
    }
}
