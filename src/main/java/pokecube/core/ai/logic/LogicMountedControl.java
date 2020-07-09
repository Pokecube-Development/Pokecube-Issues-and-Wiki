package pokecube.core.ai.logic;

import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.DimensionType;
import net.minecraftforge.server.permission.IPermissionHandler;
import net.minecraftforge.server.permission.PermissionAPI;
import net.minecraftforge.server.permission.context.PlayerContext;
import pokecube.core.PokecubeCore;
import pokecube.core.database.PokedexEntry;
import pokecube.core.handlers.Config;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.utils.Permissions;

/**
 * This manages the ridden controls of the pokemob. The booleans are set on the
 * client side, then sent via a packet to the server, and then the mob is moved
 * accordingly.
 */
public class LogicMountedControl extends LogicBase
{
    public static Set<DimensionType> BLACKLISTED = Sets.newHashSet();

    public boolean leftInputDown    = false;
    public boolean rightInputDown   = false;
    public boolean forwardInputDown = false;
    public boolean backInputDown    = false;
    public boolean upInputDown      = false;
    public boolean downInputDown    = false;
    public boolean followOwnerLook  = false;
    public double  throttle         = 0.5;

    public LogicMountedControl(final IPokemob pokemob_)
    {
        super(pokemob_);
        if (this.entity.getPersistentData().contains("pokecube:mob_throttle")) this.throttle = this.entity
                .getPersistentData().getDouble("pokecube:mob_throttle");
    }

    @Override
    public void tick(final World world)
    {
        final Entity rider = this.entity.getControllingPassenger();
        this.entity.stepHeight = 1.1f;
        this.pokemob.setGeneralState(GeneralStates.CONTROLLED, rider != null);

        if (rider == null) return;

        final Config config = PokecubeCore.getConfig();
        boolean move = false;
        this.entity.rotationYaw = this.pokemob.getHeading();
        boolean shouldControl = this.entity.onGround || this.pokemob.floats();
        boolean verticalControl = false;
        boolean waterSpeed = false;
        boolean airSpeed = !this.entity.onGround;

        boolean canFly = this.pokemob.canUseFly();
        boolean canSurf = this.pokemob.canUseSurf();
        boolean canDive = this.pokemob.canUseDive();

        ServerPlayerEntity player = null;

        if (rider instanceof ServerPlayerEntity)
        {
            player = (ServerPlayerEntity) rider;
            final IPermissionHandler handler = PermissionAPI.getPermissionHandler();
            final PlayerContext context = new PlayerContext(player);
            final PokedexEntry entry = this.pokemob.getPokedexEntry();
            if (config.permsFly && canFly && !handler.hasPermission(player.getGameProfile(), Permissions.FLYPOKEMOB,
                    context)) canFly = false;
            if (config.permsFlySpecific && canFly && !handler.hasPermission(player.getGameProfile(),
                    Permissions.FLYSPECIFIC.get(entry), context)) canFly = false;
            if (config.permsSurf && canSurf && !handler.hasPermission(player.getGameProfile(), Permissions.SURFPOKEMOB,
                    context)) canSurf = false;
            if (config.permsSurfSpecific && canSurf && !handler.hasPermission(player.getGameProfile(),
                    Permissions.SURFSPECIFIC.get(entry), context)) canSurf = false;
            if (config.permsDive && canDive && !handler.hasPermission(player.getGameProfile(), Permissions.DIVEPOKEMOB,
                    context)) canDive = false;
            if (config.permsDiveSpecific && canDive && !handler.hasPermission(player.getGameProfile(),
                    Permissions.DIVESPECIFIC.get(entry), context)) canDive = false;
        }
        if (canFly) canFly = !LogicMountedControl.BLACKLISTED.contains(world.getDimension().getType());
        if (canFly)
        {
            shouldControl = verticalControl = PokecubeCore.getConfig().flyEnabled || shouldControl;
            if (verticalControl) this.entity.setNoGravity(true);
        }
        if ((canSurf || canDive) && (waterSpeed = this.entity.isInWater()))
            shouldControl = verticalControl = PokecubeCore.getConfig().surfEnabled || shouldControl;

        if (waterSpeed) airSpeed = false;

        final Entity controller = rider;
        if (this.pokemob.getPokedexEntry().shouldDive)
        {
            final EffectInstance vision = new EffectInstance(Effects.NIGHT_VISION, 300, 1, true, false);
            final EffectInstance breathing = new EffectInstance(Effects.WATER_BREATHING, 300, 1, true, false);
            final ItemStack stack = new ItemStack(Blocks.BARRIER);
            vision.setCurativeItems(Lists.newArrayList(stack));
            breathing.setCurativeItems(Lists.newArrayList(stack));
            for (final Entity e : this.entity.getRecursivePassengers())
                if (e instanceof LivingEntity) if (this.entity.isInWater())
                {
                    ((LivingEntity) e).addPotionEffect(vision);
                    ((LivingEntity) e).addPotionEffect(breathing);
                }
                else((LivingEntity) e).curePotionEffects(stack);
        }
        final float speedFactor = (float) (1 + Math.sqrt(this.pokemob.getPokedexEntry().getStatVIT()) / 10F);
        final float moveSpeed = (float) (0.25f * this.throttle * speedFactor);
        double vx = this.entity.getMotion().x;
        double vy = this.entity.getMotion().y;
        double vz = this.entity.getMotion().z;
        if (this.forwardInputDown)
        {
            move = true;
            float f = moveSpeed / 2;

            if (airSpeed) f *= config.flySpeedFactor;
            else if (waterSpeed) f *= config.surfSpeedFactor;
            else f *= config.groundSpeedFactor;

            if (shouldControl)
            {
                if (!this.entity.onGround) f *= 2;
                vx += MathHelper.sin(-this.entity.rotationYaw * 0.017453292F) * f;
                vz += MathHelper.cos(this.entity.rotationYaw * 0.017453292F) * f;
            }
            else if (this.entity.isInLava() || this.entity.isInWater())
            {
                f *= 0.1;
                vx += MathHelper.sin(-this.entity.rotationYaw * 0.017453292F) * f;
                vz += MathHelper.cos(this.entity.rotationYaw * 0.017453292F) * f;
            }
        }
        if (this.backInputDown)
        {
            move = true;
            float f = -moveSpeed / 4;
            if (shouldControl)
            {
                if (airSpeed) f *= config.flySpeedFactor;
                else if (waterSpeed) f *= config.surfSpeedFactor;
                else f *= config.groundSpeedFactor;
                vx += MathHelper.sin(-this.entity.rotationYaw * 0.017453292F) * f;
                vz += MathHelper.cos(this.entity.rotationYaw * 0.017453292F) * f;
            }
        }
        if (this.upInputDown) if (this.entity.onGround && !verticalControl)
        {
            this.entity.isAirBorne = true;
            // TODO somehow configure this jump value.
            vy += 1;
            net.minecraftforge.common.ForgeHooks.onLivingJump(this.entity);
        }
        else if (verticalControl) vy += 0.1 * this.throttle;
        else if (this.entity.isInLava() || this.entity.isInWater()) vy += 0.05 * this.throttle;
        if (this.downInputDown)
        {
            if (verticalControl && !this.entity.onGround) vy -= 0.1 * this.throttle;
        }
        else if (!verticalControl && !this.entity.onGround) vy -= 0.1;
        if (!this.followOwnerLook)
        {// TODO some way to make this change based on how long button is held?
            if (this.leftInputDown) this.pokemob.setHeading(this.pokemob.getHeading() - 5);
            if (this.rightInputDown) this.pokemob.setHeading(this.pokemob.getHeading() + 5);
        }
        else if (!this.entity.getPassengers().isEmpty())
        {
            this.pokemob.setHeading(controller.rotationYaw);
            float f = moveSpeed / 2;
            if (this.leftInputDown)
            {
                move = true;
                if (shouldControl)
                {
                    if (!this.entity.onGround) f *= 2;
                    if (airSpeed) f *= config.flySpeedFactor;
                    else if (waterSpeed) f *= config.surfSpeedFactor;
                    else f *= config.groundSpeedFactor;
                    vx += MathHelper.cos(-this.entity.rotationYaw * 0.017453292F) * f;
                    vz += MathHelper.sin(this.entity.rotationYaw * 0.017453292F) * f;
                }
                else if (this.entity.isInLava() || this.entity.isInWater())
                {
                    f *= 0.1;
                    vx += MathHelper.cos(-this.entity.rotationYaw * 0.017453292F) * f;
                    vz += MathHelper.sin(this.entity.rotationYaw * 0.017453292F) * f;
                }
            }
            if (this.rightInputDown)
            {
                move = true;
                if (shouldControl)
                {
                    if (!this.entity.onGround) f *= 2;
                    if (airSpeed) f *= config.flySpeedFactor;
                    else if (waterSpeed) f *= config.surfSpeedFactor;
                    else f *= config.groundSpeedFactor;
                    vx -= MathHelper.cos(-this.entity.rotationYaw * 0.017453292F) * f;
                    vz -= MathHelper.sin(this.entity.rotationYaw * 0.017453292F) * f;
                }
                else if (this.entity.isInLava() || this.entity.isInWater())
                {
                    f *= 0.1;
                    vx -= MathHelper.cos(-this.entity.rotationYaw * 0.017453292F) * f;
                    vz -= MathHelper.sin(this.entity.rotationYaw * 0.017453292F) * f;
                }
            }
        }
        if (!move)
        {
            vx *= 0.5;
            vz *= 0.5;
        }
        this.entity.setMotion(vx, vy, vz);
    }

}
