package pokecube.core.moves.damage.attributes;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.registries.DeferredRegister;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.core.PokecubeCore;
import pokecube.core.network.pokemobs.PacketSyncModifier;

@SuppressWarnings("unchecked")
public class PokecubeAttributes
{
    public static final DeferredRegister<Attribute> REGISTER;

    public static final Holder<Attribute> HP;
    public static final Holder<Attribute> ATTACK;
    public static final Holder<Attribute> DEFENSE;
    public static final Holder<Attribute> SPATTACK;
    public static final Holder<Attribute> SPDEFENSE;
    public static final Holder<Attribute> VIT;

    public static final Holder<Attribute> EVASION;
    public static final Holder<Attribute> ACCURACY;

    public static final Holder<Attribute>[] ATTRIBUTES = new Holder[8];

    static
    {
        REGISTER = DeferredRegister.create(Registries.ATTRIBUTE, PokecubeCore.MODID);

        HP = Attributes.MAX_HEALTH; // Hp is different from the rest.
        ATTACK = REGISTER.register("attack", () -> new StatAttribute("attribute.name.pokemob.attack", 5));
        DEFENSE = REGISTER.register("defense", () -> new StatAttribute("attribute.name.pokemob.defense", 5));
        SPATTACK = REGISTER.register("spatk", () -> new StatAttribute("attribute.name.pokemob.spatk", 5));
        SPDEFENSE = REGISTER.register("spdef", () -> new StatAttribute("attribute.name.pokemob.spdef", 5));
        VIT = REGISTER.register("vit", () -> new StatAttribute("attribute.name.pokemob.vit", 5));

        EVASION = REGISTER.register("evasion", () -> new StatAttribute("attribute.name.pokemob.evasion", 0));
        ACCURACY = REGISTER.register("accuracy", () -> new StatAttribute("attribute.name.pokemob.accuracy", 0));

        ATTRIBUTES[0] = HP;
        ATTRIBUTES[1] = ATTACK;
        ATTRIBUTES[2] = DEFENSE;
        ATTRIBUTES[3] = SPATTACK;
        ATTRIBUTES[4] = SPDEFENSE;
        ATTRIBUTES[5] = VIT;

        ATTRIBUTES[6] = EVASION;
        ATTRIBUTES[7] = ACCURACY;
    }

    public static final ResourceLocation NATURE = ResourceLocation.parse("pokecube:nature");
    public static final ResourceLocation STAT_MOD = ResourceLocation.parse("pokecube:stat_modifier");

    public static void resetToEntry(IPokemob pokemob)
    {
        var mob = pokemob.getEntity();
        AttributeInstance attr;
        float beforeDamage = mob.getMaxHealth() - mob.getHealth();
        for (int i = 0; i < ATTRIBUTES.length; i++)
        {
            // Checked like this to support possibly partial attributes on non pokemobs.
            if ((attr = mob.getAttribute(ATTRIBUTES[i])) != null)
            {
                // Remove any modifiers
                attr.removeModifier(STAT_MOD);
                attr.removeModifier(NATURE);
                // Hp gets HP function applied
                if (i == 0)
                {
                    int actualStat = pokemob.getMaxHPStat();
                    attr.setBaseValue(actualStat);
                }
                // Stats besides Accuracy and Evasion get theirs applied.
                else if (i < 6)
                {
                    int actualStat = valuesToRegularStat(IPokemob.Stats.values()[i], pokemob);
                    attr.setBaseValue(actualStat);
                    // Check for nature modifier
                    var nature = pokemob.getNature().stats[i];
                    if (nature != 0)
                    {
                        double amt = nature * 0.1;
                        // add the nature multiplier
                        attr.addOrReplacePermanentModifier(
                                new AttributeModifier(NATURE, amt, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                    }
                }
            }
        }
        mob.setHealth(mob.getMaxHealth() - beforeDamage);
        if (pokemob.getEntity().isAddedToLevel()) PacketSyncModifier.sendUpdate(pokemob.getEntity());
    }

    public static double getStatValue(LivingEntity entity, IPokemob.Stats stat)
    {
        var hold = ATTRIBUTES[stat.ordinal()];
        var attr = entity.getAttribute(hold);
        if (attr != null) return attr.getValue();
        return 0;
    }

    public static double getBaseValue(LivingEntity entity, IPokemob.Stats stat)
    {
        var hold = ATTRIBUTES[stat.ordinal()];
        var attr = entity.getAttribute(hold);
        if (attr != null) return attr.getBaseValue();
        return 1;
    }

    public static void setStat(LivingEntity entity, IPokemob.Stats stat, double value)
    {
        var hold = ATTRIBUTES[stat.ordinal()];
        var attr = entity.getAttribute(hold);
        if (attr != null)
        {
            attr.setBaseValue(value);
            PacketSyncModifier.sendUpdate(entity);
        }
    }

    public static int getModifier(LivingEntity entity, IPokemob.Stats stat)
    {
        var hold = ATTRIBUTES[stat.ordinal()];
        var attr = entity.getAttribute(hold);
        if (attr != null && attr.hasModifier(STAT_MOD))
        {
            var stat_mod = attr.getModifier(STAT_MOD);
            return ratioToModifier(stat_mod.amount(), stat.ordinal());
        }
        return 0;
    }

    public static double getNatureModifier(LivingEntity entity, IPokemob.Stats stat)
    {
        var hold = ATTRIBUTES[stat.ordinal()];
        var attr = entity.getAttribute(hold);
        if (attr != null && attr.hasModifier(NATURE))
        {
            var stat_mod = attr.getModifier(NATURE);
            return stat_mod.amount();
        }
        return 1;
    }

    public static double getModifierValue(LivingEntity entity, IPokemob.Stats stat)
    {
        var hold = ATTRIBUTES[stat.ordinal()];
        var attr = entity.getAttribute(hold);
        if (attr != null && attr.hasModifier(STAT_MOD))
        {
            var stat_mod = attr.getModifier(STAT_MOD);
            return stat_mod.amount();
        }
        return 1;
    }

    public static int ratioToModifier(double amount, int index)
    {
        // Accuracy case
        if (index > 5)
        {
            if (amount > 0)
            {
                // Simple linear for increasing stats
                int m = (int) Math.round((amount - 1) * 3);
                return Math.max(Math.min(m, 6), 0);
            }
            int m = (int) -Math.round((1 / amount) * 3 - 3);
            return Math.min(Math.max(m, 0), -6);
        }
        else
        {
            if (amount > 0)
            {
                // Simple linear for increasing stats
                int m = (int) Math.round((amount - 1) * 2);
                return Math.max(Math.min(m, 6), 0);
            }
            int m = (int) -Math.round((1 / amount) * 2 - 2);
            return Math.min(Math.max(m, 0), -6);
        }
    }

    public static double modifierToRatio(int mod, int index)
    {
        if (mod == 0) return 1;
        double m = Math.abs(mod);
        if (index > 5)
        {
            if (mod > 0) return 1 + m / 3;
            return 3 / (m + 3);
        }
        else
        {
            if (mod > 0) return 1 + m / 2;
            return 2 / (m + 2);
        }
    }

    public static int valuesToRegularStat(IPokemob.Stats stat, IPokemob pokemob)
    {
        int IV = pokemob.getIVs()[stat.ordinal()];
        int EV = pokemob.getEVs()[stat.ordinal()] / 4;
        int baseStat = pokemob.getBaseStat(stat);
        return 5 + pokemob.getLevel() * (2 * baseStat + IV + EV) / 100;
    }

    public static void applyStatModifier(LivingEntity entity, IPokemob.Stats stat, int newValue)
    {
        var hold = ATTRIBUTES[stat.ordinal()];
        var attr = entity.getAttribute(hold);
        if (attr != null)
        {
            double modifier = modifierToRatio(newValue, stat.ordinal());
            attr.addOrReplacePermanentModifier(new AttributeModifier(STAT_MOD, modifier, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            PacketSyncModifier.sendUpdate(entity);
        }
    }
}
