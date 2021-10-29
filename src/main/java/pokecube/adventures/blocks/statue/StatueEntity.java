package pokecube.adventures.blocks.statue;

import java.util.UUID;

import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.LazyOptional;
import pokecube.adventures.PokecubeAdv;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.FormeHolder;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import thut.api.ThutCaps;
import thut.api.entity.CopyCaps;
import thut.api.entity.IAnimated.IAnimationHolder;
import thut.api.entity.ICopyMob;
import thut.api.entity.IMobColourable;
import thut.api.entity.animation.CapabilityAnimation;
import thut.core.common.network.TileUpdate;

public class StatueEntity extends TileEntity
{
    public int ticks = 0;

    public StatueEntity(final TileEntityType<?> type)
    {
        super(type);
    }

    public StatueEntity()
    {
        super(PokecubeAdv.STATUE_TYPE.get());
    }

    public void checkMob()
    {
        final ICopyMob copy = CopyCaps.get(this);
        check:
        if (copy != null)
        {
            LivingEntity before = copy.getCopiedMob();
            if (before == null)
            {
                copy.setCopiedMob(before = PokecubeCore.createPokemob(Database.missingno, this.level));
                if (copy.getCopiedID() == null) copy.setCopiedID(before.getType().getRegistryName());
                if (!copy.getCopiedNBT().isEmpty()) before.deserializeNBT(copy.getCopiedNBT());
                before = null;
            }
            copy.onBaseTick(this.level, null);
            if (copy.getCopiedMob() == null) break check;
            if (copy.getCopiedMob() != before)
            {
                final BlockPos pos = this.getBlockPos();
                final LivingEntity mob = copy.getCopiedMob();
                final LazyOptional<IMobColourable> colourable = mob.getCapability(ThutCaps.COLOURABLE);
                if (colourable.isPresent()) colourable.orElse(null).getRGBA();
                mob.setUUID(UUID.randomUUID());
                mob.setPos(pos.getX(), pos.getY(), pos.getZ());
                final Direction dir = this.getBlockState().getValue(HorizontalBlock.FACING);
                switch (dir)
                {
                case EAST:
                    mob.yRot = mob.yBodyRot = mob.yRotO = mob.yBodyRotO = -90;
                    break;
                case NORTH:
                    mob.yRot = mob.yBodyRot = mob.yRotO = mob.yBodyRotO = 180;
                    break;
                case SOUTH:
                    mob.yRot = mob.yBodyRot = mob.yRotO = mob.yBodyRotO = 0;
                    break;
                case WEST:
                    mob.yRot = mob.yBodyRot = mob.yRotO = mob.yBodyRotO = 90;
                    break;
                default:
                    break;
                }
                copy.setCopiedNBT(copy.getCopiedMob().serializeNBT());
                this.requestModelDataUpdate();
            }
        }
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket()
    {
        this.checkMob();
        return new SUpdateTileEntityPacket(this.worldPosition, 6, this.getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag()
    {
        this.checkMob();
        return this.serializeNBT();
    }

    @Override
    public void handleUpdateTag(final BlockState state, final CompoundNBT tag)
    {
        this.deserializeNBT(tag);
        this.checkMob();
    }

    @Override
    public void load(final BlockState state, final CompoundNBT tag)
    {
        super.load(state, tag);
        // The stuff below only matters for when this is placed directly or nbt
        // edited. when loading normally, level is null, so we exit here.
        if (this.level == null) return;
        final ICopyMob copy = CopyCaps.get(this);
        if (tag.contains("custom_model"))
        {
            final CompoundNBT modelTag = tag.getCompound("custom_model");
            String tex = null;
            String anim = null;
            String id = null;
            float size = 1;
            if (modelTag.contains("id")) id = modelTag.getString("id");
            if (modelTag.contains("tex")) tex = modelTag.getString("tex");
            if (modelTag.contains("anim")) anim = modelTag.getString("anim");
            if (modelTag.contains("size")) size = modelTag.getFloat("size");

            // First update ID if present, and refresh the mob
            if (id != null)
            {
                copy.setCopiedID(new ResourceLocation(id));
                copy.setCopiedMob(null);
            }
            this.checkMob();
            final IPokemob pokemob = CapabilityPokemob.getPokemobFor(copy.getCopiedMob());
            if (tex != null && pokemob != null)
            {
                final ResourceLocation texRes = new ResourceLocation(tex);
                final ResourceLocation name = new ResourceLocation(texRes.getNamespace(), pokemob.getPokedexEntry()
                        .getTrimmedName() + texRes.getPath());
                final FormeHolder old = pokemob.getCustomHolder();
                final ResourceLocation model = old != null ? old.model : null;
                final ResourceLocation animation = old != null ? old.animation : null;
                final FormeHolder holder = FormeHolder.get(model, texRes, animation, name);
                pokemob.setCustomHolder(holder);
            }
            if (pokemob != null) pokemob.setSize(size);
            final IAnimationHolder anims = copy.getCopiedMob().getCapability(CapabilityAnimation.CAPABILITY).orElse(
                    null);
            if (anim != null && anims != null)
            {
                anims.setFixed(true);
                anims.overridePlaying(anim);
            }
            copy.setCopiedNBT(copy.getCopiedMob().serializeNBT());
        }
        // Server side send packet that it changed
        if (!this.level.isClientSide()) TileUpdate.sendUpdate(this);
        // Client side clear the mob
        else copy.setCopiedMob(null);
        // Both sides refresh mob if changed
        this.checkMob();
    }
}
