package pokecube.core.gimmicks.dynamax;

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
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraft.world.entity.ai.attributes.Attributes;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.SharedAttributes;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.api.entity.pokemob.commandhandlers.ChangeFormHandler;
import pokecube.api.entity.pokemob.commandhandlers.ChangeFormHandler.IChangeHandler;
import pokecube.api.events.pokemobs.ChangeForm;
import pokecube.api.events.pokemobs.InitAIEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.logic.LogicBase;
import pokecube.core.blocks.maxspot.MaxTile;
import pokecube.core.database.Database;
import pokecube.core.entity.pokemobs.genetics.genes.DynamaxGene;
import pokecube.core.eventhandlers.SpawnHandler;
import pokecube.core.eventhandlers.PokemobEventsHandler.MegaEvoTicker;
import pokecube.core.eventhandlers.SpawnHandler.ForbiddenEntry;
import pokecube.core.handlers.PokecubePlayerDataHandler;
import thut.api.Tracker;
import thut.lib.TComponent;

/**
 * This class handles the dynamax mechanic
 *
 */
@Mod.EventBusSubscriber(bus = Bus.MOD, modid = PokecubeCore.MODID)
public class DynamaxHelper
{
    /**
     * Setup and register tera type stuff.
     */
    @SubscribeEvent
    public static void init(FMLLoadCompleteEvent event)
    {
        // Handles reverting from dynamax
        PokecubeAPI.POKEMOB_BUS.addListener(DynamaxHelper::onFormRevert);
        // Handles reverting from dynamax
        PokecubeAPI.POKEMOB_BUS.addListener(DynamaxHelper::postFormChange);
        // Handles adding the tick logic to automatically revert from dynamax.
        // If you want to replace this with an addon, you can put a
        // lower-priority listener for InitAIEvent.Post, and then remove the
        // entry in getTickLogic() which is a DynaLogic
        PokecubeAPI.POKEMOB_BUS.addListener(DynamaxHelper::onAIAdd);
        ChangeFormHandler.addChangeHandler(new DynaMaxer());
    }

    /**
     * Implements the tick logic for handling automatic dyna reversion after the
     * configured delay. This sub-class is public so other addons can disable it
     * if they want to.
     *
     */
    public static class DynaLogic extends LogicBase
    {
        private long dynatime = -1;
        private boolean de_dyna = false;

        public DynaLogic(IPokemob pokemob)
        {
            super(pokemob);
        }

        @Override
        public void tick(Level world)
        {
            super.tick(world);
            if (world.isClientSide()) return;

            boolean isDyna = DynamaxHelper.isDynamax(this.pokemob);
            // check dynamax timer for cooldown.
            if (isDyna)
            {
                final long time = Tracker.instance().getTick();
                int dynaEnd = this.entity.getPersistentData().getInt("pokecube:dynadur");
                this.dynatime = this.entity.getPersistentData().getLong("pokecube:dynatime");
                if (!this.de_dyna && time - dynaEnd > this.dynatime)
                {
                    Component mess = TComponent.translatable("pokemob.dynamax.timeout.revert",
                            this.pokemob.getDisplayName());
                    this.pokemob.displayMessageToOwner(mess);

                    final PokedexEntry newEntry = this.pokemob.getBasePokedexEntry();
                    mess = TComponent.translatable("pokemob.dynamax.revert", this.pokemob.getDisplayName());
                    MegaEvoTicker.scheduleRevert(PokecubeCore.getConfig().evolutionTicks / 2, newEntry, pokemob, mess);
                    if (PokecubeCore.getConfig().debug_commands) PokecubeAPI.logInfo("Reverting Dynamax");

                    this.de_dyna = true;
                    this.dynatime = -1;
                }
            }
            else
            {
                this.dynatime = -1;
                this.de_dyna = false;
            }
        }

    }

    /**
     * Handles dynamaxing on command. Checks for valid max spots, etc.
     */
    private static class DynaMaxer implements IChangeHandler
    {
        @Override
        public boolean handleChange(IPokemob pokemob)
        {
            final LivingEntity owner = pokemob.getOwner();

            final Entity mob = pokemob.getEntity();
            Player player = owner instanceof Player p ? p : null;
            final Level world = mob.level();
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
                                // Flag as evolving for animation effects
                                pokemob.setGeneralState(GeneralStates.EVOLVING, true);
                                pokemob.setGeneralState(GeneralStates.EXITINGCUBE, false);
                                pokemob.setEvolutionTicks(PokecubeCore.getConfig().evolutionTicks + 50);
                                PokecubeAPI.POKEMOB_BUS.post(new ChangeForm.Pre(pokemob));
                            }, () -> {
                                DynamaxHelper.onDynamax(pokemob, PokecubeCore.getConfig().dynamax_duration);
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

    private static void onAIAdd(InitAIEvent.Post event)
    {
        event.getPokemob().getTickLogic().add(new DynaLogic(event.getPokemob()));
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

    private static AttributeModifier makeHealthBoost(double scale)
    {
        return new AttributeModifier(DYNAMOD, "pokecube:dynamax", scale, Operation.MULTIPLY_TOTAL);
    }

    /**
     * Applies the modifiers for when the mob has dynamaxed, This includes the
     * health boost and the size boost. Then also marks the duration and start
     * time for the dynamaxing.
     * 
     * @param pokemob
     * @param duration
     */
    public static void onDynamax(IPokemob pokemob, int duration)
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

    private static boolean isDynamax(IPokemob pokemob)
    {
        var entity = pokemob.getEntity();
        return entity.getPersistentData().contains("pokecube:dynadur");
    }
}
