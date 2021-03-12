package pokecube.core.entity.pokemobs;

import com.google.common.collect.ImmutableSet;

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

    public PokemobType(final EntityType.IFactory<T> factory, final PokedexEntry entry)
    {
        super(factory, EntityClassification.CREATURE, true, true, false, true, ImmutableSet.of(), EntitySize.scalable(entry.width, entry.height), 64, 3);
        this.entry = entry;
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
    public ResourceLocation getDefaultLootTable()
    {
        if (this.entry.lootTable != null) return this.entry.lootTable;
        return super.getDefaultLootTable();
    }

    @Override
    public boolean fireImmune()
    {
        return this.entry.isType(PokeType.getType("fire")) || this.entry.isHeatProof;
    }
}
