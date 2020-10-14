
package net.mcreator.moddymcmodface.block;

import net.minecraftforge.registries.ObjectHolder;
import net.minecraftforge.common.extensions.IForgeBlock;

import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.World;
import net.minecraft.world.IBlockReader;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.StateContainer;
import net.minecraft.state.IntegerProperty;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.BlockItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.block.material.Material;
import net.minecraft.block.SoundType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Block;

import net.mcreator.moddymcmodface.ModdymcmodfaceModElements;

import java.util.List;
import java.util.Collections;

@ModdymcmodfaceModElements.ModElement.Tag
public class RedstoneIlluminatorBlock extends ModdymcmodfaceModElements.ModElement {
	@ObjectHolder("moddymcmodface:redstone_illuminator")
	public static final Block block = null;
	public RedstoneIlluminatorBlock(ModdymcmodfaceModElements instance) {
		super(instance, 75);
	}

	@Override
	public void initElements() {
		elements.blocks.add(() -> new CustomBlock());
		elements.items.add(() -> new BlockItem(block, new Item.Properties().group(ItemGroup.REDSTONE)).setRegistryName(block.getRegistryName()));
	}
	public static class CustomBlock extends Block implements IForgeBlock {
		public static final IntegerProperty POWER = BlockStateProperties.POWER_0_15;
		public CustomBlock() {
			super(Block.Properties.create(Material.REDSTONE_LIGHT).sound(SoundType.GLASS).hardnessAndResistance(0.3f, 0.3f).lightValue(15));
			setRegistryName("redstone_illuminator");
			this.setDefaultState(this.stateContainer.getBaseState().with(POWER, Integer.valueOf(0)));
		}

		@Override
		public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
			return (int) 15 - state.get(POWER);
		}

		@Override
		protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
			builder.add(POWER);
		}

		@Override
		public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
			this.updatePower(state, worldIn, pos);
		}

		@Override
		public void neighborChanged(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean moving) {
			super.neighborChanged(state, world, pos, neighborBlock, fromPos, moving);
			this.updatePower(state, world, pos);
		}

		public void updatePower(BlockState state, World world, BlockPos pos) {
			if (!world.isRemote) {
				int pow = world.getRedstonePowerFromNeighbors(pos);
				world.setBlockState(pos, state.with(POWER, MathHelper.clamp(pow, 0, 15)), 2);
			}
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
