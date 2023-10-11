package pokecube.api.utils;

import java.util.List;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.ai.attributes.Attributes;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.SharedAttributes;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.commandhandlers.ChangeFormHandler;
import pokecube.api.entity.pokemob.commandhandlers.ChangeFormHandler.IChangeHandler;
import pokecube.api.events.pokemobs.ChangeForm;
import pokecube.core.PokecubeCore;
import pokecube.core.blocks.maxspot.MaxTile;
import pokecube.core.entity.pokemobs.genetics.genes.DynamaxGene;
import pokecube.core.eventhandlers.SpawnHandler;
import pokecube.core.eventhandlers.PokemobEventsHandler.MegaEvoTicker;
import pokecube.core.eventhandlers.SpawnHandler.ForbiddenEntry;
import pokecube.core.handlers.PokecubePlayerDataHandler;
import thut.api.Tracker;
import thut.lib.TComponent;

public class DynamaxHelper
{
    public static void init()
    {
        PokecubeAPI.POKEMOB_BUS.addListener(DynamaxHelper::onFormRevert);
        ChangeFormHandler.addChangeHandler(new DynaMaxer());
    }

    public static class DynaMaxer implements IChangeHandler
    {
        @Override
        public boolean handleChange(IPokemob pokemob)
        {
            final LivingEntity owner = pokemob.getOwner();

            final Entity mob = pokemob.getEntity();
            Player player = owner instanceof Player p ? p : null;
            final Level world = mob.getLevel();
            final BlockPos pos = mob.blockPosition();
            final PokedexEntry entry = pokemob.getPokedexEntry();
            final Component oldName = pokemob.getDisplayName();

            // Check dynamax/gigantamax first.
            List<ForbiddenEntry> reasons = SpawnHandler.getForbiddenEntries(world, pos);
            boolean isMaxSpot = false;
            for (ForbiddenEntry e : reasons)
            {
                if (e.reason == MaxTile.MAXSPOT)
                {
                    isMaxSpot = true;
                    break;
                }
            }

            boolean isDyna = DynamaxHelper.isDynamax(pokemob);
            if (isMaxSpot)
            {
                PokedexEntry newEntry = entry;
                if (isDyna)
                {
                    Component mess = TComponent.translatable("pokemob.dynamax.command.revert", oldName);
                    pokemob.displayMessageToOwner(mess);
                    mess = TComponent.translatable("pokemob.dynamax.revert", oldName);
                    MegaEvoTicker.scheduleRevert(newEntry, pokemob, mess);
                    return true;
                }
                else
                {
                    final long dynatime = PokecubePlayerDataHandler.getCustomDataTag(owner.getUUID())
                            .getLong("pokecube:dynatime");
                    final long time = Tracker.instance().getTick();
                    final long dynaagain = dynatime + PokecubeCore.getConfig().dynamax_cooldown;
                    if (dynatime != 0 && time < dynaagain)
                    {
                        thut.lib.ChatHelper.sendSystemMessage(player,
                                TComponent.translatable("pokemob.dynamax.too_soon", pokemob.getDisplayName()));
                        return true;
                    }
                    Component mess = TComponent.translatable("pokemob.dynamax.command.evolve", oldName);
                    pokemob.displayMessageToOwner(mess);
                    mess = TComponent.translatable("pokemob.dynamax.success", oldName);
                    DynamaxHelper.dynamax(pokemob, PokecubeCore.getConfig().dynamax_duration);
                    MegaEvoTicker.scheduleEvolve(newEntry, pokemob, mess);
                    return true;
                }
            }
            PokedexEntry newEntry = entry;
            if (isDyna)
            {
                Component mess = TComponent.translatable("pokemob.dynamax.command.revert", oldName);
                pokemob.displayMessageToOwner(mess);
                mess = TComponent.translatable("pokemob.dynamax.revert", oldName);
                MegaEvoTicker.scheduleRevert(newEntry, pokemob, mess);
                return true;
            }
            return false;
        }

        @Override
        public String changeKey()
        {
            return "dynamax";
        }

        @Override
        public int getPriority()
        {
            return 10;
        }

    }

    private static void onDynaRevert(IPokemob pokemob)
    {
        var entity = pokemob.getEntity();
        entity.getPersistentData().remove("pokecube:dynatime");
    }

    private static void onFormRevert(ChangeForm.Revert event)
    {
        onDynaRevert(event.getPokemob());
    }

    private static final UUID DYNAMOD = new UUID(343523462346243l, 23453246267457l);

    public static AttributeModifier makeHealthBoost(double scale)
    {
        return new AttributeModifier(DYNAMOD, "pokecube:dynamax", scale, Operation.MULTIPLY_TOTAL);
    }

    public static void dynamax(IPokemob pokemob, int duration)
    {
        var entity = pokemob.getEntity();
        long time = Tracker.instance().getTick();
        entity.getPersistentData().putLong("pokecube:dynatime", time);
        entity.getPersistentData().putInt("pokecube:dynaend", duration);
        var info = DynamaxGene.getDyna(entity);
        float scale = 1.5f + 0.05f * info.dynaLevel;
        var hpBoost = makeHealthBoost(scale);
        entity.getAttribute(Attributes.MAX_HEALTH).addTransientModifier(hpBoost);

        if (entity.getAttributes().hasAttribute(SharedAttributes.MOB_SIZE_SCALE.get()))
        {
            var sizeBoost = new AttributeModifier(DYNAMOD, "pokecube:dynamax", PokecubeCore.getConfig().dynamax_scale,
                    Operation.MULTIPLY_TOTAL);
            entity.getAttribute(SharedAttributes.MOB_SIZE_SCALE.get()).addTransientModifier(sizeBoost);
        }
    }

    public static boolean isDynamax(IPokemob pokemob)
    {
        var entity = pokemob.getEntity();
        return entity.getPersistentData().contains("pokecube:dynatime");
    }
}
