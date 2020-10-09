
package net.mcreator.moddymcmodface.block;

import net.minecraftforge.registries.ObjectHolder;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.World;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IBlockReader;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.Rotation;
import net.minecraft.util.Mirror;
import net.minecraft.util.Hand;
import net.minecraft.util.Direction;
import net.minecraft.util.ActionResultType;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.StateContainer;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.BlockItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.block.material.Material;
import net.minecraft.block.SoundType;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.Block;

import net.mcreator.moddymcmodface.ModdymcmodfaceModElements;

import java.util.List;
import java.util.Collections;

@ModdymcmodfaceModElements.ModElement.Tag
public class CrankBlock extends ModdymcmodfaceModElements.ModElement {
	@ObjectHolder("moddymcmodface:crank")
	public static final Block block = null;
	public CrankBlock(ModdymcmodfaceModElements instance) {
		super(instance, 148);
	}

	@Override
	public void initElements() {
		elements.blocks.add(() -> new CustomBlock());
		elements.items.add(() -> new BlockItem(block, new Item.Properties().group(ItemGroup.REDSTONE)).setRegistryName(block.getRegistryName()));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void clientLoad(FMLClientSetupEvent event) {
		RenderTypeLookup.setRenderLayer(block, RenderType.getCutout());
	}
	public static class CustomBlock extends Block {
		public static final DirectionProperty FACING = DirectionalBlock.FACING;
		public static final IntegerProperty POWER = BlockStateProperties.POWER_0_15;
		public CustomBlock() {
			super(Block.Properties.create(Material.WOOD).sound(SoundType.STONE).hardnessAndResistance(0.6f, 0.6f).lightValue(0).doesNotBlockMovement()
					.notSolid());
			this.setDefaultState(this.stateContainer.getBaseState().with(POWER, 0).with(FACING, Direction.NORTH));
			setRegistryName("crank");
		}

		@Override
		public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos,
				BlockPos facingPos) {
			return facing.getOpposite() == stateIn.get(FACING) && !stateIn.isValidPosition(worldIn, currentPos)
					? Blocks.AIR.getDefaultState()
					: stateIn;
		}

		public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
			Direction direction = state.get(FACING);
			BlockPos blockpos = pos.offset(direction.getOpposite());
			BlockState blockstate = worldIn.getBlockState(blockpos);
			if (direction == Direction.UP || direction == Direction.DOWN) {
				return hasEnoughSolidSide(worldIn, blockpos, direction);
			} else {
				return blockstate.isSolidSide(worldIn, blockpos, direction);
			}
		}

		@Override
		public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
				BlockRayTraceResult hit) {
			if (worldIn.isRemote) {
				Direction direction = state.get(FACING).getOpposite();
				// Direction direction1 = getFacing(state).getOpposite();
				double d0 = (double) pos.getX() + 0.5D + 0.1D * (double) direction.getXOffset() + 0.2D * (double) direction.getXOffset();
				double d1 = (double) pos.getY() + 0.5D + 0.1D * (double) direction.getYOffset() + 0.2D * (double) direction.getYOffset();
				double d2 = (double) pos.getZ() + 0.5D + 0.1D * (double) direction.getZOffset() + 0.2D * (double) direction.getZOffset();
				worldIn.addParticle(ParticleTypes.SMOKE, d0, d1, d2, 0, 0, 0);
				return ActionResultType.SUCCESS;
			} else {
				BlockState blockstate = this.activate(state, worldIn, pos);
				float f = 0.4f;
				worldIn.playSound((PlayerEntity) null, pos, SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.BLOCKS, 0.3F, f);
				return ActionResultType.SUCCESS;
			}
		}

		public BlockState activate(BlockState state, World world, BlockPos pos) {
			state = state.cycle(POWER);
			world.setBlockState(pos, state, 3);
			this.updateNeighbors(state, world, pos);
			return state;
		}

		public int getWeakPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
			return blockState.get(POWER);
		}

		@Override
		public int getStrongPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
			return blockState.get(FACING) == side ? blockState.get(POWER) : 0;
		}

		@Override
		public boolean canProvidePower(BlockState state) {
			return true;
		}

		private void updateNeighbors(BlockState state, World world, BlockPos pos) {
			world.notifyNeighborsOfStateChange(pos, this);
			world.notifyNeighborsOfStateChange(pos.offset(state.get(FACING).getOpposite()), this);
		}

		@Override
		public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
			if (!isMoving && state.getBlock() != newState.getBlock()) {
				if (state.get(POWER) != 0) {
					this.updateNeighbors(state, worldIn, pos);
				}
				super.onReplaced(state, worldIn, pos, newState, isMoving);
			}
		}

		@Override
		public boolean isNormalCube(BlockState state, IBlockReader worldIn, BlockPos pos) {
			return false;
		}

		@Override
		public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos) {
			return true;
		}

		@Override
		public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
			switch ((Direction) state.get(FACING)) {
				case SOUTH :
				default :
					return VoxelShapes.create(0.875D, 0.125D, 0.3125D, 0.125D, 0.875D, 0D);
				case NORTH :
					return VoxelShapes.create(0.125D, 0.125D, 0.6875D, 0.875D, 0.875D, 1D);
				case WEST :
					return VoxelShapes.create(0.6875D, 0.125D, 0.875D, 1D, 0.875D, 0.125D);
				case EAST :
					return VoxelShapes.create(0.3125D, 0.125D, 0.125D, 0D, 0.875D, 0.875D);
				case UP :
					return VoxelShapes.create(0.125D, 0.3125D, 0.125D, 0.875D, 0D, 0.875D);
				case DOWN :
					return VoxelShapes.create(0.125D, 0.6875D, 0.875D, 0.875D, 1D, 0.125D);
			}
		}

		@Override
		protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
			builder.add(FACING, POWER);
		}

		public BlockState rotate(BlockState state, Rotation rot) {
			return state.with(FACING, rot.rotate(state.get(FACING)));
		}

		public BlockState mirror(BlockState state, Mirror mirrorIn) {
			return state.rotate(mirrorIn.toRotation(state.get(FACING)));
		}

		@Override
		public BlockState getStateForPlacement(BlockItemUseContext context) {
			return this.getDefaultState().with(FACING, context.getFace());
		}

		@Override
		public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
			List<ItemStack> dropsOriginal = super.getDrops(state, builder);
			if (!dropsOriginal.isEmpty())
				return dropsOriginal;
			return Collections.singletonList(new ItemStack(this, 1));
		}
	}
}
