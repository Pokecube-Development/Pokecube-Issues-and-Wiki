package pokecube.core.world.gen.feature.scattered;

import java.util.Random;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.StructureMode;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.structure.IStructurePieceType;
import net.minecraft.world.gen.feature.structure.TemplateStructurePiece;
import net.minecraft.world.gen.feature.template.BlockIgnoreStructureProcessor;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;

public class ConfigStructurePiece extends TemplateStructurePiece
{
    public static final BlockIgnoreStructureProcessor IGNORELIST = new BlockIgnoreStructureProcessor(ImmutableList.of(
            Blocks.STRUCTURE_BLOCK, Blocks.JIGSAW));
    public static IStructurePieceType                 CONFIGTYPE = (manager, nbt) -> new ConfigStructurePiece(manager,
            nbt);

    private Template               to_build;
    private final ResourceLocation template;
    private final Rotation         rot;
    private final BlockPos         centreOffset;
    private boolean                set = false;

    public ConfigStructurePiece(final TemplateManager manager, final CompoundNBT nbt)
    {
        super(ConfigStructurePiece.CONFIGTYPE, nbt);
        this.template = new ResourceLocation(nbt.getString("Template"));
        this.rot = Rotation.valueOf(nbt.getString("Rot"));
        this.centreOffset = new BlockPos(nbt.getInt("cox"), nbt.getInt("coy"), nbt.getInt("coz"));
        this.set = nbt.getBoolean("set");
        this.init(manager);
    }

    public ConfigStructurePiece(final TemplateManager manager, final ResourceLocation loc, final Rotation rot,
            final BlockPos pos, final BlockPos offset)
    {
        super(ConfigStructurePiece.CONFIGTYPE, 0);
        this.template = loc;
        this.rot = rot;
        this.templatePosition = pos;
        this.centreOffset = offset;
        this.init(manager);
    }

    private void init(final TemplateManager manager)
    {
        this.to_build = manager.getTemplateDefaulted(this.template);
        final PlacementSettings placementsettings = new PlacementSettings().setRotation(this.rot).setCenterOffset(
                this.centreOffset).setMirror(Mirror.NONE).addProcessor(ConfigStructurePiece.IGNORELIST);
        this.setup(this.to_build, this.templatePosition, placementsettings);
    }

    @Override
    public boolean addComponentParts(final IWorld worldIn, final Random randomIn,
            final MutableBoundingBox structureBoundingBoxIn, final ChunkPos p_74875_4_)
    {
        if (!this.set)
        {
            int i = 256;
            final BlockPos blockpos = this.templatePosition.add(this.to_build.getSize().getX() - 1, 0, this.to_build
                    .getSize().getZ() - 1);

            for (final BlockPos blockpos1 : BlockPos.getAllInBoxMutable(this.templatePosition, blockpos))
            {
                final int k = worldIn.getHeight(Heightmap.Type.WORLD_SURFACE_WG, blockpos1.getX(), blockpos1.getZ());
                i = Math.min(i, k);
                // TODO maybe use average?
            }
            this.templatePosition = new BlockPos(this.templatePosition.getX(), i, this.templatePosition.getZ());

            for (final Template.BlockInfo info : this.to_build.func_215381_a(this.templatePosition, this.placeSettings,
                    Blocks.STRUCTURE_BLOCK))
                if (info.nbt != null)
                {
                    final StructureMode structuremode = StructureMode.valueOf(info.nbt.getString("mode"));
                    if (structuremode == StructureMode.DATA) this.handleDataMarker(info.nbt.getString("metadata"),
                            info.pos, worldIn, randomIn, structureBoundingBoxIn);
                }
            this.set = true;
        }
        return super.addComponentParts(worldIn, randomIn, structureBoundingBoxIn, p_74875_4_);
    }

    @Override
    protected void readAdditional(final CompoundNBT nbt)
    {
        super.readAdditional(nbt);
        nbt.putString("Template", this.template.toString());
        nbt.putString("Rot", this.rot.toString());
        nbt.putInt("cox", this.centreOffset.getX());
        nbt.putInt("coy", this.centreOffset.getY());
        nbt.putInt("coz", this.centreOffset.getZ());
        nbt.putBoolean("set", this.set);
    }

    @Override
    protected void handleDataMarker(final String function, final BlockPos pos, final IWorld worldIn, final Random rand,
            final MutableBoundingBox sbb)
    {
        // TODO more tags support
        if (function.equalsIgnoreCase("Floor") && !this.set)
        {
            final int y = 2 * this.templatePosition.getY() - pos.getY();
            this.templatePosition = new BlockPos(this.templatePosition.getX(), y, this.templatePosition.getZ());
        }

    }

}
