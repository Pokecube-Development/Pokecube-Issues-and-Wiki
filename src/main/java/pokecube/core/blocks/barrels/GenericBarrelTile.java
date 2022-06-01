package pokecube.core.blocks.barrels;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.inventory.barrels.CustomBarrelContainer;

public class GenericBarrelTile extends RandomizableContainerBlockEntity
{
    private NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
    private Component name;
    private int openCount;

    private GenericBarrelTile(final BlockEntityType<?> tileEntityType, final BlockPos pos, final BlockState state)
    {
        super(tileEntityType, pos, state);
    }

    public GenericBarrelTile(final BlockPos pos, final BlockState state)
    {
        this(PokecubeItems.BARREL_TYPE.get(), pos, state);
    }

    @Override
    public int getContainerSize()
    {
        return 27;
    }

    @Override
    protected NonNullList<ItemStack> getItems()
    {
        return this.items;
    }

    @Override
    protected void setItems(final NonNullList<ItemStack> items)
    {
        this.items = items;
    }

    @Override
    protected AbstractContainerMenu createMenu(final int index, final Inventory playerInventory)
    {
        return CustomBarrelContainer.threeRows(index, playerInventory, this);
    }

    @Override
    protected Component getDefaultName()
    {
        return new TranslatableComponent("container." + PokecubeCore.MODID + ".generic_barrel");
    }

    @Override
    public void saveAdditional(final CompoundTag saveCompoundNBT)
    {
        super.saveAdditional(saveCompoundNBT);
        if (!this.trySaveLootTable(saveCompoundNBT)) ContainerHelper.saveAllItems(saveCompoundNBT, this.items);
        if (this.name != null) saveCompoundNBT.putString("CustomName", Component.Serializer.toJson(this.name));
    }

    @Override
    public void load(final CompoundTag loadCompoundNBT)
    {
        super.load(loadCompoundNBT);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(loadCompoundNBT)) ContainerHelper.loadAllItems(loadCompoundNBT, this.items);
        if (loadCompoundNBT.contains("CustomName", 8))
            this.name = Component.Serializer.fromJson(loadCompoundNBT.getString("CustomName"));
    }

    @Override
    public void startOpen(final Player player)
    {
        if (!player.isSpectator())
        {
            if (this.openCount < 0) this.openCount = 0;

            ++this.openCount;
            final BlockState blockstate = this.getBlockState();
            final boolean flag = blockstate.getValue(GenericBarrel.OPEN);
            if (!flag)
            {
                this.playSound(blockstate, SoundEvents.BARREL_OPEN);
                this.updateBlockState(blockstate, true);
            }

            this.scheduleRecheck();
        }
    }

    private void scheduleRecheck()
    {
        this.level.scheduleTick(this.getBlockPos(), this.getBlockState().getBlock(), 5);
    }

    public void recheckOpen()
    {
        final int i = this.worldPosition.getX();
        final int j = this.worldPosition.getY();
        final int k = this.worldPosition.getZ();
        this.openCount = GenericBarrelTile.getOpenCount(this.level, this, i, j, k);
        if (this.openCount > 0) this.scheduleRecheck();
        else
        {
            final BlockState blockstate = this.getBlockState();
            if (!(blockstate.getBlock() instanceof GenericBarrel))
            {
                this.setRemoved();
                return;
            }

            final boolean flag = blockstate.getValue(GenericBarrel.OPEN);
            if (flag)
            {
                this.playSound(blockstate, SoundEvents.BARREL_CLOSE);
                this.updateBlockState(blockstate, false);
            }
        }
    }

    @Override
    public void stopOpen(final Player entity)
    {
        if (!entity.isSpectator()) --this.openCount;
    }

    private void updateBlockState(final BlockState state, final boolean update)
    {
        this.level.setBlock(this.getBlockPos(), state.setValue(GenericBarrel.OPEN, Boolean.valueOf(update)), 3);
    }

    private void playSound(final BlockState state, final SoundEvent sound)
    {
        final Vec3i vector3i = state.getValue(GenericBarrel.FACING).getNormal();
        final double d0 = this.worldPosition.getX() + 0.5D + vector3i.getX() / 2.0D;
        final double d1 = this.worldPosition.getY() + 0.5D + vector3i.getY() / 2.0D;
        final double d2 = this.worldPosition.getZ() + 0.5D + vector3i.getZ() / 2.0D;
        this.level.playSound((Player) null, d0, d1, d2, sound, SoundSource.BLOCKS, 0.5F,
                this.level.random.nextFloat() * 0.1F + 0.9F);
    }

    public static int getOpenCount(final Level world, final BaseContainerBlockEntity lockableTileEntity, final int i,
            final int j, final int k, final int l, int r)
    {
        if (!world.isClientSide && r != 0 && (i + j + k + l) % 200 == 0)
            r = GenericBarrelTile.getOpenCount(world, lockableTileEntity, j, k, l);

        return r;
    }

    public static int getOpenCount(final Level world, final BaseContainerBlockEntity lockableTileEntity, final int j,
            final int k, final int l)
    {
        int i = 0;
        for (final Player player : world.getEntitiesOfClass(Player.class,
                new AABB(j - 5.0F, k - 5.0F, l - 5.0F, j + 1 + 5.0F, k + 1 + 5.0F, l + 1 + 5.0F)))
            if (player.containerMenu instanceof CustomBarrelContainer)
        {
            final Container iinventory = ((CustomBarrelContainer) player.containerMenu).getContainer();
            if (iinventory == lockableTileEntity || iinventory instanceof CompoundContainer
                    && ((CompoundContainer) iinventory).contains(lockableTileEntity))
                ++i;
        }
        return i;
    }
}