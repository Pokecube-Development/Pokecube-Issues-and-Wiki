package pokecube.core.database.rewards;

import pokecube.core.database.rewards.XMLRewardsHandler.XMLReward;
import thut.api.util.JsonUtil;

public interface IRewardParser
{
    default XMLReward deserialize(String recipe)
    {
        return JsonUtil.gson.fromJson(recipe, XMLReward.class);
    }

    void process(XMLReward reward) throws NullPointerException;

    default String serialize(XMLReward recipe)
    {
        return JsonUtil.gson.toJson(recipe);
    }
}
