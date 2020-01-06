package pokecube.core.ai;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.controller.MovementController;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import pokecube.core.PokecubeCore;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.LogicStates;

/**
 * Overriden to properly support mobs that move in 3D, such as flying or
 * swimming ones, as well as the make it so if a mob has transformed, it uses
 * the movement type of who it has transformed to.
 */
public class PokemobMoveHelper extends MovementController
{
    final IPokemob pokemob;

    public PokemobMoveHelper(final MobEntity entity)
    {
        super(entity);
        this.pokemob = CapabilityPokemob.getPokemobFor(entity);
    }

    @Override
    public void tick()
    {
        PokedexEntry entry = this.pokemob.getPokedexEntry();
        final IPokemob transformed = CapabilityPokemob.getPokemobFor(this.pokemob.getTransformedTo());
        IPokemob theMob = this.pokemob;
        if (transformed != null)
        {
            entry = transformed.getPokedexEntry();
            theMob = transformed;
        }
        final boolean water = entry.swims() && this.mob.isInWater();
        final boolean air = theMob.flys() || theMob.floats();

        if (this.action != MovementController.Action.MOVE_TO)
        {
            this.pokemob.setDirectionPitch(0);
            super.tick();
            return;
        }

        this.action = MovementController.Action.WAIT;
        final double dx = this.posX - this.mob.posX;
        final double dy = this.posY - this.mob.posY;
        final double dz = this.posZ - this.mob.posZ;
        final double dr = dx * dx + dy * dy + dz * dz;
        final double dhoriz = dx * dx + dz * dz;

        this.pokemob.setDirectionPitch(0);
        this.mob.setMoveVertical(0);
        boolean shouldGoDown = false;
        boolean shouldGoUp = false;
        PathPoint p = null;
        if (!this.mob.getNavigator().noPath() && Math.abs(dy) > 0.05)
        {
            p = this.mob.getNavigator().getPath().getPathPointFromIndex(this.mob.getNavigator().getPath()
                    .getCurrentPathIndex());
            shouldGoDown = p.y < this.mob.posY - this.mob.stepHeight;
            shouldGoUp = p.y > this.mob.posY + this.mob.stepHeight;
            if (air || water)
            {
                shouldGoUp = p.y > this.mob.posY;
                shouldGoDown = !shouldGoUp;
            }
        }
        if ((this.pokemob.getLogicState(LogicStates.SLEEPING) || (this.pokemob.getStatus() & IMoveConstants.STATUS_SLP
                + IMoveConstants.STATUS_FRZ) > 0) && air) shouldGoDown = true;
        final float length = this.pokemob.getPokedexEntry().length * this.pokemob.getSize();
        float dSize = Math.max(0.25f, this.mob.getWidth() * this.mob.getWidth() + length * length);
        if (!this.mob.getNavigator().noPath())
        {
            final BlockPos pos = this.mob.getPosition();
            final PathPoint p2 = this.mob.getNavigator().getPath().getFinalPathPoint();
            if (p2 == p && pos.getX() == p2.x && (!(air || water) || pos.getY() == p2.y) && pos.getZ() == p2.z)
                dSize = 1;
        }
        if (dr < dSize)
        {
            this.mob.setMoveForward(0.0F);
            if (!this.mob.getNavigator().noPath()) this.mob.getNavigator().getPath().setCurrentPathIndex(this.mob
                    .getNavigator().getPath().getCurrentPathIndex() + 1);
            return;
        }

        float newYaw = (float) (MathHelper.atan2(dz, dx) * (180D / Math.PI)) - 90.0F;
        newYaw = this.limitAngle(this.mob.rotationYaw, newYaw, 30.0F);
        this.mob.rotationYaw = newYaw;
        float v = (float) (this.speed * this.mob.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getValue());
        if (air && !this.mob.onGround && !this.mob.isInWater()) v *= PokecubeCore.getConfig().flyPathingSpeedFactor;
        else if (water) v *= PokecubeCore.getConfig().swimPathingSpeedFactor;
        this.mob.setAIMoveSpeed(v);

        if (shouldGoDown || shouldGoUp)
        {
            final float pitch = -(float) (Math.atan(dy / Math.sqrt(dhoriz)) * 180 / Math.PI);
            this.pokemob.setDirectionPitch(pitch);
            float factor = 1;
            if (water && dy < 0.5) factor = 2;
            final float up = -MathHelper.sin(pitch * (float) Math.PI / 180.0F) * factor;
            this.mob.setMoveVertical(up);
        }

        final boolean upLadder = dy > 0 && this.mob.isOnLadder();
        final boolean jump = upLadder || dy > this.mob.stepHeight && dhoriz < Math.max(1.0F, this.mob.getWidth())
                || dy > this.mob.stepHeight && dhoriz <= 2 * this.speed;

        if (jump)
        {
            this.mob.getJumpController().setJumping();
            this.action = MovementController.Action.JUMPING;
        }
    }
}