package pokecube.adventures.blocks.afa;

import java.util.Random;

import org.nfunk.jep.JEP;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.IInventoryChangedListener;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Hand;
import net.minecraft.util.IIntArray;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.network.PacketAFA;
import pokecube.core.PokecubeCore;
import pokecube.core.blocks.InteractableTile;
import pokecube.core.database.abilities.Ability;
import pokecube.core.events.pokemob.SpawnEvent;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.items.pokecubes.PokecubeManager;
import thut.api.ThutCaps;
import thut.api.block.IOwnableTE;
import thut.api.item.ItemList;
import thut.api.maths.Vector3;
import thut.core.common.network.TileUpdate;

public class AfaTile extends InteractableTile implements ITickableTileEntity, IEnergyStorage, IInventoryChangedListener
{
    public static TileEntityType<? extends TileEntity> TYPE;
    public static final ResourceLocation               SHINYTAG = new ResourceLocation(PokecubeAdv.MODID,
            "shiny_charm");

    public static JEP parser;
    public static JEP parserS;

    public static void initParser(final String function, final String functionS)
    {
        AfaTile.parser = new JEP();
        AfaTile.parser.initFunTab(); // clear the contents of the function table
        AfaTile.parser.addStandardFunctions();
        AfaTile.parser.initSymTab(); // clear the contents of the symbol table
        AfaTile.parser.addStandardConstants();
        AfaTile.parser.addComplex(); // among other things adds i to the symbol
        // table
        AfaTile.parser.addVariable("d", 0);
        AfaTile.parser.addVariable("l", 0);
        AfaTile.parser.parseExpression(function);

        AfaTile.parserS = new JEP();
        AfaTile.parserS.initFunTab(); // clear the contents of the function
                                      // table
        AfaTile.parserS.addStandardFunctions();
        AfaTile.parserS.initSymTab(); // clear the contents of the symbol table
        AfaTile.parserS.addStandardConstants();
        AfaTile.parserS.addComplex(); // among other things adds i to the symbol
        // table
        AfaTile.parserS.addVariable("d", 0);
        AfaTile.parserS.parseExpression(functionS);
    }

    public final IIntArray syncValues = new IIntArray()
    {

        @Override
        public int get(final int index)
        {
            switch (index)
            {
            case 0:
                return AfaTile.this.orig;
            case 1:
                return AfaTile.this.distance;
            case 2:
                return AfaTile.this.cost;
            }
            return 0;
        }

        @Override
        public void set(final int index, final int value)
        {
            switch (index)
            {
            case 0:
                AfaTile.this.orig = value;
                break;
            case 1:
                AfaTile.this.distance = value;
                break;
            case 2:
                AfaTile.this.cost = value;
                break;
            }
        }

        @Override
        public int size()
        {
            // TODO Auto-generated method stub
            return 3;
        }

    };

    private final IItemHandlerModifiable itemstore;
    public final IInventory              inventory;

    public IPokemob pokemob       = null;
    boolean         shiny         = false;
    public int[]    shift         = { 0, 0, 0 };
    public int      scale         = 1000;
    public String   animation     = "idle";
    public Ability  ability       = null;
    public int      distance      = 4;
    public int      transparency  = 128;
    public boolean  rotates       = true;
    public float    angle         = 0;
    public boolean  noEnergy      = false;
    public boolean  frozen        = true;
    public float    animationTime = 0;

    public int orig   = 0;
    public int energy = 0;
    public int cost   = 0;

    int tick = 0;

    boolean noEnergyNeed = false;

    public AfaTile()
    {
        super(AfaTile.TYPE);
        this.itemstore = (IItemHandlerModifiable) this.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                .orElse(null);
        this.inventory = new AfaContainer.InvWrapper(this.itemstore, (IOwnableTE) this.getCapability(
                ThutCaps.OWNABLE_CAP).orElse(null));
        ((AfaContainer.InvWrapper) this.inventory).addListener(this);
    }

    public void refreshAbility(final boolean update)
    {
        if (this.pokemob != null)
        {
            this.pokemob.getEntity().remove();
            this.pokemob = null;
            this.ability = null;
        }
        if (this.ability != null)
        {
            this.ability.destroy();
            this.ability = null;
        }
        final ItemStack stack = this.itemstore.getStackInSlot(0);
        this.shiny = ItemList.is(AfaTile.SHINYTAG, stack);
        if (this.shiny) return;
        this.pokemob = PokecubeManager.itemToPokemob(stack, this.getWorld());
        if (this.pokemob != null && this.pokemob.getAbility() != null)
        {
            this.ability = this.pokemob.getAbility();
            this.ability.destroy();
            this.pokemob.getEntity().setPosition(this.getPos().getX() + 0.5, this.getPos().getY() + 0.5, this.getPos()
                    .getZ() + 0.5);
            this.ability.init(this.pokemob, this.distance);
            if (this.getWorld() instanceof ServerWorld && update) TileUpdate.sendUpdate(this);
        }
    }

    @Override
    public void onLoad()
    {
        super.onLoad();
        this.refreshAbility(false);
    }

    @Override
    public void tick()
    {
        if (!(this.getWorld() instanceof ServerWorld)) return;
        if (this.tick++ % PokecubeAdv.config.afaTickRate != 0) return;

        int levelFactor = 0;
        if (this.pokemob != null) levelFactor = this.pokemob.getLevel();
        double value = 0;
        if (this.shiny)
        {
            AfaTile.parserS.setVarValue("d", this.distance);
            value = AfaTile.parserS.getValue();
        }
        else if (this.pokemob != null)
        {
            AfaTile.parser.setVarValue("l", levelFactor);
            AfaTile.parser.setVarValue("d", this.distance);
            value = AfaTile.parser.getValue();
        }
        this.cost = (int) Math.ceil(value);

        boolean shouldUseEnergy = this.pokemob != null && this.ability != null && !this.noEnergy;
        if (this.pokemob != null && this.ability != null) this.shiny = false;

        if (shouldUseEnergy && this.energy < this.cost)
        {
            this.energy = 0;
            return;
        }
        else this.energy -= this.cost;

        final boolean hasEnergy = !shouldUseEnergy || this.energy >= 0;

        if (this.pokemob != null && this.ability != null && hasEnergy)
        {
            this.shiny = false;
            // Tick increase incase ability tracks this for update.
            // Renderer can also then render it animated.
            this.pokemob.getEntity().ticksExisted++;
            // Do not call ability update on client.
            this.ability.onUpdate(this.pokemob);
        }
        shouldUseEnergy = shouldUseEnergy || this.shiny;
    }

    @Override
    public void remove()
    {
        super.remove();
        if (this.ability != null) this.ability.destroy();
        PokecubeCore.POKEMOB_BUS.unregister(this);
    }

    @Override
    public void validate()
    {
        super.validate();
        PokecubeCore.POKEMOB_BUS.register(this);
    }

    @Override
    public void read(final CompoundNBT nbt)
    {
        super.read(nbt);
        this.energy = nbt.getInt("energy");
        this.noEnergyNeed = nbt.getBoolean("noEnergyNeed");
        this.shift = nbt.getIntArray("shift");
        if (nbt.contains("scale")) this.scale = nbt.getInt("scale");
        this.distance = nbt.getInt("distance");
        this.angle = nbt.getFloat("angle");
        this.rotates = nbt.getBoolean("rotates");
        this.transparency = nbt.getInt("transparency");
        this.energy = nbt.getInt("energy");
        this.frozen = nbt.getBoolean("frozen");
        this.animationTime = nbt.getFloat("animTime");
        this.animation = nbt.getString("animation");
        this.shiny = ItemList.is(AfaTile.SHINYTAG, this.itemstore.getStackInSlot(0));
        this.orig = this.energy;
    }

    @Override
    public CompoundNBT write(final CompoundNBT nbt)
    {
        final CompoundNBT tag = new CompoundNBT();
        nbt.put("dest", tag);
        nbt.putInt("energy", this.energy);
        nbt.putBoolean("noEnergyNeed", this.noEnergyNeed);
        nbt.putIntArray("shift", this.shift);
        nbt.putInt("scale", this.scale);
        nbt.putInt("distance", this.distance);
        nbt.putFloat("angle", this.angle);
        nbt.putBoolean("rotates", this.rotates);
        nbt.putInt("transparency", this.transparency);
        nbt.putBoolean("frozen", this.frozen);
        nbt.putFloat("animTime", this.animationTime);
        nbt.putString("animation", this.animation);
        return super.write(nbt);
    }

    @Override
    public int receiveEnergy(final int maxReceive, final boolean simulate)
    {
        int var = maxReceive;
        if (maxReceive + this.energy > this.getMaxEnergyStored()) var = this.getMaxEnergyStored() - this.energy;
        if (!simulate) this.energy += var;
        this.energy = Math.max(0, this.energy);
        this.energy = Math.min(this.getMaxEnergyStored(), this.energy);
        this.orig = this.energy;
        return var;
    }

    @Override
    public int extractEnergy(final int maxExtract, final boolean simulate)
    {
        int var = maxExtract;
        if (maxExtract < this.energy) var = this.energy;
        if (!simulate) this.energy -= var;
        this.energy = Math.max(0, this.energy);
        this.energy = Math.min(this.getMaxEnergyStored(), this.energy);
        return var;
    }

    @SubscribeEvent
    public void spawnEvent(final SpawnEvent.Post evt)
    {
        if (this.shiny) if (evt.location.distanceTo(Vector3.getNewVector().set(this)) <= this.distance)
        {
            final Random rand = new Random();
            final int rate = Math.max(PokecubeAdv.config.afaShinyRate, 1);
            if (rand.nextInt(rate) == 0)
            {
                if (!this.noEnergy && !this.world.isRemote)
                {
                    AfaTile.parserS.setVarValue("d", this.distance);
                    final double value = AfaTile.parserS.getValue();
                    final int needed = (int) Math.ceil(value);
                    if (this.energy < needed)
                    {
                        this.energy = 0;
                        this.world.playSound(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(),
                                SoundEvents.BLOCK_NOTE_BLOCK_BASEDRUM, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
                        return;
                    }
                    this.energy -= needed;
                }
                evt.pokemob.setShiny(true);
                this.world.playSound(evt.entity.posX, evt.entity.posY, evt.entity.posZ,
                        SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
                this.world.playSound(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(),
                        SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
            }
        }
    }

    @Override
    public boolean onInteract(final BlockPos pos, final PlayerEntity player, final Hand hand,
            final BlockRayTraceResult hit)
    {
        if (player instanceof ServerPlayerEntity) PacketAFA.openGui((ServerPlayerEntity) player, this);
        return true;
    }

    @Override
    public int getEnergyStored()
    {
        return this.energy;
    }

    @Override
    public int getMaxEnergyStored()
    {
        return PokecubeAdv.config.afaMaxEnergy;
    }

    @Override
    public boolean canExtract()
    {
        return false;
    }

    @Override
    public boolean canReceive()
    {
        return true;
    }

    @Override
    public void onInventoryChanged(final IInventory invBasic)
    {
        this.refreshAbility(true);
    }
}
