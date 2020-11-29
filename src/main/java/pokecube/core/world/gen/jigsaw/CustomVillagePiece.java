package pokecube.core.world.gen.jigsaw;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.gen.feature.jigsaw.JigsawPiece;
import net.minecraft.world.gen.feature.structure.AbstractVillagePiece;
import net.minecraft.world.gen.feature.structure.IStructurePieceType;
import net.minecraft.world.gen.feature.template.TemplateManager;
import pokecube.core.database.worldgen.WorldgenHandler.JigSawConfig;
import pokecube.core.database.worldgen.WorldgenHandler.Options;

public class CustomVillagePiece extends AbstractVillagePiece
{
    public static IStructurePieceType PCVP;

    public JigSawConfig struct_config;

    public Options opts;

    public CustomVillagePiece(final TemplateManager manager, final JigsawPiece piece, final BlockPos pos, final int dy,
            final Rotation direction, final MutableBoundingBox box)
    {
        super(manager, piece, pos, dy, direction, box);
        this.structurePieceType = CustomVillagePiece.PCVP;
        if (piece instanceof CustomJigsawPiece)
        {
            this.struct_config = ((CustomJigsawPiece) piece).struct_config;
            this.opts = ((CustomJigsawPiece) piece).opts;
        }
    }

    public CustomVillagePiece(final TemplateManager p_i242037_1_, final CompoundNBT nbt)
    {
        super(p_i242037_1_, nbt);
        this.struct_config = JigSawConfig.deserialize(nbt.getString("struct_config"));
        this.opts = Options.deserialize(nbt.getString("opts"));
        this.structurePieceType = CustomVillagePiece.PCVP;
    }

    @Override
    // This is actually write additional, it is badly named
    protected void readAdditional(final CompoundNBT tagCompound)
    {
        super.readAdditional(tagCompound);
        if (this.struct_config != null) tagCompound.putString("struct_config", this.struct_config.serialize());
        if (this.opts != null) tagCompound.putString("opts", this.opts.serialize());
    }

}
