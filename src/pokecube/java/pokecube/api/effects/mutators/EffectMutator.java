package pokecube.api.effects.mutators;

import net.minecraft.resources.ResourceLocation;
import pokecube.api.effects.EffectPacketInfo;

public interface EffectMutator
{
    public static ResourceLocation MOVE = ResourceLocation.fromNamespaceAndPath("pokecube", "move_entry");
    public static ResourceLocation FLAVOUR = ResourceLocation.fromNamespaceAndPath("pokecube", "flavour");
    public static ResourceLocation DYE = ResourceLocation.fromNamespaceAndPath("pokecube", "dye");

    void mutate(EffectPacketInfo info);
    ResourceLocation getKey();
}
