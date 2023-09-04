package pokecube.tests;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.IItemHandlerModifiable;
import pokecube.api.PokecubeAPI;
import thut.api.ThutCaps;
import thut.api.Tracker;
import thut.api.maths.Vector3;
import thut.api.world.WorldTickManager;
import thut.lib.TComponent;

@Mod.EventBusSubscriber
public class DebugInteractions
{

//    @SubscribeEvent
//    public static void onKeyInput(final Key evt)
//    {
//        if (Minecraft.getInstance().screen != null) return;
//        final Player player = Minecraft.getInstance().player;
//        if (evt.getKey() == GLFW.GLFW_KEY_UP)
//        {
//            player.xRot -= 1;
//            player.xRotO = player.xRot;
//        }
//        if (evt.getKey() == GLFW.GLFW_KEY_DOWN)
//        {
//            player.xRot += 1;
//            player.xRotO = player.xRot;
//        }
//        if (evt.getKey() == GLFW.GLFW_KEY_LEFT)
//        {
//            player.yRot -= 1;
//            player.yRotO = player.xRot;
//        }
//        if (evt.getKey() == GLFW.GLFW_KEY_RIGHT)
//        {
//            player.yRot += 1;
//            player.yRotO = player.xRot;
//        }
//    }

    @SubscribeEvent
    public static void onBlockRightClick(final PlayerInteractEvent.RightClickBlock evt)
    {
        if (!(evt.getEntity() instanceof ServerPlayer player)
                || !(evt.getEntity().level() instanceof ServerLevel level))
            return;
        boolean isStructureMaker = evt.getItemStack().getDisplayName().getString().contains("structure_maker");

        var te = level.getBlockEntity(evt.getPos());

        long tick = Tracker.instance().getTick();
        if (player.getPersistentData().getLong("__debug_interaction__") == tick) return;
        player.getPersistentData().putLong("__debug_interaction__", tick);

        if (te instanceof ChestBlockEntity chest && isStructureMaker)
        {
            IItemHandlerModifiable itemSource = (IItemHandlerModifiable) chest.getCapability(ThutCaps.ITEM_HANDLER)
                    .orElse(null);
            BlockPos origin = evt.getPos();
            Direction orientation = level.getBlockState(origin).getValue(ChestBlock.FACING);

            StructureBuilder builder = new StructureBuilder(origin, orientation, itemSource);
            WorldTickManager.addWorldData(level.dimension(), builder);
        }
    }

    @SubscribeEvent
    public static void onItemRightClick(final PlayerInteractEvent.RightClickItem evt)
    {
        if (!(evt.getEntity() instanceof ServerPlayer player)
                || !(evt.getEntity().level() instanceof ServerLevel level))
            return;
        boolean isStructureDebug = evt.getItemStack().getDisplayName().getString().contains("structure_debug");
        Vector3 v = new Vector3().set(player);
        if (isStructureDebug)
        {
            var registry = level.registryAccess().registryOrThrow(Registry.STRUCTURE_REGISTRY);
            var list = registry.stream().toList();
            List<ResourceLocation> found = Lists.newArrayList();
            List<ResourceLocation> not_found = Lists.newArrayList();
            Map<ResourceLocation, Pair<Integer, BlockPos>> found_map = Maps.newHashMap();
            thut.lib.ChatHelper.sendSystemMessage(player, TComponent.literal("Searching for Structures!"));
            for (var feature : list)
            {
                var name = registry.getKey(feature);
                if (name.toString().startsWith("pokecube"))
                {
                    thut.lib.ChatHelper.sendSystemMessage(player, TComponent.literal("Checking " + name));
                    final ResourceKey<Structure> structure = ResourceKey.create(Registry.STRUCTURE_REGISTRY, name);
                    var holder = registry.getHolderOrThrow(structure);
                    HolderSet<Structure> holderset = HolderSet.direct(holder);
                    Pair<BlockPos, Holder<Structure>> thing = level.getChunkSource().getGenerator()
                            .findNearestMapStructure(level, holderset, v.getPos(), 100, false);
                    if (thing != null)
                    {
                        found.add(name);
                        found_map.put(name,
                                Pair.of((int) Math.sqrt(thing.getFirst().distSqr(v.getPos())), thing.getFirst()));
                    }
                    else
                    {
                        not_found.add(name);
                    }
                }
            }
            thut.lib.ChatHelper.sendSystemMessage(player, TComponent.literal("Search Complete"));
            found.sort(null);
            not_found.sort(null);
            PokecubeAPI.LOGGER.info("Structures Found:");
            for (var name : found)
            {
                PokecubeAPI.LOGGER.info("{}\t{}\t{}", found_map.get(name).getFirst(), found_map.get(name).getSecond(),
                        name);
            }
            PokecubeAPI.LOGGER.info("Structures Missing:");
            for (var name : not_found)
            {
                PokecubeAPI.LOGGER.info(name);
            }
        }
    }
}
