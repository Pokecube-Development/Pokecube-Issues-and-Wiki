package pokecube.core.database.moves;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.moves.Moves.Animation;
import pokecube.api.data.moves.Moves.Move;
import pokecube.api.data.moves.Moves.MoveHolder;
import pokecube.api.data.moves.Parsers;
import pokecube.api.moves.MoveEntry;
import pokecube.core.PokecubeCore;
import pokecube.core.database.resources.PackFinder;
import pokecube.core.database.tags.Tags;
import pokecube.core.moves.implementations.MovesAdder;
import thut.api.util.JsonUtil;
import thut.lib.ResourceHelper;

public class MovesDatabases
{
    public static final String DATABASES = "database/moves/";

    public static void preInitLoad()
    {
        // We need at least the moves tag for processing some things.
        Tags.MOVE.reload(new AtomicBoolean());

        final String moves_path = "database/moves/entries/";
        final String anims_path = "database/moves/animations/";

        Map<ResourceLocation, Resource> resources = PackFinder.getJsonResources(moves_path);
        Map<String, List<Move>> movesToLoad = Maps.newHashMap();
        resources.forEach((l, r) -> {
            try
            {
                final InputStreamReader reader = new InputStreamReader(ResourceHelper.getStream(r));
                Move database = JsonUtil.gson.fromJson(reader, Move.class);
                reader.close();

                movesToLoad.compute(database.name, (key, list) -> {
                    var ret = list;
                    if (ret == null) ret = Lists.newArrayList();
                    ret.add(database);
                    return ret;
                });
            }
            catch (Exception e)
            {
                PokecubeAPI.LOGGER.error("Error with move file {}", l, e);
            }
        });

        resources = PackFinder.getJsonResources(anims_path);
        Map<String, List<Animation>> animsToLoad = Maps.newHashMap();
        resources.forEach((l, r) -> {
            try
            {
                final InputStreamReader reader = new InputStreamReader(ResourceHelper.getStream(r));
                Animation database = JsonUtil.gson.fromJson(reader, Animation.class);
                reader.close();

                animsToLoad.compute(database.name, (key, list) -> {
                    var ret = list;
                    if (ret == null) ret = Lists.newArrayList();
                    ret.add(database);
                    return ret;
                });
            }
            catch (Exception e)
            {
                PokecubeAPI.LOGGER.error("Error with move animation file {}", l, e);
            }
        });

        if (PokecubeCore.getConfig().debug_data)
            PokecubeAPI.logInfo("Loaded {} moves and {} animations", movesToLoad.size(), animsToLoad.size());

        List<Move> loadedMoves = new ArrayList<>();

        movesToLoad.forEach((k, v) -> {
            v.sort(null);
            Move e = null;
            for (Move e1 : v)
            {
                if (e == null) e = e1;
                else
                {
                    e = e.mergeFrom(e1);
                }
            }
            loadedMoves.add(e);
        });
        loadedMoves.sort(null);

        Map<String, Animation> loadedAnimations = new HashMap<>();

        animsToLoad.forEach((k, v) -> {
            v.sort(null);
            Animation e = null;
            for (Animation e1 : v)
            {
                if (e == null) e = e1;
                else
                {
                    e = e.mergeFrom(e1);
                }
            }
            loadedAnimations.put(e.name, e);
        });

        // Now we process the loaded moves.

        for (Move json : loadedMoves)
        {
            // If flagged as to remove, skip it.
            if (json.remove) continue;
            // Start by making a move entry for it.
            MoveEntry entry = new MoveEntry(json.name);

            MoveHolder holder = new MoveHolder();
            holder.setMove(json);
            holder.animation = loadedAnimations.get(json.name);

            // Initialises preset/cleans up text.
            holder.preParse();

            // Create and assign a root entry.
            entry.root_entry = holder;

            var parser = Parsers.getParser(json.move_category);

            if (parser == null) parser = Parsers.getCustomParser(entry.name);
            if (parser == null)
            {
                if (PokecubeCore.getConfig().debug_data)
                    PokecubeAPI.LOGGER.error("Warning, no parser for {} {}", json.name, json.move_category);
            }
            // Process the move values.
            else parser.process(entry);

            // Register the move entry.
            MoveEntry.addMove(entry);
        }
        if (PokecubeCore.getConfig().debug_data) PokecubeAPI.logInfo("Registered {} moves", loadedMoves.size());
    }

    public static void postInitMoves()
    {
        MoveEntry.values().forEach(move -> MovesAdder.moveProcessors.forEach(processor -> processor.accept(move)));
    }
}
