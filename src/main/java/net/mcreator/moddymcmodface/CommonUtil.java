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
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.Vector3f;

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
	//renderer

	//centered on x,z. aligned on y=0
	@OnlyIn(Dist.CLIENT)
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

	@OnlyIn(Dist.CLIENT)
	public static void addDoubleQuadSide(IVertexBuilder builder, MatrixStack matrixStackIn, float x0, float y0, float z0, float x1, float y1, float z1, float u0, float v0, float u1, float v1, float r, float g,
			float b, float a, int lu, int lv){
		addQuadSide(builder, matrixStackIn, x0, y0, z0, x1, y1, z1, u0, v0, u1, v1, r, g, b, a, lu, lv);	
		addQuadSide(builder, matrixStackIn, x1, y0, z1, x0, y1, z0, u0, v0, u1, v1, r, g, b, a, lu, lv);
	}

	@OnlyIn(Dist.CLIENT)
	public static void addQuadSide(IVertexBuilder builder, MatrixStack matrixStackIn, float x0, float y0, float z0, float x1, float y1, float z1, float u0, float v0, float u1, float v1, float r, float g,
			float b, float a, int lu, int lv) {
		addVert(builder, matrixStackIn, x0, y0, z0, u0, v1, r, g, b, a, lu, lv);
		addVert(builder, matrixStackIn, x1, y0, z1, u1, v1, r, g, b, a, lu, lv);
		addVert(builder, matrixStackIn, x1, y1, z1, u1, v0, r, g, b, a, lu, lv);
		addVert(builder, matrixStackIn, x0, y1, z0, u0, v0, r, g, b, a, lu, lv);		
	}
	
	@OnlyIn(Dist.CLIENT)
	public static void addQuadSideF(IVertexBuilder builder, MatrixStack matrixStackIn, float x0, float y0, float z0, float x1, float y1, float z1, float u0, float v0, float u1, float v1, float r, float g,
			float b, float a, int lu, int lv) {
		addVert(builder, matrixStackIn, x0, y0, z0, u0, v0, r, g, b, a, lu, lv);
		addVert(builder, matrixStackIn, x1, y0, z1, u1, v0, r, g, b, a, lu, lv);
		addVert(builder, matrixStackIn, x1, y1, z1, u1, v1, r, g, b, a, lu, lv);
		addVert(builder, matrixStackIn, x0, y1, z0, u0, v1, r, g, b, a, lu, lv);		
	}

	@OnlyIn(Dist.CLIENT)
	public static void addQuadTop(IVertexBuilder builder, MatrixStack matrixStackIn, float x0, float y0, float z0, float x1, float y1, float z1, float u0, float v0, float u1, float v1, float r, float g,
			float b, float a, int lu, int lv) {
		addVert(builder, matrixStackIn, x0, y0, z0, u0, v1, r, g, b, a, lu, lv);
		addVert(builder, matrixStackIn, x1, y0, z0, u1, v1, r, g, b, a, lu, lv);
		addVert(builder, matrixStackIn, x1, y1, z1, u1, v0, r, g, b, a, lu, lv);
		addVert(builder, matrixStackIn, x0, y1, z1, u0, v0, r, g, b, a, lu, lv);		
	}

	@OnlyIn(Dist.CLIENT)
	public static void addVert(IVertexBuilder builder, MatrixStack matrixStackIn, float x, float y, float z, float u, float v, float r, float g,
			float b, float a, int lu, int lv) {
		builder.pos(matrixStackIn.getLast().getMatrix(), x, y, z).color(r, g, b, a).tex(u, v).overlay(OverlayTexture.NO_OVERLAY).lightmap(lu, lv)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
	}

	@OnlyIn(Dist.CLIENT)
	public static void renderFish(IVertexBuilder builder, MatrixStack matrixStackIn, float wo, float ho, int fishType, int combinedLightIn,
			int combinedOverlayIn) {
		ResourceLocation texture = new ResourceLocation("moddymcmodface:blocks/jar_fishes");
		TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(texture);
		float w = 5 / 16f;
		float h = 4 / 16f;
		float hw = w / 2f;
		float hh = h / 2f;
		int lu = combinedLightIn & '\uffff';
		int lv = combinedLightIn >> 16 & '\uffff';
		float atlasscaleU = sprite.getMaxU() - sprite.getMinU();
		float atlasscaleV = sprite.getMaxV() - sprite.getMinV();
		float minu = sprite.getMinU();
		float maxv = sprite.getMinV() + atlasscaleV * fishType * h;
		float maxu = atlasscaleU * w + minu;
		float minv = atlasscaleV * h + maxv;
		for (int j = 0; j < 2; j++) {
			CommonUtil.addVert(builder, matrixStackIn, hw - Math.abs(wo / 2), -hh + ho, +wo, minu, minv, 1, 1, 1, 1, lu, lv);
			CommonUtil.addVert(builder, matrixStackIn, -hw + Math.abs(wo / 2), -hh + ho, -wo, maxu, minv, 1, 1, 1, 1, lu, lv);
			CommonUtil.addVert(builder, matrixStackIn, -hw + Math.abs(wo / 2), hh + ho, -wo, maxu, maxv, 1, 1, 1, 1, lu, lv);
			CommonUtil.addVert(builder, matrixStackIn, hw - Math.abs(wo / 2), hh + ho, +wo, minu, maxv, 1, 1, 1, 1, lu, lv);
			matrixStackIn.rotate(Vector3f.YP.rotationDegrees(180));
			float temp = minu;
			minu = maxu;
			maxu = temp;
		}
	}

	
	
}
