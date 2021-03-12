package pokecube.adventures.utils;

import java.util.List;
import java.util.Map;

import org.nfunk.jep.JEP;

import com.google.common.collect.Maps;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import pokecube.adventures.Config;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.blocks.afa.AfaTile;
import pokecube.adventures.blocks.genetics.helper.BaseGeneticsTile;
import pokecube.adventures.blocks.siphon.SiphonTickEvent;
import pokecube.adventures.blocks.siphon.SiphonTile;
import pokecube.adventures.blocks.warppad.WarppadTile;
import pokecube.core.database.PokedexEntry;
import pokecube.core.handlers.events.EventsHandler;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.Stats;
import pokecube.core.interfaces.capabilities.PokemobCaps;
import pokecube.core.utils.PokeType;
import thut.api.maths.Vector3;

public class EnergyHandler
{
    private static final ResourceLocation ENERGYCAP = new ResourceLocation("pokecube:energy");

    public static JEP parser;

    public static class EnergyStore implements IEnergyStorage, ICapabilityProvider
    {
        private final IEnergyStorage               tile;
        private final LazyOptional<IEnergyStorage> holder = LazyOptional.of(() -> this);

        public EnergyStore(final IEnergyStorage tile)
        {
            this.tile = tile;
        }

        @Override
        public int receiveEnergy(final int maxReceive, final boolean simulate)
        {
            return this.tile.receiveEnergy(maxReceive, simulate);
        }

        @Override
        public int extractEnergy(final int maxExtract, final boolean simulate)
        {
            return this.tile.extractEnergy(maxExtract, simulate);
        }

        @Override
        public int getEnergyStored()
        {
            return this.tile.getEnergyStored();
        }

        @Override
        public int getMaxEnergyStored()
        {
            return this.tile.getEnergyStored();
        }

        @Override
        public boolean canExtract()
        {
            return this.tile.canExtract();
        }

        @Override
        public boolean canReceive()
        {
            return this.tile.canReceive();
        }

        @Override
        public <T> LazyOptional<T> getCapability(final Capability<T> cap, final Direction side)
        {
            return CapabilityEnergy.ENERGY.orEmpty(cap, this.holder);
        }

    }

    public static int getEnergyGain(final int level, final int spAtk, final int atk, final PokedexEntry entry)
    {
        int power = Math.max(atk, spAtk);
        if (EnergyHandler.parser == null) EnergyHandler.initParser();
        EnergyHandler.parser.setVarValue("x", level);
        EnergyHandler.parser.setVarValue("a", power);
        double value = EnergyHandler.parser.getValue();
        if (Double.isNaN(value))
        {
            EnergyHandler.initParser();
            EnergyHandler.parser.setVarValue("x", level);
            EnergyHandler.parser.setVarValue("a", power);
            value = EnergyHandler.parser.getValue();
            if (Double.isNaN(value)) value = 0;
        }
        power = (int) value;
        return Math.max(1, power);
    }

    public static int getMaxEnergy(final int level, final int spAtk, final int atk, final PokedexEntry entry)
    {
        return EnergyHandler.getEnergyGain(level, spAtk, atk, entry);
    }

    public static void initParser()
    {
        EnergyHandler.parser = new JEP();
        EnergyHandler.parser.initFunTab(); // clear the contents of the function
                                           // table
        EnergyHandler.parser.addStandardFunctions();
        EnergyHandler.parser.initSymTab(); // clear the contents of the symbol
                                           // table
        EnergyHandler.parser.addStandardConstants();
        EnergyHandler.parser.addComplex(); // among other things adds i to the
                                           // symbol
        // table
        EnergyHandler.parser.addVariable("x", 0);
        EnergyHandler.parser.addVariable("a", 0);
        EnergyHandler.parser.parseExpression(PokecubeAdv.config.powerFunction);
    }

    public static int getOutput(final SiphonTile tile, int power, final boolean simulated)
    {
        if (tile.getLevel() == null || power == 0) return 0;
        final Vector3 v = Vector3.getNewVector().set(tile);
        final AxisAlignedBB box = tile.box != null ? tile.box : (tile.box = v.getAABB().inflate(10, 10, 10));
        List<MobEntity> l = tile.mobs;
        if (tile.updateTime == -1 || tile.updateTime < tile.getLevel().getGameTime())
        {
            l.clear();
            l = tile.mobs = tile.getLevel().getEntitiesOfClass(MobEntity.class, box);
            tile.updateTime = tile.getLevel().getGameTime() + PokecubeAdv.config.siphonUpdateRate;
        }
        int ret = 0;
        power = Math.min(power, PokecubeAdv.config.maxOutput);
        for (final MobEntity living : l)
            if (living != null && living.inChunk && living.isAlive())
            {
                final IEnergyStorage producer = living.getCapability(CapabilityEnergy.ENERGY).orElse(null);
                if (producer != null)
                {
                    final double dSq = Math.max(1, living.distanceToSqr(tile.getBlockPos().getX() + 0.5, tile.getBlockPos().getY()
                            + 0.5, tile.getBlockPos().getZ() + 0.5));
                    final int input = producer.extractEnergy((int) (PokecubeAdv.config.maxOutput / dSq), simulated);
                    ret += input;
                    if (ret >= power)
                    {
                        ret = power;
                        break;
                    }
                }
            }
        ret = Math.min(ret, PokecubeAdv.config.maxOutput);
        if (!simulated) tile.energy.currentOutput = ret;
        return ret;
    }

    @SubscribeEvent
    public static void SiphonEvent(final SiphonTickEvent event)
    {
        if (!(event.getTile().getLevel() instanceof ServerWorld)) return;
        final ServerWorld world = (ServerWorld) event.getTile().getLevel();

        final Map<IEnergyStorage, Integer> tiles = Maps.newHashMap();
        Integer output = (int) EnergyHandler.getOutput(event.getTile(), PokecubeAdv.config.maxOutput, true);
        event.getTile().energy.theoreticalOutput = output;
        event.getTile().energy.currentOutput = output;
        final IEnergyStorage producer = event.getTile().getCapability(CapabilityEnergy.ENERGY).orElse(null);
        final Integer start = output;
        final Vector3 v = Vector3.getNewVector().set(event.getTile());
        for (final Direction side : Direction.values())
        {
            final TileEntity te = v.getTileEntity(world, side);
            IEnergyStorage cap;
            if (te != null && (cap = te.getCapability(CapabilityEnergy.ENERGY, side.getOpposite()).orElse(
                    null)) != null)
            {
                if (!cap.canReceive()) continue;
                final Integer toSend = cap.receiveEnergy(output, true);
                if (toSend > 0) tiles.put(cap, toSend);
            }
        }
        if (PokecubeAdv.config.wirelessSiphons) for (final GlobalPos pos : event.getTile().wirelessLinks)
        {
            final BlockPos bpos = pos.pos();
            final RegistryKey<World> dim = pos.dimension();
            if (dim != world.dimension()) continue;
            final ChunkPos cpos = new ChunkPos(bpos);
            if (!world.hasChunk(cpos.x, cpos.z)) continue;
            final TileEntity te = world.getBlockEntity(bpos);
            if (te == null) continue;
            IEnergyStorage cap;
            sides:
            for (final Direction side : Direction.values())
                if ((cap = te.getCapability(CapabilityEnergy.ENERGY, side.getOpposite()).orElse(null)) != null)
                {
                    if (!cap.canReceive()) continue;
                    final Integer toSend = cap.receiveEnergy(output, true);
                    if (toSend > 0)
                    {
                        tiles.put(cap, toSend);
                        break sides;
                    }
                }
        }
        for (final Map.Entry<IEnergyStorage, Integer> entry : tiles.entrySet())
        {
            final Integer fraction = output / tiles.size();
            Integer request = entry.getValue();
            if (request > fraction) request = fraction;
            if (fraction == 0 || output <= 0) continue;
            final IEnergyStorage h = entry.getKey();
            output -= request;
            h.receiveEnergy(request, false);
        }
        producer.extractEnergy(start - output, false);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    /** Priority low, so that the IPokemob capability is added first. */
    public static void onEntityCapabilityAttach(final AttachCapabilitiesEvent<Entity> event)
    {
        if (!event.getCapabilities().containsKey(EventsHandler.POKEMOBCAP) || event.getCapabilities().containsKey(
                EnergyHandler.ENERGYCAP) || event.getObject().getCommandSenderWorld() == null) return;
        final IPokemob pokemob = event.getCapabilities().get(EventsHandler.POKEMOBCAP).getCapability(
                PokemobCaps.POKEMOB_CAP).orElse(null);
        if (pokemob != null) event.addCapability(EnergyHandler.ENERGYCAP, new ProviderPokemob(pokemob));
    }

    @SubscribeEvent
    public static void onTileCapabilityAttach(final AttachCapabilitiesEvent<TileEntity> event)
    {
        if (event.getCapabilities().containsKey(EnergyHandler.ENERGYCAP)) return;

        if (event.getObject() instanceof SiphonTile)
        {
            ((SiphonTile) event.getObject()).energy = new SiphonTile.EnergyStore();
            event.addCapability(EnergyHandler.ENERGYCAP, ((SiphonTile) event.getObject()).energy);
        }
        if (event.getObject() instanceof BaseGeneticsTile) event.addCapability(EnergyHandler.ENERGYCAP, new EnergyStore(
                (IEnergyStorage) event.getObject()));
        if (event.getObject() instanceof WarppadTile) event.addCapability(EnergyHandler.ENERGYCAP, new EnergyStore(
                (IEnergyStorage) event.getObject()));
        if (event.getObject() instanceof AfaTile) event.addCapability(EnergyHandler.ENERGYCAP, new EnergyStore(
                (IEnergyStorage) event.getObject()));
    }

    public static class ProviderPokemob extends EnergyStorage implements ICapabilityProvider
    {
        private final LazyOptional<IEnergyStorage> holder = LazyOptional.of(() -> this);

        final IPokemob pokemob;

        long lastTickCheck = -1;

        public ProviderPokemob(final IPokemob pokemob)
        {
            super(0);
            this.pokemob = pokemob;
        }

        @Override
        public <T> LazyOptional<T> getCapability(final Capability<T> cap, final Direction side)
        {
            return CapabilityEnergy.ENERGY.orEmpty(cap, this.holder);
        }

        @Override
        public boolean canReceive()
        {
            return this.checkElectricType();
        }

        @Override
        public boolean canExtract()
        {
            return this.checkElectricType();
        }

        /**
         * This checks if we are electric type, and also does an update of the
         * internal power, if this is the first time this is run during a tick.
         *
         * @return
         */
        private boolean checkElectricType()
        {
            // Not electric type, no energy to extract.
            if (!this.pokemob.isType(PokeType.getType("electric"))) return false;

            final MobEntity living = this.pokemob.getEntity();
            // We will update our energy when this is called, as that
            if (living.getCommandSenderWorld().getGameTime() != this.lastTickCheck)
            {
                this.lastTickCheck = living.getCommandSenderWorld().getGameTime();
                final int spAtk = this.pokemob.getStat(Stats.SPATTACK, true);
                final int atk = this.pokemob.getStat(Stats.ATTACK, true);
                final int level = this.pokemob.getLevel();
                this.capacity = EnergyHandler.getMaxEnergy(level, spAtk, atk, this.pokemob.getPokedexEntry());
                this.energy = living.getPersistentData().getInt("pokecube:energy");
                final int dE = this.capacity - this.energy;
                this.maxReceive = this.capacity / 5;
                this.maxExtract = this.capacity;
                final double regen = Math.min(this.capacity / 10d, dE) / this.capacity;
                if (dE > 0)
                {
                    this.energy += dE;
                    this.pokemob.applyHunger((int) (Config.instance.energyHungerCost + regen
                            * Config.instance.energyHungerCost));
                    living.getPersistentData().putInt("pokecube:energy", this.energy);
                }
            }
            return true;
        }
    }

}
