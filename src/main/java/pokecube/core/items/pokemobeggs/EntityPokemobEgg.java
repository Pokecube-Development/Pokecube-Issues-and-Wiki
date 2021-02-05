package pokecube.core.items.pokemobeggs;

import java.util.UUID;

import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.HopperTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.events.EggEvent;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import thut.api.maths.Vector3;

/** @author Manchou */
public class EntityPokemobEgg extends AgeableEntity
{
    public static final EntityType<EntityPokemobEgg> TYPE;

    static
    {
        TYPE = EntityType.Builder.create(EntityPokemobEgg::new, EntityClassification.CREATURE).disableSummoning()
                .immuneToFire().size(0.35f, 0.35f).build("egg");
    }

    int               delayBeforeCanPickup = 0;
    int               lastIncubate         = 0;
    public IPokemob   mother               = null;
    Vector3           here                 = Vector3.getNewVector();
    private ItemStack eggCache             = null;
    boolean           init                 = false;

    private IPokemob toHatch = null;
    private IPokemob sounds  = null;

    /**
     * Do not call this, this is here only for vanilla reasons
     *
     * @param world
     */
    public EntityPokemobEgg(final EntityType<EntityPokemobEgg> type, final World world)
    {
        super(type, world);
        final int hatch = 1000 + this.getEntityWorld().rand.nextInt(PokecubeCore.getConfig().eggHatchTime);
        this.setGrowingAge(-hatch);
        this.init = true;
        this.enablePersistence();
        this.delayBeforeCanPickup = 20;
    }

    @Override
    /** Called when the entity is attacked. */
    public boolean attackEntityFrom(final DamageSource source, final float damage)
    {
        final Entity e = source.getImmediateSource();
        if (!this.getEntityWorld().isRemote && e instanceof PlayerEntity)
        {
            if (this.delayBeforeCanPickup > 0) return false;

            final ItemStack itemstack = this.getHeldItemMainhand();
            final int i = itemstack.getCount();
            final PlayerEntity player = (PlayerEntity) e;
            if (this.mother != null && this.mother.getOwner() != player) BrainUtils.initiateCombat(this.mother
                    .getEntity(), player);
            if (i <= 0 || player.inventory.addItemStackToInventory(itemstack))
            {
                player.onItemPickup(this, i);
                if (itemstack.isEmpty()) this.remove();
                return true;
            }
        }
        if (this.isInvulnerableTo(source)) return false;
        this.markVelocityChanged();
        return false;
    }

    public Entity getEggOwner()
    {
        final IPokemob pokemob = this.getPokemob(true);
        if (pokemob != null) return pokemob.getOwner();
        return null;
    }

    @Override
    public ItemStack getHeldItemMainhand()
    {
        if (this.getEntityWorld().isRemote) return super.getHeldItemMainhand();
        if (this.eggCache == null) this.eggCache = super.getHeldItemMainhand();
        if (!this.eggCache.hasTag()) this.eggCache.setTag(new CompoundNBT());
        this.eggCache.getTag().putInt("timer", this.getGrowingAge());
        return this.eggCache;
    }

    public UUID getMotherId()
    {
        if (this.getHeldItemMainhand() != null && this.getHeldItemMainhand().hasTag()) if (this.getHeldItemMainhand()
                .getTag().contains("motherId")) return UUID.fromString(this.getHeldItemMainhand().getTag().getString(
                        "motherId"));
        return null;
    }

    /**
     * Called when a user uses the creative pick block button on this entity.
     *
     * @param target
     *            The full target the player is looking at
     * @return A ItemStack to add to the player's inventory, Null if nothing
     *         should be added.
     */
    @Override
    public ItemStack getPickedResult(final RayTraceResult target)
    {
        return this.getHeldItemMainhand().copy();
    }

    /**
     * Returns a generic pokemob instance with the data of the one in the egg,
     * this is not to be used for spawning into the world.
     *
     * @return
     */
    public IPokemob getPokemob(final boolean real)
    {
        if (!real)
        {
            if (this.sounds != null)
            {
                this.sounds.getEntity().setPosition(this.getPosX(), this.getPosY(), this.getPosZ());
                return this.sounds;
            }
            this.sounds = ItemPokemobEgg.getFakePokemob(this.getEntityWorld(), this.here, this.getHeldItemMainhand());
            if (this.sounds == null) return null;
            this.sounds.getEntity().setWorld(this.getEntityWorld());
            return this.sounds;
        }

        if (this.toHatch != null) return this.toHatch;
        this.toHatch = ItemPokemobEgg.make(this.getEntityWorld(), this.getHeldItemMainhand(), this);
        return this.toHatch;
    }

    public void incubateEgg()
    {
        if (this.ticksExisted != this.lastIncubate)
        {
            this.lastIncubate = this.ticksExisted;
            int i = this.getGrowingAge();
            i = i + 1;
            if (i > 0) i = 0;
            this.setGrowingAge(i);
        }
    }

    @Override
    public ActionResultType applyPlayerInteraction(final PlayerEntity player, final Vector3d pos, final Hand hand)
    {
        if (this.delayBeforeCanPickup > 0) return ActionResultType.FAIL;
        final ItemStack itemstack = this.getHeldItemMainhand();
        final int i = itemstack.getCount();
        if (this.mother != null && this.mother.getOwner() != player) BrainUtils.initiateCombat(this.mother.getEntity(),
                player);
        if (i <= 0 || player.inventory.addItemStackToInventory(itemstack))
        {
            player.onItemPickup(this, i);
            if (itemstack.isEmpty()) this.remove();
            return ActionResultType.SUCCESS;
        }
        return super.applyPlayerInteraction(player, pos, hand);
    }

    @Override
    public void setHeldItem(final Hand hand, final ItemStack stack)
    {
        super.setHeldItem(hand, stack);
        this.eggCache = stack;
    }

    public EntityPokemobEgg setPos(final double d, final double d1, final double d2)
    {
        this.setPosition(d, d1, d2);
        return this;
    }

    public EntityPokemobEgg setPos(final Vector3 pos)
    {
        return this.setPos(pos.x, pos.y, pos.z);
    }

    public EntityPokemobEgg setStack(final ItemStack stack)
    {
        this.setHeldItem(Hand.MAIN_HAND, stack);
        if (stack.hasTag() && stack.getTag().contains("timer")) this.setGrowingAge(stack.getTag().getInt("timer"));
        return this;
    }

    public EntityPokemobEgg setStackByParents(final Entity placer, final IPokemob father)
    {
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(placer);
        final ItemStack itemstack = ItemPokemobEgg.getEggStack(pokemob);
        ItemPokemobEgg.initStack(placer, father, itemstack);
        this.setHeldItem(Hand.MAIN_HAND, itemstack);
        return this;
    }

    /**
     * This is called when Entity's growing age timer reaches 0 (negative values
     * are considered as a child, positive as
     * an adult)
     */
    @Override
    protected void onGrowingAdult()
    {
        if (!this.init) return;

        if (this.getHeldItemMainhand().hasTag())
        {
            final CompoundNBT nbt = this.getHeldItemMainhand().getTag();
            final boolean hasNest = nbt.contains("nestLoc");
            if (!hasNest) ItemPokemobEgg.tryImprint(this.getPokemob(true));
        }
        final EggEvent.PreHatch event = new EggEvent.PreHatch(this);
        MinecraftForge.EVENT_BUS.post(event);
        if (!event.isCanceled()) ItemPokemobEgg.spawn(this.getPokemob(true), this.getHeldItemMainhand(), this
                .getEntityWorld(), this);
        this.remove();
    }

    @Override
    public void tick()
    {
        this.here.set(this);
        if (net.minecraftforge.common.ForgeHooks.onLivingUpdate(this)) return;

        this.baseTick();
        this.livingTick();

        if (this.isInWater() || this.isInLava())
        {
            final Vector3d motion = this.getMotion();
            double dy = motion.y + 0.1;
            dy = Math.min(dy, 0.1);
            this.setMotion(motion.x, dy, motion.z);
        }

        if (this.getEntityWorld().isRemote) return;
        if (this.getHeldItemMainhand().isEmpty() || this.getGrowingAge() > 0)
        {
            this.remove();
            return;
        }
        this.delayBeforeCanPickup--;
        if (this.rand.nextInt(20 - this.getGrowingAge()) == 0)
        {
            final IPokemob mob = this.getPokemob(false);
            if (mob == null) this.remove();
            else mob.getEntity().playAmbientSound();
        }
        TileEntity te = this.here.getTileEntity(this.getEntityWorld(), Direction.DOWN);
        if (te == null) te = this.here.getTileEntity(this.getEntityWorld());
        if (te instanceof HopperTileEntity)
        {
            final HopperTileEntity hopper = (HopperTileEntity) te;
            final ItemEntity item = new ItemEntity(this.getEntityWorld(), this.getPosX(), this.getPosY(), this
                    .getPosZ(), this.getHeldItemMainhand());
            if (HopperTileEntity.captureItem(hopper, item)) this.remove();
        }
    }

    @Override
    public AgeableEntity func_241840_a(final ServerWorld p_241840_1_, final AgeableEntity p_241840_2_)
    {
        // This is the child.
        return null;
    }
}