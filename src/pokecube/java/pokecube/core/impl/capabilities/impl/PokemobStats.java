package pokecube.core.impl.capabilities.impl;

import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.stats.StatModifiers;
import pokecube.api.events.pokemobs.LevelUpEvent;
import pokecube.api.utils.PokeType;
import pokecube.api.utils.Tools;
import pokecube.core.network.pokemobs.PacketNickname;
import pokecube.core.network.pokemobs.PacketSyncExp;
import thut.core.common.ThutCore;
import thut.lib.TComponent;

public abstract class PokemobStats extends PokemobGenes
{
    @Override
    public void addHappiness(final int toAdd)
    {
        this.bonusHappiness += toAdd;
        this.params.HAPPYDW.set(this.bonusHappiness);
    }

    @Override
    public int getExp()
    {
        return this.getMoveStats().exp;
    }

    @Override
    public int getHappiness()
    {
        this.bonusHappiness = this.params.HAPPYDW.get();
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
        return this.params.NICKNAMEDW.get();
    }

    @Override
    public int getRNGValue()
    {
        if (this.personalityValue == 0) this.personalityValue = ThutCore.newRandom().nextInt();
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
        final PokeType type = PokeType.getType(this.params.TYPE1DW.get());
        return type != PokeType.unknown ? type : super.getType1();
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
        final PokeType type = PokeType.getType(this.params.TYPE2DW.get());
        return type != PokeType.unknown ? type : super.getType2();
    }

    @Override
    public boolean isShadow()
    {
        final boolean isShadow = this.getPokedexEntry().isShadowForme;
        if (isShadow && !this.wasShadow) this.wasShadow = true;
        return isShadow;
    }

    @Override
    public void setExp(int exp, final boolean notifyLevelUp)
    {
        Mob mob = this.getEntity();
        if (!mob.isAlive()) return;
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
            PokecubeAPI.POKEMOB_BUS.post(lvlup);
            if (!lvlup.isCanceled())
            {
                if (notifyLevelUp)
                {
                    this.updateHealth();
                    final ItemStack held = this.getHeldItem();
                    if (mob.isAlive() && (this.canEvolve(ItemStack.EMPTY) || this.canEvolve(held)))
                    {
                        this.levelUp(newLvl);
                        this.evolve(true, false, held);
                    }
                    ret.levelUp(newLvl);
                    if (mob.isAddedToLevel() && ret.getOwner() instanceof Player
                            && mob.level().getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)
                            && !mob.level().isClientSide)
                        mob.level().addFreshEntity(
                                new ExperienceOrb(mob.level(), mob.getX(), mob.getY(), mob.getZ(), 1));
                }
            }
            else this.getMoveStats().exp = old;
        }
        PacketSyncExp.sendUpdate(ret);
    }

    @Override
    public void setForSpawn(final int exp, final boolean evolve)
    {
        final int level = Tools.xpToLevel(this.getExperienceMode(), exp);
        this.getMoveStats().oldLevel = 0;
        this.getMoveStats().exp = exp;
        this.levelUp(level);
        final ItemStack held = this.getHeldItem();
        if (evolve) while (this.canEvolve(held))
        {
            boolean evolved = this.evolve(false, true, held);
            if (!evolved) break;
            this.getMoveStats().exp = exp;
            this.levelUp(level);
        }
    }

    @Override
    public void setPokemonNickname(final String nickname)
    {
        final boolean oldName = this.getPokedexEntry().getName().equals(nickname) || nickname.trim().isEmpty();
        String name = nickname;
        if (!this.getEntity().isEffectiveAi())
        {
            if (!nickname.equals(this.getPokemonNickname()) && this.getEntity().isAddedToLevel())
                PacketNickname.sendPacket(this.getEntity(), nickname);
        }
        else if (oldName) name = "";
        this.params.NICKNAMEDW.set(name);
        if (this.getEntity().isAddedToLevel())
            this.getEntity().setCustomName(oldName ? null : TComponent.literal(nickname));
    }

    @Override
    public void setRNGValue(int value)
    {
        if (value == 0) value = ThutCore.newRandom().nextInt();
        this.personalityValue = value;
    }

    @Override
    public void setType1(final PokeType type1)
    {
        if (type1 == this.getType1()) return;
        final String name = type1 == null || type1 == PokeType.unknown ? "" : type1.name;
        this.params.TYPE1DW.set(name);
    }

    @Override
    public void setType2(final PokeType type2)
    {
        if (type2 == this.getType2()) return;
        final String name = type2 == null || type2 == PokeType.unknown ? "" : type2.name;
        this.params.TYPE2DW.set(name);
    }

}
