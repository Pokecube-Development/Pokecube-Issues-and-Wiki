package pokecube.gimmicks.pokeplayer;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.events.pokemobs.EvolveEvent;
import pokecube.api.moves.Battle;
import pokecube.api.utils.PokeType;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.tasks.idle.HungerTask;
import pokecube.core.database.Database;
import pokecube.core.entity.npc.NpcMob;
import pokecube.core.entity.npc.NpcType;
import pokecube.core.items.ItemPokedex;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.moves.damage.effects.StatusEffects;
import pokecube.core.utils.PokemobTracker;
import pokecube.gimmicks.pokeplayer.blocks.TransformBlock;
import pokecube.gimmicks.pokeplayer.network.PokeplayerPacketHandler;
import pokecube.gimmicks.pokeplayer.network.packets.PacketHandshake;
import thut.api.ThutCaps;
import thut.api.attachments.TrackedAttachment;
import thut.api.entity.EntityProvider;
import thut.api.entity.ICopyMob;
import thut.api.entity.event.CopySetEvent;
import thut.api.entity.event.CopyUpdateEvent;
import thut.api.maths.Vector3;
import thut.api.util.PermNodes;
import thut.core.common.ThutCore;
import thut.wearables.inventory.PlayerWearables;

@EventBusSubscriber(modid = PokecubeCore.MODID)
public class Pokeplayer
{
    public static final DeferredBlock<Block> TRANSFORM_BLOCK;
    public static final DeferredItem<BlockItem> TRANSFORM_BLOCK_ITEM;

    /**
     * Setup and register pokeplayer stuff.
     */
    @SubscribeEvent
    public static void init(FMLLoadCompleteEvent event)
    {
        // The commmand to turn into a pokemob
        ThutCore.FORGE_BUS.addListener(Pokeplayer::onCommandRegister);
        // For syncing pokeplayer when joining a world
        ThutCore.FORGE_BUS.addListener(Pokeplayer::onPlayerJoinWorld);
        // We want to sync from copy to us, not other way, so handle that here.
        ThutCore.FORGE_BUS.addListener(Pokeplayer::onCopyTick);
        // Handles resetting flight permissions when un-setting mob
        ThutCore.FORGE_BUS.addListener(Pokeplayer::onCopySet);
        // This syncs step height for the mob over
        ThutCore.FORGE_BUS.addListener(Pokeplayer::onPlayerTick);

        // interaction with self with items
        ThutCore.FORGE_BUS.addListener(Pokeplayer::onRightClickItem);

        // Interaction with entities (e.g. healing when right clicking a healer)
        ThutCore.FORGE_BUS.addListener(Pokeplayer::onEntityInteractSpecific);

        // Events for ensuring pokeplayers behave properly

        // Evolution
        PokecubeAPI.POKEMOB_BUS.addListener(Pokeplayer::onEvolve);


        // Add a check to not remove pokeplayers
        Battle.BATTLE_TESTS.add(testSet -> {
            // Check if id below 0, ie is a copy, and if the same side also contains the owner
            if (testSet.mob().getId() < 0 && testSet.mobSide().contains(EntityProvider.getTracked(testSet.mob())))
            {
                testSet.battle().markAsValid(testSet.mob());
            }
        });
    }

    /// Packets need to be initialised somewhere, called here
    @SubscribeEvent
    public static void setup(FMLCommonSetupEvent event)
    {
        PokeplayerPacketHandler.init();
    }


    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(
            Component.translatable("not copy?"));

    public static final String PERMSELF = "pokeplayer.self";
    public static final String PERMOTHER = "pokeplayer.other";

    static
    {
        PermNodes.registerBooleanNode(PokecubeCore.MODID, PERMSELF, PermNodes.DefaultPermissionLevel.OP,
                "Allowed to use pokeplayer command on self");
        PermNodes.registerBooleanNode(PokecubeCore.MODID, PERMOTHER, PermNodes.DefaultPermissionLevel.OP,
                "Allowed to use pokeplayer command on other");

        TRANSFORM_BLOCK  = PokecubeCore.BLOCKS.register("transform_block",
                () -> new TransformBlock(
                        BlockBehaviour.Properties.of().sound(SoundType.STONE)));
        TRANSFORM_BLOCK_ITEM = PokecubeCore.ITEMS.register("transform_block",
                () -> new BlockItem(TRANSFORM_BLOCK.get(), new Item.Properties()));
    }

    public static int doPokeplayerCommand(String argument, Entity entity)
    {
        if(!(entity instanceof LivingEntity player)) return -1;
        try
        {
            var copy = ThutCaps.getCopyMob(player);
            if (copy == null) throw Pokeplayer.ERROR_FAILED.create();

            // Putting none or player into entry arg reverts a transformed player.
            if (argument.equalsIgnoreCase("none") || argument.equalsIgnoreCase("player"))
            {
                player.sendSystemMessage(Component.literal("Reverted " + player.getName().getString() + " back into a player"));
                copy.setCopiedMob(player,null); // Changes player back into a player
                // Reset the no gravity rules
                player.setNoGravity(false);
                return 0;
            }
            return transformPlayer(PokemobCaps.getPokemobFor(PokecubeCore.createPokemob(Database.getEntry(argument), player.level())),player);
        }
        catch (CommandSyntaxException c)
        {
            player.sendSystemMessage(Component.literal("Transform command has failed! Check command syntax."));
            return -1;
        }
    }

    public static int transformPlayer(IPokemob pokemob, LivingEntity player)
    {
        var copy = ThutCaps.getCopyMob(player);
        if (pokemob == null)
        {
            player.sendSystemMessage(Component.literal("Reverted " + player.getName().getString() + " back into a player"));
            copy.setCopiedMob(player, null); // Changes player back into a player
            player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(20.0);
            player.setHealth(20.0f);
            return 0;
        }
        copy.setCopiedMob(player, pokemob.getEntity());
        pokemob.updateHealth();
        float maxHP = pokemob.getMaxHealth();
        player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(maxHP);
        // Re-sync hp
        player.setHealth(pokemob.getHealth());
        player.sendSystemMessage(Component.literal("Transformed " + player.getName().getString() + " into " + pokemob.getDisplayName().getString()));
        return 0;
    }

    private static void onCommandRegister(final RegisterCommandsEvent event)
    {
        var command = Commands.literal("pokeplayer").requires(s -> {
            if (!(s.getEntity() instanceof ServerPlayer player)) return true;
            return PermNodes.getBooleanPerm(player, PERMSELF);
        }).then(Commands.argument("entry_or_none", StringArgumentType.string()).executes(
                        ctx -> doPokeplayerCommand(StringArgumentType.getString(ctx, "entry_or_none"),
                                ctx.getSource().getEntity()))
                .then(Commands.argument("player", EntityArgument.player()).requires(s -> {
                    if (!(s.getEntity() instanceof ServerPlayer player)) return true;
                    return PermNodes.getBooleanPerm(player, PERMOTHER);
                }).executes(ctx -> doPokeplayerCommand(StringArgumentType.getString(ctx, "entry_or_none"),
                        ctx.getSource().getEntity()))));
        event.getDispatcher().register(command);
    }

    private static void onPlayerJoinWorld(final EntityJoinLevelEvent evt)
    {
        if (!(evt.getEntity() instanceof Player player)) return;
        var copy = ThutCaps.getCopyMob(player);
        if (copy == null) return;
    }

    private static void onEvolve(EvolveEvent.Post event)
    {
        var entity = event.mob.getEntity();
        if (entity.getPersistentData().hasUUID("copy_parent"))
        {
            var id = entity.getPersistentData().getUUID("copy_parent");
            var player = entity.level().getPlayerByUUID(id);
            var copy = ThutCaps.getCopyMob(player);
            if (copy != null && copy.getCopiedMob() != null)
            {
                copy.setCopiedMob(entity);
                event.setCanceled(true);
            }
        }
    }

    private static final ResourceLocation STEP = ResourceLocation.parse("pokeplayer:step_adjust");

    private static void onRightClickItem(PlayerInteractEvent.RightClickItem evt)
    {
        // Try using it on self if it is a usable item or a pokedex
        final Player player = evt.getEntity();
        final ICopyMob copy = ThutCaps.getCopyMob(player);
        if (copy != null && copy.getCopiedMob() != null)
        {
            var stack = evt.getItemStack();
            if (stack.getItem() instanceof ItemPokedex && player.isShiftKeyDown())
            {
                stack.interactLivingEntity(player, copy.getCopiedMob(), evt.getHand());
                evt.setCanceled(true);
                return;
            }
            var usable = PokemobCaps.getPokemobUsable(stack);
            var pokemob = PokemobCaps.getPokemobFor(copy.getCopiedMob());
            if (pokemob != null)
            {
                if (usable != null)
                {
                    var res = usable.onUse(pokemob, stack, player);
                    if (res.getResult().indicateItemUse())
                    {
                        evt.setCancellationResult(res.getResult());
                        evt.setCanceled(true);
                    }
                }
            }
            if (copy instanceof TrackedAttachment tracked) tracked.markDirty();
        }
    }

    private static void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific evt)
    {
        final Player player = evt.getEntity();
        final ICopyMob copy = ThutCaps.getCopyMob(player);
        if (copy == null) return;
        final LivingEntity pokemob = EntityProvider.getTracked(copy.getCopiedMob());
        if (pokemob == null) return;

        Entity entity = EntityProvider.getTracked(evt.getTarget());

        if (entity instanceof NpcMob npc)
        {
            if (npc.getNpcType().equals(NpcType.byType("healer")))
                PokecubeManager.heal(pokemob);
        }
    }

    private static void onPlayerTick(final PlayerTickEvent.Pre event)
    {
        final ICopyMob copy = ThutCaps.getCopyMob(event.getEntity());
        copy.setFullTick(true);

        // If we are copied, then just use the mob's step height, but only for ones which are tall.
        if (copy.getCopiedMob() != null && copy.getCopiedMob().getBbHeight() > 2)
        {
            double dStep = copy.getCopiedMob().getAttribute(Attributes.STEP_HEIGHT).getValue() - event.getEntity()
                    .getAttribute(Attributes.STEP_HEIGHT).getValue();
            AttributeModifier mod = new AttributeModifier(STEP, dStep, AttributeModifier.Operation.ADD_VALUE);
            event.getEntity().getAttribute(Attributes.STEP_HEIGHT).addOrUpdateTransientModifier(mod);
        }
    }

    private static void onCopySet(final CopySetEvent event)
    {
        if (event.getEntity() instanceof Player player)
        {
            ResourceLocation FLYID = ResourceLocation.parse("pokeplayer:fly_sync");
            player.getAttribute(NeoForgeMod.CREATIVE_FLIGHT).removeModifier(FLYID);

            // first reset the step height
            player.getAttribute(Attributes.STEP_HEIGHT).removeModifier(STEP);
            // Here we also reset the hitbox, eye pos, etc
            player.setPose(Pose.STANDING);
            player.setNoGravity(false); // Stop them floating if they were
            player.refreshDimensions();// Ensure dimensions start to reset
            // Make them sneak for a tick to ensure this applies
            player.setShiftKeyDown(true);

            IPokemob oldMob = PokemobCaps.getPokemobFor(event.oldCopy);
            if (oldMob != null) PokemobTracker.removePokemob(oldMob);
            if (event.newCopy != null) event.newCopy.getPersistentData().putUUID("copy_parent", player.getUUID());
        }
    }

    private static void onCopyTick(final CopyUpdateEvent event)
    {
        if (!(event.realEntity instanceof Player player)) return;
        final Pose pose = event.realEntity.getPose();
        var entity = event.getEntity();
        entity.getPersistentData().putUUID("copy_parent", player.getUUID());
        // Short mobs need to be able to walk properly in small spaces, so force
        // standing pose if not in water
        if (entity.getBbHeight() < 1 && pose == Pose.SWIMMING && !event.realEntity.isInWaterOrBubble())
            event.realEntity.setPose(Pose.STANDING);

        entity.setData(PlayerWearables.TYPE, player.getData(PlayerWearables.TYPE));

        final IPokemob pokemob = PokemobCaps.getPokemobFor(entity);
        if (pokemob != null)
        {
            var hunger = pokemob.getHungerTime();
            var foodData = player.getFoodData();
            int food = foodData.getFoodLevel();
            float pokeHunger = HungerTask.calculateHunger(pokemob);
            int hungerRate = PokecubeCore.getConfig().pokemobLifeSpan / 25;
            // If we are below berry gen threshold, sync back from the player's hunger
            if (pokeHunger < HungerTask.BERRYGEN)
            {
                if (food > 0)
                {
                    foodData.setFoodLevel(food - 1);
                    pokemob.setHungerTime(hunger - hungerRate);
                }
            }
            // If we are above the hunt threshold, sync from pokemob to player
            // hunt threshold is where they will do things like eat rocks, etc
            else if (foodData.needsFood() && pokeHunger >= HungerTask.HUNTTHRESHOLD)
            {
                foodData.setFoodLevel(food + 1);
                foodData.setSaturation(5f);
                pokemob.setHungerTime(hunger + hungerRate);
            }
            pokemob.setOwner(player);
            pokemob.setDataSync(ThutCaps.getDataSync(player));
            Pokeplayer.setFlying(player, pokemob);
            Pokeplayer.updateFloating(player, pokemob);
            Pokeplayer.updateFlying(player, pokemob);
            Pokeplayer.updateSwimming(player, pokemob);
            Pokeplayer.updateStatus(player, pokemob);
            Pokeplayer.updateFireResistance(player, pokemob);
        }
    }

    private static void setFlying(final Player player, final IPokemob pokemob)
    {
        if (pokemob == null) return;
        final boolean fly = pokemob.floats() || pokemob.flys();
        if (player.mayFly() != fly)
        {
            ResourceLocation FLYID = ResourceLocation.parse("pokeplayer:fly_sync");
            if (fly) player.getAttribute(NeoForgeMod.CREATIVE_FLIGHT).addOrReplacePermanentModifier(
                    new AttributeModifier(FLYID, 1, AttributeModifier.Operation.ADD_VALUE));
            else player.getAttribute(NeoForgeMod.CREATIVE_FLIGHT).addOrReplacePermanentModifier(
                    new AttributeModifier(FLYID, -1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            player.onUpdateAbilities();
        }
    }

    private static void updateFlying(final Player player, final IPokemob pokemob)
    {
        if (pokemob == null) return;
        if (pokemob.floats() || pokemob.flys())
        {
            player.fallDistance = 0;
            if (player instanceof ServerPlayer) ((ServerPlayer) player).connection.aboveGroundTickCount = 0;
        }
    }

    private static void updateFloating(final Player player, final IPokemob pokemob)
    {
        if (pokemob == null || !pokemob.floats())
        {
            if(player.getPersistentData().getBoolean("pokeplayer:floating"))
            {
                player.setNoGravity(false);
                player.getPersistentData().remove("pokeplayer:floating");
            }
            return;
        }
        player.getPersistentData().putBoolean("pokeplayer:floating", true);
        if (!player.isShiftKeyDown())
        {
            player.setNoGravity(false);
            double gravity = player.getGravity();
            player.setNoGravity(true);
            var level = player.level();
            Vector3 hereVec = new Vector3(player);
            Vector3 nextVec = new Vector3(hereVec).addTo(0, -pokemob.getFloatHeight(), 0);
            var hit = level.clip(new ClipContext(hereVec.toVec3d(), nextVec.toVec3d(), ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.ANY, player));
            Vector3 push = new Vector3(0, gravity, 0);
            if (hit.getType() == HitResult.Type.MISS) push.scalarMultBy(-1);
            else
            {
                double offset = 1 - (player.getY() - hit.getLocation().y()) / pokemob.getFloatHeight();
                push.scalarMultBy(offset);
            }
            double vy = player.getDeltaMovement().y;
            if (Math.signum(vy) != Math.signum(push.y)) push.addVelocities(player);
        }
        else player.setNoGravity(false);
    }

    private static void updateSwimming(final Player player, final IPokemob pokemob)
    {
        if (pokemob == null) return;
        if (pokemob.getPokedexEntry().swims() || pokemob.isType(PokeType.getType("water"))) player.setAirSupply(300);
    }

    private static void updateFireResistance(final Player player, final IPokemob pokemob)
    {
        if (pokemob == null) return;
        if (pokemob.getPokedexEntry().isHeatProof || pokemob.isType(PokeType.getType("fire"))) player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 60, 1, true, false));
    }

    // This should run before anything giving a status effect (e.g. updateFireResistance)
    // This makes sure that pokeplayers do not lose status effects they should have (e.g. fire res for fire types)
    private static void updateStatus(final Player player, final IPokemob pokemob)
    {
        player.removeAllEffects();
        StatusEffects.setStatus(player, pokemob.getEntity(), StatusEffects.getStatusEffect(pokemob.getEntity()));
    }

}
