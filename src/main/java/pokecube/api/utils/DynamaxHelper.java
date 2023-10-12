package pokecube.api.utils;

import java.util.List;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.api.entity.pokemob.commandhandlers.ChangeFormHandler;
import pokecube.api.entity.pokemob.commandhandlers.ChangeFormHandler.IChangeHandler;
import pokecube.api.events.pokemobs.ChangeForm;
import pokecube.core.PokecubeCore;
import pokecube.core.blocks.maxspot.MaxTile;
import pokecube.core.database.Database;
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
        PokecubeAPI.POKEMOB_BUS.addListener(DynamaxHelper::postFormChange);
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
                    newEntry = pokemob.getBasePokedexEntry();
                    mess = TComponent.translatable("pokemob.dynamax.revert", oldName);
                    MegaEvoTicker.scheduleRevert(newEntry, pokemob, mess);
                    return true;
                }
                else
                {
                    final long dynatime = PokecubePlayerDataHandler.getCustomDataTag(owner.getUUID())
                            .getLong("pokecube:dynatime");
                    final long time = Tracker.instance().getTick();
                    if (dynatime != 0 && time - dynatime < PokecubeCore.getConfig().dynamax_cooldown)
                    {
                        thut.lib.ChatHelper.sendSystemMessage(player,
                                TComponent.translatable("pokemob.dynamax.too_soon", pokemob.getDisplayName()));
                        return true;
                    }
                    var info = DynamaxGene.getDyna(mob);
                    if (info.gigantamax)
                    {
                        var gEntry = Database.getEntry(entry.getName() + "-gmax");
                        if (gEntry != null) newEntry = gEntry;
                    }

                    Component mess = TComponent.translatable("pokemob.dynamax.command.evolve", oldName);
                    pokemob.displayMessageToOwner(mess);
                    mess = TComponent.translatable("pokemob.dynamax.success", oldName);
                    PokecubePlayerDataHandler.getCustomDataTag(owner.getUUID()).putLong("pokecube:dynatime", time);
                    MegaEvoTicker.scheduleChange(PokecubeCore.getConfig().evolutionTicks, newEntry, pokemob, mess,
                            () ->
                            {
                                // Flag as evolving
                                pokemob.setGeneralState(GeneralStates.EVOLVING, true);
                                pokemob.setGeneralState(GeneralStates.EXITINGCUBE, false);
                                pokemob.setEvolutionTicks(PokecubeCore.getConfig().evolutionTicks + 50);
                                PokecubeAPI.POKEMOB_BUS.post(new ChangeForm.Pre(pokemob));
                            }, () -> {
                                DynamaxHelper.dynamax(pokemob, PokecubeCore.getConfig().dynamax_duration);
                                PokecubeAPI.POKEMOB_BUS.post(new ChangeForm.Post(pokemob));
                            });
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

        @Override
        public void onFail(IPokemob pokemob)
        {
            final LivingEntity owner = pokemob.getOwner();
            if (owner instanceof ServerPlayer player) thut.lib.ChatHelper.sendSystemMessage(player,
                    TComponent.translatable("pokemob.dynamax.failed", pokemob.getDisplayName()));
        }

    }

    private static void onFormRevert(ChangeForm.Revert event)
    {
        var entity = event.getPokemob().getEntity();
        entity.getPersistentData().putBoolean("pokecube:dyna_reverted", true);
    }

    private static void postFormChange(ChangeForm.Post event)
    {
        var entity = event.getPokemob().getEntity();
        if (entity.getPersistentData().contains("pokecube:dyna_reverted"))
        {
            entity.getPersistentData().remove("pokecube:dyna_reverted");
            entity.getPersistentData().remove("pokecube:dynadur");
            var hpAttr = entity.getAttribute(Attributes.MAX_HEALTH);
            hpAttr.removeModifier(DYNAMOD);
            // Reset health to clip to new maximum.
            entity.setHealth(Math.min(entity.getHealth(), entity.getMaxHealth()));

            if (entity.getAttributes().hasAttribute(SharedAttributes.MOB_SIZE_SCALE.get()))
            {
                var scaleAttr = entity.getAttribute(SharedAttributes.MOB_SIZE_SCALE.get());
                scaleAttr.removeModifier(DYNAMOD);
            }
        }
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
        entity.getPersistentData().putInt("pokecube:dynadur", duration);
        var info = DynamaxGene.getDyna(entity);
        float scale = 1.5f + 0.05f * info.dynaLevel;
        var hpBoost = makeHealthBoost(scale);
        var hpAttr = entity.getAttribute(Attributes.MAX_HEALTH);
        hpAttr.removeModifier(DYNAMOD);
        float health = entity.getMaxHealth();
        hpAttr.addPermanentModifier(hpBoost);
        float toAdd = entity.getMaxHealth() - health;
        entity.heal(toAdd);

        if (!info.gigantamax && entity.getAttributes().hasAttribute(SharedAttributes.MOB_SIZE_SCALE.get()))
        {
            var scaleAttr = entity.getAttribute(SharedAttributes.MOB_SIZE_SCALE.get());
            var sizeBoost = new AttributeModifier(DYNAMOD, "pokecube:dynamax", PokecubeCore.getConfig().dynamax_scale,
                    Operation.MULTIPLY_BASE);
            scaleAttr.removeModifier(DYNAMOD);
            scaleAttr.addPermanentModifier(sizeBoost);
        }
    }

    public static boolean isDynamax(IPokemob pokemob)
    {
        var entity = pokemob.getEntity();
        return entity.getPersistentData().contains("pokecube:dynadur");
    }
}
