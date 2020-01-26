package pokecube.adventures.blocks.afa;

import java.util.Random;

import org.nfunk.jep.JEP;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import pokecube.adventures.PokecubeAdv;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.blocks.InteractableTile;
import pokecube.core.database.abilities.Ability;
import pokecube.core.events.pokemob.SpawnEvent;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.items.pokecubes.PokecubeManager;
import thut.api.maths.Vector3;

public class AfaTile extends InteractableTile implements ITickableTileEntity, IEnergyStorage
{
    public static TileEntityType<? extends TileEntity> TYPE;
    public static final ResourceLocation               SHINYTAG = new ResourceLocation(PokecubeAdv.ID, "shiny_charm");

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

    private final IItemHandlerModifiable itemstore;
    public IPokemob                      pokemob       = null;
    boolean                              shiny         = false;
    public int[]                         shift         = { 0, 0, 0 };
    public int                           scale         = 1000;
    public String                        animation     = "idle";
    public Ability                       ability       = null;
    public int                           distance      = 4;
    public int                           transparency  = 128;
    public boolean                       rotates       = true;
    public float                         angle         = 0;
    public boolean                       noEnergy      = false;
    public boolean                       frozen        = true;
    public float                         animationTime = 0;

    public int energy       = 0;
    boolean    noEnergyNeed = false;

    public AfaTile()
    {
        super(AfaTile.TYPE);
        this.itemstore = (IItemHandlerModifiable) this.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                .orElse(null);
    }

    public void refreshAbility()
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
        this.shiny = PokecubeItems.is(AfaTile.SHINYTAG, stack);
        if (this.shiny) return;
        this.pokemob = PokecubeManager.itemToPokemob(stack, this.getWorld());
        if (this.pokemob != null && this.pokemob.getAbility() != null)
        {
            this.ability = this.pokemob.getAbility();
            this.ability.destroy();
            this.pokemob.getEntity().setPosition(this.getPos().getX() + 0.5, this.getPos().getY() + 0.5, this.getPos()
                    .getZ() + 0.5);
            this.ability.init(this.pokemob, this.distance);
        }
    }

    @Override
    public void tick()
    {
        final ItemStack stack = this.itemstore.getStackInSlot(0);
        if (!stack.isEmpty() && this.pokemob == null) this.refreshAbility();
        else if (stack.isEmpty()) this.refreshAbility();

        boolean shouldUseEnergy = this.pokemob != null && this.ability != null;
        int levelFactor = 0;
        if (this.pokemob != null && this.ability != null) this.shiny = false;

        if (shouldUseEnergy) if (!this.noEnergy && !this.world.isRemote)
        {
            double value;
            if (this.shiny)
            {
                AfaTile.parserS.setVarValue("d", this.distance);
                value = AfaTile.parserS.getValue();
            }
            else
            {
                AfaTile.parser.setVarValue("l", levelFactor);
                AfaTile.parser.setVarValue("d", this.distance);
                value = AfaTile.parser.getValue();
            }
            final int needed = (int) Math.ceil(value);
            if (this.energy < needed)
            {
                this.energy = 0;
                return;
            }
            else this.energy -= needed;
        }

        if (this.pokemob != null && this.ability != null)
        {
            this.shiny = false;
            // Tick increase incase ability tracks this for update.
            // Renderer can also then render it animated.
            this.pokemob.getEntity().ticksExisted++;
            levelFactor = this.pokemob.getLevel();
            // Do not call ability update on client.
            if (!this.world.isRemote) this.ability.onUpdate(this.pokemob);
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
        this.shiny = PokecubeItems.is(AfaTile.SHINYTAG, this.itemstore.getStackInSlot(0));
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
        if (maxReceive + this.energy < this.getMaxEnergyStored()) var = this.getMaxEnergyStored() - this.energy;
        if (!simulate) this.energy += var;
        this.energy = Math.max(0, this.energy);
        this.energy = Math.min(this.getMaxEnergyStored(), this.energy);
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
    public int getEnergyStored()
    {
        return this.energy;
    }

    @Override
    public int getMaxEnergyStored()
    {
        return PokecubeAdv.config.warpPadMaxEnergy;
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
}
