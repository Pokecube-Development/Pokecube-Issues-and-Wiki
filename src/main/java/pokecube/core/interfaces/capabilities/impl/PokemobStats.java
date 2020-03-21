package pokecube.core.interfaces.capabilities.impl;

import java.util.Random;

import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.GameRules;
import pokecube.core.PokecubeCore;
import pokecube.core.events.pokemob.LevelUpEvent;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.stats.StatModifiers;
import pokecube.core.network.pokemobs.PacketNickname;
import pokecube.core.network.pokemobs.PacketSyncExp;
import pokecube.core.utils.PokeType;
import pokecube.core.utils.Tools;

public abstract class PokemobStats extends PokemobGenes
{
    @Override
    public void addHappiness(final int toAdd)
    {
        this.bonusHappiness += toAdd;
        this.dataSync().set(this.params.HAPPYDW, Integer.valueOf(this.bonusHappiness));
    }

    @Override
    public int getExp()
    {
        return this.getMoveStats().exp;
    }

    @Override
    public int getHappiness()
    {
        this.bonusHappiness = this.dataSync().get(this.params.HAPPYDW);
        this.bonusHappiness = Math.max(this.bonusHappiness, -this.getPokedexEntry().getHappiness());
        this.bonusHappiness = Math.min(this.bonusHappiness, 255 - this.getPokedexEntry().getHappiness());
        return this.bonusHappiness + this.getPokedexEntry().getHappiness();
    }

    @Override
    public StatModifiers getModifiers()
    {
        return this.modifiers;
    }

    @Override
    public String getPokemonNickname()
    {
        return this.dataSync().get(this.params.NICKNAMEDW);
    }

    @Override
    public int getRNGValue()
    {
        if (this.personalityValue == 0) this.personalityValue = new Random().nextInt();
        return this.personalityValue;
    }

    /**
     * Returns 1st type.
     *
     * @see PokeType
     * @return the byte type
     */
    @Override
    public PokeType getType1()
    {
        final PokeType type = PokeType.getType(this.dataSync().get(this.params.TYPE1DW));
        return type != PokeType.unknown ? type : this.getPokedexEntry().getType1();
    }

    /**
     * Returns 2nd type.
     *
     * @see PokeType
     * @return the byte type
     */
    @Override
    public PokeType getType2()
    {
        final PokeType type = PokeType.getType(this.dataSync().get(this.params.TYPE2DW));
        return type != PokeType.unknown ? type : this.getPokedexEntry().getType2();
    }

    @Override
    public boolean isShadow()
    {
        final boolean isShadow = this.getPokedexEntry().isShadowForme;
        if (isShadow && !this.wasShadow) this.wasShadow = true;
        return isShadow;
    }

    @Override
    public IPokemob setExp(int exp, final boolean notifyLevelUp)
    {
        if (!this.getEntity().isAlive()) return this;
        final int old = this.getMoveStats().exp;
        this.getMoveStats().oldLevel = this.getLevel();
        final int lvl100xp = Tools.maxXPs[this.getExperienceMode()];
        exp = Math.min(lvl100xp, exp);
        this.getMoveStats().exp = exp;
        final int newLvl = Tools.xpToLevel(this.getExperienceMode(), exp);
        final int oldLvl = Tools.xpToLevel(this.getExperienceMode(), old);
        IPokemob ret = this;
        if (oldLvl != newLvl)
        {
            // Fire event to allow others to interfere
            final LevelUpEvent lvlup = new LevelUpEvent(this, newLvl, this.getMoveStats().oldLevel);
            PokecubeCore.POKEMOB_BUS.post(lvlup);
            if (!lvlup.isCanceled())
            {
                if (notifyLevelUp)
                {
                    this.updateHealth();
                    final ItemStack held = this.getHeldItem();
                    if (this.getEntity().isAlive() && (this.canEvolve(ItemStack.EMPTY) || this.canEvolve(held)))
                    {
                        this.levelUp(newLvl);
                        final IPokemob evo = this.evolve(true, false, held);
                        if (evo != null) ret = evo;
                    }
                    ret.levelUp(newLvl);
                    if (this.getEntity().addedToChunk && ret.getOwner() instanceof PlayerEntity && this.getEntity()
                            .getEntityWorld().getGameRules().getBoolean(GameRules.DO_MOB_LOOT) && !this.getEntity()
                                    .getEntityWorld().isRemote) this.getEntity().getEntityWorld().addEntity(
                                            new ExperienceOrbEntity(this.getEntity().getEntityWorld(), this
                                                    .getEntity().posX, this.getEntity().posY, this.getEntity().posZ,
                                                    1));
                }
            }
            else this.getMoveStats().exp = old;
        }
        PacketSyncExp.sendUpdate(ret);
        return ret;
    }

    @Override
    public IPokemob setForSpawn(final int exp, final boolean evolve)
    {
        final int level = Tools.xpToLevel(this.getExperienceMode(), exp);
        this.getMoveStats().oldLevel = 0;
        this.getMoveStats().exp = exp;
        IPokemob ret = this.levelUp(level);
        final ItemStack held = this.getHeldItem();
        if (evolve) while (ret.canEvolve(held))
        {
            final IPokemob temp = ret.evolve(false, true, held);
            if (temp == null) break;
            ret = temp;
            ret.getMoveStats().exp = exp;
            ret.levelUp(level);
        }
        return ret;
    }

    @Override
    public void setPokemonNickname(final String nickname)
    {
        if (!this.getEntity().isServerWorld())
        {
            if (!nickname.equals(this.getPokemonNickname()) && this.getEntity().addedToChunk) PacketNickname.sendPacket(
                    this.getEntity(), nickname);
        }
        else if (this.getPokedexEntry().getName().equals(nickname)) this.dataSync().set(this.params.NICKNAMEDW, "");
        else this.dataSync().set(this.params.NICKNAMEDW, nickname);
    }

    @Override
    public void setRNGValue(int value)
    {
        if (value == 0) value = new Random().nextInt();
        this.personalityValue = value;
    }

    @Override
    public void setType1(final PokeType type1)
    {
        if (type1 == this.getType1()) return;
        final String name = type1 == null || type1 == PokeType.unknown ? "" : type1.name;
        this.dataSync().set(this.params.TYPE1DW, name);
    }

    @Override
    public void setType2(final PokeType type2)
    {
        if (type2 == this.getType2()) return;
        final String name = type2 == null || type2 == PokeType.unknown ? "" : type2.name;
        this.dataSync().set(this.params.TYPE2DW, name);
    }

}
