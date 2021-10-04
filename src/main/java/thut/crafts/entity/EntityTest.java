package thut.crafts.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;

public class EntityTest extends Mob
{
    public static final EntityType<EntityTest> TYPE = EntityType.Builder
            .of(EntityTest::new, MobCategory.CREATURE)
            .setCustomClientFactory((s, w) -> EntityTest.TYPE.create(w)).build("thutcrafts:testmob");

    protected EntityTest(EntityType<? extends Mob> type, Level worldIn)
    {
        super(type, worldIn);
    }

}
