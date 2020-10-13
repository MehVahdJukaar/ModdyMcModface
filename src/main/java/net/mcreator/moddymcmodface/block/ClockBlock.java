
package net.mcreator.moddymcmodface.block;

import net.minecraftforge.registries.ObjectHolder;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.World;
import net.minecraft.world.IBlockReader;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.Rotation;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Mirror;
import net.minecraft.util.Hand;
import net.minecraft.util.Direction;
import net.minecraft.util.ActionResultType;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.StateContainer;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.BooleanProperty;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.network.NetworkManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.BlockItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.block.material.PushReaction;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.SoundType;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Block;

import net.mcreator.moddymcmodface.ModdymcmodfaceModElements;

import java.util.List;
import java.util.Collections;

import com.mojang.blaze3d.matrix.MatrixStack;

@ModdymcmodfaceModElements.ModElement.Tag
public class ClockBlock extends ModdymcmodfaceModElements.ModElement {
	@ObjectHolder("moddymcmodface:clock")
	public static final Block block = null;
	@ObjectHolder("moddymcmodface:clock")
	public static final TileEntityType<CustomTileEntity> tileEntityType = null;
	public ClockBlock(ModdymcmodfaceModElements instance) {
		super(instance, 35);
		FMLJavaModLoadingContext.get().getModEventBus().register(this);
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
		ClientRegistry.bindTileEntityRenderer(tileEntityType, CustomRender::new);
	}

	@SubscribeEvent
	public void registerTileEntity(RegistryEvent.Register<TileEntityType<?>> event) {
		event.getRegistry().register(TileEntityType.Builder.create(CustomTileEntity::new, block).build(null).setRegistryName("clock"));
	}
	public static class CustomBlock extends Block {
		public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;
		public static final IntegerProperty POWER = BlockStateProperties.POWER_0_15;
		public static final IntegerProperty HOUR = IntegerProperty.create("hour", 0, 23);
		public static final BooleanProperty INVERTED = BlockStateProperties.INVERTED; // is it hand only? (used only for
																						// tile entity renrering)
		public CustomBlock() {
			super(Block.Properties.create(Material.WOOD).sound(SoundType.WOOD).hardnessAndResistance(2f, 6f).lightValue(1).harvestLevel(0)
					.harvestTool(ToolType.AXE).notSolid());
			setRegistryName("clock");
			this.setDefaultState(
					this.stateContainer.getBaseState().with(FACING, Direction.NORTH).with(INVERTED, false).with(POWER, Integer.valueOf(0)));
		}

		@Override
		public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
				BlockRayTraceResult hit) {
			if (!worldIn.isRemote()) {
				int time = ((int) (worldIn.getWorld().getDayTime()+6000) % 24000);
				int h = time / 1000;
				int m = (int) (((float) (time % 1000f) / 1000f) * 60);
				String a = time < 12000 ? " AM" : " PM";
				player.sendStatusMessage(new StringTextComponent(h + ":" + ((m<10)?"0":"") + m+ a), true);
			}
			return ActionResultType.SUCCESS;
		}

		public BlockState rotate(BlockState state, Rotation rot) {
			return state.with(FACING, rot.rotate(state.get(FACING)));
		}

		public BlockState mirror(BlockState state, Mirror mirrorIn) {
			return state.rotate(mirrorIn.toRotation(state.get(FACING)));
		}

		@Override
		public BlockState getStateForPlacement(BlockItemUseContext context) {
			return this.getDefaultState().with(FACING, context.getPlacementHorizontalFacing().getOpposite());
		}

		@Override
		public boolean isNormalCube(BlockState state, IBlockReader worldIn, BlockPos pos) {
			return false;
		}

		@Override
		public int getFlammability(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
			return 2;
		}

		/*
		 * public int getWeakPower(BlockState blockState, IBlockReader blockAccess,
		 * BlockPos pos, Direction side) { return blockState.get(POWER); }
		 */
		@Override
		public MaterialColor getMaterialColor(BlockState state, IBlockReader blockAccess, BlockPos pos) {
			return MaterialColor.BROWN;
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
			return Collections.singletonList(new ItemStack(this, 1));
		}

		@Override
		public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
			switch ((Direction) state.get(FACING)) {
				case NORTH :
				default :
					return VoxelShapes.create(1D, 0D, 1D, 0D, 1D, 0.0625D);
				case SOUTH :
					return VoxelShapes.create(0D, 0D, 0D, 1D, 1D, 0.9375D);
				case EAST :
					return VoxelShapes.create(0D, 0D, 1D, 0.9375D, 1D, 0D);
				case WEST :
					return VoxelShapes.create(1D, 0D, 0D, 0.0625D, 1D, 1D);
			}
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

		@Override
		public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
			if (state.getBlock() != newState.getBlock()) {
				TileEntity tileentity = world.getTileEntity(pos);
				if (tileentity instanceof CustomTileEntity) {
					world.updateComparatorOutputLevel(pos, this);
				}
				super.onReplaced(state, world, pos, newState, isMoving);
			}
		}

		public boolean canProvidePower(BlockState state) {
			return false;
		}

		@Override
		protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
			builder.add(POWER,HOUR,FACING,INVERTED);
		}

		@Override
		public boolean hasComparatorInputOverride(BlockState state) {
			return true;
		}

		@Override
		public int getComparatorInputOverride(BlockState blockState, World world, BlockPos pos) {
			return blockState.get(POWER);
		}

		public static int getHour(BlockState state) {
			return state.get(HOUR);
		}

		public static int getYaw(BlockState state) {
			switch ((Direction) state.get(FACING)) {
				case NORTH :
					return 0;
				case SOUTH :
					return 180;
				case WEST :
					return 90;
				case EAST :
					return 270;
				default :
					return 0;
			}
		}

		public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
			this.updatePower(state, world, pos);
		}

		public static void updatePower(BlockState bs, World world, BlockPos pos) {
			// 0-24000
			int time = (int) (world.getWorld().getDayTime() % 24000);
			int power = MathHelper.clamp((int) MathHelper.floor(time / 1500D), 0, 15);
			int hour = MathHelper.clamp((int) MathHelper.floor(time / 1000D), 0, 24);
			int flag = 3;
			if (bs.get(HOUR) != hour){
				ResourceLocation res;
				if (hour % 2 == 0) {
					res = new ResourceLocation("moddymcmodface:tick_1");
				} else {
					res = new ResourceLocation("moddymcmodface:tick_2");
				}

				world.getWorld().playSound(null, pos, (net.minecraft.util.SoundEvent) ForgeRegistries.SOUND_EVENTS.getValue(res),
							SoundCategory.BLOCKS, (float) .3, (float) 1.2f);
				
			}
			world.setBlockState(pos, bs.with(POWER, power).with(HOUR, hour), 2);
		}
	}

	public static class CustomTileEntity extends TileEntity implements ITickableTileEntity {
		private float roll = 0;
		private float prevRoll = 0;
		private float targetRoll = 0;
		protected CustomTileEntity() {
			super(tileEntityType);
			// this.setInitialRoll();
		}

		protected CustomTileEntity(int r) {
			super(tileEntityType);
			this.roll = r;
			this.prevRoll = r;
		}

		@Override
		public void read(CompoundNBT compound) {
			super.read(compound);
			this.roll = compound.getFloat("roll");
			this.prevRoll = compound.getFloat("prevroll");
			this.targetRoll = compound.getFloat("targetroll");
		}

		@Override
		public CompoundNBT write(CompoundNBT compound) {
			super.write(compound);
			compound.putFloat("roll", this.roll);
			compound.putFloat("prevroll", this.prevRoll);
			compound.putFloat("targetroll", this.targetRoll);
			return compound;
		}

		@Override
		public SUpdateTileEntityPacket getUpdatePacket() {
			return new SUpdateTileEntityPacket(this.pos, 0, this.getUpdateTag());
		}

		@Override
		public CompoundNBT getUpdateTag() {
			return this.write(new CompoundNBT());
		}

		@Override
		public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
			this.read(pkt.getNbtCompound());
		}

		public void setRoll(float roll) {
			this.prevRoll = this.roll;
			this.roll = roll;
		}

		public void setInitialRoll() {
			double time = MathHelper.floor(this.world.getDayTime() / 24000D);
			int r = 30 * MathHelper.clamp((int) MathHelper.floor(time / 1000D), 0, 24);
			this.prevRoll = r;
			this.roll = r;
			/*
			 * BlockState blockstate = this.getBlockState(); if (block instanceof
			 * ClockBlock.CustomBlock) { float r = 30f * CustomBlock.getHour(blockstate);
			 * this.prevRoll = r; this.roll = r; }
			 */
		}

		public float wrapDegrees(float a) {
			// return a<360f ? a : a-360f;
			return a % 360f;
		}

		public void tick() {
			if (this.world != null && this.world.getGameTime() % 20L == 0L) {
				BlockState blockstate = this.getBlockState();
				if (!this.world.isRemote) {
					Block block = blockstate.getBlock();
					if (block instanceof ClockBlock.CustomBlock) {
						CustomBlock.updatePower(blockstate, this.world, this.pos);
					}
				}
				float t = this.wrapDegrees(30f * CustomBlock.getHour(blockstate));
				this.targetRoll = t;
			}
			if (this.targetRoll < this.roll) {
				this.prevRoll = this.roll;
			} else {
				this.prevRoll = this.roll;
			}
			if (this.roll != this.targetRoll) {
				float r = this.wrapDegrees(this.roll + 8);
				if (r >= this.targetRoll && r <= this.targetRoll + 8) {
					r = this.targetRoll;
				}
				this.roll = r;
			}
		}

		public float getRoll() {
			return (float) this.roll;
		}

		public float getPrevRoll() {
			return (float) this.prevRoll;
		}

		public int getYaw() {
			return CustomBlock.getYaw(this.getBlockState());
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static class CustomRender extends TileEntityRenderer<CustomTileEntity> {
		private static final ResourceLocation texture = new ResourceLocation("moddymcmodface:textures/clock_hand.png");
		public CustomRender(TileEntityRendererDispatcher rendererDispatcherIn) {
			super(rendererDispatcherIn);
		}

		@Override
		@OnlyIn(Dist.CLIENT)
		public void render(CustomTileEntity entityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
				int combinedOverlayIn) {
			matrixStackIn.push();
			matrixStackIn.translate(0.5d, 0.5d, 0.5d);
			matrixStackIn.rotate(Vector3f.YP.rotationDegrees(entityIn.getYaw()));
			matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(MathHelper.interpolateAngle(partialTicks, entityIn.getPrevRoll(), entityIn.getRoll())));
			/*
			 * IVertexBuilder vb =
			 * bufferIn.getBuffer(RenderType.getEntityCutout(this.getEntityTexture(entityIn)
			 * )); matrixStackIn.translate(0d, -1d, 0d); EntityModel model = new
			 * ModelClock_Hand(); model.render(matrixStackIn, vb, combinedLightIn,
			 * OverlayTexture.NO_OVERLAY, 1, 1, 1, 1f);
			 */
			matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(180));
			BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
			matrixStackIn.translate(-0.5, -0.5, -0.5);
			BlockState state = ClockBlock.block.getDefaultState().with(BlockStateProperties.INVERTED, true);
			blockRenderer.renderBlock(state, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);
			matrixStackIn.pop();
		}

		public ResourceLocation getEntityTexture(CustomTileEntity entity) {
			return texture;
		}
	}
	/*
	 * @OnlyIn(Dist.CLIENT) public static class ModelClock_Hand extends
	 * EntityModel<Entity> { private final ModelRenderer bb_main; public
	 * ModelClock_Hand() { textureWidth = 16; textureHeight = 16; bb_main = new
	 * ModelRenderer(this); bb_main.setRotationPoint(0.0F, 24.0F, 0.0F);
	 * bb_main.setTextureOffset(4, 0).addBox(-1.0F, -9.0F, -8.0F, 2.0F, 2.0F, 1.0F,
	 * 0.0F, false); bb_main.setTextureOffset(0, 0).addBox(-0.5F, -14.0F, -8.0F,
	 * 1.0F, 5.0F, 1.0F, 0.0F, false); }
	 * 
	 * @Override public void setRotationAngles(Entity entity, float limbSwing, float
	 * limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) { //
	 * previously the render function, render code was moved to a method below }
	 * 
	 * @Override public void render(MatrixStack matrixStack, IVertexBuilder buffer,
	 * int packedLight, int packedOverlay, float red, float green, float blue, float
	 * alpha) { bb_main.render(matrixStack, buffer, packedLight, packedOverlay); }
	 * 
	 * public void setRotationAngle(ModelRenderer modelRenderer, float x, float y,
	 * float z) { modelRenderer.rotateAngleX = x; modelRenderer.rotateAngleY = y;
	 * modelRenderer.rotateAngleZ = z; } }
	 */
}
