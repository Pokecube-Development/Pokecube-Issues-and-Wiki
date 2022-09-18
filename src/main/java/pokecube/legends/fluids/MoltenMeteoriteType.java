package pokecube.legends.fluids;

import com.google.common.collect.ImmutableMap;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.LavaFluid;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.RegistryObject;
import pokecube.api.PokecubeAPI;
import pokecube.core.items.pokecubes.Pokecube;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.init.FluidInit;
import pokecube.legends.init.ItemInit;

public class MoltenMeteoriteType extends FluidType
{
    public static final ResourceLocation MOLTEN_METEORITE_STILL = new ResourceLocation("pokecube_legends:block/molten_meteorite");
    public static final ResourceLocation MOLTEN_METEORITE_FLOWING = new ResourceLocation("pokecube_legends:block/molten_meteorite_flowing");
    public static final ResourceLocation MOLTEN_METEORITE_OVERLAY = new ResourceLocation("pokecube_legends:block/molten_meteorite_overlay");

    public MoltenMeteoriteType(Properties properties) {
        super(properties);

        this.initClient();
    }

    public static ForgeFlowingFluid.Properties makeProperties()
    {
        return new ForgeFlowingFluid.Properties(MOLTEN_METEORITE_TYPE, FluidInit.MOLTEN_METEORITE,
                FluidInit.MOLTEN_METEORITE_FLOWING).bucket(ItemInit.MOLTEN_METEORITE_BUCKET)
                .block(FluidInit.MOLTEN_METEORITE_BLOCK);
    }


    private Object renderProperties;

    private void initClient()
    {
        // Minecraft instance isn't available in datagen, so don't call initializeClient if in datagen
        if (net.minecraftforge.fml.loading.FMLEnvironment.dist == net.minecraftforge.api.distmarker.Dist.CLIENT && !net.minecraftforge.fml.loading.FMLLoader.getLaunchHandler().isData())
        {
            initializeClient(properties ->
            {
                if (properties == this)
                    throw new IllegalStateException("Don't extend IFluidTypeRenderProperties in your fluid type, use an anonymous class instead.");
                this.renderProperties = properties;
            });
        }
    }

    public static final RegistryObject<FluidType> MOLTEN_METEORITE_TYPE = PokecubeLegends.FLUID_TYPES.register("molten_meteorite", () ->
            new FluidType(FluidType.Properties.create()
                    .descriptionId("block.pokecube_legends.molten_meteorite")
                    .pathType(BlockPathTypes.LAVA).rarity(Rarity.UNCOMMON)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL_LAVA)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY_LAVA).supportsBoating(true)
                    .density(6000).temperature(1300).viscosity(9000).lightLevel(10).fallDistanceModifier(0.6F).motionScale(0.0001D).adjacentPathType(null)
                    .canPushEntity(true).canSwim(false).canDrown(false).canExtinguish(false).canConvertToSource(false).canHydrate(false))
            {

                @Override
                public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer)
                {
                    consumer.accept(new IClientFluidTypeExtensions()
                    {
                        @Override
                        public ResourceLocation getStillTexture()
                        {
                            return MOLTEN_METEORITE_STILL;
                        }

                        @Override
                        public ResourceLocation getFlowingTexture()
                        {
                            return MOLTEN_METEORITE_FLOWING;
                        }

                        @Nullable
                        @Override
                        public ResourceLocation getOverlayTexture()
                        {
                            return MOLTEN_METEORITE_OVERLAY;
                        }
                    });
                }

                @Override
                public double motionScale(Entity entity)
                {
                    return entity.level.dimensionType().ultraWarm() ? 0.007D : 0.0023333333333333335D;
                }

                @Override
                public void setItemMovement(ItemEntity entity)
                {
                    Vec3 vec3 = entity.getDeltaMovement();
                    entity.setDeltaMovement(vec3.x * (double)0.95F, vec3.y + (double)(vec3.y < (double)0.06F ? 5.0E-4F : 0.0F), vec3.z * (double)0.95F);
                }
            });
}