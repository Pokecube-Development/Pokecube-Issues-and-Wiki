package pokecube.core.items.pokemobeggs;

import java.util.UUID;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.events.EggEvent;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import thut.api.maths.Vector3;

/** @author Manchou */
public class EntityPokemobEgg extends AgeableMob
{
    public static final EntityType<EntityPokemobEgg> TYPE;

    static
    {
        TYPE = EntityType.Builder.of(EntityPokemobEgg::new, MobCategory.CREATURE).noSummon().fireImmune()
                .sized(0.35f, 0.35f).build("egg");
    }

    int delayBeforeCanPickup = 0;
    int lastIncubate = 0;
    public IPokemob mother = null;
    Vector3 here = new Vector3();
    private ItemStack eggCache = null;
    boolean init = false;

    private IPokemob toHatch = null;
    private IPokemob sounds = null;

    /**
     * Do not call this, this is here only for vanilla reasons
     *
     * @param world
     */
    public EntityPokemobEgg(final EntityType<EntityPokemobEgg> type, final Level world)
    {
        super(type, world);
        final int hatch = 1000 + this.getLevel().random.nextInt(PokecubeCore.getConfig().eggHatchTime);
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
        if (!this.getLevel().isClientSide && e instanceof Player)
        {
            if (this.delayBeforeCanPickup > 0) return false;

            final ItemStack itemstack = this.getMainHandItem();
            final int i = itemstack.getCount();
            final Player player = (Player) e;
            if (this.mother != null && this.mother.getOwner() != player)
                BrainUtils.initiateCombat(this.mother.getEntity(), player);
            if (i <= 0 || player.getInventory().add(itemstack))
            {
                player.take(this, i);
                if (itemstack.isEmpty()) this.discard();
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
        if (this.getLevel().isClientSide) return super.getMainHandItem();
        if (this.eggCache == null) this.eggCache = super.getMainHandItem();
        if (!this.eggCache.hasTag()) this.eggCache.setTag(new CompoundTag());
        this.eggCache.getTag().putInt("timer", this.getAge());
        return this.eggCache;
    }

    public UUID getMotherId()
    {
        if (this.getMainHandItem() != null && this.getMainHandItem().hasTag())
            if (this.getMainHandItem().getTag().contains("motherId"))
                return UUID.fromString(this.getMainHandItem().getTag().getString("motherId"));
        return null;
    }

    /**
     * Called when a user uses the creative pick block button on this entity.
     *
     * @param target The full target the player is looking at
     * @return A ItemStack to add to the player's inventory, Null if nothing
     *         should be added.
     */
    @Override
    public ItemStack getPickedResult(final HitResult target)
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
            this.sounds = ItemPokemobEgg.getFakePokemob(this.getLevel(), this.here, this.getMainHandItem());
            if (this.sounds == null) return null;
            this.sounds.getEntity().level = this.getLevel();
            return this.sounds;
        }

        if (this.toHatch != null) return this.toHatch;
        this.toHatch = ItemPokemobEgg.make(this.getLevel(), this.getMainHandItem(), this);
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
    public InteractionResult interactAt(final Player player, final Vec3 pos, final InteractionHand hand)
    {
        if (this.delayBeforeCanPickup > 0) return InteractionResult.FAIL;
        final ItemStack itemstack = this.getMainHandItem();
        final int i = itemstack.getCount();
        if (this.mother != null && this.mother.getOwner() != player)
            BrainUtils.initiateCombat(this.mother.getEntity(), player);
        if (i <= 0 || player.getInventory().add(itemstack))
        {
            player.take(this, i);
            if (itemstack.isEmpty()) this.discard();
            return InteractionResult.SUCCESS;
        }
        return super.interactAt(player, pos, hand);
    }

    @Override
    public void setItemInHand(final InteractionHand hand, final ItemStack stack)
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
        this.setItemInHand(InteractionHand.MAIN_HAND, stack);
        if (stack.hasTag() && stack.getTag().contains("timer")) this.setAge(stack.getTag().getInt("timer"));
        return this;
    }

    public EntityPokemobEgg setStackByParents(final Entity placer, final IPokemob father)
    {
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(placer);
        final ItemStack itemstack = ItemPokemobEgg.getEggStack(pokemob);
        ItemPokemobEgg.initStack(placer, father, itemstack);
        this.setItemInHand(InteractionHand.MAIN_HAND, itemstack);
        return this;
    }

    /**
     * This is called when Entity's growing age timer reaches 0 (negative values
     * are considered as a child, positive as an adult)
     */
    @Override
    protected void ageBoundaryReached()
    {
        if (!this.init) return;

        if (this.getMainHandItem().hasTag())
        {
            final CompoundTag nbt = this.getMainHandItem().getTag();
            final boolean hasNest = nbt.contains("nestLoc");
            if (!hasNest) ItemPokemobEgg.tryImprint(this.getPokemob(true));
        }
        final EggEvent.PreHatch event = new EggEvent.PreHatch(this);
        MinecraftForge.EVENT_BUS.post(event);
        if (!event.isCanceled())
            ItemPokemobEgg.spawn(this.getPokemob(true), this.getMainHandItem(), this.getLevel(), this);
        this.discard();
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
            final Vec3 motion = this.getDeltaMovement();
            double dy = motion.y + 0.1;
            dy = Math.min(dy, 0.1);
            this.setDeltaMovement(motion.x, dy, motion.z);
        }

        if (this.getLevel().isClientSide) return;
        if (this.getMainHandItem().isEmpty() || this.getAge() > 0)
        {
            this.discard();
            return;
        }
        this.delayBeforeCanPickup--;
        if (this.random.nextInt(20 - this.getAge()) == 0)
        {
            final IPokemob mob = this.getPokemob(false);
            if (mob == null) this.discard();
            else mob.getEntity().playAmbientSound();
        }
        BlockEntity te = this.here.getTileEntity(this.getLevel(), Direction.DOWN);
        if (te == null) te = this.here.getTileEntity(this.getLevel());
        if (te instanceof HopperBlockEntity)
        {
            final HopperBlockEntity hopper = (HopperBlockEntity) te;
            final ItemEntity item = new ItemEntity(this.getLevel(), this.getX(), this.getY(), this.getZ(),
                    this.getMainHandItem());
            if (HopperBlockEntity.addItem(hopper, item)) this.discard();
        }
    }

    @Override
    public AgeableMob getBreedOffspring(final ServerLevel p_241840_1_, final AgeableMob p_241840_2_)
    {
        // This is the child.
        return null;
    }
}