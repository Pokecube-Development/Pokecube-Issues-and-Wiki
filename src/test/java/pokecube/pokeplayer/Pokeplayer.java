package pokecube.pokeplayer;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.utils.PokeType;
import thut.api.entity.CopyCaps;
import thut.api.entity.ICopyMob;
import thut.api.entity.event.CopySetEvent;
import thut.api.entity.event.CopyUpdateEvent;
import thut.bot.entity.BotPlayer;
import thut.core.common.network.CapabilitySync;

@Mod(value = "pokecube_pokeplayer")
public class Pokeplayer
{

    public Pokeplayer()
    {
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class,
                () -> new IExtensionPoint.DisplayTest(() -> "pokecube_pokeplayer", (incoming, isNetwork) -> true));

        // The commmand to turn into a pokemob
        MinecraftForge.EVENT_BUS.addListener(Pokeplayer::onCommandRegister);
        // We want to sync from copy to us, not other way, so handle that here.
        MinecraftForge.EVENT_BUS.addListener(Pokeplayer::onCopyTick);
        // Handles resetting flight permissions when un-setting mob
        MinecraftForge.EVENT_BUS.addListener(Pokeplayer::onCopySet);
        // This syncs step height for the mob over
        MinecraftForge.EVENT_BUS.addListener(Pokeplayer::onPlayerTick);

        CopyCaps.register(EntityType.PLAYER);
    }

    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(
            new TranslatableComponent("not copy?"));

    private static void onCommandRegister(final RegisterCommandsEvent event)
    {
        final LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("pokeplayer").then(Commands
                .argument("val", StringArgumentType.string()).executes(ctx ->
                {
                    final String wrd = StringArgumentType.getString(ctx, "val");

                    final PokedexEntry var = Database.getEntry(wrd);
                    final ICopyMob copy = CopyCaps.get(ctx.getSource().getEntity());
                    if (copy == null) throw Pokeplayer.ERROR_FAILED.create();
                    if (wrd.equals("check"))
                    {
                        ctx.getSource().sendSuccess(new TextComponent(copy.getCopiedID() + ""), false);
                        return 0;
                    }

                    if (var == null) copy.setCopiedID(null);
                    else copy.setCopiedID(var.getEntityType().getRegistryName());
                    CapabilitySync.sendUpdate(ctx.getSource().getEntity());
                    return 0;
                }));
        event.getDispatcher().register(command);
    }

    private static void onPlayerTick(final PlayerTickEvent event)
    {
        final ICopyMob copy = CopyCaps.get(event.player);

        if (event.player instanceof final ServerPlayer player)
        {
            final Entity cam = player.getCamera();
            if (cam != player && cam instanceof BotPlayer)
            {
                ICopyMob.copyPositions(player, cam);
                player.connection.teleport(player.getX(), player.getY(), player.getZ(), player.getYRot(), player
                        .getXRot());
            }
        }

        // If we are copied, then just use the mob's step height.
        if (copy != null && copy.getCopiedMob() != null)
        {
            if (!event.player.getPersistentData().contains("prevStepUp")) event.player.getPersistentData().putFloat(
                    "prevStepUp", event.player.maxUpStep);
            event.player.maxUpStep = copy.getCopiedMob().maxUpStep;
        }
        else if (event.player.getPersistentData().contains("prevStepUp"))
        {
            final float prev = event.player.getPersistentData().getFloat("prevStepUp");
            event.player.getPersistentData().remove("prevStepUp");
            event.player.maxUpStep = prev;
        }
    }

    private static void onCopySet(final CopySetEvent event)
    {
        if (event.newCopy == null && event.getEntity() instanceof Player)
        {
            final Player player = (Player) event.getEntity();
            if (!player.getAbilities().instabuild)
            {
                player.getAbilities().mayfly = false;
                player.onUpdateAbilities();
            }
        }
    }

    private static void onCopyTick(final CopyUpdateEvent event)
    {
        if (!(event.realEntity instanceof Player)) return;
        final Player player = (Player) event.realEntity;
        final Pose pose = event.realEntity.getPose();
        // Short mobs need to be able to walk properly in small spaces, so force
        // standing pose if not in water
        if (event.getEntity().getBbHeight() < 1 && pose == Pose.SWIMMING && !event.realEntity.isInWaterOrBubble())
            event.realEntity.setPose(Pose.STANDING);

        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(event.getEntity());
        if (pokemob != null)
        {
            Pokeplayer.setFlying(player, pokemob);
            Pokeplayer.updateFloating(player, pokemob);
            Pokeplayer.updateFlying(player, pokemob);
            Pokeplayer.updateSwimming(player, pokemob);
        }
        event.setCanceled(true);
    }

    private static void setFlying(final Player player, final IPokemob pokemob)
    {
        if (pokemob == null) return;
        final boolean fly = pokemob.floats() || pokemob.flys();
        if (!player.getAbilities().instabuild && player.getAbilities().mayfly != fly)
        {
            player.getAbilities().mayfly = fly;
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
        if (pokemob == null) return;
        if (!player.isShiftKeyDown() && pokemob.floats() && !player.isFallFlying())
        {
            // TODO fix floating effects
        }
    }

    private static void updateSwimming(final Player player, final IPokemob pokemob)
    {
        if (pokemob == null) return;
        if (pokemob.getPokedexEntry().swims() || pokemob.isType(PokeType.getType("water"))) player.setAirSupply(300);
    }
}
