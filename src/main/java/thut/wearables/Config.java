package thut.wearables;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import thut.core.common.config.Config.ConfigData;
import thut.core.common.config.Configure;

public class Config extends ConfigData
{
    public static final String client = "client";

    @Configure(category = Config.client, comment = "Position of the Wearables button. [Default: [26, 9]]")
    public List<Integer> buttonPos       = Lists.newArrayList(new Integer[] { 26, 9 });
    @Configure(category = Config.client, comment = "Enables the Wearables button. [Default: true]")
    public boolean       hasButton       = true;
    @Configure(category = Config.client, comment = "Render blacklist for wearables.")
    public List<Integer> renderBlacklist = Lists.newArrayList();

    // TODO decide on how to do these.
    public Map<Integer, float[]> renderOffsets      = Maps.newHashMap();
    public Map<Integer, float[]> renderOffsetsSneak = Maps.newHashMap();

    // This allows addons, etc to override the button.
    public boolean noButton = false;

    public Config()
    {
        super(ThutWearables.MODID);
    }

    @Override
    public void onUpdated()
    {
        // Nothing to do, as the gui just directly uses the list.
    }

}
