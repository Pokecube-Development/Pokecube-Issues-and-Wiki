package pokecube.legends.worldgen.dimension;

import net.minecraft.client.world.DimensionRenderInfo;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class UltraSpaceRenderInfo extends DimensionRenderInfo {

    public UltraSpaceRenderInfo() {
        super(Float.NaN, false, FogType.END, false, true);
    }

    @Override
    public Vector3d func_230494_a_(Vector3d vector3d, float sun) {
        return vector3d;
    }

    //thick fog
    @Override
    public boolean func_230493_a_(int x, int y) {
        return false;
    }
}