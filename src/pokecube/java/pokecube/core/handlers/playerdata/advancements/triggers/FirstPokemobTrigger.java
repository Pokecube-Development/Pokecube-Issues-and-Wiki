package pokecube.core.handlers.playerdata.advancements.triggers;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import pokecube.core.impl.PokecubeMod;

public class FirstPokemobTrigger extends SimplePokemobTrigger
{
    public static ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(PokecubeMod.ID, "get_first_pokemob");

    public void trigger(ServerPlayer player)
    {
        this.trigger(player, instance -> true);
    }
}
