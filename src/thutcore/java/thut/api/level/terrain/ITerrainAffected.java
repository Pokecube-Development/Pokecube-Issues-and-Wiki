package thut.api.level.terrain;

import net.minecraft.world.entity.LivingEntity;

public interface ITerrainAffected
{
    void attach(LivingEntity mob);

    LivingEntity getAttached();

    void onTerrainTick();
}
