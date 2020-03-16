package pokecube.core.entity.pokemobs;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.FMLPlayMessages.SpawnEntity;
import pokecube.core.database.PokedexEntry;
import pokecube.core.utils.PokeType;

public class PokemobType<T extends TameableEntity> extends EntityType<T>
{
    final PokedexEntry       entry;
    private final EntitySize baseSize;

    public PokemobType(final EntityType.IFactory<T> factory, final PokedexEntry entry)
    {
        super(factory, EntityClassification.CREATURE, true, true, false, true, null, c -> true, c -> 64, c -> 3, null);
        this.entry = entry;
        this.baseSize = EntitySize.flexible(entry.width, entry.height);
        entry.setEntityType(this);
    }

    @Override
    public T customClientSpawn(final SpawnEntity packet, final World world)
    {
        return this.create(world);
    }

    public PokedexEntry getEntry()
    {
        return this.entry;
    }

    @Override
    public ResourceLocation getLootTable()
    {
        if (this.entry.lootTable != null) return this.entry.lootTable;
        return super.getLootTable();
    }

    @Override
    public EntitySize getSize()
    {
        return this.baseSize;
    }

    @Override
    public boolean isImmuneToFire()
    {
        return this.entry.isType(PokeType.getType("fire"));
    }
}
