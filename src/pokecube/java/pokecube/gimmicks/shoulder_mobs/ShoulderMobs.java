package pokecube.gimmicks.shoulder_mobs;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.AIRoutine;
import pokecube.api.entity.pokemob.ai.LogicStates;
import pokecube.api.events.pokemobs.InitAIEvent;
import pokecube.api.items.PokecubeContents;
import pokecube.core.PokecubeCore;
import pokecube.core.handlers.PokecubePlayerDataHandler;
import pokecube.core.network.packets.PacketDataSync;
import thut.core.common.ThutCore;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = PokecubeCore.MODID)
public class ShoulderMobs
{
    public static final String ON_SHOULDER = "pokecube:on_shoulder";
    public static final String ON_SHOULDER_TIMER = "pokecube:on_shoulder_timer";

    public static final AIRoutine SHOULDER = new AIRoutine("SHOULDER", true, p -> p.getPokedexEntry().canSitShoulder);

    @SubscribeEvent
    public static void init(FMLLoadCompleteEvent event)
    {
        PokecubeAPI.POKEMOB_BUS.addListener(ShoulderMobs::addAI);
        ThutCore.FORGE_BUS.addListener(ShoulderMobs::onMobJoinWorld);
        ThutCore.FORGE_BUS.addListener(ShoulderMobs::preTick);
        ThutCore.FORGE_BUS.addListener(ShoulderMobs::onPokeStick);

        PokecubeContents.TAGSTOREMOVE.add(ShoulderMobs.ON_SHOULDER);
        PokecubeContents.TAGSTOREMOVE.add(ShoulderMobs.ON_SHOULDER_TIMER);
    }

    private static void addAI(InitAIEvent.Init event)
    {
        if (event.type == InitAIEvent.Init.Type.IDLE)
        {
            var pokemob = event.getPokemob();
            // Jump on shoulder if able to
            if (pokemob.getPokedexEntry().canSitShoulder)
                event.add(new IdleJumpOnShoulderTask(pokemob).setPriority(15));
        }
    }

    private static void onMobJoinWorld(final EntityJoinLevelEvent evt)
    {
        var entity = evt.getEntity();
        IPokemob pokemob = PokemobCaps.getPokemobFor(entity);
        if (pokemob != null && entity.getPersistentData().getBoolean(ShoulderMobs.ON_SHOULDER))
        {
            pokemob.setLogicState(LogicStates.SITTING, false);
            entity.getPersistentData().remove(ShoulderMobs.ON_SHOULDER);
        }
    }

    private static void onPokeStick(PlayerInteractEvent.EntityInteractSpecific event)
    {
        var player = event.getEntity();
        var pokemob = PokemobCaps.getPokemobFor(event.getTarget());
        if (pokemob == null) return;
        if (player != pokemob.getOwner()) return;
        var held = event.getItemStack();
        if (held.getItem() == Items.STICK || held.getItem() == Blocks.TORCH.asItem())
        {
            if (player.isShiftKeyDown())
            {
                if (held.getDisplayName().getString().contains("shoulder"))
                    IdleJumpOnShoulderTask.moveToShoulder(player, pokemob);
            }
            else if (pokemob.getEntity().isPassenger())
            {
                pokemob.getEntity().stopRiding();
            }
        }
    }

    private static void preTick(EntityTickEvent.Pre event)
    {
        var pokemob = PokemobCaps.getPokemobFor(event.getEntity());
        if (pokemob != null)
        {
            var living = pokemob.getEntity();
            if (living.getVehicle() instanceof Player)
            {
                if (!pokemob.getLogicState(LogicStates.SITTING))
                {
                    living.getPersistentData().remove(ShoulderMobs.ON_SHOULDER);
                    living.getPersistentData().remove(ShoulderMobs.ON_SHOULDER_TIMER);
                    living.stopRiding();
                }
            }
            if (living.getVehicle() instanceof ServerPlayer player)
            {
                CompoundTag tag = PokecubePlayerDataHandler.getCustomDataTag(player);
                int[] rid = tag.getIntArray("rider");
                boolean alreadyKnown = false;
                for (int i : rid) alreadyKnown |= i == living.getId();
                if (!alreadyKnown)
                {
                    int[] rid2 = new int[rid.length + 1];
                    System.arraycopy(rid, 0, rid2, 0, rid.length);
                    rid2[rid.length] = living.getId();
                    tag.putIntArray("rider", rid2);
                    PacketDataSync.syncData(player, "pokecube-custom");
                }
            }
            else if (living.level() instanceof ServerLevel && living.getPersistentData()
                    .getBoolean(ShoulderMobs.ON_SHOULDER) && pokemob.getLogicState(LogicStates.SITTING))
            {
                int remountTimer = living.getPersistentData().getInt(ShoulderMobs.ON_SHOULDER_TIMER);
                if (pokemob.getOwner() instanceof Player player)
                {
                    IdleJumpOnShoulderTask.moveToShoulder(player, pokemob);
                    living.getPersistentData().remove(ShoulderMobs.ON_SHOULDER_TIMER);
                }
                else if (remountTimer > 10)
                {
                    pokemob.setLogicState(LogicStates.SITTING, false);
                    living.getPersistentData().remove(ShoulderMobs.ON_SHOULDER);
                    living.getPersistentData().remove(ShoulderMobs.ON_SHOULDER_TIMER);
                }
                else
                {
                    living.getPersistentData().putInt(ShoulderMobs.ON_SHOULDER_TIMER, remountTimer + 1);
                }
            }
        }
    }
}
