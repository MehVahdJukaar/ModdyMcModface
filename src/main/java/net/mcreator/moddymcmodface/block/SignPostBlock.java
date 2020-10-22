
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
import net.minecraft.util.ActionResultType;
import net.minecraft.item.ItemUseContext;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.Blocks;
import net.minecraft.util.SoundCategory;
import net.minecraft.block.BlockRenderType;
import net.minecraft.nbt.NBTUtil;

@ModdymcmodfaceModElements.ModElement.Tag
public class SignPostBlock extends ModdymcmodfaceModElements.ModElement {
	@ObjectHolder("moddymcmodface:sign_post")
	public static final Block block = null;
	@ObjectHolder("moddymcmodface:sign_post")
	public static final TileEntityType<CustomTileEntity> tileEntityType = null;
	@ObjectHolder("moddymcmodface:sign_post")
	public static final Item item = null;

	public SignPostBlock(ModdymcmodfaceModElements instance) {
		super(instance, 171);
		FMLJavaModLoadingContext.get().getModEventBus().register(this);
	}

	@Override
	public void initElements() {
		elements.blocks.add(() -> new CustomBlock());
		elements.items.add(() -> new ItemCustom());
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


	public static class ItemCustom extends Item {
		public ItemCustom() {
			super( new Item.Properties().group(ItemGroup.DECORATIONS).maxStackSize(64));
			setRegistryName("sign_post");
		}
		
		@Override
		public int getUseDuration(ItemStack itemstack) {
			return 0;
		}
		@Override 
		public ActionResultType onItemUse(ItemUseContext context) {
			//if (!context.canPlace()) return ActionResultType.FAIL;
			
			PlayerEntity playerentity = context.getPlayer();
			BlockPos blockpos = context.getPos();
			World world = context.getWorld();
			ItemStack itemstack = context.getItem();

			Block targetblock = world.getBlockState(blockpos).getBlock();

			boolean isfence = targetblock instanceof FenceBlock;
			boolean issignpost = targetblock instanceof CustomBlock;
			if(isfence || issignpost){
				
				world.setBlockState(blockpos, block.getDefaultState());

				boolean flag = false;

				TileEntity tileentity = world.getTileEntity(blockpos);
				if(tileentity instanceof CustomTileEntity){
					CustomTileEntity signtile = ((CustomTileEntity) tileentity);

				
					int r = Integer.valueOf(MathHelper.floor((double)((180.0F + context.getPlacementYaw()) * 16.0F / 360.0F) + 0.5D) & 15);
				
					double y = context.getHitVec().y;
					
					
					boolean up = y%((int)y) > 0.5d;


					if(up){
						if(signtile.up != up){
							signtile.up = true;
							signtile.yawUp = 90 + r*-22.5f;
							flag = true;
						}
					}
					else if(signtile.down != !up){
						signtile.down = true;
						signtile.yawDown = 90 + r*-22.5f;
						flag = true; 
					}
					if(flag && isfence)signtile.fenceblock = targetblock.getDefaultState();

				}

				if(flag){
					if(world.isRemote()){
						BlockState newstate = world.getBlockState(blockpos);
						SoundType soundtype = newstate.getSoundType(world, blockpos, playerentity);
						world.playSound(playerentity, blockpos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
					}
					if(!context.getPlayer().isCreative()) itemstack.shrink(1);
					return ActionResultType.SUCCESS;
				}
				

			}
		






			return ActionResultType.PASS;
		}


	}




	
	public static class CustomBlock extends Block {
		public static final BooleanProperty INVERTED = BlockStateProperties.INVERTED;
		public CustomBlock() {
			super(Block.Properties.create(Material.WOOD).sound(SoundType.WOOD).hardnessAndResistance(2f, 3f).lightValue(0).notSolid());
			this.setDefaultState(this.stateContainer.getBaseState().with(INVERTED, false));
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
		public BlockRenderType getRenderType(BlockState state){
			return state.get(INVERTED) ? super.getRenderType(state) : BlockRenderType.INVISIBLE;
		}
		
		@Override
		protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
			builder.add(INVERTED);
		}

		@Override
		public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
			List<ItemStack> dropsOriginal = super.getDrops(state, builder);
			if (!dropsOriginal.isEmpty())
				return dropsOriginal; //TODO:add drops
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
		
		public BlockState fenceblock = Blocks.OAK_FENCE.getDefaultState();
		public float yawUp = 0;
		public float yawDown = 0;
		public boolean leftUp = true;
		public boolean leftDown = true;
		public boolean up = false;
		public boolean down = false;
		
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

			this.fenceblock = NBTUtil.readBlockState(compound.getCompound("Fence"));
			this.yawUp = compound.getFloat("Yaw_up");
			this.yawDown = compound.getFloat("Yaw_down");
			this.leftUp = compound.getBoolean("Left_up");
			this.leftDown = compound.getBoolean("Left_down");
			this.up = compound.getBoolean("Up");
			this.down = compound.getBoolean("Down");
			
		}

		@Override
		public CompoundNBT write(CompoundNBT compound) {
			super.write(compound);

			String s = ITextComponent.Serializer.toJson(this.signText);
			compound.putString("Text", s);
			
			compound.putString("Color", this.textColor.getTranslationKey());
			compound.put("Fence", NBTUtil.writeBlockState(fenceblock));
			compound.putFloat("Yaw_up",this.yawUp);
			compound.putFloat("Yaw_down",this.yawDown);
			compound.putBoolean("Left_up",this.leftUp);
			compound.putBoolean("Left_down",this.leftDown);
			compound.putBoolean("Up", this.up);
			compound.putBoolean("Down", this.down);
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

		
	}


	public static class CustomRender extends TileEntityRenderer<CustomTileEntity> {
		public CustomRender(TileEntityRendererDispatcher rendererDispatcherIn) {
			super(rendererDispatcherIn);
		}

		@Override
		public void render(CustomTileEntity entityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
				int combinedOverlayIn) {
			
			BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
			
			BlockState fence = entityIn.fenceblock;
			blockRenderer.renderBlock(fence, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);	

			boolean up = entityIn.up;
			boolean down = entityIn.down;
			//render signs
			if(up||down){

				BlockState state = block.getDefaultState().with(BlockStateProperties.INVERTED, true);

				// sign code
				FontRenderer fontrenderer = this.renderDispatcher.getFontRenderer();
				int i = entityIn.getTextColor().getTextColor();
				double d0 = 0.4D;
				int j = (int) ((double) NativeImage.getRed(i) * 0.4D);
				int k = (int) ((double) NativeImage.getGreen(i) * 0.4D);
				int l = (int) ((double) NativeImage.getBlue(i) * 0.4D);
				int i1 = NativeImage.getCombined(0, l, k, j);

				matrixStackIn.push();
				matrixStackIn.translate(0.5, 0.5, 0.5);
				if(up){
					matrixStackIn.push();
					
					matrixStackIn.rotate(Vector3f.YP.rotationDegrees(entityIn.yawUp));
					matrixStackIn.translate(-0.5, -0.5, -0.5);
					blockRenderer.renderBlock(state, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);
					
					//text up
					matrixStackIn.translate(0.5, 0.5, 0.5);
					matrixStackIn.rotate(Vector3f.YP.rotationDegrees(-90));
					matrixStackIn.translate(-0.0625, 0.28125, 0.1875 + 0.005);
					matrixStackIn.scale(0.010416667F, -0.010416667F, 0.010416667F);
					matrixStackIn.translate(0, 1, 0);
					
					String s = entityIn.getRenderText((p_212491_1_) -> {
						List<ITextComponent> list = RenderComponentsUtil.splitText(p_212491_1_, 75, fontrenderer, false, true);
						return list.isEmpty() ? "" : list.get(0).getFormattedText();
					});
					if (s != null) {
						float f3 = (float) (-fontrenderer.getStringWidth(s) / 2);
						fontrenderer.renderString(s, f3, (float) (- 5), i1, false, matrixStackIn.getLast().getMatrix(), bufferIn, false, 0, combinedLightIn);
					}
					
					
					
					matrixStackIn.pop();
				}
				if(down){
					matrixStackIn.push();
					
					matrixStackIn.rotate(Vector3f.YP.rotationDegrees(entityIn.yawDown));
					matrixStackIn.translate(-0.5, -1, -0.5);
					blockRenderer.renderBlock(state, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);
					
					
					//text down
					matrixStackIn.translate(0.5, 0.5, 0.5);
					matrixStackIn.rotate(Vector3f.YP.rotationDegrees(-90));
					matrixStackIn.translate(-0.0625, 0.28125, 0.1875 + 0.005);
					matrixStackIn.scale(0.010416667F, -0.010416667F, 0.010416667F);
					matrixStackIn.translate(0, 1, 0);
					
					String s = entityIn.getRenderText((p_212491_1_) -> {
						List<ITextComponent> list = RenderComponentsUtil.splitText(p_212491_1_, 75, fontrenderer, false, true);
						return list.isEmpty() ? "" : list.get(0).getFormattedText();
					});
					if (s != null) {
						float f3 = (float) (-fontrenderer.getStringWidth(s) / 2);
						fontrenderer.renderString(s, f3, (float) (- 5), i1, false, matrixStackIn.getLast().getMatrix(), bufferIn, false, 0, combinedLightIn);
					}
					
					
					
					matrixStackIn.pop();
				}
		

					
			}
			
			
			matrixStackIn.pop();
		}
	}


	
}
