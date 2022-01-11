package pokecube.core.items.pokemobeggs;

import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;

import net.minecraft.Util;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.blocks.nests.NestTile;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager;
import pokecube.core.entity.pokemobs.genetics.genes.SpeciesGene;
import pokecube.core.entity.pokemobs.genetics.genes.SpeciesGene.SpeciesInfo;
import pokecube.core.events.EggEvent;
import pokecube.core.handlers.Config;
import pokecube.core.interfaces.IPokecube.PokecubeBehavior;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.utils.PermNodes;
import pokecube.core.utils.Permissions;
import pokecube.core.utils.TagNames;
import pokecube.core.utils.Tools;
import thut.api.IOwnable;
import thut.api.OwnableCaps;
import thut.api.ThutCaps;
import thut.api.entity.genetics.Alleles;
import thut.api.entity.genetics.IMobGenetics;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;
import thut.core.common.genetics.DefaultGenetics;

/** @author Manchou */
public class ItemPokemobEgg extends Item
{
    public static double PLAYERDIST = 2;
    public static double MOBDIST = 4;
    static HashMap<PokedexEntry, IPokemob> fakeMobs = new HashMap<>();
    public static Item EGG = null;

    public static byte[] getColour(final int[] fatherColours, final int[] motherColours)
    {
        final byte[] ret = new byte[]
        { 127, 127, 127, 127 };
        if (fatherColours.length < 3 && motherColours.length < 3) return ret;
        for (int i = 0; i < 3; i++) ret[i] = (byte) ((fatherColours[i] + motherColours[i]) / 2 - 128);
        return ret;
    }

    public static ItemStack getEggStack(final IPokemob pokemob)
    {
        final ItemStack stack = ItemPokemobEgg.getEggStack(pokemob.getPokedexEntry());
        ItemPokemobEgg.initStack(pokemob.getEntity(), pokemob, stack);
        return stack;
    }

    public static ItemStack getEggStack(final PokedexEntry entry)
    {
        final ItemStack eggItemStack = new ItemStack(ItemPokemobEgg.EGG);
        eggItemStack.setTag(new CompoundTag());
        eggItemStack.getTag().putString("pokemob", entry.getName());
        return eggItemStack;
    }

    public static PokedexEntry getEntry(final ItemStack stack)
    {
        if (stack.isEmpty() || stack.getTag() == null) return null;
        if (stack.getTag().contains("pokemob")) return Database.getEntry(stack.getTag().getString("pokemob"));
        genes:
        if (stack.getTag().contains(GeneticsManager.GENES))
        {
            final Tag genes = stack.getTag().get(GeneticsManager.GENES);
            final IMobGenetics eggs = new DefaultGenetics();
            eggs.deserializeNBT((ListTag) genes);
            final Alleles<SpeciesInfo, SpeciesGene> gene = eggs.getAlleles(GeneticsManager.SPECIESGENE);
            if (gene == null) break genes;
            final SpeciesGene sgene = gene.getExpressed();
            final SpeciesInfo info = sgene.getValue();
            // Lets cache this for easier lookup.
            stack.getTag().putString("pokemob", info.entry.getTrimmedName());
            return info.entry;
        }
        return Database.getEntry(stack.getTag().getString("pokemob"));
    }

    public static IPokemob getFakePokemob(final Level world, final Vector3 location, final ItemStack stack)
    {
        final PokedexEntry entry = ItemPokemobEgg.getEntry(stack);
        IPokemob pokemob = ItemPokemobEgg.fakeMobs.get(entry);
        if (pokemob == null)
        {
            pokemob = CapabilityPokemob.getPokemobFor(PokecubeCore.createPokemob(entry, world));
            if (pokemob == null) return null;
            ItemPokemobEgg.fakeMobs.put(entry, pokemob);
        }
        location.moveEntity(pokemob.getEntity());
        return pokemob;
    }

    private static void getGenetics(final IPokemob mother, final IPokemob father, final CompoundTag nbt)
    {
        final IMobGenetics eggs = new DefaultGenetics();
        final IMobGenetics mothers = mother.getEntity().getCapability(ThutCaps.GENETICS_CAP, null).orElse(null);
        final IMobGenetics fathers = father.getEntity().getCapability(ThutCaps.GENETICS_CAP, null).orElse(null);
        GeneticsManager.initEgg(eggs, mothers, fathers);
        final Tag tag = eggs.serializeNBT();
        nbt.put(GeneticsManager.GENES, tag);
        try
        {
            final Alleles<SpeciesInfo, SpeciesGene> alleles = eggs.getAlleles(GeneticsManager.SPECIESGENE);
            final SpeciesGene gene = alleles.getExpressed();
            final SpeciesInfo info = gene.getValue();
            nbt.putString("pokemob", info.entry.getName());
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
        nbt.putString("motherId", mother.getEntity().getStringUUID());
        return;
    }

    public static IPokemob getPokemob(final Level world, final ItemStack stack)
    {
        final PokedexEntry entry = ItemPokemobEgg.getEntry(stack);
        if (entry == null) return null;
        final IPokemob ret = CapabilityPokemob.getPokemobFor(PokecubeCore.createPokemob(entry, world));
        return ret;
    }

    public static float getSize(final float fatherSize, final float motherSize)
    {
        float ret = 1;
        ret = (fatherSize + motherSize) * 0.5f * (1 + 0.075f * (float) ThutCore.newRandom().nextGaussian());
        ret = Math.min(Math.max(0.1f, ret), 2);
        return ret;
    }

    private static LivingEntity imprintOwner(final IPokemob mob)
    {
        final Vector3 location = new Vector3().set(mob.getEntity());
        Player player = mob.getEntity().getLevel().getNearestPlayer(location.x, location.y, location.z,
                ItemPokemobEgg.PLAYERDIST, EntitySelector.NO_SPECTATORS);
        LivingEntity owner = player;
        final AABB box = location.getAABB().inflate(ItemPokemobEgg.MOBDIST, ItemPokemobEgg.MOBDIST,
                ItemPokemobEgg.MOBDIST);
        if (owner == null)
        {
            final List<LivingEntity> list = mob.getEntity().getLevel().getEntitiesOfClass(
                    LivingEntity.class, box, (Predicate<LivingEntity>) input -> !(input instanceof EntityPokemobEgg));
            final LivingEntity closestTo = mob.getEntity();
            LivingEntity t = null;
            double d0 = Double.MAX_VALUE;
            for (final LivingEntity t1 : list) if (t1 != closestTo && EntitySelector.NO_SPECTATORS.test(t1))
            {
                final double d1 = closestTo.distanceToSqr(t1);

                if (d1 <= d0)
                {
                    t = t1;
                    d0 = d1;
                }
            }
            owner = t;
        }
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(owner);
        final IOwnable ownable = OwnableCaps.getOwnable(owner);
        if (owner == null || pokemob != null || ownable != null)
        {
            if (pokemob != null && pokemob.getOwner() instanceof Player) player = (Player) pokemob.getOwner();
            else if (ownable != null && ownable.getOwner() instanceof Player) player = (Player) ownable.getOwner();
            owner = player;
        }
        return owner;
    }

    public static void tryImprint(final IPokemob mob)
    {
        final LivingEntity owner = ItemPokemobEgg.imprintOwner(mob);
        final Config config = PokecubeCore.getConfig();
        // Check permissions
        if (owner instanceof ServerPlayer player && (config.permsHatch || config.permsHatchSpecific))
        {
            final PokedexEntry entry = mob.getPokedexEntry();
            if (config.permsHatch && !PermNodes.getBooleanPerm(player, Permissions.SENDOUTPOKEMOB)) return;
            if (config.permsHatchSpecific && !PermNodes.getBooleanPerm(player, Permissions.SENDOUTSPECIFIC.get(entry)))
                return;
        }
        if (owner != null)
        {
            mob.setOwner(owner.getUUID());
            mob.setGeneralState(GeneralStates.TAMED, true);
            mob.setPokecube(new ItemStack(PokecubeItems.getFilledCube(PokecubeBehavior.DEFAULTCUBE)));
            mob.setHeldItem(ItemStack.EMPTY);
        }
        else mob.getEntity().getPersistentData().remove(TagNames.HATCHED);
    }

    public static void initPokemobGenetics(final IPokemob mob, final CompoundTag nbt, final boolean imprint)
    {
        mob.setForSpawn(10);
        if (imprint) mob.getEntity().getPersistentData().putBoolean(TagNames.HATCHED, true);
        if (nbt.contains(GeneticsManager.GENES))
        {
            final Tag genes = nbt.get(GeneticsManager.GENES);
            final IMobGenetics eggs = new DefaultGenetics();
            eggs.deserializeNBT((ListTag) genes);
            GeneticsManager.initFromGenes(eggs, mob);
        }
    }

    public static void initStack(final Entity mother, final IPokemob father, final ItemStack stack)
    {
        if (!stack.hasTag()) stack.setTag(new CompoundTag());
        final IPokemob mob = CapabilityPokemob.getPokemobFor(mother);
        if (mob != null && father != null) ItemPokemobEgg.getGenetics(mob, father, stack.getTag());
    }

    public static IPokemob make(final Level world, final ItemStack stack, final EntityPokemobEgg egg)
    {
        final PokedexEntry entry = ItemPokemobEgg.getEntry(stack);
        final Mob entity = PokecubeCore.createPokemob(entry, world);
        if (entity != null)
        {
            final IPokemob mob = CapabilityPokemob.getPokemobFor(entity);
            mob.setGeneralState(GeneralStates.EXITINGCUBE, true);
            mob.setHealth(mob.getMaxHealth());
            int exp = Tools.levelToXp(mob.getExperienceMode(), 1);
            exp = Math.max(1, exp);
            mob.setForSpawn(exp);
            entity.moveTo(Math.floor(egg.getX()) + 0.5, Math.floor(egg.getY()) + 0.5, Math.floor(egg.getZ()) + 0.5,
                    world.random.nextFloat() * 360F, 0.0F);
            final CompoundTag nbt = stack.getTag();
            final boolean hasNest = nbt.contains("nestLoc");
            if (stack.hasTag()) ItemPokemobEgg.initPokemobGenetics(mob, stack.getTag(), !hasNest);
            mob.spawnInit();
        }
        return CapabilityPokemob.getPokemobFor(entity);
    }

    public static void spawn(final IPokemob mob, final ItemStack stack, final Level world, final EntityPokemobEgg egg)
    {
        final Mob entity = mob.getEntity();
        final CompoundTag nbt = stack.getTag();
        world.addFreshEntity(entity);
        if (mob.getOwner() != null)
        {
            final LivingEntity owner = mob.getOwner();
            owner.sendMessage(new TranslatableComponent("pokemob.hatch", mob.getDisplayName().getString()),
                    Util.NIL_UUID);
            if (world.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) world.addFreshEntity(new ExperienceOrb(world,
                    entity.getX(), entity.getY(), entity.getZ(), entity.getRandom().nextInt(7) + 1));
        }
        final EggEvent.Hatch evt = new EggEvent.Hatch(egg);
        PokecubeCore.POKEMOB_BUS.post(evt);
        if (nbt.contains("nestLoc"))
        {
            final BlockPos pos = NbtUtils.readBlockPos(nbt.getCompound("nestLoc"));
            final BlockEntity tile = world.getBlockEntity(pos);
            if (tile instanceof NestTile) ((NestTile) tile).addResident(mob);
            mob.setGeneralState(GeneralStates.EXITINGCUBE, false);
        }
        entity.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        entity.playAmbientSound();
    }

    public ItemPokemobEgg(final Properties props)
    {
        super(props);
        ItemPokemobEgg.EGG = this;
    }

    /**
     * allows items to add custom lines of information to the mouseover
     * description
     */
    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(final ItemStack stack, @Nullable final Level playerIn, final List<Component> tooltip,
            final TooltipFlag advanced)
    {
        final PokedexEntry entry = ItemPokemobEgg.getEntry(stack);
        if (entry != null) tooltip.add(1,
                new TranslatableComponent("item.pokecube.pokemobegg.named", I18n.get(entry.getUnlocalizedName())));
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
            final EntityPokemobEgg egg = new EntityPokemobEgg(EntityPokemobEgg.TYPE, world).setStack(itemstack)
                    .setToPos(oldItem.getX(), oldItem.getY(), oldItem.getZ());
            egg.setDeltaMovement(oldItem.getDeltaMovement());
            return egg;
        }
        return null;
    }

    public boolean dropEgg(final Level world, final ItemStack stack, final Vector3 location, final Entity placer)
    {
        final PokedexEntry entry = ItemPokemobEgg.getEntry(stack);
        if (entry == null) return false;
        final ItemStack eggItemStack = new ItemStack(ItemPokemobEgg.EGG, 1);
        if (stack.hasTag()) eggItemStack.setTag(stack.getTag());
        else eggItemStack.setTag(new CompoundTag());
        final EntityPokemobEgg entity = new EntityPokemobEgg(EntityPokemobEgg.TYPE, world).setToPos(location)
                .setStack(eggItemStack);
        final EggEvent.Place event = new EggEvent.Place(entity);
        MinecraftForge.EVENT_BUS.post(event);
        world.addFreshEntity(entity);
        return true;
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
        return ItemPokemobEgg.getEntry(stack) != null;
    }

    @Override
    public InteractionResult useOn(final UseOnContext context)
    {
        final Level worldIn = context.getLevel();
        if (worldIn.isClientSide) return InteractionResult.SUCCESS;
        final Vec3 hit = context.getClickLocation();
        final Vector3 loc = new Vector3().set(hit);
        final ItemStack stack = context.getItemInHand();
        final Player playerIn = context.getPlayer();
        if (this.dropEgg(worldIn, stack, loc, playerIn) && !playerIn.getAbilities().instabuild) stack.shrink(1);
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean shouldOverrideMultiplayerNbt()
    {
        return true;
    }

}