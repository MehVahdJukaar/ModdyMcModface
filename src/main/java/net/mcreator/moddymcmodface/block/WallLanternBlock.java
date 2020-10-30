
package net.mcreator.moddymcmodface.block;

import net.minecraftforge.registries.ObjectHolder;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.client.model.data.EmptyModelData;
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
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Rotation;
import net.minecraft.util.Mirror;
import net.minecraft.util.Direction;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.state.StateContainer;
import net.minecraft.state.DirectionProperty;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.network.NetworkManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.BlockItem;
import net.minecraft.entity.Entity;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.block.material.Material;
import net.minecraft.block.SoundType;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.Block;

import net.mcreator.moddymcmodface.ModdymcmodfaceModElements;

import java.util.List;
import java.util.Collections;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;

@ModdymcmodfaceModElements.ModElement.Tag
public class WallLanternBlock extends ModdymcmodfaceModElements.ModElement {
	@ObjectHolder("moddymcmodface:wall_lantern")
	public static final Block block = null;
	@ObjectHolder("moddymcmodface:wall_lantern")
	public static final Item item = null;
	@ObjectHolder("moddymcmodface:wall_lantern")
	public static final TileEntityType<CustomTileEntity> tileEntityType = null;
	public WallLanternBlock(ModdymcmodfaceModElements instance) {
		super(instance, 181);
		FMLJavaModLoadingContext.get().getModEventBus().register(this);
	}

	@Override
	public void initElements() {
		elements.blocks.add(() -> new CustomBlock());
		elements.items.add(() -> new BlockItem(block, new Item.Properties().group(null)).setRegistryName(block.getRegistryName()));
	}

	@SubscribeEvent
	public void registerTileEntity(RegistryEvent.Register<TileEntityType<?>> event) {
		event.getRegistry().register(TileEntityType.Builder.create(CustomTileEntity::new, block).build(null).setRegistryName("wall_lantern"));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void clientLoad(FMLClientSetupEvent event) {
		RenderTypeLookup.setRenderLayer(block, RenderType.getCutout());
		ClientRegistry.bindTileEntityRenderer(tileEntityType, CustomRender::new);
	}
	public static class CustomBlock extends Block {
		public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;
		public CustomBlock() {
			super(Block.Properties.create(Material.IRON).sound(SoundType.LANTERN).hardnessAndResistance(3.5f, 3.5f).lightValue(15).notSolid());
			this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.NORTH));
			setRegistryName("wall_lantern");
		}

		@Override
		public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
			return new ItemStack(Blocks.LANTERN, 1);
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
		public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
			Direction direction = state.get(FACING);
			BlockPos blockpos = pos.offset(direction.getOpposite());
			BlockState blockstate = worldIn.getBlockState(blockpos);
			return blockstate.isSolidSide(worldIn, blockpos, direction);
		}
		
		@Override
		public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos,
				BlockPos facingPos) {
			return facing == stateIn.get(FACING).getOpposite() && !stateIn.isValidPosition(worldIn, currentPos)
					? Blocks.AIR.getDefaultState()
					: super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
		}

		@Override
		public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
			super.onEntityCollision(state, world, pos, entity);
			TileEntity tileentity = world.getTileEntity(pos);
			if (tileentity instanceof CustomTileEntity) {
				((CustomTileEntity) tileentity).counter = 0;
			}
		}

		@Override
		public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
			switch ((Direction) state.get(FACING)) {
				case UP :
				case DOWN :
				case SOUTH :
				default :
					return VoxelShapes.create(0.6875D, 0.125D, 0.625D, 0.3125D, 1D, 0D);
				case NORTH :
					return VoxelShapes.create(0.3125D, 0.125D, 0.375D, 0.6875D, 1D, 1D);
				case WEST :
					return VoxelShapes.create(0.375D, 0.125D, 0.6875D, 1D, 1D, 0.3125D);
				case EAST :
					return VoxelShapes.create(0.625D, 0.125D, 0.3125D, 0D, 1D, 0.6875D);
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
			if (context.getFace() == Direction.UP || context.getFace() == Direction.DOWN)
				return this.getDefaultState().with(FACING, Direction.NORTH);
			return this.getDefaultState().with(FACING, context.getFace());
		}

		@Override
		public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
			return Collections.singletonList(new ItemStack(Items.LANTERN));
		}

		@Override
		public boolean hasTileEntity(BlockState state) {
			return true;
		}

		@Override
		public TileEntity createTileEntity(BlockState state, IBlockReader world) {
			return new CustomTileEntity();
		}

		@Override
		public boolean eventReceived(BlockState state, World world, BlockPos pos, int eventID, int eventParam) {
			super.eventReceived(state, world, pos, eventID, eventParam);
			TileEntity tileentity = world.getTileEntity(pos);
			return tileentity == null ? false : tileentity.receiveClientEvent(eventID, eventParam);
		}
	}

	public static class CustomTileEntity extends TileEntity implements ITickableTileEntity {
		public float angle = 0;
		public float prevAngle = 0;
		public int counter = 800;
		// lower counter is used by hitting animation
		protected CustomTileEntity() {
			super(tileEntityType);
		}

		// end of sign code
		@Override
		public SUpdateTileEntityPacket getUpdatePacket() {
			return new SUpdateTileEntityPacket(this.pos, 9, this.getUpdateTag());
		}

		@Override
		public CompoundNBT getUpdateTag() {
			return this.write(new CompoundNBT());
		}

		@Override
		public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
			this.read(pkt.getNbtCompound());
		}

		public Direction getDirection() {
			return this.getBlockState().get(CustomBlock.FACING);
		}

		@Override
		public void tick() {
			if (this.world.isRemote) {
				this.counter++;
				this.prevAngle = this.angle;
				float maxswingangle = 45f;
				float minswingangle = 1.9f;
				float maxperiod = 28f;
				float angleledamping = 80f;
				float perioddamping = 70f;
				// actually tey are the inverse of damping. increase them to fave less damping
				float a = minswingangle;
				float k = 0.01f;
				if (counter < 800) {
					a = (float) Math.max((float) maxswingangle * Math.pow(Math.E, -(counter / angleledamping)), minswingangle);
					k = (float) Math.max(Math.PI * 2 * (float) Math.pow(Math.E, -(counter / perioddamping)), 0.01f);
				}
				this.angle = a * MathHelper.cos((counter / maxperiod) - k);
				// this.angle = 90*(float)
				// Math.cos((float)counter/40f)/((float)this.counter/20f);;
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static class CustomRender extends TileEntityRenderer<CustomTileEntity> {
		public CustomRender(TileEntityRendererDispatcher rendererDispatcherIn) {
			super(rendererDispatcherIn);
		}

		@Override
		public void render(CustomTileEntity entityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
				int combinedOverlayIn) {
			matrixStackIn.push();
			// rotate towards direction
			matrixStackIn.translate(0.5, 0.875, 0.5);
			matrixStackIn.rotate(entityIn.getDirection().getOpposite().getRotation());
			matrixStackIn.rotate(Vector3f.XP.rotationDegrees(-90));
			// animation
			matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(MathHelper.lerp(partialTicks, entityIn.prevAngle, entityIn.angle)));
			matrixStackIn.translate(-0.5, -0.75, -0.375);
			// render block
			BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
			BlockState state = Blocks.LANTERN.getDefaultState();
			blockRenderer.renderBlock(state, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);
			matrixStackIn.pop();
		}
	}
}
