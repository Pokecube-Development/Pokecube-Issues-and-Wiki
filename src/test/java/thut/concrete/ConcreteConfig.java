package thut.concrete;

import com.google.common.collect.Lists;
import java.util.List;
import thut.core.common.config.Config.ConfigData;
import thut.core.common.config.Configure;

public class ConcreteConfig extends ConfigData
{

    @Configure(category = "Volcanoes", comment = "Are volcanoes active, set to false to prevent them from growing. [Default: true]")
    public boolean volcanoes_tick = true;

    @Configure(category = "Painting", comment = "Blocks listed here will be dyeable with the paint brush. \nBlocks must include \"red\" in the id for this to function.")
    public List<String> dyeable_blocks = Lists.newArrayList(
            //@formatter:off
            "minecraft:red_banner",
            "minecraft:red_bed",
            "minecraft:red_candle",
            "minecraft:red_candle_cake",
            "minecraft:red_carpet",
            "minecraft:red_concrete",
            "minecraft:red_concrete_powder",
            "minecraft:red_glazed_terracotta",
            "minecraft:red_shulker_box",
            "minecraft:red_stained_glass",
            "minecraft:red_stained_glass_pane",
            "minecraft:red_terracotta",
            "minecraft:red_wall_banner",
            "minecraft:red_wool",
            "pokecube_legends:bulu_red_totem",
            "pokecube_legends:dyna_leaves_red",
            "pokecube_legends:distortic_one_way_red_stained_glass",
            "pokecube_legends:fini_red_totem",
            "pokecube_legends:koko_red_totem",
            "pokecube_legends:lele_red_totem",
            "warp_pipes:red_warp_pipe");
    //@formatter:on

    /**
     * @param MODID
     */
    public ConcreteConfig(final String MODID)
    {
        super(MODID);
    }

    @Override
    public void onUpdated()
    {}

}
