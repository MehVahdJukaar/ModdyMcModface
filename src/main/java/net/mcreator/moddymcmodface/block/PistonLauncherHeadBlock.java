
package net.mcreator.moddymcmodface.block;

import net.minecraftforge.registries.ObjectHolder;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.common.ToolType;
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
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Rotation;
import net.minecraft.util.Mirror;
import net.minecraft.util.Direction;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.StateContainer;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.BooleanProperty;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.BlockItem;
import net.minecraft.fluid.IFluidState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.block.material.PushReaction;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.SoundType;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.Block;

import net.mcreator.moddymcmodface.ModdymcmodfaceModElements;

import java.util.List;
import java.util.Collections;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

@ModdymcmodfaceModElements.ModElement.Tag
public class PistonLauncherHeadBlock extends ModdymcmodfaceModElements.ModElement {
	@ObjectHolder("moddymcmodface:piston_launcher_head")
	public static final Block block = null;
	public PistonLauncherHeadBlock(ModdymcmodfaceModElements instance) {
		super(instance, 42);
	}

	@Override
	public void initElements() {
		elements.blocks.add(() -> new CustomBlock());
		elements.items.add(() -> new BlockItem(block, new Item.Properties().group(null)).setRegistryName(block.getRegistryName()));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void clientLoad(FMLClientSetupEvent event) {
		RenderTypeLookup.setRenderLayer(block, RenderType.getCutout());
	}
	public static class CustomBlock extends DirectionalBlock {
		public static final DirectionProperty FACING = DirectionalBlock.FACING;
		public static final BooleanProperty EXTENDED = BlockStateProperties.EXTENDED; // is not small? (only used for
																						// tile entity, leave true
		public CustomBlock() {
			super(Block.Properties.create(Material.IRON).sound(SoundType.METAL).hardnessAndResistance(1f, 20f).lightValue(0).harvestLevel(1)
					.harvestTool(ToolType.PICKAXE));
			this.setDefaultState(this.stateContainer.getBaseState().with(EXTENDED, true).with(FACING, Direction.NORTH));
			setRegistryName("piston_launcher_head");
		}

		@Override
		public boolean isNormalCube(BlockState state, IBlockReader worldIn, BlockPos pos) {
			return false;
		}

		@Override
		public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos) {
			return true;
		}

		public boolean isTransparent(BlockState state) {
			return true;
		}

		@Override
		public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
			switch ((Direction) state.get(FACING)) {
				default :
				case NORTH :
					return VoxelShapes.or(VoxelShapes.create(0.9375D, 0.0625D, 1.1875D, 0.0625D, 0.938D, 0.125D),
							VoxelShapes.create(0D, 0D, 0D, 1D, 1D, 0.125D));
				case SOUTH :
					return VoxelShapes.or(VoxelShapes.create(0.0625D, 0.0625D, -0.1875D, 0.938D, 0.938D, 0.875D),
							VoxelShapes.create(1D, 0D, 1D, 0D, 1D, 0.875D));
				case EAST :
					return VoxelShapes.or(VoxelShapes.create(-0.1875D, 0.0625D, 0.9375D, 0.875D, 0.938D, 0.0625D),
							VoxelShapes.create(1D, 0D, 0D, 0.875D, 1D, 1D));
				case WEST :
					return VoxelShapes.or(VoxelShapes.create(1.1875D, 0.0625D, 0.0625D, 0.125D, 0.938D, 0.938D),
							VoxelShapes.create(0D, 0D, 1D, 0.125D, 1D, 0D));
				case DOWN :
					return VoxelShapes.or(VoxelShapes.create(0.0625D, 1.1875D, 0.0625D, 0.938D, 0.125D, 0.938D),
							VoxelShapes.create(0D, 0D, 1D, 1D, 0.125D, 0D));
				case UP :
					return VoxelShapes.or(VoxelShapes.create(0.0625D, -0.1875D, 0.9375D, 0.938D, 0.875D, 0.0625D),
							VoxelShapes.create(0D, 1D, 0D, 1D, 0.875D, 1D));
			}
		}

		@Override
		protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
			builder.add(FACING);
			builder.add(EXTENDED);
		}

		public BlockState rotate(BlockState state, Rotation rot) {
			return state.with(FACING, rot.rotate(state.get(FACING)));
		}

		public BlockState mirror(BlockState state, Mirror mirrorIn) {
			return state.rotate(mirrorIn.toRotation(state.get(FACING)));
		}

		@Override
		public BlockState getStateForPlacement(BlockItemUseContext context) {
			return this.getDefaultState().with(FACING, context.getNearestLookingDirection().getOpposite());
		}

		@Override
		public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
			return new ItemStack(PistonLauncherBlock.block, (int) (1));
		}

		@Override
		public MaterialColor getMaterialColor(BlockState state, IBlockReader blockAccess, BlockPos pos) {
			return MaterialColor.IRON;
		}

		@Override
		public PushReaction getPushReaction(BlockState state) {
			return PushReaction.BLOCK;
		}

		@Override
		public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
			List<ItemStack> dropsOriginal = super.getDrops(state, builder);
			if (!dropsOriginal.isEmpty())
				return dropsOriginal;
			return Collections.singletonList(new ItemStack(PistonLauncherBlock.block, (int) (0)));
		}


		// piston code



	   /**
	    * Called before the Block is set to air in the world. Called regardless of if the player's tool can actually collect
	    * this block
	    */
	   public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
	      if (!worldIn.isRemote && player.abilities.isCreativeMode) {
	         BlockPos blockpos = pos.offset(state.get(FACING).getOpposite());
	         Block block = worldIn.getBlockState(blockpos).getBlock();

	         if (block == PistonLauncherBlock.block) {
	            worldIn.removeBlock(blockpos, false);
	         }
	      }
	
	      super.onBlockHarvested(worldIn, pos, state, player);
	   }
	
	   public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
	     	BlockState comp = PistonLauncherArmTileBlock.block.getDefaultState().with(EXTENDED, false).with(FACING, state.get(FACING));
	      if ((state.getBlock() != newState.getBlock()) &&
			 (newState != comp)) {
	         super.onReplaced(state, worldIn, pos, newState, isMoving);
	         Direction direction = state.get(FACING).getOpposite();
	         pos = pos.offset(direction);
	         BlockState blockstate = worldIn.getBlockState(pos);
	         if ((blockstate.getBlock() == PistonLauncherBlock.block ) && blockstate.get(BlockStateProperties.EXTENDED)) {
	            spawnDrops(blockstate, worldIn, pos);
	            worldIn.removeBlock(pos, false);
	         }
	
	      }
	   }


		
		/**
		 * Update the provided state given the provided neighbor facing and neighbor
		 * state, returning a new state. For example, fences make their connections to
		 * the passed in state if possible, and wet concrete powder immediately returns
		 * its solidified counterpart. Note that this method should ideally consider
		 * only the specific face passed in.
		 */
		public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos,
				BlockPos facingPos) {
			return facing.getOpposite() == stateIn.get(FACING) && !stateIn.isValidPosition(worldIn, currentPos)
					? Blocks.AIR.getDefaultState()
					: super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
		}

		public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
			BlockState bs = worldIn.getBlockState(pos.offset(state.get(FACING).getOpposite()));
			if(bs == PistonLauncherBlock.block.getDefaultState().with(EXTENDED, true).with(FACING, state.get(FACING))){
				return true;
			}
			return false;
			
			//return bs == PistonLauncherBlock.block || block == PistonLauncherArmTileBlock.block;
		}

		public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
			if (state.isValidPosition(worldIn, pos)) {
				BlockPos blockpos = pos.offset(state.get(FACING).getOpposite());
				worldIn.getBlockState(blockpos).neighborChanged(worldIn, blockpos, blockIn, fromPos, false);
			}
			
		}
	}
}
