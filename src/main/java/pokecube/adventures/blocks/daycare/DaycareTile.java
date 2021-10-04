package pokecube.adventures.blocks.daycare;

import java.util.List;

import org.nfunk.jep.JEP;

import com.google.common.collect.Lists;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import pokecube.adventures.PokecubeAdv;
import pokecube.core.blocks.InteractableTile;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.utils.EntityTools;
import pokecube.core.utils.Tools;

public class DaycareTile extends InteractableTile implements TickingBlockEntity
{
    public static JEP expToGive;
    public static JEP pwrPerExp;

    public static void initParser(final String pwr, final String exp)
    {
        DaycareTile.pwrPerExp = new JEP();
        DaycareTile.pwrPerExp.initFunTab(); // clear the contents of the
                                            // function
                                            // table
        DaycareTile.pwrPerExp.addStandardFunctions();
        DaycareTile.pwrPerExp.initSymTab(); // clear the contents of the symbol
                                            // table
        DaycareTile.pwrPerExp.addStandardConstants();
        DaycareTile.pwrPerExp.addComplex(); // among other things adds i to the
                                            // symbol
        // table
        DaycareTile.pwrPerExp.addVariable("x", 0); // Exp
        DaycareTile.pwrPerExp.addVariable("l", 0); // Level
        DaycareTile.pwrPerExp.addVariable("n", 0); // total needed this level
        DaycareTile.pwrPerExp.parseExpression(pwr);

        DaycareTile.expToGive = new JEP();
        DaycareTile.expToGive.initFunTab(); // clear the contents of the
                                            // function
                                            // table
        DaycareTile.expToGive.addStandardFunctions();
        DaycareTile.expToGive.initSymTab(); // clear the contents of the symbol
                                            // table
        DaycareTile.expToGive.addStandardConstants();
        DaycareTile.expToGive.addComplex(); // among other things adds i to the
                                            // symbol
        // table
        DaycareTile.expToGive.addVariable("x", 0); // Exp
        DaycareTile.expToGive.addVariable("l", 0); // Level
        DaycareTile.expToGive.addVariable("n", 0); // total needed this level
        DaycareTile.expToGive.parseExpression(exp);
    }

    private IItemHandlerModifiable itemstore;

    public float power = 0;

    public int redstonePower = 0;

    public DaycareTile()
    {
        super(PokecubeAdv.DAYCARE_TYPE.get());
        this.itemstore = (IItemHandlerModifiable) this.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                .orElse(null);
    }

    public DaycareTile(final BlockEntityType<?> tileEntityTypeIn)
    {
        super(tileEntityTypeIn);
    }

    private void checkPower(final int target)
    {
        if (this.power > target) return;
        ItemStack emeralds = this.itemstore.getStackInSlot(0);
        if (emeralds.isEmpty()) return;
        int needed = Mth.ceil(target / (double) PokecubeAdv.config.dayCarePowerPerFuel);
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
        if (!(this.getLevel() instanceof ServerLevel)) return;
        if (this.getLevel().getGameTime() % PokecubeAdv.config.dayCareTickRate != 0) return;
        this.checkPower(1);
        if (this.power == 0)
        {
            if (this.redstonePower != 0)
            {
                this.redstonePower = 0;
                this.getLevel().updateNeighborsAt(this.getBlockPos(), this.getBlockState().getBlock());
            }
            return;
        }
        final List<Entity> list = Lists.newArrayList();
        EntityTools.getNearMobsFast(list, this.getLevel(), this.getBlockPos(), 16, e -> true);
        for (final Entity mob : list)
        {
            final IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
            if (pokemob == null) continue;
            int level = pokemob.getLevel();
            final boolean gainExp = level < 100;
            if (level >= 100) level = PokecubeAdv.config.dayCareLvl100EffectiveLevel;
            final int type = pokemob.getExperienceMode();

            final int exp_diff = Tools.levelToXp(type, level + 1) - Tools.levelToXp(type, level);

            DaycareTile.expToGive.setVarValue("x", pokemob.getExp());
            DaycareTile.expToGive.setVarValue("l", level);
            DaycareTile.expToGive.setVarValue("n", exp_diff);
            final int exp_out = Mth.ceil(DaycareTile.expToGive.getValue());

            DaycareTile.pwrPerExp.setVarValue("x", exp_out);
            DaycareTile.pwrPerExp.setVarValue("l", level);
            DaycareTile.pwrPerExp.setVarValue("n", exp_diff);
            final int needed = Mth.ceil(DaycareTile.pwrPerExp.getValue() * exp_out);

            this.checkPower(needed);
            if (this.power < needed)
            {
                this.power = 0;
                break;
            }
            this.power -= needed;
            if (gainExp) pokemob.setExp(pokemob.getExp() + exp_out, true);
            if (PokecubeAdv.config.dayCareBreedSpeedup) pokemob.tickBreedDelay(PokecubeAdv.config.dayCareBreedAmount);
        }
        final int power = this.power == 0 ? 0 : 15;
        if (power != this.redstonePower)
        {
            this.redstonePower = power;
            this.getLevel().updateNeighborsAt(this.getBlockPos(), this.getBlockState().getBlock());
        }
    }

    @Override
    public void load(final BlockState state, final CompoundTag compound)
    {
        this.power = compound.getInt("fuel_cache");
        super.load(state, compound);
    }

    @Override
    public CompoundTag save(final CompoundTag compound)
    {
        compound.putFloat("fuel_cache", this.power);
        return super.save(compound);
    }
}
