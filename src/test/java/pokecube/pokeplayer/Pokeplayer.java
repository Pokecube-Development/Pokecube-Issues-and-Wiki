package pokecube.pokeplayer;

import org.apache.commons.lang3.tuple.Pair;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.utils.PokeType;
import thut.api.entity.CopyCaps;
import thut.api.entity.ICopyMob;
import thut.api.entity.event.CopyUpdateEvent;
import thut.core.common.network.CapabilitySync;

@Mod(value = "pokecube_pokeplayer")
public class Pokeplayer
{

    public Pokeplayer()
    {
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(
                () -> FMLNetworkConstants.IGNORESERVERONLY, (in, net) -> true));

        // The commmand to turn into a pokemob
        MinecraftForge.EVENT_BUS.addListener(Pokeplayer::onCommandRegister);
        // We want to sync from copy to us, not other way, so handle that here.
        MinecraftForge.EVENT_BUS.addListener(Pokeplayer::onCopyTick);
        // This syncs step height for the mob over
        MinecraftForge.EVENT_BUS.addListener(Pokeplayer::onPlayerTick);

        CopyCaps.register(EntityType.PLAYER);
    }

    private static void onCommandRegister(final RegisterCommandsEvent event)
    {
        final LiteralArgumentBuilder<CommandSource> command = Commands.literal("pokeplayer").then(Commands.argument(
                "val", StringArgumentType.string()).executes(ctx ->
                {
                    final String wrd = StringArgumentType.getString(ctx, "val");

                    final PokedexEntry var = Database.getEntry(wrd);
                    final ICopyMob copy = CopyCaps.get(ctx.getSource().getEntity());
                    if (copy == null) throw new CommandException(new StringTextComponent("not copy?"));

                    if (wrd.equals("check"))
                    {
                        ctx.getSource().sendSuccess(new StringTextComponent(copy.getCopiedID() + ""), false);
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

    private static void onCopyTick(final CopyUpdateEvent event)
    {
        if (!(event.realEntity instanceof PlayerEntity)) return;
        final PlayerEntity player = (PlayerEntity) event.realEntity;
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

    private static void setFlying(final PlayerEntity player, final IPokemob pokemob)
    {
        if (pokemob == null) return;
        final boolean fly = pokemob.floats() || pokemob.flys();
        if (!player.abilities.instabuild && player.abilities.mayfly != fly)
        {
            player.abilities.mayfly = fly;
            player.onUpdateAbilities();
        }
    }

    private static void updateFlying(final PlayerEntity player, final IPokemob pokemob)
    {
        if (pokemob == null) return;
        if (pokemob.floats() || pokemob.flys())
        {
            player.fallDistance = 0;
            if (player instanceof ServerPlayerEntity) ((ServerPlayerEntity) player).connection.aboveGroundTickCount = 0;
        }
    }

    private static void updateFloating(final PlayerEntity player, final IPokemob pokemob)
    {
        if (pokemob == null) return;
        if (!player.isShiftKeyDown() && pokemob.floats() && !player.isFallFlying())
        {
            // TODO fix floating effects
        }
    }

    private static void updateSwimming(final PlayerEntity player, final IPokemob pokemob)
    {
        if (pokemob == null) return;
        if (pokemob.getPokedexEntry().swims() || pokemob.isType(PokeType.getType("water"))) player.setAirSupply(300);
    }
}
