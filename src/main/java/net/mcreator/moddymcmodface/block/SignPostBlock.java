
package net.mcreator.moddymcmodface.block;

import net.minecraftforge.registries.ObjectHolder;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.World;
import net.minecraft.world.IBlockReader;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Rotation;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Mirror;
import net.minecraft.util.Direction;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.state.StateContainer;
import net.minecraft.state.DirectionProperty;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.network.NetworkManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.BlockItem;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.block.material.Material;
import net.minecraft.block.SoundType;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Block;

import net.mcreator.moddymcmodface.ModdymcmodfaceModElements;
import net.mcreator.moddymcmodface.CommonUtil;

import javax.annotation.Nullable;

import java.util.stream.IntStream;
import java.util.List;
import java.util.Collections;
import net.minecraft.util.math.MathHelper;
import net.minecraft.state.IntegerProperty;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraft.client.Minecraft;
import net.minecraft.item.DyeColor;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.command.CommandSource;
import java.util.function.Function;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.entity.Entity;
import net.minecraft.command.ICommandSource;
import net.minecraft.util.text.TextComponentUtils;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.Style;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.util.math.Vec2f;
import net.minecraft.client.gui.RenderComponentsUtil;

@ModdymcmodfaceModElements.ModElement.Tag
public class SignPostBlock extends ModdymcmodfaceModElements.ModElement {
	@ObjectHolder("moddymcmodface:sign_post")
	public static final Block block = null;
	@ObjectHolder("moddymcmodface:sign_post")
	public static final TileEntityType<CustomTileEntity> tileEntityType = null;
	public SignPostBlock(ModdymcmodfaceModElements instance) {
		super(instance, 171);
		FMLJavaModLoadingContext.get().getModEventBus().register(this);
	}

	@Override
	public void initElements() {
		elements.blocks.add(() -> new CustomBlock());
		elements.items.add(() -> new BlockItem(block, new Item.Properties().group(null)).setRegistryName(block.getRegistryName())); //ItemGroup.DECORATIONS
	}

	@SubscribeEvent
	public void registerTileEntity(RegistryEvent.Register<TileEntityType<?>> event) {
		event.getRegistry().register(TileEntityType.Builder.create(CustomTileEntity::new, block).build(null).setRegistryName("sign_post"));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void clientLoad(FMLClientSetupEvent event) {
		RenderTypeLookup.setRenderLayer(block, RenderType.getCutout());
		ClientRegistry.bindTileEntityRenderer(tileEntityType, CustomRender::new);
	}
	public static class CustomBlock extends Block {
		public static final IntegerProperty ROTATION = CommonUtil.ROTATION;
		public static final BooleanProperty INVERTED = BlockStateProperties.INVERTED;
		public CustomBlock() {
			super(Block.Properties.create(Material.WOOD).sound(SoundType.WOOD).hardnessAndResistance(2f, 3f).lightValue(0).notSolid());
			this.setDefaultState(this.stateContainer.getBaseState().with(ROTATION, 0).with(INVERTED, false));
			setRegistryName("sign_post");
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
			return VoxelShapes.create(0.625D, 0D, 0.625D, 0.365D, 1D, 0.375D);			
		}

		@Override
		protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
			builder.add(ROTATION, INVERTED);
		}

		public BlockState rotate(BlockState state, Rotation rot) {
			return state.with(ROTATION, ((state.get(ROTATION) + 4) % 16) -1 );
		}

		@Override
		public BlockState getStateForPlacement(BlockItemUseContext context) {
			float f = (float) MathHelper.floor((MathHelper.wrapDegrees(context.getPlacementYaw() - 180.0F) + 11.25) / 22.5F) * 22.5F;
			float b = f < 0 ? 360 + f : f;
			int a = MathHelper.clamp((int)(b/22.5f), 0, 15);

			return this.getDefaultState().with(ROTATION, a);
		}

		@Override
		public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
			List<ItemStack> dropsOriginal = super.getDrops(state, builder);
			if (!dropsOriginal.isEmpty())
				return dropsOriginal;
			return Collections.singletonList(new ItemStack(this, 1));
		}

		@Override
		public INamedContainerProvider getContainer(BlockState state, World worldIn, BlockPos pos) {
			TileEntity tileEntity = worldIn.getTileEntity(pos);
			return tileEntity instanceof INamedContainerProvider ? (INamedContainerProvider) tileEntity : null;
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

	public static class CustomTileEntity extends TileEntity {
		public ITextComponent signText;
		private boolean isEditable = true;
		private PlayerEntity player;
		private String renderText = null;
		private DyeColor textColor = DyeColor.BLACK;

		protected CustomTileEntity() {
			super(tileEntityType);
		}
		

		@Override
		public void read(CompoundNBT compound) {
			super.read(compound);
			// sign code
			this.isEditable = false;
			this.textColor = DyeColor.byTranslationKey(compound.getString("Color"), DyeColor.BLACK);

			String s = compound.getString("Text");
			ITextComponent itextcomponent = ITextComponent.Serializer.fromJson(s.isEmpty() ? "\"\"" : s);
			if (this.world instanceof ServerWorld) {
				try {
					this.signText = TextComponentUtils.updateForEntity(this.getCommandSource((ServerPlayerEntity) null), itextcomponent,
							(Entity) null, 0);
				} catch (CommandSyntaxException var6) {
					this.signText = itextcomponent;
				}
			} else {
				this.signText = itextcomponent;
			}
			this.renderText = null;
			
		}

		@Override
		public CompoundNBT write(CompoundNBT compound) {
			super.write(compound);

			String s = ITextComponent.Serializer.toJson(this.signText);
			compound.putString("Text", s);
			
			compound.putString("Color", this.textColor.getTranslationKey());
			return compound;
		}

		// lots of sign code coming up
		@OnlyIn(Dist.CLIENT)
		public ITextComponent getText() {
			return this.signText;
		}

		public void setText(ITextComponent p_212365_2_) {
			this.signText = p_212365_2_;
			this.renderText = null;
		}

		@Nullable
		@OnlyIn(Dist.CLIENT)
		public String getRenderText(Function<ITextComponent, String> p_212364_2_) {
			if (this.renderText == null && this.signText != null) {
				this.renderText = p_212364_2_.apply(this.signText);
			}
			//return this.renderText;
			return "Supplementaries";
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
			ITextComponent itextcomponent = this.signText;
			Style style = itextcomponent == null ? null : itextcomponent.getStyle();
			if (style != null && style.getClickEvent() != null) {
				ClickEvent clickevent = style.getClickEvent();
				if (clickevent.getAction() == ClickEvent.Action.RUN_COMMAND) {
					playerIn.getServer().getCommandManager().handleCommand(this.getCommandSource((ServerPlayerEntity) playerIn),
							clickevent.getValue());
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
				this.world.notifyBlockUpdate(this.getPos(), this.getBlockState(),this.getBlockState(), 3);
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

		public float getYaw(){
			return this.getBlockState().get(CustomBlock.ROTATION)*-22.5f;
			
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
			matrixStackIn.translate(0.5, 0.5, 0.5);
			matrixStackIn.rotate(Vector3f.YP.rotationDegrees(entityIn.getYaw()));
			matrixStackIn.translate(-0.5, -0.5, -0.5);
			//render block
			BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
			BlockState state = block.getDefaultState().with(BlockStateProperties.INVERTED, true);
			blockRenderer.renderBlock(state, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);
			matrixStackIn.translate(0.5, 0.5, 0.5);
			matrixStackIn.rotate(Vector3f.YP.rotationDegrees(-90));
			// render item
			
			// render text

			// sign code
			FontRenderer fontrenderer = this.renderDispatcher.getFontRenderer();
			int i = entityIn.getTextColor().getTextColor();
			double d0 = 0.4D;
			int j = (int) ((double) NativeImage.getRed(i) * 0.4D);
			int k = (int) ((double) NativeImage.getGreen(i) * 0.4D);
			int l = (int) ((double) NativeImage.getBlue(i) * 0.4D);
			int i1 = NativeImage.getCombined(0, l, k, j);


			matrixStackIn.translate(-0.0625, 0.15625, 0.1875 + 0.005);
			matrixStackIn.scale(0.010416667F, -0.010416667F, 0.010416667F);
			matrixStackIn.translate(0, 1, 0);
			
			String s = entityIn.getRenderText((p_212491_1_) -> {
				List<ITextComponent> list = RenderComponentsUtil.splitText(p_212491_1_, 75, fontrenderer, false, true);
				return list.isEmpty() ? "" : list.get(0).getFormattedText();
			});
			if (s != null) {
				float f3 = (float) (-fontrenderer.getStringWidth(s) / 2);
				fontrenderer.renderString(s, f3, (float) (- 5), i1, false,
						matrixStackIn.getLast().getMatrix(), bufferIn, false, 0, combinedLightIn);
			}
				

			
			
			matrixStackIn.pop();
		}
	}


	
}
