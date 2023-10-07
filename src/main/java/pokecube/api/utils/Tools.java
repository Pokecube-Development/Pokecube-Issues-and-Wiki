package pokecube.api.utils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.items.IPokecube;
import pokecube.api.moves.MoveEntry;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.core.PokecubeItems;
import pokecube.core.moves.MovesUtils;
import pokecube.core.utils.EntityTools;
import thut.api.maths.Cruncher;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;
import thut.lib.RegHelper;

public class Tools
{
    public static enum MergeOrder
    {
        REPLACE, AFTER, BEFORE;
    }

    /**
     * This is an array of what lvl has what exp for the varying exp modes. This
     * array came from: http://bulbapedia.bulbagarden.net/wiki/Experience
     */
    private static final int[][] expMap =
    {
            //@formatter:off
            { 0, 0, 0, 0, 0, 0, 1 },
            { 15, 6, 8, 9, 10, 4, 2 },
            { 52, 21, 27, 57, 33, 13, 3 },
            { 122, 51, 64, 96, 80, 32, 4 },
            { 237, 100, 125, 135, 156, 65, 5 },
            { 406, 172, 216, 179, 270, 112, 6 },
            { 637, 274, 343, 236, 428, 178, 7 },
            { 942, 409, 512, 314, 640, 276, 8 },
            { 1326, 583, 729, 419, 911, 393, 9 },
            { 1800, 800, 1000, 560, 1250, 540, 10 },
            { 2369, 1064, 1331, 742, 1663, 745, 11 },
            { 3041, 1382, 1728, 973, 2160, 967, 12 },
            { 3822, 1757, 2197, 1261, 2746, 1230, 13 },
            { 4719, 2195, 2744, 1612, 3430, 1591, 14 },
            { 5737, 2700, 3375, 2035, 4218, 1957, 15 },
            { 6881, 3276, 4096, 2535, 5120, 2457, 16 },
            { 8155, 3930, 4913, 3120, 6141, 3046, 17 },
            { 9564, 4665, 5832, 3798, 7290, 3732, 18 },
            { 11111, 5487, 6859, 4575, 8573, 4526, 19 },
            { 12800, 6400, 8000, 5460, 10000, 5440, 20 },
            { 14632, 7408, 9261, 6458, 11576, 6482, 21 },
            { 16610, 8518, 10648, 7577, 13310, 7666, 22 },
            { 18737, 9733, 12167, 8825, 15208, 9003, 23 },
            { 21012, 11059, 13824, 10208, 17280, 10506, 24 },
            { 23437, 12500, 15625, 11735, 19531, 12187, 25 },
            { 26012, 14060, 17576, 13411, 21970, 14060, 26 },
            { 28737, 15746, 19683, 15244, 24603, 16140, 27 },
            { 31610, 17561, 21952, 17242, 27440, 18439, 28 },
            { 34632, 19511, 24389, 19411, 30486, 20974, 29 },
            { 37800, 21600, 27000, 21760, 33750, 23760, 30 },
            { 41111, 23832, 29791, 24294, 37238, 26811, 31 },
            { 44564, 26214, 32768, 27021, 40960, 30146, 32 },
            { 48155, 28749, 35937, 29949, 44921, 33780, 33 },
            { 51881, 31443, 39304, 33084, 49130, 37731, 34 },
            { 55737, 34300, 42875, 36435, 53593, 42017, 35 },
            { 59719, 37324, 46656, 40007, 58320, 46656, 36 },
            { 63822, 40522, 50653, 43808, 63316, 50653, 37 },
            { 68041, 43897, 54872, 47846, 68590, 55969, 38 },
            { 72369, 47455, 59319, 52127, 74148, 60505, 39 },
            { 76800, 51200, 64000, 56660, 80000, 66560, 40 },
            { 81326, 55136, 68921, 61450, 86151, 71677, 41 },
            { 85942, 59270, 74088, 66505, 92610, 78533, 42 },
            { 90637, 63605, 79507, 71833, 99383, 84277, 43 },
            { 95406, 68147, 85184, 77440, 106480, 91998, 44 },
            { 100237, 72900, 91125, 83335, 113906, 98415, 45 },
            { 105122, 77868, 97336, 89523, 121670, 107069, 46 },
            { 110052, 83058, 103823, 96012, 129778, 114205, 47 },
            { 115015, 88473, 110592, 102810, 138240, 123863, 48 },
            { 120001, 94119, 117649, 109923, 147061, 131766, 49 },
            { 125000, 100000, 125000, 117360, 156250, 142500, 50 },
            { 131324, 106120, 132651, 125126, 165813, 151222, 51 },
            { 137795, 112486, 140608, 133229, 175760, 163105, 52 },
            { 144410, 119101, 148877, 141677, 186096, 172697, 53 },
            { 151165, 125971, 157464, 150476, 196830, 185807, 54 },
            { 158056, 133100, 166375, 159635, 207968, 196322, 55 },
            { 165079, 140492, 175616, 169159, 219520, 210739, 56 },
            { 172229, 148154, 185193, 179056, 231491, 222231, 57 },
            { 179503, 156089, 195112, 189334, 243890, 238036, 58 },
            { 186894, 164303, 205379, 199999, 256723, 250562, 59 },
            { 194400, 172800, 216000, 211060, 270000, 267840, 60 },
            { 202013, 181584, 226981, 222522, 283726, 281456, 61 },
            { 209728, 190662, 238328, 234393, 297910, 300293, 62 },
            { 217540, 200037, 250047, 246681, 312558, 315059, 63 },
            { 225443, 209715, 262144, 259392, 327680, 335544, 64 },
            { 233431, 219700, 274625, 272535, 343281, 351520, 65 },
            { 241496, 229996, 287496, 286115, 359370, 373744, 66 },
            { 249633, 240610, 300763, 300140, 375953, 390991, 67 },
            { 257834, 251545, 314432, 314618, 393040, 415050, 68 },
            { 267406, 262807, 328509, 329555, 410636, 433631, 69 },
            { 276458, 274400, 343000, 344960, 428750, 459620, 70 },
            { 286328, 286328, 357911, 360838, 447388, 479600, 71 },
            { 296358, 298598, 373248, 377197, 466560, 507617, 72 },
            { 305767, 311213, 389017, 394045, 486271, 529063, 73 },
            { 316074, 324179, 405224, 411388, 506530, 559209, 74 },
            { 326531, 337500, 421875, 429235, 527343, 582187, 75 },
            { 336255, 351180, 438976, 447591, 548720, 614566, 76 },
            { 346965, 365226, 456533, 466464, 570666, 639146, 77 },
            { 357812, 379641, 474552, 485862, 593190, 673863, 78 },
            { 367807, 394431, 493039, 505791, 616298, 700115, 79 },
            { 378880, 409600, 512000, 526260, 640000, 737280, 80 },
            { 390077, 425152, 531441, 547274, 664301, 765275, 81 },
            { 400293, 441094, 551368, 568841, 689210, 804997, 82 },
            { 411686, 457429, 571787, 590969, 714733, 834809, 83 },
            { 423190, 474163, 592704, 613664, 740880, 877201, 84 },
            { 433572, 491300, 614125, 636935, 767656, 908905, 85 },
            { 445239, 508844, 636056, 660787, 795070, 954084, 86 },
            { 457001, 526802, 658503, 685228, 823128, 987754, 87 },
            { 467489, 545177, 681472, 710266, 851840, 1035837, 88 },
            { 479378, 563975, 704969, 735907, 881211, 1071552, 89 },
            { 491346, 583200, 729000, 762160, 911250, 1122660, 90 },
            { 501878, 602856, 753571, 789030, 941963, 1160499, 91 },
            { 513934, 622950, 778688, 816525, 973360, 1214753, 92 },
            { 526049, 643485, 804357, 844653, 1005446, 1254796, 93 },
            { 536557, 664467, 830584, 873420, 1038230, 1312322, 94 },
            { 548720, 685900, 857375, 902835, 1071718, 1354652, 95 },
            { 560922, 707788, 884736, 932903, 1105920, 1415577, 96 },
            { 571333, 730138, 912673, 963632, 1140841, 1460276, 97 },
            { 583539, 752953, 941192, 995030, 1176490, 1524731, 98 },
            { 591882, 776239, 970299, 1027103, 1212873, 1571884, 99 },
            { 600000, 800000, 1000000, 1059860, 1250000, 1640000, 100 } };
    //@formatter:on
    // cache these in tables, for easier lookup.
    public static int[] maxXPs =
    { 800000, 1000000, 1059860, 1250000, 600000, 1640000 };

    /**
     * This is a cache of a radial lookup map, note that it only has +- 4 blocks
     * along y, and has null for entries outside of that.
     */
    public static final byte[][] indexArr = new byte[32768][3];

    static
    {
        final Vector3 r = new Vector3();
        for (int i = 0; i < Tools.indexArr.length; i++)
        {
            Cruncher.indexToVals(i, r);
            Tools.indexArr[i][0] = (byte) r.intX();
            Tools.indexArr[i][1] = (byte) r.intY();
            Tools.indexArr[i][2] = (byte) r.intZ();
        }
    }

    public static float getAttackEfficiency(final PokeType type, final PokeType defenseType1,
            final PokeType defenseType2)
    {
        float multiplier = 1;
        if (type == null) return multiplier;
        if (defenseType1 != PokeType.unknown && defenseType1 != null)
            multiplier *= PokeType.typeTable[type.ordinal()][defenseType1.ordinal()];
        if (defenseType2 != PokeType.unknown && defenseType2 != null)
            multiplier *= PokeType.typeTable[type.ordinal()][defenseType2.ordinal()];
        return multiplier;
    }

    public static int computeCatchRate(final IPokemob pokemob, final double cubeBonus)
    {
        return Tools.computeCatchRate(pokemob, cubeBonus, 0);
    }

    public static int computeCatchRate(final IPokemob pokemob, final double cubeBonus, final int cubeBonus2)
    {
        final float HPmax = pokemob.getMaxHealth();
        final Random rand = ThutCore.newRandom();
        final float HP = pokemob.getHealth();
        float statusBonus = 1F;
        final int status = pokemob.getStatus();
        if (status == IMoveConstants.STATUS_FRZ || status == IMoveConstants.STATUS_SLP) statusBonus = 2F;
        else if (status != IMoveConstants.STATUS_NON) statusBonus = 1.5F;
        final int catchRate = pokemob.getCatchRate();

        final double a = Tools.getCatchRate(HPmax, HP, catchRate, cubeBonus, statusBonus) + cubeBonus2;

        if (a > 255) return 5;
        final double b = 1048560 / Math.sqrt(Math.sqrt(16711680 / a));
        int n = 0;

        if (rand.nextInt(65535) <= b) n++;

        if (rand.nextInt(65535) <= b) n++;

        if (rand.nextInt(65535) <= b) n++;

        if (rand.nextInt(65535) <= b) n++;

        return n;
    }

    public static int computeCatchRate(final IPokemob pokemob, final ResourceLocation pokecubeId)
    {
        double cubeBonus = 0;
        int additionalBonus = 0;
        final Item cube = PokecubeItems.getFilledCube(pokecubeId);
        if (cube instanceof IPokecube pokecube) cubeBonus = pokecube.getCaptureModifier(pokemob, pokecubeId);
        if (IPokecube.PokecubeBehaviour.BEHAVIORS.containsKey(pokecubeId))
            additionalBonus = IPokecube.PokecubeBehaviour.BEHAVIORS.get(pokecubeId).getAdditionalBonus(pokemob);
        return Tools.computeCatchRate(pokemob, cubeBonus, additionalBonus);
    }

    public static double getCatchRate(final float hPmax, final float hP, final float catchRate, final double cubeBonus,
            final double statusBonus)
    {
        return (3D * hPmax - 2D * hP) * catchRate * cubeBonus * statusBonus / (3D * hPmax);
    }

    public static int getExp(final float coef, final int baseXP, final int level)
    {
        return Mth.floor(coef * baseXP * level / 7F);
    }

    private static int getLevelFromTable(final int index, final int exp)
    {
        int level = 100;
        for (int i = 0; i < 99; i++) if (Tools.expMap[i][index] <= exp && Tools.expMap[i + 1][index] > exp)
        {
            level = Tools.expMap[i][6];
            break;
        }
        return level;
    }

    public static Entity getPointedEntity(Entity entity, double distance)
    {
        return Tools.getPointedEntity(entity, distance, null);
    }

    public static Entity getPointedEntity(Entity entity, double distance, double extraSize)
    {
        return Tools.getPointedEntity(entity, distance, null, extraSize);
    }

    public static boolean isRidingOrRider(final Entity a, final Entity b)
    {
        for (final Entity c : a.getIndirectPassengers()) if (b.equals(c)) return true;
        for (final Entity c : b.getIndirectPassengers()) if (a.equals(c)) return true;
        return false;
    }

    public static Entity getPointedEntity(final Entity entity, double distance, final Predicate<Entity> selector)
    {
        return getPointedEntity(entity, distance, selector, 0);
    }

    public static Entity getPointedEntity(final Entity entity, double distance, final Predicate<Entity> selector,
            double extraSize)
    {
        final Vector3 pos = new Vector3().set(entity, true);
        final Vector3 loc = Tools.getPointedLocation(entity, distance);
        if (loc != null) distance = Math.min(loc.distanceTo(pos) + 1, distance);

        float f = 1.0F;

        Vec3 vec3 = entity.getEyePosition(f);
        Vec3 vec31 = entity.getViewVector(f);
        Vec3 vec32 = vec3.add(vec31.x * distance, vec31.y * distance, vec31.z * distance);

        AABB aabb = entity.getBoundingBox().expandTowards(vec31.scale(distance)).inflate(f, f, f);
        Predicate<Entity> predicate = EntitySelector.NO_SPECTATORS.and(c -> entity.isPickable());
        if (selector != null) predicate = predicate.and(selector);
        predicate = predicate.and(c -> c.isPickable() && !Tools.isRidingOrRider(entity, c));
        EntityHitResult hitResult = Tools.getEntityHitResult(entity, vec3, vec32, aabb, predicate, 0, extraSize);
        Entity hit = hitResult != null ? hitResult.getEntity() : null;
        if (hit != null) hit = EntityTools.getCoreEntity(hit);
        return hit;
    }

    @Nullable
    public static EntityHitResult getEntityHitResult(Entity source, Vec3 start, Vec3 end, AABB volume,
            Predicate<Entity> valid, double minDistance, double extraSize)
    {
        Level level = source.level;
        double d0 = minDistance;
        Entity entity = null;
        Vec3 vec3 = null;

        for (Entity entity1 : level.getEntities(source, volume, valid))
        {
            AABB aabb = entity1.getBoundingBox().inflate(entity1.getPickRadius() + extraSize);
            Optional<Vec3> optional = aabb.clip(start, end);
            if (aabb.contains(start))
            {
                if (d0 >= 0.0D)
                {
                    entity = entity1;
                    vec3 = optional.orElse(start);
                    d0 = 0.0D;
                }
            }
            else if (optional.isPresent())
            {
                Vec3 vec31 = optional.get();
                double d1 = start.distanceToSqr(vec31);
                if (d1 < d0 || d0 == 0.0D)
                {
                    if (entity1.getRootVehicle() == source.getRootVehicle() && !entity1.canRiderInteract())
                    {
                        if (d0 == 0.0D)
                        {
                            entity = entity1;
                            vec3 = vec31;
                        }
                    }
                    else
                    {
                        entity = entity1;
                        vec3 = vec31;
                        d0 = d1;
                    }
                }
            }
        }
        return entity == null ? null : new EntityHitResult(entity, vec3);
    }

    public static Vector3 getPointedLocation(final Entity entity, final double distance)
    {
        final HitResult hit = entity.pick(distance, 1.0f, false);
        if (hit == null || hit.getType() != Type.BLOCK || !(hit instanceof BlockHitResult result)) return null;
        final Vector3 vec = new Vector3().set(result.getLocation());
        return vec;
    }

    public static int getPower(final String move, final IPokemob user, final LivingEntity target)
    {
        final MoveEntry attack = MovesUtils.getMove(move);
        if (attack == null) return 0;
        int pwr = attack.getPWR(user, target);
        final IPokemob mob = PokemobCaps.getPokemobFor(target);
        if (mob != null)
        {
            pwr *= Tools.getAttackEfficiency(attack.getType(user), mob.getType1(), mob.getType2());
            if (mob.getAbility() != null)
            {
                MoveApplication test = new MoveApplication(attack, user, target);
                pwr = mob.getAbility().beforeDamage(mob, test, pwr);
                mob.getAbility().preMoveUse(mob, test);
                if (test.canceled) pwr = 0;
            }
        }
        return pwr;
    }

    public static byte getRandomIV(final Random random)
    {
        return (byte) random.nextInt(32);
    }

    /**
     * Can be {@link IPokemob#MALE}, {@link IPokemob#FEMALE} or
     * {@link IPokemob#NOSEXE}
     *
     * @param baseValue the sexe ratio of the Pokemon, 254=Only female, 255=no
     *                  sexe, 0=Only male
     * @param random
     * @return the int gender
     */
    public static byte getSexe(final int baseValue, final Random random)
    {
        if (baseValue == 255) return IPokemob.NOSEXE;
        if (random.nextInt(255) >= baseValue) return IPokemob.MALE;
        return IPokemob.FEMALE;
    }

    public static ItemStack getStack(final Map<String, String> values)
    {
        return Tools.getStack(values, null);
    }

    public static ItemStack getStack(final Map<String, String> values, final ServerLevel world)
    {
        String id = "";
        int size = 1;
        boolean resource = false;
        String tag = "";
        boolean isTable = false;
        String table = "";

        for (final String key : values.keySet()) if (key.toString().equals("id")) id = values.get(key);
        else if (key.toString().equals("n")) size = Integer.parseInt(values.get(key));
        else if (key.toString().equals("tag")) tag = values.get(key).trim();
        else if (key.toString().equals("table"))
        {
            table = values.get(key).trim();
            isTable = true;
        }

        if (isTable && world != null)
        {
            final LootTable loottable = world.getServer().getLootTables().get(new ResourceLocation(table));
            final LootContext.Builder lootcontext$builder = new LootContext.Builder(world)
                    .withRandom(world.getRandom());
            // Generate the loot list.
            final List<ItemStack> list = loottable.getRandomItems(lootcontext$builder.create(loottable.getParamSet()));
            // Shuffle the list.
            if (!list.isEmpty()) Collections.shuffle(list);
            for (final ItemStack itemstack : list)
                // Pick first valid item in it.
                if (!itemstack.isEmpty())
            {
                final ItemStack stack = itemstack.copy();
                if (RegHelper.getKey(stack).equals(new ResourceLocation("pokecube", "candy")))
                    PokecubeItems.makeStackValid(stack);
                return stack;
            }
        }

        if (id.isEmpty()) return ItemStack.EMPTY;
        resource = id.contains(":");
        ItemStack stack = ItemStack.EMPTY;
        Item item = null;
        if (resource) item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));

        if (!resource || item == null) stack = PokecubeItems.getStack(id, false);
        if (!stack.isEmpty()) item = stack.getItem();
        if (item == null)
            for (final ResourceLocation loc : ForgeRegistries.ITEMS.getKeys()) if (loc.getPath().equals(id))
        {
            item = ForgeRegistries.ITEMS.getValue(loc);
            break;
        }

        if (item == null && stack.isEmpty())
        {
            PokecubeAPI.LOGGER.error(id + " not found!");
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) stack = new ItemStack(item, 1);
        stack.setCount(size);
        if (!tag.isEmpty()) try
        {
            stack.setTag(TagParser.parseTag(tag));
        }
        catch (final CommandSyntaxException e)
        {
            PokecubeAPI.LOGGER.error("Error parsing items for " + values, e);
        }
        return stack;
    }

    public static int getType(String name)
    {
        name = ThutCore.trim(name);

        switch (name)
        {
        case "slow_then_very_fast":
            return 0;
        case "slow-then-very-fast":
            return 0;
        case "erratic":
            return 0;
        case "fast":
            return 1;
        case "medium_fast":
            return 1;
        case "medium-fast":
            return 1;
        case "medium":
            return 2;
        case "medium_slow":
            return 3;
        case "medium-slow":
            return 3;
        case "slow":
            return 4;
        case "fast_then_very_slow":
            return 5;
        case "fast-then-very-slow":
            return 5;
        case "fluctuating":
            return 5;
        }

        /*
         * 1 - slow - 4 2 - medium - 2 3 - fast - 1 4 - medium-slow - 3 5 -
         * slow-then-very-fast - 0 6 - fast-then-very-slow - 5 5 3 2 4 1 6 { 52,
         * 21, 27, 57, 33, 13, 3 },
         */
        PokecubeAPI.LOGGER.error(new IllegalArgumentException("Error parsing EXP Type for " + name));
        return 0;
    }

    public static void giveItem(final Player PlayerEntity, final ItemStack itemstack)
    {
        final boolean flag = PlayerEntity.getInventory().add(itemstack);
        if (flag)
        {
            PlayerEntity.getLevel().playSound((Player) null, PlayerEntity.getX(), PlayerEntity.getY(),
                    PlayerEntity.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F,
                    ((PlayerEntity.getRandom().nextFloat() - PlayerEntity.getRandom().nextFloat()) * 0.7F + 1.0F)
                            * 2.0F);
            PlayerEntity.inventoryMenu.broadcastChanges();
        }
        if (!flag)
        {
            final ItemEntity ItemEntity = PlayerEntity.drop(itemstack, false);
            if (ItemEntity != null)
            {
                ItemEntity.setNoPickUpDelay();
                ItemEntity.setOwner(PlayerEntity.getUUID());
            }
        }
    }

    public static boolean hasMove(final String move, final IPokemob mob)
    {
        for (final String s : mob.getMoves()) if (s != null && s.equalsIgnoreCase(move)) return true;
        return false;
    }

    public static boolean isAnyPlayerInRange(final double range, final Entity entity)
    {
        final Level world = entity.getLevel();
        return world.getNearestPlayer(entity.getX(), entity.getY(), entity.getZ(), range,
                EntitySelector.NO_SPECTATORS) != null;
    }

    public static boolean isSameStack(final ItemStack a, final ItemStack b)
    {
        return Tools.isSameStack(a, b, false);
    }

    public static boolean isSameStack(final ItemStack a, final ItemStack b, final boolean strict)
    {
        // TODO determine if to use the tags?
        return ItemStack.isSameIgnoreDurability(a, b);
    }

    public static int levelToXp(final int type, int level)
    {
        level = Math.min(100, level);
        level = Math.max(1, level);
        int index = type;
        switch (type)
        {
        case 4:
            index = 0;
            break;
        case 5:
            index = 5;
            break;
        default:
            index++;
        }
        return Tools.expMap[level - 1][index];
    }

    public static int xpToLevel(final int type, final int exp)
    {
        int index = type;
        switch (type)
        {
        case 4:
            index = 0;
            break;
        case 5:
            index = 5;
            break;
        default:
            index++;
        }
        return Tools.getLevelFromTable(index, exp);
    }
}
