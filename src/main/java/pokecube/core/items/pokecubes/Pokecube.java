package pokecube.core.items.pokecubes;

import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
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
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.IPokemob.Stats;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.items.IPokecube;
import pokecube.api.utils.TagNames;
import pokecube.api.utils.Tools;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.ai.tasks.idle.HungerTask;
import pokecube.core.entity.pokecubes.EntityPokecube;
import pokecube.core.entity.pokecubes.EntityPokecubeBase;
import pokecube.core.handlers.playerdata.PlayerPokemobCache;
import pokecube.core.impl.PokecubeMod;
import pokecube.core.init.Config;
import pokecube.core.init.EntityTypes;
import pokecube.core.moves.MovesUtils;
import pokecube.core.utils.AITools;
import pokecube.core.utils.Permissions;
import thut.api.Tracker;
import thut.api.item.ItemList;
import thut.api.maths.Vector3;
import thut.api.maths.vecmath.Vec3f;
import thut.core.common.ThutCore;
import thut.core.common.commands.CommandTools;
import thut.lib.RegHelper;
import thut.lib.TComponent;

public class Pokecube extends Item implements IPokecube
{
    public static boolean renderingOverlay = false;

    private static final List<Predicate<Entity>> _blacklist = Lists.newArrayList();

    public static void clearSnagBlacklist()
    {
        _blacklist.clear();
    }

    public static void registerSnagBlacklist(String var)
    {
        if (var.startsWith("#"))
        {
            TagKey<EntityType<?>> tag = TagKey.create(Registry.ENTITY_TYPE_REGISTRY,
                    new ResourceLocation(var.replace("#", "")));
            _blacklist.add(e -> e.getType().is(tag));
        }
        else
        {
            ResourceLocation id = new ResourceLocation(var.replace("#", ""));
            _blacklist.add(e -> id.equals(RegHelper.getKey(e)));
        }
    }

    private static final Predicate<Entity> capturable = t -> {
        if (t == null) return false;
        if (_blacklist.stream().anyMatch(e -> e.test(t))) return false;
        return true;
    };

    @OnlyIn(Dist.CLIENT)
    public static void displayInformation(final CompoundTag nbt, final List<Component> list)
    {
        final boolean flag2 = nbt.getBoolean("Flames");

        if (flag2) list.add(TComponent.translatable("item.pokecube.flames").withStyle(ChatFormatting.RED));

        final boolean flag3 = nbt.getBoolean("Bubbles");

        if (flag3) list.add(TComponent.translatable("item.pokecube.bubbles").withStyle(ChatFormatting.AQUA));

        final boolean flag4 = nbt.getBoolean("Leaves");

        if (flag4) list.add(TComponent.translatable("item.pokecube.leaves").withStyle(ChatFormatting.GREEN));

        final boolean flag5 = nbt.contains("dye");

        if (flag5) list.add(TComponent.translatable(DyeColor.byId(nbt.getInt("dye")).getName()));
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
                list.add(TComponent.translatable("pokecube.filled.error"));
                return;
            }
            final IPokemob pokemob = PokemobCaps.getPokemobFor(mob);
            if (pokemob == null) return;
//            list.add(TComponent.translatable(pokemob.getDisplayName().getString(), ChatFormatting.BOLD, ChatFormatting.GOLD));
            list.add(TComponent.translatable("pokecube.tooltip.pokemob", pokemob.getDisplayName())
                    .withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD));

            final CompoundTag pokeTag = item.getTag().getCompound(TagNames.POKEMOB);

            final float health = pokeTag.getFloat("Health");
            final float maxHealth = pokemob.getStat(Stats.HP, false);
            final int lvlexp = Tools.levelToXp(pokemob.getExperienceMode(), pokemob.getLevel());
            final int exp = pokemob.getExp() - lvlexp;
            final int neededexp = Tools.levelToXp(pokemob.getExperienceMode(), pokemob.getLevel() + 1) - lvlexp;
            list.add(TComponent.translatable("pokecube.tooltip.level", pokemob.getLevel())
                    .withStyle(ChatFormatting.GRAY));
            list.add(TComponent.translatable("pokecube.tooltip.health", health, maxHealth)
                    .withStyle(ChatFormatting.GRAY));
            list.add(TComponent.translatable("pokecube.tooltip.xp", exp, neededexp).withStyle(ChatFormatting.GRAY));

            if (Screen.hasShiftDown())
            {
                String arg = "";
                for (int i = 0; i < pokemob.getMovesCount(); i++)
                {
                    String s = pokemob.getMove(i);
                    if (s != null) arg += I18n.get(MovesUtils.getUnlocalizedMove(s)) + ", ";
                }
                if (arg.endsWith(", ")) arg = arg.substring(0, arg.length() - 2);
                list.add(TComponent.translatable("pokecube.tooltip.moves", arg).withStyle(ChatFormatting.GRAY));
                arg = "";
                for (final Byte b : pokemob.getIVs()) arg += b + ", ";
                list.add(TComponent.translatable("pokecube.tooltip.nature", pokemob.getNature())
                        .withStyle(ChatFormatting.GRAY));
                list.add(TComponent.translatable("pokecube.tooltip.ability", pokemob.getAbility())
                        .withStyle(ChatFormatting.GRAY));
                if (arg.endsWith(", ")) arg = arg.substring(0, arg.length() - 2);
                list.add(TComponent.translatable("pokecube.tooltip.ivs", arg).withStyle(ChatFormatting.GRAY));
                arg = "";
                for (final Byte b : pokemob.getEVs())
                {
                    final int n = b + 128;
                    arg += n + ", ";
                }
                if (arg.endsWith(", ")) arg = arg.substring(0, arg.length() - 2);
                list.add(TComponent.translatable("pokecube.tooltip.evs", arg).withStyle(ChatFormatting.GRAY));

                byte sexe = pokemob.getEntity().getPersistentData().getByte(TagNames.SEXE);
                final String gender = sexe == IPokemob.MALE ? "\u2642" : sexe == IPokemob.FEMALE ? "\u2640" : "";
                if (!gender.isBlank())
                    if (sexe == IPokemob.MALE) list.add(TComponent.translatable("pokecube.tooltip.male"));
                if (sexe == IPokemob.FEMALE) list.add(TComponent.translatable("pokecube.tooltip.female"));
            }
            else list.add(TComponent.translatable("pokecube.tooltip.advanced"));
        }
        else
        {
            final ResourceLocation name = RegHelper.getKey(item.getItem());
            list.add(TComponent.translatable("item.pokecube." + name.getPath() + ".desc".translateEscapes()));
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
        if (id != null && id.getPath().equals("snagcube"))
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
            final EntityPokecube cube = new EntityPokecube(EntityTypes.getPokecube(), world);
            cube.shootingEntity = player;
            cube.shooter = player.getUUID();
            cube.setItem(itemstack);
            cube.setDeltaMovement(0, 0, 0);
            cube.shootingEntity = null;
            cube.shooter = null;
            new Vector3().set(oldItem).moveEntity(cube);
            cube.setNoCollisionRelease();
            cube.targetLocation.clear();
            return cube;
        }
        return null;
    }

    @Override
    public double getCaptureModifier(final IPokemob mob, final ResourceLocation id)
    {
        if (IPokecube.PokecubeBehaviour.BEHAVIORS.containsKey(id))
            return IPokecube.PokecubeBehaviour.BEHAVIORS.get(id).getCaptureModifier(mob);
        return 0;
    }

    @Override
    public double getCaptureModifier(final Entity mob, final ResourceLocation pokecubeId)
    {
        if (pokecubeId.getPath().equals("snagcube"))
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
        return !renderingOverlay && PokecubeManager.isFilled(stack);
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
        if (MobEntity instanceof Player player && !worldIn.isClientSide)
        {
            if (stack.hasTag())
            {
                long cooldownStart = stack.getTag().getLong("pokecube:recall_tick");
                if (Tracker.instance().getTick() < cooldownStart + PokecubeCore.getConfig().deadDespawnTimer)
                {
                    return;
                }
            }
            final Predicate<Entity> selector = input -> {
                final IPokemob pokemob = PokemobCaps.getPokemobFor(input);
                if (!AITools.validCombatTargets.test(input)) return false;
                if (pokemob == null) return true;
                return pokemob.getOwner() != player;
            };
            Entity target = Tools.getPointedEntity(player, 32, selector, 1);
            final Vector3 direction = new Vector3().set(player.getViewVector(1));
            final Vector3 targetLocation = Tools.getPointedLocation(player, 32);
            if (target instanceof EntityPokecube) target = null;
            final IPokemob targetMob = PokemobCaps.getPokemobFor(target);
            if (targetMob != null) if (targetMob.getOwner() == MobEntity) target = null;
            final int dt = this.getUseDuration(stack) - timeLeft;
            final boolean filled = PokecubeManager.isFilled(stack);
            if (!filled && target instanceof LivingEntity
                    && this.getCaptureModifier(target, PokecubeItems.getCubeId(stack)) == 0)
                target = null;
            boolean used = false;
            final boolean filledOrSneak = filled || player.isShiftKeyDown() || dt > 10;
            if (target != null && EntityPokecubeBase.CUBES_SEEK)
                used = this.throwPokecubeAt(worldIn, player, stack, targetLocation, target) != null;
            else if (filledOrSneak || !EntityPokecubeBase.CUBES_SEEK)
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
        if (id == null || !IPokecube.PokecubeBehaviour.BEHAVIORS.containsKey(id)) return null;
        final ItemStack stack = cube.copy();
        final boolean hasMob = PokecubeManager.isFilled(stack);
        final Config config = PokecubeCore.getConfig();
        // Check permissions
        if (hasMob && thrower instanceof ServerPlayer player)
        {
            final PokedexEntry entry = PokecubeManager.getPokedexEntry(stack);
            if (!Permissions.canSendOut(entry, player)) return null;
        }
        stack.setCount(1);
        entity = new EntityPokecube(EntityTypes.getPokecube(), world);
        entity.shootingEntity = thrower.isShiftKeyDown() ? null : thrower;
        if (thrower.isShiftKeyDown()) entity.setNoCollisionRelease();
        else entity.autoRelease = config.pokecubeAutoSendOutDelay;
        entity.shooter = thrower.getUUID();
        entity.setItem(stack);

        final Vector3 temp = new Vector3().set(thrower).addTo(0, thrower.getEyeHeight(), 0);
        if (thrower instanceof ServerPlayer player && !(thrower instanceof FakePlayer))
        {
            final InteractionHand hand = player.getUsedItemHand();
            final Vec3 tmp = thrower.getLookAngle();
            final Vec3f look = new Vec3f((float) tmp.x, (float) tmp.y, (float) tmp.z);
            final Vec3f shift = new Vec3f();
            shift.cross(look, new Vec3f(0, 1, 0));
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
        entity.setSeeking(null);
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
        if (id == null || !IPokecube.PokecubeBehaviour.BEHAVIORS.containsKey(id)) return null;
        final ItemStack stack = cube.copy();
        stack.setCount(1);
        entity = new EntityPokecube(EntityTypes.getPokecube(), world);
        entity.shootingEntity = thrower;
        entity.shooter = thrower.getUUID();
        entity.setItem(stack);
        final boolean rightclick = target == thrower;
        if (rightclick) target = null;

        if (target instanceof LivingEntity || PokecubeManager.isFilled(cube) || thrower.isShiftKeyDown()
                || thrower instanceof FakePlayer)
        {
            if (target instanceof LivingEntity living) entity.setSeeking(living);
            if (target == null && targetLocation == null && PokecubeManager.isFilled(cube))
                targetLocation = Vector3.secondAxisNeg;
            entity.targetLocation.set(targetLocation);
            final Vector3 temp = new Vector3().set(thrower).add(0, thrower.getEyeHeight(), 0);
            temp.moveEntity(entity);
            if (thrower instanceof ServerPlayer player && !(thrower instanceof FakePlayer))
            {
                final InteractionHand hand = player.getUsedItemHand();
                final Vec3 tmp = thrower.getLookAngle();
                final Vec3f look = new Vec3f((float) tmp.x, (float) tmp.y, (float) tmp.z);
                final Vec3f shift = new Vec3f();
                shift.cross(look, new Vec3f(0, 1, 0));
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
                entity.setSeeking(null);
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
