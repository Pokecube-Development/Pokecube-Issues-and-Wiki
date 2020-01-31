package pokecube.core.world.gen.feature.scattered.jigsaw;

import java.util.List;
import java.util.Random;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;

import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.StructureMode;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.jigsaw.JigsawManager;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern.PlacementBehaviour;
import net.minecraft.world.gen.feature.jigsaw.JigsawPiece;
import net.minecraft.world.gen.feature.jigsaw.ListJigsawPiece;
import net.minecraft.world.gen.feature.jigsaw.SingleJigsawPiece;
import net.minecraft.world.gen.feature.structure.AbstractVillagePiece;
import net.minecraft.world.gen.feature.structure.IStructurePieceType;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.StructureProcessor;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraftforge.common.MinecraftForge;
import pokecube.core.database.worldgen.WorldgenHandler.JigSawConfig;
import pokecube.core.events.StructureEvent;
import pokecube.core.world.gen.template.PokecubeStructureProcessor;

public class JigsawPieces
{
    public static final IStructurePieceType CSP = CustomJigsawPiece::new;

    public static void initStructure(final ChunkGenerator<?> chunk_gen, final TemplateManager templateManagerIn,
            final BlockPos pos, final List<StructurePiece> parts, final SharedSeedRandom rand,
            final JigSawConfig struct)
    {
        final ResourceLocation key = new ResourceLocation(struct.plate_name);
        JigsawManager.func_214889_a(key, struct.size, CustomJigsawPiece::new, chunk_gen, templateManagerIn, pos, parts,
                rand);
    }

    public static void registerJigsaw(final JigSawConfig jigsaw)
    {
        final ResourceLocation platesKey = new ResourceLocation(jigsaw.plate_name);
        final ResourceLocation buildingKey = new ResourceLocation(jigsaw.building_name);
        final List<Pair<JigsawPiece, Integer>> plates = Lists.newArrayList();
        for (final String plate : jigsaw.plates)
            plates.add(Pair.of(new SingleOffsetPiece(plate, ImmutableList.of(PokecubeStructureProcessor.PROCESSOR),
                    JigsawPattern.PlacementBehaviour.RIGID, -jigsaw.offset), 1));
        final List<JigsawPiece> buildings = Lists.newArrayList();
        for (final String part : jigsaw.parts)
            buildings.add(new SingleOffsetPiece(part, ImmutableList.of(PokecubeStructureProcessor.PROCESSOR),
                    JigsawPattern.PlacementBehaviour.RIGID, -jigsaw.offset));

        // Register the plate.
        JigsawManager.field_214891_a.register(new JigsawPattern(platesKey, new ResourceLocation("empty"), plates,
                JigsawPattern.PlacementBehaviour.RIGID));

        // Register the buildings
        JigsawManager.field_214891_a.register(new JigsawPattern(buildingKey, new ResourceLocation("empty"),
                ImmutableList.of(Pair.of(new ListJigsawPiece(buildings, JigsawPattern.PlacementBehaviour.RIGID), 1)),
                JigsawPattern.PlacementBehaviour.RIGID));

    }

    public static class SingleOffsetPiece extends SingleJigsawPiece
    {
        private final int offset;

        public SingleOffsetPiece(final String location, final List<StructureProcessor> processors,
                final PlacementBehaviour type, final int offset)
        {
            super(location, processors, type);
            this.offset = offset;
        }

        @Override
        public int func_214850_d()
        {
            // This is the ground level delta.
            return this.offset;
        }

        @Override
        public boolean func_214848_a(final TemplateManager manager, final IWorld worldIn, final BlockPos pos,
                final Rotation rotation, final MutableBoundingBox box, final Random rand)
        {

            final Template template = manager.getTemplateDefaulted(this.location);
            final PlacementSettings placementsettings = this.func_214860_a(rotation, box);
            if (!template.addBlocksToWorld(worldIn, pos, placementsettings, 18)) return false;
            else
            {
                // This section is added what is modifed in, it copies the
                // structure block processing from the template structures, so
                // that we can also handle metadata on marker blocks.
                for (final Template.BlockInfo template$blockinfo : template.func_215381_a(pos, placementsettings,
                        Blocks.STRUCTURE_BLOCK))
                    if (template$blockinfo.nbt != null)
                    {
                        final StructureMode structuremode = StructureMode.valueOf(template$blockinfo.nbt.getString(
                                "mode"));
                        if (structuremode == StructureMode.DATA) this.handleDataMarker(template$blockinfo.nbt.getString(
                                "metadata"), template$blockinfo.pos, worldIn, rand, box);
                    }
                // Back to the stuff that the superclass does.
                for (final Template.BlockInfo template$blockinfo : Template.processBlockInfos(template, worldIn, pos,
                        placementsettings, this.func_214857_a(manager, pos, rotation, false)))
                    this.func_214846_a(worldIn, template$blockinfo, pos, rotation, rand, box);
                return true;
            }
        }

        protected void handleDataMarker(final String function, final BlockPos pos, final IWorld worldIn,
                final Random rand, final MutableBoundingBox sbb)
        {
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
            else MinecraftForge.EVENT_BUS.post(new StructureEvent.ReadTag(function.trim(), pos, worldIn, rand, sbb));
        }
    }

    public static class CustomJigsawPiece extends AbstractVillagePiece
    {
        public CustomJigsawPiece(final TemplateManager manager, final JigsawPiece piece, final BlockPos pos,
                final int groundLevelDelta, final Rotation dir, final MutableBoundingBox box)
        {
            super(JigsawPieces.CSP, manager, piece, pos, groundLevelDelta, dir, box);
        }

        public CustomJigsawPiece(final TemplateManager manager, final CompoundNBT tag)
        {
            super(manager, tag, JigsawPieces.CSP);
        }
    }
}
