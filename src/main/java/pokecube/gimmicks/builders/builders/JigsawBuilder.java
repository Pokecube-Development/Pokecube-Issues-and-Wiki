package pokecube.gimmicks.builders.builders;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.google.common.collect.Maps;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandlerModifiable;
import pokecube.world.gen.structures.pool_elements.ExpandedJigsawPiece;
import thut.api.level.structures.NamedVolumes.INamedStructure;

/**
 * This class is effectively a list of {@link StructureBuilder}, which is
 * initialised from the result of a jigsaw structure assembly. We also have the
 * functions for saving/loading the state of the structure, so that we do not
 * reset and randomise when world closes and re-opens.<br>
 * <br>
 * Our {@link IBlocksBuilder} and {@link IBlocksClearer} methods are forwarded
 * to the appropriate entries in our list of {@link StructureBuilder}.
 *
 */
public class JigsawBuilder implements INBTSerializable<CompoundTag>, IBlocksBuilder, IBlocksClearer
{
    public List<StructureBuilder> builders = new ArrayList<>();
    private ServerLevel level;
    private boolean creative = false;

    public JigsawBuilder()
    {}

    public JigsawBuilder(ServerLevel level, INamedStructure structure)
    {
        for (var part : structure.getParts())
        {
            if (part.getWrapped() instanceof PoolElementStructurePiece pooled)
            {
                if (pooled.getElement() instanceof SinglePoolElement elem)
                {
                    BlockPos origin = pooled.getPosition();
                    var builder = new StructureBuilder(origin, pooled.getRotation(), pooled.getMirror());

                    // Now make the settings object
                    StructurePlaceSettings settings = null;
                    if (elem instanceof ExpandedJigsawPiece jig)
                    {
                        settings = jig.getSettings(pooled.getRotation(), null, false);
                    }
                    builder.settings = settings;
                    builder._source = pooled;
                    builder._loaded = elem.getTemplate(level.getStructureManager());
                    builder.checkBlueprint(level);
                    builders.add(builder);
                }
            }
        }
    }

    public JigsawBuilder(StructurePiecesBuilder pieceBuilder, BlockPos shift, ServerLevel level)
    {
        pieceBuilder.build().pieces().forEach(piece -> {
            if (piece instanceof PoolElementStructurePiece pooled)
            {
                if (pooled.getElement() instanceof SinglePoolElement elem)
                {
                    BlockPos origin = pooled.getPosition().offset(shift);
                    var builder = new StructureBuilder(origin, pooled.getRotation(), pooled.getMirror());

                    // Now make the settings object
                    StructurePlaceSettings settings = null;
                    if (elem instanceof ExpandedJigsawPiece jig)
                    {
                        settings = jig.getSettings(pooled.getRotation(), null, false);
                    }
                    builder.settings = settings;
                    builder._source = pooled;
                    builder._loaded = elem.getTemplate(level.getStructureManager());
                    builder.checkBlueprint(level);
                    builders.add(builder);
                }
            }
        });
    }

    @Override
    public CompoundTag serializeNBT()
    {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        builders.forEach(b -> {
            var nbt = b.serializeNBT();
            if (!nbt.isEmpty())
            {
                list.add(nbt);
            }
        });
        tag.put("builders", list);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt)
    {
        ListTag list = nbt.getList("builders", Tag.TAG_COMPOUND);
        this.builders.clear();
        list.forEach(tag -> {
            if (tag instanceof CompoundTag nbt2)
            {
                StructureBuilder builder = new StructureBuilder();
                builder.deserializeNBT(nbt2);
                this.builders.add(builder);
            }
        });
    }

    private StructureBuilder next()
    {
        if (builders.size() == 0) return null;
        while (builders.size() > 0 && !builders.get(0).validBuilder())
        {
            builders.remove(0);
        }
        if (builders.size() == 0) return null;
        return builders.get(0);
    }

    @Override
    public boolean tryClear(Consumer<ItemStack> dropHandler)
    {
        var next = next();
        if (next == null) return false;
        return next.tryClear(dropHandler);
    }

    @Override
    public BlockPos nextRemoval()
    {
        var next = next();
        if (next == null) return null;
        return next.nextRemoval();
    }

    @Override
    public void markPendingClear(BlockPos pos)
    {
        var next = next();
        if (next == null) return;
        next.markPendingClear(pos);
    }

    @Override
    public void markCleared(BlockPos pos)
    {
        var next = next();
        if (next == null) return;
        next.markCleared(pos);
    }

    @Override
    public boolean validBuilder()
    {
        var next = next();
        if (next == null) return false;
        return next.validBuilder();
    }

    @Override
    public void clearBoMRecords()
    {
        var next = next();
        if (next == null) return;
        next.clearBoMRecords();
    }

    @Override
    public void addBoMRecord(BoMRecord BoM)
    {
        var next = next();
        if (next == null) return;
        next.addBoMRecord(BoM);
    }

    @Override
    public void provideBoM(BoMRecord record, boolean onlyNeeded)
    {
        if (onlyNeeded)
        {
            var next = next();
            if (next == null) return;
            next.provideBoM(record, onlyNeeded);
        }
        else
        {
            List<ItemStack> items = new ArrayList<>();
            // Recompute entire list, and provide that.
            Map<Item, List<ItemStack>> stacks = Maps.newHashMap();
            record.neededStacks().clear();

            for (var builder : this.builders)
            {
                builder.provideBoM(record, onlyNeeded);
                for (var stack : record.neededStacks())
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
            record.neededStacks().clear();
            record.neededStacks().addAll(items);
        }
    }

    @Override
    public void checkBoM(BoMRecord record)
    {
        var next = next();
        if (next == null) return;
        next.checkBoM(record);
    }

    @Override
    public ServerLevel getLevel()
    {
        return this.level;
    }

    @Override
    public void update(ServerLevel level)
    {
        this.level = level;
        for (var builder : this.builders) builder.update(level);
    }

    @Override
    public boolean tryPlace(PlaceInfo placement, IItemHandlerModifiable itemSource)
    {
        var next = next();
        if (next == null) return false;
        return next.tryPlace(placement, itemSource);
    }

    @Override
    public PlaceInfo getNextPlacement(IItemHandlerModifiable itemSource)
    {
        var next = next();
        if (next == null) return null;
        return next.getNextPlacement(itemSource);
    }

    @Override
    public PlaceInfo canPlace(StructureBlockInfo info, IItemHandlerModifiable itemSource)
    {
        var next = next();
        if (next == null) return null;
        return next.canPlace(info, itemSource);
    }

    @Override
    public void markPendingBuild(BlockPos pos)
    {
        var next = next();
        if (next == null) return;
        next.markPendingBuild(pos);
    }

    @Override
    public void markBuilt(BlockPos pos)
    {
        var next = next();
        if (next == null) return;
        next.markBuilt(pos);
    }

    @Override
    public void getNextNeeded(List<ItemStack> requested, int number)
    {
        var next = next();
        if (next == null) return;
        next.getNextNeeded(requested, number);
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
        for (var builder : this.builders) builder.setCreative(creative);
    }
}
