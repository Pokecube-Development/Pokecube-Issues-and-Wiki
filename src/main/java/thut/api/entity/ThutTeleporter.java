package thut.api.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import thut.api.maths.Vector4;

public class ThutTeleporter
{
    public static void transferTo(Entity entity, Vector4 dest)
    {
        if (entity.getEntityWorld() instanceof ServerWorld)
        {
            moveMob(entity, dest);
            if (dest.w == entity.dimension.getId()) { return; }
            if (entity instanceof ServerPlayerEntity)
            {
                ServerPlayerEntity player = (ServerPlayerEntity) entity;
                ServerWorld destWorld = player.server.getWorld(DimensionType.getById((int) dest.w));
                player.teleport(destWorld, dest.x, dest.y, dest.z, entity.rotationYaw, entity.rotationPitch);
            }
        }
    }

    private static void moveMob(Entity entity, Vector4 dest)
    {
        if (entity instanceof ServerPlayerEntity)
        {
            ((ServerPlayerEntity) entity).connection.setPlayerLocation(dest.x, dest.y, dest.z, entity.rotationYaw,
                    entity.rotationPitch);
            ((ServerPlayerEntity) entity).connection.captureCurrentPosition();
        }
        else
        {
            entity.setLocationAndAngles(dest.x, dest.y, dest.z, entity.rotationYaw, entity.rotationPitch);
        }
    }
}
