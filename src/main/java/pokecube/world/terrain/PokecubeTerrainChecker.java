package pokecube.world.terrain;

import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.TerrainShaper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.material.Material;
import pokecube.api.data.spawns.SpawnCheck.TerrainType;
import pokecube.core.PokecubeCore;
import pokecube.world.gen.structures.configs.ExpandedJigsawConfiguration;
import thut.api.level.structures.NamedVolumes.INamedStructure;
import thut.api.level.structures.StructureManager;
import thut.api.level.terrain.BiomeType;
import thut.api.level.terrain.TerrainChecker;
import thut.api.level.terrain.TerrainSegment;
import thut.api.level.terrain.TerrainSegment.ISubBiomeChecker;
import thut.api.maths.Vector3;
import thut.core.common.handlers.ConfigHandler;

public class PokecubeTerrainChecker extends TerrainChecker implements ISubBiomeChecker
{
    public static void init()
    {
        final PokecubeTerrainChecker checker = new PokecubeTerrainChecker();
        TerrainSegment.defaultChecker = checker;
    }

    public static TerrainType getTerrain(Vector3 v, LevelAccessor world)
    {
        if (!(world instanceof ServerLevel level)) return TerrainType.FLAT;

        ChunkGenerator generator = level.getChunkSource().getGenerator();
        BlockPos pos = v.getPos();

        int i = QuartPos.fromBlock(pos.getX());
        int j = QuartPos.fromBlock(pos.getY());
        int k = QuartPos.fromBlock(pos.getZ());
        Climate.TargetPoint climate$targetpoint = generator.climateSampler().sample(i, j, k);
        float f4 = Climate.unquantizeCoord(climate$targetpoint.weirdness());
        double d0 = TerrainShaper.peaksAndValleys(f4);
        return d0 > 0.5 ? TerrainType.HILLS : TerrainType.FLAT;
    }

    @Override
    public BiomeType getSubBiome(final LevelAccessor world, final Vector3 v, final TerrainSegment segment,
            final boolean caveAdjusted)
    {
        if (!(world instanceof ServerLevel rworld)) return BiomeType.NONE;
        if (caveAdjusted)
        {
            final Set<INamedStructure> set = StructureManager.getFor(rworld.dimension(), v.getPos(), true);
            for (var info : set)
            {
                String name = info.getName();
                if (!name.contains(":")) name = "minecraft:" + name;

                String subbiome = null;
                var obj = info.getWrapped();
                // first manually check structures to see if they define a
                // subbiome internally, if so, set to that first.
                if (obj instanceof ConfiguredStructureFeature<?, ?> feature)
                {
                    var registry = world.registryAccess()
                            .registryOrThrow(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY);
                    var opt_holder = registry.getHolder(registry.getId(feature));
                    opt_check:
                    if (!opt_holder.isEmpty())
                    {
                        var holder = opt_holder.get();
                        if (holder.value().config instanceof ExpandedJigsawConfiguration config)
                        {
                            if (!config.biome_type.equals("none"))
                            {
                                subbiome = config.biome_type;
                                break opt_check;
                            }
                        }
                    }
                }
                // Now we check the tags.
                var list = ConfigHandler.STRUCTURE_SUBBIOMES.getValues(TerrainChecker.tagKey);
                for (var value : list)
                {
                    var key = value.name;
                    if (info.is(key))
                    {
                        subbiome = value.getValue();
                        break;
                    }
                }
                if (subbiome == null && obj != null && PokecubeCore.getConfig().structs_default_ruins)
                    subbiome = "ruin";
                if (subbiome != null)
                {
                    final BiomeType biom = BiomeType.getBiome(subbiome, true);
                    return biom;
                }
            }
            if (rworld.dimensionType().hasCeiling() || v.canSeeSky(world)
                    || !PokecubeCore.getConfig().autoDetectSubbiomes)
                return BiomeType.NONE;
            boolean sky = false;
            final Vector3 temp1 = new Vector3();
            final int x0 = segment.chunkX * 16, y0 = segment.chunkY * 16, z0 = segment.chunkZ * 16;
            final int dx = (v.intX() - x0) / TerrainSegment.GRIDSIZE * TerrainSegment.GRIDSIZE;
            final int dy = (v.intY() - y0) / TerrainSegment.GRIDSIZE * TerrainSegment.GRIDSIZE;
            final int dz = (v.intZ() - z0) / TerrainSegment.GRIDSIZE * TerrainSegment.GRIDSIZE;
            final int x1 = x0 + dx, y1 = y0 + dy, z1 = z0 + dz;
            int industrial = 0;
            int water = 0;
            outer:
            for (int x = x1; x < x1 + TerrainSegment.GRIDSIZE; x++)
                for (int y = y1; y < y1 + TerrainSegment.GRIDSIZE; y++)
                    for (int z = z1; z < z1 + TerrainSegment.GRIDSIZE; z++)
            {
                temp1.set(x, y, z);
                if (segment.isInTerrainSegment(temp1.x, temp1.y, temp1.z))
                {
                    final double y2 = temp1.getMaxY(world);
                    sky = y2 <= temp1.y;
                }
                BlockState state;
                if (PokecubeTerrainChecker.isIndustrial(state = temp1.getBlockState(world))) industrial++;
                if (industrial > 2) return BiomeType.INDUSTRIAL;
                if (state.getMaterial() == Material.WATER) water++;
                if (sky) break outer;
            }
            if (sky) return BiomeType.NONE;
            if (water > 4) return BiomeType.CAVE_WATER;
            else if (this.isCave(v, world)) return BiomeType.CAVE;
            return PokecubeTerrainChecker.INSIDE;
        }
        BiomeType biome = BiomeType.NONE;
        final Holder<Biome> b = v.getBiomeHolder(world);
        if (!PokecubeCore.getConfig().autoDetectSubbiomes) return biome;
        final boolean notLake = this.isWatery(b);
        int industrial = 0;
        int water = 0;
        int flower = 0;
        final Vector3 temp1 = new Vector3();
        final int x0 = segment.chunkX * 16, y0 = segment.chunkY * 16, z0 = segment.chunkZ * 16;
        final int dx = (v.intX() - x0) / TerrainSegment.GRIDSIZE * TerrainSegment.GRIDSIZE;
        final int dy = (v.intY() - y0) / TerrainSegment.GRIDSIZE * TerrainSegment.GRIDSIZE;
        final int dz = (v.intZ() - z0) / TerrainSegment.GRIDSIZE * TerrainSegment.GRIDSIZE;
        final int x1 = x0 + dx, y1 = y0 + dy, z1 = z0 + dz;
        for (int i = x1; i < x1 + TerrainSegment.GRIDSIZE; i++) for (int j = y1; j < y1 + TerrainSegment.GRIDSIZE; j++)
            for (int k = z1; k < z1 + TerrainSegment.GRIDSIZE; k++)
        {
            BlockState state = temp1.set(i, j, k).getBlockState(world);
            if (state.isAir()) continue;
            boolean isIndustrial = PokecubeTerrainChecker.isIndustrial(state);
            boolean isFlower = PokecubeTerrainChecker.isFlower(state);
            if (isIndustrial) industrial++;
            if (isFlower) flower++;
            if (industrial > 2) return BiomeType.INDUSTRIAL;
            if (flower > 3) return BiomeType.FLOWER;
            if (state.getMaterial() == Material.WATER) water++;
        }
        if (water > 4)
        {
            if (!notLake) biome = BiomeType.LAKE;
            return biome;
        }
        // Check nearby villages, and if in one, define as village type.
        if (world instanceof ServerLevel level)
        {
            final BlockPos pos = v.getPos();
            if (level.isVillage(pos)) biome = BiomeType.VILLAGE;
        }
        boolean sky = v.canSeeSky(world);
        // lastly check for sky, this goes after village checks so you can have
        // sky villages still be villages.
        if (sky)
        {
            int skyH = 16;
            sky = v.y >= world.getHeight(Types.WORLD_SURFACE_WG, v.intX(), v.intZ()) + skyH;
            if (sky) return BiomeType.SKY;
        }
        return biome;
    }

    public boolean isCave(final Vector3 v, final LevelAccessor world)
    {
        return this.isCaveFloor(v, world) && this.isCaveCeiling(v, world);
    }

    public boolean isCaveCeiling(final Vector3 v, final LevelAccessor world)
    {
        final double y = v.getMaxY(world);
        if (y <= v.y) return false;
        BlockState state = v.getBlockState(world);
        if (state.getMaterial().isSolid()) return PokecubeTerrainChecker.isCave(state);
        final Vector3 up = Vector3.getNextSurfacePoint(world, v, Vector3.secondAxis, y - v.y);
        if (up == null) return false;
        state = up.getBlockState(world);
        return PokecubeTerrainChecker.isCave(state);
    }

    public boolean isCaveFloor(final Vector3 v, final LevelAccessor world)
    {
        final BlockState state = v.getBlockState(world);
        if (state.getMaterial().isSolid()) return PokecubeTerrainChecker.isCave(state);
        final Vector3 down = Vector3.getNextSurfacePoint(world, v, Vector3.secondAxisNeg,
                v.y - world.getMinBuildHeight());
        if (down == null) return false;
        return PokecubeTerrainChecker.isCave(down.getBlockState(world));
    }
}
