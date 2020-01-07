package pokecube.core.items.pokecubes;

import java.util.UUID;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.HandSide;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.server.permission.IPermissionHandler;
import net.minecraftforge.server.permission.PermissionAPI;
import net.minecraftforge.server.permission.context.PlayerContext;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.abilities.AbilityManager;
import pokecube.core.events.pokemob.CaptureEvent;
import pokecube.core.events.pokemob.CaptureEvent.Pre;
import pokecube.core.events.pokemob.SpawnEvent.SendOut;
import pokecube.core.handlers.Config;
import pokecube.core.interfaces.IPokecube;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.HappinessType;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import pokecube.core.utils.Permissions;
import pokecube.core.utils.TagNames;
import pokecube.core.utils.Tools;
import thut.api.maths.Vector3;
import thut.core.common.commands.CommandTools;

public abstract class EntityPokecubeBase extends LivingEntity implements IProjectile
{
    public static final String CUBETIMETAG = "lastCubeTime";

    public static SoundEvent POKECUBESOUND;

    static final DataParameter<Integer> ENTITYID = EntityDataManager.<Integer> createKey(EntityPokecubeBase.class,
            DataSerializers.VARINT);

    private static final DataParameter<ItemStack> ITEM      = EntityDataManager.<ItemStack> createKey(
            EntityPokecubeBase.class, DataSerializers.ITEMSTACK);
    static final DataParameter<Boolean>           RELEASING = EntityDataManager.<Boolean> createKey(
            EntityPokecubeBase.class, DataSerializers.BOOLEAN);
    static final DataParameter<Integer>           TIME      = EntityDataManager.<Integer> createKey(
            EntityPokecubeBase.class, DataSerializers.VARINT);
    public static boolean                         SEEKING   = true;

    public static boolean canCaptureBasedOnConfigs(final IPokemob pokemob)
    {
        if (PokecubeCore.getConfig().captureDelayTillAttack) return !pokemob.getCombatState(CombatStates.NOITEMUSE);
        final long lastAttempt = pokemob.getEntity().getPersistentData().getLong(EntityPokecubeBase.CUBETIMETAG);
        final boolean capture = lastAttempt <= pokemob.getEntity().getEntityWorld().getGameTime();
        if (capture) pokemob.getEntity().getPersistentData().remove(EntityPokecubeBase.CUBETIMETAG);
        return capture;
    }

    public static void setNoCaptureBasedOnConfigs(final IPokemob pokemob)
    {

        if (PokecubeCore.getConfig().captureDelayTillAttack) pokemob.setCombatState(CombatStates.NOITEMUSE, true);
        else pokemob.getEntity().getPersistentData().putLong(EntityPokecubeBase.CUBETIMETAG, pokemob.getEntity()
                .getEntityWorld().getGameTime() + PokecubeCore.getConfig().captureDelayTicks);
    }

    /** Seems to be some sort of timer for animating an arrow. */
    public int              arrowShake;
    /** 1 if the player can pick up the arrow */
    public int              canBePickedUp;
    public boolean          isLoot    = false;
    public ResourceLocation lootTable = null;
    protected int           inData;
    protected boolean       inGround;
    public UUID             shooter;
    public LivingEntity     shootingEntity;

    public double       speed          = 2;
    public LivingEntity targetEntity;
    public Vector3      targetLocation = Vector3.getNewVector();

    /** The owner of this arrow. */
    protected int      ticksInGround;
    protected Block    tile;
    protected BlockPos tilePos;
    public int         tilt = -1;
    protected Vector3  v0   = Vector3.getNewVector();
    protected Vector3  v1   = Vector3.getNewVector();

    private int    xTile   = -1;
    private int    yTile   = -1;
    private int    zTile   = -1;
    public int     throwableShake;
    private Entity ignoreEntity;
    private int    ignoreTime;
    public boolean seeking = EntityPokecubeBase.SEEKING;

    NonNullList<ItemStack> stuff = NonNullList.create();

    public EntityPokecubeBase(final EntityType<? extends EntityPokecubeBase> type, final World worldIn)
    {
        super(type, worldIn);
    }

    /** Called when the entity is attacked. */
    @Override
    public boolean attackEntityFrom(final DamageSource source, final float damage)
    {
        if (this.isLoot || this.isReleasing()) return false;
        if (source.getImmediateSource() instanceof ServerPlayerEntity)
        {
            Tools.giveItem((PlayerEntity) source.getImmediateSource(), this.getItem());
            this.remove();
        }
        if (source == DamageSource.OUT_OF_WORLD)
        {
            if (PokecubeManager.isFilled(this.getItem()))
            {
                final IPokemob mob = CapabilityPokemob.getPokemobFor(this.sendOut(true));
                if (mob != null) mob.onRecall();
            }
            this.remove();
        }
        return false;
    }

    protected void captureAttempt(final Entity e)
    {
        if (e.getEntityWorld().isRemote) return;
        final IPokemob hitten = CapabilityPokemob.getPokemobFor(e);
        ServerWorld world = (ServerWorld) getEntityWorld();
        if (hitten != null)
        {
            if (this.shootingEntity != null && hitten.getOwner() == this.shootingEntity) return;

            final int tiltBak = this.tilt;
            final CaptureEvent.Pre capturePre = new Pre(hitten, this);
            PokecubeCore.POKEMOB_BUS.post(capturePre);
            if (capturePre.isCanceled() || capturePre.getResult() == Result.DENY)
            {
                if (this.tilt != tiltBak)
                {
                    if (this.tilt == 5) this.setTime(10);
                    else this.setTime(20 * this.tilt);
                    hitten.setPokecube(this.getItem());
                    this.setItem(PokecubeManager.pokemobToItem(hitten));
                    PokecubeManager.setTilt(this.getItem(), this.tilt);
                    final Vector3 v = Vector3.getNewVector();
                    v.set(this).addTo(0, hitten.getPokedexEntry().height / 2, 0).moveEntity(this);
                    world.removeEntityComplete(hitten.getEntity(), false);
                    this.setMotion(0, 0.1, 0);
                }
            }
            else
            {
                final int n = Tools.computeCatchRate(hitten, PokecubeItems.getCubeId(this.getItem()));
                this.tilt = n;

                if (n == 5) this.setTime(10);
                else this.setTime(20 * n);

                hitten.setPokecube(this.getItem());
                this.setItem(PokecubeManager.pokemobToItem(hitten));
                PokecubeManager.setTilt(this.getItem(), n);
                final Vector3 v = Vector3.getNewVector();
                v.set(this).addTo(0, hitten.getPokedexEntry().height / 2, 0).moveEntity(this);
                world.removeEntityComplete(hitten.getEntity(), false);
                this.setMotion(0, 0.1, 0);
            }
        }
        else if (e instanceof MobEntity && this.getItem().getItem() instanceof IPokecube)
        {
            final IPokecube cube = (IPokecube) this.getItem().getItem();
            final MobEntity mob = (MobEntity) e;
            int n = 0;
            rate:
            {
                final int catchRate = 250;// TODO configs for this?
                final double cubeBonus = cube.getCaptureModifier(mob, PokecubeItems.getCubeId(this.getItem()));
                final double statusbonus = 1;// TODO statuses for mobs?
                final double a = Tools.getCatchRate(mob.getMaxHealth(), mob.getHealth(), catchRate, cubeBonus,
                        statusbonus);
                if (a > 255)
                {
                    n = 5;
                    break rate;
                }
                final double b = 1048560 / Math.sqrt(Math.sqrt(16711680 / a));

                if (this.rand.nextInt(65535) <= b) n++;

                if (this.rand.nextInt(65535) <= b) n++;

                if (this.rand.nextInt(65535) <= b) n++;

                if (this.rand.nextInt(65535) <= b) n++;
            }
            this.tilt = n;

            if (n == 5) this.setTime(10);
            else this.setTime(20 * n);
            final ItemStack mobStack = this.getItem().copy();
            PokecubeManager.addToCube(mobStack, mob);
            this.setItem(mobStack);
            PokecubeManager.setTilt(this.getItem(), n);
            final Vector3 v = Vector3.getNewVector();
            v.set(this).addTo(0, mob.getHeight() / 2, 0).moveEntity(this);
            world.removeEntityComplete(mob, false);
            this.setMotion(0, 0.1, 0);
        }
    }

    protected void captureFailed()
    {
        final LivingEntity mob = this.sendOut(false);
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
        if (pokemob != null)
        {
            pokemob.getEntity().setLocationAndAngles(this.posX, this.posY + 1.0D, this.posZ, this.rotationYaw, 0.0F);
            final boolean ret = this.getEntityWorld().addEntity(pokemob.getEntity());
            if (ret == false) PokecubeCore.LOGGER.error(String.format(
                    "The pokemob %1$s spawn from pokecube has failed. ", pokemob.getDisplayName().getFormattedText()));
            EntityPokecubeBase.setNoCaptureBasedOnConfigs(pokemob);
            pokemob.setCombatState(CombatStates.ANGRY, true);
            pokemob.setLogicState(LogicStates.SITTING, false);
            pokemob.setGeneralState(GeneralStates.TAMED, false);
            pokemob.setOwner((UUID) null);
            if (this.shootingEntity instanceof PlayerEntity && !(this.shootingEntity instanceof FakePlayer))
            {
                final ITextComponent mess = new TranslationTextComponent("pokecube.missed", pokemob.getDisplayName());
                ((PlayerEntity) this.shootingEntity).sendMessage(mess);
            }
        }
        if (mob instanceof MobEntity) ((MobEntity) mob).setAttackTarget(this.shootingEntity);
    }

    protected boolean captureSucceed()
    {
        PokecubeManager.setTilt(this.getItem(), -1);
        final Entity mob = PokecubeManager.itemToMob(this.getItem(), this.getEntityWorld());
        IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
        if (mob == null || this.shootingEntity == null)
        {
            PokecubeCore.LOGGER.error("Error with mob capture?", new NullPointerException());
            return false;
        }
        if (mob instanceof TameableEntity) ((TameableEntity) mob).setOwnerId(this.shootingEntity.getUniqueID());
        if (mob instanceof AbstractHorseEntity) ((AbstractHorseEntity) mob).setOwnerUniqueId(this.shootingEntity
                .getUniqueID());

        if (pokemob == null)
        {
            final ITextComponent mess = new TranslationTextComponent("pokecube.caught", mob.getDisplayName());
            ((PlayerEntity) this.shootingEntity).sendMessage(mess);
            this.playSound(EntityPokecubeBase.POKECUBESOUND, 0.4f, 1);
            return true;
        }

        HappinessType.applyHappiness(pokemob, HappinessType.TRADE);
        if (this.shootingEntity != null && !pokemob.getGeneralState(GeneralStates.TAMED)) pokemob.setOwner(
                this.shootingEntity);
        if (pokemob.getCombatState(CombatStates.MEGAFORME) || pokemob.getPokedexEntry().isMega)
        {
            pokemob.setCombatState(CombatStates.MEGAFORME, false);
            final IPokemob revert = pokemob.megaEvolve(pokemob.getPokedexEntry().getBaseForme());
            if (revert != null) pokemob = revert;
            if (pokemob.getEntity().getPersistentData().contains(TagNames.ABILITY)) pokemob.setAbility(AbilityManager
                    .getAbility(pokemob.getEntity().getPersistentData().getString(TagNames.ABILITY)));
        }
        final ItemStack pokemobStack = PokecubeManager.pokemobToItem(pokemob);
        this.setItem(pokemobStack);
        if (this.shootingEntity instanceof PlayerEntity && !(this.shootingEntity instanceof FakePlayer))
        {
            final ITextComponent mess = new TranslationTextComponent("pokecube.caught", pokemob.getDisplayName());
            ((PlayerEntity) this.shootingEntity).sendMessage(mess);
            this.setPosition(this.shootingEntity.posX, this.shootingEntity.posY, this.shootingEntity.posZ);
            this.playSound(EntityPokecubeBase.POKECUBESOUND, 1, 1);
        }
        return true;
    }

    @Override
    public Iterable<ItemStack> getArmorInventoryList()
    {
        return this.stuff;
    }

    /**
     * Gets the amount of gravity to apply to the thrown entity with each
     * tick.
     */
    protected float getGravityVelocity()
    {
        return 0.03F;
    }

    /**
     * Returns the ItemStack corresponding to the Entity (Note: if no item
     * exists, will log an error but still return an ItemStack containing
     * Block.stone)
     */
    public ItemStack getItem()
    {
        final ItemStack itemstack = this.getDataManager().get(EntityPokecubeBase.ITEM);
        return itemstack.isEmpty() ? new ItemStack(Blocks.STONE) : itemstack;
    }

    // For compatiblity.
    public ItemStack getItemEntity()
    {
        return this.getItem();
    }

    @Override
    public ItemStack getItemStackFromSlot(final EquipmentSlotType slotIn)
    {
        return ItemStack.EMPTY;
    }

    @Override
    public HandSide getPrimaryHand()
    {
        return HandSide.LEFT;
    }

    public Entity getReleased()
    {
        final int id = this.getDataManager().get(EntityPokecubeBase.ENTITYID);
        final Entity ret = this.getEntityWorld().getEntityByID(id);
        return ret;
    }

    public int getTime()
    {
        return this.getDataManager().get(EntityPokecubeBase.TIME);
    }

    public boolean isReleasing()
    {
        return this.getDataManager().get(EntityPokecubeBase.RELEASING);
    }

    /** Called when this EntityThrowable hits a block or entity. */
    protected void onImpact(final RayTraceResult result)
    {

        switch (result.getType())
        {
        case BLOCK:
            // No phasing through stuff.
            this.setMotion(0, 0, 0);

            // Only handle this on clients, and if not capturing something
            if (this.isServerWorld()) if (PokecubeManager.isFilled(this.getItem()) && this.tilt < 0) this.sendOut(true);
            break;
        case ENTITY:
            // Capturing or on client, break early.
            if (!this.isServerWorld() || this.tilt >= 0) break;
            final EntityRayTraceResult hit = (EntityRayTraceResult) result;
            // Send out or try to capture.
            if (PokecubeManager.isFilled(this.getItem())) this.sendOut(true);
            else this.captureAttempt(hit.getEntity());
            break;
        case MISS:
            break;
        default:
            break;
        }
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from
     * NBT.
     */
    @Override
    public void readAdditional(final CompoundNBT compound)
    {
        super.readAdditional(compound);
        this.tilt = compound.getInt("tilt");
        this.setTime(compound.getInt("time"));
        this.setReleasing(compound.getBoolean("releasing"));
        final CompoundNBT CompoundNBT1 = compound.getCompound("Item");
        this.setItem(ItemStack.read(CompoundNBT1));
        if (compound.contains("shooter")) this.shooter = UUID.fromString(compound.getString("shooter"));
        this.xTile = compound.getInt("xTile");
        this.yTile = compound.getInt("yTile");
        this.zTile = compound.getInt("zTile");
        this.throwableShake = compound.getByte("shake") & 255;
        this.inGround = compound.getByte("inGround") == 1;
        if (compound.contains("owner", 10)) this.shooter = NBTUtil.readUniqueId(compound.getCompound("owner"));

    }

    @Override
    protected void registerData()
    {
        super.registerData();
        this.getDataManager().register(EntityPokecubeBase.RELEASING, false);
        this.getDataManager().register(EntityPokecubeBase.ENTITYID, -1);
        this.getDataManager().register(EntityPokecubeBase.ITEM, ItemStack.EMPTY);
        this.getDataManager().register(EntityPokecubeBase.TIME, 40);
    }

    public LivingEntity sendOut(boolean summon)
    {
        if (this.getEntityWorld().isRemote || this.isReleasing()) return null;
        this.setTime(20);
        ServerWorld world = (ServerWorld) getEntityWorld();
        final Entity mob = PokecubeManager.itemToMob(this.getItem(), this.getEntityWorld());
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
        final Config config = PokecubeCore.getConfig();
        // Check permissions
        if (config.permsSendOut && this.shootingEntity instanceof PlayerEntity)
        {
            final PlayerEntity player = (PlayerEntity) this.shootingEntity;
            final IPermissionHandler handler = PermissionAPI.getPermissionHandler();
            final PlayerContext context = new PlayerContext(player);
            boolean denied = false;
            if (!handler.hasPermission(player.getGameProfile(), Permissions.SENDOUTPOKEMOB, context)) denied = true;
            if (denied)
            {
                Tools.giveItem((PlayerEntity) this.shootingEntity, this.getItem());
                this.remove();
                return null;
            }
        }

        // TODO use what spawn code uses, to check if the mob fits here, if not,
        // cancel the send out.

        // Fix the mob's position.
        final Vector3 v = this.v0.set(this);
        if (mob != null)
        {
            v.set(v.intX() + 0.5, v.y, v.intZ() + 0.5);
            final BlockState state = v.getBlockState(this.getEntityWorld());
            if (state.getMaterial().isSolid()) v.y = Math.ceil(v.y);
            mob.fallDistance = 0;
            v.moveEntity(mob);
        }

        if (pokemob != null)
        {
            // Check permissions
            if (config.permsSendOutSpecific && this.shootingEntity instanceof PlayerEntity)
            {
                final PokedexEntry entry = pokemob.getPokedexEntry();
                final PlayerEntity player = (PlayerEntity) this.shootingEntity;
                final IPermissionHandler handler = PermissionAPI.getPermissionHandler();
                final PlayerContext context = new PlayerContext(player);
                boolean denied = false;
                if (!handler.hasPermission(player.getGameProfile(), Permissions.SENDOUTSPECIFIC.get(entry), context))
                    denied = true;
                if (denied)
                {
                    Tools.giveItem((PlayerEntity) this.shootingEntity, this.getItem());
                    this.remove();
                    return null;
                }
            }

            SendOut evt = new SendOut.Pre(pokemob.getPokedexEntry(), v, this.getEntityWorld(), pokemob);
            if (PokecubeCore.POKEMOB_BUS.post(evt))
            {
                if (this.shootingEntity != null && this.shootingEntity instanceof PlayerEntity)
                {
                    Tools.giveItem((PlayerEntity) this.shootingEntity, this.getItem());
                    this.remove();
                }
                return null;
            }
            if (summon) world.summonEntity(mob);
            pokemob.onSendOut();
            pokemob.setGeneralState(GeneralStates.TAMED, true);
            pokemob.setGeneralState(GeneralStates.EXITINGCUBE, true);
            pokemob.setEvolutionTicks(50 + PokecubeCore.getConfig().exitCubeDuration);
            final Entity owner = pokemob.getOwner();
            if (owner instanceof PlayerEntity)
            {
                final ITextComponent mess = CommandTools.makeTranslatedMessage("pokemob.action.sendout", "green",
                        pokemob.getDisplayName());
                pokemob.displayMessageToOwner(mess);
            }
            this.setReleased(mob);
            this.setMotion(0, 0, 0);
            this.setTime(20);
            this.setReleasing(true);
            this.setItem(pokemob.getPokecube());
            evt = new SendOut.Post(pokemob.getPokedexEntry(), v, this.getEntityWorld(), pokemob);
            PokecubeCore.POKEMOB_BUS.post(evt);
        }
        else if (mob instanceof LivingEntity)
        {
            this.getItem().getTag().remove(TagNames.MOBID);
            this.getItem().getTag().remove(TagNames.POKEMOB);
            this.entityDropItem(this.getItem(), 0.5f);
            this.setReleased(mob);
            this.setMotion(0, 0, 0);
            this.setTime(20);
            this.setReleasing(true);
            if (summon) world.summonEntity(mob);
            return (LivingEntity) mob;
        }
        else
        {
            this.entityDropItem(this.getItem(), 0.5f);
            this.remove();
        }
        if (pokemob == null) return null;
        return pokemob.getEntity();
    }

    /** Sets the ItemStack for this entity */
    public void setItem(final ItemStack stack)
    {
        this.getDataManager().set(EntityPokecubeBase.ITEM, stack);
    }

    // For compatiblity
    public void setItemEntityStack(final ItemStack stack)
    {
        this.setItem(stack);
    }

    @Override
    public void setItemStackToSlot(final EquipmentSlotType slotIn, final ItemStack stack)
    {
    }

    @Override
    public void setMotion(final Vec3d p_213317_1_)
    {
        super.setMotion(p_213317_1_);
    }

    public void setReleased(final Entity entity)
    {
        this.getDataManager().set(EntityPokecubeBase.ENTITYID, entity.getEntityId());
    }

    public void setReleasing(final boolean tag)
    {
        this.getDataManager().set(EntityPokecubeBase.RELEASING, tag);
    }

    public void setTime(final int time)
    {
        this.getDataManager().set(EntityPokecubeBase.TIME, time);
    }

    /** Called to update the entity's position/logic. */
    @Override
    public void tick()
    {
        this.lastTickPosX = this.posX;
        this.lastTickPosY = this.posY;
        this.lastTickPosZ = this.posZ;

        if (this.throwableShake > 0) --this.throwableShake;

        // Calculate velocity if seeking

        if (this.tilt > 0 || this.targetEntity != null && !this.targetEntity.isAlive())
        {
            this.targetEntity = null;
            if (!this.targetLocation.equals(Vector3.secondAxisNeg)) this.targetLocation.clear();
        }

        final Vector3 target = Vector3.getNewVector();
        if (this.targetEntity != null) target.set(this.targetEntity);
        else target.set(this.targetLocation);
        if (!target.isEmpty() && this.seeking)
        {
            final Vector3 here = Vector3.getNewVector().set(this);
            final Vector3 dir = Vector3.getNewVector().set(target);
            if (this.targetEntity != null)
            {
                dir.x += this.targetEntity.getMotion().x;
                dir.y += this.targetEntity.getMotion().y;
                dir.z += this.targetEntity.getMotion().z;
            }
            double dist = dir.distanceTo(here) / 2;
            if (dist > 1) dist = 1;
            dir.subtractFrom(here);
            dir.scalarMultBy(dist);
            dir.setVelocities(this);
        }

        final AxisAlignedBB axisalignedbb = this.getBoundingBox().expand(this.getMotion()).grow(.2D);
        if (this.shootingEntity != null) this.ignoreEntity = this.shootingEntity;

        for (final Entity entity : this.world.getEntitiesInAABBexcluding(this, axisalignedbb, (mob) ->
        {
            return !mob.isSpectator() && mob.canBeCollidedWith() && !(mob instanceof EntityPokecubeBase)
                    && mob instanceof LivingEntity;
        }))
        {
            if (entity == this.ignoreEntity)
            {
                ++this.ignoreTime;
                break;
            }

            if (this.shootingEntity != null && this.ticksExisted < 2 && this.ignoreEntity == null)
            {
                this.ignoreEntity = entity;
                this.ignoreTime = 10;
                break;
            }
            final EntityRayTraceResult hit = new EntityRayTraceResult(entity);
            if (hit.getType() == Type.ENTITY) this.onImpact(hit);
        }

        final RayTraceResult raytraceresult = ProjectileHelper.func_221267_a(this, axisalignedbb, (p_213880_1_) ->
        {
            return !p_213880_1_.isSpectator() && p_213880_1_.canBeCollidedWith() && p_213880_1_ != this.ignoreEntity;
        }, RayTraceContext.BlockMode.OUTLINE, true);
        if (this.ignoreEntity != null && this.ignoreTime-- <= 0) this.ignoreEntity = null;

        trace:
        if (raytraceresult.getType() != RayTraceResult.Type.MISS)
        {
            if (raytraceresult.getType() == RayTraceResult.Type.BLOCK && this.world.getBlockState(
                    ((BlockRayTraceResult) raytraceresult).getPos()).getBlock() == Blocks.NETHER_PORTAL) this.setPortal(
                            ((BlockRayTraceResult) raytraceresult).getPos());
            if (raytraceresult instanceof BlockRayTraceResult)
            {
                final BlockRayTraceResult result = (BlockRayTraceResult) raytraceresult;
                final BlockState hit = this.getEntityWorld().getBlockState(result.getPos());
                final VoxelShape shape = hit.getCollisionShape(this.getEntityWorld(), result.getPos());
                if (!shape.isEmpty() && !shape.getBoundingBox().offset(result.getPos()).intersects(axisalignedbb))
                    break trace;
            }
            if (!net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, raytraceresult)) this.onImpact(
                    raytraceresult);
        }

        final Vec3d vec3d = this.getMotion();
        this.posX += vec3d.x;
        this.posY += vec3d.y;
        this.posZ += vec3d.z;
        final float f = MathHelper.sqrt(Entity.func_213296_b(vec3d));
        this.rotationYaw = (float) (MathHelper.atan2(vec3d.x, vec3d.z) * (180F / (float) Math.PI));

        for (this.rotationPitch = (float) (MathHelper.atan2(vec3d.y, f) * (180F / (float) Math.PI)); this.rotationPitch
                - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F)
            ;

        while (this.rotationPitch - this.prevRotationPitch >= 180.0F)
            this.prevRotationPitch += 360.0F;

        while (this.rotationYaw - this.prevRotationYaw < -180.0F)
            this.prevRotationYaw -= 360.0F;

        while (this.rotationYaw - this.prevRotationYaw >= 180.0F)
            this.prevRotationYaw += 360.0F;

        this.rotationPitch = MathHelper.lerp(0.2F, this.prevRotationPitch, this.rotationPitch);
        this.rotationYaw = MathHelper.lerp(0.2F, this.prevRotationYaw, this.rotationYaw);
        float f1;
        if (this.isInWater())
        {
            for (int i = 0; i < 4; ++i)
                this.world.addParticle(ParticleTypes.BUBBLE, this.posX - vec3d.x * 0.25D, this.posY - vec3d.y * 0.25D,
                        this.posZ - vec3d.z * 0.25D, vec3d.x, vec3d.y, vec3d.z);
            f1 = 0.8F;
        }
        else f1 = 0.99F;

        this.setMotion(vec3d.scale(f1));
        if (!this.hasNoGravity())
        {
            final Vec3d vec3d1 = this.getMotion();
            this.setMotion(vec3d1.x, vec3d1.y - this.getGravityVelocity(), vec3d1.z);
        }

        this.setPosition(this.posX, this.posY, this.posZ);
    }

    @Override
    public void writeAdditional(final CompoundNBT compound)
    {
        super.writeAdditional(compound);
        compound.putInt("tilt", this.tilt);
        compound.putInt("time", this.getTime());
        compound.putBoolean("releasing", this.isReleasing());
        if (this.shooter != null) compound.putString("shooter", this.shooter.toString());
        if (this.getItem() != null) compound.put("Item", this.getItem().write(new CompoundNBT()));
        compound.putInt("xTile", this.xTile);
        compound.putInt("yTile", this.yTile);
        compound.putInt("zTile", this.zTile);
        compound.putByte("shake", (byte) this.throwableShake);
        compound.putByte("inGround", (byte) (this.inGround ? 1 : 0));
        if (this.shooter != null) compound.put("owner", NBTUtil.writeUniqueId(this.shooter));

    }
}