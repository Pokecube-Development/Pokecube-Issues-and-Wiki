package pokecube.gimmicks.zmoves;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import com.google.common.collect.Maps;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.registries.NewRegistryEvent;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.commandhandlers.StanceHandler;
import pokecube.api.entity.pokemob.commandhandlers.StanceHandler.ModeInfo;
import pokecube.api.events.pokemobs.combat.MoveUse.DuringUse;
import pokecube.api.moves.MoveEntry;
import pokecube.api.moves.MoveEntry.PowerProvider;
import pokecube.api.utils.PokeType;
import pokecube.core.PokecubeCore;
import pokecube.core.database.tags.Tags;
import pokecube.core.items.ItemTM;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.implementations.MovesAdder;
import pokecube.gimmicks.dynamax.D_Move_Damage;
import pokecube.gimmicks.dynamax.DynamaxHelper;
import thut.api.Tracker;
import thut.api.Tracker.UpdateHandler;
import thut.core.common.ThutCore;
import thut.core.common.network.GeneralUpdate;

@Mod.EventBusSubscriber(bus = Bus.MOD, modid = PokecubeCore.MODID)
public class GZMoveManager
{
    public static int Z_MOVE_COOLDOWN = 600;

    public static Map<String, String> zmoves_map = Maps.newHashMap();
    public static Map<String, List<String>> z_sig_moves_map = Maps.newHashMap();
    private static Map<String, String> gmoves_map = Maps.newHashMap();
    private static Map<String, String> g_max_moves_map = Maps.newHashMap();

    private static Map<PokeType, MoveEntry> physical_z_moves_by_type = Maps.newHashMap();
    private static Map<PokeType, MoveEntry> special_z_moves_by_type = Maps.newHashMap();
    private static Map<PokeType, MoveEntry> d_moves_by_type = Maps.newHashMap();

    static final Pattern GMAXENTRY = Pattern.compile("(that_gigantamax_)(\\w+)(_use)");

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void preInit(final NewRegistryEvent event)
    {
        MovesAdder.moveProcessors.add(GZMoveManager::process);
        MovesAdder.moveValidators.add(GZMoveManager::isGZDMove);
    }

    @SubscribeEvent
    public static void init(FMLLoadCompleteEvent event)
    {
        ItemTM.INVALID_TMS.add(s -> isGZDMove(MovesUtils.getMove(s)));
        ThutCore.FORGE_BUS.addListener(GZMoveManager::onMobUpdate);
        PokecubeAPI.MOVE_BUS.addListener(GZMoveManager::postMoveUse);
        Tracker.HANDLERS.put(ZMoveModeHandler.HANDLER.getKey(), ZMoveModeHandler.HANDLER);
        StanceHandler.MODE_LISTENERS.put(StanceHandler.MODE, ZMoveModeHandler.HANDLER);
        postProcess();
    }

    @SubscribeEvent
    public static void registerCapabilities(final RegisterCapabilitiesEvent event)
    {
        // Initialize the capabilities.
        event.register(ZPower.class);
    }

    private static void onMobUpdate(LivingUpdateEvent event)
    {
        IPokemob pokemob = PokemobCaps.getPokemobFor(event.getEntity());
        if (pokemob == null || pokemob.getOwner() == null) return;
        LivingEntity owner = pokemob.getOwner();

        // we don't handle in this case!
        if (DynamaxHelper.isDynamax(pokemob)) return;

        long lastUse = owner.getPersistentData().getLong("pokecube:used-z-move");
        long tick = Tracker.instance().getTick();
        if (lastUse + Z_MOVE_COOLDOWN > tick) return;

        if (!pokemob.getEntity().getPersistentData().contains("pokecube:use-z-move")) return;
        boolean z_moves = pokemob.getEntity().getPersistentData().getBoolean("pokecube:use-z-move");
        if (!z_moves) pokemob.getEntity().getPersistentData().remove("pokecube:use-z-move");

        String[] g_z_moves = pokemob.getMoveStats().getMovesToUse();
        for (int i = 0; i < 4; i++)
        {
            String move = g_z_moves[i];
            String zmove = GZMoveManager.getZMove(pokemob, move);
            if (z_moves && zmove != null) g_z_moves[i] = zmove;
            else g_z_moves[i] = pokemob.getMoveStats().getBaseMoves()[i];
        }
    }

    private static void postMoveUse(DuringUse.Post event)
    {
        IPokemob pokemob = event.getUser();

        // we don't handle in this case!
        if (DynamaxHelper.isDynamax(pokemob)) return;
        
        LivingEntity owner = pokemob.getOwner();
        if (owner != null && isZMove(event.getMove()))
        {
            long tick = Tracker.instance().getTick();
            owner.getPersistentData().putLong("pokecube:used-z-move", tick);
            pokemob.getEntity().getPersistentData().remove("pokecube:use-z-move");

            String[] g_z_moves = pokemob.getMoveStats().getMovesToUse();
            for (int i = 0; i < 4; i++)
            {
                String move = pokemob.getMove(i);
                String zmove = GZMoveManager.getZMove(pokemob, move);
                if (zmove != null) g_z_moves[i] = pokemob.getMoveStats().getBaseMoves()[i];
            }
        }
    }

    public static class ZMoveModeHandler implements UpdateHandler, Consumer<ModeInfo>
    {
        public static ZMoveModeHandler HANDLER = new ZMoveModeHandler();

        @Override
        public void accept(ModeInfo t)
        {
            var pokemob = t.pokemob();
            if (pokemob.getEntity().getPersistentData().contains("pokecube:use-z-move"))
                pokemob.getEntity().getPersistentData().remove("pokecube:use-z-move");
            else pokemob.getEntity().getPersistentData().putBoolean("pokecube:use-z-move", true);

            CompoundTag nbt = new CompoundTag();
            nbt.putBoolean("M", pokemob.getEntity().getPersistentData().contains("pokecube:use-z-move"));
            nbt.putInt("I", pokemob.getEntity().getId());
            GeneralUpdate.sendToTracking(nbt, getKey(), pokemob.getEntity());
        }

        @Override
        public String getKey()
        {
            return "z-move-mode";
        }

        @Override
        public void read(CompoundTag nbt, ServerPlayer player)
        {
            Level level = null;
            // This case, it was sent from server to client, an update packet!
            if (player == null)
            {
                level = PokecubeCore.proxy.getPlayer().getLevel();
            }
            // Otherwise we use player's level
            else level = player.getLevel();

            int id = nbt.getInt("I");
            boolean mode = nbt.getBoolean("M");
            Entity e = PokecubeAPI.getEntityProvider().getEntity(level, id, true);
            if (e != null) e.getPersistentData().putBoolean("pokecube:use-z-move", mode);
        }

    }

    public static boolean isGZDMove(final MoveEntry entry)
    {
        return GZMoveManager.isZMove(entry) || GZMoveManager.isGMove(entry) || GZMoveManager.isDMove(entry);
    }

    public static boolean isZMove(final MoveEntry entry)
    {
        return Tags.MOVE.isIn("z-move", entry.name);
    }

    public static boolean isDMove(final MoveEntry entry)
    {
        return Tags.MOVE.isIn("d-move", entry.name);
    }

    public static boolean isGMove(final MoveEntry entry)
    {
        return Tags.MOVE.isIn("g-move", entry.name);
    }

    public static PowerProvider getPowerProvider(MoveEntry e)
    {
        return isZMove(e) ? Z_Move_Damage.INSTANCE : D_Move_Damage.INSTANCE;
    }

    public static void process(final MoveEntry move)
    {
        if (isZMove(move))
        {
            move.root_entry._manually_defined = true;
            if (PokecubeCore.getConfig().debug_data) PokecubeAPI.logInfo("Z-move: {}", move.name);
            if (move.name.endsWith("--special"))
            {
                special_z_moves_by_type.put(move.type, move);
            }
            if (move.name.endsWith("--physical"))
            {
                physical_z_moves_by_type.put(move.type, move);
            }
        }
        if (isDMove(move))
        {
            move.root_entry._manually_defined = true;
            if (PokecubeCore.getConfig().debug_data) PokecubeAPI.logInfo("D-move: {}", move.name);
            d_moves_by_type.put(move.type, move);
        }
    }

    public static void postProcess()
    {
        // Here we loop over the registered moves, and assign each one a Z or D
        // move accordingly.
        MoveEntry.values().forEach(move -> {
            d_moves_by_type.computeIfPresent(move.type, (type, zmove) -> {
                gmoves_map.put(move.name, zmove.name);
                return zmove;
            });
            switch (move.category)
            {
            case PHYSICAL:
                physical_z_moves_by_type.computeIfPresent(move.type, (type, zmove) -> {
                    zmoves_map.put(move.name, zmove.name);
                    return zmove;
                });
                break;
            default:
                special_z_moves_by_type.computeIfPresent(move.type, (type, zmove) -> {
                    zmoves_map.put(move.name, zmove.name);
                    return zmove;
                });
                break;
            }
        });
    }

    /**
     * Returns a Z move based on the current selected attack of the user, if no
     * zmove available, or not able to use one (ie no crystal held), or the mob
     * is on cooldown for zmoves, then this will return null.
     *
     * @param user
     * @param index - move index to check
     * @return
     */
    public static String getZMove(final IPokemob user, final String base_move)
    {
        if (base_move == null) return null;
        final MoveEntry move = MovesUtils.getMove(base_move);
        if (move == null) return null;
        final ZPower checker = CapabilityZMove.get(user.getEntity());
        if (!checker.canZMove(user, base_move)) return null;

        final String name = user.getPokedexEntry().getTrimmedName();

        // Check if a valid signature Z-move is available.
        if (GZMoveManager.z_sig_moves_map.containsKey(name))
        {
            final List<String> moves = GZMoveManager.z_sig_moves_map.get(name);
            for (final String zmove : moves) if (checker.canZMove(user, zmove)) return zmove;
        }
        // Otherwise just pick the one from the map.
        return GZMoveManager.zmoves_map.get(base_move);
    }

    /**
     * Returns a Z move based on the current selected attack of the user, if no
     * zmove available, or not able to use one (ie no crystal held), or the mob
     * is on cooldown for zmoves, then this will return null.
     *
     * @param user
     * @param gigant
     * @param index  - move index to check
     * @return
     */
    public static String getGMove(final IPokemob user, final String base_move, boolean gigant)
    {
        if (base_move == null) return null;
        if (!DynamaxHelper.isDynamax(user)) return null;
        final MoveEntry move = MovesUtils.getMove(base_move);
        if (move == null) return null;
        gigant = gigant && GZMoveManager.g_max_moves_map.containsKey(base_move);
        return gigant ? GZMoveManager.g_max_moves_map.get(base_move) : GZMoveManager.gmoves_map.get(base_move);
    }
}
