package pokecube.core.interfaces.capabilities.impl;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.Vector;

import com.google.common.collect.Lists;

import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import pokecube.core.ai.logic.Logic;
import pokecube.core.ai.logic.LogicMountedControl;
import pokecube.core.ai.routes.IGuardAICapability;
import pokecube.core.database.PokedexEntry;
import pokecube.core.entity.pokemobs.AnimalChest;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.moves.PokemobMoveStats;
import pokecube.core.interfaces.pokemob.stats.StatModifiers;
import pokecube.core.moves.animations.EntityMoveUse;
import thut.api.IOwnable;
import thut.api.ThutCaps;
import thut.api.entity.IBreedingMob;
import thut.api.entity.genetics.Alleles;
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

        public int         HELDITEMDW;
        public int         EVOLTICKDW;
        public int         DYNAPOWERDW;
        public int         HAPPYDW;
        public int         ATTACKCOOLDOWN;
        public int         NICKNAMEDW;
        public int         ZMOVECD;
        public int         DIRECTIONPITCHDW;
        public int         HEADINGDW;
        public int         TRANSFORMEDTODW;
        public int         GENERALSTATESDW;
        public int         LOGICSTATESDW;
        public int         COMBATSTATESDW;
        public int         ATTACKTARGETIDDW;
        public int         HUNGERDW;
        public int         STATUSDW;
        public int         STATUSTIMERDW;
        public int         MOVEINDEXDW;
        public int         DYECOLOUR;
        public int         TYPE1DW;
        public int         TYPE2DW;
        public final int[] DISABLE = new int[4];
        public int         ACTIVEMOVEID;

        public void register(final IPokemob pokemob)
        {
            final DataSync sync = pokemob.dataSync();
            // Held Item timer
            this.HELDITEMDW = sync.register(new Data_ItemStack(), ItemStack.EMPTY);

            // Humger timer
            this.HUNGERDW = sync.register(new Data_Int(), new Integer(0));
            // // for sheared status
            this.NICKNAMEDW = sync.register(new Data_String(), "");// nickname
            this.HAPPYDW = sync.register(new Data_Int(), new Integer(0));// Happiness
            this.TYPE1DW = sync.register(new Data_String(), "");// overriden
                                                                // type1
            this.TYPE2DW = sync.register(new Data_String(), "");// overriden
                                                                // type2

            // From EntityAiPokemob
            this.DIRECTIONPITCHDW = sync.register(new Data_Float(), Float.valueOf(0));
            this.HEADINGDW = sync.register(new Data_Float(), Float.valueOf(0));
            this.ATTACKTARGETIDDW = sync.register(new Data_Int(), Integer.valueOf(-1));
            this.GENERALSTATESDW = sync.register(new Data_Int(), Integer.valueOf(0));
            this.LOGICSTATESDW = sync.register(new Data_Int(), Integer.valueOf(0));
            this.COMBATSTATESDW = sync.register(new Data_Int(), Integer.valueOf(0));

            // from EntityEvolvablePokemob
            this.EVOLTICKDW = sync.register(new Data_Int(), new Integer(0));// evolution
            this.DYNAPOWERDW = sync.register(new Data_Float(), Float.valueOf(1));
            // tick

            // From EntityMovesPokemb
            this.STATUSDW = sync.register(new Data_Byte(), Byte.valueOf((byte) -1));
            this.MOVEINDEXDW = sync.register(new Data_Byte(), Byte.valueOf((byte) -1));
            this.STATUSTIMERDW = sync.register(new Data_Int(), Integer.valueOf(0));
            this.ATTACKCOOLDOWN = sync.register(new Data_Int(), Integer.valueOf(0));

            this.DYECOLOUR = sync.register(new Data_Int(), Integer.valueOf(-1));
            this.TRANSFORMEDTODW = sync.register(new Data_Int(), Integer.valueOf(-1));

            this.ZMOVECD = sync.register(new Data_Int(), Integer.valueOf(-1));

            // Flavours for various berries eaten.
            for (int i = 0; i < 5; i++)
                this.FLAVOURS[i] = sync.register(new Data_Int(), Integer.valueOf(0));

            // Flavours for various berries eaten.
            for (int i = 0; i < 4; i++)
                this.DISABLE[i] = sync.register(new Data_Int(), Integer.valueOf(0));

            // EntityID of the active move use entity.
            this.ACTIVEMOVEID = sync.register(new Data_Int(), Integer.valueOf(-1));
        }
    }

    private static final UUID DYNAMOD = new UUID(343523462346243l, 23453246267457l);

    /** Inventory of the pokemob. */
    protected AnimalChest  pokeChest;
    /** Prevents duplication on returning to pokecubes */
    protected boolean      returning = false;
    /** Is this owned by a player? */
    protected boolean      players   = false;
    /** Cached Team for this Pokemob */
    protected String       team      = "";
    protected double       moveSpeed;
    /** Cached Pokedex Entry for this pokemob. */
    protected PokedexEntry entry;

    /** The happiness value of the pokemob */
    protected int                  bonusHappiness   = 0;
    /** Tracks whether this was a shadow mob at some point. */
    protected boolean              wasShadow        = false;
    /** Number used as seed for various RNG things. */
    protected int                  personalityValue = 0;
    /** Modifiers on stats. */
    protected StatModifiers        modifiers        = new StatModifiers();
    /** Egg we are trying to protect. */
    protected Entity               egg              = null;
    /**
     * Timer for determining whether wants to breed, will only do so if this is
     * greater than 0
     */
    protected int                  loveTimer;
    /** List of nearby male mobs to breed with */
    protected Vector<IBreedingMob> males            = new Vector<>();
    /** Simpler UID for some client sync things. */
    protected int                  uid              = -1;
    /** The pokecube this mob is "in" */
    protected ItemStack            pokecube         = ItemStack.EMPTY;
    /** Tracker for things related to moves. */
    protected PokemobMoveStats     moveInfo         = new PokemobMoveStats();
    /**
     * The current move being used, this is used to track whether the mob can
     * launch a new move, only allows sending a new move if this returns true
     * for isDone()
     */
    protected EntityMoveUse        activeMove;
    /** Used for size when pathing */
    protected Vector3              sizes            = Vector3.getNewVector();
    /** Cooldown for hunger AI */
    protected int                  hungerCooldown   = 0;

    // Here we have all of the genes currently used.
    Alleles genesSize;
    Alleles genesIVs;
    Alleles genesEVs;
    Alleles genesMoves;
    Alleles genesNature;
    Alleles genesAbility;
    Alleles genesColour;
    Alleles genesShiny;
    Alleles genesSpecies;

    /** Data manager used for syncing data */
    public DataSync                dataSync;
    /** Holds the data parameters used for syncing our stuff. */
    protected final DataParameters params = new DataParameters();

    /** Stack which will be used for evolution */
    protected ItemStack          stack = ItemStack.EMPTY;
    /** Manages mounted control */
    public LogicMountedControl   controller;
    /** Used for various cases where things at mobs location need checking */
    protected Vector3            here  = Vector3.getNewVector();
    /** The Entity this IPokemob is attached to. */
    protected AgeableEntity      entity;
    /** RNG used, should be entity.getRNG() */
    protected Random             rand  = new Random();
    /** Our original owner. */
    protected UUID               OTID;
    /** Used for maintaining/storing homes and routes. */
    protected IGuardAICapability guardCap;
    /** How long the mob is */
    protected float              length;
    /** The IMobGenetics used to store our genes. */
    public IMobGenetics          genes;
    /** The IMobGenetics used to store our genes. */
    private IOwnable             ownerHolder;
    /**
     * Used to cache current texture for quicker lookups, array to include any
     * animated textures
     */
    protected ResourceLocation[] textures;
    /**
     * This is the nbt of searalizable tasks.
     */
    protected CompoundNBT        loadedTasks;

    protected List<Logic> logic = Lists.newArrayList();

    @Override
    public DataSync dataSync()
    {
        if (this.dataSync == null) this.dataSync = SyncHandler.getData(this.getEntity());
        return this.dataSync;
    }

    @Override
    public AgeableEntity getEntity()
    {
        return this.entity;
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
    public void setEntity(final MobEntity entityIn)
    {
        this.rand = entityIn.getRNG();
        this.entity = (AgeableEntity) entityIn;
    }

    protected void setMaxHealth(final float maxHealth)
    {
        final IAttributeInstance health = this.getEntity().getAttribute(SharedMonsterAttributes.MAX_HEALTH);
        final List<AttributeModifier> mods = Lists.newArrayList(health.getModifiers());
        for (final AttributeModifier modifier : mods)
            health.removeModifier(modifier);
        final AttributeModifier dynahealth = new AttributeModifier(PokemobBase.DYNAMOD, "pokecube:dynamax", this
                .getDynamaxFactor(), Operation.MULTIPLY_BASE);
        if (this.getCombatState(CombatStates.DYNAMAX)) health.applyModifier(dynahealth);
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
}
