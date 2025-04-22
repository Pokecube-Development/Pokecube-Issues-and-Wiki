package pokecube.gimmicks.shoulder_mobs;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.EntityAttachments;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
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
import thut.api.maths.vecmath.Mat3f;
import thut.api.maths.vecmath.Vec3f;
import thut.core.common.ThutCore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        if (event.getEntity() instanceof LivingEntity living)
        {
            // Server side check if still have a rider, sync that.
            if (living instanceof ServerPlayer player)
            {
                CompoundTag tag = PokecubePlayerDataHandler.getCustomDataTag(player);
                int[] rid = tag.getIntArray("rider");
                for (int i : rid)
                {
                    Entity e = player.level.getEntity(i);
                    if (e == null || e.getVehicle() != player)
                    {
                        tag.remove("rider");
                        PacketDataSync.syncData(player, "pokecube-custom");
                    }
                }
            }
            else if (living instanceof Player player)
            {
                CompoundTag tag = PokecubePlayerDataHandler.getCustomDataTag(player);
                int[] rid = tag.getIntArray("rider");
                for (int i : rid)
                {
                    Entity e = player.level.getEntity(i);
                    if (e != null && e.getVehicle() != player)
                    {
                        e.startRiding(player);
                    }
                }
            }
        }

        var pokemob = PokemobCaps.getPokemobFor(event.getEntity());
        if (pokemob != null)
        {
            var living = pokemob.getEntity();
            if (living.getVehicle() instanceof Player player)
            {
                if (!pokemob.getLogicState(LogicStates.SITTING))
                {
                    living.getPersistentData().remove(ShoulderMobs.ON_SHOULDER);
                    living.getPersistentData().remove(ShoulderMobs.ON_SHOULDER_TIMER);
                    living.stopRiding();
                }
                // Ensure attachments are correct
                {
                    Map<EntityAttachment, List<Vec3>> map = ObfuscationReflectionHelper.getPrivateValue(
                            EntityAttachments.class, player.getAttachments(), "attachments");
                    Vec3 seatL = new Vec3(+player.getBbWidth(), player.getBbHeight() * 0.75, 0);
                    Vec3 seatR = new Vec3(-player.getBbWidth(), player.getBbHeight() * 0.75, 0);

                    final float yaw = -(player.yBodyRot - player.getYRot()) * 0.017453292F;
                    final float pitch = 0;
                    final float sinYaw = Mth.sin(yaw);
                    final float cosYaw = Mth.cos(yaw);
                    final float sinPitch = Mth.sin(pitch);
                    final float cosPitch = Mth.cos(pitch);
                    final Mat3f matrixYaw = new Mat3f(cosYaw, 0, sinYaw, 0, 1, 0, -sinYaw, 0, cosYaw);
                    final Mat3f matrixPitch = new Mat3f(cosPitch, -sinPitch, 0, sinPitch, cosPitch, 0, 0, 0, 1);
                    final Mat3f transform = new Mat3f();
                    transform.mul(matrixYaw, matrixPitch);

                    boolean left = living == player.getPassengers().getFirst();
                    float dx = left ? 0.2f + living.getBbWidth() / 2 : -(0.4f + living.getBbWidth() / 2);

                    Vec3f v = new Vec3f(dx, player.getBbHeight() * 0.75f, 0);
                    transform.transform(v);
                    if (left) seatL = new Vec3(v.toMC());
                    else seatR = new Vec3(v.toMC());

                    living.yBodyRot = player.yBodyRot;
                    living.yBodyRotO = player.yBodyRotO;

                    List<Vec3> seats = map.get(EntityAttachment.PASSENGER);
                    if (!(seats instanceof ArrayList<Vec3>))
                    {
                        seats = new ArrayList<>();
                        seats.add(seatL);
                        seats.add(seatR);
                        map.put(EntityAttachment.PASSENGER, seats);
                    }
                    else
                    {
                        if (left) seats.set(0, new Vec3(v.toMC()));
                        else seats.set(1, new Vec3(v.toMC()));
                    }
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
