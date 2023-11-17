package pokecube.mobs.moves;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.EventPriority;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.moves.IMove;
import pokecube.api.data.moves.MoveApplicationRegistry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.IPokemob.Stats;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.stats.DefaultModifiers;
import pokecube.api.events.init.InitMoveEntry;
import pokecube.api.events.pokemobs.combat.MoveUse.DuringUse;
import pokecube.api.moves.MoveEntry.MoveSounds;
import pokecube.api.moves.MoveEntry.PowerProvider;
import pokecube.api.moves.MoveEntry.TypeProvider;
import pokecube.api.utils.PokeType;
import pokecube.api.utils.Tools.MergeOrder;
import pokecube.core.moves.PokemobTerrainEffects.EntryEffectType;
import pokecube.core.moves.PokemobTerrainEffects.TerrainEffectType;
import pokecube.core.moves.PokemobTerrainEffects.WeatherEffectType;
import pokecube.core.moves.implementations.MovesAdder;
import pokecube.core.moves.templates.Move_Ongoing;
import pokecube.core.moves.templates.TerrainMove;
import pokecube.mobs.moves.attacks.FireSpin;
import pokecube.mobs.moves.attacks.Infestation;
import pokecube.mobs.moves.attacks.Leechseed;
import pokecube.mobs.moves.attacks.Perishsong;
import pokecube.mobs.moves.attacks.Taunt;
import pokecube.mobs.moves.attacks.Whirlpool;
import pokecube.mobs.moves.attacks.Yawn;
import thut.core.common.ThutCore;

public class MoveRegister
{
    public static void init()
    {
        MovesAdder.worldActionPackages.add(MoveRegister.class.getPackage());
        MovesAdder.moveRegistryPackages.add(MoveRegister.class.getPackage());

        // Ours is registered as HIGH so that other addons can replace if
        // needed.
        PokecubeAPI.MOVE_BUS.addListener(EventPriority.HIGH, MoveRegister::onMoveInit);
        PokecubeAPI.MOVE_BUS.addListener(MoveRegister::preMoveUse);
    }

    private static final Map<String, TypeProvider> TYPES = Maps.newHashMap();
    private static final Map<String, PowerProvider> POWER = Maps.newHashMap();
    private static final Map<String, Move_Ongoing> ONGOING = Maps.newHashMap();
    private static final Map<String, IMove> CUSTOM = Maps.newHashMap();

    private static void moveTypeChangers()
    {
        TYPES.put("hidden-power", new TypeProvider()
        {
            PokeType[] types;

            @Override
            public PokeType getType(IPokemob user)
            {
                if (user == null) return PokeType.unknown;

                if (types == null) types = new PokeType[]
                { PokeType.getType("fighting"), PokeType.getType("flying"), PokeType.getType("poison"),
                        PokeType.getType("ground"), PokeType.getType("rock"), PokeType.getType("bug"),
                        PokeType.getType("ghost"), PokeType.getType("steel"), PokeType.getType("fire"),
                        PokeType.getType("water"), PokeType.getType("grass"), PokeType.getType("electric"),
                        PokeType.getType("psychic"), PokeType.getType("ice"), PokeType.getType("dragon"),
                        PokeType.getType("dark") };
                int index = 0;
                final byte[] ivs = user.getIVs();
                final int a = ivs[0] & 1;
                final int b = ivs[1] & 1;
                final int c = ivs[2] & 1;
                final int d = ivs[5] & 1;
                final int e = ivs[3] & 1;
                final int f = ivs[4] & 1;
                final int abcdef = (a + 2 * b + 4 * c + 8 * d + 16 * e + 32 * f) * 15;
                index = abcdef / 63;
                return types[index];
            }
        });

        TYPES.put("judgment", user -> {
            if (user == null) return PokeType.unknown;
            return user.getType1();
        });

        TYPES.put("multi-attack", user -> {
            if (user == null) return PokeType.unknown;
            return user.getType1();
        });

    }

    private static void movePowerChangers()
    {
        POWER.put("hidden-power", (IPokemob user, LivingEntity target, int pwr) -> {
            final byte[] ivs = user.getIVs();
            final int u = (ivs[0] & 2) / 2;
            final int v = (ivs[1] & 2) / 2;
            final int w = (ivs[2] & 2) / 2;
            final int x = (ivs[5] & 2) / 2;
            final int y = (ivs[3] & 2) / 2;
            final int z = (ivs[4] & 2) / 2;
            pwr = 30 + (u + 2 * v + 4 * w + 8 * x + 16 * y + 32 * z) * 40 / 63;
            return pwr;
        });

        POWER.put("super-fang", (IPokemob user, LivingEntity target, int pwr) -> {
            return (int) Math.ceil(target.getHealth() / 2);
        });

        PowerProvider FURY_CUTTER = (IPokemob user, LivingEntity target, int pwr) -> {
            final double rollOut = user.getMoveStats().FURYCUTTERCOUNTER;
            final int PWR = (int) Math.max(pwr, Math.min(160, rollOut * 2 * pwr));
            return PWR;
        };
        PowerProvider ECHO_VOICE = (IPokemob user, LivingEntity target, int pwr) -> {
            final double rollOut = user.getMoveStats().FURYCUTTERCOUNTER;
            final int PWR = (int) Math.max(pwr, Math.min(200, rollOut * 2 * pwr));
            return PWR;
        };

        POWER.put("echoed-voice", ECHO_VOICE);
        POWER.put("fury-cutter", FURY_CUTTER);

        POWER.put("electro-ball", (IPokemob user, LivingEntity target, int pwr) -> {
            final IPokemob targetMob = PokemobCaps.getPokemobFor(target);
            if (targetMob == null) return 50;
            final int targetSpeed = targetMob.getStat(Stats.VIT, true);
            final int userSpeed = user.getStat(Stats.VIT, true);
            pwr = 60;
            final double var = (double) targetSpeed / (double) userSpeed;
            if (var < 0.25) pwr = 150;
            else if (var < 0.33) pwr = 120;
            else if (var < 0.5) pwr = 80;
            else pwr = 60;
            return pwr;
        });

        POWER.put("grass-knot", (IPokemob user, LivingEntity target, int pwr) -> {
            pwr = 120;
            final IPokemob targetMob = PokemobCaps.getPokemobFor(target);
            if (targetMob == null) return pwr;
            final double mass = targetMob.getWeight();
            if (mass < 10) return 20;
            if (mass < 25) return 40;
            if (mass < 50) return 60;
            if (mass < 100) return 80;
            if (mass < 200) return 100;
            return pwr;
        });

        POWER.put("gyro-ball", (IPokemob user, LivingEntity target, int pwr) -> {
            final IPokemob targetMob = PokemobCaps.getPokemobFor(target);
            if (targetMob == null) return 50;
            final int targetSpeed = targetMob.getStat(Stats.VIT, true);
            final int userSpeed = user.getStat(Stats.VIT, true);
            pwr = 25 * targetSpeed / userSpeed;
            return pwr;
        });

        POWER.put("low-kick", (IPokemob user, LivingEntity target, int pwr) -> {
            pwr = 120;
            final IPokemob targetMob = PokemobCaps.getPokemobFor(target);
            if (targetMob == null) return pwr;
            final double mass = targetMob.getWeight();
            if (mass < 10) return 20;
            if (mass < 25) return 40;
            if (mass < 50) return 60;
            if (mass < 100) return 80;
            if (mass < 200) return 100;
            return pwr;
        });

        POWER.put("heat-crash", (IPokemob user, LivingEntity target, int pwr) -> {
            pwr = 80;
            final IPokemob targetMob = PokemobCaps.getPokemobFor(target);
            if (targetMob == null) return pwr;
            final double mass = targetMob.getWeight() / user.getWeight();
            if (mass <= 0.2) return 120;
            if (mass <= 0.25) return 100;
            if (mass <= 0.33) return 80;
            if (mass <= 0.5) return 60;
            return 40;
        });

        POWER.put("night-shade", (IPokemob user, LivingEntity target, int pwr) -> {
            return user.getLevel();
        });

        POWER.put("seismic-toss", (IPokemob user, LivingEntity target, int pwr) -> {
            return user.getLevel();
        });

        POWER.put("acrobatics", (IPokemob user, LivingEntity target, int pwr) -> {
            int bonus = 1;
            if (user.getHeldItem().isEmpty()) bonus = 2;
            return pwr * bonus;
        });

        POWER.put("magnitude", (IPokemob user, LivingEntity target, int pwr) -> {
            pwr = 0;
            final int rand = ThutCore.newRandom().nextInt(20);
            if (rand == 0) pwr = 10;
            else if (rand <= 2) pwr = 30;
            else if (rand <= 6) pwr = 50;
            else if (rand <= 12) pwr = 70;
            else if (rand <= 16) pwr = 90;
            else if (rand <= 18) pwr = 110;
            else pwr = 150;
            return pwr;
        });

        POWER.put("rollout", (IPokemob user, LivingEntity target, int pwr) -> {
            final double defCurl = user.getMoveStats().DEFENSECURLCOUNTER > 0 ? 2 : 1;
            double rollOut = user.getMoveStats().ROLLOUTCOUNTER;
            if (rollOut > 4) rollOut = user.getMoveStats().ROLLOUTCOUNTER = 0;
            rollOut = Math.max(0, rollOut);
            return (int) (Math.pow(2, rollOut) * pwr * defCurl);
        });

        POWER.put("ice-ball", (IPokemob user, LivingEntity target, int pwr) -> {
            final double defCurl = user.getMoveStats().DEFENSECURLCOUNTER > 0 ? 2 : 1;
            double rollOut = user.getMoveStats().ROLLOUTCOUNTER;
            if (rollOut > 4) rollOut = user.getMoveStats().ROLLOUTCOUNTER = 0;
            rollOut = Math.max(0, rollOut);
            return (int) (Math.pow(2, rollOut) * pwr * defCurl);
        });

        POWER.put("psywave", (IPokemob user, LivingEntity target, int pwr) -> {
            final int lvl = user.getLevel();
            pwr = (int) Math.max(1, lvl * (Math.random() + 0.5));
            return pwr;
        });

        POWER.put("stored-power", (IPokemob user, LivingEntity target, int pwr) -> {
            final DefaultModifiers mods = user.getModifiers().getDefaultMods();
            for (final Stats stat : Stats.values())
            {
                final float b = mods.getModifierRaw(stat);
                if (b > 0) pwr += 20 * b;
            }
            return pwr;
        });

        POWER.put("present", (IPokemob user, LivingEntity target, int pwr) -> {
            final double rand = ThutCore.newRandom().nextDouble();
            if (rand < 0.4) return 40;
            if (rand < 0.7) return 80;
            if (rand < 0.8) return 120;
            return 0;
        });
    }

    private static void ongoingMoves()
    {
        ONGOING.put("fire-spin", new FireSpin());
        ONGOING.put("infestation", new Infestation());
        ONGOING.put("leech-seed", new Leechseed());
        ONGOING.put("perish-song", new Perishsong());
        ONGOING.put("whirlpool", new Whirlpool());
        ONGOING.put("yawn", new Yawn());
        ONGOING.put("taunt", new Taunt());
    }

    private static void terrainMoves()
    {
        // Weather moves
        CUSTOM.put("mist", TerrainMove.forEffect(WeatherEffectType.MIST));
        CUSTOM.put("sandstorm", TerrainMove.forEffect(WeatherEffectType.SAND));
        CUSTOM.put("rain-dance", TerrainMove.forEffect(WeatherEffectType.RAIN));
        CUSTOM.put("sunny-day", TerrainMove.forEffect(WeatherEffectType.SUN));
        CUSTOM.put("hail", TerrainMove.forEffect(WeatherEffectType.HAIL));

        // Terrain moves
        CUSTOM.put("mud-sport", TerrainMove.forEffect(TerrainEffectType.MUD));
        CUSTOM.put("water-sport", TerrainMove.forEffect(TerrainEffectType.WATER));
        CUSTOM.put("grassy-terrain", TerrainMove.forEffect(TerrainEffectType.GRASS));
        CUSTOM.put("misty-terrain", TerrainMove.forEffect(TerrainEffectType.MISTY));
        CUSTOM.put("electric-terrain", TerrainMove.forEffect(TerrainEffectType.ELECTRIC));
        CUSTOM.put("psychic-terrain", TerrainMove.forEffect(TerrainEffectType.PHYSIC));

        // Entry hazards
        CUSTOM.put("spikes", TerrainMove.forEffect(EntryEffectType.SPIKES));
        CUSTOM.put("toxic-spikes", TerrainMove.forEffect(EntryEffectType.POISON));
        CUSTOM.put("stealth-rock", TerrainMove.forEffect(EntryEffectType.ROCKS));
        CUSTOM.put("sticky-web", TerrainMove.forEffect(EntryEffectType.WEBS));
    }

    static
    {
        moveTypeChangers();
        movePowerChangers();
        ongoingMoves();
        terrainMoves();
    }

    private static void onMoveInit(InitMoveEntry event)
    {
        String name = event.getEntry().getName();
        if (POWER.containsKey(name))
        {
            event.getEntry().powerp = POWER.get(name);
            event.getEntry().root_entry._manually_defined = true;
        }
        if (TYPES.containsKey(name))
        {
            event.getEntry().typer = TYPES.get(name);
        }
        if (ONGOING.containsKey(name))
        {
            MoveApplicationRegistry.registerOngoingEffect(event.getEntry(), ONGOING.get(name));
        }
        if (CUSTOM.containsKey(name))
        {
            MoveApplicationRegistry.addMoveModifier(event.getEntry(), MergeOrder.BEFORE, CUSTOM.get(name));
        }
    }

    private static void preMoveUse(DuringUse.Pre event)
    {
        String name = event.getMove().getName();
        if ("growl".equals(name) || "sing".equals(name))
        {
            event.getPacket().sounds = new MoveSounds(event.getUser().getSound(), null);
        }
    }
}
