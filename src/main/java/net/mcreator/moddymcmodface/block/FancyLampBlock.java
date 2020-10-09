
package net.mcreator.moddymcmodface.block;

import net.minecraftforge.registries.ObjectHolder;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.IBlockReader;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Rotation;
import net.minecraft.util.Mirror;
import net.minecraft.util.Direction;
import net.minecraft.state.StateContainer;
import net.minecraft.state.DirectionProperty;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.BlockItem;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.block.material.Material;
import net.minecraft.block.SoundType;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Block;

import net.mcreator.moddymcmodface.ModdymcmodfaceModElements;

import java.util.List;
import java.util.Collections;

@ModdymcmodfaceModElements.ModElement.Tag
public class FancyLampBlock extends ModdymcmodfaceModElements.ModElement {
	@ObjectHolder("moddymcmodface:fancy_lamp")
	public static final Block block = null;
	public FancyLampBlock(ModdymcmodfaceModElements instance) {
		super(instance, 112);
	}

	@Override
	public void initElements() {
		elements.blocks.add(() -> new CustomBlock());
		elements.items.add(() -> new BlockItem(block, new Item.Properties().group(ItemGroup.DECORATIONS)).setRegistryName(block.getRegistryName()));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void clientLoad(FMLClientSetupEvent event) {
		RenderTypeLookup.setRenderLayer(block, RenderType.getTranslucent());
	}
	public static class CustomBlock extends Block {
		public static final DirectionProperty FACING = DirectionalBlock.FACING;
		public CustomBlock() {
			super(Block.Properties.create(Material.GLASS).sound(SoundType.GLASS).hardnessAndResistance(0.3f, 0.3f).lightValue(15).notSolid());
			this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.NORTH));
			setRegistryName("fancy_lamp");
		}

		@OnlyIn(Dist.CLIENT)
		@Override
		public boolean isEmissiveRendering(BlockState blockState) {
			return true;
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
					return VoxelShapes.create(0.875D, 0.125D, 0.4375D, 0.125D, 0.875D, 0D);
				case NORTH :
					return VoxelShapes.create(0.125D, 0.125D, 0.5625D, 0.875D, 0.875D, 1D);
				case WEST :
					return VoxelShapes.create(0.5625D, 0.125D, 0.875D, 1D, 0.875D, 0.125D);
				case EAST :
					return VoxelShapes.create(0.4375D, 0.125D, 0.125D, 0D, 0.875D, 0.875D);
				case UP :
					return VoxelShapes.create(0.125D, 0.4375D, 0.125D, 0.875D, 0D, 0.875D);
				case DOWN :
					return VoxelShapes.create(0.125D, 0.5625D, 0.875D, 0.875D, 1D, 0.125D);
			}
		}

		@Override
		protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
			builder.add(FACING);
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
