package pokecube.core.impl.capabilities.impl;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import pokecube.api.data.spawns.SpawnRule;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.moves.PokemobMoveStats;
import pokecube.core.ai.logic.Logic;
import pokecube.core.ai.logic.LogicMountedControl;
import pokecube.core.ai.routes.IGuardAICapability;
import pokecube.core.network.pokemobs.PacketPingBoss;
import pokecube.core.utils.PokemobTracker;
import thut.api.attachments.CopyMob;
import thut.api.attachments.IOwnable;
import thut.api.attachments.Ownable;
import thut.api.entity.ICopyMob;
import thut.api.entity.genetics.Gene;
import thut.api.entity.genetics.IMobGenetics;
import thut.api.maths.Vector3;
import thut.api.world.mobs.data.Data;
import thut.api.world.mobs.data.DataSync;
import thut.core.common.genetics.DefaultGenetics;
import thut.core.common.world.mobs.data.DataSync_Impl;
import thut.core.common.world.mobs.data.types.Data_Byte;
import thut.core.common.world.mobs.data.types.Data_Float;
import thut.core.common.world.mobs.data.types.Data_Int;
import thut.core.common.world.mobs.data.types.Data_ItemStack;
import thut.core.common.world.mobs.data.types.Data_Long;
import thut.core.common.world.mobs.data.types.Data_String;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public abstract class PokemobBase implements IPokemob, Consumer<Gene<?>>
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
            this.HELDITEMDW = sync.register(new Data_ItemStack("held_item"));

            // Humger timer
            this.HUNGERDW = sync.register(new Data_Int("hunger"));
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
            this.MOVEINDEXDW = sync.register(new Data_Byte("move_index", (byte) -1).setRealtime());
            this.ATTACKCOOLDOWN = sync.register(new Data_Int("attack_cd").setRealtime());

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
    protected SimpleContainer pokeChest;
    /** Prevents duplication on returning to pokecubes */
    public boolean returning = false;
    /** Is this owned by a player? */
    protected boolean players = false;
    /** Cached Team for this Pokemob */
    protected String team = "";

    /** The happiness value of the pokemob */
    protected int bonusHappiness = 0;
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
    /** Our original owner. */
    protected UUID OTID;
    /** Used for maintaining/storing homes and routes. */
    protected IGuardAICapability guardCap;
    /** How long the mob is */
    protected float length;
    /** The IMobGenetics used to store our genes. */
    private IMobGenetics genes = new DefaultGenetics();
    /** The IMobGenetics used to store our genes. */
    private IOwnable ownerHolder = new Ownable.Impl();

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
    public DataSync dataSync()
    {
        return this.dataSync;
    }

    /**
     * @return the ownerHolder
     */
    public IOwnable getOwnerHolder()
    {
        return this.ownerHolder;
    }

    public void setOwnerHolder(IOwnable holder)
    {
        this.ownerHolder = holder;
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
    public void setEntity(final Mob entityIn)
    {
        this.entity = entityIn;
        // ensure we are the entity's IPokemob
        entityIn.setData(PokemobCaps.POKEMOB, this);

        this.setGenes(entityIn.getData(DefaultGenetics.TYPE));
        this.setDataSync(entityIn.getData(DataSync_Impl.TYPE));
        this.setOwnerHolder(entityIn.getData(Ownable.TYPE));
        this.setCopy(entityIn.getData(CopyMob.TYPE_COPY));
    }

    protected void setMaxHealth(final float maxHealth)
    {
        final AttributeInstance health = this.getEntity().getAttribute(Attributes.MAX_HEALTH);
        health.setBaseValue(maxHealth);
    }

    /**
     * Handles health update.
     */
    @Override
    public void updateHealth()
    {
        final float old = this.getMaxHealth();
        final float maxHealth = this.getStat(Stats.HP, false);
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

    @Override
    public IMobGenetics getGenes()
    {
        return genes;
    }

    @Override
    public void setGenes(IMobGenetics genes)
    {
        if (genes != this.genes)
        {
            genes.copyMissingFrom(this.genes);
            this.genes = genes;
            this.genes.addChangeListener(this);
            this.onGenesChanged();
        }
        this.getMoveStats().reset();
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
