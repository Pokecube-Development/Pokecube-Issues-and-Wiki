package pokecube.core.moves.damage.sources;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import pokecube.core.PokecubeCore;

public class PokecubeDamageSources
{
    public static final ResourceKey<DamageType> POKEMOB_ATTACK_CONTACT;
    public static final ResourceKey<DamageType> POKEMOB_ATTACK_RANGED;
    public static final ResourceKey<DamageType> POKEMOB_STATUS;
    public static final ResourceKey<DamageType> POKEMOB_ONGOING;
    public static final ResourceKey<DamageType> POKEMOB_GENERIC;
    public static final ResourceKey<DamageType> TERRAIN_EFFECT;

    public static final TagKey<DamageType> POKEMOB_DAMAGE = TagKey.create(Registries.DAMAGE_TYPE,
            ResourceLocation.parse("pokecube:pokemob_damage"));

    static
    {
        POKEMOB_ATTACK_CONTACT = ResourceKey.create(Registries.DAMAGE_TYPE,
                ResourceLocation.parse("pokecube:pokemob_attack_contact"));
        POKEMOB_ATTACK_RANGED = ResourceKey.create(Registries.DAMAGE_TYPE,
                ResourceLocation.parse("pokecube:pokemob_attack_ranged"));
        POKEMOB_STATUS = ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.parse("pokecube:pokemob_status"));
        POKEMOB_ONGOING = ResourceKey.create(Registries.DAMAGE_TYPE,
                ResourceLocation.parse("pokecube:pokemob_ongoing"));
        POKEMOB_GENERIC = ResourceKey.create(Registries.DAMAGE_TYPE,
                ResourceLocation.parse("pokecube:pokemob_generic"));
        TERRAIN_EFFECT = ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.parse("pokecube:pokemob_terrain"));
    }

    public static Holder<DamageType> pokemobAttackContact()
    {
        return PokecubeCore.proxy.getRegistries().holderOrThrow(POKEMOB_ATTACK_CONTACT);
    }

    public static Holder<DamageType> pokemobAttackRanged()
    {
        return PokecubeCore.proxy.getRegistries().holderOrThrow(POKEMOB_ATTACK_RANGED);
    }

    public static Holder<DamageType> pokemobStatus()
    {
        return PokecubeCore.proxy.getRegistries().holderOrThrow(POKEMOB_STATUS);
    }

    public static Holder<DamageType> pokemobOngoing()
    {
        return PokecubeCore.proxy.getRegistries().holderOrThrow(POKEMOB_ONGOING);
    }

    public static Holder<DamageType> pokemobGeneric()
    {
        return PokecubeCore.proxy.getRegistries().holderOrThrow(POKEMOB_GENERIC);
    }

    public static Holder<DamageType> pokemobTerrain()
    {
        return PokecubeCore.proxy.getRegistries().holderOrThrow(TERRAIN_EFFECT);
    }
}
