package pokecube.compat.cct.modules;

import pokecube.adventures.blocks.siphon.SiphonTile;

public class Siphon extends BasePeripheral<SiphonTile>
{
    public static class Provider
    {
        private final SiphonTile tile;

        public Provider(final SiphonTile tile)
        {
            this.tile = tile;
        }

        public int getMaxPower()
        {
            return this.tile.energy.theoreticalOutput;
        }

        public int getActualPower()
        {
            return this.tile.energy.currentOutput;
        }
    }

    private final Provider provider;

    public Siphon(final SiphonTile tile)
    {
        super(tile, "pokesiphon");
        this.provider = new Provider(tile);
    }

    @Override
    public Object getTarget()
    {
        return this.provider;
    }
}
