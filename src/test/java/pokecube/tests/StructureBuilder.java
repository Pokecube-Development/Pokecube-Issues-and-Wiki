package pokecube.tests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonObject;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.JigsawReplacementProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraftforge.items.IItemHandlerModifiable;
import pokecube.api.PokecubeAPI;
import thut.api.util.JsonUtil;
import thut.api.world.IWorldTickListener;
import thut.api.world.StructureTemplateTools;
import thut.api.world.WorldTickManager;

public class StructureBuilder implements IWorldTickListener
{
    boolean done = false;
    int layer = Integer.MIN_VALUE;
    IItemHandlerModifiable itemSource = null;
    BlockPos origin;
    Direction orientation;
    Tag key_info = null;
    Map<Integer, List<StructureBlockInfo>> toPlace = new HashMap<>();
    StructurePlaceSettings settings;

    public StructureBuilder(BlockPos origin, Direction orientation, IItemHandlerModifiable itemSource)
    {
        this.origin = origin;
        this.orientation = orientation;
        this.itemSource = itemSource;
    }

    private void checkBlueprint(ServerLevel level)
    {
        ItemStack key = this.itemSource.getStackInSlot(0);
        ResourceLocation toMake = null;
        int dy = 0;
        check:
        if (key.is(Items.WRITTEN_BOOK) || key.is(Items.WRITABLE_BOOK))
        {
            var tag = key.getOrCreateTag().get("pages");
            if (key_info != null && tag.toString().equals(key_info.toString())) break check;
            key_info = tag;
            toPlace.clear();
            if (tag instanceof ListTag list && !list.isEmpty() && list.get(0) instanceof StringTag entry)
            {

                try
                {
                    String string = entry.getAsString();
                    if (!string.startsWith("{")) string = "{\"text\":\"" + string + "\"}";
                    var parsed = JsonUtil.gson.fromJson(string, JsonObject.class);
                    String[] lines = parsed.get("text").getAsString().strip().split("\n");
                    if (!lines[0].equals("build:")) break check;
                    if (lines.length >= 2) toMake = new ResourceLocation(lines[1]);
                    if (lines.length >= 3) dy = Integer.parseInt(lines[2]);
                }
                catch (Exception e)
                {
                    PokecubeAPI.LOGGER.error(e);
                }
            }

            if (toMake == null)
            {
                PokecubeAPI.LOGGER.error("No ResourceLocation!");
                return;
            }

            var opt = level.getStructureManager().get(toMake);

            if (!opt.isPresent())
            {
                PokecubeAPI.LOGGER.error("No Template for {}!", toMake);
                return;
            }

            if (opt.isPresent())
            {
                var template = opt.get();
                var size = template.getSize();

                settings = new StructurePlaceSettings();
                Rotation rotation = Rotation.NONE;
                int dz = 0;
                int dx = 0;
                switch (orientation)
                {
                case NORTH:
                    dz = size.getZ();
                    rotation = Rotation.CLOCKWISE_180;
                    break;
                case EAST:
                    rotation = Rotation.COUNTERCLOCKWISE_90;
                    dx = -size.getX();
                    break;
                case SOUTH:
                    dz = -size.getZ();
                    break;
                case WEST:
                    dx = size.getX();
                    rotation = Rotation.CLOCKWISE_90;
                    break;
                default:
                    break;
                }

                BlockPos shift = new BlockPos(dx - size.getX() / 2, 0, dz);

                BlockPos location = origin.above(dy).offset(shift);
                settings.setRotation(rotation);
                settings.setRandom(level.getRandom());
                settings.setRotationPivot(new BlockPos(size.getX() / 2, 0, 0));
                settings.setIgnoreEntities(true);
                settings.addProcessor(JigsawReplacementProcessor.INSTANCE);
                settings.addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);

                List<StructureBlockInfo> list = settings.getRandomPalette(template.palettes, location).blocks();
                List<StructureBlockInfo> infos = StructureTemplate.processBlockInfos(level, location, BlockPos.ZERO,
                        settings, list, template);

                for (var info : infos)
                {
                    if (info.state != null)
                    {
                        Integer y = info.pos.getY();
                        toPlace.compute(y, (i, l) -> {
                            List<StructureBlockInfo> atY = l;
                            if (atY == null)
                            {
                                atY = new ArrayList<>();
                            }
                            atY.add(info);
                            return atY;
                        });
                    }
                }
            }
        }
        // If key removed, clear as well
        else toPlace.clear();
    }

    @Override
    public void onTickEnd(ServerLevel level)
    {
        checkBlueprint(level);
        if (toPlace.isEmpty()) return;

        List<Integer> ys = new ArrayList<>(toPlace.keySet());
        ys.sort(null);

        for (Integer y : ys)
        {
            List<StructureBlockInfo> infos = toPlace.get(y);
            List<ItemStack> stacks = StructureTemplateTools.getNeededMaterials(level, infos);
            List<BlockPos> remove = StructureTemplateTools.getNeedsRemoval(level, settings, infos);

            System.out.println(stacks.size() + " " + remove.size() + " " + infos.size() + " " + y);

            if (stacks.isEmpty())
            {
                toPlace.remove(y);
                continue;
            }
            var info = infos.get(0);
            if (remove.contains(info.pos))
            {
                BlockState block = level.getBlockState(info.pos);
                boolean broke = level.destroyBlock(info.pos, true);
                System.out.println("Broke? " + block + " " + broke + " " + level.getBlockState(info.pos));
                return;
            }
            infos.remove(0);
            BlockState placeState = info.state.mirror(settings.getMirror()).rotate(level, info.pos,
                    settings.getRotation());
            level.setBlockAndUpdate(info.pos, placeState);

            if (infos.isEmpty()) toPlace.remove(y);
            return;
        }
        // If we finished, remove.
        WorldTickManager.removeWorldData(level.dimension(), this);
        PokecubeAPI.LOGGER.info("Finished structure!");
    }
}
