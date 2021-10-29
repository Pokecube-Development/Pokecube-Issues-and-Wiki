package pokecube.adventures.blocks.statue;

import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
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
import thut.core.common.network.TileUpdate;

public class StatueEntity extends BlockEntity
{
    public int ticks = 0;

    public StatueEntity(final BlockEntityType<?> type, final BlockPos pos, final BlockState state)
    {
        super(type, pos, state);
    }

    public StatueEntity(final BlockPos pos, final BlockState state)
    {
        this(PokecubeAdv.STATUE_TYPE.get(), pos, state);
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
                final Direction dir = this.getBlockState().getValue(HorizontalDirectionalBlock.FACING);
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
    public ClientboundBlockEntityDataPacket getUpdatePacket()
    {
        this.checkMob();
        return new ClientboundBlockEntityDataPacket(this.worldPosition, 6, this.getUpdateTag());
    }

    @Override
    public CompoundTag getUpdateTag()
    {
        this.checkMob();
        return this.serializeNBT();
    }

    @Override
    public void handleUpdateTag(final CompoundTag tag)
    {
        this.deserializeNBT(tag);
        this.checkMob();
    }

    @Override
    public void load(final CompoundTag tag)
    {
        super.load(tag);
        // The stuff below only matters for when this is placed directly or nbt
        // edited. when loading normally, level is null, so we exit here.
        if (this.level == null) return;
        final ICopyMob copy = CopyCaps.get(this);
        if (tag.contains("custom_model"))
        {
            final CompoundTag modelTag = tag.getCompound("custom_model");
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
            final IAnimationHolder anims = copy.getCopiedMob().getCapability(ThutCaps.ANIMCAP).orElse(null);
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
