package pokecube.gimmicks.builders.builders;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandlerModifiable;
import pokecube.world.gen.structures.pool_elements.ExpandedJigsawPiece;

/**
 * This class is effectively a list of {@link StructureBuilder}, which is
 * initialised from the result of a jigsaw structure assembly. We also have the
 * functions for saving/loading the state of the structure, so that we do not
 * reset and randomise when world closes and re-opens.
 *
 */
public class JigsawBuilder implements INBTSerializable<CompoundTag>
{
    public List<StructureBuilder> builders = new ArrayList<>();
    public ServerLevel level;

    public JigsawBuilder()
    {}

    public JigsawBuilder(StructurePiecesBuilder pieceBuilder, BlockPos shift, IItemHandlerModifiable itemSource,
            ServerLevel level)
    {
        pieceBuilder.build().pieces().forEach(piece -> {
            if (piece instanceof PoolElementStructurePiece pooled)
            {
                if (pooled.getElement() instanceof SinglePoolElement elem)
                {
                    BlockPos origin = pooled.getPosition().offset(shift);
                    var builder = new StructureBuilder(origin, pooled.getRotation(), pooled.getMirror(), itemSource);

                    // Now make the settings object
                    StructurePlaceSettings settings = null;
                    if (elem instanceof ExpandedJigsawPiece jig)
                        settings = jig.getSettings(pooled.getRotation(), null, false);
                    builder.settings = settings;
                    builder._source = pooled;
                    builder._loaded = elem.getTemplate(level.getStructureManager());
                    builder.checkBlueprint(level);
                    builders.add(builder);
                }
            }
        });
    }

    public void setLevel(ServerLevel level)
    {
        this.level = level;
        for (var b : builders) b.checkBlueprint(level);
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
}
