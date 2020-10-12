
package net.mcreator.moddymcmodface.block;

import net.minecraftforge.registries.ObjectHolder;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.World;
import net.minecraft.world.IBlockReader;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.network.NetworkManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.BlockItem;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.Matrix3f;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.block.material.Material;
import net.minecraft.block.SoundType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Block;

import net.mcreator.moddymcmodface.entity.FireflyEntity;
import net.mcreator.moddymcmodface.Particles;
import net.mcreator.moddymcmodface.ModdymcmodfaceModElements;

import java.util.concurrent.Callable;
import java.util.Random;
import java.util.List;
import java.util.Collections;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraft.server.MinecraftServer;

@ModdymcmodfaceModElements.ModElement.Tag
public class FireflyJarBlock extends ModdymcmodfaceModElements.ModElement {
	@ObjectHolder("moddymcmodface:firefly_jar")
	public static final Block block = null;
	@ObjectHolder("moddymcmodface:firefly_jar")
	public static final TileEntityType<CustomTileEntity> tileEntityType = null;
	public FireflyJarBlock(ModdymcmodfaceModElements instance) {
		super(instance, 144);
		FMLJavaModLoadingContext.get().getModEventBus().register(this);
	}

	@Override
	public void initElements() {
		elements.blocks.add(() -> new CustomBlock());
		elements.items.add(() -> new BlockItem(block, new Item.Properties().setISTER(ISTERProvider::CustomISTER).group(ItemGroup.DECORATIONS))
				.setRegistryName(block.getRegistryName()));
	}

	@SubscribeEvent
	public void registerTileEntity(RegistryEvent.Register<TileEntityType<?>> event) {
		event.getRegistry().register(TileEntityType.Builder.create(CustomTileEntity::new, block).build(null).setRegistryName("firefly_jar"));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void clientLoad(FMLClientSetupEvent event) {
		RenderTypeLookup.setRenderLayer(block, RenderType.getCutoutMipped());
	}
	public static class CustomBlock extends Block {
		public CustomBlock() {
			super(Block.Properties.create(Material.GLASS).sound(SoundType.GLASS).hardnessAndResistance(1f, 10f).lightValue(8).notSolid());
			setRegistryName("firefly_jar");
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
			return VoxelShapes.or(VoxelShapes.create(0.1875D, 0D, 0.1875D, 0.8125D, 0.875D, 0.8125D),
					VoxelShapes.create(0.3125, 0.875, 0.3125, 0.6875, 1, 0.6875));
		}

		@Override
		public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
			List<ItemStack> dropsOriginal = super.getDrops(state, builder);
			if (!dropsOriginal.isEmpty())
				return dropsOriginal;
			return Collections.singletonList(new ItemStack(this, 1));
		}

		@OnlyIn(Dist.CLIENT)
		@Override
		public void animateTick(BlockState state, World world, BlockPos pos, Random random) {
			super.animateTick(state, world, pos, random);
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

	public static class CustomTileEntity extends TileEntity implements ITickableTileEntity {
		protected final Random rand = new Random();
		protected CustomTileEntity() {
			super(tileEntityType);
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

		public void tick() {
			if (this.world.isRemote() && this.world.getGameTime() % 8L == 0L) {
				int x = this.pos.getX();
				int y = this.pos.getY();
				int z = this.pos.getZ();
				double pr = 0.15d;
				if (this.rand.nextFloat() > 0.6f) {
					for (int l = 0; l < 1; ++l) {
						double d0 = (x + 0.5 + (this.rand.nextFloat() - 0.5) * (0.625D - pr));
						double d1 = (y + 0.5 - 0.0625 + (this.rand.nextFloat() - 0.5) * (0.875D - pr));
						double d2 = (z + 0.5 + (this.rand.nextFloat() - 0.5) * (0.625D - pr));
						world.addParticle(Particles.ParticleList.FIREFLY_GLOW.get(), d0, d1, d2, 0, 0, 0);
					}
				}
			}
		}
	}

	public static class ISTERProvider {
		public static Callable<ItemStackTileEntityRenderer> CustomISTER() {
			return CustomItemRender::new;
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static class CustomItemRender extends ItemStackTileEntityRenderer {
		private static final ResourceLocation texture = new ResourceLocation("moddymcmodface:textures/firefly.png");

		//private static final ResourceLocation texture = new ResourceLocation("moddymcmodface:particles/firefly_glow");
		@Override
		public void render(ItemStack stack, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
			matrixStackIn.push();
			BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
			
			BlockState state = FireflyJarBlock.block.getDefaultState();
			blockRenderer.renderBlock(state, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);
			
			
			int j = 255;
			int k = 255;
			int l = 255;
			int a = 255;
			EntityRendererManager renderManager = Minecraft.getInstance().getRenderManager();
		
			matrixStackIn.translate(0.5, 0.5, 0.5);
			matrixStackIn.translate(0, -0.1, 0);
			//matrixStackIn.rotate(renderManager.getCameraOrientation());

			//renderManager.renderEntityStatic(new FireflyEntity.CustomEntity(FireflyEntity.entity, (World)Minecraft.getInstance().world), 0d, 0d, 0d, 0f, 1f, matrixStackIn, bufferIn, combinedLightIn);

			//TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_PARTICLES_TEXTURE).apply(texture);
			matrixStackIn.scale(0.6f, 0.6f, 0.6f);


			//matrixStackIn.rotate(Vector3f.YP.rotationDegrees(180.0F));
			float f9 = 0.32F;
			//matrixStackIn.scale(0.3F, 0.3F, 0.3F);

			float minu = 0;//sprite.getMinU();
			float minv = 0;//sprite.getMinV();
			float maxu = 1;//sprite.getMaxU();
			float maxv = 1;//sprite.getMaxV();

			//IVertexBuilder ivertexbuilder = bufferIn.getBuffer(RenderType.getCutout());
			IVertexBuilder ivertexbuilder = bufferIn.getBuffer(RenderType.getEntityTranslucent(texture));
			MatrixStack.Entry matrixstack$entry = matrixStackIn.getLast();
			Matrix4f matrix4f = matrixstack$entry.getMatrix();
			Matrix3f matrix3f = matrixstack$entry.getNormal();
			matrixStackIn.rotate(Vector3f.YP.rotationDegrees(45));
			for (int i =0; i<4; i++){
				vertex(ivertexbuilder, matrix4f, matrix3f, -0.5F, -0.5F, j, k, l, a, minu, minv, combinedLightIn);
				vertex(ivertexbuilder, matrix4f, matrix3f, 0.5F, -0.5F, j, k, l, a, maxu, minv, combinedLightIn);
				vertex(ivertexbuilder, matrix4f, matrix3f, 0.5F, 0.5F, j, k, l, a, maxu, maxv, combinedLightIn);
				vertex(ivertexbuilder, matrix4f, matrix3f, -0.5F, 0.5F, j, k, l, a, minu, maxv, combinedLightIn);
				matrixStackIn.rotate(Vector3f.YP.rotationDegrees(90));
			}
			matrixStackIn.pop();
		}

		private static void vertex(IVertexBuilder bufferIn, Matrix4f matrixIn, Matrix3f matrixNormalIn, float x, float y, int red, int green,
				int blue, int alpha, float texU, float texV, int packedLight) {
			bufferIn.pos(matrixIn, x, y, 0.0F).color(red, green, blue, alpha).tex(texU, texV).overlay(OverlayTexture.NO_OVERLAY).lightmap(240, 0)
					.normal(matrixNormalIn, 0.0F, 1.0F, 0.0F).endVertex();
		}
	}
}
