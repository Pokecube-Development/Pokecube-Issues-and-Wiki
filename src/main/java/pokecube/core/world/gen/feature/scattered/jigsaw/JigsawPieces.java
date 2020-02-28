package pokecube.core.world.gen.feature.scattered.jigsaw;

import java.util.List;
import java.util.Random;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;

import net.minecraft.block.BlockState;
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
import net.minecraft.world.gen.feature.jigsaw.EmptyJigsawPiece;
import net.minecraft.world.gen.feature.jigsaw.JigsawManager;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern.PlacementBehaviour;
import net.minecraft.world.gen.feature.jigsaw.JigsawPiece;
import net.minecraft.world.gen.feature.jigsaw.SingleJigsawPiece;
import net.minecraft.world.gen.feature.structure.AbstractVillagePiece;
import net.minecraft.world.gen.feature.structure.IStructurePieceType;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.template.AlwaysTrueRuleTest;
import net.minecraft.world.gen.feature.template.BlockIgnoreStructureProcessor;
import net.minecraft.world.gen.feature.template.BlockMatchRuleTest;
import net.minecraft.world.gen.feature.template.JigsawReplacementStructureProcessor;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.RandomBlockMatchRuleTest;
import net.minecraft.world.gen.feature.template.RuleEntry;
import net.minecraft.world.gen.feature.template.RuleStructureProcessor;
import net.minecraft.world.gen.feature.template.StructureProcessor;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraftforge.common.MinecraftForge;
import pokecube.core.database.PokedexEntryLoader;
import pokecube.core.database.worldgen.WorldgenHandler.JigSawConfig;
import pokecube.core.database.worldgen.WorldgenHandler.JigSawConfig.JigSawPart;
import pokecube.core.events.StructureEvent;
import pokecube.core.world.gen.template.FillerProcessor;
import pokecube.core.world.gen.template.PokecubeStructureProcessor;
import thut.api.maths.Vector3;

public class JigsawPieces
{
    public static final IStructurePieceType CSP = CustomJigsawPiece::new;

    public static final RuleEntry PATHTOOAK    = new RuleEntry(new BlockMatchRuleTest(Blocks.GRASS_PATH),
            new BlockMatchRuleTest(Blocks.WATER), Blocks.OAK_PLANKS.getDefaultState());
    public static final RuleEntry PATHTOGRASS  = new RuleEntry(new RandomBlockMatchRuleTest(Blocks.GRASS_PATH, 0.05F),
            AlwaysTrueRuleTest.INSTANCE, Blocks.GRASS_BLOCK.getDefaultState());
    public static final RuleEntry GRASSTOWATER = new RuleEntry(new BlockMatchRuleTest(Blocks.GRASS_BLOCK),
            new BlockMatchRuleTest(Blocks.WATER), Blocks.WATER.getDefaultState());
    public static final RuleEntry DIRTTOWATER  = new RuleEntry(new BlockMatchRuleTest(Blocks.DIRT),
            new BlockMatchRuleTest(Blocks.WATER), Blocks.WATER.getDefaultState());

    public static final RuleStructureProcessor RULES = new RuleStructureProcessor(ImmutableList.of(
            JigsawPieces.PATHTOOAK, JigsawPieces.PATHTOGRASS, JigsawPieces.GRASSTOWATER, JigsawPieces.DIRTTOWATER));

    public static void initStructure(final ChunkGenerator<?> chunk_gen, final TemplateManager templateManagerIn,
            final BlockPos pos, final List<StructurePiece> parts, final SharedSeedRandom rand,
            final JigSawConfig struct)
    {
        final ResourceLocation key = new ResourceLocation(struct.root.name);
        JigsawManager.func_214889_a(key, struct.size, CustomJigsawPiece::new, chunk_gen, templateManagerIn, pos, parts,
                rand);
    }

    private static void registerPart(final JigSawConfig jigsaw, final JigSawPart part, final int offset,
            String subbiome)
    {
        final ResourceLocation key = new ResourceLocation(part.name);
        final PlacementBehaviour behaviour = part.rigid ? PlacementBehaviour.RIGID
                : PlacementBehaviour.TERRAIN_MATCHING;
        final List<Pair<JigsawPiece, Integer>> parts = Lists.newArrayList();
        for (String option : part.options)
        {
            final String[] args = option.split(";");
            boolean ignoreAir = part.ignoreAir;
            Integer second = 1;
            PlacementBehaviour place = behaviour;
            if (args.length > 1) try
            {
                final JsonObject thing = PokedexEntryLoader.gson.fromJson(args[1], JsonObject.class);
                if (thing.has("weight")) second = thing.get("weight").getAsInt();
                if (thing.has("ignoreAir")) ignoreAir = thing.get("ignoreAir").getAsBoolean();
                if (thing.has("subbiome")) subbiome = thing.get("subbiome").getAsString();
                if (thing.has("rigid")) place = thing.get("rigid").getAsBoolean() ? PlacementBehaviour.RIGID
                        : PlacementBehaviour.TERRAIN_MATCHING;
            }
            catch (final Exception e)
            {
                e.printStackTrace();
            }
            option = args[0];
            if (option.equals("empty")) parts.add(Pair.of(EmptyJigsawPiece.INSTANCE, second));
            else parts.add(Pair.of(new SingleOffsetPiece(part, option, ImmutableList.of(
                    PokecubeStructureProcessor.PROCESSOR, JigsawPieces.RULES), place, offset, ignoreAir, subbiome),
                    second));

        }

        // Register the buildings
        JigsawManager.REGISTRY.register(new JigsawPatternCustom(key, new ResourceLocation(part.target), parts,
                behaviour));
    }

    public static void registerJigsaw(final JigSawConfig jigsaw)
    {
        JigsawPieces.registerPart(jigsaw, jigsaw.root, jigsaw.offset, jigsaw.biomeType);
        for (final JigSawPart part : jigsaw.parts)
            JigsawPieces.registerPart(jigsaw, part, jigsaw.offset, jigsaw.biomeType);
    }

    public static class SingleOffsetPiece extends SingleJigsawPiece
    {
        protected final JigSawPart part;
        private final int          offset;
        private final String       subbiome;
        private final boolean      ignoreAir;

        public SingleOffsetPiece(final JigSawPart part, final String location,
                final List<StructureProcessor> processors, final PlacementBehaviour type, final int offset,
                final boolean ignoreAir, final String subbiome)
        {
            super(location, processors, type);
            this.offset = offset;
            this.ignoreAir = ignoreAir;
            this.subbiome = subbiome;
            this.part = part;
        }

        public SingleOffsetPiece(final JigSawPart part, final String location,
                final List<StructureProcessor> processors, final PlacementBehaviour type, final boolean ignoreAir,
                final String subbiome)
        {
            super(location, processors, type);
            this.offset = 0;
            this.ignoreAir = ignoreAir;
            this.subbiome = subbiome;
            this.part = part;
        }

        @Override
        public int func_214850_d()
        {
            // This is the ground level delta.
            return this.offset;
        }

        @Override
        protected PlacementSettings createPlacementSettings(final Rotation p_214860_1_,
                final MutableBoundingBox p_214860_2_)
        {
            final PlacementSettings placementsettings = new PlacementSettings();
            placementsettings.setBoundingBox(p_214860_2_);
            placementsettings.setRotation(p_214860_1_);
            placementsettings.func_215223_c(true);
            placementsettings.setIgnoreEntities(false);
            if (this.part.filler) placementsettings.addProcessor(FillerProcessor.PROCESSOR);
            if (this.ignoreAir) placementsettings.addProcessor(BlockIgnoreStructureProcessor.AIR_AND_STRUCTURE_BLOCK);
            else placementsettings.addProcessor(BlockIgnoreStructureProcessor.STRUCTURE_BLOCK);
            placementsettings.addProcessor(JigsawReplacementStructureProcessor.INSTANCE);
            this.processors.forEach(placementsettings::addProcessor);
            this.getPlacementBehaviour().getStructureProcessors().forEach(placementsettings::addProcessor);
            return placementsettings;
        }

        @Override
        public boolean place(final TemplateManager manager, final IWorld worldIn, final BlockPos pos,
                final Rotation rotation, final MutableBoundingBox box, final Random rand)
        {

            final Template template = manager.getTemplateDefaulted(this.location);
            final PlacementSettings placementsettings = this.createPlacementSettings(rotation, box);

            if (!template.addBlocksToWorld(worldIn, pos, placementsettings, 18)) return false;
            else
            {
                final StructureEvent.BuildStructure event = new StructureEvent.BuildStructure(box, worldIn,
                        this.location.toString(), placementsettings);
                event.setBiomeType(this.subbiome);
                MinecraftForge.EVENT_BUS.post(event);

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
                if (this.part.base_under)
                {
                    final MutableBoundingBox box2 = template.getMutableBoundingBox(placementsettings, pos);
                    final Vector3 v = Vector3.getNewVector();
                    for (int x = box2.minX; x <= box2.maxX; x++)
                        for (int z = box2.minZ; z <= box2.maxZ; z++)
                        {
                            v.set(x, box2.minY, z);
                            final BlockState toFill = v.getBlockState(worldIn);
                            for (int y = box2.minY - 1; y > box2.minY - 32; y--)
                            {
                                v.set(x, y, z);
                                if (!box.isVecInside(v.getPos())) continue;
                                final BlockState check = v.getBlockState(worldIn);
                                if (check.isAir(worldIn, v.getPos())) worldIn.setBlockState(v.getPos(), toFill, 2);
                                else break;
                            }
                        }
                }
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
