package pokecube.legends.handlers;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Teleporter;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;

public class TeleportUltraSpace extends Teleporter
{
    public TeleportUltraSpace(final ServerWorld worldIn)
    {
        super(worldIn);
    }

    @Override
    public boolean placeInPortal(final Entity entity, final float p_222268_2_)
    {
        final BlockPos entityPos = entity.getPosition();

        entity.setMotion(0, 0, 0);

        int destY = entityPos.getY();

        // This should always be the case, but check anyway...
        if (entity.getEntityWorld() instanceof ServerWorld)
        {
            final ServerWorld old = (ServerWorld) entity.getEntityWorld();
            ServerWorld dest;
            // This transfers between dim 0 and ultraspace.

            // In ultraspace, so going to overworld. TODO ultraspace dim instead
            if (old.getDimension().getType() == DimensionType.THE_NETHER) dest = old.getServer().getWorld(
                    DimensionType.OVERWORLD);
            else dest = old.getServer().getWorld(DimensionType.THE_NETHER);
            // forces the chunk to load
            dest.getChunkAt(entityPos);
            // finds surface height for location.
            destY = dest.getHeight(Heightmap.Type.WORLD_SURFACE, entityPos.getX(), entityPos.getZ()) + 2;
        }

        if (entity instanceof ServerPlayerEntity) ((ServerPlayerEntity) entity).connection.setPlayerLocation(entityPos
                .getX() + 0.5, destY + 0.5, entityPos.getZ() + 0.5, entity.rotationYaw, entity.rotationPitch);
        else entity.setLocationAndAngles(entityPos.getX() + 0.5, destY + 0.5, entityPos.getZ() + 0.5,
                entity.rotationYaw, entity.rotationPitch);
        return true;
    }

    @Override
    public boolean makePortal(final Entity entityIn)
    {
        return true;
    }
}