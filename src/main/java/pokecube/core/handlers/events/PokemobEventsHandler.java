package pokecube.core.handlers.events;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.vecmath.Vector3f;

import org.nfunk.jep.JEP;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.StartTracking;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.permission.IPermissionHandler;
import net.minecraftforge.server.permission.PermissionAPI;
import net.minecraftforge.server.permission.context.PlayerContext;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.ai.logic.Logic;
import pokecube.core.database.Pokedex;
import pokecube.core.database.PokedexEntry;
import pokecube.core.entity.pokemobs.EntityPokemob;
import pokecube.core.events.pokemob.InteractEvent;
import pokecube.core.events.pokemob.combat.KillEvent;
import pokecube.core.handlers.Config;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemobUseable;
import pokecube.core.interfaces.Nature;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.items.berries.ItemBerry;
import pokecube.core.moves.PokemobDamageSource;
import pokecube.core.network.pokemobs.PacketPokemobGui;
import pokecube.core.network.pokemobs.PacketSyncGene;
import pokecube.core.utils.Permissions;
import pokecube.core.utils.Tools;
import thut.api.entity.genetics.Alleles;
import thut.api.entity.genetics.GeneRegistry;
import thut.api.entity.genetics.IMobGenetics;
import thut.api.maths.Vector3;

@Mod.EventBusSubscriber
public class PokemobEventsHandler
{
    private static Map<DyeColor, Tag<Item>> DYETAGS = Maps.newHashMap();

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void dropEvent(final LivingDropsEvent event)
    {
        // Handles the mobs dropping their inventory. TODO test if this works.
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(event.getEntity());
        if (pokemob != null)
        {
            final Collection<ItemEntity> bak = event.getEntity().captureDrops();
            event.getEntity().captureDrops(Lists.newArrayList());
            if (!pokemob.getGeneralState(GeneralStates.TAMED)) for (int i = 0; i < pokemob.getInventory()
                    .getSizeInventory(); i++)
            {
                final ItemStack stack = pokemob.getInventory().getStackInSlot(i);
                if (!stack.isEmpty()) event.getEntity().entityDropItem(stack.copy(), 0.0f);
                pokemob.getInventory().setInventorySlotContents(i, ItemStack.EMPTY);
            }
            else event.getDrops().clear();
            event.getEntity().captureDrops(bak);
        }
    }

    private static void dropItem(final IPokemob dropper)
    {
        final ItemStack toDrop = dropper.getHeldItem();
        if (toDrop.isEmpty()) return;
        final Entity entity = dropper.getEntity();
        final ItemEntity drop = new ItemEntity(entity.getEntityWorld(), entity.posX, entity.posY + 0.5, entity.posZ,
                toDrop);
        entity.getEntityWorld().addEntity(drop);
        dropper.setHeldItem(ItemStack.EMPTY);
    }

    public static Map<DyeColor, Tag<Item>> getDyeTagMap()
    {
        if (PokemobEventsHandler.DYETAGS.isEmpty()) for (final DyeColor colour : DyeColor.values())
        {
            final ResourceLocation tag = new ResourceLocation("forge", "dyes/" + colour.getName());
            PokemobEventsHandler.DYETAGS.put(colour, ItemTags.getCollection().getOrCreate(tag));
        }
        return PokemobEventsHandler.DYETAGS;
    }

    private static void handleExp(final MobEntity pokemob, final IPokemob attacker, final LivingEntity attacked)
    {
        final IPokemob attackedMob = CapabilityPokemob.getPokemobFor(attacked);
        if (PokecubeCore.getConfig().nonPokemobExp && attackedMob == null)
        {
            final JEP parser = new JEP();
            parser.initFunTab(); // clear the contents of the function table
            parser.addStandardFunctions();
            parser.initSymTab(); // clear the contents of the symbol table
            parser.addStandardConstants();
            parser.addComplex();
            parser.addVariable("h", 0);
            parser.addVariable("a", 0);
            parser.parseExpression(PokecubeCore.getConfig().nonPokemobExpFunction);
            parser.setVarValue("h", attacked.getMaxHealth());
            parser.setVarValue("a", attacked.getTotalArmorValue());
            int exp = (int) parser.getValue();
            if (parser.hasError()) exp = 0;
            attacker.setExp(attacker.getExp() + exp, true);
            return;
        }
        if (attackedMob != null && attacked.getHealth() <= 0)
        {
            boolean giveExp = !attackedMob.isShadow();
            final boolean pvp = attackedMob.getGeneralState(GeneralStates.TAMED) && attackedMob
                    .getOwner() instanceof PlayerEntity;
            if (pvp && !PokecubeCore.getConfig().pvpExp) giveExp = false;
            if (attackedMob.getGeneralState(GeneralStates.TAMED) && !PokecubeCore.getConfig().trainerExp)
                giveExp = false;
            final KillEvent event = new KillEvent(attacker, attackedMob, giveExp);
            PokecubeCore.POKEMOB_BUS.post(event);
            giveExp = event.giveExp;
            if (event.isCanceled())
            {

            }
            else if (giveExp)
            {
                attacker.setExp(attacker.getExp() + Tools.getExp((float) (pvp ? PokecubeCore
                        .getConfig().pvpExpMultiplier : PokecubeCore.getConfig().expScaleFactor), attackedMob
                                .getBaseXP(), attackedMob.getLevel()), true);
                final byte[] evsToAdd = Pokedex.getInstance().getEntry(attackedMob.getPokedexNb()).getEVs();
                attacker.addEVs(evsToAdd);
            }
            final Entity targetOwner = attackedMob.getOwner();
            attacker.displayMessageToOwner(new TranslationTextComponent("pokemob.action.faint.enemy", attackedMob
                    .getDisplayName()));
            if (targetOwner instanceof PlayerEntity && attacker.getOwner() != targetOwner) pokemob.setAttackTarget(
                    (LivingEntity) targetOwner);
            else pokemob.setAttackTarget(null);
            if (attacker.getPokedexEntry().isFood(attackedMob.getPokedexEntry()) && attacker.getCombatState(
                    CombatStates.HUNTING))
            {
                attacker.eat(pokemob.getAttackTarget());
                attacker.setCombatState(CombatStates.HUNTING, false);
                pokemob.getNavigator().clearPath();
            }
        }
        else pokemob.setAttackTarget(null);
    }

    private static boolean handleHmAndSaddle(final PlayerEntity PlayerEntity, final IPokemob pokemob)
    {
        if (PokemobEventsHandler.isRidable(PlayerEntity, pokemob))
        {
            if (PlayerEntity.isServerWorld()) PlayerEntity.startRiding(pokemob.getEntity());
            return true;
        }
        return false;
    }

    @SubscribeEvent
    public static void interactEvent(final PlayerInteractEvent.EntityInteract evt)
    {
        final String ID = "LastSuccessInteractEvent";
        final long time = evt.getEntity().getPersistentData().getLong(ID);
        if (time == evt.getEntity().getEntityWorld().getGameTime())
        {
            evt.setCanceled(true);
            return;
        }
        PokemobEventsHandler.processInteract(evt, evt.getTarget());
        if (evt.isCanceled()) evt.getEntity().getPersistentData().putLong(ID, evt.getEntity().getEntityWorld()
                .getGameTime());
    }

    @SubscribeEvent
    public static void interactEvent(final PlayerInteractEvent.EntityInteractSpecific evt)
    {
        final String ID = "LastSuccessInteractEvent";
        final long time = evt.getEntity().getPersistentData().getLong(ID);
        if (time == evt.getEntity().getEntityWorld().getGameTime())
        {
            evt.setCanceled(true);
            return;
        }
        PokemobEventsHandler.processInteract(evt, evt.getTarget());
        if (evt.isCanceled()) evt.getEntity().getPersistentData().putLong(ID, evt.getEntity().getEntityWorld()
                .getGameTime());
    }

    @SubscribeEvent
    public static void interactEvent(final PlayerInteractEvent.RightClickBlock evt)
    {
        final String ID = "LastSuccessInteractEvent";
        final long time = evt.getEntity().getPersistentData().getLong(ID);
        if (time == evt.getEntity().getEntityWorld().getGameTime())
        {
            evt.setCanceled(true);
            return;
        }
    }

    @SubscribeEvent
    public static void interactEvent(final PlayerInteractEvent.RightClickItem evt)
    {
        final String ID = "LastSuccessInteractEvent";
        final long time = evt.getEntity().getPersistentData().getLong(ID);
        if (time == evt.getEntity().getEntityWorld().getGameTime())
        {
            evt.setCanceled(true);
            return;
        }
    }

    private static boolean isRidable(final Entity rider, final IPokemob pokemob)
    {
        final PokedexEntry entry = pokemob.getPokedexEntry();
        if (entry == null)
        {
            System.err.println("Null Entry for " + pokemob);
            return false;
        }
        if (!entry.ridable || pokemob.getCombatState(CombatStates.GUARDING)) return false;
        if (pokemob.getInventory().getStackInSlot(0).isEmpty()) return false;

        if (rider instanceof ServerPlayerEntity && rider == pokemob.getOwner())
        {
            final PlayerEntity player = (PlayerEntity) rider;
            final IPermissionHandler handler = PermissionAPI.getPermissionHandler();
            final PlayerContext context = new PlayerContext(player);
            final Config config = PokecubeCore.getConfig();
            if (config.permsRide && !handler.hasPermission(player.getGameProfile(), Permissions.RIDEPOKEMOB, context))
                return false;
            if (config.permsRideSpecific && !handler.hasPermission(player.getGameProfile(), Permissions.RIDESPECIFIC
                    .get(entry), context)) return false;
        }
        final float scale = pokemob.getSize();
        final Vector3f dims = pokemob.getPokedexEntry().getModelSize();
        return dims.y * scale + dims.x * scale > rider.getWidth() && Math.max(dims.x, dims.z) * scale > rider.getWidth()
                * 1.8;
    }

    @SubscribeEvent
    public static void KillEvent(final KillEvent evt)
    {
        final IPokemob killer = evt.killer;
        final IPokemob killed = evt.killed;
        // Handles extra EXP gain from lucky egg and exp share.
        if (killer != null && evt.giveExp)
        {
            final LivingEntity owner = killer.getOwner();
            final ItemStack stack = killer.getHeldItem();
            if (PokecubeItems.is(new ResourceLocation("pokecube", "luckyegg"), stack))
            {
                final int exp = killer.getExp() + Tools.getExp((float) PokecubeCore.getConfig().expScaleFactor, killed
                        .getBaseXP(), killed.getLevel());
                killer.setExp(exp, true);
            }
            if (owner != null)
            {
                final List<Entity> pokemobs = PCEventsHandler.getOutMobs(owner, false);
                for (final Entity mob : pokemobs)
                {
                    final IPokemob poke = CapabilityPokemob.getPokemobFor(mob);
                    if (poke != null) if (PokecubeItems.is(new ResourceLocation("pokecube", "exp_share"), poke
                            .getHeldItem()))
                    {
                        final int exp = poke.getExp() + Tools.getExp((float) PokecubeCore.getConfig().expScaleFactor,
                                killed.getBaseXP(), killed.getLevel());
                        poke.setExp(exp, true);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void livingDeath(final LivingDeathEvent evt)
    {
        final DamageSource damageSource = evt.getSource();
        // Handle transferring the kill info over, This is in place for mod
        // support.
        if (damageSource instanceof PokemobDamageSource) damageSource.getImmediateSource().onKillEntity(
                (LivingEntity) evt.getEntity());

        // Handle exp gain for the mob.
        final IPokemob attacker = CapabilityPokemob.getPokemobFor(damageSource.getImmediateSource());
        if (attacker != null && damageSource.getImmediateSource() instanceof MobEntity) PokemobEventsHandler.handleExp(
                (MobEntity) damageSource.getImmediateSource(), attacker, (LivingEntity) evt.getEntity());

        // Recall if it is a pokemob.
        IPokemob attacked = CapabilityPokemob.getPokemobFor(evt.getEntity());
        if (attacked != null) attacked.onRecall();
    }

    @SubscribeEvent
    public static void onJoinWorld(final EntityJoinWorldEvent event)
    {
        final Entity mob = event.getEntity();
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
        if (pokemob == null) return;
        pokemob.setEntity((MobEntity) mob);
        pokemob.initAI();
    }

    public static void processInteract(final PlayerInteractEvent evt, final Entity target)
    {
        if (!(evt.getPlayer() instanceof ServerPlayerEntity)) return;
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(target);
        if (pokemob == null) return;

        final ServerPlayerEntity player = (ServerPlayerEntity) evt.getPlayer();
        final Hand hand = evt.getHand();
        final ItemStack held = player.getHeldItem(hand);
        final MobEntity entity = pokemob.getEntity();

        final InteractEvent event = new InteractEvent(pokemob, player, evt);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.getResult() != Result.DEFAULT)
        {
            evt.setCanceled(true);
            evt.setCancellationResult(ActionResultType.SUCCESS);
            return;
        }

        // Item has custom entity interaction, let that run instead.
        if (held.getItem().itemInteractionForEntity(held, player, entity, hand))
        {
            evt.setCanceled(true);
            evt.setCancellationResult(ActionResultType.SUCCESS);
            return;
        }

        final PokedexEntry entry = pokemob.getPokedexEntry();

        // Check Pokedex Entry defined Interaction for player.
        if (entry.interact(player, pokemob, true))
        {
            evt.setCanceled(true);
            evt.setCancellationResult(ActionResultType.SUCCESS);
            return;
        }

        boolean isOwner = false;
        if (pokemob.getOwnerId() != null) isOwner = pokemob.getOwnerId().equals(player.getUniqueID());
        // Owner only interactions phase 1
        if (isOwner)
        {
            // Either push pokemob around, or if sneaking, make it try to
            // climb
            // on shoulder
            if (held.getItem() == Items.STICK || held.getItem() == Blocks.TORCH.asItem())
            {
                if (player.isSneaking())
                {
                    if (pokemob.getEntity().isAlive()) pokemob.moveToShoulder(player);
                    return;
                }
                final Vector3 look = Vector3.getNewVector().set(player.getLookVec()).scalarMultBy(1);
                look.y = 0.2;
                look.addVelocities(target);
                return;
            }
            // Debug thing to maximize happiness
            if (held.getItem() == Items.APPLE) if (player.abilities.isCreativeMode && player.isSneaking()) pokemob
                    .addHappiness(255);
            // Debug thing to increase hunger time
            if (held.getItem() == Items.GOLDEN_HOE) if (player.abilities.isCreativeMode && player.isSneaking()) pokemob
                    .setHungerTime(pokemob.getHungerTime() + 4000);
            // Use shiny charm to make shiny
            if (PokecubeItems.is(new ResourceLocation("pokecube:shiny_charm"), held))
            {
                if (player.isSneaking())
                {
                    pokemob.setShiny(!pokemob.isShiny());
                    if (!player.abilities.isCreativeMode) held.split(1);
                }
                evt.setCanceled(true);
                evt.setCancellationResult(ActionResultType.SUCCESS);
                return;
            }
        }

        // is Dyeable
        if (!held.isEmpty() && entry.dyeable)
        {
            final Tag<Item> dyeTag = Tags.Items.DYES;
            DyeColor dye = null;
            if (held.getItem().isIn(dyeTag))
            {
                final Map<DyeColor, Tag<Item>> tags = PokemobEventsHandler.getDyeTagMap();
                for (final DyeColor colour : DyeColor.values())
                    if (held.getItem().isIn(tags.get(colour)))
                    {
                        dye = colour;
                        break;
                    }
            }
            if (dye != null && (entry.validDyes.isEmpty() || entry.validDyes.contains(dye)))
            {
                pokemob.setDyeColour(dye.getId());
                if (!player.abilities.isCreativeMode) held.shrink(1);
                evt.setCanceled(true);
                evt.setCancellationResult(ActionResultType.SUCCESS);
                return;
            }
            else if (held.getItem() == Items.SHEARS) return;
        }

        boolean deny = pokemob.getCombatState(CombatStates.NOITEMUSE);
        if (deny && entity.getAttackTarget() == null)
        {
            deny = false;
            pokemob.setCombatState(CombatStates.NOITEMUSE, false);
        }

        if (deny)
        {
            // Add message here about cannot use items right now
            player.sendMessage(new TranslationTextComponent("pokemob.action.cannotuse"));
            return;
        }

        final boolean saddleCheck = !player.isSneaking() && held.isEmpty() && (isOwner || pokemob
                .getEntity() instanceof EntityPokemob && ((EntityPokemob) pokemob.getEntity()).canFitPassenger(player))
                && PokemobEventsHandler.handleHmAndSaddle(player, pokemob);

        // Check if favourte berry and sneaking, if so, do breeding stuff.
        if (isOwner || player instanceof FakePlayer)
        {
            final int fav = Nature.getFavouriteBerryIndex(pokemob.getNature());
            if (PokecubeCore.getConfig().berryBreeding && (player.isSneaking() || player instanceof FakePlayer)
                    && entity.getAttackTarget() == null && held.getItem() instanceof ItemBerry && (fav == -1
                            || fav == ((ItemBerry) held.getItem()).type.index))
            {
                if (!player.abilities.isCreativeMode)
                {
                    held.shrink(1);
                    if (held.isEmpty()) player.inventory.setInventorySlotContents(player.inventory.currentItem,
                            ItemStack.EMPTY);
                }
                pokemob.setLoveTimer(0);
                entity.setAttackTarget(null);
                entity.getEntityWorld().setEntityState(entity, (byte) 18);
                evt.setCanceled(true);
                evt.setCancellationResult(ActionResultType.SUCCESS);
                return;
            }
        }

        // Owner only interactions phase 2
        if (isOwner)
        {
            if (!held.isEmpty())
            {
                // Check if it should evolve from item, do so if yes.
                if (pokemob.canEvolve(held))
                {
                    final IPokemob evolution = pokemob.evolve(true, false, held);
                    if (evolution != null) if (!player.abilities.isCreativeMode)
                    {
                        held.shrink(1);
                        if (held.isEmpty()) player.inventory.setInventorySlotContents(player.inventory.currentItem,
                                ItemStack.EMPTY);
                    }
                    evt.setCanceled(true);
                    evt.setCancellationResult(ActionResultType.SUCCESS);
                    return;
                }
                // Otherwise check if useable item.
                final IPokemobUseable usable = IPokemobUseable.getUsableFor(held);
                if (usable != null)
                {
                    final ActionResult<ItemStack> result = usable.onUse(pokemob, held, player);
                    if (result.getType() == ActionResultType.SUCCESS)
                    {
                        player.setHeldItem(hand, result.getResult());
                        pokemob.setCombatState(CombatStates.NOITEMUSE, true);
                        evt.setCanceled(true);
                        evt.setCancellationResult(ActionResultType.SUCCESS);
                        return;
                    }
                }
                // Try to hold the item.
                if (PokecubeItems.isValidHeldItem(held))
                {
                    final ItemStack heldItem = pokemob.getHeldItem();
                    if (!heldItem.isEmpty()) PokemobEventsHandler.dropItem(pokemob);
                    final ItemStack toSet = held.copy();
                    toSet.setCount(1);
                    pokemob.setHeldItem(toSet);
                    pokemob.setCombatState(CombatStates.NOITEMUSE, true);
                    if (!player.abilities.isCreativeMode) held.shrink(1);
                    {
                        if (held.isEmpty()) player.inventory.setInventorySlotContents(player.inventory.currentItem,
                                ItemStack.EMPTY);
                    }
                    evt.setCanceled(true);
                    evt.setCancellationResult(ActionResultType.SUCCESS);
                    return;
                }
            }
            // Open Gui
            if (!saddleCheck)
            {
                PacketPokemobGui.sendOpenPacket(entity, player);
                evt.setCanceled(true);
                evt.setCancellationResult(ActionResultType.SUCCESS);
                return;
            }
        }

        // Check saddle for riding.
        if (saddleCheck)
        {
            entity.setJumping(false);
            evt.setCanceled(true);
            evt.setCancellationResult(ActionResultType.SUCCESS);
            return;
        }

    }

    @SubscribeEvent
    public static void startTracking(final StartTracking event)
    {
        // Sync genes over to players when they start tracking a pokemob
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(event.getTarget());
        final IMobGenetics genes = event.getTarget().getCapability(GeneRegistry.GENETICS_CAP).orElse(null);
        if (pokemob != null && event.getEntity() instanceof ServerPlayerEntity) for (final Alleles allele : genes
                .getAlleles().values())
            PacketSyncGene.syncGene(event.getTarget(), allele, (ServerPlayerEntity) event.getPlayer());
    }

    @SubscribeEvent
    public static void tick(final LivingUpdateEvent evt)
    {
        // Tick the logic stuff for this mob.
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(evt.getEntity());
        if (pokemob != null) for (final Logic l : pokemob.getTickLogic())
            if (l.shouldRun()) l.tick(evt.getEntity().getEntityWorld());
    }
}
