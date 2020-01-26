package pokecube.adventures.blocks.daycare;

import org.nfunk.jep.JEP;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import pokecube.adventures.PokecubeAdv;
import pokecube.core.blocks.InteractableTile;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;

public class DaycareTile extends InteractableTile implements ITickableTileEntity
{
    public static TileEntityType<? extends TileEntity> TYPE;
    public static JEP                                  pwrToExp;

    public static void initParser(final String function)
    {
        DaycareTile.pwrToExp = new JEP();
        DaycareTile.pwrToExp.initFunTab(); // clear the contents of the function
                                           // table
        DaycareTile.pwrToExp.addStandardFunctions();
        DaycareTile.pwrToExp.initSymTab(); // clear the contents of the symbol
                                           // table
        DaycareTile.pwrToExp.addStandardConstants();
        DaycareTile.pwrToExp.addComplex(); // among other things adds i to the
                                           // symbol
        // table
        DaycareTile.pwrToExp.addVariable("l", 0); // Level
        DaycareTile.pwrToExp.parseExpression(function);
    }

    private IItemHandlerModifiable itemstore;
    private Chunk                  chunk = null;

    int power = 0;

    public DaycareTile()
    {
        super(DaycareTile.TYPE);
        this.itemstore = (IItemHandlerModifiable) this.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                .orElse(null);
    }

    public DaycareTile(final TileEntityType<?> tileEntityTypeIn)
    {
        super(tileEntityTypeIn);
    }

    private void checkPower(final int target)
    {
        if (this.power > target) return;
        ItemStack emeralds = this.itemstore.getStackInSlot(0);
        if (emeralds.isEmpty()) return;
        int needed = MathHelper.ceil(target / (double) PokecubeAdv.config.dayCarePowerPerFuel);
        final int have = emeralds.getCount();
        if (have > needed) emeralds.split(needed);
        else
        {
            emeralds = ItemStack.EMPTY;
            needed = have;
        }
        this.power += needed * PokecubeAdv.config.dayCarePowerPerFuel;
        this.itemstore.setStackInSlot(0, emeralds);
    }

    @Override
    public void tick()
    {
        if (this.getWorld().getGameTime() % PokecubeAdv.config.dayCareTickRate != 0) return;
        this.checkPower(1);
        if (this.power == 0) return;
        if (this.chunk == null) this.chunk = this.getWorld().getChunkAt(this.getPos());
        final ClassInheritanceMultiMap<Entity> mobs = this.chunk.getEntityLists()[this.getPos().getY() / 16];
        for (final Entity mob : mobs)
        {
            final IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
            if (pokemob == null || pokemob.getLevel() == 100) continue;
            DaycareTile.pwrToExp.setVarValue("l", pokemob.getLevel());
            final int exp_out = MathHelper.ceil(DaycareTile.pwrToExp.getValue());
            this.checkPower(1);
            if (this.power == 0)
            {
                mob.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BASEDRUM, 1.0F, 1.0F);
                return;
            }
            mob.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 0.25f, 1);
            pokemob.setExp(pokemob.getExp() + exp_out, true);
            if (PokecubeAdv.config.dayCareBreedSpeedup) pokemob.setLoveTimer(pokemob.getLoveTimer()
                    + PokecubeAdv.config.dayCareBreedAmount);
        }
    }

    @Override
    public void read(final CompoundNBT compound)
    {
        this.power = compound.getInt("fuel_cache");
        super.read(compound);
    }

    @Override
    public CompoundNBT write(final CompoundNBT compound)
    {
        compound.putInt("fuel_cache", this.power);
        return super.write(compound);
    }
}
