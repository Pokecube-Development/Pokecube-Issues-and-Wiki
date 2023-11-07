package pokecube.legends.spawns;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.events.pokemobs.SpawnEvent.SpawnContext;
import pokecube.api.stats.ISpecialCaptureCondition;
import pokecube.api.stats.ISpecialSpawnCondition;
import pokecube.api.stats.ISpecialSpawnCondition.CanSpawn;
import pokecube.api.stats.SpecialCaseRegister;
import pokecube.api.utils.TagNames;
import pokecube.api.utils.Tools;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.handlers.PokecubePlayerDataHandler;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.conditions.AbstractCondition;
import pokecube.legends.conditions.data.Conditions.Spawn;
import thut.api.Tracker;
import thut.api.maths.Vector3;
import thut.lib.TComponent;

public class LegendarySpawn
{
    private static enum SpawnResult
    {
        SUCCESS, WRONGITEM, NOCAPTURE, ALREADYHAVE, NOSPAWN, FAIL;
    }

    private static List<LegendarySpawn> spawns = Lists.newArrayList();
    public static List<LegendarySpawn> data_spawns = Lists.newArrayList();

    private static List<LegendarySpawn> getForBlock(final BlockState state)
    {
        final List<LegendarySpawn> matches = Lists.newArrayList();
        LegendarySpawn.spawns.forEach(s -> {
            if (s.spawn.getTarget().test(state)) matches.add(s);
        });
        LegendarySpawn.data_spawns.forEach(s -> {
            if (s.spawn.getTarget().test(state)) matches.add(s);
        });
        return matches;
    }

    private static SpawnResult trySpawn(final LegendarySpawn spawn, final ItemStack stack,
            final PlayerInteractEvent.RightClickBlock evt, SpawnContext context, final boolean message)
    {
        final ServerPlayer playerIn = context.player();
        final ServerLevel worldIn = context.level();
        final PokedexEntry entry = context.entry();

        final SpawnResult result = !spawn.spawn.getKey().test(stack) ? SpawnResult.WRONGITEM : SpawnResult.FAIL;

        final ISpecialSpawnCondition spawnCondition = SpecialCaseRegister.getSpawnCondition(entry);
        final ISpecialCaptureCondition captureCondition = SpecialCaseRegister.getCaptureCondition(entry);
        if (spawnCondition != null)
        {
            final Vector3 location = new Vector3().set(evt.getPos());
            if (spawnCondition instanceof AbstractCondition sCond) sCond.setSpawnRule(spawn.spawn);

            final CanSpawn test = spawnCondition.canSpawn(context, message);

            // Priority of errors:
            //
            // Wrong location
            // Wrong item
            // Already have

            if (test == CanSpawn.NOTHERE) return SpawnResult.NOSPAWN;
            if (result == SpawnResult.WRONGITEM) return result;
            if (test == CanSpawn.ALREADYHAVE) return SpawnResult.ALREADYHAVE;

            if (test.test())
            {
                Mob entity = PokecubeCore.createPokemob(entry, worldIn);
                final IPokemob pokemob = PokemobCaps.getPokemobFor(entity);
                if (captureCondition != null && !captureCondition.canCapture(playerIn, pokemob))
                {
                    if (message && captureCondition instanceof AbstractCondition cond)
                        cond.canCapture(playerIn, message);
                    evt.setCanceled(true);
                    return SpawnResult.NOCAPTURE;
                }
                // This puts player on a cooldown for respawning the mob
                PokecubePlayerDataHandler.getCustomDataTag(playerIn).putLong("spwned:" + entry.getTrimmedName(),
                        Tracker.instance().getTick());
                // Mob gets spawnedby to prevent others from capturing
                entity.getPersistentData().putUUID("spwnedby", playerIn.getUUID());
                // These prevent drops and the mob disappearing when it dies
                entity.getPersistentData().putBoolean(TagNames.NOPOOF, true);
                entity.getPersistentData().putBoolean(TagNames.NODROP, true);

                entity.setHealth(entity.getMaxHealth());
                location.add(0, 1, 0).moveEntity(entity);
                spawnCondition.onSpawn(pokemob);
                playerIn.getMainHandItem().setCount(0);
                if (PokecubeLegends.config.singleUseLegendSpawns)
                    worldIn.setBlockAndUpdate(evt.getPos(), Blocks.AIR.defaultBlockState());
                if (pokemob.getExp() < 100)
                    entity = pokemob.setForSpawn(Tools.levelToXp(entry.getEvolutionMode(), 50)).getEntity();
                worldIn.addFreshEntity(entity);
                evt.setCanceled(true);
                return SpawnResult.SUCCESS;
            }
            else return test == CanSpawn.NO ? result : SpawnResult.NOSPAWN;
        }
        return SpawnResult.FAIL;
    }

    /**
     * Check if we match a spawn condition, and if so, give appropriate
     * messages.
     *
     * @param evt
     */
    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void interactRightClickBlock(final PlayerInteractEvent.RightClickBlock evt)
    {
        if (!(evt.getWorld() instanceof ServerLevel level)) return;
        final BlockState state = level.getBlockState(evt.getPos());
        final List<LegendarySpawn> matches = LegendarySpawn.getForBlock(state);
        if (matches.isEmpty()) return;
        final long now = Tracker.instance().getTick();
        final boolean repeated = evt.getPlayer().getPersistentData().getLong("pokecube_legends:msgtick") != now;
        if (!repeated || evt.getPlayer().isCrouching()) return;
        evt.getPlayer().getPersistentData().putLong("pokecube_legends:msgtick", now);

        final Vector3 location = new Vector3().set(evt.getPos());

        ItemStack stack = evt.getItemStack();
        // Try both hands just incase.
        if (stack.isEmpty()) stack = evt.getPlayer().getMainHandItem();
        if (stack.isEmpty()) stack = evt.getPlayer().getOffhandItem();

        if (stack.isEmpty())
        {
            Collections.shuffle(matches);
            LegendarySpawn match = matches.get(0);
            PokedexEntry entry = match.entry;
            for (final LegendarySpawn match1 : matches)
            {
                match = match1;
                entry = match1.entry;
                final ISpecialSpawnCondition spawnCondition = SpecialCaseRegister.getSpawnCondition(entry);
                if (spawnCondition == null) continue;
                SpawnContext context = new SpawnContext((ServerPlayer) evt.getPlayer(), level, entry, location);
                if (spawnCondition.canSpawn(context, false).test()) break;
            }
            evt.getPlayer().displayClientMessage(TComponent.translatable("msg.noitem.info",
                    TComponent.translatable(match.entry.getUnlocalizedName())), true);
            evt.getPlayer().getPersistentData().putLong("pokecube_legends:msgtick", Tracker.instance().getTick());
            return;
        }

        boolean worked = false;
        SpawnResult result = SpawnResult.FAIL;
        final List<PokedexEntry> wrong_items = Lists.newArrayList();
        final List<PokedexEntry> wrong_biomes = Lists.newArrayList();
        final List<PokedexEntry> already_spawned = Lists.newArrayList();

        for (final LegendarySpawn match : matches)
        {
            SpawnContext context = new SpawnContext((ServerPlayer) evt.getPlayer(), level, match.entry, location);
            result = LegendarySpawn.trySpawn(match, stack, evt, context, false);
            worked = result == SpawnResult.SUCCESS;
            if (worked) break;

            item:
            if (result == SpawnResult.WRONGITEM)
            {
                final ISpecialSpawnCondition spawnCondition = SpecialCaseRegister.getSpawnCondition(match.entry);
                if (spawnCondition != null)
                {
                    if (spawnCondition instanceof AbstractCondition sCond) sCond.setSpawnRule(match.spawn);
                    final CanSpawn test = spawnCondition.canSpawn(context, false);
                    if (test == CanSpawn.ALREADYHAVE) break item;
                }
                wrong_items.add(match.entry);
            }
            if (result == SpawnResult.NOSPAWN) wrong_biomes.add(match.entry);
            if (result == SpawnResult.ALREADYHAVE) already_spawned.add(match.entry);
            // Try again but with message, as this probably has a custom one.
            if (result == SpawnResult.FAIL || result == SpawnResult.NOCAPTURE)
            {
                // This should have given the error message itself, so lets
                // report that, then return
                LegendarySpawn.trySpawn(match, stack, evt, context, true);
                return;
            }
        }

        wrong_items.removeAll(wrong_biomes);

        if (worked)
        {
            evt.setCanceled(true);
            evt.setUseItem(Result.DENY);
            evt.setUseBlock(Result.DENY);
            return;
        }
        if (already_spawned.size() > 0)
        {
            Collections.shuffle(already_spawned);
            evt.getPlayer().displayClientMessage(TComponent.translatable("msg.alreadyspawned.info",
                    TComponent.translatable(already_spawned.get(0).getUnlocalizedName())), true);
            return;
        }
        if (wrong_items.size() > 0)
        {
            Collections.shuffle(wrong_items);
            evt.getPlayer().displayClientMessage(TComponent.translatable("msg.wrongitem.info",
                    TComponent.translatable(wrong_items.get(0).getUnlocalizedName())), true);
            return;
        }
        if (wrong_biomes.size() > 0)
        {
            Collections.shuffle(wrong_biomes);
            evt.getPlayer().displayClientMessage(TComponent.translatable("msg.nohere.info",
                    TComponent.translatable(matches.get(0).entry.getUnlocalizedName())), true);
            return;
        }

    }

    private final Spawn spawn;
    private final PokedexEntry entry;

    public LegendarySpawn(final String entry, Spawn spawn, boolean data_based)
    {
        this.entry = Database.getEntry(entry);
        this.spawn = spawn;
        if (this.entry == null)
            PokecubeAPI.LOGGER.warn("Tried to register spawn entry for {}, which is not a valid entry!", entry);
        else if (!data_based) LegendarySpawn.spawns.add(this);
    }
}
