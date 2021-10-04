package pokecube.core.blocks.healer;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;

public class HealerTile extends BlockEntity implements TickingBlockEntity
{
    public static SoundEvent MUSICLOOP;

    public boolean play = false;

    public HealerTile()
    {
        super(PokecubeItems.HEALER_TYPE.get());
    }

    @Override
    public void tick()
    {
        if (!PokecubeCore.getConfig().pokeCenterMusic) return;
        if (!this.getLevel().isClientSide || HealerTile.MUSICLOOP == null) return;
        final int power = this.getLevel().getBestNeighborSignal(this.getBlockPos());
        this.play = power > 0;
        PokecubeCore.proxy.pokecenterloop(this, this.play);
    }
}
