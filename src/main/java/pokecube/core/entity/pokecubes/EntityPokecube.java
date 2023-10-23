package pokecube.core.entity.pokecubes;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.INBTSerializable;
import pokecube.api.PokecubeAPI;
import pokecube.api.events.pokemobs.CaptureEvent;
import pokecube.api.utils.Tools;
import pokecube.core.handlers.PokecubePlayerDataHandler;
import pokecube.core.handlers.playerdata.PokecubePlayerCustomData;
import pokecube.core.init.EntityTypes;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.items.pokecubes.helper.CaptureManager;
import pokecube.core.items.pokecubes.helper.SendOutManager;
import pokecube.core.network.packets.PacketPokecube;
import thut.api.Tracker;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;

public class EntityPokecube extends EntityPokecubeBase
{

    public static class CollectEntry
    {
        static CollectEntry createFromNBT(final CompoundTag nbt)
        {
            String player = nbt.getString("id");
            long time = nbt.getLong("time");
            short resetKey = nbt.getShort("resetKey");
            return new CollectEntry(player, time, resetKey);
        }

        final String id;
        long time;
        final short resetKey;

        public CollectEntry(String player, long time, short resetKey)
        {
            this.id = player;
            this.time = time;
            this.resetKey = resetKey;
        }

        void writeToNBT(final CompoundTag nbt)
        {
            nbt.putString("id", this.id);
            nbt.putLong("time", this.time);
            nbt.putShort("resetKey", this.resetKey);
        }
    }

    public static class CollectList implements INBTSerializable<CompoundTag>
    {
        private final Map<String, CollectEntry> map = Maps.newHashMap();

        public void clear()
        {
            this.map.clear();
        }

        public boolean isValid(final Entity in, final long resetTime, short resetKey)
        {
            if (in == null) return false;
            if (!this.map.containsKey(in.getStringUUID())) return false;
            final var s = this.map.get(in.getStringUUID());
            // If reset key does not match, invalidate the entry.
            if (s.resetKey != resetKey)
            {
                this.map.remove(in.getStringUUID());
                return false;
            }
            // If this is the case, then this mob is not re-battleable.
            if (resetTime <= 0) return true;
            // Otherwise check the diff.
            final long diff = Tracker.instance().getTick() - s.time;
            if (diff > resetTime) return false;
            return true;
        }

        public void validate(final Entity in, short key)
        {
            if (in == null) return;
            final var s = this.map.getOrDefault(in.getStringUUID(), new CollectEntry(in.getStringUUID(), 0, key));
            s.time = Tracker.instance().getTick();
            this.map.put(in.getStringUUID(), s);
        }

        private void load(final ListTag list)
        {
            this.clear();
            for (int i = 0; i < list.size(); i++)
            {
                final CollectEntry d = CollectEntry.createFromNBT(list.getCompound(i));
                if (d.id.isEmpty()) continue;
                this.map.put(d.id, d);
            }
        }

        private ListTag save()
        {
            final ListTag list = new ListTag();
            for (final CollectEntry entry : this.map.values())
            {
                final CompoundTag CompoundNBT = new CompoundTag();
                entry.writeToNBT(CompoundNBT);
                list.add(CompoundNBT);
            }
            return list;
        }

        @Override
        public CompoundTag serializeNBT()
        {
            CompoundTag tag = new CompoundTag();
            tag.put("list", this.save());
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag nbt)
        {
            this.load(nbt.getList("list", 10));
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

    static
    {
        PokecubePlayerCustomData.registerDataType("loot_pokecubes", CollectList::new);
    }

    public long reset = 0;
    public long resetTime = 0;

    public short resetKey = 0;

    public ArrayList<LootEntry> loot = Lists.newArrayList();
    public ArrayList<ItemStack> lootStacks = Lists.newArrayList();

    public EntityPokecube(final EntityType<? extends EntityPokecubeBase> type, final Level worldIn)
    {
        super(type, worldIn);
    }

    public void addLoot(final LootEntry entry)
    {
        this.loot.add(entry);
        for (int i = 0; i < entry.rolls; i++) this.lootStacks.add(entry.loot);
    }

    public boolean cannotCollect(final Entity e)
    {
        if (e == null || !(e instanceof Player)) return false;
        final String name = e.getStringUUID();
        CollectList collected = PokecubePlayerDataHandler.getCustomDataValue(name, "loot_pokecubes");
        return collected.isValid(this, resetTime, resetKey);
    }

    @Override
    public EntityPokecubeBase copy()
    {
        final EntityPokecube copy = new EntityPokecube(EntityTypes.getPokecube(), this.getLevel());
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
            if (player.isCrouching() && PokecubeManager.isFilled(this.getItem()) && player.getAbilities().instabuild)
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
                    final String name = player.getStringUUID();
                    CollectList collected = PokecubePlayerDataHandler.getCustomDataValue(name, "loot_pokecubes");
                    collected.validate(this, resetKey);
                    ItemStack loot = ItemStack.EMPTY;
                    boolean did = false;
                    if (!this.lootStacks.isEmpty())
                    {
                        loot = this.lootStacks.get(ThutCore.newRandom().nextInt(this.lootStacks.size()));
                        if (!loot.isEmpty())
                        {
                            Tools.giveItem(player, loot.copy());
                            did = true;
                        }
                    }
                    else if (this.lootTable != null)
                    {
                        final LootTable loottable = this.getLevel().getServer().getLootTables().get(this.lootTable);
                        final LootContext.Builder lootcontext$builder = new LootContext.Builder(
                                (ServerLevel) this.getLevel()).withParameter(LootContextParams.THIS_ENTITY, this);
                        for (final ItemStack itemstack : loottable
                                .getRandomItems(lootcontext$builder.create(loottable.getParamSet())))
                            if (!itemstack.isEmpty()) Tools.giveItem(player, itemstack.copy());
                        did = true;
                    }
                    if (did)
                        PacketPokecube.sendMessage(player, this.getId(), Tracker.instance().getTick() + this.resetTime);
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
        this.loot.clear();
        this.lootStacks.clear();
        if (nbt.contains("loot", 9))
        {
            final ListTag ListNBT = nbt.getList("loot", 10);
            for (int i = 0; i < ListNBT.size(); i++) this.addLoot(LootEntry.createFromNBT(ListNBT.getCompound(i)));
        }
        final String lootTable = nbt.getString("lootTable");
        if (!lootTable.isBlank()) this.lootTable = new ResourceLocation(lootTable);
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
        int time = this.getTime();
        if (this.isReleasing() && time <= 0)
        {
            this.discard();
            return;
        }
        capture:
        if (this.getLevel() instanceof ServerLevel level)
        {
            final boolean validTime = time <= 0;
            boolean succeeded = this.getTilt() >= 4;

            if (succeeded && time < 5)
            {
                double size = 2 * this.getBbWidth();
                double x = this.getX();
                double y = this.getY();
                double z = this.getZ();

                Random r = ThutCore.newRandom();
                for (int l = 0; l < 2; l++)
                {
                    double i = (0.5 - r.nextDouble()) * size;
                    double j = (0.5 - r.nextDouble()) * size + size / 2;
                    double k = (0.5 - r.nextDouble()) * size;
                    level.sendParticles(ParticleTypes.ELECTRIC_SPARK, x + i, y + j, z + k, 1, 0, 0, 0, 0);
                }
            }

            if (!validTime) break capture;
            // Captured the pokemon
            if (succeeded)
            {
                if (CaptureManager.captureSucceed(this))
                {
                    boolean gave = false;
                    final boolean filled = PokecubeManager.isFilled(this.getItem());
                    if (filled)
                    {
                        final CaptureEvent.Post event = new CaptureEvent.Post(this);
                        gave = PokecubeAPI.POKEMOB_BUS.post(event);
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
                return;
            }
        }
        if (time > 0) this.setTime(time - 1);
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
        for (final LootEntry entry : this.loot)
        {
            final CompoundTag CompoundNBT = new CompoundTag();
            entry.writeToNBT(CompoundNBT);
            ListNBT.add(CompoundNBT);
        }
        nbt.put("loot", ListNBT);
        if (this.lootTable != null) nbt.putString("lootTable", this.lootTable.toString());
        else nbt.putString("lootTable", "");
    }

}