package pokecube.pokeplayer.events;

import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.logic.Logic;
import pokecube.core.utils.EntityTools;
import pokecube.pokeplayer.data.PokeInfo;
import thut.core.common.handlers.PlayerDataHandler;

@EventBusSubscriber
public class SizeHandler
{
    private static void setSize(final PlayerEntity player, final float height, final float width)
    {
        final EntitySize entitysize = player.size;
        final EntitySize entitysize1 = new EntitySize(width, height, true);
        player.size = entitysize1;
        if (entitysize1.width < entitysize.width)
        {
            final double d0 = entitysize1.width / 2.0D;
            player.setBoundingBox(new AxisAlignedBB(player.getPosX() - d0, player.getPosY(), player.getPosZ() - d0, player.getPosX() + d0,
                    player.getPosY() + entitysize1.height, player.getPosZ() + d0));
        }
        else
        {
            final AxisAlignedBB axisalignedbb = player.getBoundingBox();
            player.setBoundingBox(new AxisAlignedBB(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ,
                    axisalignedbb.minX + entitysize1.width, axisalignedbb.minY + entitysize1.height, axisalignedbb.minZ
                            + entitysize1.width));
            if (entitysize1.width > entitysize.width && !player.world.isRemote)
            {
                final float f = entitysize.width - entitysize1.width;
                player.move(MoverType.SELF, new Vector3d(f, 0.0D, f));
            }
        }
    }

    @SubscribeEvent
    public static void eyes(final EntityEvent.Size evt)
    {
        if (!(evt.getEntity() instanceof PlayerEntity)) return;
        final PlayerEntity player = (PlayerEntity) evt.getEntity();
        final PokeInfo info = PlayerDataHandler.getInstance().getPlayerData(player).getData(PokeInfo.class);
        final float oldEyePos = evt.getOldEyeHeight();
        if (info.getPokemob() == null) {
        	evt.setNewEyeHeight(oldEyePos);
        }
        else {
        	evt.setNewEyeHeight(info.getPokemob().getSize() * info.getPokemob().getPokedexEntry().height * 0.85f);
        }
    }

    @SubscribeEvent
    public static void tick(final LivingUpdateEvent evt)
    {
        if (!(evt.getEntity() instanceof PlayerEntity)) return;
        final PlayerEntity player = (PlayerEntity) evt.getEntity();
        final PokeInfo info = PlayerDataHandler.getInstance().getPlayerData(player).getData(PokeInfo.class);
        info.onAttach(player);
        if (info.getPokemob() != null)
        {
            final float h = info.getPokemob().getSize() * info.getPokemob().getPokedexEntry().height;
            final float w = info.getPokemob().getSize() * info.getPokemob().getPokedexEntry().width;
            SizeHandler.setSize(player, h, w);
            info.getPokemob().getEntity().setPosition(player.getPosX(), player.getPosY(), player.getPosZ());
            EntityTools.copyEntityTransforms(info.getPokemob().getEntity(), player);
            
            // Reset death time if we are not dead.
            if (info.getPokemob().getEntity().getHealth() > 0) evt.getEntityLiving().deathTime = 0;
            info.getPokemob().setHungerTime(-PokecubeCore.getConfig().pokemobLifeSpan / 4);
            if (player.getEntityWorld() instanceof ServerWorld)
            {
                @SuppressWarnings("unchecked")
                final Brain<? super AgeableEntity> brain = (Brain<? super AgeableEntity>) info.getPokemob().getEntity()
                        .getBrain();
                brain.tick((ServerWorld) player.getEntityWorld(), info.getPokemob().getEntity());
            }
            // Tick the logic stuff for this mob.
            for (final Logic l : info.getPokemob().getTickLogic())
                if (l.shouldRun()) l.tick(evt.getEntity().getEntityWorld());
        }
    }
}
