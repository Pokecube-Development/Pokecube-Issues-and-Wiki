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
        TYPE = EntityType.Builder.of(EntityPokemobEgg::new, EntityClassification.CREATURE).noSummon()
                .fireImmune().sized(0.35f, 0.35f).build("egg");
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
        final int hatch = 1000 + this.getCommandSenderWorld().random.nextInt(PokecubeCore.getConfig().eggHatchTime);
        this.setAge(-hatch);
        this.init = true;
        this.setPersistenceRequired();
        this.delayBeforeCanPickup = 20;
    }

    @Override
    /** Called when the entity is attacked. */
    public boolean hurt(final DamageSource source, final float damage)
    {
        final Entity e = source.getDirectEntity();
        if (!this.getCommandSenderWorld().isClientSide && e instanceof PlayerEntity)
        {
            if (this.delayBeforeCanPickup > 0) return false;

            final ItemStack itemstack = this.getMainHandItem();
            final int i = itemstack.getCount();
            final PlayerEntity player = (PlayerEntity) e;
            if (this.mother != null && this.mother.getOwner() != player) BrainUtils.initiateCombat(this.mother
                    .getEntity(), player);
            if (i <= 0 || player.inventory.add(itemstack))
            {
                player.take(this, i);
                if (itemstack.isEmpty()) this.remove();
                return true;
            }
        }
        if (this.isInvulnerableTo(source)) return false;
        this.markHurt();
        return false;
    }

    public Entity getEggOwner()
    {
        final IPokemob pokemob = this.getPokemob(true);
        if (pokemob != null) return pokemob.getOwner();
        return null;
    }

    @Override
    public ItemStack getMainHandItem()
    {
        if (this.getCommandSenderWorld().isClientSide) return super.getMainHandItem();
        if (this.eggCache == null) this.eggCache = super.getMainHandItem();
        if (!this.eggCache.hasTag()) this.eggCache.setTag(new CompoundNBT());
        this.eggCache.getTag().putInt("timer", this.getAge());
        return this.eggCache;
    }

    public UUID getMotherId()
    {
        if (this.getMainHandItem() != null && this.getMainHandItem().hasTag()) if (this.getMainHandItem()
                .getTag().contains("motherId")) return UUID.fromString(this.getMainHandItem().getTag().getString(
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
        return this.getMainHandItem().copy();
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
                this.sounds.getEntity().setPos(this.getX(), this.getY(), this.getZ());
                return this.sounds;
            }
            this.sounds = ItemPokemobEgg.getFakePokemob(this.getCommandSenderWorld(), this.here, this.getMainHandItem());
            if (this.sounds == null) return null;
            this.sounds.getEntity().setLevel(this.getCommandSenderWorld());
            return this.sounds;
        }

        if (this.toHatch != null) return this.toHatch;
        this.toHatch = ItemPokemobEgg.make(this.getCommandSenderWorld(), this.getMainHandItem(), this);
        return this.toHatch;
    }

    public void incubateEgg()
    {
        if (this.tickCount != this.lastIncubate)
        {
            this.lastIncubate = this.tickCount;
            int i = this.getAge();
            i = i + 1;
            if (i > 0) i = 0;
            this.setAge(i);
        }
    }

    @Override
    public ActionResultType interactAt(final PlayerEntity player, final Vector3d pos, final Hand hand)
    {
        if (this.delayBeforeCanPickup > 0) return ActionResultType.FAIL;
        final ItemStack itemstack = this.getMainHandItem();
        final int i = itemstack.getCount();
        if (this.mother != null && this.mother.getOwner() != player) BrainUtils.initiateCombat(this.mother.getEntity(),
                player);
        if (i <= 0 || player.inventory.add(itemstack))
        {
            player.take(this, i);
            if (itemstack.isEmpty()) this.remove();
            return ActionResultType.SUCCESS;
        }
        return super.interactAt(player, pos, hand);
    }

    @Override
    public void setItemInHand(final Hand hand, final ItemStack stack)
    {
        super.setItemInHand(hand, stack);
        this.eggCache = stack;
    }

    public EntityPokemobEgg setToPos(final double d, final double d1, final double d2)
    {
        this.setPos(d, d1, d2);
        return this;
    }

    public EntityPokemobEgg setToPos(final Vector3 pos)
    {
        return this.setToPos(pos.x, pos.y, pos.z);
    }

    public EntityPokemobEgg setStack(final ItemStack stack)
    {
        this.setItemInHand(Hand.MAIN_HAND, stack);
        if (stack.hasTag() && stack.getTag().contains("timer")) this.setAge(stack.getTag().getInt("timer"));
        return this;
    }

    public EntityPokemobEgg setStackByParents(final Entity placer, final IPokemob father)
    {
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(placer);
        final ItemStack itemstack = ItemPokemobEgg.getEggStack(pokemob);
        ItemPokemobEgg.initStack(placer, father, itemstack);
        this.setItemInHand(Hand.MAIN_HAND, itemstack);
        return this;
    }

    /**
     * This is called when Entity's growing age timer reaches 0 (negative values
     * are considered as a child, positive as
     * an adult)
     */
    @Override
    protected void ageBoundaryReached()
    {
        if (!this.init) return;

        if (this.getMainHandItem().hasTag())
        {
            final CompoundNBT nbt = this.getMainHandItem().getTag();
            final boolean hasNest = nbt.contains("nestLoc");
            if (!hasNest) ItemPokemobEgg.tryImprint(this.getPokemob(true));
        }
        final EggEvent.PreHatch event = new EggEvent.PreHatch(this);
        MinecraftForge.EVENT_BUS.post(event);
        if (!event.isCanceled()) ItemPokemobEgg.spawn(this.getPokemob(true), this.getMainHandItem(), this
                .getCommandSenderWorld(), this);
        this.remove();
    }

    @Override
    public void tick()
    {
        this.here.set(this);
        if (net.minecraftforge.common.ForgeHooks.onLivingUpdate(this)) return;

        this.baseTick();
        this.aiStep();

        if (this.isInWater() || this.isInLava())
        {
            final Vector3d motion = this.getDeltaMovement();
            double dy = motion.y + 0.1;
            dy = Math.min(dy, 0.1);
            this.setDeltaMovement(motion.x, dy, motion.z);
        }

        if (this.getCommandSenderWorld().isClientSide) return;
        if (this.getMainHandItem().isEmpty() || this.getAge() > 0)
        {
            this.remove();
            return;
        }
        this.delayBeforeCanPickup--;
        if (this.random.nextInt(20 - this.getAge()) == 0)
        {
            final IPokemob mob = this.getPokemob(false);
            if (mob == null) this.remove();
            else mob.getEntity().playAmbientSound();
        }
        TileEntity te = this.here.getTileEntity(this.getCommandSenderWorld(), Direction.DOWN);
        if (te == null) te = this.here.getTileEntity(this.getCommandSenderWorld());
        if (te instanceof HopperTileEntity)
        {
            final HopperTileEntity hopper = (HopperTileEntity) te;
            final ItemEntity item = new ItemEntity(this.getCommandSenderWorld(), this.getX(), this.getY(), this
                    .getZ(), this.getMainHandItem());
            if (HopperTileEntity.addItem(hopper, item)) this.remove();
        }
    }

    @Override
    public AgeableEntity getBreedOffspring(final ServerWorld p_241840_1_, final AgeableEntity p_241840_2_)
    {
        // This is the child.
        return null;
    }
}