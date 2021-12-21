package thut.bot.entity.ai.modules;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.resources.ResourceLocation;
import thut.bot.entity.BotPlayer;
import thut.bot.entity.ai.BotAI;

@BotAI(key = "pokecube:village_routes", mod = "pokecube")
public class VillageRouteMaker extends RouteMaker
{
    public static final Pattern startPattern = Pattern.compile(START);
    public static final Pattern startPattern_num = Pattern.compile(START + SPACE + INT);
    public static final Pattern startPattern_num_speed = Pattern.compile(START + SPACE + INT + SPACE + INT);

    public VillageRouteMaker(final BotPlayer player)
    {
        super(player);
        this.target = new ResourceLocation("pokecube:town");
    }

    @Override
    public boolean init(String args)
    {
        Matcher match = startPattern_num_speed.matcher(args);
        if (match.find())
        {
            try
            {
                maxNodes = Integer.parseInt(match.group(5));
                road_maker.tpTicks = Integer.parseInt(match.group(7));
            }
            catch (Exception e)
            {
                player.chat(e.getLocalizedMessage());
            }
        }
        else if ((match = startPattern_num.matcher(args)).find())
        {
            try
            {
                maxNodes = Integer.parseInt(match.group(5));
            }
            catch (Exception e)
            {
                player.chat(e.getLocalizedMessage());
            }
        }
        this.getTag().putInt("max_n", this.maxNodes);
        return true;
    }
}
