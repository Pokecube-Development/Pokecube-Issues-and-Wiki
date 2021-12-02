package pokecube.legends.spawns;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import com.google.common.collect.Lists;

import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.RegistryObject;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.stats.ISpecialCaptureCondition;
import pokecube.core.database.stats.ISpecialSpawnCondition;
import pokecube.core.database.stats.ISpecialSpawnCondition.CanSpawn;
import pokecube.core.handlers.PokecubePlayerDataHandler;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.utils.TagNames;
import pokecube.core.utils.Tools;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.conditions.AbstractCondition;
import thut.api.Tracker;
import thut.api.maths.Vector3;

public class LegendarySpawn
{
    private static enum SpawnResult
    {
        SUCCESS, WRONGITEM, NOCAPTURE, ALREADYHAVE, NOSPAWN, FAIL;
    }

    private static List<LegendarySpawn> spawns      = Lists.newArrayList();
    public static List<LegendarySpawn>  data_spawns = Lists.newArrayList();

    private static List<LegendarySpawn> getForBlock(final BlockState state)
    {
        final List<LegendarySpawn> matches = Lists.newArrayList();
        LegendarySpawn.spawns.forEach(s ->
        {
            if (s.targetBlockChecker.test(state)) matches.add(s);
        });
        LegendarySpawn.data_spawns.forEach(s ->
        {
            if (s.targetBlockChecker.test(state)) matches.add(s);
        });
        return matches;
    }

    private static SpawnResult trySpawn(final LegendarySpawn spawn, final ItemStack stack,
            final PlayerInteractEvent.RightClickBlock evt, final boolean message)
    {
        final Player playerIn = evt.getPlayer();
        final ServerLevel worldIn = (ServerLevel) evt.getWorld();
        final PokedexEntry entry = spawn.entry;

        final SpawnResult result = !spawn.heldItemChecker.test(stack) ? SpawnResult.WRONGITEM : SpawnResult.FAIL;

        final ISpecialSpawnCondition spawnCondition = ISpecialSpawnCondition.spawnMap.get(entry);
        final ISpecialCaptureCondition captureCondition = ISpecialCaptureCondition.captureMap.get(entry);
        if (spawnCondition != null)
        {
            final Vector3 location = Vector3.getNewVector().set(evt.getPos());
            final CanSpawn test = spawnCondition.canSpawn(playerIn, location, message);
            if (test == CanSpawn.ALREADYHAVE) return SpawnResult.ALREADYHAVE;
            if (test.test())
            {
                if (result == SpawnResult.WRONGITEM) return result;
                Mob entity = PokecubeCore.createPokemob(entry, worldIn);
                final IPokemob pokemob = CapabilityPokemob.getPokemobFor(entity);
                if (captureCondition != null && !captureCondition.canCapture(playerIn, pokemob))
                {
                    if (message && captureCondition instanceof AbstractCondition) ((AbstractCondition) captureCondition)
                            .canCapture(playerIn, message);
                    evt.setCanceled(true);
                    return SpawnResult.NOCAPTURE;
                }
                // This puts player on a cooldown for respawning the mob
                PokecubePlayerDataHandler.getCustomDataTag(playerIn).putLong("spwned:" + entry.getTrimmedName(), Tracker
                        .instance().getTick());
                // Mob gets spawnedby to prevent others from capturing
                entity.getPersistentData().putUUID("spwnedby", playerIn.getUUID());
                // These prevent drops and the mob disappearing when it dies
                entity.getPersistentData().putBoolean(TagNames.NOPOOF, true);
                entity.getPersistentData().putBoolean(TagNames.NODROP, true);

                entity.setHealth(entity.getMaxHealth());
                location.add(0, 1, 0).moveEntity(entity);
                spawnCondition.onSpawn(pokemob);
                playerIn.getMainHandItem().setCount(0);
                if (PokecubeLegends.config.singleUseLegendSpawns) worldIn.setBlockAndUpdate(evt.getPos(), Blocks.AIR
                        .defaultBlockState());
                if (pokemob.getExp() < 100) entity = pokemob.setForSpawn(Tools.levelToXp(entry.getEvolutionMode(), 50))
                        .getEntity();
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
        if (!(evt.getWorld() instanceof ServerLevel)) return;
        final BlockState state = evt.getWorld().getBlockState(evt.getPos());
        final List<LegendarySpawn> matches = LegendarySpawn.getForBlock(state);
        if (matches.isEmpty()) return;
        final long now = Tracker.instance().getTick();
        final boolean repeated = evt.getPlayer().getPersistentData().getLong("pokecube_legends:msgtick") != now;
        if (!repeated || evt.getPlayer().isCrouching()) return;
        evt.getPlayer().getPersistentData().putLong("pokecube_legends:msgtick", now);

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
                final ISpecialSpawnCondition spawnCondition = ISpecialSpawnCondition.spawnMap.get(entry);
                if (spawnCondition == null) continue;
                final Vector3 location = Vector3.getNewVector().set(evt.getPos());
                if (spawnCondition.canSpawn(evt.getPlayer(), location, false).test()) break;
            }
            evt.getPlayer().displayClientMessage(new TranslatableComponent("msg.noitem.info",
                    new TranslatableComponent(match.entry.getUnlocalizedName())), true);
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
            result = LegendarySpawn.trySpawn(match, stack, evt, false);
            worked = result == SpawnResult.SUCCESS;
            if (worked) break;

            if (result == SpawnResult.WRONGITEM) wrong_items.add(match.entry);
            if (result == SpawnResult.NOSPAWN) wrong_biomes.add(match.entry);
            if (result == SpawnResult.ALREADYHAVE) already_spawned.add(match.entry);
            // Try again but with message, as this probably has a custom one.
            if (result == SpawnResult.FAIL || result == SpawnResult.NOCAPTURE)
            {
                // This should have given the error message itself, so lets
                // report that, then return
                LegendarySpawn.trySpawn(match, stack, evt, true);
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
            evt.getPlayer().displayClientMessage(new TranslatableComponent("msg.alreadyspawned.info",
                    new TranslatableComponent(already_spawned.get(0).getUnlocalizedName())), true);
            return;
        }

        if (wrong_items.size() > 0)
        {
            Collections.shuffle(wrong_items);
            evt.getPlayer().displayClientMessage(new TranslatableComponent("msg.wrongitem.info",
                    new TranslatableComponent(wrong_items.get(0).getUnlocalizedName())), true);
            return;
        }
        if (wrong_biomes.size() > 0)
        {
            Collections.shuffle(wrong_biomes);
            evt.getPlayer().displayClientMessage(new TranslatableComponent("msg.nohere.info",
                    new TranslatableComponent(matches.get(0).entry.getUnlocalizedName())), true);
            return;
        }

    }

    private final Predicate<ItemStack>  heldItemChecker;
    private final Predicate<BlockState> targetBlockChecker;

    private final PokedexEntry entry;

    public LegendarySpawn(final String entry, final Item held, final Block target)
    {
        this(entry, (c) -> c.getItem() == held, (b) -> b.getBlock() == target, false);
    }

    public LegendarySpawn(final String entry, final RegistryObject<Item> held, final Block target)
    {
        this(entry, (c) -> c.getItem() == held.get(), (b) -> b.getBlock() == target, false);
    }

    public LegendarySpawn(final String entry, final RegistryObject<Item> held, final RegistryObject<Block> target)
    {
        this(entry, (c) -> c.getItem() == held.get(), (b) -> b.getBlock() == target.get(), false);
    }

    public LegendarySpawn(final String entry, final Predicate<ItemStack> heldItemChecker,
            final Predicate<BlockState> targetBlockChecker, final boolean data_based)
    {
        this.heldItemChecker = heldItemChecker;
        this.targetBlockChecker = targetBlockChecker;
        this.entry = Database.getEntry(entry);
        if (this.entry == null) PokecubeCore.LOGGER.warn(
                "Tried to register spawn entry for {}, which is not a valid entry!", entry);
        else if (!data_based) LegendarySpawn.spawns.add(this);
    }
}
