package pokecube.legends.init.function;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SChangeGameStatePacket;
import net.minecraft.network.play.server.SPlayEntityEffectPacket;
import net.minecraft.network.play.server.SPlaySoundEventPacket;
import net.minecraft.network.play.server.SPlayerAbilitiesPacket;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import pokecube.legends.worldgen.dimension.ModDimensions;

/**
 * Uses player interact here to also prevent opening of inventories.
 *
 * @param dependencies
 */
public class WormHoleActiveFunction
{
    public static void executeProcedure(final java.util.HashMap<String, Object> dependencies)
    {
        if (dependencies.get("entity") == null)
        {
            System.err.println("Failed to load dependency entity for procedure MCreatorTeleportDimension!");
            return;
        }
        final Entity entity = (Entity) dependencies.get("entity");
        if (!(entity instanceof ServerPlayerEntity)) return;

        if (entity.dimension.getId() == 0)
        {
            if (!entity.world.isRemote && entity instanceof ServerPlayerEntity)
            {
                final DimensionType destinationType = ModDimensions.DIMENSION_TYPE;

                ObfuscationReflectionHelper.setPrivateValue(ServerPlayerEntity.class, (ServerPlayerEntity) entity, true,
                        "field_184851_cj");
                final ServerWorld nextWorld = entity.getServer().getWorld(destinationType);

                ((ServerPlayerEntity) entity).connection.sendPacket(new SChangeGameStatePacket(4, 0));
                ((ServerPlayerEntity) entity).teleport(nextWorld, nextWorld.getSpawnPoint().getX(), nextWorld
                        .getSpawnPoint().getY() + 1, nextWorld.getSpawnPoint().getZ(), entity.rotationYaw,
                        entity.rotationPitch);
                ((ServerPlayerEntity) entity).connection.sendPacket(new SPlayerAbilitiesPacket(
                        ((ServerPlayerEntity) entity).abilities));
                for (final EffectInstance effectinstance : ((ServerPlayerEntity) entity).getActivePotionEffects())
                    ((ServerPlayerEntity) entity).connection.sendPacket(new SPlayEntityEffectPacket(entity
                            .getEntityId(), effectinstance));
                ((ServerPlayerEntity) entity).connection.sendPacket(new SPlaySoundEventPacket(1032, BlockPos.ZERO, 0,
                        false));
            }
        }
        else if (entity.dimension.getId() == ModDimensions.DIMENSION_TYPE.getId()) if (!entity.world.isRemote
                && entity instanceof ServerPlayerEntity)
        {
            final DimensionType destinationType = DimensionType.OVERWORLD;

            ObfuscationReflectionHelper.setPrivateValue(ServerPlayerEntity.class, (ServerPlayerEntity) entity, true,
                    "field_184851_cj");
            final ServerWorld nextWorld = entity.getServer().getWorld(destinationType);

            ((ServerPlayerEntity) entity).connection.sendPacket(new SChangeGameStatePacket(4, 0));
            ((ServerPlayerEntity) entity).teleport(nextWorld, nextWorld.getSpawnPoint().getX(), nextWorld
                    .getSpawnPoint().getY() + 1, nextWorld.getSpawnPoint().getZ(), entity.rotationYaw,
                    entity.rotationPitch);
            ((ServerPlayerEntity) entity).connection.sendPacket(new SPlayerAbilitiesPacket(
                    ((ServerPlayerEntity) entity).abilities));
            for (final EffectInstance effectinstance : ((ServerPlayerEntity) entity).getActivePotionEffects())
                ((ServerPlayerEntity) entity).connection.sendPacket(new SPlayEntityEffectPacket(entity.getEntityId(),
                        effectinstance));
            ((ServerPlayerEntity) entity).connection.sendPacket(new SPlaySoundEventPacket(1032, BlockPos.ZERO, 0,
                    false));
        }
    }
}