package pokecube.core.world.gen.feature.scattered;

import java.util.Random;
import java.util.function.Function;

import com.mojang.datafixers.Dynamic;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.structure.ScatteredStructure;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.gen.feature.template.TemplateManager;

public class ConfigStructure extends ScatteredStructure<NoFeatureConfig>
{
    public ResourceLocation structLoc;
    public float            chance   = 1;
    public String           subbiome = "none";
    public BlockPos         offset   = new BlockPos(0, 0, 0);

    public ConfigStructure(final Function<Dynamic<?>, ? extends NoFeatureConfig> configFactoryIn,
            final ResourceLocation name)
    {
        super(configFactoryIn);
        this.setRegistryName(name);
    }

    public ConfigStructure(final ResourceLocation name)
    {
        this(NoFeatureConfig::deserialize, name);
    }

    public ConfigStructure setStructure(final ResourceLocation structLoc)
    {
        this.structLoc = structLoc;
        return this;
    }

    public ConfigStructure setOffset(final BlockPos offset)
    {
        this.offset = offset;
        return this;
    }

    @Override
    public IStartFactory getStartFactory()
    {
        return Start::new;
    }

    @Override
    public boolean hasStartAt(final ChunkGenerator<?> chunkGen, final Random rand, final int chunkPosX,
            final int chunkPosZ)
    {
        if (rand.nextFloat() < this.chance) return true;
        return false;
    }

    @Override
    public String getStructureName()
    {
        return this.getRegistryName().toString();
    }

    @Override
    public int getSize()
    {
        return 1;
    }

    @Override
    protected int getSeedModifier()
    {
        return 452384706;
    }

    public static class Start extends StructureStart
    {
        private ConfigStructure feature;

        public Start(final Structure<?> structure, final int chunkX, final int chunkZ, final Biome biomeIn,
                final MutableBoundingBox boundsIn, final int referenceIn, final long seed)
        {
            super(structure, chunkX, chunkZ, biomeIn, boundsIn, referenceIn, seed);
            if (structure instanceof ConfigStructure) this.feature = (ConfigStructure) structure;
        }

        @Override
        public void init(final ChunkGenerator<?> generator, final TemplateManager templateManagerIn, final int chunkX,
                final int chunkZ, final Biome biomeIn)
        {
            final int i = chunkX * 16;
            final int j = chunkZ * 16;
            final BlockPos pos = new BlockPos(i + 8, 90, j + 8);
            final Rotation rot = Rotation.values()[new Random().nextInt(Rotation.values().length)];
            final ConfigStructurePiece part = new ConfigStructurePiece(templateManagerIn, this.feature.structLoc, rot,
                    pos, this.feature.offset);
            this.components.add(part);
            this.recalculateStructureSize();
        }
    }

}
