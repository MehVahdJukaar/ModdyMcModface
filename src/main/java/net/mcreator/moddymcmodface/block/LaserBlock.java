
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

import net.minecraftforge.registries.ObjectHolder;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.storage.loot.LootParameters;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.World;
import net.minecraft.world.IBlockReader;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Hand;
import net.minecraft.util.Direction;
import net.minecraft.util.ActionResultType;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.state.StateContainer;
import net.minecraft.state.IntegerProperty;
import net.minecraft.potion.Potions;
import net.minecraft.potion.PotionUtils;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.network.NetworkManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.item.PotionItem;
import net.minecraft.item.MilkBucketItem;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.HoneyBottleItem;
import net.minecraft.item.ExperienceBottleItem;
import net.minecraft.item.BlockItem;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.block.material.PushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.SoundType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Block;

import net.mcreator.moddymcmodface.ModdymcmodfaceModElements;

import javax.annotation.Nullable;

import java.util.stream.IntStream;
import java.util.List;
import java.util.Collections;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraftforge.client.event.TextureStitchEvent;
import java.awt.TextComponent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.item.BucketItem;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import java.util.concurrent.Callable;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.block.Blocks;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraft.item.GlassBottleItem;
import net.minecraft.block.GlassBlock;
import net.minecraft.world.gen.feature.ShrubFeature;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.stats.Stats;
import sun.reflect.generics.tree.BottomSignature;
import sun.reflect.generics.tree.BottomSignature;
import net.minecraft.util.SoundEvent;

import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.World;
import net.minecraft.world.IBlockReader;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.ITextComponent;
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
import net.minecraft.block.DirectionalBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Block;
import net.minecraftforge.registries.ObjectHolder;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.World;
import net.minecraft.world.IBlockReader;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Direction;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.StateContainer;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.BooleanProperty;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.network.NetworkManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.BlockItem;
import net.minecraft.entity.Entity;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.block.material.PushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.SoundType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Block;

import net.mcreator.moddymcmodface.ModdymcmodfaceModElements;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.color.IBlockColor;
import javax.annotation.Nullable;
import net.minecraft.world.ILightReader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import net.mcreator.moddymcmodface.ModdymcmodfaceModElements;

import javax.annotation.Nullable;

import java.util.stream.IntStream;
import java.util.List;
import java.util.Collections;
import net.minecraft.entity.ai.brain.task.UpdateActivityTask;
import net.minecraft.util.math.AxisAlignedBB;

@ModdymcmodfaceModElements.ModElement.Tag
public class LaserBlock extends ModdymcmodfaceModElements.ModElement {
	@ObjectHolder("moddymcmodface:laser")
	public static final Block block = null;
	@ObjectHolder("moddymcmodface:laser")
	public static final TileEntityType<CustomTileEntity> tileEntityType = null;
	public LaserBlock(ModdymcmodfaceModElements instance) {
		super(instance, 113);
		FMLJavaModLoadingContext.get().getModEventBus().register(this);
	}

	@Override
	public void initElements() {
		elements.blocks.add(() -> new CustomBlock());
		elements.items.add(() -> new BlockItem(block, new Item.Properties().group(ItemGroup.REDSTONE)).setRegistryName(block.getRegistryName()));
	}

	@SubscribeEvent
	public void registerTileEntity(RegistryEvent.Register<TileEntityType<?>> event) {
		event.getRegistry().register(TileEntityType.Builder.create(CustomTileEntity::new, block).build(null).setRegistryName("laser"));
	}


	@Override
	@OnlyIn(Dist.CLIENT)
	public void clientLoad(FMLClientSetupEvent event) {
		//RenderTypeLookup.setRenderLayer(block, RenderType.getCutout());
		ClientRegistry.bindTileEntityRenderer(tileEntityType, CustomRender::new);
	}




	
	public static class CustomBlock extends Block {
		public static final DirectionProperty FACING = DirectionalBlock.FACING;
		public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
		public static final BooleanProperty RECEIVING =  BooleanProperty.create("laser_receiving"); //it's dececting incoming laser

		
		public CustomBlock() {
			super(Block.Properties.create(Material.ROCK).sound(SoundType.STONE).hardnessAndResistance(3.5f, 3.5f).lightValue(0).notSolid());
			this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.NORTH).with(RECEIVING, false).with(POWERED,false));
			setRegistryName("laser");
		}

		@Override
		public boolean isNormalCube(BlockState state, IBlockReader worldIn, BlockPos pos) {
			return false;
		}


		@Override
		protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
			builder.add(FACING,POWERED,RECEIVING);
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
		public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
			List<ItemStack> dropsOriginal = super.getDrops(state, builder);
			if (!dropsOriginal.isEmpty())
				return dropsOriginal;
			return Collections.singletonList(new ItemStack(this, 1));
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
		public BlockPos endpos; //block that the laser is touching
		public int lenght = 0;
		public Direction dir = Direction.NORTH;
		protected CustomTileEntity() {
			super(tileEntityType);
		}

		//TODO:cache the blockposition on a list for faster accsssing
		public void updateBeam(){
			int maxrange =6;
			BlockPos p =this.pos;
			this.dir = this.getBlockState().get(CustomBlock.FACING);
			int i=0;
			for(i=0; i<=maxrange; i++){
				p = this.pos.offset(this.dir,i+1);
				BlockState state = this.world.getBlockState(p);
				if(state.getOpacity(this.world, p) < 15) continue;
				if(state.isSolidSide(world, p, this.dir.getOpposite()))break;
			}
			this.lenght = i;
			
			this.world.setBlockState(p, Blocks.DIAMOND_ORE.getDefaultState(),3);

			
		}

		@Override
		public AxisAlignedBB getRenderBoundingBox(){
			return new AxisAlignedBB(getPos(), getPos().offset(this.dir,this.lenght+2).add(1,1,1));
		}


		
		@Override
		public void tick(){
			if (this.world != null && this.world.getGameTime() % 20L == 0L) {
				this.updateBeam();
			}
		}

		@Override
		public void read(CompoundNBT compound) {
			super.read(compound);
		}

		@Override
		public CompoundNBT write(CompoundNBT compound) {
			super.write(compound);
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


		@Override
		public void remove() {
			super.remove();

		}

		public Direction getDirection(){
			return this.getBlockState().get(CustomBlock.FACING);
		}

		
	}
	
	
	
	
	
	

	@OnlyIn(Dist.CLIENT)
	public static class CustomRender extends TileEntityRenderer<CustomTileEntity> {
		public CustomRender(TileEntityRendererDispatcher rendererDispatcherIn) {
			super(rendererDispatcherIn);
		}

		// shaded rectangle with wx = wz with texture flipped vertically. starts from
		// block 0,0,0

		@Override
		public void render(CustomTileEntity entityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
				int combinedOverlayIn) {
			ResourceLocation texture = new ResourceLocation("moddymcmodface:blocks/blaze_block");
			TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(texture);
			//IVertexBuilder builder = bufferIn.getBuffer(RenderType.getTranslucent());
			IVertexBuilder builder = bufferIn.getBuffer(RenderType.getBeaconBeam(texture, true));
			// IVertexBuilder builder
			// =bufferIn.getBuffer(Customrender.CustomRenderTypes.TRANSLUCENT_CUSTOM);
			int color =0xff0000;
			float opacity = 0.4f;
			float height = 1;

			int lenght = entityIn.lenght;

			Direction dir = entityIn.getDirection();
			float yaw = dir.getHorizontalAngle();
			float pitch =0;
			if (dir==Direction.UP) pitch=90f;
			else if (dir==Direction.DOWN) pitch=-90f;
		
			matrixStackIn.push();
			matrixStackIn.translate(0.5, 0.5, 0.5);
			matrixStackIn.rotate(dir.getRotation());
			matrixStackIn.translate(-0.5, -0.5, -0.5);
			
			matrixStackIn.translate(0.25, 0, 0.25);

			//matrixStackIn.translate(0, 1, 0);
			for(int i=0; i<lenght; i++){
				matrixStackIn.translate(0, 1, 0);
				addCube(builder, matrixStackIn, 0.5f, height, sprite, combinedLightIn, color, opacity, combinedOverlayIn, i==lenght-1);
			}
			
			matrixStackIn.pop();
		}
	}





	private static void addCube(IVertexBuilder builder, MatrixStack matrixStackIn, float w, float h, TextureAtlasSprite sprite, int combinedLightIn,
			int color, float a, int combinedOverlayIn, boolean end) {
		int lu = combinedLightIn & '\uffff';
		int lv = combinedLightIn >> 16 & '\uffff'; // ok
		float minu = sprite.getMinU();
		float minv = sprite.getMinV();
		float maxu = (sprite.getMaxU() - minu) * w + minu;
		float maxv = (sprite.getMaxV() - minv) * h + minv;
		float maxv2 = (sprite.getMaxV() - minv) * w + minv;
		float r = (float) ((color >> 16 & 255)) / 255.0F;
		float g = (float) ((color >> 8 & 255)) / 255.0F;
		float b = (float) ((color >> 0 & 255)) / 255.0F;
		// float a = 1f;// ((color >> 24) & 0xFF) / 255f;
	
		// south z+
		// x y z u v r g b a lu lv
		addVert(builder, matrixStackIn, 0, 0, w, minu, minv, r, g, b, a, lu, lv, 0, 0, 1);
		addVert(builder, matrixStackIn, w, 0, w, maxu, minv, r, g, b, a, lu, lv, 0, 0, 1);
		addVert(builder, matrixStackIn, w, h, w, maxu, maxv, r, g, b, a, lu, lv, 0, 0, 1);
		addVert(builder, matrixStackIn, 0, h, w, minu, maxv, r, g, b, a, lu, lv, 0, 0, 1);
		// west
		addVert(builder, matrixStackIn, 0, 0, 0, minu, minv, r, g, b, a, lu, lv, -1, 0, 0);
		addVert(builder, matrixStackIn, 0, 0, w, maxu, minv, r, g, b, a, lu, lv, -1, 0, 0);
		addVert(builder, matrixStackIn, 0, h, w, maxu, maxv, r, g, b, a, lu, lv, -1, 0, 0);
		addVert(builder, matrixStackIn, 0, h, 0, minu, maxv, r, g, b, a, lu, lv, -1, 0, 0);
		// north
		addVert(builder, matrixStackIn, w, 0, 0, minu, minv, r, g, b, a, lu, lv, 0, 0, -1);
		addVert(builder, matrixStackIn, 0, 0, 0, maxu, minv, r, g, b, a, lu, lv, 0, 0, -1);
		addVert(builder, matrixStackIn, 0, h, 0, maxu, maxv, r, g, b, a, lu, lv, 0, 0, -1);
		addVert(builder, matrixStackIn, w, h, 0, minu, maxv, r, g, b, a, lu, lv, 0, 0, -1);
		// east
		addVert(builder, matrixStackIn, w, 0, w, minu, minv, r, g, b, a, lu, lv, 1, 0, 0);
		addVert(builder, matrixStackIn, w, 0, 0, maxu, minv, r, g, b, a, lu, lv, 1, 0, 0);
		addVert(builder, matrixStackIn, w, h, 0, maxu, maxv, r, g, b, a, lu, lv, 1, 0, 0);
		addVert(builder, matrixStackIn, w, h, w, minu, maxv, r, g, b, a, lu, lv, 1, 0, 0);
		if(end){
			// up
			addVert(builder, matrixStackIn, 0, h, w, minu, minv, r, g, b, a, lu, lv, 0, 1, 0);
			addVert(builder, matrixStackIn, w, h, w, maxu, minv, r, g, b, a, lu, lv, 0, 1, 0);
			addVert(builder, matrixStackIn, w, h, 0, maxu, maxv2, r, g, b, a, lu, lv, 0, 1, 0);
			addVert(builder, matrixStackIn, 0, h, 0, minu, maxv2, r, g, b, a, lu, lv, 0, 1, 0);
		}
	}

	private static void addVert(IVertexBuilder builder, MatrixStack matrixStackIn, float x, float y, float z, float u, float v, float r, float g,
			float b, float a, int lu, int lv, float dx, float dy, float dz) {
		builder.pos(matrixStackIn.getLast().getMatrix(), x, y, z).color(r, g, b, a).tex(u, v).overlay(OverlayTexture.NO_OVERLAY).lightmap(lu, lv)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
	}	
			
	
}
