package pokecube.core.world.gen.jigsaw;

import java.util.Random;

import com.mojang.serialization.Codec;

import net.minecraft.block.Blocks;
import net.minecraft.state.properties.StructureMode;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationStage.Decoration;
import net.minecraft.world.gen.feature.structure.AbstractVillagePiece;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;
import net.minecraft.world.gen.feature.template.Template.Palette;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraft.world.server.ServerWorld;
import pokecube.core.PokecubeCore;
import pokecube.core.utils.PokecubeSerializer;

public class CustomJigsawStructure extends Structure<JigsawConfig>
{
    public CustomJigsawStructure(final Codec<JigsawConfig> p_i231977_1_)
    {
        super(p_i231977_1_);
    }

    @Override
    public IStartFactory<JigsawConfig> getStartFactory()
    {
        return Start::new;
    }

    @Override
    public Decoration getDecorationStage()
    {
        if (super.getDecorationStage() == null) return Decoration.SURFACE_STRUCTURES;
        else return super.getDecorationStage();
    }

    /**
     * Handles calling up the structure's pieces class and height that structure
     * will spawn at.
     */
    public static class Start extends StructureStart<JigsawConfig>
    {
        public Start(final Structure<JigsawConfig> structureIn, final int chunkX, final int chunkZ,
                final MutableBoundingBox mutableBoundingBox, final int referenceIn, final long seedIn)
        {
            super(structureIn, chunkX, chunkZ, mutableBoundingBox, referenceIn, seedIn);
        }

        @Override
        public void func_230364_a_(final DynamicRegistries dynamicRegistryManager, final ChunkGenerator chunkGenerator,
                final TemplateManager templateManagerIn, final int chunkX, final int chunkZ, final Biome biomeIn,
                final JigsawConfig config)
        {

            // Turns the chunk coordinates into actual coordinates we can use.
            // (Gets center of that chunk)
            final int x = (chunkX << 4) + 7;
            final int z = (chunkZ << 4) + 7;
            final BlockPos blockpos = new BlockPos(x, 0, z);

            final JigsawAssmbler assembler = new JigsawAssmbler(config.struct_config);
            boolean built = assembler.build(dynamicRegistryManager, new ResourceLocation(config.struct_config.root),
                    config.struct_config.size, AbstractVillagePiece::new, chunkGenerator, templateManagerIn, blockpos,
                    this.components, this.rand, biomeIn, c->true);

            int n = 0;
            while (!built && n++ < 20)
            {
                this.components.clear();
                final Random newRand = new Random(this.rand.nextLong());
                built = assembler.build(dynamicRegistryManager, new ResourceLocation(config.struct_config.root),
                        config.struct_config.size, AbstractVillagePiece::new, chunkGenerator, templateManagerIn,
                        blockpos, this.components, newRand, biomeIn, c->true);
                PokecubeCore.LOGGER.warn("Try {}, {} parts.", n, this.components.size());
            }
            if (!built) PokecubeCore.LOGGER.warn("Failed to complete a structure at " + blockpos);

            // Check if any components are valid spawn spots, if so, set the
            // spawned flag

            for (final StructurePiece part : this.components)
                if (part instanceof AbstractVillagePiece)
                {
                    final AbstractVillagePiece p = (AbstractVillagePiece) part;
                    if (p.getJigsawPiece() instanceof CustomJigsawPiece)
                    {
                        final CustomJigsawPiece piece = (CustomJigsawPiece) p.getJigsawPiece();
                        // Check if the part needs a shift.
                        p.offset(0, -piece.opts.dy, 0);

                        // Check if we should place a professor.
                        if (!PokecubeSerializer.getInstance().hasPlacedSpawn())
                        {
                            final Template t = piece.getTemplate(templateManagerIn);
                            if (piece.toUse == null) piece.func_230379_a_(part.getRotation(), part.getBoundingBox(),
                                    false);
                            components:
                            for (final Palette list : t.blocks)
                            {
                                boolean foundWorldspawn = false;
                                String tradeString = "";
                                BlockPos pos = null;
                                for (final BlockInfo i : list.func_237157_a_())
                                    if (i != null && i.nbt != null && i.state.getBlock() == Blocks.STRUCTURE_BLOCK)
                                    {
                                        final StructureMode structuremode = StructureMode.valueOf(i.nbt.getString(
                                                "mode"));
                                        if (structuremode == StructureMode.DATA)
                                        {
                                            final String meta = i.nbt.getString("metadata");
                                            foundWorldspawn = foundWorldspawn || meta.startsWith("pokecube:worldspawn");
                                            if (pos == null && foundWorldspawn) pos = i.pos;
                                            if (meta.startsWith("pokecube:mob:trader")) tradeString = meta;
                                        }
                                    }
                                if (!tradeString.isEmpty() && foundWorldspawn)
                                {
                                    final ServerWorld sworld = JigsawAssmbler.getForGen(chunkGenerator);
                                    final BlockPos spos = Template.transformedBlockPos(piece.toUse, pos).add(blockpos)
                                            .add(0, part.getBoundingBox().minY, 0);
                                    PokecubeCore.LOGGER.info("Setting spawn to {} {}", spos, pos);
                                    sworld.getServer().execute(() ->
                                    {
                                        sworld.func_241124_a__(spos, 0);
                                    });
                                    PokecubeSerializer.getInstance().setPlacedSpawn();
                                    piece.isSpawn = true;
                                    piece.spawnReplace = tradeString;
                                    piece.mask = new MutableBoundingBox(part.getBoundingBox());
                                    break components;
                                }
                            }
                        }
                    }
                }
            // Sets the bounds of the structure once you are finished.
            this.recalculateStructureSize();

            // I use to debug and quickly find out if the structure is spawning
            // or not and where it is.
            PokecubeCore.LOGGER.info(config.struct_config.name + " at " + blockpos.getX() + " " + blockpos.getY() + " "
                    + blockpos.getZ() + " of size " + this.components.size());
        }

    }
}
