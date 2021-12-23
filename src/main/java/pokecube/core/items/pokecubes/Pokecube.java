package pokecube.core.items.pokecubes;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.google.common.collect.Sets;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.FakePlayer;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.ai.tasks.idle.HungerTask;
import pokecube.core.database.PokedexEntry;
import pokecube.core.handlers.Config;
import pokecube.core.handlers.playerdata.PlayerPokemobCache;
import pokecube.core.interfaces.IPokecube;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.Stats;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.moves.MovesUtils;
import pokecube.core.utils.AITools;
import pokecube.core.utils.PermNodes;
import pokecube.core.utils.Permissions;
import pokecube.core.utils.TagNames;
import pokecube.core.utils.Tools;
import thut.api.item.ItemList;
import thut.api.maths.Vector3;
import thut.api.maths.vecmath.Vector3f;
import thut.core.common.ThutCore;
import thut.core.common.commands.CommandTools;

public class Pokecube extends Item implements IPokecube
{
    public static final Set<ResourceLocation> snagblacklist = Sets.newHashSet();

    private static final Predicate<Entity> capturable = t -> {
        if (Pokecube.snagblacklist.contains(t.getType().getRegistryName())) return false;
        return true;
    };

    @OnlyIn(Dist.CLIENT)
    public static void displayInformation(final CompoundTag nbt, final List<Component> list)
    {
        final boolean flag2 = nbt.getBoolean("Flames");

        if (flag2) list.add(new TranslatableComponent("item.pokecube.flames"));

        final boolean flag3 = nbt.getBoolean("Bubbles");

        if (flag3) list.add(new TranslatableComponent("item.pokecube.bubbles"));

        final boolean flag4 = nbt.getBoolean("Leaves");

        if (flag4) list.add(new TranslatableComponent("item.pokecube.leaves"));

        final boolean flag5 = nbt.contains("dye");

        if (flag5) list.add(new TranslatableComponent(DyeColor.byId(nbt.getInt("dye")).getName()));
    }

    public Pokecube(final Properties properties)
    {
        super(properties);
    }

    /**
     * allows items to add custom lines of information to the mouseover
     * description
     */
    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(final ItemStack item, @Nullable final Level world, final List<Component> list,
            final TooltipFlag advanced)
    {
        if (PokecubeManager.isFilled(item))
        {
            final Entity mob = PokecubeManager.itemToMob(item, world);
            if (mob == null)
            {
                list.add(new TranslatableComponent("pokecube.filled.error"));
                return;
            }
            final IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
            if (pokemob == null) return;
            list.add(pokemob.getDisplayName());

            final CompoundTag pokeTag = item.getTag().getCompound(TagNames.POKEMOB);

            final float health = pokeTag.getFloat("Health");
            final float maxHealth = pokemob.getStat(Stats.HP, false);
            final int lvlexp = Tools.levelToXp(pokemob.getExperienceMode(), pokemob.getLevel());
            final int exp = pokemob.getExp() - lvlexp;
            final int neededexp = Tools.levelToXp(pokemob.getExperienceMode(), pokemob.getLevel() + 1) - lvlexp;
            list.add(new TranslatableComponent("pokecube.tooltip.level", pokemob.getLevel()));
            list.add(new TranslatableComponent("pokecube.tooltip.health", health, maxHealth));
            list.add(new TranslatableComponent("pokecube.tooltip.xp", exp, neededexp));

            if (Screen.hasShiftDown())
            {
                String arg = "";
                for (final String s : pokemob.getMoves())
                    if (s != null) arg += I18n.get(MovesUtils.getUnlocalizedMove(s)) + ", ";
                if (arg.endsWith(", ")) arg = arg.substring(0, arg.length() - 2);
                list.add(new TranslatableComponent("pokecube.tooltip.moves", arg));
                arg = "";
                for (final Byte b : pokemob.getIVs()) arg += b + ", ";
                if (arg.endsWith(", ")) arg = arg.substring(0, arg.length() - 2);
                list.add(new TranslatableComponent("pokecube.tooltip.ivs", arg));
                arg = "";
                for (final Byte b : pokemob.getEVs())
                {
                    final int n = b + 128;
                    arg += n + ", ";
                }
                if (arg.endsWith(", ")) arg = arg.substring(0, arg.length() - 2);
                list.add(new TranslatableComponent("pokecube.tooltip.evs", arg));
                list.add(new TranslatableComponent("pokecube.tooltip.nature", pokemob.getNature()));
                list.add(new TranslatableComponent("pokecube.tooltip.ability", pokemob.getAbility()));
            }
            else list.add(new TranslatableComponent("pokecube.tooltip.advanced"));
        }
        else
        {
            final ResourceLocation name = item.getItem().getRegistryName();
            list.add(new TranslatableComponent("item.pokecube." + name.getPath() + ".desc"));
        }

        if (item.hasTag())
        {
            final CompoundTag CompoundNBT = PokecubeManager.getSealTag(item);
            Pokecube.displayInformation(CompoundNBT, list);
        }
    }

    @Override
    public boolean canCapture(final Entity hit, final ItemStack cube)
    {
        final ResourceLocation id = PokecubeItems.getCubeId(cube);
        if (id != null && id.getPath().equals("snag"))
        {
            if (this.getCaptureModifier(hit, id) <= 0) return false;
            return Pokecube.capturable.test(hit);
        }
        return IPokecube.super.canCapture(hit, cube);
    }

    /**
     * This function should return a new entity to replace the dropped item.
     * Returning null here will not kill the ItemEntity and will leave it to
     * function normally. Called when the item it placed in a world.
     *
     * @param world     The world object
     * @param location  The ItemEntity object, useful for getting the position
     *                  of the entity
     * @param itemstack The current item stack
     * @return A new Entity object to spawn or null
     */
    @Override
    public Entity createEntity(final Level world, final Entity oldItem, final ItemStack itemstack)
    {
        if (this.hasCustomEntity(itemstack))
        {
            final FakePlayer player = PokecubeMod.getFakePlayer(world);
            final EntityPokecube cube = new EntityPokecube(EntityPokecube.TYPE, world);
            cube.shootingEntity = player;
            cube.shooter = player.getUUID();
            cube.setItem(itemstack);
            cube.setDeltaMovement(0, 0, 0);
            cube.shootingEntity = null;
            cube.shooter = null;
            Vector3.getNewVector().set(oldItem).moveEntity(cube);
            cube.setNoCollisionRelease();
            cube.targetLocation.clear();
            return cube;
        }
        return null;
    }

    @Override
    public double getCaptureModifier(final IPokemob mob, final ResourceLocation id)
    {
        if (IPokecube.BEHAVIORS.containsKey(id)) return IPokecube.BEHAVIORS.getValue(id).getCaptureModifier(mob);
        return 0;
    }

    @Override
    public double getCaptureModifier(final Entity mob, final ResourceLocation pokecubeId)
    {
        if (pokecubeId.getPath().equals("snag"))
        {
            if (mob.isInvulnerable()) return 0;
            return 1;
        }
        return IPokecube.super.getCaptureModifier(mob, pokecubeId);
    }

    @Override
    public int getItemStackLimit(ItemStack stack)
    {
        return PokecubeManager.isFilled(stack) ? 1 : 64;
    }

    @Override
    public boolean isDamaged(final ItemStack stack)
    {
        return PokecubeManager.isFilled(stack);
    }

    @Override
    public int getDamage(final ItemStack stack)
    {
        if (stack.hasTag() && stack.getTag().contains("CHP"))
        {
            final float chp = stack.getTag().getFloat("CHP");
            final float mhp = stack.getTag().getFloat("MHP");
            return (int) (255 - 255 * (chp) / mhp);
        }
        return super.getDamage(stack);
    }

    @Override
    public int getMaxDamage(final ItemStack stack)
    {
        return 255;
    }

    @Override
    public boolean isValidRepairItem(ItemStack cube, ItemStack item)
    {
        if (PokecubeManager.isFilled(cube))
        {
            return ItemList.is(HungerTask.FOODTAG, item);
        }
        return super.isValidRepairItem(cube, item);
    }

    @Override
    /**
     * returns the action that specifies what animation to play when the items
     * is being used
     */
    public UseAnim getUseAnimation(final ItemStack stack)
    {
        return UseAnim.BOW;
    }

    @Override
    /** How long it takes to use or consume an item */
    public int getUseDuration(final ItemStack stack)
    {
        return 2000;
    }

    /**
     * Determines if this Item has a special entity for when they are in the
     * world. Is called when a ItemEntity is spawned in the world, if true and
     * Item#createCustomEntity returns non null, the ItemEntity will be
     * destroyed and the new Entity will be added to the world.
     *
     * @param stack The current item stack
     * @return True of the item has a custom entity, If true,
     *         Item#createCustomEntity will be called
     */
    @Override
    public boolean hasCustomEntity(final ItemStack stack)
    {
        return PokecubeManager.isFilled(stack);
    }

    // Pokeseal stuff

    @Override
    public InteractionResultHolder<ItemStack> use(final Level world, final Player player, final InteractionHand hand)
    {
        player.startUsingItem(hand);
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, player.getItemInHand(hand));
    }

    @Override
    /**
     * Called when the player stops using an Item (stops holding the right mouse
     * button).
     */
    public void releaseUsing(final ItemStack stack, final Level worldIn, final LivingEntity MobEntity,
            final int timeLeft)
    {
        if (MobEntity instanceof Player && !worldIn.isClientSide)
        {
            final Player player = (Player) MobEntity;
            final Predicate<Entity> selector = input -> {
                final IPokemob pokemob = CapabilityPokemob.getPokemobFor(input);
                if (!AITools.validTargets.test(input)) return false;
                if (pokemob == null) return true;
                return pokemob.getOwner() != player;
            };
            Entity target = Tools.getPointedEntity(player, 32, selector);
            final Vector3 direction = Vector3.getNewVector().set(player.getViewVector(0));
            final Vector3 targetLocation = Tools.getPointedLocation(player, 32);
            if (target instanceof EntityPokecube) target = null;
            final IPokemob targetMob = CapabilityPokemob.getPokemobFor(target);
            if (targetMob != null) if (targetMob.getOwner() == MobEntity) target = null;
            final int dt = this.getUseDuration(stack) - timeLeft;
            final boolean filled = PokecubeManager.isFilled(stack);
            if (!filled && target instanceof LivingEntity
                    && this.getCaptureModifier(target, PokecubeItems.getCubeId(stack)) == 0)
                target = null;
            boolean used = false;
            final boolean filledOrSneak = filled || player.isShiftKeyDown() || dt > 5;
            if (target != null && EntityPokecubeBase.SEEKING)
                used = this.throwPokecubeAt(worldIn, player, stack, targetLocation, target) != null;
            else if (filledOrSneak || !EntityPokecubeBase.SEEKING)
            {
                float power = (this.getUseDuration(stack) - timeLeft) / (float) 100;
                power = Math.min(1, power);
                used = this.throwPokecube(worldIn, player, stack, direction, power) != null;
            }
            else
            {
                CommandTools.sendError(player, "pokecube.badaim");
                used = false;
            }
            if (used)
            {
                if (PokecubeManager.isFilled(stack) || !player.isCreative()) stack.split(1);
                if (stack.isEmpty()) for (int i = 0; i < player.getInventory().getContainerSize(); i++)
                    if (player.getInventory().getItem(i) == stack)
                {
                    player.getInventory().setItem(i, ItemStack.EMPTY);
                    break;
                }
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public boolean requiresMultipleRenderPasses()
    {
        return true;
    }

    /**
     * If this function returns true (or the item is damageable), the
     * ItemStack's NBT tag will be sent to the client.
     */
    @Override
    public boolean shouldOverrideMultiplayerNbt()
    {
        return true;
    }

    @Override
    public EntityPokecubeBase throwPokecube(final Level world, final LivingEntity thrower, final ItemStack cube,
            final Vector3 direction, final float power)
    {
        EntityPokecube entity = null;
        final ResourceLocation id = PokecubeItems.getCubeId(cube);
        if (id == null || !IPokecube.BEHAVIORS.containsKey(id)) return null;
        final ItemStack stack = cube.copy();
        final boolean hasMob = PokecubeManager.isFilled(stack);
        final Config config = PokecubeCore.getConfig();
        // Check permissions
        if (hasMob && (config.permsSendOut || config.permsSendOutSpecific) && thrower instanceof ServerPlayer player)
        {
            final PokedexEntry entry = PokecubeManager.getPokedexEntry(stack);
            if (config.permsSendOut && !PermNodes.getBooleanPerm(player, Permissions.SENDOUTPOKEMOB)) return null;
            if (config.permsSendOutSpecific
                    && !PermNodes.getBooleanPerm(player, Permissions.SENDOUTSPECIFIC.get(entry)))
                return null;
        }
        stack.setCount(1);
        entity = new EntityPokecube(EntityPokecube.TYPE, world);
        entity.shootingEntity = thrower.isShiftKeyDown() ? null : thrower;
        if (thrower.isShiftKeyDown()) entity.setNoCollisionRelease();
        else entity.autoRelease = config.pokecubeAutoSendOutDelay;
        entity.shooter = thrower.getUUID();
        entity.setItem(stack);

        final Vector3 temp = Vector3.getNewVector().set(thrower).addTo(0, thrower.getEyeHeight(), 0);
        if (thrower instanceof ServerPlayer && !(thrower instanceof FakePlayer))
        {
            final ServerPlayer player = (ServerPlayer) thrower;
            final InteractionHand hand = player.getUsedItemHand();
            final Vec3 tmp = thrower.getLookAngle();
            final Vector3f look = new Vector3f((float) tmp.x, (float) tmp.y, (float) tmp.z);
            final Vector3f shift = new Vector3f();
            shift.cross(look, new Vector3f(0, 1, 0));
            shift.scale(player.getBbWidth() / 2);
            switch (hand)
            {
            case MAIN_HAND:
                break;
            case OFF_HAND:
                shift.negate();
                break;
            default:
                break;
            }
            temp.addTo(shift.x, shift.y, shift.z);
        }

        temp.moveEntity(entity);
        entity.shoot(direction.norm(), power * 10);
        entity.seeking = false;
        entity.targetEntity = null;
        entity.targetLocation.clear();
        if (hasMob && !thrower.isShiftKeyDown()) entity.targetLocation.y = -1;
        if (!world.isClientSide)
        {
            thrower.playSound(SoundEvents.EGG_THROW, 0.5F, 0.4F / (ThutCore.newRandom().nextFloat() * 0.4F + 0.8F));
            world.addFreshEntity(entity);
            if (hasMob && thrower instanceof Player) PlayerPokemobCache.UpdateCache(stack, false, false);
        }
        return entity;
    }

    @Override
    public EntityPokecubeBase throwPokecubeAt(final Level world, final LivingEntity thrower, final ItemStack cube,
            Vector3 targetLocation, Entity target)
    {
        EntityPokecube entity = null;
        final ResourceLocation id = PokecubeItems.getCubeId(cube);
        if (id == null || !IPokecube.BEHAVIORS.containsKey(id)) return null;
        final ItemStack stack = cube.copy();
        stack.setCount(1);
        entity = new EntityPokecube(EntityPokecube.TYPE, world);
        entity.shootingEntity = thrower;
        entity.shooter = thrower.getUUID();
        entity.setItem(stack);
        final boolean rightclick = target == thrower;
        if (rightclick) target = null;

        if (target instanceof LivingEntity || PokecubeManager.isFilled(cube) || thrower.isShiftKeyDown()
                || thrower instanceof FakePlayer)
        {
            if (target instanceof LivingEntity) entity.targetEntity = (LivingEntity) target;
            if (target == null && targetLocation == null && PokecubeManager.isFilled(cube))
                targetLocation = Vector3.secondAxisNeg;
            entity.targetLocation.set(targetLocation);
            final Vector3 temp = Vector3.getNewVector().set(thrower).add(0, thrower.getEyeHeight(), 0);
            temp.moveEntity(entity);
            if (thrower instanceof ServerPlayer && !(thrower instanceof FakePlayer))
            {
                final ServerPlayer player = (ServerPlayer) thrower;
                final InteractionHand hand = player.getUsedItemHand();
                final Vec3 tmp = thrower.getLookAngle();
                final Vector3f look = new Vector3f((float) tmp.x, (float) tmp.y, (float) tmp.z);
                final Vector3f shift = new Vector3f();
                shift.cross(look, new Vector3f(0, 1, 0));
                shift.scale(player.getBbWidth() / 2);
                switch (hand)
                {
                case MAIN_HAND:
                    break;
                case OFF_HAND:
                    shift.negate();
                    break;
                default:
                    break;
                }
                temp.addTo(shift.x, shift.y, shift.z);
            }
            if (thrower.isShiftKeyDown())
            {
                temp.clear().setVelocities(entity);
                entity.targetEntity = null;
                entity.targetLocation.clear();
            }
            if (!world.isClientSide)
            {
                thrower.playSound(SoundEvents.EGG_THROW, 0.5F, 0.4F / (ThutCore.newRandom().nextFloat() * 0.4F + 0.8F));
                world.addFreshEntity(entity);
                if (PokecubeManager.isFilled(stack) && thrower instanceof Player)
                    PlayerPokemobCache.UpdateCache(stack, false, false);
            }
        }
        else if (!rightclick) return null;
        return entity;
    }
}
