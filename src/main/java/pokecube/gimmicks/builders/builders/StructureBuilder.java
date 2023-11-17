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
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.WritableBookItem;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.JigsawReplacementProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandlerModifiable;
import pokecube.api.PokecubeAPI;
import pokecube.gimmicks.builders.BuilderTasks;
import pokecube.world.gen.structures.pool_elements.ExpandedJigsawPiece;
import pokecube.world.gen.structures.processors.MarkerToAirProcessor;
import thut.api.util.JsonUtil;
import thut.api.world.StructureTemplateTools;
import thut.api.world.StructureTemplateTools.PlaceContext;
import thut.lib.TComponent;

/**
 * This {@link IBlocksBuilder} and {@link IBlocksClearer} handles constructing a
 * building based on a defined {@link StructureTemplate}.
 */
public class StructureBuilder implements INBTSerializable<CompoundTag>, IBlocksBuilder, IBlocksClearer
{

    public static Supplier<StructureTemplate> loadFromTemplate(StructureBuilder builder)
    {
        return () -> {
            if (builder._loaded != null) return builder._loaded;

            // Load our pool element from tag
            if (builder._source_tag != null)
            {
                var ctx = StructurePieceSerializationContext.fromLevel(builder.level);
                builder._source = new PoolElementStructurePiece(ctx, builder._source_tag);
                // Now discard the tag
                builder._source_tag = null;
            }

            // Now load our template from the pool element
            if (builder._source != null)
            {
                ServerLevel level = builder.level;
                var e = builder._source.getElement();
                // The below block is very similar to what is done in
                // JigsawBuilder when it makes it initially.
                if (e instanceof SinglePoolElement elem)
                {
                    // Now make the settings object
                    StructurePlaceSettings settings = null;
                    if (elem instanceof ExpandedJigsawPiece jig)
                        settings = jig.getSettings(builder._source.getRotation(), null, false);
                    // Then set the settings
                    builder.settings = settings;
                    // And load the template
                    builder._loaded = elem.getTemplate(level.getStructureManager());
                }
            }

            // Otherwise check the toMake
            ResourceLocation toMake = builder.toMake;
            if (toMake == null)
            {
                PokecubeAPI.LOGGER.error("No ResourceLocation!");
                return null;
            }
            var opt = builder.level.getStructureManager().get(toMake);
            if (!opt.isPresent())
            {
                PokecubeAPI.LOGGER.error("No Template for {}!", toMake);
                return null;
            }
            return builder._loaded = opt.get();
        };
    }

    private boolean done = false;
    private boolean creative = false;

    private Supplier<StructureTemplate> keyProvider;
    private StructureTemplate _template;

    protected StructurePlaceSettings settings;
    protected PoolElementStructurePiece _source;
    protected StructureTemplate _loaded;

    protected ResourceLocation toMake;

    private PlaceContext placement;

    private CompoundTag _source_tag;
    private BlockPos origin;
    private ServerLevel level;
    private Rotation rotation;
    private Mirror mirror;

    /**
     * Map of y-coordinate -> list of blocks to place/remove
     */
    private Map<Integer, List<StructureBlockInfo>> removeOrder = new HashMap<>();
    /**
     * Sorted list of keys for {@link #removeOrder}
     */
    private List<Integer> ys = new ArrayList<>();
    /**
     * List of blocks to place, in the order that the {@link StructureTemplate}
     * defines. This order ensures proper placement of torches, redstone wire,
     * etc.
     */
    private List<StructureBlockInfo> placeOrder = new ArrayList<>();
    /**
     * Map of location - itemstack needed to place at that location. The values
     * in this map are not unique.
     */
    private Map<BlockPos, ItemStack> neededItems = Maps.newHashMap();
    /**
     * A sorted list of the values from {@link #neededItems}, sorted with
     * largest amount needed first. This is what gets printed in the BoM.
     */
    private List<ItemStack> sortedNeededItems = new ArrayList<>();

    /**
     * Set of items we don't have to place, by index in
     * {@link #sortedNeededItems}
     */
    private BitSet missingItems = new BitSet();
    /**
     * Set of items we explicitly ignore for placement, set in
     * {@link #checkBoM(pokecube.gimmicks.builders.builders.IBlocksBuilder.BoMRecord)},
     * and by index in {@link #sortedNeededItems}
     */
    private BitSet ignoredItems = new BitSet();

    private List<BoMRecord> BoMs = new ArrayList<>();
    private Set<BlockPos> pendingBuild = new HashSet<>();
    private Set<BlockPos> pendingClear = new HashSet<>();

    private int passes = 0;

    public StructureBuilder()
    {
        this.keyProvider = loadFromTemplate(this);
    }

    public StructureBuilder(BlockPos origin, Rotation rotation, Mirror mirror)
    {
        this();
        this.origin = origin;

        if (mirror == null) mirror = Mirror.NONE;
        if (rotation == null) rotation = Rotation.NONE;

        this.rotation = rotation;
        this.mirror = mirror;
    }

    @Override
    public void clearBoMRecords()
    {
        BoMs.clear();
    }

    @Override
    public void addBoMRecord(BoMRecord BoM)
    {
        BoMs.add(BoM);
    }

    /**
     * Checks if the bill of materials has marked the stacks as do not require.
     */
    @Override
    public void checkBoM(BoMRecord BoM)
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

    @Override
    public void provideBoM(BoMRecord BoM, boolean onlyNeeded)
    {
        ItemStack book = BoM.BoMProvider().get();
        boolean compiled = false;
        if (book.getItem() instanceof WritableBookItem || (compiled = (book.getItem() instanceof WrittenBookItem)))
        {
            // Check first to see if maybe things were denied.
            checkBoM(BoM);

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
            this.getNextNeeded(requested, 24);
            for (var needed : requested)
            {
                if (needed == null || needed.isEmpty()) continue;
                if (n++ > 8) break;
                MutableComponent name = (MutableComponent) needed.getDisplayName();
                name.setStyle(name.getStyle().withColor(0));
                String count = needed.getCount() + "x";
                int index = sortedNeededItems.indexOf(needed);
                if (index >= 0 && this.ignoredItems.get(index)) continue;
                msg = msg.append(TComponent.literal("\n")).append(TComponent.literal(count)).append(name);
            }

            if (compiled) pages.add(StringTag.valueOf(Component.Serializer.toJson(msg)));
            else pages.add(StringTag.valueOf(msg.getString()));

            n = 0;
            msg = TComponent.literal("Total Cost:");

            List<ItemStack> items = this.sortedNeededItems;
            if (!onlyNeeded)
            {
                // Otherwise provide to the BoM stacks.
                items = BoM.neededStacks();
                items.clear();

                var builder = this;
                // Recompute entire list, and provide that.
                Map<Item, List<ItemStack>> stacks = Maps.newHashMap();

                for (var info : builder.placeOrder)
                {
                    ItemStack stack = StructureTemplateTools.getForInfo(info);
                    if (stack != null && !stack.isEmpty()) items.add(stack);
                }

                for (var stack : items)
                {
                    var list = stacks.getOrDefault(stack.getItem(), new ArrayList<>());
                    stacks.put(stack.getItem(), list);
                    if (list.isEmpty()) list.add(stack);
                    else
                    {
                        if (!stack.hasTag())
                        {
                            boolean found = false;
                            for (ItemStack held : list)
                            {
                                if (!held.hasTag())
                                {
                                    held.grow(stack.getCount());
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) list.add(stack);
                        }
                        else
                        {
                            boolean found = false;
                            for (ItemStack held : list)
                            {
                                if (held.hasTag() && held.getTag().equals(stack.getTag()))
                                {
                                    held.grow(stack.getCount());
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) list.add(stack);
                        }
                    }
                }
                items.clear();
                for (var list : stacks.values()) items.addAll(list);
            }
            for (int i = 0; i < items.size(); i++)
            {
                ItemStack s = items.get(i);
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
                if (onlyNeeded && this.ignoredItems.get(i)) count = "-x";
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

    @Override
    public ServerLevel getLevel()
    {
        return this.level;
    }

    @Override
    public void update(ServerLevel level)
    {
        checkBlueprint(level);
    }

    /**
     * Here we ensure the {@link StructureTemplate} we are using is valid, and
     * populate maps of location - {@link StructureBlockInfo} for
     * removal/construction.
     * 
     * @param level
     */
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

            BlockPos location = origin;
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
            pendingClear.clear();
            placeOrder.clear();
            placeOrder.addAll(infos);
            neededItems.clear();
            neededItems = StructureTemplateTools.getNeededMaterials(level, infos, BuilderTasks.config::canHaveTag);
            if (neededItems.isEmpty())
            {
                this.done = true;
                return;
            }
            sortedNeededItems.clear();
            // The set is to ensure each stack is only added once!
            sortedNeededItems.addAll(Sets.newHashSet(neededItems.values()));
            sortedNeededItems.sort((a, b) -> b.getCount() - a.getCount());

            // Update the BoMs
            for (var BoM : BoMs) provideBoM(BoM, true);

            // Now populate the map of removal positions to check. this is
            // sorted by y value, to allow removal in the approprate order.
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
            ys.clear();
            ys.addAll(removeOrder.keySet());
            ys.sort(null);
        }
        // If key removed, clear as well
        else
        {
            pendingBuild.clear();
            pendingClear.clear();
            placeOrder.clear();
            removeOrder.clear();
            missingItems.clear();
            ignoredItems.clear();
        }
    }

    @Override
    public void markPendingClear(BlockPos pos)
    {
        pendingClear.add(pos);
    }

    @Override
    public void markCleared(BlockPos pos)
    {
        pendingClear.remove(pos);
    }

    @Override
    public BlockPos nextRemoval()
    {
        for (int i = ys.size() - 1; i >= 0; i--)
        {
            int y = ys.get(i);
            List<StructureBlockInfo> infos = removeOrder.get(y);
            if (infos == null) continue;
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

    @Override
    public boolean tryClear(Consumer<ItemStack> dropHandler)
    {
        BlockPos pos = nextRemoval();
        if (pos == null) return true;
        BlockState state = level.getBlockState(pos);
        if (!creative)
        {
            final List<ItemStack> list = Block.getDrops(state, level, pos, level.getBlockEntity(pos));
            list.forEach(dropHandler);
        }
        level.destroyBlock(pos, false);
        return true;
    }

    @Override
    public void markPendingBuild(BlockPos pos)
    {
        pendingBuild.add(pos);
    }

    @Override
    public void markBuilt(BlockPos pos)
    {
        pendingBuild.remove(pos);
    }

    @Override
    public PlaceInfo canPlace(StructureBlockInfo info, IItemHandlerModifiable itemSource)
    {
        if (info.state == null || info.state.isAir()) return new PlaceInfo(CanPlace.YES, info, -1);

        int index = sortedNeededItems.indexOf(neededItems.get(info.pos));
        // If we are marked as to ignore this point. return no directly, this
        // skips the placement.
        if (index >= 0 && ignoredItems.get(index)) return new PlaceInfo(CanPlace.NO, info, -1);

        ItemStack stack = StructureTemplateTools.getForInfo(info);
        if (!stack.isEmpty())
        {
            if (creative)
            {
                return new PlaceInfo(CanPlace.YES, info, -1);
            }

            // Only check needed item if not creative
            if (!creative && itemSource != null) for (int i = 1; i < itemSource.getSlots(); i++)
            {
                ItemStack inSlot = itemSource.getStackInSlot(i);
                if (ItemStack.isSame(stack, inSlot))
                {
                    return new PlaceInfo(CanPlace.YES, info, i);
                }
            }

            ItemStack needed = neededItems.get(info.pos);

            if (needed == null)
            {
                return new PlaceInfo(CanPlace.NO, info, -1);
            }
            else
            {
                missingItems.set(index);
            }
            return new PlaceInfo(CanPlace.NEED_ITEM, info, -1);
        }
        return new PlaceInfo(CanPlace.NO, info, -1);
    }

    @Override
    public PlaceInfo getNextPlacement(IItemHandlerModifiable itemSource)
    {
        for (int i = 0; i < placeOrder.size(); i++)
        {
            var info = placeOrder.get(i);
            // Someone else is working on this.
            if (pendingBuild.contains(info.pos)) continue;

            if (level.getMinBuildHeight() >= info.pos.getY())
            {
                return new PlaceInfo(CanPlace.NO, info, -1);
            }
            if (level.getMaxBuildHeight() <= info.pos.getY())
            {
                return new PlaceInfo(CanPlace.NO, info, -1);
            }
            PlaceInfo canPlace = canPlace(info, itemSource);
            if (canPlace.valid() == CanPlace.NO)
            {
                return canPlace;
            }
            else if (canPlace.valid() == CanPlace.NEED_ITEM)
            {
                // Check if the bill of materials has this listed as to ignore.
                for (var BoM : BoMs) checkBoM(BoM);
                return canPlace;
            }
            // We do not use the "recommended" rotate function, as that is
            // for blocks already in world. Using it prevents pistons from
            // rotating properly!
            @SuppressWarnings("deprecation")
            BlockState placeState = info.state.rotate(settings.getRotation());// .mirror(settings.getMirror())
            BlockState old = level.getBlockState(info.pos);
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

        if (passes++ < 3)
        {
            // Reset and check again
            this._template = null;
            this.checkBlueprint(level);
            return getNextPlacement(itemSource);
        }
        this.done = true;
        return null;
    }

    @Override
    public boolean tryPlace(PlaceInfo placement, IItemHandlerModifiable itemSource)
    {
        if (placement != null)
        {
            var info = placement.info();
            var type = placement.valid();

            // If placement has NEED_ITEM, and this is called anyway, re-check.
            if (type == CanPlace.NEED_ITEM || type == CanPlace.YES)
            {
                placement = canPlace(info, itemSource);
                type = placement.valid();
            }

            switch (type)
            {
            case NEED_ITEM:
                break;
            case NO:
                placeOrder.remove(info);
                markBuilt(info.pos);
                break;
            case YES:
                BlockState old = level.getBlockState(info.pos);
                boolean same = old.isAir() & info.state.isAir();
                placeOrder.remove(info);
                markBuilt(info.pos);
                if (same) break;

                if (placement.itemSlot() >= 0 && itemSource != null)
                {
                    ItemStack needed = neededItems.remove(info.pos);
                    if (needed != null) needed.shrink(1);
                    ItemStack inSlot = itemSource.getStackInSlot(placement.itemSlot());
                    inSlot.shrink(1);
                    itemSource.setStackInSlot(placement.itemSlot(), inSlot);
                }
                StructureTemplateTools.placeBlock(info, getPlacement());
                break;
            default:
                break;
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean validBuilder()
    {
        return _template != null && !done;
    }

    @Override
    public void getNextNeeded(List<ItemStack> requested, int number)
    {
        int n = 0;
        needed_check:
        for (int i = 0, max = placeOrder.size(); i < max; i++)
        {
            ItemStack needed = neededItems.get(placeOrder.get(i).pos);
            if (needed == null || needed.isEmpty()) continue;
            int index = sortedNeededItems.indexOf(needed);
            if (index >= 0 && this.ignoredItems.get(index)) continue;
            if (n++ > number) break;
            for (var stack : requested) if (ItemStack.isSame(stack, needed)) continue needed_check;
            requested.add(needed);
        }
    }

    @Override
    public CompoundTag serializeNBT()
    {
        CompoundTag tag = new CompoundTag();
        if (this._source != null)
        {
            var contx = StructurePieceSerializationContext.fromLevel(level);;
            tag.put("source", this._source.createTag(contx));
            tag.putString("rotation", this.rotation.name());
            tag.putString("mirror", this.mirror.name());
            tag.put("origin", this.newIntegerList(this.origin.getX(), this.origin.getY(), this.origin.getZ()));
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt)
    {
        this._source_tag = nbt.getCompound("source");
        this.rotation = Rotation.valueOf(nbt.getString("rotation"));
        this.mirror = Mirror.valueOf(nbt.getString("mirror"));
        ListTag list = nbt.getList("origin", Tag.TAG_INT);
        this.origin = new BlockPos(list.getInt(0), list.getInt(1), list.getInt(2));
    }

    private ListTag newIntegerList(int... array)
    {
        ListTag listtag = new ListTag();

        for (int i : array)
        {
            listtag.add(IntTag.valueOf(i));
        }

        return listtag;
    }

    @Override
    public boolean isCreative()
    {
        return this.creative;
    }

    @Override
    public void setCreative(boolean creative)
    {
        this.creative = creative;
    }

    private PlaceContext getPlacement()
    {
        if (placement == null)
        {
            placement = new PlaceContext(settings, level, (info) -> {
                if (this.isCreative()) return true;
                ItemStack stack = StructureTemplateTools.getForInfo(info);
                return BuilderTasks.config.canHaveTag(stack);
            });
        }
        return placement;
    }
}
