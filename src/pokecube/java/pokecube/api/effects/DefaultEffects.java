package pokecube.api.effects;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.moves.Moves;
import pokecube.core.PokecubeCore;
import pokecube.core.database.resources.PackFinder;
import pokecube.core.effects.AnimationMultiAnimations;
import pokecube.core.effects.MoveAnimationHelper;
import thut.api.util.JsonUtil;
import thut.lib.ResourceHelper;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DefaultEffects
{
    public static void loadEffects()
    {
        final String effects_root = "effect/";

        var resources = PackFinder.getJsonResources(effects_root);
        Map<String, List<Moves.Animation>> animsToLoad = Maps.newHashMap();
        if (PokecubeCore.getConfig().debug_data) PokecubeAPI.logInfo("Effects resources " + resources);
        resources.forEach((l, r) -> {
            try
            {
                final InputStreamReader reader = new InputStreamReader(ResourceHelper.getStream(r));
                Moves.Animation animationFile = JsonUtil.gson.fromJson(reader, Moves.Animation.class);
                reader.close();
                if (animationFile != null)
                    animsToLoad.compute(l.getPath().replaceFirst(effects_root, ""), (key, list) -> {
                        var ret = list;
                        if (ret == null) ret = Lists.newArrayList();
                        ret.add(animationFile);
                        return ret;
                    });
                else PokecubeAPI.LOGGER.error("Error with effects animation file {}", l);
            }
            catch (Exception e)
            {
                PokecubeAPI.LOGGER.error("Error with effects animation file {}", l, e);
            }
        });
        for(var entry: animsToLoad.entrySet()){
            var key = entry.getKey();
            var list = entry.getValue();
            // TODO See if datapacks can replace this properly
            var animRoot = list.getFirst();
            List<IAnimatedEffects> anims = new ArrayList<>();
            animRoot.animations.forEach(anim->{
                final IAnimatedEffects animation = MoveAnimationHelper.getAnimationPreset(anim.preset, anim.preset_values);
                if (animation == null)
                {
                    PokecubeAPI.LOGGER.warn("Warning, unknown animation for preset: {}", anim.preset);
                    return;
                }
                anims.add(animation);
            });
            var effect_key = key.replace(".json", "").replace("/", ".");
            var effect = new IAnimatedEffects.EffectRecord(effect_key,
                    anims.size() > 1 ? new AnimationMultiAnimations(anims) : anims.getFirst());
            ParticleEffects.registerRecord(effect_key, () -> effect);
        }
    }
}
