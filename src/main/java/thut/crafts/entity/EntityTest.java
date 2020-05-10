package thut.crafts.entity;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.world.World;

public class EntityTest extends MobEntity
{
    public static final EntityType<EntityTest> TYPE = EntityType.Builder
            .create(EntityTest::new, EntityClassification.CREATURE)
            .setCustomClientFactory((s, w) -> EntityTest.TYPE.create(w)).build("thutcrafts:testmob");

    protected EntityTest(EntityType<? extends MobEntity> type, World worldIn)
    {
        super(type, worldIn);
    }

}
