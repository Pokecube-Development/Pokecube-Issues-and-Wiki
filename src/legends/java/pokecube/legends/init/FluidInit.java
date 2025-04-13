package pokecube.legends.init;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.fluids.DistorticWaterType;
import pokecube.legends.fluids.MoltenMeteoriteType;

import java.util.function.Supplier;

public class FluidInit
{
    public static Supplier<FlowingFluid> DISTORTIC_WATER;
    public static Supplier<FlowingFluid> DISTORTIC_WATER_FLOWING;
    public static Supplier<LiquidBlock> DISTORTIC_WATER_BLOCK;

    public static Supplier<FlowingFluid> MOLTEN_METEORITE;
    public static Supplier<FlowingFluid> MOLTEN_METEORITE_FLOWING;

    public static final Supplier<FluidType> DISTORTIC_WATER_TYPE;
    public static final Supplier<FluidType> MOLTEN_METEORITE_TYPE;

    static
    {

        DISTORTIC_WATER_FLOWING = PokecubeLegends.FLUIDS.register("distortic_water_flowing",
                () -> new BaseFlowingFluid.Flowing(DistorticWaterType.makeProperties()));

        DISTORTIC_WATER_BLOCK = PokecubeLegends.BLOCKS.register("distortic_water_block",
                () -> new LiquidBlock(DISTORTIC_WATER.get(),
                        BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE).noCollission()
                                .strength(100.0F).noLootTable()));

        DISTORTIC_WATER = PokecubeLegends.FLUIDS.register("distortic_water",
                () -> new BaseFlowingFluid.Source(DistorticWaterType.makeProperties()));
        MOLTEN_METEORITE = PokecubeLegends.FLUIDS.register("molten_meteorite",
                () -> new BaseFlowingFluid.Source(MoltenMeteoriteType.makeProperties()));
        MOLTEN_METEORITE_FLOWING = PokecubeLegends.FLUIDS.register("molten_meteorite_flowing",
                () -> new BaseFlowingFluid.Flowing(MoltenMeteoriteType.makeProperties()));

        DISTORTIC_WATER_TYPE = PokecubeLegends.FLUID_TYPES.register("distortic_water", () -> new FluidType(
                FluidType.Properties.create().descriptionId("block.pokecube_legends.distortic_water").density(1000)
                        .temperature(100).viscosity(1000).lightLevel(0).motionScale(1.5).supportsBoating(true)
                        .canDrown(true).canPushEntity(true).canExtinguish(true).canConvertToSource(true)
                        .canHydrate(true).canSwim(true).rarity(Rarity.RARE)));

        MOLTEN_METEORITE_TYPE = PokecubeLegends.FLUID_TYPES.register("molten_meteorite", () -> new FluidType(
                FluidType.Properties.create().descriptionId("block.pokecube_legends.molten_meteorite")
                        .pathType(PathType.LAVA).rarity(Rarity.UNCOMMON)
                        .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL_LAVA)
                        .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY_LAVA).supportsBoating(true)
                        .density(6000).temperature(1300).viscosity(9000).lightLevel(10).fallDistanceModifier(0.6F)
                        .motionScale(0.0001D).adjacentPathType(null).canPushEntity(true).canSwim(false).canDrown(false)
                        .canExtinguish(false).canConvertToSource(false).canHydrate(false))
        {
            @Override
            public double motionScale(Entity entity)
            {
                return entity.level.dimensionType().ultraWarm() ? 0.007D : 0.0023333333333333335D;
            }

            @Override
            public void setItemMovement(ItemEntity entity)
            {
                Vec3 vec3 = entity.getDeltaMovement();
                entity.setDeltaMovement(vec3.x * (double) 0.95F,
                        vec3.y + (double) (vec3.y < (double) 0.06F ? 5.0E-4F : 0.0F), vec3.z * (double) 0.95F);
            }
        });
    }

    public static void init()
    {}
}
