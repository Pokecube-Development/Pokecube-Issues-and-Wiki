package thut.api.terrain;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.BiomeDictionary.Type;
import thut.api.maths.Vector3;

public class TerrainSegment
{

    public static class DefaultChecker implements ISubBiomeChecker
    {
        @Override
        public int getSubBiome(World world, Vector3 v, TerrainSegment segment, Chunk chunk, boolean caveAdjusted)
        {
            if (caveAdjusted)
            {
                // Do not return this for cave worlds
                if (!world.dimension.isSurfaceWorld()) return -1;
                boolean sky = false;
                final Vector3 temp1 = Vector3.getNewVector();
                final int x0 = segment.chunkX * 16, y0 = segment.chunkY * 16, z0 = segment.chunkZ * 16;
                final int dx = (v.intX() - x0) / TerrainSegment.GRIDSIZE * TerrainSegment.GRIDSIZE;
                final int dy = (v.intY() - y0) / TerrainSegment.GRIDSIZE * TerrainSegment.GRIDSIZE;
                final int dz = (v.intZ() - z0) / TerrainSegment.GRIDSIZE * TerrainSegment.GRIDSIZE;
                final int x1 = x0 + dx, y1 = y0 + dy, z1 = z0 + dz;

                // Check if this location can see the sky.
                outer:
                for (int i = x1; i < x1 + TerrainSegment.GRIDSIZE; i++)
                    for (int j = y1; j < y1 + TerrainSegment.GRIDSIZE; j++)
                        for (int k = z1; k < z1 + TerrainSegment.GRIDSIZE; k++)
                        {
                            temp1.set(i, j, k);
                            if (segment.isInTerrainSegment(temp1.x, temp1.y, temp1.z))
                            {
                                final double y = temp1.getMaxY(world);
                                sky = y <= temp1.y;
                            }
                            if (sky) break outer;
                        }
                if (sky) return -1;

                // If not can see sky, if there is water, it is cave_water,
                // otherwise it is cave.
                if (!sky && TerrainSegment.count(world, Blocks.WATER, v, 1) > 2) return BiomeType.CAVE_WATER.getType();
                else if (!sky) return BiomeType.CAVE.getType();
            }
            else
            {
                int biome = -1;

                final Biome b = v.getBiome(world);

                // Do not define lakes on watery biomes.
                final boolean notLake = BiomeDatabase.contains(b, Type.OCEAN) || BiomeDatabase.contains(b, Type.SWAMP)
                        || BiomeDatabase.contains(b, Type.RIVER) || BiomeDatabase.contains(b, Type.WATER)
                        || BiomeDatabase.contains(b, Type.BEACH);
                if (!notLake)
                {
                    // If it isn't a water biome, define it as a lake if more
                    // than a certain amount of water.
                    final int water = v.blockCount2(world, Blocks.WATER, 3);
                    if (water > 4)
                    {
                        biome = BiomeType.LAKE.getType();
                        return biome;
                    }
                }
                // Check nearby villages, and if in one, define as village type.
                if (world instanceof ServerWorld)
                {
                    final BlockPos pos = v.getPos();
                    final ServerWorld server = (ServerWorld) world;
                    if (server.func_217483_b_(pos)) biome = BiomeType.VILLAGE.getType();
                }

                return biome;
            }
            return -1;
        }
    }

    public static interface ISubBiomeChecker
    {
        /**
         * This should return -1 if it is not a relevant biome for this biome
         * checker.
         *
         * @param world
         * @param v
         * @param segment
         * @param chunk
         * @param caveAdjusted
         * @return
         */
        int getSubBiome(World world, Vector3 v, TerrainSegment segment, Chunk chunk, boolean caveAdjusted);
    }

    public static interface ITerrainEffect
    {
        /**
         * Called when the terrain effect is assigned to the terrain segment
         *
         * @param x
         *            chunkX of terrainsegment
         * @param y
         *            chunkY of terrainsegment
         * @param z
         *            chunkZ of terrainsegement
         */
        void bindToTerrain(int x, int y, int z);

        void doEffect(LivingEntity entity, boolean firstEntry);

        String getIdenitifer();

        // Does not currently work TODO make this work
        void readFromNBT(CompoundNBT nbt);

        // Does not currently work TODO make this work
        void writeToNBT(CompoundNBT nbt);
    }

    public static final int                            GRIDSIZE             = 4;
    public static ISubBiomeChecker                     defaultChecker       = new DefaultChecker();
    public static List<ISubBiomeChecker>               biomeCheckers        = Lists.newArrayList();
    public static Set<Class<? extends ITerrainEffect>> terrainEffectClasses = Sets.newHashSet();
    public static boolean                              noLoad               = false;

    public static int count(World world, Block b, Vector3 v, int range)
    {
        final Vector3 temp = Vector3.getNewVector();
        temp.set(v);
        int ret = 0;
        for (int i = -range; i <= range; i++)
            for (int j = -range; j <= range; j++)
                for (int k = -range; k <= range; k++)
                {

                    boolean bool = true;
                    final int i1 = MathHelper.floor(v.intX() + i) >> 4;
                    final int k1 = MathHelper.floor(v.intZ() + i) >> 4;

                    bool = i1 == v.intX() >> 4 && k1 == v.intZ() >> 4;

                    if (bool)
                    {
                        temp.set(v).addTo(i, j, k);
                        final BlockState state = world.getBlockState(temp.getPos());
                        if (state.getBlock() == b || b == null && state.getBlock() == null) ret++;
                    }
                }
        return ret;
    }

    public static boolean isInTerrainColumn(Vector3 t, Vector3 point)
    {
        boolean ret = true;
        final int i = MathHelper.floor(point.intX() / 16.0D);
        final int k = MathHelper.floor(point.intZ() / 16.0D);

        ret = i == t.intX() && k == t.intZ();
        return ret;
    }

    public static void readFromNBT(TerrainSegment t, CompoundNBT nbt)
    {
        if (TerrainSegment.noLoad) return;
        final int[] biomes = nbt.getIntArray("biomes");
        t.toSave = nbt.getBoolean("toSave");
        t.init = false;
        boolean replacements = false;
        if (t.idReplacements != null) for (int i = 0; i < biomes.length; i++)
            if (t.idReplacements.containsKey(biomes[i]))
            {
                biomes[i] = t.idReplacements.get(biomes[i]);
                replacements = true;
            }
        if (replacements) System.out.println("Replacement subbiomes found for " + t.chunkX + " " + t.chunkY + " "
                + t.chunkZ);
        t.setBiome(biomes);
    }

    public Map<Integer, Integer> idReplacements;

    public final int chunkX;

    public final int chunkY;

    public final int chunkZ;

    public final BlockPos pos;

    public Chunk chunk;

    public boolean toSave = false;

    public boolean isSky = false;

    public boolean init = true;

    Vector3 temp = Vector3.getNewVector();

    Vector3 temp1 = Vector3.getNewVector();

    Vector3 mid = Vector3.getNewVector();

    int[] biomes = new int[TerrainSegment.GRIDSIZE * TerrainSegment.GRIDSIZE * TerrainSegment.GRIDSIZE];

    HashMap<String, ITerrainEffect> effects = new HashMap<>();
    public final ITerrainEffect[]   effectArr;

    public TerrainSegment(int x, int y, int z)
    {
        this.chunkX = x;
        this.chunkY = y;
        this.chunkZ = z;
        this.pos = new BlockPos(x, y, z);
        this.mid.set(this.chunkX * 16 + 8, this.chunkY * 16 + 8, this.chunkZ * 16 + 8);
        for (final Class<? extends ITerrainEffect> clas : TerrainSegment.terrainEffectClasses)
            try
            {
                final ITerrainEffect effect = clas.newInstance();
                this.addEffect(effect, effect.getIdenitifer());
            }
            catch (InstantiationException | IllegalAccessException e)
            {
                e.printStackTrace();
            }
        final List<ITerrainEffect> toSort = Lists.newArrayList(this.effects.values());
        toSort.sort((o1, o2) -> o1.getIdenitifer().compareTo(o2.getIdenitifer()));
        this.effectArr = toSort.toArray(new ITerrainEffect[0]);
    }

    private void addEffect(ITerrainEffect effect, String name)
    {
        effect.bindToTerrain(this.chunkX, this.chunkY, this.chunkZ);
        this.effects.put(name, effect);
    }

    public int adjustedCaveBiome(World world, Vector3 v)
    {
        return this.getBiome(world, v, true);
    }

    public int adjustedNonCaveBiome(World world, Vector3 v)
    {
        return this.getBiome(world, v, false);
    }

    void checkToSave()
    {
        final int subCount = this.biomes.length;
        for (int i = 0; i < subCount; i++)
        {
            final int temp1 = this.biomes[i];
            if (temp1 > 255 && temp1 != BiomeType.SKY.getType())
            {
                this.toSave = true;
                return;
            }
        }
        this.toSave = false;
    }

    @Override
    public boolean equals(Object o)
    {
        boolean ret = false;
        if (o instanceof TerrainSegment) ret = ((TerrainSegment) o).chunkX == this.chunkX
                && ((TerrainSegment) o).chunkY == this.chunkY && ((TerrainSegment) o).chunkZ == this.chunkZ;

        return ret;
    }

    public double getAverageSlope(World world, Vector3 point, int range)
    {
        double slope = 0;

        final double prevY = point.getMaxY(world);

        double dy = 0;
        double dz = 0;
        this.temp1.set(this.temp);
        this.temp.set(point);
        int count = 0;
        for (int i = -range; i <= range; i++)
        {
            dz = 0;
            for (int j = -range; j <= range; j++)
            {
                if (TerrainSegment.isInTerrainColumn(point, this.temp.addTo(i, 0, j))) dy += Math.abs(point.getMaxY(
                        world, point.intX() + i, point.intZ() + j) - prevY);
                dz++;
                count++;
                this.temp.set(point);
            }
            slope += dy / dz;
        }
        this.temp.set(this.temp1);

        return slope / count;
    }

    public int getBiome(int x, int y, int z)
    {
        int ret = 0;
        final int relX = (x & 15) / TerrainSegment.GRIDSIZE, relY = (y & 15) / TerrainSegment.GRIDSIZE, relZ = (z & 15)
                / TerrainSegment.GRIDSIZE;

        if (relX < 4 && relY < 4 && relZ < 4) ret = this.biomes[relX + TerrainSegment.GRIDSIZE * relY
                + TerrainSegment.GRIDSIZE * TerrainSegment.GRIDSIZE * relZ];
        if (ret > 255) this.toSave = true;

        return ret;
    }

    public int getBiome(Vector3 v)
    {
        return this.getBiome(v.intX(), v.intY(), v.intZ());
    }

    private int getBiome(World world, Vector3 v, boolean caveAdjust)
    {
        if (this.chunk == null || this.chunk.getPos().x != this.chunkX || this.chunk.getPos().z != this.chunkZ)
            this.chunk = world.getChunk(this.chunkX, this.chunkZ);
        if (this.chunk == null)
        {
            Thread.dumpStack();
            return -1;
        }
        if (!TerrainSegment.biomeCheckers.isEmpty()) for (final ISubBiomeChecker checker : TerrainSegment.biomeCheckers)
        {
            final int biome = checker.getSubBiome(world, v, this, this.chunk, caveAdjust);
            if (biome != -1) return biome;
        }
        return TerrainSegment.defaultChecker.getSubBiome(world, v, this, this.chunk, caveAdjust);
    }

    public int getBiomeLocal(int x, int y, int z)
    {
        final int relX = x % TerrainSegment.GRIDSIZE;
        final int relY = y % TerrainSegment.GRIDSIZE;
        final int relZ = z % TerrainSegment.GRIDSIZE;
        return this.biomes[relX + TerrainSegment.GRIDSIZE * relY + TerrainSegment.GRIDSIZE * TerrainSegment.GRIDSIZE
                * relZ];
    }

    public Vector3 getCentre()
    {
        return this.mid;
    }

    public BlockPos getChunkCoords()
    {
        return this.pos;
    }

    public Collection<ITerrainEffect> getEffects()
    {
        return this.effects.values();
    }

    public ITerrainEffect geTerrainEffect(String name)
    {
        return this.effects.get(name);
    }

    @Override
    public int hashCode()
    {
        return this.chunkX + this.chunkZ << 8 << 8 + this.chunkY;
    }

    public void initBiomes(World world)
    {
        if (this.init)
        {
            this.refresh(world);
            this.init = false;
        }
    }

    public boolean isInTerrainSegment(double x, double y, double z)
    {
        boolean ret = true;
        final int i = MathHelper.floor(x / 16.0D);
        final int j = MathHelper.floor(y / 16.0D);
        final int k = MathHelper.floor(z / 16.0D);

        ret = i == this.chunkX && k == this.chunkZ && j == this.chunkY;
        return ret;
    }

    public void refresh(World world)
    {
        final long time = System.nanoTime();
        if (this.chunk == null) this.chunk = world.getChunk(this.chunkX, this.chunkZ);
        for (int i = 0; i < TerrainSegment.GRIDSIZE; i++)
            for (int j = 0; j < TerrainSegment.GRIDSIZE; j++)
                for (int k = 0; k < TerrainSegment.GRIDSIZE; k++)
                {
                    this.temp.set(this.chunkX * 16 + i * 16 / TerrainSegment.GRIDSIZE, this.chunkY * 16 + j * 16
                            / TerrainSegment.GRIDSIZE, this.chunkZ * 16 + k * 16 / TerrainSegment.GRIDSIZE);
                    int biome = this.adjustedCaveBiome(world, this.temp);
                    final int biome2 = this.adjustedNonCaveBiome(world, this.temp);
                    if (biome != -1 || biome2 != -1) this.toSave = true;
                    if (biome == -1) biome = biome2;
                    this.biomes[i + TerrainSegment.GRIDSIZE * j + TerrainSegment.GRIDSIZE * TerrainSegment.GRIDSIZE
                            * k] = biome;
                }
        final double dt = (System.nanoTime() - time) / 10e9;
        if (dt > 0.01) System.out.println("subBiome refresh took " + dt);
    }

    public void saveToNBT(CompoundNBT nbt)
    {
        if (!this.toSave) return;
        nbt.putIntArray("biomes", this.biomes);
        nbt.putInt("x", this.chunkX);
        nbt.putInt("y", this.chunkY);
        nbt.putInt("z", this.chunkZ);
        nbt.putBoolean("toSave", this.toSave);
    }

    public void setBiome(BlockPos p, int type)
    {
        this.setBiome(p.getX(), p.getY(), p.getZ(), type);
    }

    public void setBiome(int x, int y, int z, int biome)
    {
        final int relX = (x & 15) / TerrainSegment.GRIDSIZE, relY = (y & 15) / TerrainSegment.GRIDSIZE, relZ = (z & 15)
                / TerrainSegment.GRIDSIZE;
        this.biomes[relX + TerrainSegment.GRIDSIZE * relY + TerrainSegment.GRIDSIZE * TerrainSegment.GRIDSIZE
                * relZ] = biome;
        if (biome > 255) this.toSave = true;

    }

    public void setBiome(int[] biomes)
    {
        if (biomes.length == this.biomes.length) this.biomes = biomes;
        else for (int i = 0; i < biomes.length; i++)
        {
            if (i >= this.biomes.length) return;
            this.biomes[i] = biomes[i];
        }
    }

    public void setBiome(Vector3 v, BiomeType type)
    {
        this.setBiome(v, type.getType());
    }

    public void setBiome(Vector3 v, int i)
    {
        this.setBiome(v.intX(), v.intY(), v.intZ(), i);
    }

    public void setBiomeLocal(int x, int y, int z, int biome)
    {
        final int relX = x % TerrainSegment.GRIDSIZE;
        final int relY = y % TerrainSegment.GRIDSIZE;
        final int relZ = z % TerrainSegment.GRIDSIZE;
        this.biomes[relX + TerrainSegment.GRIDSIZE * relY + TerrainSegment.GRIDSIZE * TerrainSegment.GRIDSIZE
                * relZ] = biome;
    }

    @Override
    public String toString()
    {
        String ret = "Terrian Segment " + this.chunkX + "," + this.chunkY + "," + this.chunkZ + " Centre:" + this
                .getCentre();
        final String eol = System.getProperty("line.separator");
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
            {
                String line = "[";
                for (int k = 0; k < 4; k++)
                {
                    line = line + this.biomes[i + TerrainSegment.GRIDSIZE * j + TerrainSegment.GRIDSIZE
                            * TerrainSegment.GRIDSIZE * k];
                    if (k != 3) line = line + ", ";
                }
                line = line + "]";
                ret = ret + eol + line;
            }

        return ret;
    }
}
