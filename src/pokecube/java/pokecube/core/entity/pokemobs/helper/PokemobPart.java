package pokecube.core.entity.pokemobs.helper;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import pokecube.api.entity.pokemob.IPokemob;
import thut.api.entity.multipart.BBPartEntity;
import thut.core.client.render.bbmodel.BBModel;
import thut.core.client.render.model.parts.Part;

public class PokemobPart extends BBPartEntity<PokemobHasParts>
{
    public final IPokemob pokemob;

    public PokemobPart(final PokemobHasParts base, Part part, BBModel model)
    {
        super(base, part, model);
        this.pokemob = base.getPokemob();
    }

    @Override
    public InteractionResult interactAt(final Player player, final Vec3 vec, final InteractionHand hand)
    {
        if (this.pokemob.getTrackedEntity() == player) return InteractionResult.FAIL;
        return super.interactAt(player, vec, hand);
    }

    @Override
    public InteractionResult interact(final Player player, final InteractionHand hand)
    {
        if (this.pokemob.getTrackedEntity() == player) return InteractionResult.FAIL;
        return super.interact(player, hand);
    }
}
