package pokecube.core.ai.logic;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.ai.AIRoutine;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.tasks.TaskBase;
import pokecube.core.utils.Permissions;
import thut.api.maths.Interpolator1d;
import thut.lib.ChatHelper;

import java.util.List;
import java.util.Set;

/**
 * This manages the ridden controls of the pokemob. The booleans are set on the client side, then sent via a packet to
 * the server, and then the mob is moved accordingly.
 */
public class LogicMountedControl extends LogicBase
{
    private static final ResourceLocation UID = ResourceLocation.parse("pokecube:ridden_step");
    private final AttributeModifier riddenStep;

    public static Set<ResourceKey<Level>> BLACKLISTED = Sets.newHashSet();
    public static Interpolator1d FLYVITSCALER;
    public static Interpolator1d WALKVITSCALER;
    public static Interpolator1d SURFVITSCALER;

    public boolean leftInputDown = false;
    public boolean rightInputDown = false;
    public boolean forwardInputDown = false;
    public boolean backInputDown = false;
    public boolean upInputDown = false;
    public boolean downInputDown = false;
    public boolean followOwnerLook = false;
    public boolean canPathWhileRidden = false;
    public double throttle = 0.5;

    private boolean input = false;
    private boolean wasRiding = false;

    public boolean canFly;
    public boolean canSurf;
    public boolean canDive;

    public float moveFwd = 0;
    public float moveSide = 0;
    public float moveUp = 0;

    public boolean verticalControl = false;
    public boolean shouldControl = false;

    public boolean inFluid;

    public LogicMountedControl(final IPokemob pokemob_)
    {
        super(pokemob_);
        if (this.entity.getPersistentData().contains("pokecube:mob_throttle"))
            this.throttle = this.entity.getPersistentData().getDouble("pokecube:mob_throttle");
        this.riddenStep = new AttributeModifier(UID, 0.75, Operation.ADD_VALUE);
    }

    public boolean blocksPathing()
    {
        if (!TaskBase.canMove(this.pokemob)) return true;
        final Entity rider = this.entity.getControllingPassenger();
        if (rider == null) return false;
        return !this.canPathWhileRidden;
    }

    public void refreshInput()
    {
        this.input = this.leftInputDown || this.rightInputDown || this.forwardInputDown || this.backInputDown
                || this.upInputDown || this.downInputDown;

        final Entity rider = this.entity.getControllingPassenger();

        this.inFluid = this.entity.isInWater() || this.entity.isInLava();

        this.canFly = this.pokemob.canUseFly();
        this.canSurf = this.pokemob.canUseSurf();
        this.canDive = this.pokemob.canUseDive();

        if (rider instanceof ServerPlayer player)
        {
            if (this.canFly && !Permissions.canFly(pokemob, player)) this.canFly = false;
            if (this.canSurf && !Permissions.canSurf(pokemob, player)) this.canSurf = false;
            if (this.canDive && !Permissions.canDive(pokemob, player)) this.canDive = false;
        }
        if (this.canFly)
        {
            boolean noFly = LogicMountedControl.BLACKLISTED.contains(rider.level().dimension());
            if (noFly)
            {
                if (this.pokemob.isRoutineEnabled(AIRoutine.AIRBORNE))
                {
                    this.pokemob.setRoutineState(AIRoutine.AIRBORNE, false);
                    if (rider instanceof ServerPlayer player) ChatHelper.sendSystemMessage(player,
                            Component.translatable("pokemob.fly.disabled", pokemob.getDisplayName()));
                }
                this.canFly = false;
            }
        }
    }

    // TODO decide if these need an acceleration component
    public float getFlightSpeedScale()
    {
        if(FLYVITSCALER != null)
        {
            return (float) FLYVITSCALER.interpolate(this.pokemob.getStat(IPokemob.Stats.VIT, true));
        }
        return 1;
    }

    public float getWalkSpeedScale()
    {
        if(WALKVITSCALER != null)
        {
            return (float) WALKVITSCALER.interpolate(this.pokemob.getStat(IPokemob.Stats.VIT, true));
        }
        return 1;
    }

    public float getSurfSpeedScale()
    {
        if(SURFVITSCALER != null)
        {
            return (float) SURFVITSCALER.interpolate(this.pokemob.getStat(IPokemob.Stats.VIT, true));
        }
        return 1;
    }

    public boolean hasInput()
    {
        return this.input;
    }

    @Override
    public void tick(final Level level)
    {
        super.tick(level);

        final Entity rider = this.entity.getControllingPassenger();
        moveUp = moveSide = moveFwd = 0;
        this.pokemob.setGeneralState(GeneralStates.CONTROLLED, rider != null);
        boolean noGrav = entity.isNoGravity();
        AttributeInstance stepHeightAttribute = this.entity.getAttribute(Attributes.STEP_HEIGHT);
        if (!stepHeightAttribute.hasModifier(UID)) stepHeightAttribute.addTransientModifier(riddenStep);

        if (rider == null)
        {
            if (this.wasRiding)
            {
                if (noGrav && !level.isClientSide()) this.entity.setNoGravity(false);
                this.wasRiding = false;
            }
            return;
        }

        this.wasRiding = true;
        this.entity.setYRot(this.pokemob.getHeading());

        shouldControl = this.entity.onGround() || this.pokemob.canUseFly();
        verticalControl = false;
        boolean waterSpeed = false;
        boolean airSpeed = !this.entity.onGround();

        final boolean fluidRestricted = this.inFluid && !(this.canSurf || this.canDive);

        if (this.canFly)
        {
            shouldControl = verticalControl = PokecubeCore.getConfig().flyEnabled || shouldControl;
            if (verticalControl && !noGrav) this.entity.setNoGravity(noGrav = true);
        }
        if ((this.canSurf || this.canDive) && (waterSpeed = this.entity.isInWater()))
            shouldControl = verticalControl = PokecubeCore.getConfig().surfEnabled || shouldControl;
        if (waterSpeed) airSpeed = false;

        final List<MobEffectInstance> buffs = Lists.newArrayList();

        if (waterSpeed && this.pokemob.getPokedexEntry().shouldDive)
        {
            final MobEffectInstance breathing = new MobEffectInstance(MobEffects.WATER_BREATHING, 300, 1, true, false);
            buffs.add(breathing);
        }

        if (this.entity.isInLava() && this.entity.fireImmune())
        {
            final MobEffectInstance no_burning = new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 60, 1, true, false);
            shouldControl = true;
            verticalControl = true;
            buffs.add(no_burning);
        }
        buffs.forEach(b -> b.getCures().clear());

        shouldControl |= verticalControl || this.inFluid;

        if (!level.isClientSide() && noGrav != verticalControl) this.entity.setNoGravity(verticalControl);

        for (final Entity e : this.entity.getIndirectPassengers())
            if (e instanceof LivingEntity living)
            {
                final boolean doBuffs = !buffs.isEmpty();
                if (doBuffs) for (final MobEffectInstance buff : buffs) living.addEffect(buff);
            }

        double vx = this.entity.getDeltaMovement().x;
        double vy = this.entity.getDeltaMovement().y;
        double vz = this.entity.getDeltaMovement().z;

        if (!this.hasInput())
        {
            vx *= 0.5;
            vz *= 0.5;
            if (verticalControl) vy *= 0.5;
            this.entity.setDeltaMovement(vx, vy, vz);
            return;
        }

        if (!shouldControl) return;
        float speedFactor;

        if(airSpeed)
        {
            speedFactor = getFlightSpeedScale();
        }
        else if (waterSpeed)
        {
            speedFactor = getSurfSpeedScale();
        }
        else
        {
            speedFactor = getWalkSpeedScale();
        }

        float baseSpd = (float) (0.5f * this.throttle * speedFactor);

        if (fluidRestricted) baseSpd *= 0.5f;

        moveFwd = this.backInputDown ? -baseSpd / 2 : this.forwardInputDown ? baseSpd : 0;
        moveSide = this.leftInputDown ? baseSpd : this.rightInputDown ? -baseSpd : 0;
        moveUp = this.upInputDown ? baseSpd : this.downInputDown ? -baseSpd : 0;
        float pitch = rider.getXRot();

        if (Math.abs(pitch) > 45 && this.followOwnerLook && verticalControl)
        {
            pitch *= -0.017453292F;
            if (this.backInputDown) pitch *= -1;
            final float sin = (float) Math.sin(pitch);
            final float cos = (float) Math.cos(pitch);
            moveUp = baseSpd;
            moveFwd *= cos;
            moveUp *= sin;
            if (Math.abs(pitch) > 75)
            {
                moveFwd = 0;
                moveUp = Math.signum(pitch) * baseSpd;
            }
            if (this.upInputDown) moveUp = Math.abs(moveUp);
            else if (this.downInputDown) moveUp = -Math.abs(moveUp);
        }
        if (!verticalControl) moveUp = 0;

        if (!this.entity.getPassengers().isEmpty())
        {
            this.pokemob.setHeading(rider.getYRot());
        }
    }

}
