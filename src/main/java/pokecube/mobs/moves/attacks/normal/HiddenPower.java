package pokecube.mobs.moves.attacks.normal;

import net.minecraft.entity.Entity;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.moves.templates.Move_Basic;
import pokecube.core.utils.PokeType;

public class HiddenPower extends Move_Basic
{
    private static PokeType[] types;

    public HiddenPower()
    {
        super("hiddenpower");
    }

    /**
     * PWR getter
     *
     * @return the power of this move
     */
    @Override
    public int getPWR(IPokemob user, Entity target)
    {
        final byte[] ivs = user.getIVs();
        final int u = (ivs[0] & 2) / 2;
        final int v = (ivs[1] & 2) / 2;
        final int w = (ivs[2] & 2) / 2;
        final int x = (ivs[5] & 2) / 2;
        final int y = (ivs[3] & 2) / 2;
        final int z = (ivs[4] & 2) / 2;
        final int pwr = 30 + (u + 2 * v + 4 * w + 8 * x + 16 * y + 32 * z) * 40 / 63;
        return pwr;
    }

    /**
     * Type getter
     *
     * @return the type of this move
     */
    @Override
    public PokeType getType(IPokemob user)
    {
        if (user == null) return this.move.type;

        if (HiddenPower.types == null) HiddenPower.types = new PokeType[] { PokeType.getType("fighting"),
                PokeType.getType("flying"), PokeType.getType("poison"), PokeType.getType("ground"), PokeType.getType(
                        "rock"), PokeType.getType("bug"), PokeType.getType("ghost"), PokeType.getType("steel"), PokeType
                                .getType("fire"), PokeType.getType("water"), PokeType.getType("grass"), PokeType
                                        .getType("electric"), PokeType.getType("psychic"), PokeType.getType("ice"),
                PokeType.getType("dragon"), PokeType.getType("dark") };
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
        return HiddenPower.types[index];
    }
}
