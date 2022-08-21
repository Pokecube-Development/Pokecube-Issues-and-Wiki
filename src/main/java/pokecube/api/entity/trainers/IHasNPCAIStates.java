package pokecube.api.entity.trainers;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

public interface IHasNPCAIStates extends INBTSerializable<CompoundTag>
{
    public static enum AIState
    {
        STATIONARY(1 << 0), INBATTLE(1 << 1, true), THROWING(1 << 2, true), PERMFRIENDLY(1 << 3),
        FIXEDDIRECTION(1 << 4), MATES(1 << 5, false, true), INVULNERABLE(1 << 6), TRADES_ITEMS(1 << 7, false, true),
        TRADES_MOBS(1 << 8, false, true);

        private final int mask;

        private final boolean temporary;
        private final boolean _default;

        private AIState(final int mask)
        {
            this(mask, false, false);
        }

        private AIState(final int mask, final boolean temporary)
        {
            this(mask, temporary, false);
        }

        private AIState(final int mask, final boolean temporary, final boolean _default)
        {
            this.mask = mask;
            this.temporary = temporary;
            this._default = _default;
        }

        public int getMask()
        {
            return this.mask;
        }

        public boolean isTemporary()
        {
            return this.temporary;
        }

        public boolean getDefault()
        {
            return _default;
        }
    }

    boolean getAIState(AIState state);

    /** @return Direction to face if FIXEDDIRECTION */
    public float getDirection();

    int getTotalState();

    void setAIState(AIState state, boolean flag);

    /**
     * @param direction Direction to face if FIXEDDIRECTION
     */
    public void setDirection(float direction);

    void setTotalState(int state);
}