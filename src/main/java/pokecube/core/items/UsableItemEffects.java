package pokecube.core.items;

import java.util.Map;

import com.google.common.collect.Maps;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import pokecube.core.PokecubeItems;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemobUseable;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.pokemob.moves.MovePacket;
import pokecube.core.items.berries.BerryManager;
import pokecube.core.items.berries.ItemBerry;
import pokecube.core.items.vitamins.ItemCandy;
import pokecube.core.items.vitamins.ItemVitamin;
import pokecube.core.utils.Tools;

public class UsableItemEffects
{
    public abstract static class BaseUseable implements IPokemobUseable, ICapabilityProvider
    {
        private final LazyOptional<IPokemobUseable> holder = LazyOptional.of(() -> this);

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side)
        {
            return UsableItemEffects.USABLEITEM_CAP.orEmpty(cap, this.holder);
        }

    }

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
        public ActionResult<ItemStack> onTick(IPokemob pokemob, ItemStack stack)
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
        public ActionResult<ItemStack> onUse(IPokemob pokemob, ItemStack stack, LivingEntity user)
        {
            final LivingEntity mob = pokemob.getEntity();
            final float health = pokemob.getHealth();
            if ((int) health <= 0) return new ActionResult<>(ActionResultType.FAIL, stack);
            final float maxHealth = pokemob.getMaxHealth();
            if (user == mob) if (health >= maxHealth / 3) return new ActionResult<>(ActionResultType.FAIL, stack);
            if (health + 20 < maxHealth) pokemob.setHealth(health + 20);
            else pokemob.setHealth(maxHealth);
            boolean useStack = true;
            if (user instanceof PlayerEntity && ((PlayerEntity) user).abilities.isCreativeMode) useStack = false;
            if (useStack) stack.split(1);
            return new ActionResult<>(ActionResultType.SUCCESS, stack);
        }

    }

    public static class BerryUsable extends BaseUseable
    {
        public static interface BerryEffect extends IPokemobUseable
        {
        }

        public static Int2ObjectArrayMap<BerryEffect> effects = new Int2ObjectArrayMap<>();

        /**
         * @param pokemob
         * @param stack
         * @return
         */
        @Override
        public ActionResult<ItemStack> onMoveTick(IPokemob pokemob, ItemStack stack, MovePacket moveuse)
        {
            if (stack.getItem() instanceof ItemBerry)
            {
                final int berryId = ((ItemBerry) stack.getItem()).type.index;
                if (!BerryManager.berryNames.containsKey(berryId)) return new ActionResult<>(ActionResultType.FAIL,
                        stack);
                final BerryEffect effect = BerryUsable.effects.get(berryId);
                if (effect != null) return effect.onMoveTick(pokemob, stack, moveuse);
            }
            return new ActionResult<>(ActionResultType.FAIL, stack);
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
        public ActionResult<ItemStack> onTick(IPokemob pokemob, ItemStack stack)
        {
            if (stack.getItem() instanceof ItemBerry)
            {
                final int berryId = ((ItemBerry) stack.getItem()).type.index;
                if (!BerryManager.berryNames.containsKey(berryId)) return new ActionResult<>(ActionResultType.FAIL,
                        stack);
                final BerryEffect effect = BerryUsable.effects.get(berryId);
                if (effect != null) return effect.onTick(pokemob, stack);
            }
            return new ActionResult<>(ActionResultType.FAIL, stack);
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
        public ActionResult<ItemStack> onUse(IPokemob pokemob, ItemStack stack, LivingEntity user)
        {
            if (stack.getItem() instanceof ItemBerry)
            {
                final int berryId = ((ItemBerry) stack.getItem()).type.index;
                if (!BerryManager.berryNames.containsKey(berryId)) return new ActionResult<>(ActionResultType.FAIL,
                        stack);
                final BerryEffect effect = BerryUsable.effects.get(berryId);
                if (effect != null) return effect.onUse(pokemob, stack, user);
            }
            return new ActionResult<>(ActionResultType.FAIL, stack);
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
        public ActionResult<ItemStack> onUse(IPokemob pokemob, ItemStack stack, LivingEntity user)
        {
            if (user != pokemob.getEntity() && user != pokemob.getOwner()) return new ActionResult<>(
                    ActionResultType.FAIL, stack);
            final boolean used = true;
            final int xp = Tools.levelToXp(pokemob.getExperienceMode(), pokemob.getLevel() + (PokecubeItems.isValid(
                    stack) ? 1 : -1));
            pokemob.setExp(xp, true);
            if (used)
            {
                stack.split(1);
                PokecubeItems.deValidate(stack);
            }
            stack.setTag(null);
            return new ActionResult<>(used ? ActionResultType.SUCCESS : ActionResultType.FAIL, stack);
        }
    }

    public static class PotionUse extends BaseUseable
    {
        @Override
        public ActionResult<ItemStack> onUse(IPokemob pokemob, ItemStack stack, LivingEntity user)
        {
            final MobEntity mob = pokemob.getEntity();
            boolean applied = false;
            for (final EffectInstance potioneffect : PotionUtils.getEffectsFromStack(stack))
            {
                if (potioneffect.getPotion().isInstant()) potioneffect.getPotion().affectEntity(mob, mob, mob,
                        potioneffect.getAmplifier(), 1.0D);
                else mob.addPotionEffect(new EffectInstance(potioneffect));
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
                return new ActionResult<>(ActionResultType.SUCCESS, stack);
            }
            return new ActionResult<>(ActionResultType.FAIL, stack);
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
        public ActionResult<ItemStack> onUse(IPokemob pokemob, ItemStack stack, LivingEntity user)
        {
            if (user != pokemob.getEntity() && user != pokemob.getOwner()) return new ActionResult<>(
                    ActionResultType.FAIL, stack);
            final boolean used = ItemTM.applyEffect(pokemob.getEntity(), stack);
            if (used) stack.split(1);
            return new ActionResult<>(used ? ActionResultType.SUCCESS : ActionResultType.FAIL, stack);
        }
    }

    public static class VitaminUsable extends BaseUseable
    {
        public static interface VitaminEffect extends IPokemobUseable
        {
        }

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
        public ActionResult<ItemStack> onUse(IPokemob pokemob, ItemStack stack, LivingEntity user)
        {
            if (user != pokemob.getEntity() && user != pokemob.getOwner() && !(stack.getItem() instanceof ItemVitamin))
                return new ActionResult<>(ActionResultType.FAIL, stack);
            final ItemVitamin vitamin = (ItemVitamin) stack.getItem();
            ActionResult<ItemStack> result = null;
            final VitaminEffect effect = VitaminUsable.effects.get(vitamin.type);
            if (effect != null) result = effect.onUse(pokemob, stack, user);
            else return new ActionResult<>(ActionResultType.FAIL, stack);
            if (result.getType() == ActionResultType.SUCCESS) stack.split(1);
            return result;
        }
    }

    @CapabilityInject(IPokemobUseable.class)
    public static final Capability<IPokemobUseable> USABLEITEM_CAP = null;

    public static final ResourceLocation USABLE = new ResourceLocation(PokecubeMod.ID, "usables");

    /** 1.12 this needs to be ItemStack instead of item. */
    public static void registerCapabilities(AttachCapabilitiesEvent<ItemStack> event)
    {
        if (event.getCapabilities().containsKey(UsableItemEffects.USABLE)) return;
        final Item item = event.getObject().getItem();
        if (item instanceof ItemBerry) event.addCapability(UsableItemEffects.USABLE, new BerryUsable());
        if (item instanceof ItemTM) event.addCapability(UsableItemEffects.USABLE, new TMUsable());
        if (item instanceof ItemCandy) event.addCapability(UsableItemEffects.USABLE, new CandyUsable());
        if (item instanceof ItemVitamin) event.addCapability(UsableItemEffects.USABLE, new VitaminUsable());
        if (item instanceof PotionItem) event.addCapability(UsableItemEffects.USABLE, new PotionUse());
        if (item == PokecubeItems.BERRYJUICE) event.addCapability(UsableItemEffects.USABLE, new BerryJuice());
    }

}
