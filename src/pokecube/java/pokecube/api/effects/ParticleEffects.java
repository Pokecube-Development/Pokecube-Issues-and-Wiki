package pokecube.api.effects;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import pokecube.api.effects.context.EffectContext;
import pokecube.api.effects.context.EntityContext;
import pokecube.api.effects.context.MoveEntryContext;
import pokecube.api.effects.context.NBTContext;
import pokecube.api.effects.context.PokemobContext;
import pokecube.api.effects.mutators.DyeEffectMutator;
import pokecube.api.effects.mutators.EffectMutator;
import pokecube.api.effects.mutators.FlavourEffectMutator;
import pokecube.api.effects.mutators.MoveEffectMutator;
import pokecube.api.effects.network.PacketEffects;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ParticleEffects
{
    /**
     * Adds the MovePacketInfo for rendering client side. This is set in the ClientMod, so as to
     * not upset the server for trying to load in any client side code for rendering, etc
     */
    public static Consumer<EffectPacketInfo> ADD_FOR_RENDER = info->{
        if(!info.level.isClientSide()) PacketEffects.sendPacket(info);
    };
    /**
     * Adds the MovePacketInfo for any server side processing needed, this is presently not implemented.
     */
    public static Consumer<EffectPacketInfo> ADD_FOR_SERVER = info->{};

    /**
     * List of locators for evolution effect to anchor to, it picks first in the list that matches
     */
    public static List<String> EVO_ANCHORS = new ArrayList<>();

    public static Map<ResourceLocation, StreamCodec<ByteBuf, ? extends EffectContext<?>>> CONTEXT_REGISTRY = new ConcurrentHashMap<>();
    public static Map<ResourceLocation, EffectMutator> MUTATOR_REGISTRY = new ConcurrentHashMap<>();

    public static Map<String, Supplier<IAnimatedEffects.EffectRecord>> EFFECT_REGISTRY = new ConcurrentHashMap<>();

    static
    {
        // Put this in as a default.
        EVO_ANCHORS.add("body");
        CONTEXT_REGISTRY.put(EffectContext.MOVE, MoveEntryContext.STREAM_CODEC);
        CONTEXT_REGISTRY.put(EffectContext.POKEMOB, PokemobContext.STREAM_CODEC);
        CONTEXT_REGISTRY.put(EffectContext.ENTITY, EntityContext.STREAM_CODEC);
        CONTEXT_REGISTRY.put(EffectContext.NBT, NBTContext.STREAM_CODEC);

        MUTATOR_REGISTRY.put(EffectMutator.MOVE, new MoveEffectMutator());
        MUTATOR_REGISTRY.put(EffectMutator.FLAVOUR, new FlavourEffectMutator());
        MUTATOR_REGISTRY.put(EffectMutator.DYE, new DyeEffectMutator());
    }

    public static void init()
    {
        VectorPositionSource.init();
        EffectPacketInfo.TaggedEntityTracker.init();
    }

    public static void registerRecord(String key, Supplier<IAnimatedEffects.EffectRecord> effect)
    {
        EFFECT_REGISTRY.put(key, effect);
    }

    public static Supplier<IAnimatedEffects.EffectRecord> getEffect(String key)
    {
        return EFFECT_REGISTRY.getOrDefault(key, () -> new IAnimatedEffects.EffectRecord(key));
    }
}
