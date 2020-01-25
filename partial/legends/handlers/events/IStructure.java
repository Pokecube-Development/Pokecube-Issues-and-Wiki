package pokecube.legends.handlers.events;

import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraftforge.fml.common.FMLCommonHandler;

public interface IStructure
{
    public static final PlacementSettings settings1 = new PlacementSettings().setChunk(null).setIgnoreEntities(false)
            .setIgnoreStructureBlock(false).setMirror(Mirror.NONE).setRotation(Rotation.CLOCKWISE_90);
    public static final PlacementSettings settings2 = new PlacementSettings().setChunk(null).setIgnoreEntities(false)
            .setIgnoreStructureBlock(false).setMirror(Mirror.NONE).setRotation(Rotation.CLOCKWISE_180);
    public static final PlacementSettings settings3 = new PlacementSettings().setChunk(null).setIgnoreEntities(false)
            .setIgnoreStructureBlock(false).setMirror(Mirror.NONE).setRotation(Rotation.COUNTERCLOCKWISE_90);

    public static final WorldServer       worldServer = FMLCommonHandler.instance().getMinecraftServerInstance()
            .getWorld(0);
    public static final PlacementSettings settings    = new PlacementSettings().setChunk(null).setIgnoreEntities(false)
            .setIgnoreStructureBlock(false).setMirror(Mirror.NONE).setRotation(Rotation.NONE);
}