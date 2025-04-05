package pokecube.core.items;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.google.common.collect.Maps;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionContents;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.PokemobCaps.UsableItem;
import pokecube.api.items.IPokemobUseable;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.api.utils.Tools;
import pokecube.core.PokecubeItems;
import pokecube.core.impl.PokecubeMod;
import pokecube.core.items.berries.BerryManager;
import pokecube.core.items.berries.ItemBerry;
import pokecube.core.items.vitamins.ItemCandy;
import pokecube.core.items.vitamins.ItemVitamin;

public class UsableItemEffects
{
    public abstract static class BaseUseable implements IPokemobUseable
    {}

    public static class BerryJuice extends BaseUseable
    {

        /**
         * Called every tick while this item is the active held item for the
         * pokemob.
         *
         * @param pokemob
         * @param stack
         * @return something happened
         */
        @Override
        public InteractionResultHolder<ItemStack> onTick(final IPokemob pokemob, final ItemStack stack)
        {
            return this.onUse(pokemob, stack, pokemob.getEntity());
        }

        /**
         * Called when this item is "used". Normally this means via right
         * clicking the pokemob with the itemstack. It can also be called via
         * onTick or onMoveTick, in which case user will be pokemob.getEntity()
         *
         * @param user
         * @param pokemob
         * @param stack
         * @return something happened
         */
        @Override
        public InteractionResultHolder<ItemStack> onUse(final IPokemob pokemob, final ItemStack stack,
                final LivingEntity user)
        {
            final LivingEntity mob = pokemob.getEntity();
            final float health = pokemob.getHealth();
            if ((int) health <= 0) return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
            final float maxHealth = pokemob.getMaxHealth();
            if (user == mob)
                if (health >= maxHealth / 3) return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
            if (health + 20 < maxHealth) pokemob.setHealth(health + 20);
            else pokemob.setHealth(maxHealth);
            boolean useStack = true;
            if (user instanceof Player player && player.getAbilities().instabuild) useStack = false;
            if (useStack) stack.split(1);
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
        }

    }

    public static class BerryUsable extends BaseUseable
    {
        public static interface BerryEffect extends IPokemobUseable
        {}

        public static Int2ObjectArrayMap<BerryEffect> effects = new Int2ObjectArrayMap<>();

        /**
         * @param pokemob
         * @param stack
         * @return
         */
        @Override
        public InteractionResultHolder<ItemStack> onMoveTick(final IPokemob pokemob, final ItemStack stack,
                final MoveApplication moveuse, boolean pre)
        {
            if (stack.getItem() instanceof ItemBerry berry)
            {
                final int berryId = berry.type.index;
                if (!BerryManager.berryNames.containsKey(berryId))
                    return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
                final BerryEffect effect = BerryUsable.effects.get(berryId);
                if (effect != null) return effect.onMoveTick(pokemob, stack, moveuse, pre);
            }
            return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
        }

        /**
         * Called every tick while this item is the active held item for the
         * pokemob.
         *
         * @param pokemob
         * @param stack
         * @return something happened
         */
        @Override
        public InteractionResultHolder<ItemStack> onTick(final IPokemob pokemob, final ItemStack stack)
        {
            if (stack.getItem() instanceof ItemBerry berry)
            {
                final int berryId = berry.type.index;
                if (!BerryManager.berryNames.containsKey(berryId))
                    return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
                final BerryEffect effect = BerryUsable.effects.get(berryId);
                if (effect != null) return effect.onTick(pokemob, stack);
            }
            return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
        }

        /**
         * Called when this item is "used". Normally this means via right
         * clicking the pokemob with the itemstack. It can also be called via
         * onTick or onMoveTick, in which case user will be pokemob.getEntity()
         *
         * @param user
         * @param pokemob
         * @param stack
         * @return something happened
         */
        @Override
        public InteractionResultHolder<ItemStack> onUse(final IPokemob pokemob, final ItemStack stack,
                final LivingEntity user)
        {
            if (stack.getItem() instanceof ItemBerry berry)
            {
                final int berryId = berry.type.index;
                if (!BerryManager.berryNames.containsKey(berryId))
                    return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
                final BerryEffect effect = BerryUsable.effects.get(berryId);
                if (effect != null) return effect.onUse(pokemob, stack, user);
            }
            return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
        }
    }

    public static class CandyUsable extends BaseUseable
    {

        /**
         * Called when this item is "used". Normally this means via right
         * clicking the pokemob with the itemstack. It can also be called via
         * onTick or onMoveTick, in which case user will be pokemob.getEntity()
         *
         * @param user
         * @param pokemob
         * @param stack
         * @return something happened
         */
        @Override
        public InteractionResultHolder<ItemStack> onUse(final IPokemob pokemob, final ItemStack stack,
                final LivingEntity user)
        {
            if (user != pokemob.getEntity() && user != pokemob.getOwner())
                return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
            final boolean used = true;
            final int xp = Tools.levelToXp(pokemob.getExperienceMode(),
                    pokemob.getLevel() + (PokecubeItems.isValid(stack) ? 1 : -1));
            pokemob.setExp(xp, true);
            if (used)
            {
                stack.split(1);
                PokecubeItems.deValidate(stack);
            }
            stack.remove(DataComponents.CUSTOM_DATA);
            return new InteractionResultHolder<>(used ? InteractionResult.SUCCESS : InteractionResult.FAIL, stack);
        }
    }

    public static class PotionUse extends BaseUseable
    {
        @Override
        public InteractionResultHolder<ItemStack> onUse(final IPokemob pokemob, ItemStack stack,
                final LivingEntity user)
        {
            final Mob mob = pokemob.getEntity();
            boolean applied = false;
            PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
            for (final MobEffectInstance potioneffect : contents.customEffects())
            {
                if (potioneffect.getEffect().value().isInstantenous())
                    potioneffect.getEffect().value().applyInstantenousEffect(mob, mob, mob, potioneffect.getAmplifier(), 1.0D);
                else mob.addEffect(new MobEffectInstance(potioneffect));
                applied = true;
            }
            if (applied)
            {
                stack.shrink(1);
                if (stack.isEmpty()) stack = new ItemStack(Items.GLASS_BOTTLE);
                else
                {
                    // Add to inventory or drop
                }
                return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
            }
            return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
        }

    }

    public static class TMUsable extends BaseUseable
    {
        /**
         * Called when this item is "used". Normally this means via right
         * clicking the pokemob with the itemstack. It can also be called via
         * onTick or onMoveTick, in which case user will be pokemob.getEntity()
         *
         * @param user
         * @param pokemob
         * @param stack
         * @return something happened
         */
        @Override
        public InteractionResultHolder<ItemStack> onUse(final IPokemob pokemob, final ItemStack stack,
                final LivingEntity user)
        {
            if (user != pokemob.getEntity() && user != pokemob.getOwner())
                return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
            final boolean used = ItemTM.applyEffect(pokemob.getEntity(), stack);
            if (used) stack.split(1);
            return new InteractionResultHolder<>(used ? InteractionResult.SUCCESS : InteractionResult.FAIL, stack);
        }
    }

    public static class VitaminUsable extends BaseUseable
    {
        public static interface VitaminEffect extends IPokemobUseable
        {}

        public static Map<String, VitaminEffect> effects = Maps.newHashMap();

        /**
         * Called when this item is "used". Normally this means via right
         * clicking the pokemob with the itemstack. It can also be called via
         * onTick or onMoveTick, in which case user will be pokemob.getEntity()
         *
         * @param user
         * @param pokemob
         * @param stack
         * @return something happened
         */
        @Override
        public InteractionResultHolder<ItemStack> onUse(final IPokemob pokemob, final ItemStack stack,
                final LivingEntity user)
        {
            if (user != pokemob.getEntity() && user != pokemob.getOwner() && !(stack.getItem() instanceof ItemVitamin))
                return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
            final ItemVitamin vitamin = (ItemVitamin) stack.getItem();
            InteractionResultHolder<ItemStack> result = null;
            final VitaminEffect effect = VitaminUsable.effects.get(vitamin.type);
            if (effect != null) result = effect.onUse(pokemob, stack, user);
            else return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
            if (result.getResult() == InteractionResult.SUCCESS) stack.split(1);
            return result;
        }
    }

    public static final ResourceLocation USABLE = ResourceLocation.fromNamespaceAndPath(PokecubeMod.ID, "usable");
    public static Map<Predicate<Item>, Supplier<IPokemobUseable>> REGISTRY = new HashMap<>();

    static
    {
        REGISTRY.put(i -> i instanceof ItemBerry, BerryUsable::new);
        REGISTRY.put(i -> i instanceof ItemTM, TMUsable::new);
        REGISTRY.put(i -> i instanceof ItemCandy, CandyUsable::new);
        REGISTRY.put(i -> i instanceof ItemBerry, BerryUsable::new);
        REGISTRY.put(i -> i instanceof ItemVitamin, VitaminUsable::new);
        REGISTRY.put(i -> i instanceof PotionItem, PotionUse::new);
        REGISTRY.put(i -> i == PokecubeItems.BERRYJUICE.get(), BerryJuice::new);
    }

    /** 1.12 this needs to be ItemStack instead of item. */
    public static void modifyComponents(final ModifyDefaultComponentsEvent event)
    {
        event.getAllItems().forEach(item -> {
            for (var pair : UsableItemEffects.REGISTRY.entrySet())
            {
                if (pair.getKey().test(item))
                {
                    var effect = pair.getValue().get();
                    UsableItem data = new UsableItem(UsableItemEffects.USABLE, effect);
                    event.modify(item, b -> b.set(PokemobCaps.USABLE_DATA.get(), data));
                    return;
                }
            }
        });
    }

}
