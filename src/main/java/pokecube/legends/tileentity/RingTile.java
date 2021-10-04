package pokecube.legends.tileentity;

import java.util.Random;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.blocks.customblocks.PortalWarp;
import pokecube.legends.blocks.customblocks.PortalWarpPart;
import pokecube.legends.init.BlockInit;
import thut.core.common.ThutCore;

public class RingTile extends BlockEntity implements TickingBlockEntity
{
    public static BlockEntityType<RingTile> TYPE;

    public int timer = 0;

    public boolean despawns = false;

    public RingTile()
    {
        super(RingTile.TYPE);
    }

    public void activatePortal()
    {
        final PortalWarp warp = (PortalWarp) BlockInit.PORTAL.get();
        final BlockState state = this.getBlockState();
        if (this.despawns)
        {
            warp.remove(this.level, this.worldPosition, state);
            this.level.destroyBlock(this.getBlockPos(), false);
        }
        else
        {
            final Random rand = ThutCore.newRandom();
            this.timer = PokecubeLegends.config.ticksPerPortalReset;
            this.timer = this.timer / 2 + rand.nextInt(this.timer);
        }
    }

    @Override
    public void tick()
    {
        if (this.getLevel().isClientSide) return;
        final BlockState state = this.getBlockState();
        final PortalWarpPart part = state.getValue(PortalWarp.PART);
        final boolean active = state.getValue(PortalWarp.ACTIVE);
        if (part != PortalWarpPart.MIDDLE) return;
        final PortalWarp warp = (PortalWarp) BlockInit.PORTAL.get();
        if (this.despawns && this.timer++ > PokecubeLegends.config.ticksPortalDespawn)
        {
            warp.remove(this.level, this.worldPosition, state);
            this.level.setBlockAndUpdate(this.worldPosition, Blocks.AIR.defaultBlockState());
            this.level.playLocalSound(this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5,
                    SoundEvents.ENDERMAN_STARE, SoundSource.BLOCKS, 0.5F, this.level.getRandom().nextFloat()
                            * 0.4F + 0.8F, false);
        }
        else if (!active && this.timer-- < 0)
        {
            this.level.setBlockAndUpdate(this.worldPosition, state.setValue(PortalWarp.ACTIVE, true));
            warp.setActiveState(this.level, this.worldPosition, state, true);
            this.timer = 0;
        }
    }

    @Override
    public void load(final BlockState state, final CompoundTag nbt)
    {
        super.load(state, nbt);
        this.despawns = nbt.getBoolean("despawns");
        this.timer = nbt.getInt("timer");
    }

    @Override
    public CompoundTag save(final CompoundTag compound)
    {
        compound.putInt("timer", this.timer);
        compound.putBoolean("despawns", this.despawns);
        return super.save(compound);
    }

}
