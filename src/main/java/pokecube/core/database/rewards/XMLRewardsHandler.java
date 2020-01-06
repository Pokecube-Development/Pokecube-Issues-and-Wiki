package pokecube.core.database.rewards;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntryLoader.Drop;
import pokecube.core.database.stats.CaptureStats;
import pokecube.core.handlers.PokecubePlayerDataHandler;
import pokecube.core.handlers.PokedexInspector;
import pokecube.core.handlers.PokedexInspector.IInspectReward;
import pokecube.core.handlers.playerdata.PokecubePlayerCustomData;
import pokecube.core.utils.Tools;

public class XMLRewardsHandler
{
    public static class CaptureParser implements IRewardParser
    {
        public static class InspectCapturesReward implements IInspectReward
        {
            final ItemStack reward;
            final boolean   percent;
            final double    num;
            final String    message;
            final String    tagString;

            public InspectCapturesReward(final ItemStack reward, final double num, final boolean percent,
                    final String message, final String tagString)
            {
                this.reward = reward;
                this.message = message;
                this.tagString = tagString;
                this.num = num;
                this.percent = percent;
            }

            private boolean check(final Entity entity, final CompoundNBT tag, final ItemStack reward, final int num,
                    final boolean giveReward)
            {
                if (reward == null || tag.getBoolean(this.tagString)) return false;
                if (this.matches(num))
                {
                    if (giveReward)
                    {
                        tag.putBoolean(this.tagString, true);
                        entity.sendMessage(new TranslationTextComponent(this.message));
                        final PlayerEntity PlayerEntity = (PlayerEntity) entity;
                        Tools.giveItem(PlayerEntity, reward.copy());
                        PokecubePlayerDataHandler.saveCustomData(entity.getCachedUniqueIdString());
                    }
                    return true;
                }
                return false;
            }

            @Override
            public boolean inspect(final PokecubePlayerCustomData data, final Entity entity, final boolean giveReward)
            {
                final int num = CaptureStats.getNumberUniqueCaughtBy(entity.getUniqueID());
                try
                {
                    return this.check(entity, data.tag, this.reward, num, giveReward);
                }
                catch (final IllegalArgumentException e)
                {
                    e.printStackTrace();
                }
                return false;
            }

            private boolean matches(final int num)
            {
                int required = 0;
                if (this.percent) required = (int) (this.num * Database.spawnables.size() / 100d);
                else required = (int) this.num;
                return required <= num;
            }
        }

        static final QName KEY     = new QName("key");
        static final QName NUM     = new QName("num");
        static final QName MESS    = new QName("mess");
        static final QName PERCENT = new QName("percent");

        @Override
        public void process(final XMLReward reward)
        {
            final String key = reward.condition.values.get(CaptureParser.KEY);
            final String mess = reward.condition.values.get(CaptureParser.MESS);
            final double num = Double.parseDouble(reward.condition.values.get(CaptureParser.NUM));
            boolean percent = false;
            if (reward.condition.values.containsKey(CaptureParser.PERCENT)) percent = Boolean.parseBoolean(
                    reward.condition.values.get(CaptureParser.PERCENT));
            final ItemStack give = XMLRewardsHandler.getStack(reward.output);
            if (give == null || key == null || mess == null) throw new NullPointerException(key + " " + mess + " "
                    + give);
            PokedexInspector.rewards.add(new InspectCapturesReward(give, num, percent, mess, key));
        }

    }

    public static class FreeBookParser implements IRewardParser
    {

        public static class FreeTranslatedReward implements IInspectReward
        {
            public final String  key;
            public final boolean watch_only;
            final String         message;
            final String         tagKey;
            final String         langFile;

            public FreeTranslatedReward(final String key, final String message, final String tagKey,
                    final String langFile, final boolean watch_only)
            {
                this.key = key;
                this.message = message;
                this.tagKey = tagKey;
                this.langFile = langFile;
                this.watch_only = watch_only;
            }

            public ItemStack getInfoBook(final String lang)
            {
                final String name = "";
                // TODO new way to define the localized books.
                final ItemStack stack = new ItemStack(Items.WRITTEN_BOOK);
                try
                {
                    stack.setTag(JsonToNBT.getTagFromJson(name));
                }
                catch (final Exception e)
                {
                    PokecubeCore.LOGGER.error("Error with book for " + this.tagKey + " " + name, e);
                }
                return stack;
            }

            @Override
            public boolean inspect(final PokecubePlayerCustomData data, final Entity entity, final boolean giveReward)
            {
                if (this.watch_only) return false;
                String lang = data.tag.getString("lang");
                if (lang.isEmpty()) lang = "en_US";
                if (data.tag.getBoolean(this.key)) return false;
                if (giveReward)
                {
                    final ItemStack book = this.getInfoBook(lang);
                    data.tag.putBoolean(this.key, true);
                    entity.sendMessage(new TranslationTextComponent(this.message));
                    final PlayerEntity PlayerEntity = (PlayerEntity) entity;
                    Tools.giveItem(PlayerEntity, book);
                    PokecubePlayerDataHandler.saveCustomData(entity.getCachedUniqueIdString());
                }
                return true;
            }
        }

        static final QName KEY       = new QName("key");
        static final QName MESS      = new QName("mess");
        static final QName LANG      = new QName("file");
        static final QName TAG       = new QName("tag");
        static final QName WATCHONLY = new QName("watch_only");

        @Override
        public void process(final XMLReward reward)
        {
            final String key = reward.condition.values.get(FreeBookParser.KEY);
            final String mess = reward.condition.values.get(FreeBookParser.MESS);
            final String lang = reward.condition.values.get(FreeBookParser.LANG);
            final String tag = reward.condition.values.get(FreeBookParser.TAG);
            boolean watch_only = false;
            if (reward.condition.values.containsKey(FreeBookParser.WATCHONLY)) watch_only = Boolean.parseBoolean(
                    reward.condition.values.get(FreeBookParser.WATCHONLY));
            if (key == null || mess == null || lang == null || tag == null) throw new NullPointerException(key + " "
                    + mess + " " + lang + " " + tag);
            PokedexInspector.rewards.add(new FreeTranslatedReward(key, mess, tag, lang, watch_only));
        }
    }

    @XmlRootElement(name = "Reward")
    public static class XMLReward
    {
        @XmlAttribute
        String                    handler = "default";
        @XmlAttribute
        String                    key     = "default";
        @XmlElement(name = "Item")
        public XMLRewardOutput    output;
        @XmlElement(name = "Condition")
        public XMLRewardCondition condition;
        @XmlAnyAttribute
        public Map<QName, String> values  = Maps.newHashMap();

        @Override
        public String toString()
        {
            return "output: " + this.output + " condition: " + this.condition + " key: " + this.key;
        }
    }

    @XmlRootElement(name = "Condition")
    public static class XMLRewardCondition
    {
        @XmlAnyAttribute
        public Map<QName, String> values = Maps.newHashMap();

        @Override
        public String toString()
        {
            return "values: " + this.values;
        }
    }

    @XmlRootElement(name = "Item")
    public static class XMLRewardOutput extends Drop
    {
        @Override
        public String toString()
        {
            return "values: " + this.values + " tag: " + this.tag;
        }
    }

    @XmlRootElement(name = "Rewards")
    public static class XMLRewards
    {
        @XmlElement(name = "Reward")
        public List<XMLReward> recipes = Lists.newArrayList();
    }

    public static Set<ResourceLocation> recipeFiles = Sets.newHashSet();

    public static Map<String, IRewardParser> recipeParsers = Maps.newHashMap();

    static
    {
        XMLRewardsHandler.recipeParsers.put("default", new CaptureParser());
        XMLRewardsHandler.recipeParsers.put("freebook", new FreeBookParser());
    }

    public static void addReward(final XMLReward recipe)
    {
        final IRewardParser parser = XMLRewardsHandler.recipeParsers.get(recipe.handler);
        try
        {
            parser.process(recipe);
        }
        catch (final NullPointerException e)
        {
            PokecubeCore.LOGGER.error("Error with a recipe, Error for: " + recipe, e);
        }
    }

    public static ItemStack getStack(final Drop drop)
    {
        final Map<QName, String> values = drop.values;
        if (drop.tag != null)
        {
            final QName name = new QName("tag");
            values.put(name, drop.tag);
        }
        return Tools.getStack(drop.values);
    }
}
