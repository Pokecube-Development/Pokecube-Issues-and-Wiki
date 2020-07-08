package pokecube.legends.init.moves.world;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.text.TranslationTextComponent;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IMoveAction;
import pokecube.core.interfaces.IPokemob;
import pokecube.legends.handlers.WormHoleSpawnHandler;
import thut.api.entity.IHungrymob;
import thut.api.maths.Vector3;

public class ActionHiddenPower implements IMoveAction
{
    public ActionHiddenPower()
    {
    }

    @Override
    public boolean applyEffect(final IPokemob user, final Vector3 location)
    {
        if (user.inCombat()) return false;
        final LivingEntity owner = user.getOwner();
        final TranslationTextComponent message;
        if (owner == null) return false;
        final IHungrymob mob = user;
        int count = 1;
        final int level = user.getLevel();
        final int hungerValue = PokecubeCore.getConfig().pokemobLifeSpan / 16;
        count = (int) Math.max(1, Math.ceil(count * Math.pow((100 - level) / 100d, 3))) * hungerValue;
        
        if(level < 30){
		    message = new TranslationTextComponent("msg.spaceacess.deny.info");
		    owner.sendMessage(message);
		    return false;
        }
        else {
        	WormHoleSpawnHandler.portalSpawnTick(owner.world);
	        message = new TranslationTextComponent("msg.spaceacess.accept.info");
	        mob.setHungerTime(mob.getHungerTime() + count);
	        return true;
        }
    }

	@Override
    public String getMoveName()
    {
        return "hiddenpower";
    }
}