
package net.mcreator.moddymcmodface.block;

import net.minecraftforge.registries.ObjectHolder;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.World;
import net.minecraft.world.IBlockReader;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.Rotation;
import net.minecraft.util.Mirror;
import net.minecraft.util.Direction;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.StateContainer;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.BooleanProperty;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.BlockItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.block.material.PushReaction;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.SoundType;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Block;

import net.mcreator.moddymcmodface.ModdymcmodfaceModElements;
import net.mcreator.moddymcmodface.CommonUtil;

import java.util.List;
import java.util.Collections;

@ModdymcmodfaceModElements.ModElement.Tag
public class PistonLauncherBlock extends ModdymcmodfaceModElements.ModElement {
	@ObjectHolder("moddymcmodface:piston_launcher")
	public static final Block block = null;
	public PistonLauncherBlock(ModdymcmodfaceModElements instance) {
		super(instance, 34);
	}

	@Override
	public void initElements() {
		elements.blocks.add(() -> new CustomBlock());
		elements.items.add(() -> new BlockItem(block, new Item.Properties().group(ItemGroup.REDSTONE)).setRegistryName(block.getRegistryName()));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void clientLoad(FMLClientSetupEvent event) {
		RenderTypeLookup.setRenderLayer(block, RenderType.getCutoutMipped());
	}
	public static class CustomBlock extends Block {
		public static final DirectionProperty FACING = DirectionalBlock.FACING;
		public static final BooleanProperty EXTENDED = BlockStateProperties.EXTENDED; // is base only?
		public CustomBlock() {
			super(Block.Properties.create(Material.IRON).sound(SoundType.METAL).hardnessAndResistance(1f, 20f).lightValue(0).harvestLevel(1)
					.harvestTool(ToolType.PICKAXE));
			this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.NORTH).with(EXTENDED, false));
			setRegistryName("piston_launcher");
		}

		@Override
		public boolean isNormalCube(BlockState state, IBlockReader worldIn, BlockPos pos) {
			return false;
		}

		public boolean isTransparent(BlockState state) {
			return true;
		}

		@Override
		public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos) {
			return true;
		}

		@Override
		protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
			builder.add(FACING, EXTENDED);
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
		public MaterialColor getMaterialColor(BlockState state, IBlockReader blockAccess, BlockPos pos) {
			return MaterialColor.IRON;
		}

		@Override
		public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
			if (state.get(EXTENDED)) {
				switch ((Direction) state.get(FACING)) {
					case SOUTH :
					default :
						return VoxelShapes.create(1D, 0D, 0.812D, 0D, 1D, 0D);
					case NORTH :
						return VoxelShapes.create(0D, 0D, 0.188D, 1D, 1D, 1D);
					case WEST :
						return VoxelShapes.create(0.188D, 0D, 1D, 1D, 1D, 0D);
					case EAST :
						return VoxelShapes.create(0.812D, 0D, 0D, 0D, 1D, 1D);
					case UP :
						return VoxelShapes.create(0D, 0.812D, 0D, 1D, 0D, 1D);
					case DOWN :
						return VoxelShapes.create(0D, 0.188D, 1D, 1D, 1D, 0D);
				}
			} else {
				switch ((Direction) state.get(FACING)) {
					case SOUTH :
					default :
						return VoxelShapes.or(VoxelShapes.create(1D, 0D, 0.812D, 0D, 1D, 0D), VoxelShapes.create(1D, 0D, 1D, 0D, 1D, 0.875D),
								VoxelShapes.create(0.9375D, 0.062D, 0.8125D, 0.0625D, 0.9375D, 0.875D));
					case NORTH :
						return VoxelShapes.or(VoxelShapes.create(0D, 0D, 0.188D, 1D, 1D, 1D), VoxelShapes.create(0D, 0D, 0D, 1D, 1D, 0.125D),
								VoxelShapes.create(0.0625D, 0.062D, 0.1875D, 0.9375D, 0.9375D, 0.125D));
					case WEST :
						return VoxelShapes.or(VoxelShapes.create(0.188D, 0D, 1D, 1D, 1D, 0D), VoxelShapes.create(0D, 0D, 1D, 0.125D, 1D, 0D),
								VoxelShapes.create(0.1875D, 0.062D, 0.9375D, 0.125D, 0.9375D, 0.0625D));
					case EAST :
						return VoxelShapes.or(VoxelShapes.create(0.812D, 0D, 0D, 0D, 1D, 1D), VoxelShapes.create(1D, 0D, 0D, 0.875D, 1D, 1D),
								VoxelShapes.create(0.8125D, 0.062D, 0.0625D, 0.875D, 0.9375D, 0.9375D));
					case UP :
						return VoxelShapes.or(VoxelShapes.create(0D, 0.812D, 0D, 1D, 0D, 1D), VoxelShapes.create(0D, 1D, 0D, 1D, 0.875D, 1D),
								VoxelShapes.create(0.0625D, 0.8125D, 0.062D, 0.9375D, 0.875D, 0.9375D));
					case DOWN :
						return VoxelShapes.or(VoxelShapes.create(0D, 0.188D, 1D, 1D, 1D, 0D), VoxelShapes.create(0D, 0D, 1D, 1D, 0.125D, 0D),
								VoxelShapes.create(0.0625D, 0.1875D, 0.938D, 0.9375D, 0.125D, 0.0625D));
				}
				// return VoxelShapes.create(0D, 0D, 0D, 1D, 0.5D, 1D);
			}
		}

		@Override
		public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
			List<ItemStack> dropsOriginal = super.getDrops(state, builder);
			if (!dropsOriginal.isEmpty())
				return dropsOriginal;
			return Collections.singletonList(new ItemStack(this, 1));
		}

		@Override
		public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
			this.checkForMove(state, worldIn, pos);
		}

		public void checkForMove(BlockState state, World world, BlockPos pos) {
			if (!world.isRemote()) {
				boolean flag = this.shouldBeExtended(world, pos, state.get(FACING));
				BlockPos _bp = pos.add(state.get(FACING).getDirectionVec());
				if (flag && !state.get(EXTENDED)) {
					boolean flag2 = false;
					BlockState targetblock = world.getBlockState(_bp);
					if (targetblock.getPushReaction() == PushReaction.DESTROY || targetblock.isAir()) {
						TileEntity tileentity = targetblock.hasTileEntity() ? world.getTileEntity(_bp) : null;
						spawnDrops(targetblock, world, _bp, tileentity);
						flag2 = true;
					}
					/*
					 * else if (targetblock.getBlock() instanceof FallingBlock &&
					 * world.getBlockState(_bp.add(state.get(FACING).getDirectionVec())).isAir(
					 * world, _bp)){ FallingBlockEntity fallingblockentity = new
					 * FallingBlockEntity(world, (double)_bp.getX() + 0.5D, (double)_bp.getY() ,
					 * (double)_bp.getZ() + 0.5D, world.getBlockState(_bp));
					 * 
					 * world.addEntity(fallingblockentity); flag2=true; }
					 */
					if (flag2) {
						world.setBlockState(_bp,
								PistonLauncherArmTileBlock.block.getDefaultState().with(CommonUtil.EXTENDING, true).with(FACING, state.get(FACING)),3);
						world.setBlockState(pos, state.with(EXTENDED, true));
						world.playSound((PlayerEntity) null, pos, SoundEvents.BLOCK_PISTON_EXTEND, SoundCategory.BLOCKS, 0.53F, world.rand.nextFloat() * 0.25F + 0.45F);
					}
				} else if (!flag && state.get(EXTENDED)) {
					BlockState bs = world.getBlockState(_bp);
					if (bs.getBlock() == PistonLauncherHeadBlock.block.getDefaultState().getBlock() && state.get(FACING) == bs.get(FACING)) {
						// world.setBlockState(_bp, Blocks.AIR.getDefaultState(), 3);
						world.setBlockState(_bp,PistonLauncherArmTileBlock.block.getDefaultState().with(CommonUtil.EXTENDING, false).with(FACING, state.get(FACING)),3);
						world.playSound((PlayerEntity) null, pos, SoundEvents.BLOCK_PISTON_CONTRACT, SoundCategory.BLOCKS, 0.53F,
								world.rand.nextFloat() * 0.15F + 0.45F);
					} else if (bs.getBlock() == PistonLauncherArmTileBlock.block.getDefaultState().getBlock()
							&& state.get(FACING) == bs.get(FACING)) {
						if (world.getTileEntity(_bp) instanceof PistonLauncherArmTileBlock.CustomTileEntity) {
							world.getPendingBlockTicks().scheduleTick(pos, world.getBlockState(pos).getBlock(), 1);
						}
					}
				}
			}
		}

		// piston code
		private boolean shouldBeExtended(World worldIn, BlockPos pos, Direction facing) {
			for (Direction direction : Direction.values()) {
				if (direction != facing && worldIn.isSidePowered(pos.offset(direction), direction)) {
					return true;
				}
			}
			if (worldIn.isSidePowered(pos, Direction.DOWN)) {
				return true;
			} else {
				BlockPos blockpos = pos.up();
				for (Direction direction1 : Direction.values()) {
					if (direction1 != Direction.DOWN && worldIn.isSidePowered(blockpos.offset(direction1), direction1)) {
						return true;
					}
				}
				return false;
			}
		}

		@Override
		public void neighborChanged(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean moving) {
			super.neighborChanged(state, world, pos, neighborBlock, fromPos, moving);
			this.checkForMove(state, world, pos);
		}
	}
}
