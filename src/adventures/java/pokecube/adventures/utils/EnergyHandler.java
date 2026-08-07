package pokecube.adventures.utils;

import com.google.common.collect.Maps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;
import org.nfunk.jep.JEP;
import pokecube.adventures.Config;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.blocks.siphon.SiphonTickEvent;
import pokecube.adventures.blocks.siphon.SiphonTile;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.IPokemob.Stats;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.utils.PokeType;
import pokecube.core.PokecubeCore;
import pokecube.core.entity.pokemobs.EntityPokemob;
import thut.api.ThutCaps;
import thut.api.Tracker;
import thut.api.attachments.Energy;
import thut.api.attachments.Energy.Wrapping;
import thut.api.attachments.Linkable;
import thut.api.data.HolderProvider;
import thut.api.maths.Vector3;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class EnergyHandler
{
    public static JEP powerParser, hungerParser;

    public static int getEnergyGain(final int level, final int spAtk, final int atk)
    {
        int power = Math.max(atk, spAtk);
        EnergyHandler.powerParser.setVarValue("x", level);
        EnergyHandler.powerParser.setVarValue("a", power);
        double value = EnergyHandler.powerParser.getValue();
        if (Double.isNaN(value))
        {
            value = 0;
        }
        power = (int) value;
        return Math.max(1, power);
    }

    public static int getHungerCost(final int level, final int spAtk, final int atk, double relativeGain)
    {
        int power = Math.max(atk, spAtk);
        EnergyHandler.hungerParser.setVarValue("x", level);
        EnergyHandler.hungerParser.setVarValue("a", power);
        EnergyHandler.hungerParser.setVarValue("r", relativeGain);
        double value = EnergyHandler.hungerParser.getValue();
        if (Double.isNaN(value))
        {
            value = 0;
        }
        power = (int) value;
        return Math.max(0, power);
    }

    public static boolean initPowerParser(JEP jep, String func)
    {
        jep.initFunTab(); // clear the contents of the function table
        jep.addStandardFunctions();
        jep.initSymTab(); // clear the contents of the symbol table
        jep.addStandardConstants();
        jep.addComplex(); // among other things adds i to the symbol table
        jep.addVariable("x", 0); // Level
        jep.addVariable("a", 0); // Max of atk and spatk
        jep.parseExpression(func);
        return !jep.hasError();
    }

    public static boolean initHungerParser(JEP jep, String func)
    {
        jep.initFunTab(); // clear the contents of the function table
        jep.addStandardFunctions();
        jep.initSymTab(); // clear the contents of the symbol table
        jep.addStandardConstants();
        jep.addComplex(); // among other things adds i to the symbol table
        jep.addVariable("x", 0); // Level
        jep.addVariable("r", 0); // Power draw / Production of mob
        jep.addVariable("a", 0); // Max of atk and spatk
        jep.parseExpression(func);
        return !jep.hasError();
    }
    public static void initPowerParser()
    {
        powerParser = new JEP();
        initPowerParser(powerParser, PokecubeAdv.config.siphonPowerFuncion);
        hungerParser = new JEP();
        initHungerParser(hungerParser, PokecubeAdv.config.siphonHungerFunction);
    }

    public static int getOutput(final SiphonTile tile, int power, final boolean simulated, Map<UUID, Integer> energyMap)
    {
        if (tile.getLevel() == null || power == 0) return 0;
        final AABB box =
                tile.box != null ? tile.box : (tile.box = new Vector3().set(tile).getAABB().inflate(10, 10, 10));
        List<Entity> l = tile.mobs;
        if (tile.updateTime == -1 || tile.updateTime < tile.getLevel().getGameTime())
        {
            l.clear();
            l = tile.mobs = tile.getLevel().getEntitiesOfClass(Entity.class, box);
            tile.updateTime = tile.getLevel().getGameTime() + PokecubeAdv.config.siphonUpdateRate;
        }
        int ret = 0;
        for (final Entity entity : l)
            if (entity != null && entity.isAddedToLevel() && entity.isAlive())
            {
                final IEnergyStorage producer = ThutCaps.getEnergy(entity);
                if (producer != null && producer.canExtract() && producer.getEnergyStored() > 0)
                {
                    double eScale = (entity.getBbHeight()*entity.getBbWidth());
                    double dSq = Math.max(1,
                            entity.distanceToSqr(tile.getBlockPos().getX() + 0.5, tile.getBlockPos().getY() + 0.5,
                                    tile.getBlockPos().getZ() + 0.5)) / eScale;
                    int toExtract = (int) (PokecubeAdv.config.siphonMaxOutput / dSq);
                    toExtract = energyMap.getOrDefault(entity.getUUID(), toExtract);
                    energyMap.put(entity.getUUID(), toExtract);
                    if (toExtract <= 0) continue;
                    final int extract = producer.extractEnergy(toExtract, simulated);
                    ret += extract;
                    if (ret >= power)
                    {
                        ret = power;
                        break;
                    }
                }
            }
        ret = Math.min(ret, PokecubeAdv.config.siphonMaxOutput);
        return ret;
    }

    @SubscribeEvent
    public static void SiphonEvent(final SiphonTickEvent event)
    {
        var tile = event.getTile();
        if (!(tile.getLevel() instanceof ServerLevel world)) return;
        // Ensure links exist
        if(Linkable.get(tile, null) == null)
        {
            var data = Linkable.DEFAULT().make(tile);
            for (Direction d : Direction.values())
            {
                var id = d.ordinal();
                tile.setData(Linkable.TYPES[id], data);
            }
        }

        final Map<IEnergyStorage, Integer> tiles = Maps.newHashMap();
        Map<UUID, Integer> mobs = Maps.newHashMap();
        int output = EnergyHandler.getOutput(tile, PokecubeAdv.config.siphonMaxOutput, true, mobs);
        tile.energy.theoreticalOutput = output;
        tile.energy.currentOutput = output;
        final IEnergyStorage producer = ThutCaps.getEnergy(tile);
        final int start = output;
        final Vector3 v = new Vector3().set(tile);
        for (final Direction side : Direction.values())
        {
            final BlockEntity te = v.getTileEntity(world, side);
            IEnergyStorage cap = null;
            if (te != null && Energy.has(te, side.getOpposite()))
            {
                cap = Energy.get(te, side.getOpposite());
            }
            else if (te != null && Energy.has(te))
            {
                cap = Energy.get(te);
            }
            if (cap != null)
            {
                if (!cap.canReceive()) continue;
                final int toSend = cap.receiveEnergy(output, true);
                if (toSend > 0) tiles.put(cap, toSend);
            }
        }
        if (PokecubeAdv.config.wirelessSiphons) for (final GlobalPos pos : tile.wirelessLinks)
        {
            final BlockPos bpos = pos.pos();
            final ResourceKey<Level> dim = pos.dimension();
            if (dim != world.dimension()) continue;
            if (!world.isLoaded(bpos)) continue;
            final BlockEntity te = world.getBlockEntity(bpos);
            if (te == null) continue;
            IEnergyStorage cap;
            for (final Direction side : Direction.values())
                if ((cap = ThutCaps.getEnergy(te, side.getOpposite())) != null)
                {
                    if (!cap.canReceive()) continue;
                    final int toSend = cap.receiveEnergy(output, true);
                    if (toSend > 0)
                    {
                        tiles.put(cap, toSend);
                        break;
                    }
                }
        }
        // Nowhere to send power to, so return early.
        if (tiles.isEmpty()) return;

        final int fraction = output / tiles.size();
        int rem = output % tiles.size();
        for (final Map.Entry<IEnergyStorage, Integer> entry : tiles.entrySet())
        {
            int avail = fraction;
            if (rem > 0)
            {
                avail++;
                rem--;
            }
            int request = entry.getValue();
            if (request > fraction) request = avail;
            if (avail == 0 || output <= 0) continue;
            final IEnergyStorage h = entry.getKey();
            output -= request;
            h.receiveEnergy(request, false);
        }
        boolean powered = world.getDirectSignalTo(tile.getBlockPos()) >= 15;
        int extract = start - output;
        // If we are powered, try to extract all of the energy, this still
        // limits it to PokecubeAdv.config.maxOutput per mob, but allows
        // extracting that much from multiples.
        if (powered) extract = Integer.MAX_VALUE;
        producer.extractEnergy(extract, false);
        EnergyHandler.getOutput(tile, extract, false, mobs);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static void AttachCaps(final RegisterCapabilitiesEvent event)
    {
        // TODO figure out a good way to support not-pokemobs later.
        PokecubeCore.typeMap.keySet()
                .forEach(type -> event.registerEntity(Capabilities.EnergyStorage.ENTITY, type, new ProviderPokemob()));

        List<BlockEntityType<?>> TYPES = new ArrayList<>();
        TYPES.add(PokecubeAdv.AFA_TYPE.get());
        TYPES.add(PokecubeAdv.SIPHON_TYPE.get());
        TYPES.add(PokecubeAdv.WARP_PAD_TYPE.get());
        TYPES.add(PokecubeAdv.STATUE_TYPE.get());

        TYPES.add(PokecubeAdv.CLONER_TYPE.get());
        TYPES.add(PokecubeAdv.EXTRACTOR_TYPE.get());
        TYPES.add(PokecubeAdv.SPLICER_TYPE.get());

        TYPES.forEach(type -> event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, type, new ProviderTile()));

        // Now register the attachments
        Energy.REGISTRY.register(new HolderProvider.Provider<>()
        {
            final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("pokecube", "pokemob");

            @Override
            public EnergyStorage apply(IAttachmentHolder t)
            {
                if (t instanceof EntityPokemob) return new PokemobEnergy(() -> PokemobCaps.getPokemobFor(t));
                return null;
            }

            @Override
            protected ResourceLocation key()
            {
                return ID;
            }
        });

        Energy.REGISTRY.register(new HolderProvider.Provider<>()
        {
            final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("pokecube", "wrapping");

            @Override
            public EnergyStorage apply(IAttachmentHolder t)
            {
                if (t instanceof IEnergyStorage tile) return Wrapping.wrap(tile);
                return null;
            }

            @Override
            protected ResourceLocation key()
            {
                return ID;
            }
        });

        Energy.REGISTRY.register(new HolderProvider.Provider<>()
        {
            final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("pokecube", "siphon");

            @Override
            public EnergyStorage apply(IAttachmentHolder t)
            {
                if (t instanceof SiphonTile) return new SiphonTile.EnergyStore();
                return null;
            }

            @Override
            protected ResourceLocation key()
            {
                return ID;
            }
        });

    }

    public static class ProviderTile<T extends BlockEntity> implements ICapabilityProvider<T, Direction, IEnergyStorage>
    {
        @Override
        public @Nullable IEnergyStorage getCapability(T object, Direction context)
        {
            context = Direction.DOWN;
            return Energy.get_raw(object, context);
        }
    }

    public static class ProviderPokemob implements ICapabilityProvider<Mob, Direction, IEnergyStorage>
    {
        @Override
        public @Nullable IEnergyStorage getCapability(Mob object, Direction context)
        {
            context = Direction.DOWN;
            return Energy.get_raw(object, context);
        }
    }

    public static class PokemobEnergy extends EnergyStorage
    {
        final Supplier<IPokemob> pokemob;

        long lastTickCheck = -1;

        public PokemobEnergy(final Supplier<IPokemob> pokemob)
        {
            super(0);
            this.energy = -1;
            this.pokemob = pokemob;
        }

        @Override
        public boolean canReceive()
        {
            return this.getMaxEnergyStored() > 0;
        }

        @Override
        public boolean canExtract()
        {
            return this.getMaxEnergyStored() > 0;
        }

        @Override
        public int getEnergyStored()
        {
            return super.getEnergyStored();
        }

        @Override
        public int getMaxEnergyStored()
        {
            IPokemob pokemob = this.pokemob.get();
            // Not electric type, no energy to extract.
            if (!pokemob.isType(PokeType.getType("electric"))) return 0;

            final Mob living = pokemob.getEntity();
            // We will update our energy when this is called, as that
            if (Tracker.instance().getTick() != this.lastTickCheck)
            {
                this.lastTickCheck = Tracker.instance().getTick();
                final int spAtk = pokemob.getStat(Stats.SPATTACK, true);
                final int atk = pokemob.getStat(Stats.ATTACK, true);
                final int level = pokemob.getLevel();
                this.capacity = EnergyHandler.getEnergyGain(level, spAtk, atk);
                if(this.energy < 0) this.energy = living.getPersistentData().getInt("pokecube:energy");
                double dE = this.capacity - this.energy;
                this.maxReceive = this.capacity / 5;
                this.maxExtract = this.capacity;
                dE = Math.min(dE, this.maxReceive);
                if (dE > 0)
                {
                    double regen = dE / this.capacity;
                    this.energy += dE;
                    int hunger = EnergyHandler.getHungerCost(level, spAtk, atk, regen);
                    System.out.println(hunger);
                    pokemob.applyHunger(hunger);
                }
                living.getPersistentData().putInt("pokecube:energy", this.energy);
            }
            return this.capacity;
        }
    }

}
