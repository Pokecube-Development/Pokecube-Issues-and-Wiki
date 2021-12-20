package pokecube.adventures.ai.poi;

import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.compress.utils.Lists;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.RegistryEvent.Register;
import pokecube.adventures.PokecubeAdv;
import pokecube.core.PokecubeItems;
import pokecube.core.entity.npc.NpcType;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.berries.BerryManager;
import pokecube.core.items.vitamins.ItemVitamin;

public class Professions
{
    public static VillagerProfession HEALER;
    public static VillagerProfession PROFESSOR;
    public static VillagerProfession MERCHANT;

    public static void register(final Register<VillagerProfession> event)
    {
        Professions.HEALER = new VillagerProfession("pokecube_adventures:healer",
                pokecube.core.ai.poi.PointsOfInterest.HEALER.get(), ImmutableSet.of(), ImmutableSet.of(), null)
                        .setRegistryName("pokecube_adventures:healer");

        Professions.PROFESSOR = new VillagerProfession("pokecube_adventures:professor", PointsOfInterest.GENELAB.get(),
                ImmutableSet.of(), ImmutableSet.of(), null).setRegistryName("pokecube_adventures:professor");
        Professions.MERCHANT = new VillagerProfession("pokecube_adventures:trader", PointsOfInterest.TRADER.get(),
                ImmutableSet.of(), ImmutableSet.of(), null).setRegistryName("pokecube_adventures:trader");

        event.getRegistry().register(HEALER);
        event.getRegistry().register(PROFESSOR);
        event.getRegistry().register(MERCHANT);

        NpcType.byType("healer").setProfession(Professions.HEALER);
        NpcType.byType("professor").setProfession(Professions.PROFESSOR);
        NpcType.byType("trader").setProfession(Professions.MERCHANT);

        // Now add some trades for these.

        /*//@formatter:off
         * Healer Trades:
         * 
         * Revives
         * Vitamins
         * Berry buy/sales
         * 
         * Professor Trades:
         * 
         * Pokedex
         * Exp Share
         * Bag
         * Cube Sales
         * 
         * Merchant Trades:
         * 
         * Berries Buys
         * Berries Sells
         * Cube Sales
         * 
         *///@formatter:on

        makeTrades().forEach((p, t) -> VillagerTrades.TRADES.put(p, t));
    }

    public static Map<VillagerProfession, Int2ObjectMap<ItemListing[]>> makeTrades()
    {
        return Util.make(Maps.newHashMap(), (p_35633_) -> {//@formatter:off
                p_35633_.put(HEALER, toIntMap(ImmutableMap.of(
                        1, join
                        (
                            getReviveSale()
                        ),
                        2, join
                        (
                            getVitaminSales()
                        ),
                        3, join
                        (
                            getBerrySales(),
                            getBerryBuys()
                        ),
                        4, join
                        (
                        ), 
                        5, join
                        (
                        ))
                    ));
                p_35633_.put(PROFESSOR, toIntMap(ImmutableMap.of(
                        1, join
                        (
                            getPokedexSale()
                        ),
                        2, join
                        (
                            getExpShareSale()
                        ),
                        3, join
                        (
                            getBagSale()
                        ),
                        4, join
                        (
                            getCubeSales()
                        ), 
                        5, join
                        (
                        ))
                    ));
                p_35633_.put(MERCHANT, toIntMap(ImmutableMap.of(
                        1, join
                        (
                            getBerryBuys()
                        ),
                        2, join
                        ( 
                            getCubeSales()
                        ),
                        3, join
                        (
                            getBerrySales()
                        ),
                        4, join
                        (
                        ), 
                        5, join
                        (
                        ))
                    ));
            });//@formatter:on
    }

    private static Int2ObjectMap<ItemListing[]> toIntMap(ImmutableMap<Integer, ItemListing[]> p_35631_)
    {
        return new Int2ObjectOpenHashMap<>(p_35631_);
    }

    static ItemListing[] getBerrySales()
    {
        List<ItemListing> list = Lists.newArrayList();
        BerryManager.berryItems.forEach((index, item) -> {
            list.add(new ItemsForEmeralds(item, 4, 16, 16, 5));
        });
        return list.toArray(new ItemListing[list.size()]);
    }

    static ItemListing[] getBerryBuys()
    {
        List<ItemListing> list = Lists.newArrayList();
        BerryManager.berryItems.forEach((index, item) -> {
            list.add(new EmeraldForItems(item, 8, 16, 1));
        });
        return list.toArray(new ItemListing[list.size()]);
    }

    static ItemListing[] getVitaminSales()
    {
        List<ItemListing> list = Lists.newArrayList();
        for (String type : ItemVitamin.vitamins)
        {
            final ItemStack sell = PokecubeItems.getStack(new ResourceLocation(PokecubeMod.ID, "vitamin_" + type));
            list.add(new ItemsForEmeralds(sell, 16, 3, 64, 5));
        }
        return list.toArray(new ItemListing[list.size()]);
    }

    static ItemListing[] getCubeSales()
    {
        List<ItemListing> list = Lists.newArrayList();

        Item sell = PokecubeItems.getEmptyCube(new ResourceLocation("pokecube:pokecube"));
        list.add(new ItemsForEmeralds(sell, 4, 16, 64, 5));

        sell = PokecubeItems.getEmptyCube(new ResourceLocation("pokecube:greatcube"));
        list.add(new ItemsForEmeralds(sell, 4, 8, 64, 5));

        sell = PokecubeItems.getEmptyCube(new ResourceLocation("pokecube:ultracube"));
        list.add(new ItemsForEmeralds(sell, 4, 4, 64, 5));

        return list.toArray(new ItemListing[list.size()]);
    }

    static ItemListing[] getExpShareSale()
    {
        List<ItemListing> list = Lists.newArrayList();
        final ItemStack sell = new ItemStack(PokecubeAdv.EXPSHARE.get());
        list.add(new ItemsForEmeralds(sell, 16, 1, 16, 5));
        return list.toArray(new ItemListing[list.size()]);
    }

    static ItemListing[] getBagSale()
    {
        List<ItemListing> list = Lists.newArrayList();
        final ItemStack sell = new ItemStack(PokecubeAdv.BAG.get());
        list.add(new ItemsForEmeralds(sell, 16, 1, 2, 5));
        return list.toArray(new ItemListing[list.size()]);
    }

    static ItemListing[] getPokedexSale()
    {
        List<ItemListing> list = Lists.newArrayList();
        final ItemStack sell = new ItemStack(PokecubeItems.POKEDEX.get());
        list.add(new ItemsForEmeralds(sell, 16, 1, 1, 10));
        return list.toArray(new ItemListing[list.size()]);
    }

    static ItemListing[] getReviveSale()
    {
        List<ItemListing> list = Lists.newArrayList();
        final ItemStack sell = new ItemStack(PokecubeItems.REVIVE.get());
        list.add(new ItemsForEmeralds(sell, 4, 8, 64, 5));
        return list.toArray(new ItemListing[list.size()]);
    }

    static ItemListing[] join(ItemListing[]... listings)
    {
        List<ItemListing> list = Lists.newArrayList();
        for (ItemListing[] listing : listings) for (ItemListing l : listing) list.add(l);
        System.out.println(list);
        return list.toArray(new ItemListing[list.size()]);
    }

    static class EmeraldForItems implements VillagerTrades.ItemListing
    {
        private final Item item;
        private final int cost;
        private final int maxUses;
        private final int villagerXp;
        private final float priceMultiplier;

        public EmeraldForItems(ItemLike p_35657_, int cost, int uses, int exp)
        {
            this.item = p_35657_.asItem();
            this.cost = cost;
            this.maxUses = uses;
            this.villagerXp = exp;
            this.priceMultiplier = 0.05F;
        }

        public MerchantOffer getOffer(Entity p_35662_, Random p_35663_)
        {
            ItemStack itemstack = new ItemStack(this.item, this.cost);
            return new MerchantOffer(itemstack, new ItemStack(Items.EMERALD), this.maxUses, this.villagerXp,
                    this.priceMultiplier);
        }
    }

    static class ItemsForEmeralds implements VillagerTrades.ItemListing
    {
        private final ItemStack itemStack;
        private final int emeraldCost;
        private final int numberOfItems;
        private final int maxUses;
        private final int villagerXp;
        private final float priceMultiplier;

        public ItemsForEmeralds(Block p_35765_, int p_35766_, int p_35767_, int p_35768_, int p_35769_)
        {
            this(new ItemStack(p_35765_), p_35766_, p_35767_, p_35768_, p_35769_);
        }

        public ItemsForEmeralds(Item p_35741_, int p_35742_, int p_35743_, int p_35744_)
        {
            this(new ItemStack(p_35741_), p_35742_, p_35743_, 12, p_35744_);
        }

        public ItemsForEmeralds(Item p_35746_, int p_35747_, int p_35748_, int p_35749_, int p_35750_)
        {
            this(new ItemStack(p_35746_), p_35747_, p_35748_, p_35749_, p_35750_);
        }

        public ItemsForEmeralds(ItemStack p_35752_, int p_35753_, int p_35754_, int p_35755_, int p_35756_)
        {
            this(p_35752_, p_35753_, p_35754_, p_35755_, p_35756_, 0.05F);
        }

        public ItemsForEmeralds(ItemStack stack, int emeralds, int number, int uses, int exp, float multiplier)
        {
            this.itemStack = stack;
            this.emeraldCost = emeralds;
            this.numberOfItems = number;
            this.maxUses = uses;
            this.villagerXp = exp;
            this.priceMultiplier = multiplier;
        }

        public MerchantOffer getOffer(Entity p_35771_, Random p_35772_)
        {
            return new MerchantOffer(new ItemStack(Items.EMERALD, this.emeraldCost),
                    new ItemStack(this.itemStack.getItem(), this.numberOfItems), this.maxUses, this.villagerXp,
                    this.priceMultiplier);
        }
    }
}
