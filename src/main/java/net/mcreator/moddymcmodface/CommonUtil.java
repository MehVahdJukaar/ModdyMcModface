/**
 * This mod element is always locked. Enter your code in the methods below.
 * If you don't need some of these methods, you can remove them as they
 * are overrides of the base class ModdymcmodfaceModElements.ModElement.
 *
 * You can register new events in this class too.
 *
 * As this class is loaded into mod element list, it NEEDS to extend
 * ModElement class. If you remove this extend statement or remove the
 * constructor, the compilation will fail.
 *
 * If you want to make a plain independent class, create it using
 * Project Browser - New... and make sure to make the class
 * outside net.mcreator.moddymcmodface as this package is managed by MCreator.
 *
 * If you change workspace package, modid or prefix, you will need
 * to manually adapt this file to these changes or remake it.
*/
package net.mcreator.moddymcmodface;

import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.client.renderer.texture.NativeImage;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.item.FlintAndSteelItem;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.storage.loot.conditions.BlockStateProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.BooleanProperty;

@ModdymcmodfaceModElements.ModElement.Tag
public class CommonUtil extends ModdymcmodfaceModElements.ModElement {
	/**
	 * Do not remove this constructor
	 */
	public CommonUtil(ModdymcmodfaceModElements instance) {
		super(instance, 165);
	}

	@Override
	public void initElements() {
	}

	@Override
	public void init(FMLCommonSetupEvent event) {
	}

	@Override
	public void serverLoad(FMLServerStartingEvent event) {
	}

	//blockstate properties
	public static final BooleanProperty EXTENDING = BooleanProperty.create("extending");
	public static final IntegerProperty HOUR = IntegerProperty.create("hour", 0, 23);
	public static final IntegerProperty EXTENSION = IntegerProperty.create("extension", 0, 2);
	public static final IntegerProperty ROTATION = IntegerProperty.create("rotation", 0, 15); //multiply *22.5 to get yaw

	//renderer

	//centered on x,z. aligned on y=0
	public static void addCube(IVertexBuilder builder, MatrixStack matrixStackIn, float w, float h, TextureAtlasSprite sprite, int combinedLightIn,
			int color, float a, int combinedOverlayIn, boolean up, boolean down, boolean fakeshading, boolean flippedY) {
		int lu = combinedLightIn & '\uffff';
		int lv = combinedLightIn >> 16 & '\uffff'; // ok
		float atlasscaleU = sprite.getMaxU() - sprite.getMinU();
		float atlasscaleV = sprite.getMaxV() - sprite.getMinV();
		float minu = sprite.getMinU();
		float minv = sprite.getMinV();
		float maxu = minu + atlasscaleU * w;
		float maxv = minv + atlasscaleV * h;
		float maxv2 = minv + atlasscaleV * w;
		float r = (float) ((color >> 16 & 255)) / 255.0F;
		float g = (float) ((color >> 8 & 255)) / 255.0F;
		float b = (float) ((color >> 0 & 255)) / 255.0F;



		// float a = 1f;// ((color >> 24) & 0xFF) / 255f;
		// shading:
		
		float r8,g8,b8,r6,g6,b6,r5,g5,b5;

		r8 = r6 = r5 = r;
		g8 = g6 = g5 = g;
		b8 = b6 = b5 = b;

		if(fakeshading){
			// 80%: s,n
			r8 *= 0.8f;
			g8 *= 0.8f;
			b8 *= 0.8f;
			// 60%: e,w
			r6 *= 0.6f;
			g6 *= 0.6f;
			b6 *= 0.6f;
			// 50%: d
			r5 *= 0.5f;
			g5 *= 0.5f;
			b5 *= 0.5f;
		}

		float hw = w/2f;
		float hh = h/2f;

		// up
		if(up)
		addQuadTop(builder, matrixStackIn, -hw, h, hw, hw, h, -hw, minu, minv, maxu, maxv2, r, g, b, a, lu, lv);
		// down
		if(down)
		addQuadTop(builder, matrixStackIn, -hw, 0, -hw, hw, 0, hw, minu, minv, maxu, maxv2, r5, g5, b5, a, lu, lv);


		if(flippedY){
			float temp = minv;
			minv = maxv;
			maxv = temp;	
		}
		// south z+
		// x y z u v r g b a lu lv
		addQuadSide(builder, matrixStackIn, hw, 0, -hw, -hw, h, -hw, minu, minv, maxu, maxv, r8, g8, b8, a, lu, lv);
		// west
		addQuadSide(builder, matrixStackIn, -hw, 0, -hw, -hw, h, hw, minu, minv, maxu, maxv, r6, g6, b6, a, lu, lv);
		// north
		addQuadSide(builder, matrixStackIn, -hw, 0, hw, hw, h, hw, minu, minv, maxu, maxv, r8, g8, b8, a, lu, lv);
		// east
		addQuadSide(builder, matrixStackIn, hw, 0, hw, hw, h, -hw, minu, minv, maxu, maxv, r6, g6, b6, a, lu, lv);
	}


	public static void addDoubleQuadSide(IVertexBuilder builder, MatrixStack matrixStackIn, float x0, float y0, float z0, float x1, float y1, float z1, float u0, float v0, float u1, float v1, float r, float g,
			float b, float a, int lu, int lv){
		addQuadSide(builder, matrixStackIn, x0, y0, z0, x1, y1, z1, u0, v0, u1, v1, r, g, b, a, lu, lv);	
		addQuadSide(builder, matrixStackIn, x1, y0, z1, x0, y1, z0, u0, v0, u1, v1, r, g, b, a, lu, lv);
	}

	public static void addQuadSide(IVertexBuilder builder, MatrixStack matrixStackIn, float x0, float y0, float z0, float x1, float y1, float z1, float u0, float v0, float u1, float v1, float r, float g,
			float b, float a, int lu, int lv) {
		addVert(builder, matrixStackIn, x0, y0, z0, u0, v1, r, g, b, a, lu, lv);
		addVert(builder, matrixStackIn, x1, y0, z1, u1, v1, r, g, b, a, lu, lv);
		addVert(builder, matrixStackIn, x1, y1, z1, u1, v0, r, g, b, a, lu, lv);
		addVert(builder, matrixStackIn, x0, y1, z0, u0, v0, r, g, b, a, lu, lv);		
	}
	public static void addQuadSideF(IVertexBuilder builder, MatrixStack matrixStackIn, float x0, float y0, float z0, float x1, float y1, float z1, float u0, float v0, float u1, float v1, float r, float g,
			float b, float a, int lu, int lv) {
		addVert(builder, matrixStackIn, x0, y0, z0, u0, v0, r, g, b, a, lu, lv);
		addVert(builder, matrixStackIn, x1, y0, z1, u1, v0, r, g, b, a, lu, lv);
		addVert(builder, matrixStackIn, x1, y1, z1, u1, v1, r, g, b, a, lu, lv);
		addVert(builder, matrixStackIn, x0, y1, z0, u0, v1, r, g, b, a, lu, lv);		
	}

	public static void addQuadTop(IVertexBuilder builder, MatrixStack matrixStackIn, float x0, float y0, float z0, float x1, float y1, float z1, float u0, float v0, float u1, float v1, float r, float g,
			float b, float a, int lu, int lv) {
		addVert(builder, matrixStackIn, x0, y0, z0, u0, v1, r, g, b, a, lu, lv);
		addVert(builder, matrixStackIn, x1, y0, z0, u1, v1, r, g, b, a, lu, lv);
		addVert(builder, matrixStackIn, x1, y1, z1, u1, v0, r, g, b, a, lu, lv);
		addVert(builder, matrixStackIn, x0, y1, z1, u0, v0, r, g, b, a, lu, lv);		
	}

	public static void addVert(IVertexBuilder builder, MatrixStack matrixStackIn, float x, float y, float z, float u, float v, float r, float g,
			float b, float a, int lu, int lv) {
		builder.pos(matrixStackIn.getLast().getMatrix(), x, y, z).color(r, g, b, a).tex(u, v).overlay(OverlayTexture.NO_OVERLAY).lightmap(lu, lv)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
	}
	

	
/*
 * 
 * 
 * 		addQuadSide(builder, matrixStackIn, 1, 0, -hw, 0, 1, -hw, minu, minv, maxu, maxv, r8, g8, b8, a, lu, lv);
		// west
		addQuadSide(builder, matrixStackIn, 0, 0, 0, 0, 1, 1, minu, minv, maxu, maxv, r8, g8, b8, a, lu, lv);
		// north
		addQuadSide(builder, matrixStackIn, 0, 0, 1, 1, 1, 1, minu, minv, maxu, maxv, r8, g8, b8, a, lu, lv);
		// east
		addQuadSide(builder, matrixStackIn, 1, 0, 1, 1, 1, 0, minu, minv, maxu, maxv, r8, g8, b8, a, lu, lv);
		// down
		addQuadTop(builder, matrixStackIn, 0, 1, 1, 1, 1, 0, minu, minv, maxu, maxv, r8, g8, b8, a, lu, lv);
		// up
		addQuadTop(builder, matrixStackIn, 0, 0, 0, 1, 0, 1, minu, minv, maxu, maxv, r8, g8, b8, a, lu, lv);
		
	public static abstract class RenderableInventoryTileEntity extends LockableLootTileEntity{
				//receive new inv from server, then update tile
		public void updateInventoryFromServer(ItemStack stack){
			ItemStack newstack = stack.copy();
			NonNullList<ItemStack> stacks = NonNullList.<ItemStack>withSize(1, newstack);
			this.setItems(stacks);
			this.updateTile();
		}

		//hijacking this method to work with hoppers
		@Override
		public void markDirty() {
			this.updateServerAndClient();
			super.markDirty();
		}

		
		private void updateServerAndClient() {
			if (this.world instanceof World && !this.world.isRemote()) {
				Network.sendToAllNear(this.pos.getX(), this.pos.getY(), this.pos.getZ(), 128, this.world.getDimension().getType(),
							new Network.PacketUpdateNoticeBoard(this.pos, this.getStackInSlot(0)));
				this.updateTile();
			}
		}
	}*/
	
}
