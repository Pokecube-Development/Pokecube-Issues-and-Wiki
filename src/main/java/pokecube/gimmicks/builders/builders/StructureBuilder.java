package pokecube.gimmicks.builders.builders;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
import net.minecraft.world.entity.LivingEntity;
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

    public static Supplier<StructureTemplate> makeForSource(StructureBuilder structureBuilder,
            IItemHandlerModifiable itemSource, int slot)
    {
        return () -> {
            ItemStack key = itemSource.getStackInSlot(slot);
            if (!key.hasTag()) return null;
            ResourceLocation toMake = structureBuilder.toMake;
            structureBuilder.dy = 0;
            var tag = key.getTag().get("pages");
            if (structureBuilder.key_info != null && tag.toString().equals(structureBuilder.key_info.toString()))
                return structureBuilder._template;
            structureBuilder.key_info = tag;
            if (toMake == null)
            {
                PokecubeAPI.LOGGER.error("No ResourceLocation!");
                return null;
            }
            var opt = structureBuilder.level.getStructureManager().get(toMake);
            if (!opt.isPresent())
            {
                PokecubeAPI.LOGGER.error("No Template for {}!", toMake);
                return null;
            }
            return opt.get();
        };
    }

    public boolean done = false;
    public boolean creative = false;
    public int layer = Integer.MIN_VALUE;
    public IItemHandlerModifiable itemSource = null;
    public Supplier<StructureTemplate> keyProvider;
    public StructureTemplate _template;
    public int dy = 0;
    public BlockPos origin;
    public ServerLevel level;
    public Rotation rotation;
    public Mirror mirror;
    public Tag key_info = null;
    public Map<Integer, List<StructureBlockInfo>> removeOrder = new HashMap<>();
    public List<StructureBlockInfo> placeOrder = new ArrayList<>();
    public Map<BlockPos, ItemStack> neededItems = Maps.newHashMap();
    public List<ItemStack> sortedNeededItems = new ArrayList<>();
    public ResourceLocation toMake;
    public BitSet missingItems = new BitSet();
    public BitSet ignoredItems = new BitSet();
    public StructurePlaceSettings settings;
    public List<BoMRecord> BoMs = new ArrayList<>();
    public List<LivingEntity> workers = new ArrayList<>();

    public Set<BlockPos> pendingBuild = new HashSet<>();
    public Set<BlockPos> pendingClear = new HashSet<>();

    public int passes = 0;

    public StructureBuilder(BlockPos origin, Rotation rotation, Mirror mirror, IItemHandlerModifiable itemSource,
            Supplier<StructureTemplate> keyProvider)
    {
        this.origin = origin;

        if (mirror == null) mirror = Mirror.NONE;
        if (rotation == null) rotation = Rotation.NONE;

        this.rotation = rotation;
        this.mirror = mirror;

        this.itemSource = itemSource;
        if (keyProvider == null) keyProvider = makeForSource(this, itemSource, 0);
        this.keyProvider = keyProvider;
    }

    public StructureBuilder(BlockPos origin, Rotation rotation, Mirror mirror, IItemHandlerModifiable itemSource)
    {
        this(origin, rotation, mirror, itemSource, null);
    }

    public void addBoMRecord(BoMRecord BoM)
    {
        BoMs.add(BoM);
    }

    /**
     * Checks if the bill of materials has marked the stacks as do not require.
     */
    public void checkBoM()
    {
        for (var BoM : BoMs)
        {
            ItemStack book = BoM.BoMProvider().get();
            if (book.getItem() instanceof WritableBookItem && book.hasTag())
            {
                CompoundTag nbt = book.getOrCreateTag();
                ListTag pages = null;
                if (!nbt.contains("pages") || !(nbt.get("pages") instanceof ListTag t2))
                    nbt.put("pages", pages = new ListTag());
                else pages = t2;
                Set<String> itemLists = new HashSet<>();
                for (int page = 0; page < pages.size(); page++)
                {
                    var entry = pages.get(page);
                    String string = entry.getAsString();
                    if (!string.startsWith("{")) string = "{\"text\":\"" + string + "\"}";
                    var parsed = JsonUtil.gson.fromJson(string, JsonObject.class);
                    String txt = parsed.get("text").getAsString().strip();
                    if (!txt.startsWith("Total Cost:")) continue;
                    var lines = txt.split("\n");
                    for (int i = 1; i < lines.length; i++)
                    {
                        var line = lines[i];
                        if (!line.isBlank() && line.startsWith("-x")) itemLists.add(line.replace("-x", ""));
                    }
                }

                ignoredItems.clear();
                if (itemLists.size() > 0) for (int i = 0; i < sortedNeededItems.size(); i++)
                {
                    if (itemLists.contains(sortedNeededItems.get(i).getDisplayName().getString())) ignoredItems.set(i);
                }
            }
        }
    }

    public void provideBoM()
    {
        for (var BoM : BoMs)
        {
            ItemStack book = BoM.BoMProvider().get();
            boolean compiled = false;
            if (book.getItem() instanceof WritableBookItem || (compiled = (book.getItem() instanceof WrittenBookItem)))
            {
                // Check first to see if maybe things were denied.
                checkBoM();

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
                List<ItemStack> requested = new ArrayList<>();

                needed_check:
                for (int i = 0, max = placeOrder.size(); i < max; i++)
                {
                    ItemStack needed = neededItems.get(placeOrder.get(i).pos());
                    if (needed == null || needed.isEmpty()) continue;
                    if (++n > 3) break;
                    for (var stack : requested) if (ItemStack.isSameItem(stack, needed)) continue needed_check;
                    requested.add(needed);
                    MutableComponent name = (MutableComponent) needed.getDisplayName();
                    name.setStyle(name.getStyle().withColor(0));
                    String count = needed.getCount() + "x";
                    int index = sortedNeededItems.indexOf(needed);
                    if (index >= 0 && this.ignoredItems.get(index)) count = "-x";
                    msg = msg.append(TComponent.literal("\n")).append(TComponent.literal(count)).append(name);
                }

                if (compiled) pages.add(StringTag.valueOf(Component.Serializer.toJson(msg)));
                else pages.add(StringTag.valueOf(msg.getString()));

                n = 0;
                msg = TComponent.literal("Total Cost:");

                for (int i = 0; i < sortedNeededItems.size(); i++)
                {
                    ItemStack s = sortedNeededItems.get(i);
                    if (s == null || s.isEmpty()) continue;
                    if (n++ > 8)
                    {
                        if (compiled) pages.add(StringTag.valueOf(Component.Serializer.toJson(msg)));
                        else pages.add(StringTag.valueOf(msg.getString()));
                        msg = TComponent.literal("Total Cost:");
                        n = 0;
                    }
                    MutableComponent name = (MutableComponent) s.getDisplayName();
                    name.setStyle(name.getStyle().withColor(0));
                    String count = s.getCount() + "x";
                    if (this.ignoredItems.get(i)) count = "-x";
                    msg = msg.append(TComponent.literal("\n")).append(TComponent.literal(count)).append(name);
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

                if (!compiled) book.setHoverName(TComponent.literal("BoM"));

                BoM.BoMConsumer().accept(book);
            }
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
            this.done = false;
            placeOrder.clear();
            removeOrder.clear();

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

            pendingBuild.clear();
            placeOrder.clear();
            placeOrder.addAll(infos);
            neededItems.clear();
            neededItems = StructureTemplateTools.getNeededMaterials(level, infos);
            if (neededItems.isEmpty())
            {
                this.done = true;
                WorldTickManager.removeWorldData(level.dimension(), this);
                PokecubeAPI.LOGGER.info("Already Complete Structure! " + this);
                return;
            }
            sortedNeededItems.clear();
            // The set is to ensure each stack is only added once!
            sortedNeededItems.addAll(Sets.newHashSet(neededItems.values()));
            sortedNeededItems.sort((a, b) -> b.getCount() - a.getCount());

            provideBoM();

            for (var info : infos)
            {
                if (info.state() != null)
                {
                    Integer y = info.pos().getY();
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
            pendingBuild.clear();
            placeOrder.clear();
            removeOrder.clear();
            missingItems.clear();
            ignoredItems.clear();
        }
    }

    public BlockPos nextRemoval(List<Integer> ys, ServerLevel level)
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
            if (pendingClear.contains(pos)) continue;
            return pos;
        }
        return null;
    }

    public boolean tryClear(List<Integer> ys, ServerLevel level)
    {
        BlockPos pos = nextRemoval(ys, level);
        if (pos == null) return true;
        BlockState state = level.getBlockState(pos);
        final List<ItemStack> list = Block.getDrops(state, level, pos, level.getBlockEntity(pos));
        if (!creative)
        {
            list.removeIf(stack -> ItemStackTools.addItemStackToInventory(stack, itemSource, 1));
            list.forEach(c -> {
                int x = pos.getX();
                int z = pos.getZ();
                ItemEntity item = new ItemEntity(level, x + 0.5, pos.getY() + 0.5, z + 0.5, c);
                level.addFreshEntity(item);
            });
        }
        level.destroyBlock(pos, false);
        return true;
    }

    public PlaceInfo canPlace(StructureBlockInfo info, int index)
    {
        if (info.state() == null || info.state().isAir()) return new PlaceInfo(CanPlace.YES, info, -1);

        // If we are marked as to ignore this point. return no directly, this
        // skips the placement.
        if (index >= 0 && ignoredItems.get(index)) return new PlaceInfo(CanPlace.NO, info, -1);

        ItemStack stack = StructureTemplateTools.getForInfo(info);
        if (!stack.isEmpty())
        {
            ItemStack needed = neededItems.get(info.pos());
            for (int i = 1; i < itemSource.getSlots(); i++)
            {
                ItemStack inSlot = itemSource.getStackInSlot(i);
                if (ItemStack.isSameItem(stack, inSlot))
                {
                    return new PlaceInfo(CanPlace.YES, info, i);
                }
            }
            if (needed == null)
            {
                return new PlaceInfo(CanPlace.NO, info, -1);
            }
            else if (creative)
            {
                return new PlaceInfo(CanPlace.YES, info, -1);
            }
            else
            {
                missingItems.set(index);
            }
        }
        return new PlaceInfo(stack.isEmpty() ? CanPlace.NO : CanPlace.NEED_ITEM, info, -1);
    }

    public static record PlaceInfo(CanPlace valid, StructureBlockInfo info, int itemSlot)
    {
    }

    public PlaceInfo getNextPlacement(ServerLevel level)
    {
        for (int i = 0; i < placeOrder.size(); i++)
        {
            var info = placeOrder.get(i);
            // Someone else is working on this.
            if (pendingBuild.contains(info.pos())) continue;

            if (level.getMinBuildHeight() >= info.pos().getY())
            {
                return new PlaceInfo(CanPlace.NO, info, -1);
            }
            if (level.getMaxBuildHeight() <= info.pos().getY())
            {
                return new PlaceInfo(CanPlace.NO, info, -1);
            }
            int index = sortedNeededItems.indexOf(neededItems.get(info.pos()));
            PlaceInfo canPlace = canPlace(info, index);
            if (canPlace.valid == CanPlace.NO)
            {
                return canPlace;
            }
            else if (canPlace.valid == CanPlace.NEED_ITEM)
            {
                // Check if the bill of materials has this listed as to ignore.
                checkBoM();
                return canPlace;
            }
            // We do not use the "recommended" rotate function, as that is
            // for blocks already in world. Using it prevents pistons from
            // rotating properly!
            @SuppressWarnings("deprecation")
            BlockState placeState = info.state().rotate(settings.getRotation());// .mirror(settings.getMirror())
            BlockState old = level.getBlockState(info.pos());
            boolean same = old.isAir() & placeState.isAir();
            placeOrder.remove(info);
            if (same)
            {
                // Skip and decrement here
                i--;
                continue;
            }

            return canPlace;
        }
        return null;
    }

    public boolean tryPlace(PlaceInfo placement, ServerLevel level)
    {
        if (placement != null)
        {
            var info = placement.info;
            switch (placement.valid)
            {
            case NEED_ITEM:
                break;
            case NO:
                placeOrder.remove(info);
                pendingBuild.remove(info.pos());
                break;
            case YES:
                // We do not use the "recommended" rotate function, as that is
                // for blocks already in world. Using it prevents pistons from
                // rotating properly!
                @SuppressWarnings("deprecation")
                BlockState placeState = info.state().rotate(settings.getRotation());// .mirror(settings.getMirror())
                BlockState old = level.getBlockState(info.pos());
                boolean same = old.isAir() & placeState.isAir();
                placeOrder.remove(info);
                pendingBuild.remove(info.pos());
                if (same) break;

                if (placement.itemSlot >= 0)
                {
                    ItemStack needed = neededItems.get(info.pos());
                    if (needed != null) needed.shrink(1);
                    ItemStack inSlot = itemSource.getStackInSlot(placement.itemSlot);
                    inSlot.shrink(1);
                }
                StructureTemplateTools.getPlacer(placeState).placeBlock(placeState, info.pos(), level);
                break;
            default:
                break;
            }
            return false;
        }
        this.done = true;
        return true;
    }

    @Override
    public void onTickEnd(ServerLevel level)
    {
        checkBlueprint(level);
        if (!creative || removeOrder.isEmpty())
        {
            if (passes++ < 3)
            {
                _template = null;
                return;
            }
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
            PlaceInfo placement = getNextPlacement(level);
            if (!(ended = tryPlace(placement, level))) continue;
            if (ended) break;
        }
        if (!ended) return;
        // If we finished, remove.

        if (passes < 3)
        {
            _template = null;
            return;
        }
        WorldTickManager.removeWorldData(level.dimension(), this);
        PokecubeAPI.LOGGER.info("Finished structure!");
    }
}
