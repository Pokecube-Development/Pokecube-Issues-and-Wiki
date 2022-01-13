package pokecube.core.items.pokecubes;

import java.util.ArrayList;

import com.google.common.collect.Lists;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import pokecube.core.PokecubeCore;
import pokecube.core.events.pokemob.CaptureEvent;
import pokecube.core.items.pokecubes.helper.CaptureManager;
import pokecube.core.items.pokecubes.helper.SendOutManager;
import pokecube.core.network.packets.PacketPokecube;
import pokecube.core.utils.Tools;
import thut.api.Tracker;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;

public class EntityPokecube extends EntityPokecubeBase
{

    public static class CollectEntry
    {
        static CollectEntry createFromNBT(final CompoundTag nbt)
        {
            final String player = nbt.getString("player");
            final long time = nbt.getLong("time");
            return new CollectEntry(player, time);
        }

        final String player;
        final long   time;

        public CollectEntry(final String player, final long time)
        {
            this.player = player;
            this.time = time;
        }

        void writeToNBT(final CompoundTag nbt)
        {
            nbt.putString("player", this.player);
            nbt.putLong("time", this.time);
        }
    }

    public static class LootEntry
    {
        static LootEntry createFromNBT(final CompoundTag nbt)
        {
            final ItemStack loot = ItemStack.of(nbt.getCompound("loot"));
            return new LootEntry(loot, nbt.getInt("rolls"));
        }

        final ItemStack loot;

        final int rolls;

        public LootEntry(final ItemStack loot, final int rolls)
        {
            this.loot = loot;
            this.rolls = rolls;
        }

        void writeToNBT(final CompoundTag nbt)
        {
            final CompoundTag loot = new CompoundTag();
            this.loot.save(loot);
            nbt.put("loot", loot);
            nbt.putInt("rolls", this.rolls);
        }

    }

    public static final EntityType<EntityPokecube> TYPE;

    static
    {
        TYPE = EntityType.Builder.of(EntityPokecube::new, MobCategory.MISC).setShouldReceiveVelocityUpdates(
                true).setTrackingRange(32).setUpdateInterval(1).noSummon().fireImmune().sized(0.25f, 0.25f).build(
                        "pokecube");
    }

    public long reset     = 0;
    public long resetTime = 0;

    public ArrayList<CollectEntry> players    = Lists.newArrayList();
    public ArrayList<LootEntry>    loot       = Lists.newArrayList();
    public ArrayList<ItemStack>    lootStacks = Lists.newArrayList();

    public EntityPokecube(final EntityType<? extends EntityPokecubeBase> type, final Level worldIn)
    {
        super(type, worldIn);
    }

    public void addLoot(final LootEntry entry)
    {
        this.loot.add(entry);
        for (int i = 0; i < entry.rolls; i++)
            this.lootStacks.add(entry.loot);
    }

    public boolean cannotCollect(final Entity e)
    {
        if (e == null) return false;
        final String name = e.getStringUUID();
        for (final CollectEntry s : this.players)
            if (s.player.equals(name))
            {
                if (this.resetTime > 0)
                {
                    final long diff = Tracker.instance().getTick() - s.time;
                    if (diff > this.resetTime)
                    {
                        this.players.remove(s);
                        return false;
                    }
                }
                return true;
            }
        return false;
    }

    public EntityPokecube copy()
    {
        final EntityPokecube copy = new EntityPokecube(EntityPokecube.TYPE, this.getLevel());
        copy.copyPosition(this);
        copy.restoreFrom(this);
        return copy;
    }

    @Override
    public InteractionResult interact(final Player player, final InteractionHand hand)
    {
        final ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer && this.canBePickedUp)
        {
            if (player.isCrouching() && PokecubeManager.isFilled(this.getItem()) && player
                    .getAbilities().instabuild)
                if (!stack.isEmpty())
            {
                this.isLoot = true;
                this.addLoot(new LootEntry(stack, 1));
                return InteractionResult.SUCCESS;
            }
            if (!this.isReleasing()) if (PokecubeManager.isFilled(this.getItem()))
            {
                if (player.isCrouching())
                {
                    Tools.giveItem(player, this.getItem());
                    this.discard();
                }
                else SendOutManager.sendOut(this, true);
            }
            else
            {
                if (this.isLoot)
                {
                    if (this.cannotCollect(player)) return InteractionResult.FAIL;
                    this.players.add(new CollectEntry(player.getStringUUID(), Tracker.instance().getTick()));
                    ItemStack loot = ItemStack.EMPTY;
                    if (!this.lootStacks.isEmpty())
                    {
                        loot = this.lootStacks.get(ThutCore.newRandom().nextInt(this.lootStacks.size()));
                        if (!loot.isEmpty())
                        {
                            PacketPokecube.sendMessage(player, this.getId(), Tracker.instance().getTick()
                                    + this.resetTime);
                            Tools.giveItem(player, loot.copy());
                        }
                    }
                    else if (this.lootTable != null)
                    {
                        final LootTable loottable = this.getLevel().getServer().getLootTables().get(
                                this.lootTable);
                        final LootContext.Builder lootcontext$builder = new LootContext.Builder((ServerLevel) this
                                .getLevel()).withParameter(LootContextParams.THIS_ENTITY, this);
                        for (final ItemStack itemstack : loottable.getRandomItems(lootcontext$builder.create(loottable
                                .getParamSet())))
                            if (!itemstack.isEmpty()) Tools.giveItem(player, itemstack.copy());
                        PacketPokecube.sendMessage(player, this.getId(), Tracker.instance().getTick() + this.resetTime);
                    }
                    return InteractionResult.SUCCESS;
                }
                Tools.giveItem(player, this.getItem());
                this.discard();
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void readAdditionalSaveData(final CompoundTag nbt)
    {
        super.readAdditionalSaveData(nbt);
        this.isLoot = nbt.getBoolean("isLoot");
        this.setReleasing(nbt.getBoolean("releasing"));
        if (nbt.contains("resetTime")) this.resetTime = nbt.getLong("resetTime");
        this.players.clear();
        this.loot.clear();
        this.lootStacks.clear();
        if (nbt.contains("players", 9))
        {
            final ListTag ListNBT = nbt.getList("players", 10);
            for (int i = 0; i < ListNBT.size(); i++)
                this.players.add(CollectEntry.createFromNBT(ListNBT.getCompound(i)));
        }
        if (nbt.contains("loot", 9))
        {
            final ListTag ListNBT = nbt.getList("loot", 10);
            for (int i = 0; i < ListNBT.size(); i++)
                this.addLoot(LootEntry.createFromNBT(ListNBT.getCompound(i)));
        }
        final String lootTable = nbt.getString("lootTable");
        if (!lootTable.isEmpty()) this.lootTable = new ResourceLocation(lootTable);
    }

    public void shoot(final Vector3 direction, final float velocity)
    {
        this.shoot(direction.x, direction.y, direction.z, velocity, 0);
    }

    // @Override
    public void shoot(final double x, final double y, final double z, final float velocity, final float inaccuracy)
    {
        final Vec3 vec3d = new Vec3(x, y, z).normalize().add(this.random.nextGaussian() * 0.0075F * inaccuracy,
                this.random.nextGaussian() * 0.0075F * inaccuracy, this.random.nextGaussian() * 0.0075F * inaccuracy)
                .scale(velocity);
        this.setDeltaMovement(vec3d);
        final float f = (float) vec3d.horizontalDistance();
        this.yRot = (float) (Mth.atan2(vec3d.x, vec3d.z) * (180F / (float) Math.PI));
        this.xRot = (float) (Mth.atan2(vec3d.y, f) * (180F / (float) Math.PI));
        this.yRotO = this.yRot;
        this.xRotO = this.xRot;
    }

    @Override
    public void tick()
    {
        if (this.isReleasing() && this.getTime() < 0)
        {
            this.discard();
            return;
        }
        capture:
        if (this.getLevel() instanceof ServerLevel)
        {
            final boolean validTime = this.getTime() <= 0;

            if (!validTime) break capture;
            // Captured the pokemon
            if (this.getTilt() >= 4)
            {
                if (CaptureManager.captureSucceed(this))
                {
                    boolean gave = false;
                    final boolean filled = PokecubeManager.isFilled(this.getItem());
                    if (filled)
                    {
                        final CaptureEvent.Post event = new CaptureEvent.Post(this);
                        gave = PokecubeCore.POKEMOB_BUS.post(event);
                    }
                    else if (this.shootingEntity instanceof ServerPlayer
                            && !(this.shootingEntity instanceof FakePlayer))
                    {
                        Tools.giveItem((Player) this.shootingEntity, this.getItem());
                        gave = true;
                    }
                    if (!gave) this.spawnAtLocation(this.getItem(), 0.5f);
                }
                this.discard();
                return;
            }
            else if (this.getTilt() >= 0)
            {// Missed the pokemon
                CaptureManager.captureFailed(this);
                this.discard();
                return;
            }
        }
        this.setTime(this.getTime() - 1);
        super.tick();
    }

    @Override
    public void addAdditionalSaveData(final CompoundTag nbt)
    {
        super.addAdditionalSaveData(nbt);
        nbt.putLong("resetTime", this.resetTime);
        nbt.putBoolean("isLoot", this.isLoot);
        if (this.isReleasing()) nbt.putBoolean("releasing", true);
        ListTag ListNBT = new ListTag();
        for (final CollectEntry entry : this.players)
        {
            final CompoundTag CompoundNBT = new CompoundTag();
            entry.writeToNBT(CompoundNBT);
            ListNBT.add(CompoundNBT);
        }
        if (!this.players.isEmpty()) nbt.put("players", ListNBT);
        ListNBT = new ListTag();
        for (final LootEntry entry : this.loot)
        {
            final CompoundTag CompoundNBT = new CompoundTag();
            entry.writeToNBT(CompoundNBT);
            ListNBT.add(CompoundNBT);
        }
        if (!this.loot.isEmpty()) nbt.put("loot", ListNBT);
        if (this.lootTable != null) nbt.putString("lootTable", this.lootTable.toString());
        else nbt.putString("lootTable", "");
    }

}