package pokecube.legends.tileentity;

import net.minecraft.block.BlockState;
import pokecube.legends.init.BlockInit;
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
import pokecube.legends.Reference;
import pokecube.core.blocks.GenericBarrel;
import pokecube.legends.init.TileEntityInit;

public class GenericBarrelTile extends LockableLootTileEntity {
	private NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
	private int openCount;
	
	private GenericBarrelTile(TileEntityType<?> tileEntityType) {
		super(tileEntityType);
	}
	
	public GenericBarrelTile() {
		this(TileEntityInit.GENERIC_BARREL_TILE.get());
	}
	
	public int getContainerSize() {
		return 27;
	}

	protected NonNullList<ItemStack> getItems() {
		return this.items;
	}

	protected void setItems(NonNullList<ItemStack> items) {
		this.items = items;
	}

	protected Container createMenu(int index, PlayerInventory playerInventory) {
		return CustomBarrelContainer.threeRows(index, playerInventory, this);
	}
	
	protected ITextComponent getDefaultName() {
		return new TranslationTextComponent("container." + Reference.ID + ".inverted_barrel");
	}
	
	public CompoundNBT save(CompoundNBT saveCompoundNBT) {
		super.save(saveCompoundNBT);
		if (!this.trySaveLootTable(saveCompoundNBT)) {
			ItemStackHelper.saveAllItems(saveCompoundNBT, this.items);
		}

		return saveCompoundNBT;
	}

	public void load(BlockState state, CompoundNBT loadCompoundNBT) {
		super.load(state, loadCompoundNBT);
		this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
		if (!this.tryLoadLootTable(loadCompoundNBT)) {
			ItemStackHelper.loadAllItems(loadCompoundNBT, this.items);
		}

	}
	
	public void startOpen(PlayerEntity player) {
		if (!player.isSpectator()) {
			if (this.openCount < 0) {
				this.openCount = 0;
			}

			++this.openCount;
			BlockState blockstate = this.getBlockState();
			boolean flag = blockstate.getValue(GenericBarrel.OPEN);
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
		int i = this.worldPosition.getX();
		int j = this.worldPosition.getY();
		int k = this.worldPosition.getZ();
		this.openCount = getOpenCount(this.level, this, i, j, k);
		if (this.openCount > 0) {
			this.scheduleRecheck();
		} else {
			BlockState blockstate = this.getBlockState();
			if (!blockstate.is(BlockInit.INVERTED_BARREL.get()) || !blockstate.is(BlockInit.CONCRETE_BARREL.get()) ||
				!blockstate.is(BlockInit.CORRUPTED_BARREL.get()) || !blockstate.is(BlockInit.DISTORTIC_STONE_BARREL.get()) ||
				!blockstate.is(BlockInit.DISTORTIC_BARREL.get()) || !blockstate.is(BlockInit.MIRAGE_BARREL.get()) ||
				!blockstate.is(BlockInit.TEMPORAL_BARREL.get()) || !blockstate.is(BlockInit.AGED_BARREL.get())) {
				this.setRemoved();
				return;
			}

			boolean flag = blockstate.getValue(GenericBarrel.OPEN);
			if (flag) {
				this.playSound(blockstate, SoundEvents.BARREL_CLOSE);
				this.updateBlockState(blockstate, false);
			}
		}

	}

	public void stopOpen(PlayerEntity entity) {
		if (!entity.isSpectator()) {
			--this.openCount;
		}
	}

	private void updateBlockState(BlockState state, boolean update) {
		this.level.setBlock(this.getBlockPos(), state.setValue(GenericBarrel.OPEN, Boolean.valueOf(update)), 3);
	}
	
	private void playSound(BlockState state, SoundEvent sound) {
		Vector3i vector3i = state.getValue(GenericBarrel.FACING).getNormal();
		double d0 = (double) this.worldPosition.getX() + 0.5D + (double) vector3i.getX() / 2.0D;
		double d1 = (double) this.worldPosition.getY() + 0.5D + (double) vector3i.getY() / 2.0D;
		double d2 = (double) this.worldPosition.getZ() + 0.5D + (double) vector3i.getZ() / 2.0D;
		this.level.playSound((PlayerEntity) null, d0, d1, d2, sound, SoundCategory.BLOCKS, 0.5F,
				this.level.random.nextFloat() * 0.1F + 0.9F);
	}
	
	public static int getOpenCount(World world, LockableTileEntity lockableTileEntity, int i, int j, int k, int l, int r) {
      if (!world.isClientSide && r != 0 && (i + j + k + l) % 200 == 0) {
         r = getOpenCount(world, lockableTileEntity, j, k, l);
      }

      return r;
   }

   public static int getOpenCount(World world, LockableTileEntity lockableTileEntity, int p_213976_2_, int p_213976_3_, int p_213976_4_) {
      int i = 0;
      for(PlayerEntity playerentity : world.getEntitiesOfClass(PlayerEntity.class, new AxisAlignedBB((double)((float)p_213976_2_ - 5.0F), (double)((float)p_213976_3_ - 5.0F), (double)((float)p_213976_4_ - 5.0F), (double)((float)(p_213976_2_ + 1) + 5.0F), (double)((float)(p_213976_3_ + 1) + 5.0F), (double)((float)(p_213976_4_ + 1) + 5.0F)))) {
         if (playerentity.containerMenu instanceof CustomBarrelContainer) {
            IInventory iinventory = ((CustomBarrelContainer)playerentity.containerMenu).getContainer();
            if (iinventory == lockableTileEntity || iinventory instanceof DoubleSidedInventory && ((DoubleSidedInventory)iinventory).contains(lockableTileEntity)) {
               ++i;
            }
         }
      }
      return i;
   }
}