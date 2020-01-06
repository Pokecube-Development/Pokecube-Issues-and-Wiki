package pokecube.adventures.ai.tasks;

import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;
import pokecube.adventures.Config;
import pokecube.adventures.capabilities.CapabilityNPCAIStates.IHasNPCAIStates;
import pokecube.adventures.entity.trainer.TrainerSpawnHandler;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.PokecubeMod;
import thut.api.maths.Vector3;

public class AIMate extends AITrainerBase
{
    AgeableEntity                        foundMate = null;
    final Class<? extends AgeableEntity> targetClass;
    final AgeableEntity                  thisEntity;
    AgeableEntity                        child     = null;
    int                                  mateTimer = -1;
    final EntityPredicate                predicate;

    public AIMate(final LivingEntity trainer, final Class<? extends AgeableEntity> targetClass)
    {
        super(trainer);
        this.targetClass = targetClass;
        if (trainer instanceof AgeableEntity) this.thisEntity = (AgeableEntity) trainer;
        else this.thisEntity = null;
        this.predicate = new EntityPredicate().setDistance(8);
    }

    @Override
    public boolean shouldRun()
    {
        if (!Config.instance.trainersMate) return false;
        return this.thisEntity != null && this.thisEntity.getGrowingAge() == 0 && this.trainer.getGender() == 2
                && this.aiTracker.getAIState(IHasNPCAIStates.MATES) && TrainerSpawnHandler.countTrainersNear(
                        this.thisEntity, Config.instance.trainerBox) < Config.instance.trainerDensity * 2;
    }

    @Override
    public void tick()
    {
        super.tick();
        if (this.shouldRun())
        {
            if (PokecubeMod.debug) PokecubeCore.LOGGER.debug(this.thisEntity + " is Looking for mate");
            this.foundMate = this.world.getClosestEntityWithinAABB(this.targetClass, this.predicate, this.thisEntity,
                    this.thisEntity.posX, this.thisEntity.posY + this.thisEntity.getEyeHeight(), this.thisEntity.posZ,
                    this.thisEntity.getBoundingBox().grow(8.0D, 3.0D, 8.0D));
            if (this.foundMate == null)
            {
                this.thisEntity.setGrowingAge(600);
                return;
            }
            if (this.world.getEntitiesWithinAABB(this.targetClass, this.thisEntity.getBoundingBox().grow(16.0D, 10.0D,
                    16.0D)).size() > 3)
            {
                this.thisEntity.setGrowingAge(6000);
                return;
            }
            this.child = this.thisEntity.createChild(this.foundMate);
            this.thisEntity.setGrowingAge(6000);
            this.foundMate.setGrowingAge(6000);
            final BabyEntitySpawnEvent event = new net.minecraftforge.event.entity.living.BabyEntitySpawnEvent(
                    this.thisEntity, this.foundMate, this.child);
            if (MinecraftForge.EVENT_BUS.post(event) || event.getChild() == null) return;
            this.child = event.getChild();
            this.child.setGrowingAge(-24000);
            this.mateTimer = 50;
        }
        if (this.child != null && this.foundMate != null) if (this.mateTimer-- <= 0)
        {
            this.thisEntity.getNavigator().tryMoveToEntityLiving(this.foundMate, this.thisEntity.getAIMoveSpeed());
            this.foundMate.getNavigator().tryMoveToEntityLiving(this.thisEntity, this.foundMate.getAIMoveSpeed());
        }
        else
        {
            final Vector3 loc = Vector3.getNewVector().set(this.thisEntity.getLookVec());
            loc.y = 0;
            loc.norm();
            this.child.setLocationAndAngles(this.thisEntity.posX + loc.x, this.thisEntity.posY, this.thisEntity.posZ
                    + loc.z, 0.0F, 0.0F);
            this.world.addEntity(this.child);
            this.world.setEntityState(this.child, (byte) 12);
            this.child = null;
            this.foundMate = null;
        }
    }
}
