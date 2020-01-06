package pokecube.core.ai.logic;

import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.moves.TerrainDamageSource;
import pokecube.core.moves.TerrainDamageSource.TerrainType;
import thut.api.maths.Vector3;

/**
 * Manages interactions with materials for the pokemob. This is what is used to
 * make some mobs despawn in high light, or take damage from certain
 * materials.
 */
public class LogicInMaterials extends LogicBase
{
    Vector3 v = Vector3.getNewVector();

    public LogicInMaterials(IPokemob entity)
    {
        super(entity);
    }

    @Override
    public void tick(World world)
    {
        super.tick(world);
        if (this.pokemob.getPokedexEntry().hatedMaterial != null)
        {
            final String material = this.pokemob.getPokedexEntry().hatedMaterial[0];
            if (material.equalsIgnoreCase("light"))
            {
                float value = 0.5f;
                if (this.entity.getEntityWorld().isDaytime() && !this.entity.getEntityWorld().isRemote && !this.pokemob
                        .getGeneralState(GeneralStates.TAMED))
                {

                    value = Float.parseFloat(this.pokemob.getPokedexEntry().hatedMaterial[1]);
                    final String action = this.pokemob.getPokedexEntry().hatedMaterial[2];
                    final float f = this.entity.getBrightness();
                    if (f > value && this.entity.getEntityWorld().canBlockSeeSky(this.entity.getPosition())) if (action
                            .equalsIgnoreCase("despawn")) this.entity.remove();
                    else if (action.equalsIgnoreCase("hurt") && Math.random() < 0.1) this.entity.attackEntityFrom(
                            DamageSource.ON_FIRE, 1);
                }
            }
            else if (material.equalsIgnoreCase("water")) if (this.entity.isInWater() && this.entity.getRNG().nextInt(
                    10) == 0) this.entity.attackEntityFrom(new TerrainDamageSource("material", TerrainType.MATERIAL),
                            1);
        }
    }
}
