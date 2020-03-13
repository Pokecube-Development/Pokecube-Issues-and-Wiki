package pokecube.core.items.pokecubes;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.google.common.collect.Sets;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.server.permission.IPermissionHandler;
import net.minecraftforge.server.permission.PermissionAPI;
import net.minecraftforge.server.permission.context.PlayerContext;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.database.PokedexEntry;
import pokecube.core.handlers.Config;
import pokecube.core.handlers.playerdata.PlayerPokemobCache;
import pokecube.core.interfaces.IPokecube;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.Stats;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.moves.MovesUtils;
import pokecube.core.utils.Permissions;
import pokecube.core.utils.TagNames;
import pokecube.core.utils.Tools;
import thut.api.maths.Vector3;
import thut.core.common.commands.CommandTools;

public class Pokecube extends Item implements IPokecube
{
    public static final Set<Class<? extends LivingEntity>> snagblacklist = Sets.newHashSet();

    private static final Predicate<LivingEntity> capturable = t ->
    {
        if (Pokecube.snagblacklist.contains(t.getClass())) return false;
        for (final Class<? extends LivingEntity> claz : Pokecube.snagblacklist)
            if (claz.isInstance(t)) return false;
        return true;
    };

    @OnlyIn(Dist.CLIENT)
    public static void displayInformation(final CompoundNBT nbt, final List<ITextComponent> list)
    {
        final boolean flag2 = nbt.getBoolean("Flames");

        if (flag2) list.add(new TranslationTextComponent("item.pokecube.flames"));

        final boolean flag3 = nbt.getBoolean("Bubbles");

        if (flag3) list.add(new TranslationTextComponent("item.pokecube.bubbles"));

        final boolean flag4 = nbt.getBoolean("Leaves");

        if (flag4) list.add(new TranslationTextComponent("item.pokecube.leaves"));

        final boolean flag5 = nbt.contains("dye");

        if (flag5) list.add(new TranslationTextComponent(DyeColor.byId(nbt.getInt("dye")).getTranslationKey()));
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
    public void addInformation(final ItemStack item, @Nullable final World world, final List<ITextComponent> list,
            final ITooltipFlag advanced)
    {
        if (PokecubeManager.isFilled(item))
        {
            final Entity mob = PokecubeManager.itemToMob(item, world);
            if (mob == null)
            {
                list.add(new TranslationTextComponent("pokecube.filled.error"));
                return;
            }
            final IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
            if (pokemob == null) return;
            list.add(pokemob.getDisplayName());

            final CompoundNBT pokeTag = item.getTag().getCompound(TagNames.POKEMOB);

            final float health = pokeTag.getFloat("Health");
            final float maxHealth = pokemob.getStat(Stats.HP, false);
            final int lvlexp = Tools.levelToXp(pokemob.getExperienceMode(), pokemob.getLevel());
            final int exp = pokemob.getExp() - lvlexp;
            final int neededexp = Tools.levelToXp(pokemob.getExperienceMode(), pokemob.getLevel() + 1) - lvlexp;
            list.add(new TranslationTextComponent("pokecube.tooltip.level", pokemob.getLevel()));
            list.add(new TranslationTextComponent("pokecube.tooltip.health", health, maxHealth));
            list.add(new TranslationTextComponent("pokecube.tooltip.xp", exp, neededexp));

            if (Screen.hasShiftDown())
            {
                String arg = "";
                for (final String s : pokemob.getMoves())
                    if (s != null) arg += I18n.format(MovesUtils.getUnlocalizedMove(s)) + ", ";
                if (arg.endsWith(", ")) arg = arg.substring(0, arg.length() - 2);
                list.add(new TranslationTextComponent("pokecube.tooltip.moves", arg));
                arg = "";
                for (final Byte b : pokemob.getIVs())
                    arg += b + ", ";
                if (arg.endsWith(", ")) arg = arg.substring(0, arg.length() - 2);
                list.add(new TranslationTextComponent("pokecube.tooltip.ivs", arg));
                arg = "";
                for (final Byte b : pokemob.getEVs())
                {
                    final int n = b + 128;
                    arg += n + ", ";
                }
                if (arg.endsWith(", ")) arg = arg.substring(0, arg.length() - 2);
                list.add(new TranslationTextComponent("pokecube.tooltip.evs", arg));
                list.add(new TranslationTextComponent("pokecube.tooltip.nature", pokemob.getNature()));
                list.add(new TranslationTextComponent("pokecube.tooltip.ability", pokemob.getAbility()));
            }
            else list.add(new TranslationTextComponent("pokecube.tooltip.advanced"));
        }

        if (item.hasTag())
        {
            final CompoundNBT CompoundNBT = PokecubeManager.getSealTag(item);
            Pokecube.displayInformation(CompoundNBT, list);
        }
    }

    @Override
    public boolean canCapture(final MobEntity hit, final ItemStack cube)
    {
        final ResourceLocation id = PokecubeItems.getCubeId(cube);
        if (id != null && id.getPath().equals("snag"))
        {
            if (this.getCaptureModifier(hit, id) <= 0) return false;
            return Pokecube.capturable.test(hit);
        }
        return CapabilityPokemob.getPokemobFor(hit) != null;
    }

    /**
     * This function should return a new entity to replace the dropped item.
     * Returning null here will not kill the ItemEntity and will leave it to
     * function normally. Called when the item it placed in a world.
     *
     * @param world
     *            The world object
     * @param location
     *            The ItemEntity object, useful for getting the position of the
     *            entity
     * @param itemstack
     *            The current item stack
     * @return A new Entity object to spawn or null
     */
    @Override
    public Entity createEntity(final World world, final Entity oldItem, final ItemStack itemstack)
    {
        if (this.hasCustomEntity(itemstack))
        {
            final FakePlayer player = PokecubeMod.getFakePlayer(world);
            final EntityPokecube cube = new EntityPokecube(EntityPokecube.TYPE, world);
            cube.shootingEntity = player;
            cube.shooter = player.getUniqueID();
            cube.setItem(itemstack);
            cube.setMotion(0, 0, 0);
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
    public double getCaptureModifier(final LivingEntity mob, final ResourceLocation pokecubeId)
    {
        if (pokecubeId.getPath().equals("snag"))
        {
            if (mob.isInvulnerable()) return 0;
            return 1;
        }
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
        return pokemob != null ? this.getCaptureModifier(pokemob, pokecubeId) : 0;
    }

    @Override
    public double getDurabilityForDisplay(final ItemStack stack)
    {
        if (stack.hasTag() && stack.getTag().contains("CHP"))
        {
            final float chp = stack.getTag().getFloat("CHP");
            final float mhp = stack.getTag().getFloat("MHP");
            if (chp == mhp) return 1 - 0.99999;
            return 1 - chp / mhp;
        }
        return super.getDurabilityForDisplay(stack);
    }

    @Override
    /**
     * returns the action that specifies what animation to play when the items
     * is being used
     */
    public UseAction getUseAction(final ItemStack stack)
    {
        return UseAction.BOW;
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
     * @param stack
     *            The current item stack
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
    public ActionResult<ItemStack> onItemRightClick(final World world, final PlayerEntity player, final Hand hand)
    {
        player.setActiveHand(hand);
        return new ActionResult<>(ActionResultType.SUCCESS, player.getHeldItem(hand));
    }

    @Override
    /**
     * Called when the player stops using an Item (stops holding the right
     * mouse button).
     */
    public void onPlayerStoppedUsing(final ItemStack stack, final World worldIn, final LivingEntity MobEntity,
            final int timeLeft)
    {
        if (MobEntity instanceof PlayerEntity && !worldIn.isRemote)
        {
            final PlayerEntity player = (PlayerEntity) MobEntity;
            final com.google.common.base.Predicate<Entity> selector = input ->
            {
                final IPokemob pokemob = CapabilityPokemob.getPokemobFor(input);
                if (pokemob == null) return true;
                return pokemob.getOwner() != player;
            };
            Entity target = Tools.getPointedEntity(player, 32, selector);
            final Vector3 direction = Vector3.getNewVector().set(player.getLook(0));
            final Vector3 targetLocation = Tools.getPointedLocation(player, 32);

            if (target instanceof EntityPokecube) target = null;
            final IPokemob targetMob = CapabilityPokemob.getPokemobFor(target);
            if (targetMob != null) if (targetMob.getOwner() == MobEntity) target = null;
            final int dt = this.getUseDuration(stack) - timeLeft;
            final boolean filled = PokecubeManager.isFilled(stack);
            if (!filled && target instanceof LivingEntity && this.getCaptureModifier((LivingEntity) target,
                    PokecubeItems.getCubeId(stack)) == 0) target = null;
            boolean used = false;
            final boolean filledOrSneak = filled || player.isCrouching() || dt > 5;
            if (target != null && EntityPokecubeBase.SEEKING) used = this.throwPokecubeAt(worldIn, player, stack,
                    targetLocation, target);
            else if (filledOrSneak || !EntityPokecubeBase.SEEKING)
            {
                float power = (this.getUseDuration(stack) - timeLeft) / (float) 100;
                power = Math.min(1, power);
                used = this.throwPokecube(worldIn, player, stack, direction, power);
            }
            else
            {
                CommandTools.sendError(player, "pokecube.badaim");
                used = false;
            }
            if (used)
            {
                if (PokecubeManager.isFilled(stack) || !player.isCreative()) stack.split(1);
                if (stack.isEmpty()) for (int i = 0; i < player.inventory.getSizeInventory(); i++)
                    if (player.inventory.getStackInSlot(i) == stack)
                    {
                        player.inventory.setInventorySlotContents(i, ItemStack.EMPTY);
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
    public boolean shouldSyncTag()
    {
        return true;
    }

    @Override
    public boolean showDurabilityBar(final ItemStack stack)
    {
        return PokecubeManager.isFilled(stack);
    }

    @Override
    public boolean throwPokecube(final World world, final LivingEntity thrower, final ItemStack cube,
            final Vector3 direction, final float power)
    {
        EntityPokecube entity = null;
        final ResourceLocation id = PokecubeItems.getCubeId(cube);
        if (id == null || !IPokecube.BEHAVIORS.containsKey(id)) return false;
        final ItemStack stack = cube.copy();
        final boolean hasMob = PokecubeManager.isFilled(stack);
        final Config config = PokecubeCore.getConfig();
        // Check permissions
        if (hasMob && (config.permsSendOut || config.permsSendOutSpecific) && thrower instanceof PlayerEntity)
        {
            final PokedexEntry entry = PokecubeManager.getPokedexEntry(stack);
            final PlayerEntity player = (PlayerEntity) thrower;
            final IPermissionHandler handler = PermissionAPI.getPermissionHandler();
            final PlayerContext context = new PlayerContext(player);
            if (config.permsSendOut && !handler.hasPermission(player.getGameProfile(), Permissions.SENDOUTPOKEMOB,
                    context)) return false;
            if (config.permsSendOutSpecific && !handler.hasPermission(player.getGameProfile(),
                    Permissions.SENDOUTSPECIFIC.get(entry), context)) return false;
        }
        stack.setCount(1);
        entity = new EntityPokecube(EntityPokecube.TYPE, world);
        entity.shootingEntity = thrower.isCrouching() ? null : thrower;
        entity.shooter = thrower.getUniqueID();
        entity.setItem(stack);

        final Vector3 temp = Vector3.getNewVector().set(thrower).add(0, thrower.getEyeHeight(), 0);
        final Vector3 temp1 = Vector3.getNewVector().set(thrower.getLookVec()).scalarMultBy(1.5);
        temp.addTo(temp1).moveEntity(entity);
        entity.shoot(direction.norm(), power * 10);
        entity.seeking = false;
        entity.targetEntity = null;
        entity.targetLocation.clear();
        entity.forceSpawn = true;
        if (hasMob && !thrower.isCrouching()) entity.targetLocation.y = -1;
        if (!world.isRemote)
        {
            thrower.playSound(SoundEvents.ENTITY_EGG_THROW, 0.5F, 0.4F / (new Random().nextFloat() * 0.4F + 0.8F));
            world.addEntity(entity);
            if (hasMob && thrower instanceof PlayerEntity) PlayerPokemobCache.UpdateCache(stack, false, false);
        }
        return true;
    }

    @Override
    public boolean throwPokecubeAt(final World world, final LivingEntity thrower, final ItemStack cube,
            Vector3 targetLocation, Entity target)
    {
        EntityPokecube entity = null;
        final ResourceLocation id = PokecubeItems.getCubeId(cube);
        if (id == null || !IPokecube.BEHAVIORS.containsKey(id)) return false;
        final ItemStack stack = cube.copy();
        stack.setCount(1);
        entity = new EntityPokecube(EntityPokecube.TYPE, world);
        entity.shootingEntity = thrower;
        entity.shooter = thrower.getUniqueID();
        entity.forceSpawn = true;
        entity.setItem(stack);
        final boolean rightclick = target == thrower;
        if (rightclick) target = null;

        if (target instanceof LivingEntity || PokecubeManager.isFilled(cube) || thrower.isCrouching()
                || thrower instanceof FakePlayer)
        {
            if (target instanceof LivingEntity) entity.targetEntity = (LivingEntity) target;
            if (target == null && targetLocation == null && PokecubeManager.isFilled(cube))
                targetLocation = Vector3.secondAxisNeg;
            entity.targetLocation.set(targetLocation);
            if (thrower.isCrouching())
            {
                final Vector3 temp = Vector3.getNewVector().set(thrower).add(0, thrower.getEyeHeight(), 0);
                final Vector3 temp1 = Vector3.getNewVector().set(thrower.getLookVec()).norm();
                temp.addTo(temp1).moveEntity(entity);
                temp.clear().setVelocities(entity);
                entity.targetEntity = null;
                entity.targetLocation.clear();
            }
            else
            {
                final Vector3 temp = Vector3.getNewVector().set(thrower).add(0, thrower.getEyeHeight(), 0);
                final Vector3 temp1 = Vector3.getNewVector().set(thrower.getLookVec()).norm();
                temp.addTo(temp1).moveEntity(entity);
            }
            if (!world.isRemote)
            {
                thrower.playSound(SoundEvents.ENTITY_EGG_THROW, 0.5F, 0.4F / (new Random().nextFloat() * 0.4F + 0.8F));
                world.addEntity(entity);
                if (PokecubeManager.isFilled(stack) && thrower instanceof PlayerEntity) PlayerPokemobCache.UpdateCache(
                        stack, false, false);
            }
        }
        else if (!rightclick) return false;
        return true;
    }
}
