package pokecube.core.world.gen.feature.scattered;

import java.util.Random;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonSyntaxException;

import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.StructureMode;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
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
import net.minecraftforge.common.MinecraftForge;
import pokecube.core.PokecubeCore;
import pokecube.core.database.worldgen.WorldgenHandler;
import pokecube.core.database.worldgen.WorldgenHandler.JsonStructure;
import pokecube.core.events.StructureEvent;
import pokecube.core.world.gen.template.PokecubeStructureProcessor;
import thut.api.terrain.BiomeType;
import thut.api.terrain.TerrainManager;
import thut.api.terrain.TerrainSegment;

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
    private JsonStructure          struct = new JsonStructure();
    private boolean                set    = false;

    public ConfigStructurePiece(final TemplateManager manager, final CompoundNBT nbt)
    {
        super(ConfigStructurePiece.CONFIGTYPE, nbt);
        this.template = new ResourceLocation(nbt.getString("Template"));
        this.rot = Rotation.valueOf(nbt.getString("Rot"));
        this.centreOffset = new BlockPos(nbt.getInt("cox"), nbt.getInt("coy"), nbt.getInt("coz"));
        this.set = nbt.getBoolean("set");
        try
        {
            this.struct = WorldgenHandler.GSON.fromJson(nbt.getString("struct"), JsonStructure.class);
        }
        catch (final JsonSyntaxException e)
        {
            this.struct = new JsonStructure();
            PokecubeCore.LOGGER.error("Error reading structure", e);
        }
        this.init(manager);
    }

    public ConfigStructurePiece(final TemplateManager manager, final ResourceLocation loc, final Rotation rot,
            final BlockPos pos, final BlockPos offset, final JsonStructure struct)
    {
        super(ConfigStructurePiece.CONFIGTYPE, 0);
        this.template = loc;
        this.rot = rot;
        this.templatePosition = pos;
        this.centreOffset = offset;
        this.init(manager);
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
        if (this.struct != null) nbt.putString("struct", this.struct.serialize());
    }

    private void init(final TemplateManager manager)
    {
        this.to_build = manager.getTemplateDefaulted(this.template);
        final PlacementSettings placementsettings = new PlacementSettings().setIgnoreEntities(false).setRotation(
                this.rot).setCenterOffset(this.centreOffset).setMirror(Mirror.NONE).addProcessor(
                        ConfigStructurePiece.IGNORELIST).addProcessor(PokecubeStructureProcessor.PROCESSOR);
        this.setup(this.to_build, this.templatePosition, placementsettings);
    }

    @Override
    public boolean addComponentParts(final IWorld worldIn, final Random randomIn,
            final MutableBoundingBox structureBoundingBoxIn, final ChunkPos p_74875_4_)
    {
        try
        {
            if (!this.set)
            {
                int i = this.templatePosition.getY();
                final BlockPos blockpos = this.templatePosition.add(this.to_build.getSize().getX() - 1, 0, this.to_build
                        .getSize().getZ() - 1);
                boolean inWater = false;
                if (this.struct.surface) for (final BlockPos blockpos1 : BlockPos.getAllInBoxMutable(
                        this.templatePosition, blockpos))
                {
                    final int k = worldIn.getHeight(Heightmap.Type.OCEAN_FLOOR_WG, blockpos1.getX(), blockpos1.getZ());
                    if (k == 0) // Not sure what to do here?
                        continue;
                    inWater = inWater || worldIn.getBlockState(blockpos1.up()).getMaterial() == Material.WATER;
                    i = Math.min(i, k);
                    // TODO maybe use average?
                }
                if (inWater && !this.struct.water) return false;
                i += this.struct.offset;
                System.out.println("Shifted: " + this.templatePosition.getY() + "->" + i);
                this.templatePosition = new BlockPos(this.templatePosition.getX(), i, this.templatePosition.getZ());

                for (final Template.BlockInfo info : this.to_build.func_215381_a(this.templatePosition,
                        this.placeSettings, Blocks.STRUCTURE_BLOCK))
                    if (info.nbt != null)
                    {
                        final StructureMode structuremode = StructureMode.valueOf(info.nbt.getString("mode"));
                        if (structuremode == StructureMode.DATA) this.handleDataMarker(info.nbt.getString("metadata"),
                                info.pos, worldIn, randomIn, structureBoundingBoxIn);
                    }
                this.set = true;
            }
            PokecubeCore.LOGGER.debug("Adding parts for: " + this.template + ", " + this.templatePosition);

            final boolean built = super.addComponentParts(worldIn, randomIn, structureBoundingBoxIn, p_74875_4_);
            terrain:
            if (built)
            {
                final BiomeType type = BiomeType.getBiome(this.struct.biomeType, true);
                if (type == BiomeType.NONE) break terrain;
                final MutableBlockPos pos = new MutableBlockPos();
                for (int x = this.boundingBox.minX; x <= this.boundingBox.maxX; x++)
                    for (int y = this.boundingBox.minY; y <= this.boundingBox.maxY; y++)
                        for (int z = this.boundingBox.minZ; z <= this.boundingBox.maxZ; z++)
                        {
                            pos.setPos(x, y, z);
                            if (this.boundingBox.isVecInside(pos))
                            {
                                final TerrainSegment t = TerrainManager.getInstance().getTerrain(worldIn, pos);
                                t.setBiome(pos, type.getType());
                            }
                        }
            }
            return built;
        }
        catch (final Exception e)
        {
            PokecubeCore.LOGGER.error("Error building " + this.template, e);
            return false;
        }
    }

    @Override
    protected void handleDataMarker(final String function, final BlockPos pos, final IWorld worldIn, final Random rand,
            final MutableBoundingBox sbb)
    {
        if (function.equalsIgnoreCase("Floor") && !this.set)
        {
            final int y = 2 * this.templatePosition.getY() - pos.getY();
            this.templatePosition = new BlockPos(this.templatePosition.getX(), y, this.templatePosition.getZ());
        }

        if (!this.set || function.equalsIgnoreCase("Floor")) return;
        System.out.println(function);
        if (function.startsWith("pokecube:chest:"))
        {
            final BlockPos blockpos = pos.down();
            worldIn.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
            final ResourceLocation key = new ResourceLocation(function.replaceFirst("pokecube:chest:", ""));
            if (sbb.isVecInside(blockpos)) LockableLootTileEntity.setLootTable(worldIn, rand, blockpos, key);
        }
        else if (function.startsWith("Chest "))
        {
            final BlockPos blockpos = pos.down();
            worldIn.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
            final ResourceLocation key = new ResourceLocation(function.replaceFirst("Chest ", ""));
            if (sbb.isVecInside(blockpos)) LockableLootTileEntity.setLootTable(worldIn, rand, blockpos, key);
        }
        else MinecraftForge.EVENT_BUS.post(new StructureEvent.ReadTag(function, pos, worldIn, rand, sbb));
    }
}
