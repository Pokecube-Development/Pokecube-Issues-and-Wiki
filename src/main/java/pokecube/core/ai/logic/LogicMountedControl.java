package pokecube.core.ai.logic;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import pokecube.core.PokecubeCore;
import pokecube.core.database.PokedexEntry;
import pokecube.core.handlers.Config;
import pokecube.core.interfaces.IMoveConstants.AIRoutine;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.utils.PermNodes;
import pokecube.core.utils.Permissions;

/**
 * This manages the ridden controls of the pokemob. The booleans are set on the
 * client side, then sent via a packet to the server, and then the mob is moved
 * accordingly.
 */
public class LogicMountedControl extends LogicBase
{
    public static Set<ResourceKey<Level>> BLACKLISTED = Sets.newHashSet();

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

    public boolean inFluid;

    public LogicMountedControl(final IPokemob pokemob_)
    {
        super(pokemob_);
        if (this.entity.getPersistentData().contains("pokecube:mob_throttle"))
            this.throttle = this.entity.getPersistentData().getDouble("pokecube:mob_throttle");
    }

    public boolean blocksPathing()
    {
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

        final Config config = PokecubeCore.getConfig();

        if (rider instanceof ServerPlayer player)
        {
            final PokedexEntry entry = this.pokemob.getPokedexEntry();
            if (config.permsFly && this.canFly && !PermNodes.getBooleanPerm(player, Permissions.FLYPOKEMOB))
                this.canFly = false;
            if (config.permsFlySpecific && this.canFly
                    && !PermNodes.getBooleanPerm(player, Permissions.FLYSPECIFIC.get(entry)))
                this.canFly = false;
            if (config.permsSurf && this.canSurf && !PermNodes.getBooleanPerm(player, Permissions.SURFPOKEMOB))
                this.canSurf = false;
            if (config.permsSurfSpecific && this.canSurf
                    && !PermNodes.getBooleanPerm(player, Permissions.SURFSPECIFIC.get(entry)))
                this.canSurf = false;
            if (config.permsDive && this.canDive && !PermNodes.getBooleanPerm(player, Permissions.DIVEPOKEMOB))
                this.canDive = false;
            if (config.permsDiveSpecific && this.canDive
                    && !PermNodes.getBooleanPerm(player, Permissions.DIVESPECIFIC.get(entry)))
                this.canDive = false;
        }
        if (this.canFly)
            this.canFly = !LogicMountedControl.BLACKLISTED.contains(rider.getCommandSenderWorld().dimension());
    }

    public boolean hasInput()
    {
        return this.input;
    }

    @Override
    public void tick(final Level world)
    {
        super.tick(world);
        final Entity rider = this.entity.getControllingPassenger();
        this.entity.maxUpStep = 1.1f;
        this.pokemob.setGeneralState(GeneralStates.CONTROLLED, rider != null);
        if (rider == null)
        {
            if (this.wasRiding && this.pokemob.isRoutineEnabled(AIRoutine.AIRBORNE))
            {
                this.entity.setNoGravity(false);
                this.wasRiding = false;
            }
            return;
        }

        this.wasRiding = true;
        final Config config = PokecubeCore.getConfig();
        boolean move = false;
        this.entity.yRot = this.pokemob.getHeading();

        boolean shouldControl = this.entity.isOnGround() || this.pokemob.canUseFly();
        boolean verticalControl = false;
        boolean waterSpeed = false;
        boolean airSpeed = !this.entity.isOnGround();

        final boolean fluidRestricted = this.inFluid && !(this.canSurf || this.canDive);

        if (this.canFly)
        {
            shouldControl = verticalControl = PokecubeCore.getConfig().flyEnabled || shouldControl;
            if (verticalControl) this.entity.setNoGravity(true);
        }
        if ((this.canSurf || this.canDive) && (waterSpeed = this.entity.isInWater()))
            shouldControl = verticalControl = PokecubeCore.getConfig().surfEnabled || shouldControl;
        if (waterSpeed) airSpeed = false;

        final Entity controller = rider;
        final ItemStack stack = new ItemStack(Blocks.BARRIER);
        final List<MobEffectInstance> buffs = Lists.newArrayList();

        if (waterSpeed && this.pokemob.getPokedexEntry().shouldDive)
        {
            final MobEffectInstance vision = new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 1, true, false);
            final MobEffectInstance breathing = new MobEffectInstance(MobEffects.WATER_BREATHING, 300, 1, true, false);
            vision.setCurativeItems(Lists.newArrayList(stack));
            breathing.setCurativeItems(Lists.newArrayList(stack));
            buffs.add(vision);
            buffs.add(breathing);
        }

        if (this.entity.isInLava() && this.entity.fireImmune())
        {
            final MobEffectInstance no_burning = new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 60, 1, true, false);
            shouldControl = true;
            verticalControl = true;
            no_burning.setCurativeItems(Lists.newArrayList(stack));
            buffs.add(no_burning);
        }

        shouldControl = verticalControl || this.inFluid;

        this.entity.setNoGravity(verticalControl);

        if (!shouldControl) return;

        for (final Entity e : this.entity.getIndirectPassengers()) if (e instanceof LivingEntity)
        {
            final boolean doBuffs = !buffs.isEmpty();
            if (doBuffs) for (final MobEffectInstance buff : buffs) ((LivingEntity) e).addEffect(buff);
            else((LivingEntity) e).curePotionEffects(stack);
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

        final float speedFactor = (float) (1 + Math.sqrt(this.pokemob.getPokedexEntry().getStatVIT()) / 10F);
        final float baseSpd = (float) (0.25f * this.throttle * speedFactor);
        float moveFwd = this.backInputDown ? -baseSpd / 2 : baseSpd;
        float moveUp = this.upInputDown ? baseSpd : this.downInputDown ? -baseSpd : 0;

        float pitch = controller.xRot;

        if (Math.abs(pitch) > 25 && this.followOwnerLook)
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
        moveUp *= 0.25;

        final boolean goUp = this.upInputDown || moveUp > 0;
        final boolean goDown = this.downInputDown || moveUp < 0;

        if (this.forwardInputDown || this.backInputDown)
        {
            move = true;
            float f = moveFwd;
            if (airSpeed) f *= config.flySpeedFactor;
            else if (waterSpeed) f *= config.surfSpeedFactor;
            else f *= config.groundSpeedFactor;
            if (fluidRestricted) f *= 0.1;
            if (shouldControl)
            {
                vx += Mth.sin(-this.entity.yRot * 0.017453292F) * f;
                vz += Mth.cos(this.entity.yRot * 0.017453292F) * f;
            }
        }
        if ((goUp || goDown) && verticalControl)
        {
            vy += moveUp;
            move = true;
        }
        else if (fluidRestricted)
        {
            vy += 0.05 * this.throttle;
            move = true;
        }
        else if (!verticalControl && !this.entity.isOnGround()) vy -= 0.1;

        if (this.inFluid && !(this.upInputDown || this.downInputDown))
        {
            double fraction = -1;
            if (this.entity.isInWater()) fraction = this.entity.getFluidHeight(FluidTags.WATER);
            if (this.entity.isInLava()) fraction = this.entity.getFluidHeight(FluidTags.LAVA);
            final double threshold = this.entity.getFluidJumpThreshold();
            if (fraction > threshold) vy += 0.05;
        }

        if (!this.entity.getPassengers().isEmpty())
        {
            this.pokemob.setHeading(controller.yRot);
            float f = moveFwd / 2;
            if (this.leftInputDown)
            {
                move = true;
                if (shouldControl)
                {
                    if (airSpeed) f *= config.flySpeedFactor;
                    else if (waterSpeed) f *= config.surfSpeedFactor;
                    else f *= config.groundSpeedFactor;
                    vx += Mth.cos(-this.entity.yRot * 0.017453292F) * f;
                    vz += Mth.sin(this.entity.yRot * 0.017453292F) * f;
                }
                else if (this.inFluid)
                {
                    f *= 0.1;
                    vx += Mth.cos(-this.entity.yRot * 0.017453292F) * f;
                    vz += Mth.sin(this.entity.yRot * 0.017453292F) * f;
                }
            }
            if (this.rightInputDown)
            {
                move = true;
                if (shouldControl)
                {
                    if (airSpeed) f *= config.flySpeedFactor;
                    else if (waterSpeed) f *= config.surfSpeedFactor;
                    else f *= config.groundSpeedFactor;
                    vx -= Mth.cos(-this.entity.yRot * 0.017453292F) * f;
                    vz -= Mth.sin(this.entity.yRot * 0.017453292F) * f;
                }
                else if (this.inFluid)
                {
                    f *= 0.1;
                    vx -= Mth.cos(-this.entity.yRot * 0.017453292F) * f;
                    vz -= Mth.sin(this.entity.yRot * 0.017453292F) * f;
                }
            }
        }
        if (!move)
        {
            vx *= 0.5;
            vz *= 0.5;
            if (verticalControl) vy *= 0.5;
        }
        this.entity.setDeltaMovement(vx, vy, vz);
    }

}
