package pokecube.core.ai.logic;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.entity.PartEntity;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.api.entity.pokemob.ai.LogicStates;
import pokecube.api.moves.IMoveConstants.AIRoutine;
import pokecube.core.PokecubeCore;
import pokecube.core.init.Config;
import pokecube.core.utils.PermNodes;
import pokecube.core.utils.Permissions;

/**
 * This manages the ridden controls of the pokemob. The booleans are set on the
 * client side, then sent via a packet to the server, and then the mob is moved
 * accordingly.
 */
public class LogicMountedControl extends LogicBase
{
    private static final UUID UID = UUID.fromString("4454b0d8-75ef-4689-8fce-daab61a7e1b0");
    private AttributeModifier riddenStep = null;

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
        this.riddenStep = new AttributeModifier(UID, "pokecube:ridden_step", 0.75, Operation.ADDITION);
    }

    public boolean blocksPathing()
    {
        final Entity rider = this.entity.getControllingPassenger();
        if (this.pokemob.getLogicState(LogicStates.SITTING)) return true;
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
        if (this.canFly) this.canFly = !LogicMountedControl.BLACKLISTED.contains(rider.getLevel().dimension());
    }

    public boolean hasInput()
    {
        return this.input;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void tick(final Level world)
    {
        super.tick(world);
        final Entity rider = this.entity.getControllingPassenger();
        moveUp = moveSide = moveFwd = 0;
        this.pokemob.setGeneralState(GeneralStates.CONTROLLED, rider != null);
        AttributeInstance stepHeightAttribute = this.entity.getAttribute(ForgeMod.STEP_HEIGHT_ADDITION.get());
        if (rider == null)
        {
            stepHeightAttribute.removeModifier(UID);
            if (this.wasRiding && this.pokemob.isRoutineEnabled(AIRoutine.AIRBORNE))
            {
                this.entity.setNoGravity(false);
                this.wasRiding = false;
            }
            return;
        }
        if (!stepHeightAttribute.hasModifier(riddenStep)) stepHeightAttribute.addTransientModifier(riddenStep);
        if (entity.getParts() != null)
        {
            for (PartEntity<?> e : entity.getParts())
            {
                e.maxUpStep = this.entity.getStepHeight();
            }
        }

        this.wasRiding = true;
        this.entity.yRot = this.pokemob.getHeading();

        shouldControl = this.entity.isOnGround() || this.pokemob.canUseFly();
        verticalControl = false;
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

        shouldControl |= verticalControl || this.inFluid;

        this.entity.setNoGravity(verticalControl);

        for (final Entity e : this.entity.getIndirectPassengers()) if (e instanceof LivingEntity living)
        {
            final boolean doBuffs = !buffs.isEmpty();
            if (doBuffs) for (final MobEffectInstance buff : buffs) living.addEffect(buff);
            else living.curePotionEffects(stack);
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
        final Config config = PokecubeCore.getConfig();
        float speedFactor = (float) (1 + Math.sqrt(this.pokemob.getPokedexEntry().getStatVIT()) / 10F);

        speedFactor *= airSpeed ? config.flySpeedFactor
                : waterSpeed ? config.surfSpeedFactor * 0.125f : config.groundSpeedFactor;

        float baseSpd = (float) (0.5f * this.throttle * speedFactor);

        if (fluidRestricted) baseSpd *= 0.5f;

        moveFwd = this.backInputDown ? -baseSpd / 2 : this.forwardInputDown ? baseSpd : 0;
        moveSide = this.leftInputDown ? baseSpd : this.rightInputDown ? -baseSpd : 0;
        moveUp = this.upInputDown ? baseSpd : this.downInputDown ? -baseSpd : 0;
        float pitch = controller.xRot;

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
            this.pokemob.setHeading(controller.yRot);
        }
    }

}
