package pokecube.core.ai.tasks.utility.builders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;

import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.block.Mirror;
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

    public static record BoMRecord(Supplier<ItemStack> BoMProvider, Consumer<ItemStack> BoMConsumer)
    {
    }

    boolean done = false;
    public boolean creative = false;
    int layer = Integer.MIN_VALUE;
    IItemHandlerModifiable itemSource = null;
    Supplier<StructureTemplate> keyProvider;
    StructureTemplate _template;
    int dy = 0;
    BlockPos origin;
    ServerLevel level;
    Rotation rotation;
    Mirror mirror;
    Tag key_info = null;
    Map<Integer, List<StructureBlockInfo>> removeOrder = new HashMap<>();
    List<StructureBlockInfo> placeOrder = new ArrayList<>();
    Map<BlockPos, ItemStack> neededItems = Maps.newHashMap();
    public StructurePlaceSettings settings;
    public BoMRecord BoM = new BoMRecord(() -> itemSource.getStackInSlot(1),
            _book -> itemSource.setStackInSlot(1, _book));

    public StructureBuilder(BlockPos origin, Rotation rotation, Mirror mirror, IItemHandlerModifiable itemSource,
            Supplier<StructureTemplate> keyProvider)
    {
        this.origin = origin;

        if (mirror == null) mirror = Mirror.NONE;
        if (rotation == null) rotation = Rotation.NONE;

        this.rotation = rotation;
        this.mirror = mirror;

        this.itemSource = itemSource;
        if (keyProvider == null) keyProvider = this::fromSource;
        this.keyProvider = keyProvider;
    }

    public StructureBuilder(BlockPos origin, Rotation rotation, Mirror mirror, IItemHandlerModifiable itemSource)
    {
        this(origin, rotation, mirror, itemSource, null);
    }

    private StructureTemplate fromSource()
    {
        ItemStack key = this.itemSource.getStackInSlot(0);
        if (!key.hasTag()) return null;
        ResourceLocation toMake = null;
        dy = 0;
        var tag = key.getTag().get("pages");
        if (key_info != null && tag.toString().equals(key_info.toString())) return _template;
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
                if (!lines[0].equals("build:")) return null;
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
            return null;
        }

        var opt = level.getStructureManager().get(toMake);

        if (!opt.isPresent())
        {
            PokecubeAPI.LOGGER.error("No Template for {}!", toMake);
            return null;
        }

        return opt.get();
    }

    public void provideBoM()
    {
        ItemStack book = BoM.BoMProvider().get();
        boolean compiled = false;
        if (book.getItem() instanceof WritableBookItem || (compiled = (book.getItem() instanceof WrittenBookItem)))
        {
            CompoundTag nbt = book.getOrCreateTag();
            ListTag pages = null;
            if (!nbt.contains("pages") || !(nbt.get("pages") instanceof ListTag t2))
                nbt.put("pages", pages = new ListTag());
            else pages = t2;
            List<StringTag> BoMHeader = new ArrayList<>();

            for (int i = 0; i < pages.size(); i++)
            {
                var entry = pages.get(i);
                String string = entry.getAsString();
                if (!string.startsWith("{")) string = "{\"text\":\"" + string + "\"}";
                var parsed = JsonUtil.gson.fromJson(string, JsonObject.class);
                if (parsed.get("text").getAsString().strip().startsWith("Next Items:")) break;
                if (entry instanceof StringTag tag) BoMHeader.add(tag);
            }

            pages.clear();

            for (var tag : BoMHeader)
            {
                pages.add(tag);
            }

            MutableComponent msg = TComponent.literal("Next Items:");
            int n = 0;
            for (int i = 0, max = placeOrder.size(); i < max; i++)
            {
                ItemStack needed = neededItems.get(placeOrder.get(i).pos);
                if (needed == null || needed.isEmpty()) continue;
                if (n++ > 3) break;
                MutableComponent name = (MutableComponent) needed.getDisplayName();
                name.setStyle(name.getStyle().withColor(0));
                msg = msg.append(TComponent.literal("\n")).append(TComponent.literal(needed.getCount() + "x"))
                        .append(name);
            }

            if (compiled) pages.add(StringTag.valueOf(Component.Serializer.toJson(msg)));
            else pages.add(StringTag.valueOf(msg.getString()));

            n = 0;
            // The set is to ensure each stack is only added once!
            List<ItemStack> stacks = Lists.newArrayList(Sets.newHashSet(neededItems.values()));
            stacks.sort((a, b) -> b.getCount() - a.getCount());
            msg = TComponent.literal("Needed Items:");

            for (var s : stacks)
            {
                if (n++ > 8)
                {
                    if (compiled) pages.add(StringTag.valueOf(Component.Serializer.toJson(msg)));
                    else pages.add(StringTag.valueOf(msg.getString()));
                    msg = TComponent.literal("Needed Items:");
                    n = 0;
                }
                MutableComponent name = (MutableComponent) s.getDisplayName();
                name.setStyle(name.getStyle().withColor(0));
                msg = msg.append(TComponent.literal("\n")).append(TComponent.literal(s.getCount() + "x")).append(name);
            }
            if (!msg.getString().isBlank())
            {
                if (compiled) pages.add(StringTag.valueOf(Component.Serializer.toJson(msg)));
                else pages.add(StringTag.valueOf(msg.getString()));
            }
            book = compiled ? new ItemStack(Items.WRITTEN_BOOK) : new ItemStack(Items.WRITABLE_BOOK);
            nbt.putString("title", "BoM");
            nbt.putString("author", "BoM Generator");
            book.setTag(nbt);
            BoM.BoMConsumer().accept(book);
        }
    }

    public void checkBlueprint(ServerLevel level)
    {
        this.level = level;
        var old = _template;
        var template = keyProvider.get();
        if (template == old) return;
        this._template = template;
        if (template != null)
        {
            BlockPos location = origin.above(dy);
            if (settings == null)
            {
                settings = new StructurePlaceSettings();
                Rotation rotation = this.rotation;
                Mirror mirror = this.mirror;

                settings.setRotation(rotation);
                settings.setMirror(mirror);
                settings.setRandom(level.getRandom());
                settings.setIgnoreEntities(true);
                settings.addProcessor(JigsawReplacementProcessor.INSTANCE);
                settings.addProcessor(MarkerToAirProcessor.PROCESSOR);
                settings.addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
            }
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
            provideBoM();

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
            if (level.getMinBuildHeight() >= pos.getY()) continue;
            if (level.getMaxBuildHeight() <= pos.getY()) continue;
            if (pos.equals(origin)) continue;
            BlockState state = level.getBlockState(pos);
            final List<ItemStack> list = Block.getDrops(state, level, pos, level.getBlockEntity(pos));
            if (!creative)
            {
                list.removeIf(stack -> ItemStackTools.addItemStackToInventory(stack, itemSource, 1));
                list.forEach(c -> {
                    int x = pos.getX();
                    int z = pos.getZ();
                    ItemEntity item = new ItemEntity(level, x + 0.5, y + 0.5, z + 0.5, c);
                    level.addFreshEntity(item);
                });
            }
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
            else if (creative)
            {
                return CanPlace.YES;
            }
        }
        return stack.isEmpty() ? CanPlace.NO : CanPlace.NEED_ITEM;
    }

    public boolean tryPlace(List<Integer> ys, ServerLevel level)
    {
        for (var info : placeOrder)
        {
            if (level.getMinBuildHeight() >= info.pos.getY())
            {
                placeOrder.remove(info);
                return false;
            }
            if (level.getMaxBuildHeight() <= info.pos.getY())
            {
                placeOrder.remove(info);
                return false;
            }
            if (info.pos.equals(origin))
            {
                placeOrder.remove(info);
                return false;
            }
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
            // We do not use the "recommended" rotate function, as that is for
            // blocks already in world. Using it prevents pistons from rotating
            // properly!
            @SuppressWarnings("deprecation")
            BlockState placeState = info.state.rotate(settings.getRotation());// .mirror(settings.getMirror())
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
        if (!creative || removeOrder.isEmpty())
        {
            WorldTickManager.removeWorldData(level.dimension(), this);
            PokecubeAPI.LOGGER.info("terminated structure!");
            return;
        }

        List<Integer> ys = new ArrayList<>(removeOrder.keySet());
        ys.sort(null);

        long end = System.currentTimeMillis() + 100;
        boolean ended = false;
        while (System.currentTimeMillis() < end)
        {
            // Check if we need to remove invalid blocks, do that first.
            if (!tryClear(ys, level)) continue;
            // Then check if we can place blocks.
            if (!(ended = tryPlace(ys, level))) continue;
            if (ended) break;
        }
        if (!ended) return;
        // If we finished, remove.
        WorldTickManager.removeWorldData(level.dimension(), this);
        PokecubeAPI.LOGGER.info("Finished structure!");
    }
}
