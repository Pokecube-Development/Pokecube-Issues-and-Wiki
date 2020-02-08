package pokecube.legends.worldgen.dimension;

import java.util.UUID;
import java.util.function.BiFunction;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.ModDimension;
import thut.api.entity.ThutTeleporter;
import thut.api.maths.Vector4;

public class UltraSpaceModDimension extends ModDimension {

	@Override
	public BiFunction<World, DimensionType, ? extends Dimension> getFactory() { return UltraSpaceConfig.UltraSpaceDimension::new; }
	
	public static void sendToBase(final ServerPlayerEntity player, final UUID baseOwner)
    {
        final DimensionType targetDim = ModDimensions.DIMENSION_TYPE;
        final BlockPos pos = ModDimensions.getSecretBaseLoc(baseOwner, player.getServer(), targetDim);
        final Vector4 dest = new Vector4(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, targetDim.getId());
        ThutTeleporter.transferTo(player, dest, true);
        player.sendMessage(new TranslationTextComponent("pokecube.secretbase.enter"));
    }

    public static void sendToExit(final ServerPlayerEntity player, final UUID baseOwner)
    {
        final DimensionType targetDim = DimensionType.OVERWORLD;
        final BlockPos pos = ModDimensions.getSecretBaseLoc(baseOwner, player.getServer(), targetDim);
        final Vector4 dest = new Vector4(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, targetDim.getId());
        ThutTeleporter.transferTo(player, dest, true);
        player.sendMessage(new TranslationTextComponent("pokecube.secretbase.exit"));
    }
}
