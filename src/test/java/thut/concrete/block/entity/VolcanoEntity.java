package thut.concrete.block.entity;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.INBTSerializable;
import thut.api.Tracker;
import thut.api.block.ITickTile;
import thut.api.block.flowing.FlowingBlock;
import thut.api.block.flowing.IFlowingBlock;
import thut.api.block.flowing.MoltenBlock;
import thut.api.boom.ExplosionCustom;
import thut.api.boom.ExplosionCustom.BlockBreaker;
import thut.api.maths.Vector3;
import thut.concrete.Concrete;
import thut.concrete.block.VolcanoBlock;

public class VolcanoEntity extends BlockEntity implements ITickTile {
	public static abstract class Part implements INBTSerializable<CompoundTag> {
		Biome vol_biome = null;
		long completed = -1;

		@Override
		public void deserializeNBT(CompoundTag nbt) {
			completed = nbt.getLong("c");
		}

		@Override
		public CompoundTag serializeNBT() {
			CompoundTag tag = new CompoundTag();
			tag.putLong("c", completed);
			return tag;
		}

		protected boolean isMagma(BlockEntity owner, BlockPos pos) {
			return owner.getLevel().getBlockState(pos).getBlock() == Concrete.MOLTEN_BLOCK.get()
					|| pos.equals(owner.getBlockPos());
		}

		protected void setMagma(Level level, BlockPos pos, int viscosity) {
			BlockState state = Concrete.MOLTEN_BLOCK.get().defaultBlockState().setValue(MoltenBlock.HEATED, true)
					.setValue(FlowingBlock.VISCOSITY, viscosity);
			level.setBlock(pos, state, 3);
		}

		protected boolean shouldTickSelf() {
			return Tracker.instance().getTick() > completed;
		}

		protected abstract void tick(BlockEntity owner, int viscosity);
	}

	public static class Tube extends Part {
		final Chamber source;
		int size = 2;
		Chamber dest;

		public Tube(Chamber source) {
			this.source = source;
		}

		protected void tick(BlockEntity owner, int viscosity) {
			if (shouldTickSelf()) {
				Vec3 start = Vec3.atCenterOf(source.location);
				Vec3 end = Vec3.atCenterOf(dest.location);

				Vec3 dir = end.subtract(start);
				double length = dir.length();
				dir = dir.normalize();

				int magma = 0;
				int total = 0;

				Set<BlockPos> checked = Sets.newHashSet();

				for (int i = 0; i < length; i++) {
					BlockPos p = new BlockPos(start.add(dir.scale(i)));
					if (checked.contains(p))
						continue;

					BlockPos c1 = p.offset(-size, -size, -size);
					BlockPos c2 = p.offset(size, size, size);

					Iterable<BlockPos> iter = BlockPos.betweenClosed(c1, c2);
					for (BlockPos p2 : iter) {
						if (checked.contains(p2))
							continue;
						checked.add(p2 = p2.immutable());
						total++;
						if (!isMagma(owner, p2)) {
							setMagma(owner.getLevel(), p2, viscosity);
						} else
							magma++;
					}
				}
				if (total - magma < total / 20)
					completed = Tracker.instance().getTick() + 60;
			} else if (source.stable) {
//                System.out.println("ticking dest node");
				dest.tick(owner, viscosity);
			}
		}

		@Override
		public CompoundTag serializeNBT() {
			CompoundTag tag = super.serializeNBT();
			tag.put("dest", dest.serializeNBT());
			tag.putInt("size", size);
			return tag;
		}

		@Override
		public void deserializeNBT(CompoundTag nbt) {
			super.deserializeNBT(nbt);
			dest = new Chamber();
			dest.deserializeNBT(nbt.getCompound("dest"));
			size = nbt.getInt("size");
		}
	}

	public static class Chamber extends Part {
		public static class ChamberBoom implements BlockBreaker {
			int viscosity = 0;

			public ChamberBoom(int viscosity) {
				this.viscosity = viscosity;
			}

			@Override
			public BlockState applyBreak(ExplosionCustom boom, BlockPos pos, BlockState state, float power,
					boolean destroy, ServerLevel level) {
				BlockState to = BlockBreaker.super.applyBreak(boom, pos, state, power, destroy, level);
				fallout: if (to.isAir() && destroy) {
					BlockState set = Blocks.AIR.defaultBlockState();
					if (power > 5) {
						set = Concrete.MOLTEN_LAYER.get().defaultBlockState().setValue(IFlowingBlock.FALLING, true)
								.setValue(IFlowingBlock.LAYERS, 8).setValue(MoltenBlock.VISCOSITY, viscosity);
					} else {
						boolean noDust;
						Vec3 diff = boom.getPosition().subtract(pos.getX(), pos.getY(), pos.getZ()).normalize();
						double dot = diff.dot(new Vec3(0, 1, 0));
						noDust = dot > 0 || dot < -0.95;
						if (noDust)
							break fallout;
						set = Concrete.DUST_LAYER.get().defaultBlockState().setValue(IFlowingBlock.FALLING, true)
								.setValue(IFlowingBlock.LAYERS, 2);
					}

					if (!set.isAir()) {
						int n = 20;
						Vector3 hit = Vector3.getNextSurfacePoint(level, new Vector3().set(pos), Vector3.secondAxis,
								20);
						while (hit != null && n-- > 0) {
							hit = Vector3.getNextSurfacePoint(level, new Vector3().set(pos), Vector3.secondAxis, 20);
						}
						pos = pos.above(n);
						level.setBlock(pos, set, 3);
						level.scheduleTick(pos, set.getBlock(), 2);
					}
				}
				Vector3 v = new Vector3().set(pos);
				Biome b = level.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).get(Concrete.VOLCANO_BIOME);
				v.setBiome(b, level);
				return to;
			}
		}

		public float size = 3;
		public BlockPos location;
		public boolean stable = false;

		List<Tube> tubes = Lists.newArrayList();

		public Chamber(BlockPos pos, float f) {
			this.size = f;
			this.location = pos;
		}

		public Chamber() {
		}

		protected void tick(BlockEntity owner, int viscosity) {
			if (shouldTickSelf()) {
				int r = (int) size;
				int molten = 0;
				int total = 0;

				MutableBlockPos b = location.mutable();

				for (int x = -r; x <= r; x++)
					for (int y = -r; y <= r; y++)
						for (int z = -r; z <= r; z++) {
							b.set(location);
							BlockPos pos = b.move(Direction.UP, x).move(Direction.EAST, y).move(Direction.NORTH, z);
							total++;
							if (isMagma(owner, pos)) {
								molten++;
							} else
								setMagma(owner.getLevel(), pos, viscosity);
						}
				if (total - molten < total / 20)
					completed = Tracker.instance().getTick() + 60;
				stable = total == molten;
			} else {
				if (owner.getLevel().getRandom().nextDouble() < 0.0025 && tubes.isEmpty() && !stable) {
					ExplosionCustom boom = new ExplosionCustom((ServerLevel) owner.getLevel(), null,
							new Vector3().set(location.above((int) size + 5)),
							25 + owner.getLevel().getRandom().nextInt(25));
					boom.breaker = new ChamberBoom(viscosity);
					boom.doExplosion();
				}
				for (Tube t : tubes)
					t.tick(owner, viscosity);
			}
		}

		@Override
		public CompoundTag serializeNBT() {
			CompoundTag tag = super.serializeNBT();
			tag.put("location", NbtUtils.writeBlockPos(location));
			tag.putFloat("size", size);
			ListTag listTag = new ListTag();
			for (Tube t : tubes)
				listTag.add(t.serializeNBT());
			tag.put("tubes", listTag);
			return tag;
		}

		@Override
		public void deserializeNBT(CompoundTag nbt) {
			super.deserializeNBT(nbt);
			this.size = nbt.getFloat("size");
			this.location = NbtUtils.readBlockPos(nbt.getCompound("location"));
			ListTag listTag = nbt.getList("tubes", nbt.getId());
			this.tubes.clear();
			for (int i = 0; i < listTag.size(); i++) {
				Tube t = new Tube(this);
				t.deserializeNBT(listTag.getCompound(i));
				this.tubes.add(t);
			}
		}

	}

	private final Chamber mainChamber;

	public VolcanoEntity(BlockPos pos, BlockState state) {
		super(Concrete.VOLCANO_TYPE.get(), pos, state);
		mainChamber = new Chamber(pos, 3f);
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		mainChamber.deserializeNBT(tag.getCompound("chamber"));
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		tag.put("chamber", mainChamber.serializeNBT());
	}

	@Override
	public void tick() {
		if (this.level.isClientSide || !(this.level instanceof ServerLevel level))
			return;
		if (mainChamber.tubes.isEmpty()) {
			WorldgenRandom rand = new WorldgenRandom(new LegacyRandomSource(0L));
			rand.setLargeFeatureSeed(level.getSeed(), this.getBlockPos().getX(), this.getBlockPos().getZ());
			Chamber root = mainChamber;
			int n = 2 + rand.nextInt(8);
			for (int i = 0; i < n; i++) {
				BlockPos p = root.location.above((int) (root.size + 3 + rand.nextInt(16))).north(8 - rand.nextInt(17))
						.east(8 - rand.nextInt(17));
				int size = 1 + rand.nextInt(3);
				Chamber next = new Chamber(p, size);
				Tube tube = new Tube(root);
				tube.dest = next;
				tube.size = 1 + rand.next(2);
				root.tubes.add(tube);
				if (rand.nextBoolean())
					root = next;
			}
		}
		if (level.getGameTime() % 10 != 0)
			return;
		level.noSave = false;
		int v = this.getBlockState().getValue(VolcanoBlock.VISCOSITY);
		mainChamber.tick(this, v);
	}

}
