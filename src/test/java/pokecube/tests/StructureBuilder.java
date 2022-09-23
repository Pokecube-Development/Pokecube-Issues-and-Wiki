package pokecube.tests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.WritableBookItem;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.JigsawReplacementProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraftforge.items.IItemHandlerModifiable;
import pokecube.api.PokecubeAPI;
import pokecube.world.gen.structures.processors.MarkerToAirProcessor;
import thut.api.util.JsonUtil;
import thut.api.world.IWorldTickListener;
import thut.api.world.StructureTemplateTools;
import thut.api.world.WorldTickManager;
import thut.lib.ItemStackTools;
import thut.lib.TComponent;

public class StructureBuilder implements IWorldTickListener
{
    private static enum CanPlace
    {
        YES, NO, NEED_ITEM;
    }

    boolean done = false;
    int layer = Integer.MIN_VALUE;
    IItemHandlerModifiable itemSource = null;
    BlockPos origin;
    Direction orientation;
    Tag key_info = null;
    Map<Integer, List<StructureBlockInfo>> removeOrder = new HashMap<>();
    List<StructureBlockInfo> placeOrder = new ArrayList<>();
    Map<BlockPos, ItemStack> neededItems = Maps.newHashMap();
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
            placeOrder.clear();
            removeOrder.clear();
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
                settings.addProcessor(MarkerToAirProcessor.PROCESSOR);
                settings.addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);

                List<StructureBlockInfo> list = settings.getRandomPalette(template.palettes, location).blocks();
                List<StructureBlockInfo> infos = StructureTemplate.processBlockInfos(level, location, BlockPos.ZERO,
                        settings, list, template);

                placeOrder.addAll(infos);
                neededItems.clear();
                neededItems = StructureTemplateTools.getNeededMaterials(level, infos);
                if (neededItems.isEmpty())
                {
                    this.done = true;
                    WorldTickManager.removeWorldData(level.dimension(), this);
                    PokecubeAPI.LOGGER.info("Already Complete Structure!");
                    return;
                }
                ItemStack book = itemSource.getStackInSlot(1);
                if (book.getItem() instanceof WritableBookItem || book.getItem() instanceof WrittenBookItem)
                {
                    CompoundTag nbt = book.getOrCreateTag();
                    ListTag pages = null;
                    if (!nbt.contains("pages") || !(nbt.get("pages") instanceof ListTag t2))
                        nbt.put("pages", pages = new ListTag());
                    else pages = t2;

                    pages.clear();

                    int n = 0;
                    // The set is to ensure each stack is only added once!
                    List<ItemStack> stacks = Lists.newArrayList(Sets.newHashSet(neededItems.values()));
                    stacks.sort((a, b) -> b.getCount() - a.getCount());
                    MutableComponent msg = TComponent.literal("Needed Items:");

                    for (var s : stacks)
                    {
                        if (n++ > 8)
                        {
                            pages.add(StringTag.valueOf(Component.Serializer.toJson(msg)));
                            msg = TComponent.literal("Needed Items:");
                            n = 0;
                        }
                        MutableComponent name = (MutableComponent) s.getDisplayName();
                        name.setStyle(name.getStyle().withColor(0));
                        msg = msg.append(TComponent.literal("\n")).append(TComponent.literal(s.getCount() + "x"))
                                .append(name);
                    }
                    if (!msg.getString().isBlank()) pages.add(StringTag.valueOf(Component.Serializer.toJson(msg)));
                    book = new ItemStack(Items.WRITTEN_BOOK);
                    nbt.putString("title", "BoM");
                    nbt.putString("author", "BoM Generator");
                    book.setTag(nbt);
                    itemSource.setStackInSlot(1, book);
                }

                for (var info : infos)
                {
                    if (info.state != null)
                    {
                        Integer y = info.pos.getY();
                        removeOrder.compute(y, (i, l) -> {
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
        else
        {
            placeOrder.clear();
            removeOrder.clear();
        }
    }

    public boolean tryClear(List<Integer> ys, ServerLevel level)
    {
        for (int i = ys.size() - 1; i >= 0; i--)
        {
            int y = ys.get(i);
            List<StructureBlockInfo> infos = removeOrder.get(y);
            List<BlockPos> remove = StructureTemplateTools.getNeedsRemoval(level, settings, infos);
            if (remove.isEmpty()) continue;
            BlockPos pos = remove.get(0);
            BlockState state = level.getBlockState(pos);
            final List<ItemStack> list = Block.getDrops(state, level, pos, level.getBlockEntity(pos));
            list.removeIf(stack -> ItemStackTools.addItemStackToInventory(stack, itemSource, 1));
            list.forEach(c -> {
                int x = pos.getX();
                int z = pos.getZ();
                ItemEntity item = new ItemEntity(level, x + 0.5, y + 0.5, z + 0.5, c);
                level.addFreshEntity(item);
            });
            level.destroyBlock(pos, false);
            return false;
        }
        return true;
    }

    public CanPlace canPlace(StructureBlockInfo info)
    {
        if (info.state == null || info.state.isAir()) return CanPlace.YES;
        ItemStack stack = StructureTemplateTools.getForInfo(info);
        if (!stack.isEmpty())
        {
            ItemStack needed = neededItems.get(info.pos);
            for (int i = 1; i < itemSource.getSlots(); i++)
            {
                ItemStack inSlot = itemSource.getStackInSlot(i);
                if (ItemStack.isSame(stack, inSlot))
                {
                    inSlot.shrink(1);
                    if (needed != null) needed.shrink(1);
                    return CanPlace.YES;
                }
            }
            if (needed == null)
            {
                return CanPlace.NO;
            }
            else
            {
                ItemStackTools.addItemStackToInventory(needed.copy(), itemSource, 1);
            }
        }
        return stack.isEmpty() ? CanPlace.NO : CanPlace.NEED_ITEM;
    }

    public boolean tryPlace(List<Integer> ys, ServerLevel level)
    {
        for (var info : placeOrder)
        {
            CanPlace place = canPlace(info);
            if (place == CanPlace.NO)
            {
                placeOrder.remove(info);
                return false;
            }
            else if (place == CanPlace.NEED_ITEM)
            {
                return false;
            }
            BlockState placeState = info.state.mirror(settings.getMirror()).rotate(level, info.pos,
                    settings.getRotation());
            BlockState old = level.getBlockState(info.pos);
            boolean same = old.isAir() & placeState.isAir();
            if (same) continue;

            placeOrder.remove(info);
            StructureTemplateTools.getPlacer(placeState).placeBlock(placeState, info.pos, level);
            return false;
        }
        return true;
    }

    @Override
    public void onTickEnd(ServerLevel level)
    {
        checkBlueprint(level);
        if (removeOrder.isEmpty()) return;

        List<Integer> ys = new ArrayList<>(removeOrder.keySet());
        ys.sort(null);

        // Check if we need to remove invalid blocks, do that first.
        if (!tryClear(ys, level)) return;
        // Then check if we can place blocks.
        if (!tryPlace(ys, level)) return;

        // If we finished, remove.
        WorldTickManager.removeWorldData(level.dimension(), this);
        PokecubeAPI.LOGGER.info("Finished structure!");
    }
}
