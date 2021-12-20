package pokecube.core.database.rewards;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.jline.utils.InputStreamReader;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;

import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.database.Database;
import pokecube.core.database.pokedex.PokedexEntryLoader.Drop;
import pokecube.core.database.resources.PackFinder;
import pokecube.core.database.stats.CaptureStats;
import pokecube.core.handlers.PokecubePlayerDataHandler;
import pokecube.core.handlers.PokedexInspector;
import pokecube.core.handlers.PokedexInspector.IInspectReward;
import pokecube.core.handlers.playerdata.PokecubePlayerCustomData;
import pokecube.core.utils.Tools;
import thut.api.util.JsonUtil;
import thut.core.xml.bind.annotation.XmlAnyAttribute;
import thut.core.xml.bind.annotation.XmlAttribute;
import thut.core.xml.bind.annotation.XmlElement;
import thut.core.xml.bind.annotation.XmlRootElement;

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

            private boolean check(final Entity entity, final CompoundTag tag, final ItemStack reward, final int num,
                    final boolean giveReward)
            {
                if (reward == null || tag.getBoolean(this.tagString)) return false;
                if (this.matches(num))
                {
                    if (giveReward)
                    {
                        tag.putBoolean(this.tagString, true);
                        entity.sendMessage(new TranslatableComponent(this.message), Util.NIL_UUID);
                        final Player PlayerEntity = (Player) entity;
                        Tools.giveItem(PlayerEntity, reward.copy());
                        PokecubePlayerDataHandler.saveCustomData(entity.getStringUUID());
                    }
                    return true;
                }
                return false;
            }

            @Override
            public boolean inspect(final PokecubePlayerCustomData data, final Entity entity, final boolean giveReward)
            {
                final int num = CaptureStats.getNumberUniqueCaughtBy(entity.getUUID());
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
            final ItemStack give = Tools.getStack(reward.output.getValues());
            if (give == null || key == null || mess == null) throw new NullPointerException(key + " " + mess + " "
                    + give);
            PokedexInspector.rewards.add(new InspectCapturesReward(give, num, percent, mess, key));
        }
    }

    public static class FreeBookParser implements IRewardParser
    {
        public static class PagesFile
        {
            public static class Page
            {
                public List<String> lines = Lists.newArrayList();
            }

            public List<Page> pages  = Lists.newArrayList();
            public String     author = "";
            public String     title  = "";
        }

        public static String default_lang = "en_us";

        public static class FreeTranslatedReward implements IInspectReward
        {
            public Map<String, ItemStack> lang_stacks = Maps.newHashMap();
            public Map<String, PagesFile> lang_books  = Maps.newHashMap();

            public final String  key;
            public final boolean watch_only;
            public final boolean page_file;
            public final String  message;
            public final String  tagKey;
            public final String  langFile;

            public FreeTranslatedReward(final String key, final String message, final String tagKey,
                    final String langFile, final boolean watch_only)
            {
                this.key = key;
                this.message = message;
                this.tagKey = tagKey;
                this.langFile = langFile;
                this.watch_only = watch_only;
                this.page_file = tagKey.equals("pages_file");
            }

            public ItemStack getInfoStack(String lang)
            {
                lang = lang.toLowerCase(Locale.ROOT);
                if (!this.lang_stacks.containsKey(lang)) this.initLangBook(lang);
                return this.lang_stacks.getOrDefault(lang, ItemStack.EMPTY).copy();
            }

            public PagesFile getInfoBook(String lang)
            {
                lang = lang.toLowerCase(Locale.ROOT);
                if (!this.lang_books.containsKey(lang)) this.initLangBook(lang);
                return this.lang_books.getOrDefault(lang, null);
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
                    final ItemStack book = this.getInfoStack(lang);
                    data.tag.putBoolean(this.key, true);
                    entity.sendMessage(new TranslatableComponent(this.message), Util.NIL_UUID);
                    final Player PlayerEntity = (Player) entity;
                    Tools.giveItem(PlayerEntity, book);
                    PokecubePlayerDataHandler.saveCustomData(entity.getStringUUID());
                }
                return true;
            }

            public void initLangBook(final String lang)
            {
                this.initLangBook(lang, lang);
            }

            public void initLangBook(String lang, final String key)
            {
                try
                {
                    lang = lang.toLowerCase(Locale.ROOT);
                    final ResourceLocation langloc = PokecubeItems.toPokecubeResource(String.format(this.langFile,
                            lang));
                    final InputStream stream = PackFinder.getStream(langloc);
                    if (this.page_file)
                    {
                        final PagesFile book = JsonUtil.gson.fromJson(new InputStreamReader(stream, "UTF-8"),
                                PagesFile.class);
                        this.lang_books.put(key, book);
                    }
                    else
                    {
                        final JsonObject holder = JsonUtil.gson.fromJson(new InputStreamReader(stream,
                                "UTF-8"), JsonObject.class);
                        final String json = holder.get(this.tagKey).getAsString();
                        final ItemStack stack = new ItemStack(Items.WRITTEN_BOOK);
                        try
                        {
                            stack.setTag(TagParser.parseTag(json));
                            this.lang_stacks.put(key, stack);
                        }
                        catch (final Exception e)
                        {
                            PokecubeCore.LOGGER.error("Error with book for " + this.tagKey + " " + json, e);
                        }
                    }
                    stream.close();
                }
                catch (final FileNotFoundException e)
                {
                    if (lang.equals(FreeBookParser.default_lang)) PokecubeCore.LOGGER.error("Error with book for "
                            + this.tagKey, e);
                    else this.initLangBook(FreeBookParser.default_lang, lang);
                }
                catch (final Exception e)
                {
                    PokecubeCore.LOGGER.error("Error with book for " + this.tagKey, e);
                }

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
            // This is out custom structure, so only put this in watch for now.
            if (tag.equals("pages_file")) watch_only = true;
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

    public static Set<String> loadedRecipes = Sets.newHashSet();

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
}
