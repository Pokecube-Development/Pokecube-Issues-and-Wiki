package pokecube.core.interfaces.capabilities.impl;

import java.util.UUID;

import javax.xml.namespace.QName;

import com.google.common.collect.Lists;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.IInventoryChangedListener;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.logic.LogicMountedControl;
import pokecube.core.client.gui.GuiInfoMessages;
import pokecube.core.database.PokedexEntryLoader.SpawnRule;
import pokecube.core.database.abilities.AbilityManager;
import pokecube.core.database.stats.StatsCollector;
import pokecube.core.entity.pokemobs.AnimalChest;
import pokecube.core.events.PCEvent;
import pokecube.core.events.pokemob.RecallEvent;
import pokecube.core.events.pokemob.SpawnEvent;
import pokecube.core.events.pokemob.combat.MoveMessageEvent;
import pokecube.core.handlers.TeamManager;
import pokecube.core.handlers.events.EventsHandler;
import pokecube.core.handlers.events.SpawnHandler;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.items.pokecubes.EntityPokecube;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.network.pokemobs.PacketPokemobMessage;
import pokecube.core.network.pokemobs.PokemobPacketHandler.MessageServer;
import pokecube.core.utils.TagNames;
import pokecube.core.utils.Tools;
import thut.api.maths.Vector3;

public abstract class PokemobOwned extends PokemobAI implements IInventoryChangedListener
{
    public static final QName KEY   = new QName("forme_key");
    public static final QName TEX   = new QName("tex");
    public static final QName MODEL = new QName("model");
    public static final QName ANIM  = new QName("anim");

    @Override
    public void displayMessageToOwner(final ITextComponent message)
    {
        final Entity owner = this.getOwner();
        // Ensure this is actually client side before sending this.
        if (PokecubeCore.proxy.isClientSide() && PokecubeCore.proxy.getPlayer().getUniqueID().equals(this.getOwnerId()))
            GuiInfoMessages.addMessage(message);
        else if (owner instanceof ServerPlayerEntity && this.getEntity().isAlive())
        {
            if (PokecubeMod.debug) PokecubeCore.LOGGER.info(message.getFormattedText());
            final MoveMessageEvent event = new MoveMessageEvent(this, message);
            PokecubeCore.MOVE_BUS.post(event);
            PacketPokemobMessage.sendMessage((PlayerEntity) owner, this.getEntity().getEntityId(), event.message);
        }
    }

    @Override
    public LogicMountedControl getController()
    {
        return this.controller;
    }

    @Override
    public int getDyeColour()
    {
        int info = this.dataSync().get(this.params.DYECOLOUR);
        if (info == -1) info = this.isShiny() ? this.getPokedexEntry().defaultSpecials
                : this.getPokedexEntry().defaultSpecial;
        return info;
    }

    @Override
    public BlockPos getHome()
    {
        if (this.guardCap.getActiveTask() != null) return this.guardCap.getActiveTask().getPos();
        return this.guardCap.getPrimaryTask().getPos();
    }

    @Override
    public float getHomeDistance()
    {
        if (this.guardCap.getActiveTask() != null) return this.guardCap.getActiveTask().getRoamDistance();
        return this.guardCap.getPrimaryTask().getRoamDistance();
    }

    @Override
    public AnimalChest getInventory()
    {
        if (this.pokeChest == null) this.initInventory();
        return this.pokeChest;
    }

    @Override
    public UUID getOriginalOwnerUUID()
    {
        return this.OTID;
    }

    @Override
    public LivingEntity getOwner()
    {
        final UUID ownerID = this.getOwnerId();
        if (ownerID == null) return null;
        final World world = this.getEntity().getEntityWorld();
        final boolean serv = world instanceof ServerWorld;
        if (!serv && ownerID.equals(PokecubeCore.proxy.getPlayer().getUniqueID())) if (this.getOwnerHolder()
                .getOwner() == null) this.getOwnerHolder().setOwner(PokecubeCore.proxy.getPlayer());
        return serv ? this.getOwnerHolder().getOwner((ServerWorld) world) : this.getOwnerHolder().getOwner();
    }

    @Override
    public UUID getOwnerId()
    {
        return this.getOwnerHolder().getOwnerId();
    }

    @Override
    public String getPokemobTeam()
    {
        if (this.team.isEmpty()) this.team = TeamManager.getTeam(this.getEntity());
        return this.team;
    }

    @Override
    public boolean hasHomeArea()
    {
        return this.getHome() != null && this.getHomeDistance() > 0;
    }

    protected void initInventory()
    {
        AnimalChest animalchest = this.pokeChest;
        this.pokeChest = new AnimalChest();
        if (animalchest != null)
        {
            animalchest.removeListener(this);
            final int i = Math.min(animalchest.getSizeInventory(), this.pokeChest.getSizeInventory());

            for (int j = 0; j < i; ++j)
            {
                final ItemStack itemstack = animalchest.getStackInSlot(j);

                if (itemstack != ItemStack.EMPTY) this.pokeChest.setInventorySlotContents(j, itemstack.copy());
            }
            animalchest = null;
        }
        this.pokeChest.addListener(this);
    }

    @Override
    public boolean isPlayerOwned()
    {
        return this.getOwnerHolder().isPlayerOwned();
    }

    @Override
    public boolean moveToShoulder(final PlayerEntity player)
    {
        final float scale = this.getSize();
        final float width = this.getPokedexEntry().width * scale;
        final float height = this.getPokedexEntry().height * scale;
        final float length = this.getPokedexEntry().length * scale;
        boolean rightSize = width < 1 && height < 1 && length < 1;
        rightSize |= this.getPokedexEntry().canSitShoulder;
        if (!rightSize) return false;
        if (super.moveToShoulder(player))
        {
            this.returning = true;
            return true;
        }
        return false;
    }

    @Override
    public void onInventoryChanged(final IInventory inventory)
    {
    }

    @Override
    public void onRecall(final boolean onDeath)
    {
        if (this.returning) return;
        this.returning = true;
        if (this.getOwnerId() == null)
        {
            this.getEntity().remove(false);
            return;
        }
        if (!(this.getEntity().getEntityWorld() instanceof ServerWorld)) try
        {
            final MessageServer packet = new MessageServer(MessageServer.RETURN, this.getEntity().getEntityId());
            PokecubeCore.packets.sendToServer(packet);
        }
        catch (final Exception ex)
        {
            PokecubeCore.LOGGER.error("Error recalling mob!", ex);
        }
        else this.executeRecall();
    }

    protected void executeRecall()
    {
        final UUID id = this.getEntity().getUniqueID();
        final Entity mob = this.getEntity();
        final World world = this.getEntity().getEntityWorld();
        final BlockPos pos = this.getEntity().getPosition();
        // Ensures the chunk is actually still loaded here.
        world.getChunk(pos);
        if (this.getTransformedTo() != null) this.setTransformedTo(null);
        final RecallEvent pre = new RecallEvent.Pre(this);
        PokecubeCore.POKEMOB_BUS.post(pre);
        if (pre.isCanceled()) return;
        final RecallEvent evtrec = new RecallEvent(this);
        PokecubeCore.POKEMOB_BUS.post(evtrec);
        if (this.getHealth() > 0 && evtrec.isCanceled()) return;
        this.setEvolutionTicks(0);
        this.setGeneralState(GeneralStates.EXITINGCUBE, false);
        this.setGeneralState(GeneralStates.EVOLVING, false);
        this.setCombatState(CombatStates.DYNAMAX, false);

        final boolean megaForm = this.getCombatState(CombatStates.MEGAFORME) || this.getPokedexEntry().isMega;

        if (megaForm)
        {
            final float hp = this.getHealth();
            final IPokemob base = this.megaRevert();
            base.setHealth(hp);
            if (base == this) this.returning = false;
            if (this.getEntity().getPersistentData().contains(TagNames.ABILITY)) base.setAbility(AbilityManager
                    .getAbility(this.getEntity().getPersistentData().getString(TagNames.ABILITY)));
            base.onRecall();
            this.getEntity().getPersistentData().putBoolean(TagNames.REMOVED, true);
            this.getEntity().getPersistentData().putBoolean(TagNames.CAPTURING, true);
            this.getEntity().captureDrops(null);
            this.getEntity().remove();
            EventsHandler.Schedule(world, w ->
            {
                final ServerWorld srld = (ServerWorld) w;
                final Entity original = srld.getEntityByUuid(id);
                if (original == mob) srld.removeEntity(original, false);
                return true;
            });
            return;
        }

        if (PokecubeMod.debug) PokecubeCore.LOGGER.info("Recalling " + this.getEntity());
        // Clear the pokemob's motion on recall
        this.getEntity().setMotion(0, 0, 0);

        /** If this has fainted, status should be reset. */
        if (this.getHealth() <= 0)
        {
            this.healStatus();
            this.healChanges();
        }

        final Entity owner = this.getOwner();

        this.setCombatState(CombatStates.NOMOVESWAP, false);
        this.setCombatState(CombatStates.ANGRY, false);
        BrainUtils.setAttackTarget(this.getEntity(), null);
        this.getEntity().captureDrops(Lists.newArrayList());
        final PlayerEntity tosser = PokecubeMod.getFakePlayer(this.getEntity().getEntityWorld());
        if (owner instanceof PlayerEntity)
        {
            final ItemStack itemstack = PokecubeManager.pokemobToItem(this);
            final PlayerEntity player = (PlayerEntity) owner;
            boolean noRoom = false;
            final boolean ownerDead = !player.isAlive() || player.getHealth() <= 0;
            if (ownerDead || player.inventory.getFirstEmptyStack() == -1) noRoom = true;
            if (noRoom)
            {
                final PCEvent event = new PCEvent(itemstack.copy(), tosser);
                MinecraftForge.EVENT_BUS.post(event);
                if (!event.isCanceled()) this.onToss(tosser, itemstack.copy());
            }
            else
            {
                final boolean added = player.inventory.addItemStackToInventory(itemstack);
                if (!added)
                {
                    final PCEvent event = new PCEvent(itemstack.copy(), tosser);
                    MinecraftForge.EVENT_BUS.post(event);
                    if (!event.isCanceled()) this.onToss(tosser, itemstack.copy());
                }
            }
            if (!owner.isSneaking() && this.getEntity().isAlive() && !ownerDead)
            {
                boolean has = StatsCollector.getCaptured(this.getPokedexEntry(), player) > 0;
                has = has || StatsCollector.getHatched(this.getPokedexEntry(), player) > 0;
                if (!has) StatsCollector.addCapture(this);
            }
            final ITextComponent mess = new TranslationTextComponent("pokemob.action.return", this.getDisplayName());
            this.displayMessageToOwner(mess);
        }
        else if (this.getOwnerId() != null)
        {
            final ItemStack itemstack = PokecubeManager.pokemobToItem(this);
            if (owner == null)
            {
                final PCEvent event = new PCEvent(itemstack.copy(), tosser);
                MinecraftForge.EVENT_BUS.post(event);
                if (!event.isCanceled()) this.onToss(tosser, itemstack.copy());
            }
            else
            {
                final PCEvent event = new PCEvent(itemstack.copy(), (LivingEntity) owner);
                MinecraftForge.EVENT_BUS.post(event);
                if (!event.isCanceled()) this.onToss((LivingEntity) owner, itemstack.copy());
            }
        }
        // This ensures it can't be caught by dupe
        this.getEntity().getPersistentData().putBoolean(TagNames.REMOVED, true);
        this.getEntity().getPersistentData().putBoolean(TagNames.CAPTURING, true);
        this.getEntity().captureDrops(null);
        this.getEntity().remove();

        final LivingEntity targ = BrainUtils.getAttackTarget(this.getEntity());
        /**
         * If we have a target, and we were recalled with health, assign
         * the target to our owner instead.
         */
        if (this.getCombatState(CombatStates.ANGRY) && targ != null && this.getHealth() > 0)
            if (owner instanceof LivingEntity)
        {
            final IPokemob targetMob = CapabilityPokemob.getPokemobFor(targ);
            if (targetMob != null)
            {
                BrainUtils.initiateCombat(targetMob.getEntity(), this.getOwner());
                if (PokecubeMod.debug) PokecubeCore.LOGGER.info("Swapping agro to cowardly owner!");
            }
            else targ.setRevengeTarget(this.getOwner());
        }

        EventsHandler.Schedule(world, w ->
        {
            final ServerWorld srld = (ServerWorld) w;
            final Entity original = srld.getEntityByUuid(id);
            if (original == mob) srld.removeEntity(original, false);
            return true;
        });
    }

    private void onToss(final LivingEntity owner, final ItemStack itemstack)
    {
        final EntityPokecube entity = new EntityPokecube(EntityPokecube.TYPE, owner.getEntityWorld());
        entity.shootingEntity = owner;
        entity.shooter = owner.getUniqueID();
        entity.setItem(itemstack);
        this.here.set(this.getEntity());
        this.here.moveEntity(entity);
        this.here.clear().setVelocities(entity);
        entity.targetEntity = null;
        entity.targetLocation.clear();
        this.getEntity().getEntityWorld().addEntity(entity);
    }

    @Override
    public void setDyeColour(final int info)
    {
        this.dataSync().set(this.params.DYECOLOUR, Integer.valueOf(info));
    }

    @Override
    public void setHeldItem(final ItemStack itemStack)
    {
        try
        {
            final ItemStack oldStack = this.getHeldItem();
            this.getInventory().setInventorySlotContents(1, itemStack);
            this.getPokedexEntry().onHeldItemChange(oldStack, itemStack, this);
            super.setHeldItem(itemStack);
            this.dataSync().set(this.params.HELDITEMDW, itemStack);

        }
        catch (final Exception e)
        {
            // Should not happen anymore
            e.printStackTrace();
        }
    }

    @Override
    public void setHome(final int x, final int y, final int z, final int distance)
    {
        this.guardCap.getPrimaryTask().setPos(new BlockPos(x, y, z));
        this.guardCap.getPrimaryTask().setRoamDistance(distance);
    }

    @Override
    public void setOriginalOwnerUUID(final UUID original)
    {
        this.OTID = original;
    }

    @Override
    public void setOwner(final LivingEntity e)
    {
        if (e == null)
        {
            // Clear team
            this.setPokemobTeam("");
            // Clear uuid
            this.getOwnerHolder().setOwner((UUID) null);
            /*
             * unset tame.
             */
            this.setGeneralState(GeneralStates.TAMED, false);
            return;
        }
        /*
         * Set it as tame.
         */
        this.setGeneralState(GeneralStates.TAMED, true);
        /*
         * Set not to wander around by default, they can choose to enable this
         * later.
         */
        this.setRoutineState(AIRoutine.WANDER, false);
        /*
         * Set owner, and set original owner if none already exists.
         */
        this.getOwnerHolder().setOwner(e);
        this.getOwnerHolder().setOwner(e.getUniqueID());
        if (this.getOriginalOwnerUUID() == null) this.setOriginalOwnerUUID(e.getUniqueID());
        /*
         * Trigger vanilla event for taming a mob.
         */
        if (e instanceof ServerPlayerEntity && this.getEntity() instanceof AnimalEntity) CriteriaTriggers.TAME_ANIMAL
                .trigger((ServerPlayerEntity) e, (AnimalEntity) this.getEntity());
    }

    @Override
    public void setOwner(final UUID owner)
    {
        this.getOwnerHolder().setOwner(owner);
        // Clear team, it will refresh it whenever it is actually checked.
        this.setPokemobTeam("");

        if (this.getEntity() instanceof TameableEntity) ((TameableEntity) this.getEntity()).setOwnerId(owner);
    }

    @Override
    public void setPokemobTeam(final String team)
    {
        this.team = team;
    }

    @Override
    public IPokemob spawnInit(final SpawnRule info)
    {
        IPokemob pokemob = this;
        int maxXP = this.getEntity().getPersistentData().getInt("spawnExp");
        /*
         * Check to see if the mob has spawnExp defined in its data. If not, it
         * will choose how much exp it spawns with based on the position that it
         * spawns in worls with.
         */
        if (maxXP == 0)
        {
            if (!this.getEntity().getPersistentData().getBoolean("initSpawn"))
            {
                // Only set this if we haven't had one set yet already
                if (pokemob.getHeldItem().isEmpty()) pokemob.setHeldItem(pokemob.wildHeldItem(this.getEntity()));
                if (pokemob instanceof PokemobOwned) ((PokemobOwned) pokemob).updateHealth();
                pokemob.setHealth(pokemob.getMaxHealth());
                return pokemob;
            }
            this.getEntity().getPersistentData().remove("initSpawn");
            final Vector3 spawnPoint = Vector3.getNewVector().set(this.getEntity());
            maxXP = SpawnHandler.getSpawnXp(this.getEntity().getEntityWorld(), spawnPoint, pokemob.getPokedexEntry());
            final SpawnEvent.Level event = new SpawnEvent.Level(pokemob.getPokedexEntry(), spawnPoint, this.getEntity()
                    .getEntityWorld(), Tools.xpToLevel(pokemob.getPokedexEntry().getEvolutionMode(), -1),
                    SpawnHandler.DEFAULT_VARIANCE);
            PokecubeCore.POKEMOB_BUS.post(event);
            final int level = event.getLevel();
            maxXP = Tools.levelToXp(pokemob.getPokedexEntry().getEvolutionMode(), level);
        }
        this.getEntity().getPersistentData().remove("spawnExp");

        // Set exp and held items.
        pokemob = pokemob.setForSpawn(maxXP);
        // Only set this if we haven't had one set yet already
        if (pokemob.getHeldItem().isEmpty()) pokemob.setHeldItem(pokemob.wildHeldItem(this.getEntity()));

        // Make sure heath is valid numbers.
        if (pokemob instanceof PokemobOwned) ((PokemobOwned) pokemob).updateHealth();
        pokemob.getEntity().setHealth(pokemob.getEntity().getMaxHealth());

        // If we have some spawn info, lets process it.
        if (info != null && info.values.containsKey(PokemobOwned.KEY)) pokemob.setCustomHolder(info.getForme(pokemob
                .getPokedexEntry()));

        // Reset love status to prevent immediate eggs
        this.resetLoveStatus();

        return pokemob;
    }
}
