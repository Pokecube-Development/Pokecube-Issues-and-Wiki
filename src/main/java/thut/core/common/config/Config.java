package thut.core.common.config;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent.Reloading;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import thut.core.common.ThutCore;

public class Config
{
    public static abstract class ConfigData implements IConfigHolder
    {
        public final String MODID;

        private ModConfig COMMON_CONFIG;
        private ModConfig SERVER_CONFIG;
        private ModConfig CLIENT_CONFIG;

        /** Other fields */
        public Map<Field, ConfigValue<?>> commonValues = Maps.newHashMap();
        public Map<Field, ConfigValue<?>> clientValues = Maps.newHashMap();
        public Map<Field, ConfigValue<?>> serverValues = Maps.newHashMap();

        /**
         * @param MODID
         *            modid of the mod we are made for.
         */
        public ConfigData(final String MODID)
        {
            this.MODID = MODID;
        }

        @Override
        public void init(final Type type, final Field field, final ConfigValue<?> val)
        {
            switch (type)
            {
            case CLIENT:
                this.clientValues.put(field, val);
                break;
            case COMMON:
                this.commonValues.put(field, val);
                break;
            case SERVER:
                this.serverValues.put(field, val);
                break;
            default:
                break;
            }
        }

        @SubscribeEvent
        public void onFileChange(final Reloading configEvent)
        {
            ThutCore.LOGGER.debug("{} config belongs to us!", configEvent.getConfig().getFileName());
            if (configEvent.getConfig().getConfigData() instanceof CommentedFileConfig)
                ((CommentedFileConfig) configEvent.getConfig().getConfigData()).load();
            this.read(configEvent.getConfig());
        }

        @SubscribeEvent
        public void onLoad(final ModConfigEvent.Loading configEvent)
        {
            ThutCore.LOGGER.info("Loaded {} config file {}", this.MODID, configEvent.getConfig().getFileName());
            this.read(configEvent.getConfig());
        }

        @Override
        public void read(final ModConfig modConfig)
        {
            Map<Field, ConfigValue<?>> values;
            final Type type = modConfig.getType();
            switch (type)
            {
            case CLIENT:
                values = this.clientValues;
                this.CLIENT_CONFIG = modConfig;
                break;
            case COMMON:
                values = this.commonValues;
                this.COMMON_CONFIG = modConfig;
                break;
            case SERVER:
                values = this.serverValues;
                this.SERVER_CONFIG = modConfig;
                break;
            default:
                return;
            }
            if (this.read(modConfig, values)) this.onUpdated();
        }

        private boolean read(final ModConfig config, final Map<Field, ConfigValue<?>> values)
        {
            ThutCore.LOGGER.info("Reading {}", config.getFileName());
            boolean changed = false;
            for (final Field f : values.keySet())
                try
                {
                    f.setAccessible(true);
                    final Object ours = f.get(this);
                    final Object o = values.get(f).get();
                    if (ours.equals(o)) continue;
                    ThutCore.LOGGER.info("Set {} to {}", f.getName(), o);
                    f.set(this, o);
                    changed = true;
                }
                catch (final Exception e)
                {
                    ThutCore.LOGGER.error("Error updating config value for " + f, e);
                }
            return changed;
        }

        public void updateField(final Field field, final Object update) throws Exception
        {
            field.getAnnotation(Configure.class);
            if (field.getType() == Long.TYPE || field.getType() == Long.class) field.set(this, Long.parseLong(
                    (String) update));
            else if (field.getType() == String.class) field.set(this, update);
            else if (field.getType() == Integer.TYPE || field.getType() == Integer.class) field.set(this, Integer
                    .parseInt((String) update));
            else if (field.getType() == Float.TYPE || field.getType() == Float.class) field.set(this, Float.parseFloat(
                    (String) update));
            else if (field.getType() == Double.TYPE || field.getType() == Double.class) field.set(this, Double
                    .parseDouble((String) update));
            else if (field.getType() == Boolean.TYPE || field.getType() == Boolean.class) field.set(this, Boolean
                    .parseBoolean((String) update));
            else
            {
                final Object o = field.get(this);
                if (o instanceof String[])
                {
                    final String[] vars = update instanceof String ? ((String) update).split("``") : (String[]) update;
                    field.set(this, vars);
                }
                else if (o instanceof List<?> && !((List<?>) o).isEmpty() && ((List<?>) o).get(0) instanceof String)
                {
                    @SuppressWarnings("unchecked")
                    final List<String> list = (List<String>) o;
                    final String[] vars = update instanceof String ? ((String) update).split("``") : (String[]) update;
                    list.clear();
                    for (final String s : vars)
                        list.add(s);
                }
                else if (o instanceof int[])
                {
                    final String[] vars = update instanceof String ? ((String) update).split("``")
                            : update instanceof String[] ? (String[]) update : null;
                    int[] toSet = null;
                    if (vars == null) toSet = (int[]) update;
                    else
                    {
                        toSet = new int[vars.length];
                        for (int i = 0; i < vars.length; i++)
                            toSet[i] = Integer.parseInt(vars[i].trim());
                    }
                    field.set(this, toSet);
                }
                else System.err.println("Unknown Type " + field.getType() + " " + field.getName() + " " + o.getClass());
            }
            this.onUpdated();
            this.write();
        }

        @Override
        public void write()
        {
            this.write(this.CLIENT_CONFIG, this.clientValues);
            this.write(this.COMMON_CONFIG, this.commonValues);
            this.write(this.SERVER_CONFIG, this.serverValues);
        }

        private boolean write(final ModConfig config, final Map<Field, ConfigValue<?>> values)
        {
            boolean ret = false;
            for (final Field f : values.keySet())
                try
                {
                    final Object ours = f.get(this);
                    final Object val = values.get(f).get();
                    if (ours.equals(val)) continue;
                    config.getConfigData().set(values.get(f).getPath(), ours);
                    ret = true;
                }
                catch (final Exception e)
                {
                    ThutCore.LOGGER.error("Error saving config value for " + f, e);
                }
            return ret;
        }
    }

    public static interface IConfigHolder
    {
        void init(Type type, Field field, ConfigValue<?> val);

        /**
         * This is called whenever the values in this config may have changed.
         */
        void onUpdated();

        /**
         * This is called when this config is read.
         *
         * @param spec
         */
        void read(ModConfig spec);

        void write();
    }

    private static ForgeConfigSpec[] initConfigSpecs(final IConfigHolder holder)
    {
        final Builder COMMON_BUILDER = new Builder();
        final Builder CLIENT_BUILDER = new Builder();
        final Builder SERVER_BUILDER = new Builder();

        final List<Field> commonList = Lists.newArrayList();
        final List<Field> clientList = Lists.newArrayList();
        final List<Field> serverList = Lists.newArrayList();

        for (final Field field : holder.getClass().getDeclaredFields())
        {
            final Configure conf = field.getAnnotation(Configure.class);
            if (conf == null) continue;
            switch (conf.type())
            {
            case CLIENT:
                clientList.add(field);
                field.setAccessible(true);
                break;
            case COMMON:
                commonList.add(field);
                field.setAccessible(true);
                break;
            case SERVER:
                serverList.add(field);
                field.setAccessible(true);
                break;
            default:
                break;
            }
        }

        final Comparator<Field> comp = (o1, o2) ->
        {
            final Configure conf1 = o1.getAnnotation(Configure.class);
            final Configure conf2 = o2.getAnnotation(Configure.class);
            int diff = conf1.category().compareTo(conf2.category());
            if (diff == 0) diff = o1.getName().compareTo(o2.getName());
            return diff;
        };
        Collections.sort(commonList, comp);
        Collections.sort(clientList, comp);
        Collections.sort(serverList, comp);

        Config.build(COMMON_BUILDER, commonList, holder, Type.COMMON);
        Config.build(SERVER_BUILDER, serverList, holder, Type.SERVER);
        Config.build(CLIENT_BUILDER, clientList, holder, Type.CLIENT);

        final ForgeConfigSpec COMMON_CONFIG_SPEC = commonList.isEmpty() ? null : COMMON_BUILDER.pop().build();
        final ForgeConfigSpec CLIENT_CONFIG_SPEC = clientList.isEmpty() ? null : CLIENT_BUILDER.pop().build();
        final ForgeConfigSpec SERVER_CONFIG_SPEC = serverList.isEmpty() ? null : SERVER_BUILDER.pop().build();

        return new ForgeConfigSpec[] { COMMON_CONFIG_SPEC, CLIENT_CONFIG_SPEC, SERVER_CONFIG_SPEC };
    }

    private static void addComment(final Builder builder, final String input)
    {
        // This either splits the input by lines, or just adds it as a comment.
        // it also appends a space at the beginning, so there is whitespace
        // after the # in the start of the comment
        if (input.contains("\n"))
        {
            final String[] vars = input.split("\n");
            for (int i = 0; i < vars.length; i++)
                vars[i] = " " + vars[i];
            builder.comment(vars);
        }
        else builder.comment(" " + input);
    }

    private static void build(final Builder builder, final List<Field> fields, final IConfigHolder holder,
            final Type type)
    {

        final Map<String, String> cat_comments = Maps.newHashMap();

        for (final Field field : fields)
        {
            // Check for strings, if we have those, assume they are category
            // definitions, and check for comments
            if (!Modifier.isStatic(field.getModifiers())) continue;
            try
            {
                final Object o = field.get(null);
                if (o instanceof String)
                {
                    final Configure conf = field.getAnnotation(Configure.class);
                    cat_comments.put((String) o, conf.category());
                }
            }
            catch (final Exception e)
            {
                ThutCore.LOGGER.error("Error getting field " + field, e);
            }
        }
        String cat = "";
        for (final Field field : fields)
            try
            {
                if (Modifier.isStatic(field.getModifiers())) continue;
                final Configure conf = field.getAnnotation(Configure.class);
                if (!cat.equals(conf.category()))
                {
                    // Empty the first time, otherwise we pop off
                    if (!cat.isEmpty()) builder.pop();
                    cat = conf.category();
                    // Push the category
                    builder.push(cat);
                    builder.translation(ModLoadingContext.get().getActiveNamespace() + ".config." + cat);
                    if (cat_comments.containsKey(cat)) Config.addComment(builder, cat_comments.get(cat));
                }
                if (!conf.comment().isEmpty()) Config.addComment(builder, conf.comment());
                builder.translation(ModLoadingContext.get().getActiveNamespace() + ".config." + field.getName()
                        + ".tooltip");
                final Object o = field.get(holder);
                holder.init(type, field, builder.define(field.getName(), o));
            }
            catch (final Exception e)
            {
                ThutCore.LOGGER.error("Error getting field " + field, e);
            }
    }

    private static void loadConfig(final IConfigHolder holder, final ForgeConfigSpec spec, final Path path)
    {
        ThutCore.LOGGER.debug("Loading config file {}", path);
        final CommentedFileConfig configData = CommentedFileConfig.builder(path).sync().autosave().writingMode(
                WritingMode.REPLACE).build();
        configData.load();
        spec.setConfig(configData);
    }

    /**
     * @param holder
     *            the object to store the configs.
     * @param subfolder
     *            the folder that this config is in.
     * @param prefix
     *            prefix for these config files.
     */
    public static void setupConfigs(final IConfigHolder holder, final String subfolder, final String prefix)
    {
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.register(holder);

        ForgeConfigSpec COMMON_CONFIG_SPEC;
        ForgeConfigSpec CLIENT_CONFIG_SPEC;
        ForgeConfigSpec SERVER_CONFIG__SPEC;
        final ForgeConfigSpec[] specs = Config.initConfigSpecs(holder);
        COMMON_CONFIG_SPEC = specs[0];
        CLIENT_CONFIG_SPEC = specs[1];
        SERVER_CONFIG__SPEC = specs[2];

        final File commonfile = new File(subfolder, prefix + "-common.toml");
        final File clientfile = new File(subfolder, prefix + "-client.toml");
        // Server is saved to the world itself, so it doesn't go with rest
        final File serverfile = new File(prefix + "-server.toml");

        // Setup paths for each one.
        final Path common = FMLPaths.CONFIGDIR.get().resolve(subfolder).resolve(prefix + "-common.toml");
        final Path client = FMLPaths.CONFIGDIR.get().resolve(subfolder).resolve(prefix + "-client.toml");
        // Server is saved to the world itself, so it doesn't go with rest
        final Path server = FMLPaths.CONFIGDIR.get().resolve(prefix + "-server.toml");

        // Mk dirs as needed
        if (COMMON_CONFIG_SPEC != null || CLIENT_CONFIG_SPEC != null) common.toFile().getParentFile().mkdirs();

        // Register the configs
        if (COMMON_CONFIG_SPEC != null) ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON,
                COMMON_CONFIG_SPEC, commonfile.toString());
        if (CLIENT_CONFIG_SPEC != null) ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT,
                CLIENT_CONFIG_SPEC, clientfile.toString());
        if (SERVER_CONFIG__SPEC != null) ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER,
                SERVER_CONFIG__SPEC, serverfile.toString());

        // Load configs
        if (COMMON_CONFIG_SPEC != null) Config.loadConfig(holder, COMMON_CONFIG_SPEC, common);
        if (CLIENT_CONFIG_SPEC != null) Config.loadConfig(holder, CLIENT_CONFIG_SPEC, client);
        if (SERVER_CONFIG__SPEC != null) Config.loadConfig(holder, SERVER_CONFIG__SPEC, server);

        // This ensures the values are initialized, this onUpdated is never
        // called unless the config is different
        holder.onUpdated();
    }
}
