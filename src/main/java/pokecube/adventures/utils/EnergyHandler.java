package pokecube.adventures.utils;

import java.util.List;
import java.util.Map;

import org.nfunk.jep.JEP;

import com.google.common.collect.Maps;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import pokecube.adventures.Config;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.blocks.genetics.helper.BaseGeneticsTile;
import pokecube.adventures.blocks.siphon.SiphonTickEvent;
import pokecube.adventures.blocks.siphon.SiphonTile;
import pokecube.core.database.PokedexEntry;
import pokecube.core.handlers.events.EventsHandler;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.Stats;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.utils.PokeType;
import thut.api.maths.Vector3;

@Mod.EventBusSubscriber
public class EnergyHandler
{
    private static final ResourceLocation ENERGYCAP = new ResourceLocation("pokecube:energy");

    public static int UPDATERATE = 1;
    public static JEP parser;

    public static class EnergyStore implements IEnergyStorage, ICapabilityProvider
    {
        private final IEnergyStorage               tile;
        private final LazyOptional<IEnergyStorage> holder = LazyOptional.of(() -> this);

        public EnergyStore(IEnergyStorage tile)
        {
            this.tile = tile;
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate)
        {
            return tile.receiveEnergy(maxReceive, simulate);
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate)
        {
            return tile.extractEnergy(maxExtract, simulate);
        }

        @Override
        public int getEnergyStored()
        {
            return tile.getEnergyStored();
        }

        @Override
        public int getMaxEnergyStored()
        {
            return tile.getEnergyStored();
        }

        @Override
        public boolean canExtract()
        {
            return tile.canExtract();
        }

        @Override
        public boolean canReceive()
        {
            return tile.canReceive();
        }

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side)
        {
            return CapabilityEnergy.ENERGY.orEmpty(cap, holder);
        }

    }

    public static int getEnergyGain(int level, int spAtk, int atk, PokedexEntry entry)
    {
        int power = Math.max(atk, spAtk);
        if (parser == null)
        {
            initParser();
        }
        parser.setVarValue("x", level);
        parser.setVarValue("a", power);
        double value = parser.getValue();
        if (Double.isNaN(value))
        {
            initParser();
            parser.setVarValue("x", level);
            parser.setVarValue("a", power);
            value = parser.getValue();
            if (Double.isNaN(value))
            {
                value = 0;
            }
        }
        power = (int) value;
        return Math.max(1, power);
    }

    public static int getMaxEnergy(int level, int spAtk, int atk, PokedexEntry entry)
    {
        return getEnergyGain(level, spAtk, atk, entry);
    }

    public static void initParser()
    {
        parser = new JEP();
        parser.initFunTab(); // clear the contents of the function table
        parser.addStandardFunctions();
        parser.initSymTab(); // clear the contents of the symbol table
        parser.addStandardConstants();
        parser.addComplex(); // among other things adds i to the symbol
                             // table
        parser.addVariable("x", 0);
        parser.addVariable("a", 0);
        parser.parseExpression(PokecubeAdv.config.powerFunction);
    }

    public static int getOutput(SiphonTile tile, int power, boolean simulated)
    {
        if (tile.getWorld() == null || power == 0) return 0;
        Vector3 v = Vector3.getNewVector().set(tile);
        AxisAlignedBB box = (tile.box != null) ? tile.box : (tile.box = v.getAABB().grow(10, 10, 10));
        List<MobEntity> l = tile.mobs;
        if (tile.updateTime == -1 || tile.updateTime < tile.getWorld().getGameTime())
        {
            l.clear();
            l = tile.mobs = tile.getWorld().getEntitiesWithinAABB(MobEntity.class, box);
            tile.updateTime = tile.getWorld().getGameTime() + UPDATERATE;
        }
        int ret = 0;
        power = Math.min(power, PokecubeAdv.config.maxOutput);
        for (MobEntity living : l)
        {
            if (living != null && living.addedToChunk)
            {
                IEnergyStorage producer = living.getCapability(CapabilityEnergy.ENERGY).orElse(null);
                if (producer != null)
                {
                    double dSq = Math.max(1, living.getDistanceSq(tile.getPos().getX() + 0.5, tile.getPos().getY()
                            + 0.5, tile.getPos().getZ() + 0.5));
                    int input = (producer.extractEnergy((int) (PokecubeAdv.config.maxOutput / dSq), simulated));
                    ret += input;
                    if (ret >= power)
                    {
                        ret = power;
                        break;
                    }
                }
            }
        }
        ret = Math.min(ret, PokecubeAdv.config.maxOutput);
        if (!simulated) tile.energy.currentOutput = ret;
        return ret;
    }

    @SubscribeEvent
    public static void SiphonEvent(SiphonTickEvent event)
    {
        Map<IEnergyStorage, Integer> tiles = Maps.newHashMap();
        Integer output = (int) getOutput(event.getTile(), PokecubeAdv.config.maxOutput, true);
        event.getTile().energy.theoreticalOutput = output;
        event.getTile().energy.currentOutput = 0;
        IEnergyStorage producer = event.getTile().getCapability(CapabilityEnergy.ENERGY).orElse(null);
        Integer start = output;
        Vector3 v = Vector3.getNewVector().set(event.getTile());
        for (Direction side : Direction.values())
        {
            TileEntity te = v.getTileEntity(event.getTile().getWorld(), side);
            IEnergyStorage cap;
            if (te != null && (cap = te.getCapability(CapabilityEnergy.ENERGY, side.getOpposite()).orElse(
                    null)) != null)
            {
                Integer toSend = cap.receiveEnergy(output, true);
                if (toSend > 0 && cap.canReceive())
                {
                    tiles.put(cap, toSend);
                }
            }
        }
        for (Map.Entry<IEnergyStorage, Integer> entry : tiles.entrySet())
        {
            Integer fraction = output / tiles.size();
            Integer request = entry.getValue();
            if (request > fraction)
            {
                request = fraction;
            }
            if (fraction == 0 || output <= 0) continue;
            IEnergyStorage h = entry.getKey();
            output -= request;
            h.receiveEnergy(request, false);
        }
        producer.extractEnergy(start - output, false);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    /** Priority low, so that the IPokemob capability is added first. */
    public static void onEntityCapabilityAttach(AttachCapabilitiesEvent<Entity> event)
    {
        if (!event.getCapabilities().containsKey(EventsHandler.POKEMOBCAP) || event.getCapabilities().containsKey(
                ENERGYCAP) || event.getObject().getEntityWorld() == null) return;
        IPokemob pokemob = event.getCapabilities().get(EventsHandler.POKEMOBCAP).getCapability(
                CapabilityPokemob.POKEMOB_CAP).orElse(null);
        if (pokemob != null)
        {
            event.addCapability(ENERGYCAP, new ProviderPokemob(pokemob));
        }
    }

    @SubscribeEvent
    public static void onTileCapabilityAttach(AttachCapabilitiesEvent<TileEntity> event)
    {
        if (event.getCapabilities().containsKey(ENERGYCAP)) return;
        // if (event.getObject() instanceof TileEntityAFA)
        // {
        // event.addCapability(new ResourceLocation("pokecube:tesla"), new
        // ProviderAFA((TileEntityAFA) event
        // .getObject()));
        // }
        if (event.getObject() instanceof SiphonTile)
        {
            ((SiphonTile) event.getObject()).energy = new SiphonTile.EnergyStore();
            event.addCapability(ENERGYCAP, ((SiphonTile) event.getObject()).energy);
        }
        if (event.getObject() instanceof BaseGeneticsTile)
        {
            event.addCapability(ENERGYCAP, new EnergyStore((IEnergyStorage) event.getObject()));
        }
        // if (event.getObject() instanceof TileEntityWarpPad)
        // {
        // event.addCapability(new ResourceLocation("pokecube:tesla"), new
        // ProviderWarppad((TileEntityWarpPad) event
        // .getObject()));
        // }
    }

    public static class ProviderPokemob extends EnergyStorage implements ICapabilityProvider
    {
        private final LazyOptional<IEnergyStorage> holder = LazyOptional.of(() -> this);
        final IPokemob                             pokemob;

        public ProviderPokemob(IPokemob pokemob)
        {
            super(0);
            this.pokemob = pokemob;
        }

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side)
        {
            return CapabilityEnergy.ENERGY.orEmpty(cap, holder);
        }

        @Override
        public boolean canExtract()
        {
            return pokemob.isType(PokeType.getType("electric"));
        }

        @Override
        public int extractEnergy(int power, boolean simulate)
        {
            if (!canExtract()) return 0;
            MobEntity living = pokemob.getEntity();
            int spAtk = pokemob.getStat(Stats.SPATTACK, true);
            int atk = pokemob.getStat(Stats.ATTACK, true);
            int level = pokemob.getLevel();
            int maxEnergy = getMaxEnergy(level, spAtk, atk, pokemob.getPokedexEntry());
            int pokeEnergy = maxEnergy;
            int dE;
            long energyTime = living.getEntityWorld().getGameTime();
            if (living.getPersistentData().contains("energyRemaining"))
            {
                long time = living.getPersistentData().getLong("energyTime");
                if (energyTime != time)
                {
                    pokeEnergy = maxEnergy;
                }
                else
                {
                    pokeEnergy = living.getPersistentData().getInt("energyRemaining");
                }
            }
            dE = (maxEnergy);
            dE = Math.min(dE, power);
            if (!simulate)
            {
                living.getPersistentData().putLong("energyTime", energyTime);
                living.getPersistentData().putInt("energyRemaining", pokeEnergy - dE);
                int drain = 0;
                if (pokeEnergy - dE < 0)
                {
                    drain = dE - pokeEnergy;
                }
                if (living.ticksExisted % 2 == 0)
                {
                    int time = pokemob.getHungerTime();
                    pokemob.setHungerTime(time + Config.instance.energyHungerCost + drain
                            * Config.instance.energyHungerCost);
                }
            }
            return dE;
        }
    }

}
