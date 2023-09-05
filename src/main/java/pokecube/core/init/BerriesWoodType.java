package pokecube.core.init;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.client.renderer.Sheets;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import pokecube.core.PokecubeCore;

public class BerriesWoodType
{
    private static Map<String, WoodType> TYPES = Maps.newHashMap();

    public static WoodType addWoodTypes(String name)
    {
        // TODO: Check this
        WoodType type = WoodType.register(new WoodType(new ResourceLocation(PokecubeCore.MODID, name).toString(),
                new BlockSetType(new ResourceLocation(PokecubeCore.MODID, name).toString())));
        TYPES.put(name, type);
        return type;
    }

    public static WoodType getWoodType(String name)
    {
        return TYPES.get(name);
    }

    public static void register()
    {
        TYPES.values().forEach(Sheets::addWoodType);
    }
}
