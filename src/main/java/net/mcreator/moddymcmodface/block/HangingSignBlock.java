
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
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IBlockReader;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.Rotation;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Mirror;
import net.minecraft.util.Hand;
import net.minecraft.util.Direction;
import net.minecraft.util.ActionResultType;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.StateContainer;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.BooleanProperty;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.network.NetworkManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.DyeColor;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.BlockItem;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.Entity;
import net.minecraft.command.ICommandSource;
import net.minecraft.command.CommandSource;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.gui.RenderComponentsUtil;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.block.material.Material;
import net.minecraft.block.SoundType;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.Block;

import net.mcreator.moddymcmodface.gui.EditHangingSignGui;
import net.mcreator.moddymcmodface.ModdymcmodfaceModElements;
import net.mcreator.moddymcmodface.CommonUtil;

import javax.annotation.Nullable;

import java.util.stream.IntStream;
import java.util.function.Function;
import java.util.List;
import java.util.Collections;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.WallBlock;
import net.minecraft.state.IntegerProperty;
import net.minecraft.item.DyeItem;
import net.minecraft.world.storage.MapData;
import net.minecraft.item.FilledMapItem;

@ModdymcmodfaceModElements.ModElement.Tag
public class HangingSignBlock extends ModdymcmodfaceModElements.ModElement {
	public static final int MAXLINES = 5;
	@ObjectHolder("moddymcmodface:hanging_sign")
	public static final Block block = null;
	@ObjectHolder("moddymcmodface:hanging_sign")
	public static final TileEntityType<CustomTileEntity> tileEntityType = null;
	public HangingSignBlock(ModdymcmodfaceModElements instance) {
		super(instance, 168);
		FMLJavaModLoadingContext.get().getModEventBus().register(this);
	}

	@Override
	public void initElements() {
		elements.blocks.add(() -> new CustomBlock());
		elements.items.add(() -> new BlockItem(block, new Item.Properties().group(ItemGroup.DECORATIONS)).setRegistryName(block.getRegistryName()));
	}

	@SubscribeEvent
	public void registerTileEntity(RegistryEvent.Register<TileEntityType<?>> event) {
		event.getRegistry().register(TileEntityType.Builder.create(CustomTileEntity::new, block).build(null).setRegistryName("hanging_sign"));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void clientLoad(FMLClientSetupEvent event) {
		RenderTypeLookup.setRenderLayer(block, RenderType.getCutout());
		ClientRegistry.bindTileEntityRenderer(tileEntityType, CustomRender::new);
	}
	public static class CustomBlock extends Block {
		public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;
		public static final BooleanProperty INVERTED = BlockStateProperties.INVERTED; // is it renderer by tile entity? animated part
		public static final IntegerProperty EXTENSION = CommonUtil.EXTENSION;
		public CustomBlock() {
			super(Block.Properties.create(Material.WOOD).sound(SoundType.WOOD).hardnessAndResistance(1f, 10f).lightValue(0).doesNotBlockMovement()
					.notSolid());
			this.setDefaultState(this.stateContainer.getBaseState().with(EXTENSION, 0).with(FACING, Direction.NORTH).with(INVERTED, false));
			setRegistryName("hanging_sign");
		}

		@Override
		public boolean isNormalCube(BlockState state, IBlockReader worldIn, BlockPos pos) {
			return false;
		}

		@Override
		public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos) {
			return true;
		}

		/**
		 * Return true if an entity can be spawned inside the block (used to get the
		 * player's bed spawn location)
		 */
		public boolean canSpawnInBlock() {
			return true;
		}

		@Override
		public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
				BlockRayTraceResult hit) {
			TileEntity tileentity = worldIn.getTileEntity(pos);
			if (tileentity instanceof CustomTileEntity) {
				CustomTileEntity te = (CustomTileEntity) tileentity;
				ItemStack itemstack = player.getHeldItem(handIn);
				boolean server = !worldIn.isRemote();
				boolean emptyhand = itemstack.isEmpty();
				boolean flag = itemstack.getItem() instanceof DyeItem && player.abilities.allowEdit;
				boolean flag1 = te.isEmpty() && !emptyhand;
				boolean flag2 = !te.isEmpty() && emptyhand;
				//color
				if (flag){
					if(te.setTextColor(((DyeItem) itemstack.getItem()).getDyeColor())){
						if (!player.isCreative()) {
							itemstack.shrink(1);
						}
						if(server){
							te.markDirty();
						}
						return ActionResultType.SUCCESS;
					}
				}
				//not an else to allow to place dye items after coloring
				//place item
				if (flag1) {
					ItemStack it = (ItemStack) itemstack.copy();
					it.setCount((int) 1);
					NonNullList<ItemStack> stacks = NonNullList.<ItemStack>withSize(1, it);
					te.setItems(stacks);
					if (!player.isCreative()) {
						itemstack.shrink(1);
					}
					if (!worldIn.isRemote()) {
						worldIn.playSound((PlayerEntity) null, pos, SoundEvents.ENTITY_ITEM_FRAME_ADD_ITEM, SoundCategory.BLOCKS, 1.0F,
								worldIn.rand.nextFloat() * 0.10F + 0.95F);
						te.markDirty();
					}
					return ActionResultType.SUCCESS;
				} 
				//remove item
				else if (flag2) {
					ItemStack it = te.removeStackFromSlot(0);
					player.setHeldItem(handIn, it);
					if (!worldIn.isRemote()) {
						te.markDirty();
					}
					return ActionResultType.SUCCESS;
				}
				// open gui (edit sign with empty hand)
				else if (player instanceof PlayerEntity && !server && emptyhand) {
					EditHangingSignGui.GuiWindow.open(te);
					return ActionResultType.SUCCESS;
				}
			}
			return ActionResultType.PASS;
		}

		public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
			return worldIn.getBlockState(pos.offset(state.get(FACING).getOpposite())).getMaterial().isSolid();
		}

		@Override
		public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos,
				BlockPos facingPos) {
			return facing == stateIn.get(FACING).getOpposite() && !stateIn.isValidPosition(worldIn, currentPos)
					? Blocks.AIR.getDefaultState()
					: super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
		}

		@Override
		public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
			switch ((Direction) state.get(FACING)) {
				case UP :
				case DOWN :
				case SOUTH :
				default :
					return VoxelShapes.create(0.5625D, 0D, 1D, 0.4375D, 1D, 0D);
				case NORTH :
					return VoxelShapes.create(0.4375D, 0D, 0D, 0.5625D, 1D, 1D);
				case WEST :
					return VoxelShapes.create(0D, 0D, 0.5625D, 1D, 1D, 0.4375D);
				case EAST :
					return VoxelShapes.create(1D, 0D, 0.4375D, 0D, 1D, 0.5625D);
			}
		}

		@Override
		protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
			builder.add(FACING, INVERTED, EXTENSION);
		}

		public BlockState rotate(BlockState state, Rotation rot) {
			return state.with(FACING, rot.rotate(state.get(FACING)));
		}

		public BlockState mirror(BlockState state, Mirror mirrorIn) {
			return state.rotate(mirrorIn.toRotation(state.get(FACING)));
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
		public BlockState getStateForPlacement(BlockItemUseContext context) {
			if (context.getFace() == Direction.UP || context.getFace() == Direction.DOWN)
				return this.getDefaultState().with(FACING, Direction.NORTH);
  			BlockPos blockpos = context.getPos();
  			IBlockReader world = context.getWorld();
  			Block block = world.getBlockState(blockpos.offset(context.getFace().getOpposite())).getBlock();

  			int flag = 0;
  			if(block instanceof FenceBlock) flag = 1;
  			else if(block instanceof WallBlock) flag = 2;
			return this.getDefaultState().with(FACING, context.getFace()).with(EXTENSION, flag);
		}

		@Override
		public PathNodeType getAiPathNodeType(BlockState state, IBlockReader world, BlockPos pos, MobEntity entity) {
			return PathNodeType.OPEN;
		}

		@Override
		public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
			List<ItemStack> dropsOriginal = super.getDrops(state, builder);
			if (!dropsOriginal.isEmpty())
				return dropsOriginal;
			return Collections.singletonList(new ItemStack(this, 1));
		}
		
		@Override
		public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
			if (state.getBlock() != newState.getBlock()) {
				TileEntity tileentity = world.getTileEntity(pos);
				if (tileentity instanceof CustomTileEntity) {
					InventoryHelper.dropInventoryItems(world, pos, (CustomTileEntity) tileentity);
					world.updateComparatorOutputLevel(pos, this);
				}
				super.onReplaced(state, world, pos, newState, isMoving);
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
	}

	public static class CustomTileEntity extends LockableLootTileEntity implements ITickableTileEntity, ISidedInventory {
		private NonNullList<ItemStack> stacks = NonNullList.<ItemStack>withSize(1, ItemStack.EMPTY);
		public float angle = 0;
		public float prevAngle = 0;
		public int counter = 800; //lower counter is used by hitting animation
		public final ITextComponent[] signText = new ITextComponent[]{new StringTextComponent(""), new StringTextComponent(""),
				new StringTextComponent(""), new StringTextComponent(""), new StringTextComponent("")};
		private boolean isEditable = true;
		private PlayerEntity player;
		private final String[] renderText = new String[MAXLINES];
		private DyeColor textColor = DyeColor.BLACK;
		protected CustomTileEntity() {
			super(tileEntityType);
		}

		@Override
		public void markDirty() {
			// this.world.notifyBlockUpdate(this.getPos(), this.getBlockState(),
			// this.getBlockState(), 2);
			super.markDirty();
		}

		@Override
		public void read(CompoundNBT compound) {
			super.read(compound);
			if (!this.checkLootAndRead(compound)) {
				this.stacks = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
			}
			ItemStackHelper.loadAllItems(compound, this.stacks);
			// sign code
			this.isEditable = false;
			this.textColor = DyeColor.byTranslationKey(compound.getString("Color"), DyeColor.BLACK);
			for (int i = 0; i < MAXLINES; ++i) {
				String s = compound.getString("Text" + (i + 1));
				ITextComponent itextcomponent = ITextComponent.Serializer.fromJson(s.isEmpty() ? "\"\"" : s);
				if (this.world instanceof ServerWorld) {
					try {
						this.signText[i] = TextComponentUtils.updateForEntity(this.getCommandSource((ServerPlayerEntity) null), itextcomponent,
								(Entity) null, 0);
					} catch (CommandSyntaxException var6) {
						this.signText[i] = itextcomponent;
					}
				} else {
					this.signText[i] = itextcomponent;
				}
				this.renderText[i] = null;
			}
		}

		@Override
		public CompoundNBT write(CompoundNBT compound) {
			super.write(compound);
			if (!this.checkLootAndWrite(compound)) {
				ItemStackHelper.saveAllItems(compound, this.stacks);
			}
			for (int i = 0; i < MAXLINES; ++i) {
				String s = ITextComponent.Serializer.toJson(this.signText[i]);
				compound.putString("Text" + (i + 1), s);
			}
			compound.putString("Color", this.textColor.getTranslationKey());
			return compound;
		}

		// lots of sign code coming up
		@OnlyIn(Dist.CLIENT)
		public ITextComponent getText(int line) {
			return this.signText[line];
		}

		public void setText(int line, ITextComponent p_212365_2_) {
			this.signText[line] = p_212365_2_;
			this.renderText[line] = null;
		}

		@Nullable
		@OnlyIn(Dist.CLIENT)
		public String getRenderText(int line, Function<ITextComponent, String> p_212364_2_) {
			if (this.renderText[line] == null && this.signText[line] != null) {
				this.renderText[line] = p_212364_2_.apply(this.signText[line]);
			}
			return this.renderText[line];
		}

		public boolean getIsEditable() {
			return this.isEditable;
		}

		/**
		 * Sets the sign's isEditable flag to the specified parameter.
		 */
		@OnlyIn(Dist.CLIENT)
		public void setEditable(boolean isEditableIn) {
			this.isEditable = isEditableIn;
			if (!isEditableIn) {
				this.player = null;
			}
		}

		public void setPlayer(PlayerEntity playerIn) {
			this.player = playerIn;
		}

		public PlayerEntity getPlayer() {
			return this.player;
		}

		public boolean executeCommand(PlayerEntity playerIn) {
			for (ITextComponent itextcomponent : this.signText) {
				Style style = itextcomponent == null ? null : itextcomponent.getStyle();
				if (style != null && style.getClickEvent() != null) {
					ClickEvent clickevent = style.getClickEvent();
					if (clickevent.getAction() == ClickEvent.Action.RUN_COMMAND) {
						playerIn.getServer().getCommandManager().handleCommand(this.getCommandSource((ServerPlayerEntity) playerIn),
								clickevent.getValue());
					}
				}
			}
			return true;
		}

		public CommandSource getCommandSource(@Nullable ServerPlayerEntity playerIn) {
			String s = playerIn == null ? "Sign" : playerIn.getName().getString();
			ITextComponent itextcomponent = (ITextComponent) (playerIn == null ? new StringTextComponent("Sign") : playerIn.getDisplayName());
			return new CommandSource(ICommandSource.DUMMY,
					new Vec3d((double) this.pos.getX() + 0.5D, (double) this.pos.getY() + 0.5D, (double) this.pos.getZ() + 0.5D), Vec2f.ZERO,
					(ServerWorld) this.world, 2, s, itextcomponent, this.world.getServer(), playerIn);
		}

		public DyeColor getTextColor() {
			return this.textColor;
		}

		public boolean setTextColor(DyeColor newColor) {
			if (newColor != this.getTextColor()) {
				this.textColor = newColor;
				this.markDirty();
				this.world.notifyBlockUpdate(this.getPos(), this.getBlockState(),this.getBlockState(), 3);
				return true;
			} else {
				return false;
			}
		}

		@Override
		public boolean onlyOpsCanSetNbt() {
			return true;
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

		@Override
		public int getSizeInventory() {
			return stacks.size();
		}

		@Override
		public boolean isEmpty() {
			for (ItemStack itemstack : this.stacks)
				if (!itemstack.isEmpty())
					return false;
			return true;
		}

		@Override
		public int getInventoryStackLimit() {
			return 1;
		}

		@Override
		protected NonNullList<ItemStack> getItems() {
			return this.stacks;
		}

		@Override
		protected void setItems(NonNullList<ItemStack> stacks) {
			this.stacks = stacks;
		}

		@Override
		public boolean isItemValidForSlot(int index, ItemStack stack) {
			return true;
		}

		@Override
		public boolean canInsertItem(int index, ItemStack stack, @Nullable Direction direction) {
			return false;
		}

		@Override
		public boolean canExtractItem(int index, ItemStack stack, Direction direction) {
			return false;
		}

		@Override
		public int[] getSlotsForFace(Direction side) {
			return IntStream.range(0, this.getSizeInventory()).toArray();
		}

		@Override
		public ITextComponent getDefaultName() {
			return new StringTextComponent("hanging sing");
		}

		@Override
		public Container createMenu(int id, PlayerInventory player) {
			return ChestContainer.createGeneric9X3(id, player, this);
		}

		@Override
		public ITextComponent getDisplayName() {
			return new StringTextComponent("Hanging sing");
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
				float minswingangle = 2.5f;
				float maxperiod = 25f;
				float angleledamping = 150f;
				float perioddamping = 100f;
				//actually tey are the inverse of damping. increase them to fave less damping
				
				float a = minswingangle;
				float k = 0.01f;
				if(counter<800){
					a = (float) Math.max((float) maxswingangle * Math.pow(Math.E, -(counter / angleledamping)), minswingangle);
					k = (float) Math.max(Math.PI*2*(float)Math.pow(Math.E, -(counter/perioddamping)), 0.01f);
				}				

				this.angle = a * MathHelper.cos((counter/maxperiod) - k);
				// this.angle = 90*(float)
				// Math.cos((float)counter/40f)/((float)this.counter/20f);;
			}
		}
	}

	public static class CustomRender extends TileEntityRenderer<CustomTileEntity> {
		public CustomRender(TileEntityRendererDispatcher rendererDispatcherIn) {
			super(rendererDispatcherIn);
		}

		@Override
		public void render(CustomTileEntity entityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
				int combinedOverlayIn) {
			matrixStackIn.push();
			//rotate towards direction
			matrixStackIn.translate(0.5, 0.875, 0.5);
			matrixStackIn.rotate(entityIn.getDirection().getOpposite().getRotation());
			matrixStackIn.rotate(Vector3f.XP.rotationDegrees(-90));
			//animation
			matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(MathHelper.lerp(partialTicks, entityIn.prevAngle, entityIn.angle)));
			matrixStackIn.translate(-0.5, -0.875, -0.5);
			//render block
			BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
			BlockState state = block.getDefaultState().with(BlockStateProperties.INVERTED, true);
			blockRenderer.renderBlock(state, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);
			matrixStackIn.translate(0.5, 0.5 - 0.1875, 0.5);
			matrixStackIn.rotate(Vector3f.YP.rotationDegrees(90));
			// render item
			if (!entityIn.isEmpty()) {
				ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
				ItemStack stack = entityIn.getStackInSlot(0);
				IBakedModel ibakedmodel = itemRenderer.getItemModelWithOverrides(stack, entityIn.getWorld(), null);

				MapData mapdata = FilledMapItem.getMapData(stack, entityIn.getWorld());
				for (int v = 0; v < 2; v++) {
					matrixStackIn.push();
					//render map
					if(mapdata != null){
						matrixStackIn.translate(0, 0, -0.0625 - 0.005);
						matrixStackIn.scale(.0068359375F, .0068359375F, .0068359375F);
            			matrixStackIn.translate(-64.0D, -64.0D, 0.0D);
            			//matrixStackIn.translate(0.0D, 0.0D, -1.0D);
              			Minecraft.getInstance().gameRenderer.getMapItemRenderer().renderMap(matrixStackIn, bufferIn, mapdata, true, combinedLightIn);
					}
					//render item
					else{
						matrixStackIn.translate(0, 0, 0.078125);
						matrixStackIn.scale(0.5f, 0.5f, 0.5f);
						itemRenderer.renderItem(stack, ItemCameraTransforms.TransformType.FIXED, true, matrixStackIn, bufferIn, combinedLightIn,
								combinedOverlayIn, ibakedmodel);
					}
					matrixStackIn.pop();

					matrixStackIn.rotate(Vector3f.YP.rotationDegrees(180));

				}
			}
			// render text
			else {
				// sign code
				FontRenderer fontrenderer = this.renderDispatcher.getFontRenderer();
				int i = entityIn.getTextColor().getTextColor();
				double d0 = 0.4D;
				int j = (int) ((double) NativeImage.getRed(i) * 0.4D);
				int k = (int) ((double) NativeImage.getGreen(i) * 0.4D);
				int l = (int) ((double) NativeImage.getBlue(i) * 0.4D);
				int i1 = NativeImage.getCombined(0, l, k, j);

				for (int v = 0; v < 2; v++) {
					matrixStackIn.push();
					matrixStackIn.translate(0, 0, 0.0625 + 0.005);
					matrixStackIn.scale(0.010416667F, -0.010416667F, 0.010416667F);
					for (int j1 = 0; j1 < MAXLINES; ++j1) {
						String s = entityIn.getRenderText(j1, (p_212491_1_) -> {
							List<ITextComponent> list = RenderComponentsUtil.splitText(p_212491_1_, 75, fontrenderer, false, true);
							return list.isEmpty() ? "" : list.get(0).getFormattedText();
						});
						if (s != null) {
							float f3 = (float) (-fontrenderer.getStringWidth(s) / 2);
							fontrenderer.renderString(s, f3, (float) (j1 * 10 - entityIn.signText.length * 5), i1, false,
									matrixStackIn.getLast().getMatrix(), bufferIn, false, 0, combinedLightIn);
						}
					}
					matrixStackIn.pop();
					matrixStackIn.rotate(Vector3f.YP.rotationDegrees(180));
				}
			}
			matrixStackIn.pop();
		}
	}
}
