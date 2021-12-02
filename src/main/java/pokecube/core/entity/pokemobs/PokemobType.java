package pokecube.core.entity.pokemobs;

import com.google.common.collect.ImmutableSet;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PlayMessages.SpawnEntity;
import pokecube.core.database.PokedexEntry;
import pokecube.core.utils.PokeType;

public class PokemobType<T extends TamableAnimal> extends EntityType<T>
{
    final PokedexEntry       entry;

    public PokemobType(final EntityType.EntityFactory<T> factory, final PokedexEntry entry)
    {
        super(factory, MobCategory.CREATURE, true, true, false, true, ImmutableSet.of(), EntityDimensions.scalable(entry.width, entry.height), 64, 3);
        this.entry = entry;
        entry.setEntityType(this);
    }

    @Override
    public T customClientSpawn(final SpawnEntity packet, final Level world)
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
