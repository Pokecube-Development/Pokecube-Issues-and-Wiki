package pokecube.world.gen.structures.utils;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.Palette;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import pokecube.api.PokecubeAPI;
import pokecube.core.PokecubeCore;
import pokecube.core.eventhandlers.SpawnEventsHandler;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.world.gen.structures.configs.ExpandedJigsawConfiguration;
import pokecube.world.gen.structures.pool_elements.ExpandedJigsawPiece;
import thut.api.level.terrain.BiomeType;

public class PostProcessor implements
        BiConsumer<PieceGeneratorSupplier.Context<ExpandedJigsawConfiguration>, List<PoolElementStructurePiece>>
{
    public static PostProcessor POSTPROCESS = new PostProcessor();

    @SuppressWarnings("deprecation")
    @Override
    public void accept(PieceGeneratorSupplier.Context<ExpandedJigsawConfiguration> context,
            List<PoolElementStructurePiece> parts)
    {
        ChunkGenerator chunkGenerator = context.chunkGenerator();
        ChunkPos pos = context.chunkPos();
        ExpandedJigsawConfiguration config = context.config();

        for (final PoolElementStructurePiece part : parts)
        {
            int h_extra = 2;
            int v_extra = 2;
            BlockPos min_corner = new BlockPos(part.getBoundingBox().minX() - h_extra, part.getBoundingBox().minY(),
                    part.getBoundingBox().minZ() - h_extra);
            BlockPos max_corner = new BlockPos(part.getBoundingBox().maxX() + h_extra,
                    part.getBoundingBox().maxY() + v_extra, part.getBoundingBox().maxZ() + h_extra);
            part.getBoundingBox().encapsulate(min_corner);
            part.getBoundingBox().encapsulate(max_corner);

            ServerLevel level = ExpandedJigsawPacement.getForGen(context);

            if (!"none".equals(config.biome_type))
            {
                final BiomeType subbiome = BiomeType.getBiome(config.biome_type, true);
                final BoundingBox box = part.getBoundingBox();
                final Stream<BlockPos> poses = BlockPos.betweenClosedStream(box);
                SpawnEventsHandler.queueForUpdate(poses, subbiome, level);
            }
            if (part.getElement() instanceof final ExpandedJigsawPiece piece)
            {

                if (PokecubeSerializer.getInstance().hasPlacedSpawn() || !PokecubeCore.getConfig().doSpawnBuilding)
                    return;
                // Check if we should place a professor.

                final StructureTemplate t = piece.getTemplate(context.structureManager());
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
                        final int x = pos.getBlockX(7);
                        final int z = pos.getBlockZ(7);
                        final ServerLevel sworld = level;
                        final BlockPos blockpos = new BlockPos(x, chunkGenerator.getSeaLevel(), z);
                        final BlockPos spos = StructureTemplate.calculateRelativePosition(settings, localSpawn)
                                .offset(blockpos).offset(0, part.getBoundingBox().minY, 0);
                        PokecubeAPI.logInfo("Setting spawn to {} {}, professor at {}", spos, localSpawn,
                                localTrader);
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
}