package pokecube.gimmicks.pokeplayer;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.RegisterEvent;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.events.pokemobs.EvolveEvent;
import pokecube.api.utils.PokeType;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.tasks.idle.HungerTask;
import pokecube.core.database.Database;
import pokecube.core.items.ItemPokedex;
import pokecube.core.utils.PokemobTracker;
import pokecube.gimmicks.pokeplayer.blocks.TransformPR;
import thut.api.ThutCaps;
import thut.api.attachments.TrackedAttachment;
import thut.api.entity.ICopyMob;
import thut.api.entity.event.CopySetEvent;
import thut.api.entity.event.CopyUpdateEvent;
import thut.api.maths.Vector3;
import thut.api.util.PermNodes;
import thut.core.common.ThutCore;
import thut.lib.RegHelper;
import thut.lib.TComponent;
import thut.wearables.inventory.PlayerWearables;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = PokecubeCore.MODID)
public class Pokeplayer
{
    public static final DeferredBlock<Block> TRANSFORM_PR_PLATE;
    public static final DeferredItem<BlockItem> TRANSFORM_PR_PLATE_ITEM;

    /**
     * Setup and register pokeplayer stuff.
     */
    @SubscribeEvent
    public static void init(FMLLoadCompleteEvent event)
    {
        // The commmand to turn into a pokemob
        ThutCore.FORGE_BUS.addListener(Pokeplayer::onCommandRegister);
        // We want to sync from copy to us, not other way, so handle that here.
        ThutCore.FORGE_BUS.addListener(Pokeplayer::onCopyTick);
        // Handles resetting flight permissions when un-setting mob
        ThutCore.FORGE_BUS.addListener(Pokeplayer::onCopySet);
        // This syncs step height for the mob over
        ThutCore.FORGE_BUS.addListener(Pokeplayer::onPlayerTick);

        // interaction with self with items
        ThutCore.FORGE_BUS.addListener(Pokeplayer::onRightClickItem);

        // Events for ensuring pokeplayers behave properly

        // Evolution
        PokecubeAPI.POKEMOB_BUS.addListener(Pokeplayer::onEvolve);
    }

    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(
            TComponent.translatable("not copy?"));

    public static final String PERMSELF = "pokeplayer.self";
    public static final String PERMOTHER = "pokeplayer.other";

    static
    {
        PermNodes.registerBooleanNode(PokecubeCore.MODID, PERMSELF, PermNodes.DefaultPermissionLevel.OP,
                "Allowed to use pokeplayer command on self");
        PermNodes.registerBooleanNode(PokecubeCore.MODID, PERMOTHER, PermNodes.DefaultPermissionLevel.OP,
                "Allowed to use pokeplayer command on other");

        TRANSFORM_PR_PLATE  = PokecubeCore.BLOCKS.register("transform_pressure_plate",
                () -> new TransformPR(
                        BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).sound(SoundType.STONE).requiresCorrectToolForDrops()));
        TRANSFORM_PR_PLATE_ITEM = PokecubeCore.ITEMS.register("transform_pressure_plate",
                () -> new BlockItem(TRANSFORM_PR_PLATE.get(), new Item.Properties()));
    }

    @SubscribeEvent
    public static void registerBedBlockEntity(BlockEntityTypeAddBlocksEvent event)
    {
        event.modify(BlockEntityType.BED, TRANSFORM_PR_PLATE.get());
    }

    public static int doPokeplayerCommand(String argument, Entity player) throws CommandSyntaxException
    {
        try
        {
            var copy = ThutCaps.getCopyMob(player);
            if (copy == null) throw Pokeplayer.ERROR_FAILED.create();

            // Putting none or player into entry arg reverts a transformed player.
            if (argument.equalsIgnoreCase("none") || argument.equalsIgnoreCase("player"))
            {
                player.sendSystemMessage(Component.literal("Reverted " + player.getName().getString() + " back into a player"));
                copy.setCopiedMob(null); // Changes player back into a player
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

    public static int transformPlayer(IPokemob pokemob, Entity player)
    {
        var copy = ThutCaps.getCopyMob(player);
        if (copy == null) return -2;

        if (pokemob == null) {
            player.sendSystemMessage(Component.literal("Reverted " + player.getName().getString() + " back into a player"));
            copy.setCopiedMob(null); // Changes player back into a player
            player.setNoGravity(false);
            return 0;
        }
        copy.setCopiedMob(pokemob.getEntity());
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

    private static void onEvolve(EvolveEvent.Post event)
    {
        var entity = event.mob.getEntity();
        if (entity.getPersistentData().hasUUID("copy_parent"))
        {
            var id = entity.getPersistentData().getUUID("copy_parent");
            var player = entity.level().getPlayerByUUID(id);
            var copy = ThutCaps.getCopyMob(player);
            if (copy != null)
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
        final ICopyMob copy = ThutCaps.getCopyMob(evt.getEntity());
        if (copy != null && copy.getCopiedMob() != null)
        {
            var stack = evt.getItemStack();
            if (stack.getItem() instanceof ItemPokedex && evt.getEntity().isShiftKeyDown())
            {
                stack.interactLivingEntity(evt.getEntity(), copy.getCopiedMob(), evt.getHand());
                evt.setCanceled(true);
                return;
            }
            var usable = PokemobCaps.getPokemobUsable(stack);
            var pokemob = PokemobCaps.getPokemobFor(copy.getCopiedMob());
            if (usable != null && pokemob != null)
            {
                var res = usable.onUse(pokemob, stack, evt.getEntity());
                if (res.getResult().indicateItemUse())
                {
                    evt.setCancellationResult(res.getResult());
                    evt.setCanceled(true);
                }
            }
            if (copy instanceof TrackedAttachment tracked) tracked.markDirty();
        }
    }

    private static void onPlayerTick(final PlayerTickEvent.Pre event)
    {
        final ICopyMob copy = ThutCaps.getCopyMob(event.getEntity());
        copy.setFullTick(true);

        // If we are copied, then just use the mob's step height.
        if (copy != null && copy.getCopiedMob() != null)
        {
            double dStep = copy.getCopiedMob().getAttribute(Attributes.STEP_HEIGHT).getValue() - event.getEntity()
                    .getAttribute(Attributes.STEP_HEIGHT).getValue();
            AttributeModifier mod = new AttributeModifier(STEP, dStep, AttributeModifier.Operation.ADD_VALUE);
            event.getEntity().getAttribute(Attributes.STEP_HEIGHT).addOrUpdateTransientModifier(mod);
        }
        else if (event.getEntity().getAttribute(Attributes.STEP_HEIGHT).hasModifier(STEP))
        {
            var player = event.getEntity();
            // first reset the step height
            player.getAttribute(Attributes.STEP_HEIGHT).removeModifier(STEP);
            // Here we also reset the hitbox, eye pos, etc
            player.setPose(Pose.STANDING);
            player.setNoGravity(false); // Stop them floating if they were
            player.refreshDimensions();// Ensure dimensions start to reset
            // Make them sneak for a tick to ensure this applies
            player.setShiftKeyDown(true);
        }
    }

    private static void onCopySet(final CopySetEvent event)
    {
        if (event.getEntity() instanceof Player player)
        {
            ResourceLocation FLYID = ResourceLocation.parse("pokeplayer:fly_sync");
            player.getAttribute(NeoForgeMod.CREATIVE_FLIGHT).removeModifier(FLYID);

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
            if (pokeHunger < 0.8)
            {
                if (food > 0)
                {
                    foodData.setFoodLevel(food - 1);
                    pokemob.setHungerTime(hunger - hungerRate);
                }
            }
            else if (foodData.needsFood() && pokeHunger > 0.9)
            {
                foodData.setFoodLevel(food + 1);
                pokemob.setHungerTime(hunger + hungerRate);
            }
            // TODO find appropriate places to do this instead of once per second.
            if (player.tickCount % 20 == 0) pokemob.markDirty();

            pokemob.setOwner(player);
            pokemob.setDataSync(ThutCaps.getDataSync(player));
            Pokeplayer.setFlying(player, pokemob);
            Pokeplayer.updateFloating(player, pokemob);
            Pokeplayer.updateFlying(player, pokemob);
            Pokeplayer.updateSwimming(player, pokemob);

            final ICopyMob copy = ThutCaps.getCopyMob(player);
            if (copy instanceof TrackedAttachment tracked && !(player.level().isClientSide()))
            {
                if (pokemob.isDirty()) tracked.markDirty();
                if (pokemob.getGenes().isDirty()) tracked.markDirty();
            }
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
        if (pokemob == null || !pokemob.floats()) return;
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
}
