package pokecube.gimmicks.builders.tasks;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import pokecube.api.entity.TeamManager;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.api.entity.pokemob.ai.LogicStates;
import pokecube.core.ai.tasks.utility.StoreTask;
import pokecube.core.ai.tasks.utility.UtilTask;
import pokecube.gimmicks.builders.BuilderTasks;
import pokecube.gimmicks.builders.builders.BuilderManager;
import pokecube.gimmicks.builders.builders.BuilderManager.BuildContext;
import pokecube.gimmicks.builders.builders.BuilderManager.BuilderClearer;
import pokecube.gimmicks.builders.builders.IBlocksBuilder.BoMRecord;

/**
 * This IAIRunnable assigns building tasks based on the instructions in a book
 * in the offhand slot.<br>
 * <br>
 * Format for the instructions is as follows:<br>
 * First page of a writable book should say:<br>
 * first line: t:key where key is jigsaw or building<br>
 * second line: resource location to look up<br>
 * following lines (optional):<br>
 * p: x y z - coordinates to shift by r: ROTATION - rotates the structure,
 * ROTATION is NONE, CLOCKWISE_90, CLOCKWISE_180 or COUNTERCLOCKWISE_90 <br>
 * <br>
 * Example book:<br>
 * build:jigsaw<br>
 * pokecube_legends:temples/surface/sky_pillar<br>
 * p: 0 1 0<br>
 * r: CLOCKWISE_90<br>
 * m: NONE<br>
 */
public class ManageBuild extends UtilTask implements INBTSerializable<CompoundTag>
{
    public static final String KEY = "builder_manager";

    final StoreTask storage;

    boolean hasInstructions = false;
    boolean loadedBuild = false;
    ItemStack last = ItemStack.EMPTY;
    BuilderClearer build;
    int timer = 0;
    List<IPokemob> minions = new ArrayList<>();

    public ManageBuild(IPokemob pokemob, StoreTask storage)
    {
        super(pokemob);
        this.storage = storage;
    }

    @Override
    public void reset()
    {
        hasInstructions = false;
        build = null;
    }

    /**
     * Swaps the book to the main hand, so it is no longer read for
     * instructions.
     */
    private void swapHands()
    {
        ItemStack main = entity.getMainHandItem();
        ItemStack off = entity.getOffhandItem();
        entity.setItemInHand(InteractionHand.MAIN_HAND, off);
        entity.setItemInHand(InteractionHand.OFF_HAND, main);
        pokemob.getInventory().setChanged();
    }

    public void setBuilder(BuilderClearer builder, ServerLevel level, List<IPokemob> pokemobs)
    {
        var pair = storage.getInventory(level, storage.storageLoc, Direction.UP);
        if (pair == null) return;

        this.build = builder;

        for (var pokemob : pokemobs)
        {
            var task = pokemob.getNamedTaskes().get(DoBuild.KEY);
            if (!(task instanceof DoBuild build) || build.builder != null) continue;

            build.storage.storageLoc = storage.storageLoc;
            build.storage.storageFace = storage.storageFace;

            if (pokemob != this.pokemob)
            {
                build.BoM = new BoMRecord(() -> pokemob.getEntity().getOffhandItem(),
                        _book -> pokemob.getEntity().setItemInHand(InteractionHand.OFF_HAND, _book));
            }
            if (!minions.contains(pokemob)) minions.add(pokemob);
            build.setBuilder(builder, level);
        }
    }

    @Override
    public void run()
    {
        var storeLoc = storage.storageLoc;
        // Only run if we actually have storage (and are server side)
        if (storeLoc == null || !(entity.level instanceof ServerLevel level) || build == null) return;

        // Only run this every 2.5 seconds or so
        if (entity.tickCount - timer < 50) return;
        timer = entity.tickCount;

        var builder = build.builder();
        var clearer = build.clearer();

        boolean creative = pokemob.getOwner() instanceof ServerPlayer player
                && (player.isCreative() || player.isSpectator());
        // Initialise the level, this ensures that it loads properly from nbt if
        // saved. This also calls an initial init for all of the builders
        if (builder != null && builder.getLevel() == null)
        {
            builder.setCreative(creative);
            builder.update(level);
        }
        if (clearer != null && clearer.getLevel() == null)
        {
            clearer.setCreative(creative);
            clearer.update(level);
        }

        // This means we are finished
        if (builder != null && !builder.validBuilder())
        {
            // Swap held items in this case. This prevents us immediately
            // trying to make a jigsaw again.
            swapHands();
            reset();
            return;
        }

        // Debug prints for jigsaws
//        System.out.println(this.jigsaw + " " + (this.jigsaw != null ? this.jigsaw.builders.size() : 0));
//        if (jigsaw != null && jigsaw.builders.size() > 0) System.out.println(this.jigsaw.builders.get(0).workers);

        // Building managers sit down near their storage.
        if (storeLoc.distManhattan(entity.getOnPos()) > 3)
        {
            // Path to it if too far.
            setWalkTo(storeLoc, 1, 1);
        }
        else
        {
            // Otherwise just sit down.
            pokemob.setLogicState(LogicStates.SITTING, true);
        }

        // Here we find a set of minions to make do the work
        List<IPokemob> pokemobs = new ArrayList<>();
        if (this.entity.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES))
        {
            Iterable<LivingEntity> visible = this.entity.getBrain()
                    .getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).get().findAll(e ->
                    {
                        IPokemob p = PokemobCaps.getPokemobFor(e);
                        if (p == null) return false;
                        if (!TeamManager.sameTeam(e, entity)) return false;
                        return p.getGeneralState(GeneralStates.STAYING) && p.isRoutineEnabled(BuilderTasks.BUILD)
                                && p.getNamedTaskes().containsKey(DoBuild.KEY);
                    });
            // Only allow valid guard targets.
            for (var o : visible) pokemobs.add(PokemobCaps.getPokemobFor(o));
        }

        // If no minions found, I guess we do it ourselves.
        if (pokemobs.isEmpty() && pokemob.isRoutineEnabled(BuilderTasks.BUILD) && minions.isEmpty())
        {
            pokemobs.add(pokemob);
        }

        // If we had found minions before, re-add them now.
        if (!minions.isEmpty())
        {
            for (var m : minions) if (!pokemobs.contains(m) && m.getEntity().isAddedToWorld()) pokemobs.add(m);
        }

        if (pokemobs.size() > 1 && pokemobs.contains(pokemob)) pokemobs.remove(pokemob);

        // No workers?? (maybe we are not allowed to build via AI button)
        if (pokemobs.isEmpty()) return;

        // If we have a builder, check the done status and reset if needed.
        if (builder != null && !builder.validBuilder())
        {
            for (var m : minions)
            {
                var task = m.getNamedTaskes().get(DoBuild.KEY);
                if (!(task instanceof DoBuild build) || build.builder != builder) continue;
                build.builder = null;
            }
        }
        this.setBuilder(build, level, pokemobs);
    }

    @Override
    public boolean shouldRun()
    {
        if (loadedBuild)
        {
            loadedBuild = false;
            last = pokemob.getEntity().getOffhandItem();
            hasInstructions = true;
        }

        if (last != pokemob.getEntity().getOffhandItem() && entity.level instanceof ServerLevel level)
        {
            boolean hadInstructions = hasInstructions;
            last = pokemob.getEntity().getOffhandItem();
            hasInstructions = false;

            var pair = storage.getInventory(level, storage.storageLoc, Direction.UP);
            if (pair == null) return false;

            BlockPos origin = pokemob.getHome();
            ServerPlayer owner = null;
            if (pokemob.getOwner() instanceof ServerPlayer player) owner = player;
            BuildContext context = new BuildContext(level, origin, owner);
            this.build = BuilderManager.fromInstructions(last, context);
            hasInstructions = this.build != null;
            if (hasInstructions && build.saveKey().equals("save"))
            {
                swapHands();
                hasInstructions = false;
            }

            if (hadInstructions && !hasInstructions)
            {
                for (var m : minions)
                {
                    var task = m.getNamedTaskes().get(DoBuild.KEY);
                    if (!(task instanceof DoBuild build) || build.builder == null) continue;
                    build.reset();
                }
            }
            timer = entity.tickCount;
        }
        return hasInstructions;
    }

    @Override
    public String getIdentifier()
    {
        return KEY;
    }

    @Override
    public CompoundTag serializeNBT()
    {
        CompoundTag nbt = new CompoundTag();
        if (this.build != null)
        {
            nbt.put("builder", BuilderManager.save(build));
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt)
    {
        if (nbt.contains("builder", Tag.TAG_COMPOUND))
        {
            this.build = BuilderManager.load(nbt.getCompound("builder"));
            hasInstructions = this.build != null;
            loadedBuild = this.build != null;
        }
    }

}
