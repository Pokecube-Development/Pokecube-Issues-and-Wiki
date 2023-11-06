package pokecube.gimmicks.pokeplayer;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.utils.PokeType;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.network.pokemobs.PacketSyncGene;
import pokecube.core.utils.PokemobTracker;
import thut.api.ThutCaps;
import thut.api.entity.ICopyMob;
import thut.api.entity.event.CopySetEvent;
import thut.api.entity.event.CopyUpdateEvent;
import thut.api.entity.genetics.Alleles;
import thut.core.common.ThutCore;
import thut.lib.TComponent;

@Mod.EventBusSubscriber(bus = Bus.MOD, modid = PokecubeCore.MODID)
public class Pokeplayer
{
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
    }

    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(
            TComponent.translatable("not copy?"));

    private static void onCommandRegister(final RegisterCommandsEvent event)
    {
        final LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("pokeplayer")
                .then(Commands.argument("val", StringArgumentType.string()).executes(ctx ->
                {
                    final String wrd = StringArgumentType.getString(ctx, "val");

                    final PokedexEntry var = Database.getEntry(wrd);
                    final ICopyMob copy = ThutCaps.getCopyMob(ctx.getSource().getEntity());
                    if (copy == null) throw Pokeplayer.ERROR_FAILED.create();
                    System.out.println(var);
                    // TODO apply the species gene here.

                    return 0;
                }));
        event.getDispatcher().register(command);
    }

    @SuppressWarnings("deprecation")
    private static void onPlayerTick(final PlayerTickEvent event)
    {
        final ICopyMob copy = ThutCaps.getCopyMob(event.player);

        // If we are copied, then just use the mob's step height.
        if (copy != null && copy.getCopiedMob() != null)
        {
            if (!event.player.getPersistentData().contains("prevStepUp"))
                event.player.getPersistentData().putFloat("prevStepUp", event.player.maxUpStep);
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
        if (event.newCopy == null && event.getEntity() instanceof Player player)
        {
            if (!player.getAbilities().instabuild)
            {
                player.getAbilities().mayfly = false;
                player.onUpdateAbilities();
            }

            IPokemob newMob = PokemobCaps.getPokemobFor(event.newCopy);
            if (player.level.isClientSide())
            {
                IPokemob oldMob = PokemobCaps.getPokemobFor(event.oldCopy);
                if (newMob != null) PokemobTracker.addPokemob(newMob);
                if (oldMob != null) PokemobTracker.removePokemob(oldMob);
            }
        }
    }

    private static void onCopyTick(final CopyUpdateEvent event)
    {
        if (!(event.realEntity instanceof Player player)) return;
        final Pose pose = event.realEntity.getPose();
        // Short mobs need to be able to walk properly in small spaces, so force
        // standing pose if not in water
        if (event.getEntity().getBbHeight() < 1 && pose == Pose.SWIMMING && !event.realEntity.isInWaterOrBubble())
            event.realEntity.setPose(Pose.STANDING);

        final IPokemob pokemob = PokemobCaps.getPokemobFor(event.getEntity());
        if (pokemob != null)
        {
            Pokeplayer.setFlying(player, pokemob);
            Pokeplayer.updateFloating(player, pokemob);
            Pokeplayer.updateFlying(player, pokemob);
            Pokeplayer.updateSwimming(player, pokemob);

            if (player instanceof ServerPlayer splayer && splayer.tickCount % 20 == 0)
            {
                var mobGenes = ThutCaps.getGenetics(pokemob.getEntity());
                pokemob.getEntity().onAddedToWorld();
                for (final Alleles<?, ?> allele : mobGenes.getAlleles().values())
                {
                    PacketSyncGene.syncGeneToTracking(event.getEntity(), allele);
                    PacketSyncGene.syncGene(event.getEntity(), allele, splayer);
                }
                pokemob.getEntity().onRemovedFromWorld();
            }
        }
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
