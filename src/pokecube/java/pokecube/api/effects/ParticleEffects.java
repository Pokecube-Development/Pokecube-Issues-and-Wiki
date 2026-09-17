package pokecube.api.effects;

import net.minecraft.resources.ResourceLocation;
import pokecube.api.effects.context.EffectContextType;
import pokecube.api.effects.context.EntityContext;
import pokecube.api.effects.context.MoveEntryContext;
import pokecube.api.effects.context.PokemobContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ParticleEffects
{
    /**
     * Adds the MovePacketInfo for rendering client side. This is set in the ClientMod, so as to
     * not upset the server for trying to load in any client side code for rendering, etc
     */
    public static Consumer<EffectPacketInfo> ADD_FOR_RENDER = info->{};
    /**
     * Adds the MovePacketInfo for any server side processing needed, this is presently not implemented.
     */
    public static Consumer<EffectPacketInfo> ADD_FOR_SERVER = info->{};

    /**
     * List of locators for evolution effect to anchor to, it picks first in the list that matches
     */
    public static List<String> EVO_ANCHORS = new ArrayList<>();

    public static Map<ResourceLocation, EffectContextType<?>> CONTEXT_REGISTRY = new ConcurrentHashMap<>();

    public static ResourceLocation POKEMOB_CONTEXT = ResourceLocation.fromNamespaceAndPath("pokecube", "pokemob");
    public static ResourceLocation ENTITY_CONTEXT = ResourceLocation.fromNamespaceAndPath("pokecube", "entity");
    public static ResourceLocation MOVE_CONTEXT = ResourceLocation.fromNamespaceAndPath("pokecube", "move_entry");

    public static Map<String, Supplier<IAnimatedEffects.EffectRecord>> EFFECT_REGISTRY = new ConcurrentHashMap<>();

    static
    {
        // Put this in as a default.
        EVO_ANCHORS.add("body");
        CONTEXT_REGISTRY.put(MOVE_CONTEXT, MoveEntryContext.TYPE);
        CONTEXT_REGISTRY.put(POKEMOB_CONTEXT, PokemobContext.TYPE);
        CONTEXT_REGISTRY.put(ENTITY_CONTEXT, EntityContext.TYPE);
    }

    public static void init()
    {
        VectorPositionSource.init();
        EffectPacketInfo.TaggedEntityTracker.init();
    }

    public static void registerRecord(IAnimatedEffects.EffectRecord effect)
    {
        EFFECT_REGISTRY.put(effect.key(), () -> effect);
    }
}
