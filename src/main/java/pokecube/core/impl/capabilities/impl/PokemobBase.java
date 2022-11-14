package pokecube.core.impl.capabilities.impl;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;

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
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import pokecube.api.data.PokedexEntry;
import pokecube.api.data.spawns.SpawnRule;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.ai.CombatStates;
import pokecube.api.entity.pokemob.moves.PokemobMoveStats;
import pokecube.api.entity.pokemob.stats.StatModifiers;
import pokecube.core.ai.logic.Logic;
import pokecube.core.ai.logic.LogicMountedControl;
import pokecube.core.ai.routes.IGuardAICapability;
import pokecube.core.moves.damage.EntityMoveUse;
import pokecube.core.network.pokemobs.PacketPingBoss;
import thut.api.IOwnable;
import thut.api.ThutCaps;
import thut.api.entity.CopyCaps;
import thut.api.entity.IBreedingMob;
import thut.api.entity.ICopyMob;
import thut.api.entity.genetics.IMobGenetics;
import thut.api.maths.Vector3;
import thut.api.world.mobs.data.DataSync;
import thut.core.common.world.mobs.data.SyncHandler;
import thut.core.common.world.mobs.data.types.Data_Byte;
import thut.core.common.world.mobs.data.types.Data_Float;
import thut.core.common.world.mobs.data.types.Data_Int;
import thut.core.common.world.mobs.data.types.Data_ItemStack;
import thut.core.common.world.mobs.data.types.Data_String;

public abstract class PokemobBase implements IPokemob
{
    public static class DataParameters
    {
        public final int[] FLAVOURS = new int[5];

        public int HELDITEMDW;
        public int EVOLTICKDW;
        public int DYNAPOWERDW;
        public int HAPPYDW;
        public int ATTACKCOOLDOWN;
        public int NICKNAMEDW;
        public int ZMOVECD;
        public int DIRECTIONPITCHDW;
        public int HEADINGDW;
        public int GENERALSTATESDW;
        public int LOGICSTATESDW;
        public int COMBATSTATESDW;
        public int ATTACKTARGETIDDW;
        public int ALLYTARGETIDDW;
        public int ENEMYNUMDW;
        public int ALLYNUMDW;
        public int HUNGERDW;
        public int STATUSDW;
        public int STATUSTIMERDW;
        public int MOVEINDEXDW;
        public int DYECOLOUR;
        public int TYPE1DW;
        public int TYPE2DW;
        public int ABILITYNAMEID;

        public final int[] DISABLE = new int[4];

        public void register(final IPokemob pokemob)
        {
            final DataSync sync = pokemob.dataSync();
            // Held Item timer
            this.HELDITEMDW = sync.register(new Data_ItemStack(), ItemStack.EMPTY);

            // Humger timer
            this.HUNGERDW = sync.register(new Data_Int(), Integer.valueOf(0));
            // // for sheared status
            this.NICKNAMEDW = sync.register(new Data_String(), "");// nickname
            this.HAPPYDW = sync.register(new Data_Int(), Integer.valueOf(0));// Happiness
            this.TYPE1DW = sync.register(new Data_String(), "");// overriden
            // type1
            this.TYPE2DW = sync.register(new Data_String(), "");// overriden
            // type2

            // From EntityAiPokemob
            this.DIRECTIONPITCHDW = sync.register(new Data_Float().setRealtime(), Float.valueOf(0));
            this.HEADINGDW = sync.register(new Data_Float().setRealtime(), Float.valueOf(0));
            this.ATTACKTARGETIDDW = sync.register(new Data_Int(), Integer.valueOf(-1));
            this.ALLYTARGETIDDW = sync.register(new Data_Int(), Integer.valueOf(-1));
            this.GENERALSTATESDW = sync.register(new Data_Int().setRealtime(), Integer.valueOf(0));
            this.LOGICSTATESDW = sync.register(new Data_Int().setRealtime(), Integer.valueOf(0));
            this.COMBATSTATESDW = sync.register(new Data_Int().setRealtime(), Integer.valueOf(0));

            this.ALLYNUMDW = sync.register(new Data_Int(), Integer.valueOf(1));
            this.ENEMYNUMDW = sync.register(new Data_Int(), Integer.valueOf(0));

            // from EntityEvolvablePokemob
            this.EVOLTICKDW = sync.register(new Data_Int().setRealtime(), Integer.valueOf(0));// evolution
            this.DYNAPOWERDW = sync.register(new Data_Float(), Float.valueOf(1));
            // tick

            // From EntityMovesPokemb
            this.STATUSDW = sync.register(new Data_Int(), Integer.valueOf((byte) -1));
            this.MOVEINDEXDW = sync.register(new Data_Byte().setRealtime(), Byte.valueOf((byte) -1));
            this.STATUSTIMERDW = sync.register(new Data_Int().setRealtime(), Integer.valueOf(0));
            this.ATTACKCOOLDOWN = sync.register(new Data_Int().setRealtime(), Integer.valueOf(0));

            this.DYECOLOUR = sync.register(new Data_Int(), Integer.valueOf(-1));

            this.ZMOVECD = sync.register(new Data_Int(), Integer.valueOf(-1));

            // Flavours for various berries eaten.
            for (int i = 0; i < 5; i++) this.FLAVOURS[i] = sync.register(new Data_Int(), Integer.valueOf(0));

            // Flavours for various berries eaten.
            for (int i = 0; i < 4; i++) this.DISABLE[i] = sync.register(new Data_Int(), Integer.valueOf(0));

            this.ABILITYNAMEID = sync.register(new Data_String(), "");// Name of
                                                                      // ability
        }
    }

    private static final UUID DYNAMOD = new UUID(343523462346243l, 23453246267457l);

    /** Inventory of the pokemob. */
    protected SimpleContainer pokeChest;
    /** Prevents duplication on returning to pokecubes */
    public boolean returning = false;
    /** Is this owned by a player? */
    protected boolean players = false;
    /** Cached Team for this Pokemob */
    protected String team = "";
    protected double moveSpeed;
    /** Cached Pokedex Entry for this pokemob. */
    protected PokedexEntry entry;

    /** The happiness value of the pokemob */
    protected int bonusHappiness = 0;
    /** Tracks whether this was a shadow mob at some point. */
    protected boolean wasShadow = false;
    /** Number used as seed for various RNG things. */
    protected int personalityValue = 0;
    /** Modifiers on stats. */
    protected StatModifiers modifiers = new StatModifiers();
    /** Egg we are trying to protect. */
    protected Entity egg = null;
    /**
     * Timer for determining whether wants to breed, will only do so if this is
     * greater than 0
     */
    protected int loveTimer;
    /** List of nearby male mobs to breed with */
    protected Vector<IBreedingMob> males = new Vector<>();
    /** Simpler UID for some client sync things. */
    protected int uid = -1;
    /** The pokecube this mob is "in" */
    protected ItemStack pokecube = ItemStack.EMPTY;
    /** Tracker for things related to moves. */
    protected PokemobMoveStats moveInfo = new PokemobMoveStats();
    /**
     * The current move being used, this is used to track whether the mob can
     * launch a new move, only allows sending a new move if this returns true
     * for isDone()
     */
    protected EntityMoveUse activeMove;
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
    public IMobGenetics genes;
    /** The IMobGenetics used to store our genes. */
    private IOwnable ownerHolder;

    protected ICopyMob transformed = new CopyCaps.Impl();

    protected ServerBossEvent bossEvent = null;

    /**
     * Used to cache current texture for quicker lookups, array to include any
     * animated textures
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

    @Override
    public DataSync dataSync()
    {
        if (this.dataSync == null) this.dataSync = SyncHandler.getData(this.getEntity());
        return this.dataSync;
    }

    /**
     * @return the ownerHolder
     */
    public IOwnable getOwnerHolder()
    {
        if (this.ownerHolder == null) this.ownerHolder = this.entity.getCapability(ThutCaps.OWNABLE_CAP).orElse(null);
        return this.ownerHolder;
    }

    @Override
    public void setDataSync(final DataSync sync)
    {
        this.dataSync = sync;
        this.params.register(this);
    }

    @Override
    public void setEntity(final Mob entityIn)
    {
        this.entity = entityIn;
    }

    protected void setMaxHealth(final float maxHealth)
    {
        final AttributeInstance health = this.getEntity().getAttribute(Attributes.MAX_HEALTH);
        health.removeModifier(PokemobBase.DYNAMOD);
        final AttributeModifier dynahealth = new AttributeModifier(PokemobBase.DYNAMOD, "pokecube:dynamax",
                this.getDynamaxFactor(), Operation.MULTIPLY_BASE);
        if (this.getCombatState(CombatStates.DYNAMAX)) health.addTransientModifier(dynahealth);
        health.setBaseValue(maxHealth);
    }

    /**
     * Handles health update.
     *
     * @param level
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
}
