package pokecube.legends.conditions;

import com.google.common.collect.Lists;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import pokecube.api.data.PokedexEntry;
import pokecube.api.data.PokedexEntry.SpawnData;
import pokecube.api.data.PokedexEntry.SpawnData.SpawnEntry;
import pokecube.api.data.spawns.SpawnBiomeMatcher;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.events.pokemobs.SpawnEvent.SpawnContext;
import pokecube.api.stats.CaptureStats;
import pokecube.api.stats.ISpecialCaptureCondition;
import pokecube.api.stats.ISpecialSpawnCondition;
import pokecube.api.stats.KillStats;
import pokecube.api.stats.SpecialCaseRegister;
import pokecube.api.utils.PokeType;
import pokecube.core.eventhandlers.SpawnHandler;
import pokecube.core.handlers.PokecubePlayerDataHandler;
import pokecube.core.utils.PokemobTracker;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.conditions.data.Conditions.Spawn;
import thut.api.Tracker;
import thut.api.item.ItemList;
import thut.api.maths.Vector3;
import thut.lib.TComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class AbstractCondition implements ISpecialCaptureCondition, ISpecialSpawnCondition
{

    protected static boolean isBlock(final Level world, final ArrayList<Vector3> blocks, final Block toTest)
    {
        for (final Vector3 v : blocks) if (v.getBlock(world) != toTest) return false;
        return true;
    }

    protected static boolean isBlock(final Level world, final ArrayList<Vector3> blocks, final ResourceLocation toTest)
    {
        for (final Vector3 v : blocks) if (!ItemList.is(toTest, v.getBlockState(world))) return false;
        return true;
    }

    private Spawn spawnRules = null;

    public String customFailMesg = null;
    public Consumer<IPokemob> onFail = p -> {};

    public void setSpawnRule(Spawn spawn)
    {
        this.spawnRules = spawn;
    }

    private final List<Predicate<BlockState>> relevantBlocks = Lists.newArrayList();

    public boolean isRelevant(final BlockState state)
    {
        for (final Predicate<BlockState> check : this.relevantBlocks) if (check.test(state)) return true;
        return false;
    }

    @SuppressWarnings("unchecked")
    protected void setRelevant(final Object block)
    {
        if (block instanceof Block) this.setRelevant((Block) block);
        if (block instanceof BlockState) this.setRelevant((BlockState) block);
        if (block instanceof ResourceLocation) this.setRelevant(b -> ItemList.is((ResourceLocation) block, b));
        if (block instanceof Predicate<?> check) this.setRelevant((Predicate<BlockState>) check);
    }

    protected void setRelevant(final BlockState state)
    {
        this.setRelevant(b -> b == state);
    }

    protected void setRelevant(final Predicate<BlockState> checker)
    {
        this.relevantBlocks.add(checker);
    }

    protected void setRelevant(final Block block)
    {
        this.setRelevant(b -> b.getBlock() == block);
    }

    protected int spawnNumber(final PokeType type)
    {
        return SpecialCaseRegister.countSpawnableTypes(type == PokeType.unknown ? null : type);
    }

    protected int caughtNumber(final Entity trainer, final PokeType type)
    {
        if (type == PokeType.unknown) return CaptureStats.getNumberUniqueCaughtBy(trainer.getUUID());
        return CaptureStats.getUniqueOfTypeCaughtBy(trainer.getUUID(), type);
    }

    protected int killedNumber(final Entity trainer, final PokeType type)
    {
        if (type == PokeType.unknown) return KillStats.getNumberUniqueKilledBy(trainer.getUUID());
        return KillStats.getUniqueOfTypeKilledBy(trainer.getUUID(), type);
    }

    protected int caughtNumber(final Entity trainer, final PokedexEntry entry)
    {
        return CaptureStats.getTotalNumberOfPokemobCaughtBy(trainer.getUUID(), entry);
    }

    protected abstract boolean hasRequirements(Entity trainer);

    public boolean canCapture(final Entity trainer, final boolean message)
    {
        if (!this.canCapture(trainer))
        {
            if (message && trainer instanceof Player player)
                thut.lib.ChatHelper.sendSystemMessage(player, this.getFailureMessage(trainer));
            return false;
        }
        return true;
    }

    private boolean alreadyHas(final Entity trainer)
    {
        return CaptureStats.getTotalNumberOfPokemobCaughtBy(trainer.getUUID(), this.getEntry()) > 0;
    }

    protected PokedexEntry entry;

    public void setEntry(PokedexEntry entry)
    {
        this.entry = entry;
    }

    public final PokedexEntry getEntry()
    {
        return this.entry;
    }

    protected void onCapureFail(final IPokemob pokemob)
    {
        this.onFail.accept(pokemob);
    }

    @Override
    public final boolean canCapture(final Entity trainer)
    {
        if (trainer == null) return false;
        return this.hasRequirements(trainer);
    }

    @Override
    public void onSpawn(final IPokemob mob)
    {}

    @Override
    public CanSpawn canSpawn(final SpawnContext context)
    {
        if (context.player() == null) return CanSpawn.NO;
        // Already have one, cannot spawn again.
        if (this.alreadyHas(context.player())) return CanSpawn.ALREADYHAVE;

        final String tag = "spwned:" + this.getEntry().getTrimmedName();
        final boolean prevSpawn = PokecubePlayerDataHandler.getCustomDataTag(context.player()).contains(tag);
        if (!prevSpawn) return CanSpawn.YES;
        final long spwnDied = PokecubePlayerDataHandler.getCustomDataTag(context.player()).getLong(tag);
        final boolean prevDied = spwnDied > 0;
        if (prevDied)
        {
            final long now = Tracker.instance().getTick();
            final boolean doneCooldown = spwnDied + PokecubeLegends.config.respawnLegendDelay < now;
            if (doneCooldown)
            {
                PokecubePlayerDataHandler.getCustomDataTag(context.player()).remove(tag);
                PokecubePlayerDataHandler.saveCustomData(context.player());
                return CanSpawn.YES;
            }
        }
        return CanSpawn.ALREADYHAVE;
    }

    @Override
    public CanSpawn canSpawn(SpawnContext context, final boolean message)
    {
        final CanSpawn test = this.canSpawn(context);
        if (!test.test()) return test;

        SpawnData data = this.getEntry().getSpawnData();
        boolean canSpawnHere;
        if (spawnRules != null && spawnRules.location != null)
        {
            data = new SpawnData(context.entry());
            SpawnEntry entry = new SpawnEntry();
            data.matchers.put(SpawnBiomeMatcher.get(spawnRules.location), entry);
            canSpawnHere = SpawnHandler.canSpawn(data, context, false);
        }
        else canSpawnHere = data == null;
        if (canSpawnHere)
        {
            boolean here = PokemobTracker.countPokemobs(context.location(), context.level(), 32, this.getEntry()) > 0;
            return here ? CanSpawn.ALREADYHERE : CanSpawn.YES;
        }
        if (message) this.sendNoHere(context.player());
        return CanSpawn.NOTHERE;
    }

    @Override
    public final boolean canCapture(final Entity trainer, final IPokemob pokemon)
    {
        boolean succeed = true;
        if (pokemon.getEntity().getPersistentData().hasUUID("spwnedby"))
        {
            final UUID id = pokemon.getEntity().getPersistentData().getUUID("spwnedby");
            if (!trainer.getUUID().equals(id)) succeed = false;
        }
        if (succeed) succeed = this.canCapture(trainer);
        if (!succeed) this.onCapureFail(pokemon);
        return succeed;
    }

    @Override
    public void onCaptureFail(final Entity trainer, final IPokemob pokemob)
    {
        if (trainer instanceof Player player)
            thut.lib.ChatHelper.sendSystemMessage(player, this.getFailureMessage(trainer));
    }

    public MutableComponent sendNoTrust(final Entity trainer)
    {
        final String message = "msg.notrust.info";
        return TComponent.translatable(message, TComponent.translatable(this.getEntry().getUnlocalizedName()));
    }

    public MutableComponent sendNoHere(final Entity trainer)
    {
        final String message = "msg.nohere.info";
        final MutableComponent component = TComponent.translatable(message,
                TComponent.translatable(this.getEntry().getUnlocalizedName()));
        if (trainer instanceof Player player) thut.lib.ChatHelper.sendSystemMessage(player, component);
        return component;
    }

    // Basic Legend
    public MutableComponent sendLegend(final Entity trainer, final String type, final int numA, final int numB)
    {
        final String message = "msg.infolegend.info";
        final Component typeMess = TComponent.translatable(PokeType.getUnlocalizedName(PokeType.getType(type)));
        return TComponent.translatable(message, typeMess, numA + 1, numB);
    }

    // Catch specific Legend
    public MutableComponent sendLegendExtra(final Object names)
    {
        final String message = "msg.infolegendextra.info";
        return TComponent.translatable(message, names);
    }

    // Build Legend
    public void sendLegendBuild(final Entity trainer, final Object name)
    {
        final String message = "msg.reginotlookright.info";
        final MutableComponent component = TComponent.translatable(message, name);
        if (trainer instanceof Player player) player.displayClientMessage(component, true);
    }
}
