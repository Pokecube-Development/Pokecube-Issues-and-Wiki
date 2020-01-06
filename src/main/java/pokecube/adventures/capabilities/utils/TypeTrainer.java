package pokecube.adventures.capabilities.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.INPC;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MerchantOffer;
import net.minecraft.resources.IResource;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.adventures.Config;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.capabilities.CapabilityHasPokemobs;
import pokecube.adventures.capabilities.CapabilityHasPokemobs.IHasPokemobs;
import pokecube.adventures.entity.trainer.TrainerBase;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.database.Pokedex;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokedexEntry.EvolutionData;
import pokecube.core.database.SpawnBiomeMatcher;
import pokecube.core.events.pokemob.SpawnEvent.Variance;
import pokecube.core.handlers.events.SpawnHandler;
import pokecube.core.interfaces.IPokecube.PokecubeBehavior;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.utils.Tools;

public class TypeTrainer
{
    public static interface ITypeMapper
    {
        /**
         * Mapping of LivingEntity to a TypeTrainer. EntityTrainers set
         * this on spawn, so it isn't needed for them. <br>
         * <br>
         * if forSpawn, it means this is being initialized, otherwise it is
         * during the check for whether this mob should have trainers.
         */
        default TypeTrainer getType(final LivingEntity mob, final boolean forSpawn)
        {
            if (!SpawnHandler.canSpawnInWorld(mob.getEntityWorld())) return null;
            if (!forSpawn)
            {
                if (mob instanceof TrainerBase) return TypeTrainer.merchant;
                if (Config.instance.npcsAreTrainers && mob instanceof INPC) return TypeTrainer.merchant;
                for (final Class<? extends Entity> clazz : Config.instance.customTrainers)
                    if (clazz.isInstance(mob)) return TypeTrainer.merchant;
                return null;
            }

            if (mob instanceof VillagerEntity && Config.instance.npcsAreTrainers)
            {
                final VillagerEntity villager = (VillagerEntity) mob;
                final String type = villager.getVillagerData().getProfession().toString();
                return TypeTrainer.getTrainer(type);
            }
            if (mob instanceof TrainerBase)
            {
                final TypeTrainer type = ((TrainerBase) mob).pokemobsCap.getType();
                if (type != null) return type;
                return TypeTrainer.merchant;
            }
            return null;
        }

        /**
         * Should the IHasPokemobs for this mob sync the values to client? if
         * not, it will use a server-side list of mobs instead of datamanager
         * values.
         */
        default boolean shouldSync(final LivingEntity mob)
        {
            return mob instanceof LivingEntity;
        }
    }

    public static class TrainerTrade extends MerchantOffer
    {
        public int   min    = -1;
        public int   max    = -1;
        public float chance = 1;

        public TrainerTrade(final ItemStack buy1, final ItemStack buy2, final ItemStack sell)
        {
            super(buy1, buy2, sell, -1, -1, 1);
        }

        public MerchantOffer getRecipe()
        {
            ItemStack buy1 = this.func_222218_a();
            ItemStack buy2 = this.func_222202_c();
            if (!buy1.isEmpty()) buy1 = buy1.copy();
            if (!buy2.isEmpty()) buy2 = buy2.copy();
            ItemStack sell = this.func_222200_d();
            if (!sell.isEmpty()) sell = sell.copy();
            else return null;
            if (this.min != -1 && this.max != -1)
            {
                if (this.max < this.min) this.max = this.min;
                sell.setCount(this.min + new Random().nextInt(1 + this.max - this.min));
            }
            // TODO Find out where the mess with client side so the 65 isn't
            // needed.
            final MerchantOffer ret = new MerchantOffer(buy1, buy2, sell, 0, 65, 1);
            return ret;
        }
    }

    public static class TrainerTrades
    {
        public List<TrainerTrade> tradesList = Lists.newArrayList();

        public void addTrades(final List<MerchantOffer> ret)
        {
            for (final TrainerTrade trade : this.tradesList)
                if (Math.random() < trade.chance)
                {
                    final MerchantOffer toAdd = trade.getRecipe();
                    if (toAdd != null) ret.add(toAdd);
                }
        }
    }

    public static ITypeMapper mobTypeMapper = new ITypeMapper()
    {
    };

    public static HashMap<String, TrainerTrades> tradesMap   = Maps.newHashMap();
    public static HashMap<String, TypeTrainer>   typeMap     = new HashMap<>();
    public static ArrayList<String>              maleNames   = new ArrayList<>();
    public static ArrayList<String>              femaleNames = new ArrayList<>();

    public static TypeTrainer merchant = new TypeTrainer("Merchant");
    static
    {
        TypeTrainer.merchant.tradeTemplate = "merchant";
    }

    public static void addTrainer(final String name, final TypeTrainer type)
    {
        TypeTrainer.typeMap.put(name, type);
    }

    public static void getRandomTeam(final IHasPokemobs trainer, final LivingEntity owner, int level, final World world)
    {
        final TypeTrainer type = trainer.getType();

        for (int i = 0; i < 6; i++)
            trainer.setPokemob(i, ItemStack.EMPTY);

        if (level == 0) level = 5;
        final Variance variance = SpawnHandler.DEFAULT_VARIANCE;
        int number = 1 + new Random().nextInt(6);
        number = Math.min(number, trainer.getMaxPokemobCount());

        final List<PokedexEntry> values = Lists.newArrayList();
        if (type.pokemon != null) values.addAll(type.pokemon);
        else PokecubeCore.LOGGER.warn("No mobs for " + type);

        for (int i = 0; i < number; i++)
        {
            Collections.shuffle(values);
            ItemStack item = ItemStack.EMPTY;
            for (final PokedexEntry s : values)
            {
                if (s != null) item = TypeTrainer.makeStack(s, owner, world, variance.apply(level));
                if (!item.isEmpty()) break;
            }
            trainer.setPokemob(i, item);
        }
    }

    public static TypeTrainer getTrainer(final String name)
    {
        final TypeTrainer ret = TypeTrainer.typeMap.get(name);
        if (ret == null)
        {
            for (final TypeTrainer t : TypeTrainer.typeMap.values())
                if (t != null && t.name.equalsIgnoreCase(name)) return t;
            for (final TypeTrainer t : TypeTrainer.typeMap.values())
                if (t != null) return t;
        }
        return ret;
    }

    public static void initSpawns()
    {
        for (final TypeTrainer type : TypeTrainer.typeMap.values())
            for (final SpawnBiomeMatcher matcher : type.matchers.keySet())
            {
                matcher.reset();
                matcher.parse();
            }
    }

    public static ItemStack makeStack(final PokedexEntry entry, final LivingEntity trainer, final World world,
            final int level)
    {
        final int num = entry.getPokedexNb();
        if (Pokedex.getInstance().getEntry(num) == null) return ItemStack.EMPTY;

        IPokemob pokemob = CapabilityPokemob.getPokemobFor(PokecubeCore.createPokemob(entry, world));
        if (pokemob != null)
        {
            for (int i = 1; i < level; i++)
                if (pokemob.getPokedexEntry().canEvolve(i)) for (final EvolutionData d : pokemob.getPokedexEntry()
                        .getEvolutions())
                    if (d.shouldEvolve(pokemob))
                    {
                        final IPokemob temp = CapabilityPokemob.getPokemobFor(d.getEvolution(world));
                        if (temp != null)
                        {
                            pokemob = temp;
                            break;
                        }
                    }
            pokemob.getEntity().setHealth(pokemob.getEntity().getMaxHealth());
            pokemob = pokemob.setPokedexEntry(entry);
            pokemob.setOwner(trainer);
            pokemob.setPokecube(new ItemStack(PokecubeItems.getFilledCube(PokecubeBehavior.DEFAULTCUBE)));
            final int exp = Tools.levelToXp(pokemob.getExperienceMode(), level);
            pokemob = pokemob.setForSpawn(exp);
            final ItemStack item = PokecubeManager.pokemobToItem(pokemob);
            pokemob.getEntity().remove();
            return item;
        }

        return ItemStack.EMPTY;
    }

    public static void postInitTrainers()
    {
        final List<TypeTrainer> toRemove = new ArrayList<>();
        for (final TypeTrainer t : TypeTrainer.typeMap.values())
            if (t.pokemon.size() == 0 && t != TypeTrainer.merchant) toRemove.add(t);
        if (!toRemove.isEmpty()) PokecubeCore.LOGGER.debug("Removing Trainer Types: " + toRemove);
        for (final TypeTrainer t : toRemove)
            TypeTrainer.typeMap.remove(t.name);
    }

    public final String name;
    /** 1 = male, 2 = female, 3 = both */
    public byte         genders = 1;

    public Map<SpawnBiomeMatcher, Float> matchers = Maps.newHashMap();
    public boolean                       hasBag   = false;
    public ItemStack                     bag      = ItemStack.EMPTY;
    public boolean                       hasBelt  = false;
    private ResourceLocation             texture;

    private ResourceLocation femaleTexture;

    public String             tradeTemplate = "default";
    public List<PokedexEntry> pokemon       = Lists.newArrayList();
    public TrainerTrades      trades;

    private final ItemStack[] loot = NonNullList.<ItemStack> withSize(4, ItemStack.EMPTY).toArray(new ItemStack[4]);

    public String    drops = "";
    public ItemStack held  = ItemStack.EMPTY;

    public TypeTrainer(final String name)
    {
        this.name = name;
        TypeTrainer.addTrainer(name, this);
    }

    public Collection<MerchantOffer> getRecipes()
    {
        if (this.trades == null && this.tradeTemplate != null)
        {
            this.trades = TypeTrainer.tradesMap.get(this.tradeTemplate);
            if (this.trades == null) this.tradeTemplate = null;
        }
        final List<MerchantOffer> ret = Lists.newArrayList();
        if (this.trades != null) this.trades.addTrades(ret);
        return ret;
    }

    @OnlyIn(Dist.CLIENT)
    public ResourceLocation getTexture(final LivingEntity trainer)
    {
        final IHasPokemobs cap = CapabilityHasPokemobs.getHasPokemobs(trainer);
        if (this.texture == null && (this.genders == 1 || this.genders == 2))
        {
            this.texture = new ResourceLocation(PokecubeAdv.TRAINERTEXTUREPATH + this.name.toLowerCase(Locale.US)
                    + ".png");
            if (!this.texExists(this.texture)) this.texture = null;
            if (this.genders == 2 && this.texture == null) this.texture = new ResourceLocation(
                    PokecubeAdv.TRAINERTEXTUREPATH + "female.png");
            if (this.genders == 1 && this.texture == null) this.texture = new ResourceLocation(
                    PokecubeAdv.TRAINERTEXTUREPATH + "male.png");
        }
        else if (this.genders == 3)
        {
            if (this.femaleTexture == null)
            {
                this.femaleTexture = new ResourceLocation(PokecubeAdv.TRAINERTEXTUREPATH + this.name.toLowerCase(
                        Locale.US) + "female.png");
                if (!this.texExists(this.femaleTexture)) this.femaleTexture = null;
            }
            if (this.texture == null)
            {
                this.texture = new ResourceLocation(PokecubeAdv.TRAINERTEXTUREPATH + this.name.toLowerCase(Locale.US)
                        + ".png");
                if (!this.texExists(this.texture)) this.texture = null;
            }
            if (this.femaleTexture == null) this.femaleTexture = new ResourceLocation(PokecubeAdv.TRAINERTEXTUREPATH
                    + "female.png");
            if (this.texture == null) this.texture = new ResourceLocation(PokecubeAdv.TRAINERTEXTUREPATH + "male.png");

            return cap.getGender() == 1 ? this.texture : this.femaleTexture;
        }
        return this.texture;
    }

    private void initLoot()
    {
        if (!this.loot[0].isEmpty()) return;

        if (!this.drops.equals(""))
        {
            final String[] args = this.drops.split(":");
            int num = 0;
            for (final String s : args)
            {
                if (s == null) continue;
                final String[] stackinfo = s.split("`");
                final ItemStack stack = PokecubeItems.getStack(stackinfo[0]);
                if (stackinfo.length > 1) try
                {
                    final int count = Integer.parseInt(stackinfo[1]);
                    stack.setCount(count);
                }
                catch (final NumberFormatException e)
                {
                }
                this.loot[num] = stack;
                num++;
            }
        }
        if (this.loot[0].isEmpty()) this.loot[0] = new ItemStack(Items.EMERALD);
    }

    public void initTrainerItems(final LivingEntity trainer)
    {
        this.initLoot();
        for (int i = 1; i < 5; i++)
        {
            final EquipmentSlotType slotIn = EquipmentSlotType.values()[i];
            trainer.setItemStackToSlot(slotIn, this.loot[i - 1]);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private boolean texExists(final ResourceLocation texture)
    {
        try
        {
            final IResource res = Minecraft.getInstance().getResourceManager().getResource(texture);
            res.close();
            return true;
        }
        catch (final Exception e)
        {
            return false;
        }
    }

    @Override
    public String toString()
    {
        return "" + this.name;
    }
}
