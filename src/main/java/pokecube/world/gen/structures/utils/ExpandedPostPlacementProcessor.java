package pokecube.world.gen.structures.utils;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.PostPlacementProcessor;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.Palette;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import pokecube.core.PokecubeCore;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.world.gen.structures.pool_elements.ExpandedJigsawPiece;

public class ExpandedPostPlacementProcessor implements PostPlacementProcessor
{
    public static final PostPlacementProcessor INSTANCE = new ExpandedPostPlacementProcessor();

    @Override
    public void afterPlace(WorldGenLevel level, StructureFeatureManager manager, ChunkGenerator chunkGenerator,
            Random random, BoundingBox box, ChunkPos pos, PiecesContainer pieces)
    {

        if (PokecubeSerializer.getInstance().hasPlacedSpawn() || !PokecubeCore.getConfig().doSpawnBuilding) return;

        final int x = pos.getBlockX(7);
        final int z = pos.getBlockZ(7);
        final BlockPos blockpos = new BlockPos(x, chunkGenerator.getSeaLevel(), z);

        for (final StructurePiece part : pieces.pieces()) if (part instanceof final PoolElementStructurePiece element
                && element.getElement() instanceof ExpandedJigsawPiece piece)
        {
            // Check if we should place a professor.

            final StructureTemplate t = piece.getTemplate(level.getLevel().getStructureManager());
            StructurePlaceSettings settings = piece.getSettings(part.getRotation(), part.getBoundingBox(), false);
            components:
            for (final Palette list : t.palettes)
            {
                boolean foundWorldspawn = false;
                BlockPos localSpawn = null;
                BlockPos localTrader = null;
                for (final StructureBlockInfo i : list.blocks())
                    if (i != null && i.nbt != null && i.state.getBlock() == Blocks.STRUCTURE_BLOCK)
                {
                    final StructureMode structuremode = StructureMode.valueOf(i.nbt.getString("mode"));
                    if (structuremode == StructureMode.DATA)
                    {
                        final String meta = i.nbt.getString("metadata");
                        foundWorldspawn = foundWorldspawn || meta.startsWith("pokecube:worldspawn");
                        if (localSpawn == null && foundWorldspawn) localSpawn = i.pos;
                        if (meta.contains("pokecube:mob:trader") || meta.contains("pokecube:mob:pokemart_merchant"))
                            localTrader = i.pos;
                    }
                }
                if (localTrader != null && localSpawn != null)
                {
                    final ServerLevel sworld = ExpandedJigsawPacement.getForGen(chunkGenerator);
                    final BlockPos spos = StructureTemplate.calculateRelativePosition(settings, localSpawn)
                            .offset(blockpos).offset(0, part.getBoundingBox().minY, 0);
                    PokecubeCore.LOGGER.info("Setting spawn to {} {}, professor at {}", spos, localSpawn, localTrader);
                    PokecubeSerializer.getInstance().setPlacedSpawn();
                    sworld.getServer().execute(() -> {
                        sworld.setDefaultSpawnPos(spos, 0);
                    });
                    piece.placedSpawn = false;
                    piece.isSpawn = true;
                    piece.spawnPos = localSpawn;
                    piece.profPos = localTrader;
                    break components;
                }
            }
        }
    }

}
