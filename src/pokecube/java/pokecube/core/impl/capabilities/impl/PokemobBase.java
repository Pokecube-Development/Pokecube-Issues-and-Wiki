package pokecube.core.impl.capabilities.impl;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import pokecube.api.data.PokedexEntry;
import pokecube.api.data.spawns.SpawnRule;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.api.entity.pokemob.moves.PokemobMoveStats;
import pokecube.api.utils.TagNames;
import pokecube.core.ai.logic.Logic;
import pokecube.core.ai.logic.LogicMountedControl;
import pokecube.core.ai.routes.IGuardAICapability;
import pokecube.core.inventory.pokemob.PokemobInventory;
import pokecube.core.network.pokemobs.PacketPingBoss;
import pokecube.core.utils.PokemobTracker;
import thut.api.Tracker;
import thut.api.attachments.CopyMob;
import thut.api.attachments.Ownable;
import thut.api.attachments.Shearable;
import thut.api.entity.ICopyMob;
import thut.api.item.ItemList;
import thut.api.maths.Vector3;
import thut.api.world.mobs.data.Data;
import thut.api.world.mobs.data.DataSync;
import thut.core.common.genetics.DefaultGenetics;
import thut.core.common.mobs.DefaultColourable;
import thut.core.common.world.mobs.data.DataSync_Impl;
import thut.core.common.world.mobs.data.types.Data_Byte;
import thut.core.common.world.mobs.data.types.Data_Float;
import thut.core.common.world.mobs.data.types.Data_Int;
import thut.core.common.world.mobs.data.types.Data_ItemStack;
import thut.core.common.world.mobs.data.types.Data_Long;
import thut.core.common.world.mobs.data.types.Data_String;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class PokemobBase implements IPokemob
{
    public static class DataParameters
    {
        public Data<ItemStack> HELDITEMDW;
        public Data<Integer> HUNGERDW;
        public Data<String> NICKNAMEDW;
        public Data<Integer> HAPPYDW;
        public Data<String> TYPE1DW;
        public Data<String> TYPE2DW;
        public Data<Float> DIRECTIONPITCHDW;

        public Data<Float> HEADINGDW;
        public Data<Integer> ATTACKTARGETIDDW;
        public Data<Integer> ALLYTARGETIDDW;
        public Data<Integer> GENERALSTATESDW;
        public Data<Integer> LOGICSTATESDW;
        public Data<Integer> COMBATSTATESDW;
        public Data<Integer> ALLYNUMDW;
        public Data<Integer> ENEMYNUMDW;
        public Data<Integer> EVOLTICKDW;
        public Data<Integer> EXP;
        public Data<Byte> MOVEINDEXDW;
        public Data<Integer> ATTACKCOOLDOWN;
        public Data<Integer> DYECOLOUR;
        public Data<String> ABILITYNAMEID;
        public Data<Long> TIMEOFDEATH;

        public Data<Integer> ZMOVECD;

        @SuppressWarnings("unchecked")
        public final Data<Integer>[] FLAVOURS = new Data[5];
        @SuppressWarnings("unchecked")
        public final Data<Integer>[] DISABLE = new Data[4];

        public void register(final DataSync sync)
        {
            sync.setRegisterTag("pokemob");
            // Held Item timer
            this.HELDITEMDW = sync.register(new Data_ItemStack("held_item")).setRealtime();

            // Humger timer
            this.HUNGERDW = sync.register(new Data_Int("hunger")).setRealtime();
            // // for sheared status
            this.NICKNAMEDW = sync.register(new Data_String("nickname"));
            this.HAPPYDW = sync.register(new Data_Int("happiness"));
            this.TYPE1DW = sync.register(new Data_String("type1"));
            this.TYPE2DW = sync.register(new Data_String("type2"));

            // From EntityAiPokemob
            this.DIRECTIONPITCHDW = sync.register(new Data_Float("pitch").setRealtime());
            this.HEADINGDW = sync.register(new Data_Float("yaw").setRealtime());
            this.ATTACKTARGETIDDW = sync.register(new Data_Int("target", -1));
            this.ALLYTARGETIDDW = sync.register(new Data_Int("ally", -1));
            this.GENERALSTATESDW = sync.register(new Data_Int("general_state").setRealtime());
            this.LOGICSTATESDW = sync.register(new Data_Int("logic_state").setRealtime());
            this.COMBATSTATESDW = sync.register(new Data_Int("combat_state").setRealtime());

            this.ALLYNUMDW = sync.register(new Data_Int("ally_n", 1));
            this.ENEMYNUMDW = sync.register(new Data_Int("enemy_n"));

            // from EntityEvolvablePokemob
            this.EVOLTICKDW = sync.register(new Data_Int("evo_timer").setRealtime());

            // From EntityMovesPokemb
            this.EXP = sync.register(new Data_Int("exp", 0));
            this.MOVEINDEXDW = sync.register(new Data_Byte("move_index", (byte) -1));
            this.ATTACKCOOLDOWN = sync.register(new Data_Int("attack_cd"));

            this.DYECOLOUR = sync.register(new Data_Int("dye", -1));

            this.ZMOVECD = sync.register(new Data_Int("z_cd", -1));

            // Flavours for various berries eaten.
            for (int i = 0; i < 5; i++) this.FLAVOURS[i] = sync.register(new Data_Int("flav_" + i));

            // Flavours for various berries eaten.
            for (int i = 0; i < 4; i++) this.DISABLE[i] = sync.register(new Data_Int("diable_" + i));

            // Ability name
            this.ABILITYNAMEID = sync.register(new Data_String("ability"));

            // Death time for tracking animations, respawning, etc
            this.TIMEOFDEATH = sync.register(new Data_Long("time_of_death"));
        }
    }

    /** Inventory of the pokemob. */
    protected PokemobInventory pokeChest;
    /** Prevents duplication on returning to pokecubes */
    public boolean returning = false;
    /** Is this owned by a player? */
    protected boolean players = false;
    /** Cached Team for this Pokemob */
    protected String team = "";

    /** Tracks whether this was a shadow mob at some point. */
    protected boolean wasShadow = false;
    /** Number used as seed for various RNG things. */
    protected int personalityValue = 0;

    /** Egg we are trying to protect. */
    protected Entity egg = null;
    /**
     * Timer for determining whether wants to breed, will only do so if this is greater than 0
     */
    protected int loveTimer;
    /** Simpler UID for some client sync things. */
    protected int uid = -1;
    /** The pokecube this mob is "in" */
    protected ItemStack pokecube = ItemStack.EMPTY;
    /** Tracker for things related to moves. */
    protected PokemobMoveStats moveInfo = new PokemobMoveStats();
    /** Used for size when pathing */
    protected Vector3 sizes = new Vector3();
    /** Cooldown for hunger AI */
    protected int hungerCooldown = 0;

    protected ITargetFinder targetFinder;

    protected int timeSinceCombat = 0;

    protected SpawnRule spawnInitRule = null;

    /** Data manager used for syncing data */
    public DataSync dataSync;
    /** Holds the data parameters used for syncing our stuff. */
    protected final DataParameters params = new DataParameters();

    /** Stack which will be used for evolution */
    protected ItemStack stack = ItemStack.EMPTY;
    /** Manages mounted control */
    public LogicMountedControl controller;
    /** Used for various cases where things at mobs location need checking */
    protected Vector3 here = new Vector3();
    /** The Entity this IPokemob is attached to. */
    protected Mob entity;
    /** The Entity this IPokemob is attached to for world lookups, may differ from entity */
    protected LivingEntity trackedEntity;
    /** Our original owner. */
    protected UUID OTID;
    /** Used for maintaining/storing homes and routes. */
    protected IGuardAICapability guardCap;
    /** How long the mob is */
    protected float length;

    protected ICopyMob transformed = new CopyMob.Impl();

    protected ServerBossEvent bossEvent = null;

    /**
     * Used to cache current texture for quicker lookups, array to include any animated textures
     */
    protected ResourceLocation[] textures;

    protected final Map<ResourceLocation, ResourceLocation> shinyTexs = new Object2ObjectOpenHashMap<>();
    protected final Map<ResourceLocation, ResourceLocation[]> texs = new Object2ObjectOpenHashMap<>();

    /**
     * This is the nbt of searalizable tasks.
     */
    protected CompoundTag loadedTasks;

    protected List<Logic> logic = Lists.newArrayList();

    protected boolean isRemoved = false;

    public PokemobBase()
    {
        this.dataSync = new DataSync_Impl();
        this.params.register(this.dataSync);
    }

    @Override
    public void setDataSync(final DataSync sync)
    {
        if (sync != this.dataSync)
        {
            sync.mapFrom(this.dataSync, "pokemob");
            this.dataSync = sync;
        }
    }

    @Override
    public void setEntity(final Mob entityIn, boolean onEvolution)
    {
        if (entityIn == this.entity) return;
        var oldEntity = this.entity;
        // Set this here before the below setData call for PokemobCaps.POKEMOB
        this.entity = entityIn;
        // ensure we are the entity's IPokemob
        entityIn.setData(PokemobCaps.POKEMOB, this);
        entityIn.setData(DefaultColourable.TYPE_SAVE, this);

        // If it is on evolution, copy this over from the old entity
        if (onEvolution && oldEntity != null)
        {
            entityIn.setData(DefaultGenetics.TYPE, oldEntity.getData(DefaultGenetics.TYPE));
            entityIn.setData(DataSync_Impl.TYPE, oldEntity.getData(DataSync_Impl.TYPE));
            entityIn.setData(Ownable.TYPE, oldEntity.getData(Ownable.TYPE));
            entityIn.setData(CopyMob.TYPE_COPY, oldEntity.getData(CopyMob.TYPE_COPY));
        }
        else // Otherwise set ours to the ones from the entity
        {
            this.setGenes(entityIn.getData(DefaultGenetics.TYPE));
            this.setDataSync(entityIn.getData(DataSync_Impl.TYPE));
            this.setOwnerHolder(entityIn.getData(Ownable.TYPE));
            this.setCopy(entityIn.getData(CopyMob.TYPE_COPY));
        }
    }

    @Override
    public Mob getEntity()
    {
        return this.entity;
    }

    @Override
    public void setTrackableEntity(LivingEntity entityIn)
    {
        this.trackedEntity = entityIn;
    }

    @Override
    public LivingEntity getTrackedEntity()
    {
        return trackedEntity;
    }

    protected void setMaxHealth(final float maxHealth)
    {
        final AttributeInstance health = this.getTrackedEntity().getAttribute(Attributes.MAX_HEALTH);
        health.setBaseValue(maxHealth);
    }

    /**
     * Handles health update.
     */
    @Override
    public void updateHealth()
    {
        final float old = this.getMaxHealth();
        final float maxHealth = this.getMaxHPStat();
        float health = this.getHealth();

        if (maxHealth > old)
        {
            final float damage = old - health;
            health = maxHealth - damage;

            if (health > maxHealth) health = maxHealth;
        }
        this.setMaxHealth(maxHealth);
        this.setHealth(health);
    }

    @Override
    public boolean isSheared()
    {
        boolean sheared = this.getGeneralState(GeneralStates.SHEARED);
        if (sheared && this.getEntity().isEffectiveAi())
        {
            final long lastShear = this.getEntity().getPersistentData().getLong(TagNames.SHEARTIME);
            final ItemStack key = new ItemStack(Items.SHEARS);
            if (this.getPokedexEntry().interact(key))
            {
                final PokedexEntry.InteractionLogic.Interaction action = this.getPokedexEntry().interactionLogic.getFor(key);
                final int timer = action.cooldown + this.getEntity().getRandom().nextInt(1 + action.variance);
                if (lastShear < Tracker.instance().getTick() - timer) sheared = false;
            }
            // Cannot shear this!
            else sheared = false;
            this.setGeneralState(GeneralStates.SHEARED, sheared);
        }
        return sheared;
    }

    @Override
    public void shear(final ItemStack shears)
    {
        if (this.isSheared() || !this.getEntity().isEffectiveAi()) return;
        final ResourceLocation WOOL = ResourceLocation.parse("wool");

        if (this.getPokedexEntry().interact(shears))
        {
            this.getEntity().getData(Shearable.TYPE);
            final ArrayList<ItemStack> ret = new ArrayList<>();
            this.setGeneralState(GeneralStates.SHEARED, true);
            this.getEntity().getPersistentData().putLong(TagNames.SHEARTIME, Tracker.instance().getTick());
            final PokedexEntry.InteractionLogic.Interaction action = this.getPokedexEntry().interactionLogic.getFor(shears);
            final List<ItemStack> list = action.stacks;
            this.applyHunger(action.hunger);
            for (final ItemStack stack : list)
            {
                ItemStack toAdd = stack.copy();
                if (ItemList.is(WOOL, stack))
                {
                    final DyeColor colour = DyeColor.byId(this.getDyeColour());
                    final Item wool = Sheep.ITEM_BY_DYE.get(colour).asItem();
                    final ItemStack _toAdd = new ItemStack(wool, stack.getCount());
                    stack.getComponents().keySet().forEach(c -> _toAdd.copyFrom(stack, c));
                    toAdd = _toAdd;
                }
                ret.add(toAdd);
            }
            for (final ItemStack stack : ret) this.getEntity().spawnAtLocation(stack);
            this.getEntity().playSound(SoundEvents.SHEEP_SHEAR, 1.0F, 1.0F);
        }
    }

    @Override
    public float getHeading()
    {
        if (this.getGeneralState(GeneralStates.CONTROLLED)) return this.params.HEADINGDW.get();
        return this.getEntity().getYRot();
    }

    @Override
    public void setHeading(final float heading)
    {
        if (this.getGeneralState(GeneralStates.CONTROLLED))
        {
            this.getEntity().setYRot(heading);
            this.params.HEADINGDW.set(heading);
        }
    }

    @Override
    public ServerBossEvent getBossInfo()
    {
        return this.bossEvent;
    }

    @Override
    public void setBossInfo(final ServerBossEvent event)
    {
        this.bossEvent = event;
        if (this.getEntity().level instanceof ServerLevel && event != null) PacketPingBoss.onNewBossEvent(this);
    }

    @Override
    public long getDeathTime()
    {
        return params.TIMEOFDEATH.get();
    }

    @Override
    public void setDeathTime(long time)
    {
        params.TIMEOFDEATH.set(time);
    }

    public void setCopy(ICopyMob transform)
    {
        this.transformed = transform;
    }

    private boolean isDirty = false;

    @Override
    public void markDirty()
    {
        this.isDirty = true;
    }

    @Override
    public void markClean()
    {
        this.isDirty = false;
        if (this.getEntity() != null && this.getEntity().isAddedToLevel())
        {
            PokemobTracker.addPokemob(this);
        }
    }

    @Override
    public boolean isDirty()
    {
        return isDirty;
    }
}
