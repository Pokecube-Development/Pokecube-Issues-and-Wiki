package pokecube.legends.blocks.customblocks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import pokecube.legends.Reference;
import pokecube.legends.blocks.BlockBase;
import thut.api.item.ItemList;

public class CramomaticBlock extends Rotates implements SimpleWaterloggedBlock
{

	  private static final Map<Direction, VoxelShape> CRAMOBOT  = new HashMap<>();
    private static final DirectionProperty          FACING      = HorizontalDirectionalBlock.FACING;
    private static final BooleanProperty            WATERLOGGED = BlockStateProperties.WATERLOGGED;

    // Tags
    public static ResourceLocation CRAMOMATIC_FUEL = new ResourceLocation(Reference.ID, "crambot_fuel");
    String infoName;

    @Override
    public BlockBase setToolTip(final String infoName)
    {
        this.infoName = infoName;
        return this;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(final ItemStack stack, final BlockGetter worldIn, final List<Component> tooltip,
            final TooltipFlag flagIn)
    {
        String message;
        if (Screen.hasShiftDown()) message = I18n.get("pokecube_legends." + this.infoName +".tooltip", ChatFormatting.GOLD, ChatFormatting.RESET);
        else message = I18n.get("pokecube.tooltip.advanced");
        tooltip.add(new TranslatableComponent(message));
    }

    // Precise selection box
    static
    {
    	CramomaticBlock.CRAMOBOT.put(Direction.NORTH, Shapes.or(
			Block.box(5, 0, 3, 11, 6, 9),
			Block.box(4, 5, 4, 5, 6, 8),
			Block.box(11, 5, 4, 12, 6, 8),
			Block.box(4, 6, 5.5, 5, 12, 6.5),
			Block.box(11, 6, 5.5, 12, 12, 6.5),
			Block.box(4, 11, 6.5, 5, 12, 12.5),
			Block.box(4, 10, 11.5, 5, 11, 12.5),
			Block.box(11, 8, 11.5, 12, 11, 12.5),
			Block.box(11, 11, 6.5, 12, 12, 12.5),
			Block.box(7, 7, 5, 9, 12, 7),
			Block.box(7, 6, 4, 9, 7, 8),
			Block.box(6, 12, 4, 10, 16, 8),
			Block.box(6, 12, 1, 10, 13, 4),
			Block.box(3, 0, 9, 13, 8, 15),
			Block.box(3, 8, 10, 6, 10, 14),
			Block.box(6, 8, 12, 11, 10, 12.01)).optimize());
		CramomaticBlock.CRAMOBOT.put(Direction.EAST, Shapes.or(
			Block.box(7, 0, 5, 13, 6, 11),
			Block.box(8, 5, 4, 12, 6, 5),
			Block.box(8, 5, 11, 12, 6, 12),
			Block.box(9.5, 6, 4, 10.5, 12, 5),
			Block.box(9.5, 6, 11, 10.5, 12, 12),
			Block.box(3.5, 11, 4, 9.5, 12, 5),
			Block.box(3.5, 10, 4, 4.5, 11, 5),
			Block.box(3.5, 8, 11, 4.5, 11, 12),
			Block.box(3.5, 11, 11, 9.5, 12, 12),
			Block.box(9, 7, 7, 11, 12, 9),
			Block.box(8, 6, 7, 12, 7, 9),
			Block.box(8, 12, 6, 12, 16, 10),
			Block.box(12, 12, 6, 15, 13, 10),
			Block.box(1, 0, 3, 7, 8, 13),
			Block.box(2, 8, 3, 6, 10, 6),
			Block.box(4, 8, 6, 4.01, 10, 11)).optimize());
		CramomaticBlock.CRAMOBOT.put(Direction.SOUTH, Shapes.or(
			Block.box(5, 0, 7, 11, 6, 13),
			Block.box(11, 5, 8, 12, 6, 12),
			Block.box(4, 5, 8, 5, 6, 12),
			Block.box(11, 6, 9.5, 12, 12, 10.5),
			Block.box(4, 6, 9.5, 5, 12, 10.5),
			Block.box(11, 11, 3.5, 12, 12, 9.5),
			Block.box(11, 10, 3.5, 12, 11, 4.5),
			Block.box(4, 8, 3.5, 5, 11, 4.5),
			Block.box(4, 11, 3.5, 5, 12, 9.5),
			Block.box(7, 7, 9, 9, 12, 11),
			Block.box(7, 6, 8, 9, 7, 12),
			Block.box(6, 12, 8, 10, 16, 12),
			Block.box(6, 12, 12, 10, 13, 15),
			Block.box(3, 0, 1, 13, 8, 7),
			Block.box(10, 8, 2, 13, 10, 6),
			Block.box(5, 8, 4, 10, 10, 4.01)).optimize());
		CramomaticBlock.CRAMOBOT.put(Direction.WEST, Shapes.or(
			Block.box(3, 0, 5, 9, 6, 11),
			Block.box(4, 5, 11, 8, 6, 12),
			Block.box(4, 5, 4, 8, 6, 5),
			Block.box(5.5, 6, 11, 6.5, 12, 12),
			Block.box(5.5, 6, 4, 6.5, 12, 5),
			Block.box(6.5, 11, 11, 12.5, 12, 12),
			Block.box(11.5, 10, 11, 12.5, 11, 12),
			Block.box(11.5, 8, 4, 12.5, 11, 5),
			Block.box(6.5, 11, 4, 12.5, 12, 5),
			Block.box(5, 7, 7, 7, 12, 9),
			Block.box(4, 6, 7, 8, 7, 9),
			Block.box(4, 12, 6, 8, 16, 10),
			Block.box(1, 12, 6, 4, 13, 10),
			Block.box(9, 0, 3, 15, 8, 13),
			Block.box(10, 8, 10, 14, 10, 13),
			Block.box(12, 8, 5, 12.01, 10, 10)).optimize());
    }

	  @Override
    public VoxelShape getShape(final BlockState state, final BlockGetter worldIn, final BlockPos pos,
            final CollisionContext context)
    {
		    return CramomaticBlock.CRAMOBOT.get(state.getValue(CramomaticBlock.FACING));
    }

	  public CramomaticBlock(final Properties props)
    {
        super(props);
        this.registerDefaultState(this.stateDefinition.any().setValue(CramomaticBlock.FACING, Direction.NORTH).setValue(
            CramomaticBlock.WATERLOGGED, false));
    }

	  @Override
	  public InteractionResult use(BlockState state, Level world, BlockPos pos, Player entity, InteractionHand hand,
	      BlockHitResult hit) {
  	    int x = pos.getX();
  	    int y = pos.getY();
  	    int z = pos.getZ();
  
  	    if (ItemList.is(CramomaticBlock.CRAMOMATIC_FUEL, entity.getMainHandItem()))
  	    {
  	      addParticles(entity,world,x,y,z);
  	      return InteractionResult.SUCCESS;
  	    }
  	    else if (!ItemList.is(CramomaticBlock.CRAMOMATIC_FUEL, entity.getMainHandItem()))
  	    {
  	      entity.displayClientMessage(new TranslatableComponent("msg.pokecube_legends.cramomatic.fail"), true);
  	      return InteractionResult.PASS;
  	    }
  	    return InteractionResult.PASS;
	  }

	  public static void addParticles(Player entity, Level world, int x, int y, int z) {
  	    if (world.isClientSide) {
  	      world.addParticle(ParticleTypes.TOTEM_OF_UNDYING, x + 0.5, y + 1, z + 0.5, 0, 1, 0);
  	    }
  	    world.playLocalSound(x, y, z, Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(
  	      "entity.player.levelup"))), SoundSource.NEUTRAL, 1, 1, false);
	  }
}