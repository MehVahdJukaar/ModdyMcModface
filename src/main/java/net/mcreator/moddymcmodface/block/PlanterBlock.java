
package net.mcreator.moddymcmodface.block;

import net.minecraftforge.registries.ObjectHolder;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.World;
import net.minecraft.world.IBlockReader;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Direction;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.StateContainer;
import net.minecraft.state.BooleanProperty;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.BlockItem;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.StemBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.CropsBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Block;
import net.minecraft.block.AirBlock;

import net.mcreator.moddymcmodface.ModdymcmodfaceModElements;

import java.util.List;
import java.util.Collections;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.block.BushBlock;

@ModdymcmodfaceModElements.ModElement.Tag
public class PlanterBlock extends ModdymcmodfaceModElements.ModElement {
	@ObjectHolder("moddymcmodface:planter")
	public static final Block block = null;
	public PlanterBlock(ModdymcmodfaceModElements instance) {
		super(instance, 64);
	}

	@Override
	public void initElements() {
		elements.blocks.add(() -> new CustomBlock());
		elements.items.add(() -> new BlockItem(block, new Item.Properties().group(ItemGroup.DECORATIONS)).setRegistryName(block.getRegistryName()));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void clientLoad(FMLClientSetupEvent event) {
		RenderTypeLookup.setRenderLayer(block, RenderType.getCutout());
	}
	public static class CustomBlock extends Block {
		public static final BooleanProperty EXTENDED = BlockStateProperties.EXTENDED; // raised dirt?
		public CustomBlock() {
			super(Block.Properties.create(Material.ROCK).sound(SoundType.STONE).hardnessAndResistance(15f, 42f).lightValue(0).harvestLevel(1)
					.harvestTool(ToolType.PICKAXE).notSolid());
			this.setDefaultState(this.stateContainer.getBaseState().with(EXTENDED, false));
			setRegistryName("planter");
		}

		@Override
		protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
			builder.add(EXTENDED);
		}

		@Override
		public void neighborChanged(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean moving) {
			super.neighborChanged(state, world, pos, neighborBlock, fromPos, moving);
			BlockPos up =pos.up();
			Block _bk = world.getBlockState(up).getBlock();

/*
			if(world.getBlockState(up).isAir(world,up)){
				List<Entity> list1 = world.getEntitiesWithinAABBExcludingEntity(null,new AxisAlignedBB(up).grow(0.1,1,0.1));
				if (!list1.isEmpty()) {
					for (Entity entity : list1) {
						if (entity instanceof ItemEntity) {
							Item it= ((ItemEntity)entity).getItem().getItem();
							if (it instanceof BlockItem) {
								BlockState i_bs =((BlockItem) it).getBlock().getDefaultState();
								Block i_bk =i_bs.getBlock();
								if (i_bk instanceof BushBlock){
									world.setBlockState(up, i_bs);
							
									if (!world.isRemote){
										entity.remove();
									}
									break;
								}		
							}
						}	
					}
				}
			}*/

			boolean flag = true;
			if ((_bk instanceof AirBlock) || (_bk instanceof StemBlock) || (_bk instanceof CropsBlock)) {
				flag = false;
			}
			world.setBlockState(pos, state.with(EXTENDED, flag));





					
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
		public boolean isFertile(BlockState state, IBlockReader world, BlockPos pos) {
			return true;
		}

		@Override
		public boolean canSustainPlant(BlockState state, IBlockReader world, BlockPos pos, Direction direction, IPlantable plantable) {
			return true;
		}

		@Override
		public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
			return VoxelShapes.or(VoxelShapes.create(0.125D, 0D, 0.125D, 0.875D, 0.687D, 0.875D), VoxelShapes.create(0D, 0.687D, 0D, 1D, 1D, 1D));
		}

		@Override
		public MaterialColor getMaterialColor(BlockState state, IBlockReader blockAccess, BlockPos pos) {
			return MaterialColor.RED_TERRACOTTA;
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
