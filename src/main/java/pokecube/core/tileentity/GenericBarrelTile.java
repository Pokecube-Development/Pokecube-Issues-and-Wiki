package pokecube.core.tileentity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.DoubleSidedInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.tileentity.LockableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import pokecube.core.blocks.GenericBarrel;
import pokecube.legends.Reference;
import pokecube.legends.init.TileEntityInit;

public class GenericBarrelTile extends LockableLootTileEntity {
	private NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
	private ITextComponent name;
	private int openCount;

	private GenericBarrelTile(final TileEntityType<?> tileEntityType) {
		super(tileEntityType);
	}

	public GenericBarrelTile() {
		this(TileEntityInit.GENERIC_BARREL_TILE.get());
	}

	@Override
    public int getContainerSize() {
		return 27;
	}

	@Override
    protected NonNullList<ItemStack> getItems() {
		return this.items;
	}

	@Override
    protected void setItems(final NonNullList<ItemStack> items) {
		this.items = items;
	}

	@Override
    protected Container createMenu(final int index, final PlayerInventory playerInventory) {
		return CustomBarrelContainer.threeRows(index, playerInventory, this);
	}

	@Override
    protected ITextComponent getDefaultName() {
		return new TranslationTextComponent("container." + Reference.ID + ".generic_barrel");
	}

	@Override
    public CompoundNBT save(final CompoundNBT saveCompoundNBT) {
		super.save(saveCompoundNBT);
		if (!this.trySaveLootTable(saveCompoundNBT)) ItemStackHelper.saveAllItems(saveCompoundNBT, this.items);
		if (this.name != null) saveCompoundNBT.putString("CustomName", ITextComponent.Serializer.toJson(this.name));
		return saveCompoundNBT;
	}

	@Override
    public void load(final BlockState state, final CompoundNBT loadCompoundNBT) {
		super.load(state, loadCompoundNBT);
		this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
		if (!this.tryLoadLootTable(loadCompoundNBT)) ItemStackHelper.loadAllItems(loadCompoundNBT, this.items);
		if (loadCompoundNBT.contains("CustomName", 8)) this.name = ITextComponent.Serializer.fromJson(loadCompoundNBT.getString("CustomName"));
	}

	@Override
    public void startOpen(final PlayerEntity player) {
		if (!player.isSpectator()) {
			if (this.openCount < 0) this.openCount = 0;

			++this.openCount;
			final BlockState blockstate = this.getBlockState();
			final boolean flag = blockstate.getValue(GenericBarrel.OPEN);
			if (!flag) {
				this.playSound(blockstate, SoundEvents.BARREL_OPEN);
				this.updateBlockState(blockstate, true);
			}

			this.scheduleRecheck();
		}
	}

	private void scheduleRecheck() {
		this.level.getBlockTicks().scheduleTick(this.getBlockPos(), this.getBlockState().getBlock(), 5);
	}

	public void recheckOpen() {
		final int i = this.worldPosition.getX();
		final int j = this.worldPosition.getY();
		final int k = this.worldPosition.getZ();
		this.openCount = GenericBarrelTile.getOpenCount(this.level, this, i, j, k);
		if (this.openCount > 0) this.scheduleRecheck();
        else {
			final BlockState blockstate = this.getBlockState();
			if (!(blockstate.getBlock() instanceof GenericBarrel)) {
				this.setRemoved();
				return;
			}

			final boolean flag = blockstate.getValue(GenericBarrel.OPEN);
			if (flag) {
				this.playSound(blockstate, SoundEvents.BARREL_CLOSE);
				this.updateBlockState(blockstate, false);
			}
		}
	}

	@Override
    public void stopOpen(final PlayerEntity entity) {
		if (!entity.isSpectator()) --this.openCount;
	}

	private void updateBlockState(final BlockState state, final boolean update) {
		this.level.setBlock(this.getBlockPos(), state.setValue(GenericBarrel.OPEN, Boolean.valueOf(update)), 3);
	}

	private void playSound(final BlockState state, final SoundEvent sound) {
		final Vector3i vector3i = state.getValue(GenericBarrel.FACING).getNormal();
		final double d0 = this.worldPosition.getX() + 0.5D + vector3i.getX() / 2.0D;
		final double d1 = this.worldPosition.getY() + 0.5D + vector3i.getY() / 2.0D;
		final double d2 = this.worldPosition.getZ() + 0.5D + vector3i.getZ() / 2.0D;
		this.level.playSound((PlayerEntity) null, d0, d1, d2, sound, SoundCategory.BLOCKS, 0.5F,
				this.level.random.nextFloat() * 0.1F + 0.9F);
	}

	public static int getOpenCount(final World world, final LockableTileEntity lockableTileEntity, final int i, final int j, final int k, final int l, int r) {
      if (!world.isClientSide && r != 0 && (i + j + k + l) % 200 == 0) r = GenericBarrelTile.getOpenCount(world, lockableTileEntity, j, k, l);

      return r;
   }

   public static int getOpenCount(final World world, final LockableTileEntity lockableTileEntity, final int j, final int k, final int l) {
      int i = 0;
      for(final PlayerEntity player : world.getEntitiesOfClass(PlayerEntity.class, new AxisAlignedBB(j - 5.0F, k - 5.0F, l - 5.0F, j + 1 + 5.0F, k + 1 + 5.0F, l + 1 + 5.0F)))
        if (player.containerMenu instanceof CustomBarrelContainer) {
            final IInventory iinventory = ((CustomBarrelContainer)player.containerMenu).getContainer();
            if (iinventory == lockableTileEntity || iinventory instanceof DoubleSidedInventory && ((DoubleSidedInventory)iinventory).contains(lockableTileEntity)) ++i;
         }
      return i;
   }
}