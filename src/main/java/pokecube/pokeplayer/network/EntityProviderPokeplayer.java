package pokecube.pokeplayer.network;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import pokecube.core.network.EntityProvider;
import pokecube.pokeplayer.PokeInfo;
import thut.core.common.handlers.PlayerDataHandler;

public class EntityProviderPokeplayer extends EntityProvider
{
	 public EntityProviderPokeplayer(final EntityProvider defaults)
    {
        super(defaults);
    }

    @Override
    public Entity getEntity(final World world, final int id, final boolean expectsPokemob)
    {
        final Entity ret = world.getEntityByID(id);
        if (expectsPokemob && ret instanceof PlayerEntity)
        {
            final PlayerEntity player = Minecraft.getInstance().player;
            final PokeInfo info = PlayerDataHandler.getInstance().getPlayerData(player).getData(PokeInfo.class);
            if (info.getPokemob(world) != null) return info.getPokemob(world).getEntity();
        }
        return super.getEntity(world, id, expectsPokemob);
    }
}
