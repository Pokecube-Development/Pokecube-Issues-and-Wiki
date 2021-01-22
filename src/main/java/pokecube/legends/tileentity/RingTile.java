package pokecube.legends.tileentity;

import java.util.Random;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.blocks.customblocks.PortalWarp;
import pokecube.legends.blocks.customblocks.PortalWarpPart;
import pokecube.legends.init.BlockInit;

public class RingTile extends TileEntity implements ITickableTileEntity
{
    public static TileEntityType<RingTile> TYPE;

    public int timer = 0;

    public boolean despawns = false;

    public RingTile()
    {
        super(RingTile.TYPE);
    }

    public void activatePortal()
    {
        final PortalWarp warp = (PortalWarp) BlockInit.BLOCK_PORTALWARP.get();
        final BlockState state = this.getBlockState();
        if (this.despawns)
        {
            warp.remove(this.world, this.pos, state);
            this.world.destroyBlock(this.getPos(), false);
        }
        else
        {
            final Random rand = new Random();
            this.timer = PokecubeLegends.config.ticksPerPortalReset;
            this.timer = this.timer / 2 + rand.nextInt(this.timer);
        }
    }

    @Override
    public void tick()
    {
        if (this.getWorld().isRemote) return;
        final BlockState state = this.getBlockState();
        final PortalWarpPart part = state.get(PortalWarp.PART);
        final boolean active = state.get(PortalWarp.ACTIVE);
        if (part != PortalWarpPart.MIDDLE) return;
        final PortalWarp warp = (PortalWarp) BlockInit.BLOCK_PORTALWARP.get();
        if (this.despawns && this.timer++ > PokecubeLegends.config.ticksPortalDespawn)
        {
            warp.remove(this.world, this.pos, state);
            this.world.setBlockState(this.pos, Blocks.AIR.getDefaultState());
            this.world.playSound(this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5,
                    SoundEvents.ENTITY_ENDERMAN_STARE, SoundCategory.BLOCKS, 0.5F, this.world.getRandom().nextFloat()
                            * 0.4F + 0.8F, false);
        }
        else if (!active && this.timer-- < 0)
        {
            this.world.setBlockState(this.pos, state.with(PortalWarp.ACTIVE, true));
            warp.setActiveState(this.world, this.pos, state, true);
            this.timer = 0;
        }
    }

    @Override
    public void read(final BlockState state, final CompoundNBT nbt)
    {
        super.read(state, nbt);
        this.despawns = nbt.getBoolean("despawns");
        this.timer = nbt.getInt("timer");
    }

    @Override
    public CompoundNBT write(final CompoundNBT compound)
    {
        compound.putInt("timer", this.timer);
        compound.putBoolean("despawns", this.despawns);
        return super.write(compound);
    }

}
