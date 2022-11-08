package thut.tech.common.blocks.lift;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeBlockEntity;
import thut.api.block.ITickTile;
import thut.api.entity.blockentity.IBlockEntity;
import thut.api.entity.blockentity.world.IBlockEntityWorld;
import thut.api.maths.Vector3;
import thut.core.common.network.TileUpdate;
import thut.tech.common.TechCore;
import thut.tech.common.entity.EntityLift;
import thut.tech.common.network.PacketLift;

public class ControllerTile extends BlockEntity implements ITickTile// ,
// SimpleComponent
{

    public int power = 0;
    public int prevPower = 1;
    private EntityLift lift;
    public BlockState copiedState = null;
    boolean listNull = false;
    List<Entity> list = new ArrayList<>();
    Vector3 here;
    public ControllerTile rootNode;
    public Vector<ControllerTile> connected = new Vector<>();
    Direction sourceSide;
    boolean loaded = false;
    public int floor = 0;
    public UUID liftID = null;
    UUID empty = new UUID(0, 0);
    private byte[] sides = new byte[6];
    private byte[] sidePages = new byte[6];
    int tries = 0;
    public boolean toClear = false;
    public boolean first = true;
    public boolean read = false;
    public boolean redstone = true;
    public boolean powered = false;
    public boolean[] callFaces = new boolean[6];
    public boolean[] editFace = new boolean[6];
    public boolean[] floorDisplay = new boolean[6];

    // Used for limiting how often checks for connected controllers are done.
    private int tick = 0;

    public ControllerTile(final BlockPos pos, final BlockState state)
    {
        this(TechCore.CONTROLTYPE.get(), pos, state);
    }

    public ControllerTile(final BlockEntityType<?> tileEntityTypeIn, final BlockPos pos, final BlockState state)
    {
        super(tileEntityTypeIn, pos, state);
    }

    public void buttonPress(final int button, final boolean callPanel)
    {
        if (callPanel && this.getLift() != null) this.getLift().call(this.floor);
        else if (button != 0 && button <= this.getLift().maxFloors() && this.getLift() != null
                && this.getLift().hasFloor(button))
        {
            if (button == this.floor)
            {}
            else if (this.getLift().getCurrentFloor() == this.floor) this.getLift().setCurrentFloor(-1);
            this.getLift().call(button);
        }
    }

    public boolean checkSides()
    {
        final List<EntityLift> check = this.level.getEntitiesOfClass(EntityLift.class,
                new AABB(this.getBlockPos().getX() + 0.5 - 1, this.getBlockPos().getY(),
                        this.getBlockPos().getZ() + 0.5 - 1, this.getBlockPos().getX() + 0.5 + 1,
                        this.getBlockPos().getY() + 1, this.getBlockPos().getZ() + 0.5 + 1));
        if (check != null && check.size() > 0)
        {
            this.setLift(check.get(0));
            this.liftID = this.getLift().getUUID();
        }
        return !(check == null || check.isEmpty());
    }

    public String connectionInfo()
    {
        final String ret = "";
        return ret;
    }

    public boolean doButtonClick(final LivingEntity clicker, final Direction side, final float hitX, final float hitY,
            final float hitZ)
    {
        final int button = this.getButtonFromClick(side, hitX, hitY, hitZ);
        final boolean valid = this.getLift() != null && this.getLift().hasFloor(button);
        if (!this.isSideOn(side)) return false;
        if (this.isEditMode(side))
        {
            if (!this.getLevel().isClientSide)
            {
                String message = "msg.callPanel";
                switch (button)
                {
                case 1:
                    this.callFaces[side.ordinal()] = !this.isCallPanel(side);
                    this.floorDisplay[side.ordinal()] = false;
                    clicker.sendMessage(new TranslatableComponent(message, this.isCallPanel(side)), Util.NIL_UUID);
                    break;
                case 2:
                    this.floorDisplay[side.ordinal()] = !this.isFloorDisplay(side);
                    this.callFaces[side.ordinal()] = false;
                    message = "msg.floorDisplay";
                    clicker.sendMessage(new TranslatableComponent(message, this.isFloorDisplay(side)), Util.NIL_UUID);
                    break;
                case 13:
                    if (this.getLift() != null) this.setLift(null);
                    break;
                case 16:
                    this.editFace[side.ordinal()] = false;
                    message = "msg.editMode";
                    clicker.sendMessage(new TranslatableComponent(message, false), Util.NIL_UUID);
                    break;
                }
                if (clicker instanceof ServerPlayer) this.sendUpdate((ServerPlayer) clicker);
            }
            return true;
        }
        else if (this.isFloorDisplay(side)) return false;
        else if (this.getLevel().isClientSide)
        {
            final boolean callPanel = this.isCallPanel(side);
            PacketLift.sendButtonPress(this.getLift(), this.getBlockPos(), button, callPanel);
        }
        if (clicker instanceof ServerPlayer) this.sendUpdate((ServerPlayer) clicker);
        return valid;
    }

    public int getButtonFromClick(final Direction side, double x, double y, double z)
    {
        int ret = 0;
        x -= this.getBlockPos().getX();
        y -= this.getBlockPos().getY();
        z -= this.getBlockPos().getZ();

        x = x % 1f;
        y = y % 1f;
        z = z % 1f;
        x = Math.abs(x);
        y = Math.abs(y);
        z = Math.abs(z);
        final int page = this.getSidePage(side);

        switch (side.get3DDataValue())
        {
        case 0:
        {
            return 0 + 16 * page;
        }
        case 1:
        {
            ret = 1 + (int) ((1 - x) * 4 % 4) + 4 * (int) ((1 - z) * 4 % 4);
            return ret + 16 * page;
        }
        case 2:
        {
            ret = 1 + (int) ((1 - x) * 4 % 4) + 4 * (int) ((1 - y) * 4 % 4);
            return ret + 16 * page;
        }
        case 3:
        {
            ret = 1 + (int) (x * 4 % 4) + 4 * (int) ((1 - y) * 4 % 4);
            return ret + 16 * page;
        }
        case 4:
        {
            ret = 1 + 4 * (int) ((1 - y) * 4 % 4) + (int) (z * 4 % 4);
            return ret + 16 * page;
        }
        case 5:
        {
            ret = 1 + 4 * (int) ((1 - y) * 4 % 4) + (int) ((1 - z) * 4 % 4);
            return ret + 16 * page;
        }
        default:
        {
            return 0 + 16 * page;
        }

        }

    }

    @Override
    public AABB getRenderBoundingBox()
    {
        final AABB bb = IForgeBlockEntity.INFINITE_EXTENT_AABB;
        return bb;
    }

    public int getSidePage(final Direction side)
    {
        if (this.isEditMode(side)) return 0;
        return this.sidePages[side.get3DDataValue()];
    }

    @Override
    public CompoundTag getUpdateTag()
    {
        return this.saveWithoutMetadata();
    }

    @Override
    public void handleUpdateTag(final CompoundTag tag)
    {
        this.load(tag);
    }

    public boolean isSideOn(final Direction side)
    {
        final int state = 1;
        final byte byte0 = this.sides[side.get3DDataValue()];
        return (byte0 & state) != 0;
    }

    public boolean isCallPanel(final Direction side)
    {
        return this.callFaces[side.get3DDataValue()];
    }

    public boolean isFloorDisplay(final Direction side)
    {
        return this.floorDisplay[side.get3DDataValue()];
    }

    public boolean isEditMode(final Direction side)
    {
        return this.editFace[side.get3DDataValue()];
    }

    @Override
    public void load(final CompoundTag par1)
    {
        super.load(par1);
        this.floor = par1.getInt("floor");
        // Reset this so that it will re-find after loading.
        this.lift = null;
        if (par1.hasUUID("lift"))
        {
            this.liftID = par1.getUUID("lift");
            System.out.println(liftID);
        }
        this.sides = par1.getByteArray("sides");
        for (final Direction face : Direction.Plane.HORIZONTAL)
            this.callFaces[face.ordinal()] = par1.getBoolean(face + "Call");
        for (final Direction face : Direction.Plane.HORIZONTAL)
            this.editFace[face.ordinal()] = par1.getBoolean(face + "Edit");
        for (final Direction face : Direction.Plane.HORIZONTAL)
            this.floorDisplay[face.ordinal()] = par1.getBoolean(face + "Display");
        if (this.sides.length != 6) this.sides = new byte[6];
        this.sidePages = par1.getByteArray("sidePages");
        if (this.sidePages.length != 6) this.sidePages = new byte[6];
        if (par1.contains("state"))
        {
            final CompoundTag state = par1.getCompound("state");
            this.copiedState = NbtUtils.readBlockState(state);
        }
        System.out.println(this.level + " " + par1);
    }

    public void sendUpdate(final ServerPlayer player)
    {
        if (this.level instanceof IBlockEntityWorld) return;
        TileUpdate.sendUpdate(this);
    }

    public void setFloor(final int floor)
    {
        // no lift, no set floor.
        if (this.getLift() == null) return;
        // Set the lift's floor to here
        if (this.getLift().setFoor(this, floor))
        {
            this.floor = floor;
            this.setChanged();
        }
    }

    public void setLift(final EntityLift lift)
    {
        if (lift == null && this.lift != null) this.lift.setFoor(null, this.floor);
        this.lift = lift;
        if (lift != null) this.liftID = lift.getUUID();
        else this.liftID = null;
        if (this.level != null && !this.level.isClientSide) TileUpdate.sendUpdate(this);
    }

    public void setSide(final Direction side, final boolean flag)
    {
        final int state = 1;
        final byte byte0 = this.sides[side.get3DDataValue()];
        if (side.get3DDataValue() < 2) return;
        if (flag) this.sides[side.get3DDataValue()] = (byte) (byte0 | state);
        else this.sides[side.get3DDataValue()] = (byte) (byte0 & -state - 1);
        this.setChanged();
    }

    public void setSidePage(final Direction side, final int page)
    {
        this.sidePages[side.get3DDataValue()] = (byte) page;
    }

    /** Sets the worldObj for this tileEntity. */
    public void setWorldObj(final Level worldIn)
    {
        this.level = worldIn;
        if (worldIn instanceof IBlockEntityWorld)
        {
            // TODO replace this with something like a built in tag?
            final IBlockEntity blockEntity = ((IBlockEntityWorld) worldIn).getBlockEntity();
            if (blockEntity instanceof EntityLift) this.setLift((EntityLift) blockEntity);
        }
    }

    @Override
    public void tick()
    {
        if (this.here == null) this.here = new Vector3();
        this.here.set(this);
        EntityLift lift = this.getLift();

        // Processing beyond here is only server side!
        if (this.getLevel().isClientSide) return;
        if (this.level instanceof IBlockEntityWorld) return;

        // Cleanup floor if the lift is gone.
        if (this.floor > 0 && (lift == null || !lift.isAlive()))
        {
            this.setLift(null);
            lift = null;
            this.floor = 0;
        }
        // Scan sides for a controller which actually has a lift attached, and
        // attach self to that floor.
        if (lift == null && this.tick++ % 50 == 0) for (final Direction side : Direction.values())
        {
            final BlockEntity t = this.here.getTileEntity(this.level, side);
            this.here.getBlock(this.level, side);
            if (t instanceof ControllerTile)
            {
                final ControllerTile te = (ControllerTile) t;
                if (te.getLift() != null)
                {
                    this.setLift(lift = te.getLift());
                    this.floor = te.floor;
                    this.setChanged();
                    break;
                }
            }
        }
        if (lift == null) return;

        called_floor_checks:
        if (this.floor > 0)
        {
            final BlockState state = this.level.getBlockState(this.getBlockPos());
            boolean current = state.getValue(ControllerBlock.CURRENT);
            boolean called = state.getValue(ControllerBlock.CALLED);

            final int yWhenLiftHere = this.getLift().getFloorPos(this.floor);

            // Set lifts current floor to this if it is in the area of the
            // floor.
            if ((int) Math.round(this.getLift().getY()) == yWhenLiftHere) lift.setCurrentFloor(this.floor);
            else if (this.getLift().getCurrentFloor() == this.floor) lift.setCurrentFloor(-1);

            // Below here is server side only for these checks
            if (this.level.isClientSide) break called_floor_checks;

            final boolean shouldBeCurrent = lift.blockPosition().getY() == yWhenLiftHere;
            final boolean shouldBeCalled = lift.getCalled() && lift.getDestY() == yWhenLiftHere;

            if (current && !shouldBeCurrent)
                this.level.setBlockAndUpdate(this.getBlockPos(), state.setValue(ControllerBlock.CURRENT, false));
            else if (!current && shouldBeCurrent)
                this.level.setBlockAndUpdate(this.getBlockPos(), state.setValue(ControllerBlock.CURRENT, true));

            if (called && !shouldBeCalled)
                this.level.setBlockAndUpdate(this.getBlockPos(), state.setValue(ControllerBlock.CALLED, false));
            else if (!called && shouldBeCalled)
                this.level.setBlockAndUpdate(this.getBlockPos(), state.setValue(ControllerBlock.CALLED, true));

            current = state.getValue(ControllerBlock.CURRENT);
            called = state.getValue(ControllerBlock.CALLED);

            // In this case, we can respond to redstone signals on call faces
            if (!current && !called) for (final Direction facing : Direction.values())
            {
                // Note that we do not check if the side is on, as this allows
                // only buttons, with no display number!
                if (!this.isCallPanel(facing)) continue;
                final int power = this.level.getSignal(this.getBlockPos(), facing.getOpposite());
                if (power > 0) lift.call(this.floor);
            }
            MinecraftForge.EVENT_BUS.post(new ControllerUpdate(this));
        }
    }

    @Override
    public void saveAdditional(final CompoundTag par1)
    {
        super.saveAdditional(par1);
        par1.putInt("floor", this.floor);
        par1.putByteArray("sides", this.sides);
        par1.putByteArray("sidePages", this.sidePages);
        for (final Direction face : Direction.Plane.HORIZONTAL)
            par1.putBoolean(face + "Call", this.callFaces[face.ordinal()]);
        for (final Direction face : Direction.Plane.HORIZONTAL)
            par1.putBoolean(face + "Edit", this.editFace[face.ordinal()]);
        for (final Direction face : Direction.Plane.HORIZONTAL)
            par1.putBoolean(face + "Display", this.floorDisplay[face.ordinal()]);
        if (this.lift != null) this.liftID = this.lift.getUUID();
        if (this.liftID != null) par1.putUUID("lift", liftID);
        if (this.copiedState != null)
        {
            final CompoundTag state = NbtUtils.writeBlockState(this.copiedState);
            par1.put("state", state);
        }
    }

    /**
     * @return the lift
     */
    public EntityLift getLift()
    {
        if (this.liftID == null) return null;
        else if (this.lift == null)
        {
            this.lift = EntityLift.getLiftFromUUID(this.liftID, this.getLevel());

            // Client side process this differently for when the tile entity
            // ticks before the lift actually loads in.
            if (this.getLevel().isClientSide)
            {
                if (this.lift != null) this.setLift(this.lift);
                return this.lift;
            }

            if (this.lift == null) this.setLift(null);
            else
            {
                this.setLift(this.lift);
                // Make sure that lift's floor is this one if it doesn't have
                // one defined.
                if (this.floor > 0 && !this.getLift().hasFloor(this.floor)) this.setFloor(this.floor);
            }
        }
        return this.lift;
    }

}
