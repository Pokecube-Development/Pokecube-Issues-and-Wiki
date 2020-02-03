package pokecube.legends.spawns;

import java.util.function.Predicate;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokedexEntry.SpawnData;
import pokecube.core.database.SpawnBiomeMatcher;
import pokecube.core.database.stats.ISpecialCaptureCondition;
import pokecube.core.database.stats.ISpecialSpawnCondition;
import pokecube.core.handlers.PokecubePlayerDataHandler;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.utils.Tools;
import thut.api.maths.Vector3;

public class LegendarySpawn
{
    private final Predicate<ItemStack>  heldItemChecker;
    private final Predicate<BlockState> targetBlockChecker;
    private final PokedexEntry          entry;

    public LegendarySpawn(final String entry, final Item held, final Block target)
    {
        this(Database.getEntry(entry), (c) -> c.getItem() == held, (b) -> b.getBlock() == target);
    }

    public LegendarySpawn(final PokedexEntry entry, final Item held, final Block target)
    {
        this(entry, (c) -> c.getItem() == held, (b) -> b.getBlock() == target);
    }

    public LegendarySpawn(final PokedexEntry entry, final Predicate<ItemStack> heldItemChecker,
            final Predicate<BlockState> targetBlockChecker)
    {
        this.heldItemChecker = heldItemChecker;
        this.targetBlockChecker = targetBlockChecker;
        this.entry = entry;
    }

    /**
     * Uses player interact here to also prevent opening of inventories.
     *
     * @param evt
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void interactRightClickBlock(final PlayerInteractEvent.RightClickBlock evt)
    {
        final boolean invalid = !evt.getPlayer().isCrouching() || this.heldItemChecker.test(evt.getItemStack()) || !(evt
                .getWorld() instanceof ServerWorld);
        if (invalid) return;
        final PlayerEntity playerIn = evt.getPlayer();
        final ServerWorld worldIn = (ServerWorld) evt.getWorld();
        final BlockPos pos = evt.getPos();
        final BlockState state = evt.getWorld().getBlockState(evt.getPos());
        if (this.targetBlockChecker.test(state))
        {
            final SpawnData data = this.entry.getSpawnData();
            if (data != null) for (final SpawnBiomeMatcher matcher : data.matchers.keySet())
                if (data.getWeight(matcher) > 0) return;
            final ISpecialSpawnCondition spawnCondition = ISpecialSpawnCondition.spawnMap.get(this.entry);
            final ISpecialCaptureCondition captureCondition = ISpecialCaptureCondition.captureMap.get(this.entry);
            if (spawnCondition != null)
            {
                final Vector3 location = Vector3.getNewVector().set(pos);
                if (spawnCondition.canSpawn(playerIn, location))
                {
                    MobEntity entity = PokecubeCore.createPokemob(this.entry, worldIn);
                    final IPokemob pokemob = CapabilityPokemob.getPokemobFor(entity);
                    if (captureCondition != null && !captureCondition.canCapture(playerIn, pokemob))
                    {
                        evt.setCanceled(true);
                        return;
                    }
                    PokecubePlayerDataHandler.getCustomDataTag(playerIn).putBoolean("spwn:" + this.entry
                            .getTrimmedName(), true);
                    entity.setHealth(entity.getMaxHealth());
                    location.add(0, 1, 0).moveEntity(entity);
                    spawnCondition.onSpawn(pokemob);
                    playerIn.getHeldItemMainhand().setCount(0);
                    worldIn.setBlockState(pos, Blocks.AIR.getDefaultState());
                    if (pokemob.getExp() < 100) entity = pokemob.setForSpawn(Tools.levelToXp(this.entry
                            .getEvolutionMode(), 50)).getEntity();
                    worldIn.addEntity(entity);
                }
            }
            evt.setCanceled(true);
        }
    }
}
