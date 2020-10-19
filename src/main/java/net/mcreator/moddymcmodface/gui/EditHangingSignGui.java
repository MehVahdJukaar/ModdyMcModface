
package net.mcreator.moddymcmodface.gui;

import org.lwjgl.opengl.GL11;

import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.IContainerFactory;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.World;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.network.PacketBuffer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Container;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.Minecraft;

import net.mcreator.moddymcmodface.ModdymcmodfaceModElements;
import net.mcreator.moddymcmodface.ModdymcmodfaceMod;
import net.mcreator.moddymcmodface.block.HangingSignBlock;
import net.mcreator.moddymcmodface.Network;


import java.util.function.Supplier;
import java.util.Map;
import java.util.HashMap;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.fonts.TextInputUtil;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.client.renderer.RenderHelper;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.Matrix4f;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.block.BlockState;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraft.client.renderer.Vector3d;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.client.gui.RenderComponentsUtil;
import java.util.List;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.network.play.client.CUpdateSignPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.logging.log4j.core.pattern.MaxLengthConverter;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TranslationTextComponent;

@ModdymcmodfaceModElements.ModElement.Tag
public class EditHangingSignGui extends ModdymcmodfaceModElements.ModElement {
	public EditHangingSignGui(ModdymcmodfaceModElements instance) {
		super(instance, 169);

		FMLJavaModLoadingContext.get().getModEventBus().register(this);
	}

	@OnlyIn(Dist.CLIENT)
	public void initElements() {
	}


	@OnlyIn(Dist.CLIENT)
	public static class GuiWindow extends Screen {
	
		private PlayerEntity entity;
	    private TextInputUtil textInputUtil;
	      /** The index of the line that is being edited. */
   		private int editLine = 0;
   		private int updateCounter;
   		private HangingSignBlock.CustomTileEntity tileSign = null;
   		
   		private static final int MAXLINES = 5;

		public GuiWindow(HangingSignBlock.CustomTileEntity teSign) {
			super(new TranslationTextComponent("sign.edit"));
			this.tileSign = teSign;
		}

		public static void open(HangingSignBlock.CustomTileEntity sign){
			Minecraft.getInstance().displayGuiScreen(new GuiWindow(sign));
		}

		@Override
		public boolean charTyped(char p_charTyped_1_, int p_charTyped_2_) {
			this.textInputUtil.func_216894_a(p_charTyped_1_);
			return true;
		}

		@Override
		public void onClose() {
			this.close();
		}
		
		@Override
		public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
			//up arrow
			if (p_keyPressed_1_ == 265) {
				this.editLine = Math.floorMod(this.editLine - 1, MAXLINES);
				this.textInputUtil.func_216899_b();
				return true;
			} 
			else if(p_keyPressed_1_ == 69|| true){
				
							MinecraftServer mcserv = ServerLifecycleHooks.getCurrentServer();
			if (mcserv != null)
				mcserv.getPlayerList().sendMessage(new StringTextComponent("Message"+this.textInputUtil.func_216897_a(p_keyPressed_1_)));

				return true;
			}
			//!down arrow, !enter, !enter
			else if (p_keyPressed_1_ != 264 && p_keyPressed_1_ != 257 && p_keyPressed_1_ != 335) {
				return this.textInputUtil.func_216897_a(p_keyPressed_1_) ? true : super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
			} 
			//down arrow, enter
			else {
				this.editLine = Math.floorMod(this.editLine + 1, MAXLINES);
				this.textInputUtil.func_216899_b();
				return true;
			}
		}

		@Override
		public void tick() {
			++this.updateCounter;
			if (!this.tileSign.getType().isValidBlock(this.tileSign.getBlockState().getBlock())) {
				this.close();
			}
			
		}
		

		@Override
		public void removed() {
			this.minecraft.keyboardListener.enableRepeatEvents(false);

			//send new text to the server
			Network.sendToServer( new Network.PackedUpdateServerHangingSign(this.tileSign.getPos(), this.tileSign.getText(0), 
				this.tileSign.getText(1), this.tileSign.getText(2), this.tileSign.getText(3), this.tileSign.getText(4)));
			
			this.tileSign.setEditable(true);
		}


		private void close() {
			this.tileSign.markDirty();
			this.minecraft.displayGuiScreen((Screen)null);
		}
	   
		@Override
		protected void init() {
			this.minecraft.keyboardListener.enableRepeatEvents(true);
			this.addButton(new Button(this.width / 2 - 100, this.height / 4 + 120, 200, 20, I18n.format("gui.done"), (p_214266_1_) -> {
				this.close();
				}));
			this.tileSign.setEditable(false);
			this.textInputUtil = new TextInputUtil(this.minecraft, () -> {
				return this.tileSign.getText(this.editLine).getString();
				}, (p_214265_1_) -> {
					this.tileSign.setText(this.editLine, new StringTextComponent(p_214265_1_));
				}, 75);
		}





		@Override
		@OnlyIn(Dist.CLIENT)
		public void render(int p_render_1_, int p_render_2_, float p_render_3_) {
			RenderHelper.setupGuiFlatDiffuseLighting();
			this.renderBackground();
			this.drawCenteredString(this.font, this.title.getFormattedText(), this.width / 2, 40, 16777215);
			
			
			MatrixStack matrixstack = new MatrixStack();
			
			IRenderTypeBuffer.Impl irendertypebuffer$impl = this.minecraft.getRenderTypeBuffers().getBufferSource();
			
			
			matrixstack.push();
			matrixstack.translate((double)(this.width / 2), 0.0D, 50.0D);
			float f = 93.75F;
			matrixstack.scale(93.75F, -93.75F, 93.75F);
			matrixstack.translate(0.0D, -1.3125D, 0.0D);
			
			//renders sign	
			
			matrixstack.push();
			
			//matrixstack.scale(0.6666667F, 0.6666667F, 0.6666667F);
			
			matrixstack.rotate(Vector3f.YP.rotationDegrees(90));
			matrixstack.translate(0, -0.25, -0.5);
			
			BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
			BlockState state = HangingSignBlock.block.getDefaultState().with(BlockStateProperties.INVERTED, true);
			blockRenderer.renderBlock(state, matrixstack, irendertypebuffer$impl, 15728880, OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE);

	     	matrixstack.pop();

	      
			boolean flag1 = this.updateCounter / 6 % 2 == 0;
	     
			float f2 = 0.010416667F;
			matrixstack.translate(0, 0, 0.0625 +0.005);
			matrixstack.scale(0.010416667F, -0.010416667F, 0.010416667F);
			int i = this.tileSign.getTextColor().getTextColor();
			String[] astring = new String[MAXLINES];
	
	      for(int j = 0; j < astring.length; ++j) {
	      		
	         astring[j] = this.tileSign.getRenderText(j, (p_228192_1_) -> {
	            List<ITextComponent> list = RenderComponentsUtil.splitText(p_228192_1_, 75, this.minecraft.fontRenderer, false, true);
	            return list.isEmpty() ? "" : list.get(0).getFormattedText();
	         });
	      }
	
	      Matrix4f matrix4f = matrixstack.getLast().getMatrix();
	      int k = this.textInputUtil.func_216896_c();
	      int l = this.textInputUtil.func_216898_d();
	      int i1 = this.minecraft.fontRenderer.getBidiFlag() ? -1 : 1;
	      int j1 = this.editLine * 10 - this.tileSign.signText.length * 5;
	
	      for(int k1 = 0; k1 < astring.length; ++k1) {
	         String s = astring[k1];
	         if (s != null) {
	            float f3 = (float)(-this.minecraft.fontRenderer.getStringWidth(s) / 2);
	            
	            this.minecraft.fontRenderer.renderString(s, f3, (float)(k1 * 10 - this.tileSign.signText.length * 5), i, false, matrix4f, irendertypebuffer$impl, false, 0, 15728880);
	            if (k1 == this.editLine && k >= 0 && flag1) {
	               int l1 = this.minecraft.fontRenderer.getStringWidth(s.substring(0, Math.max(Math.min(k, s.length()), 0)));
	               int i2 = (l1 - this.minecraft.fontRenderer.getStringWidth(s) / 2) * i1;
	               if (k >= s.length()) {
	                  this.minecraft.fontRenderer.renderString("_", (float)i2, (float)j1, i, false, matrix4f, irendertypebuffer$impl, false, 0, 15728880);
	               }
	            }
	         }
	      }
	
	      irendertypebuffer$impl.finish();
	
	      for(int k3 = 0; k3 < astring.length; ++k3) {
	         String s1 = astring[k3];
	         if (s1 != null && k3 == this.editLine && k >= 0) {
	            int l3 = this.minecraft.fontRenderer.getStringWidth(s1.substring(0, Math.max(Math.min(k, s1.length()), 0)));
	            int i4 = (l3 - this.minecraft.fontRenderer.getStringWidth(s1) / 2) * i1;
	            if (flag1 && k < s1.length()) {
	               fill(matrix4f, i4, j1 - 1, i4 + 1, j1 + 9, -16777216 | i);
	            }
	
	            if (l != k) {
	               int j4 = Math.min(k, l);
	               int j2 = Math.max(k, l);
	               int k2 = (this.minecraft.fontRenderer.getStringWidth(s1.substring(0, j4)) - this.minecraft.fontRenderer.getStringWidth(s1) / 2) * i1;
	               int l2 = (this.minecraft.fontRenderer.getStringWidth(s1.substring(0, j2)) - this.minecraft.fontRenderer.getStringWidth(s1) / 2) * i1;
	               int i3 = Math.min(k2, l2);
	               int j3 = Math.max(k2, l2);
	               Tessellator tessellator = Tessellator.getInstance();
	               BufferBuilder bufferbuilder = tessellator.getBuffer();
	               RenderSystem.disableTexture();
	               RenderSystem.enableColorLogicOp();
	               RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
	               bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
	               bufferbuilder.pos(matrix4f, (float)i3, (float)(j1 + 9), 0.0F).color(0, 0, 255, 255).endVertex();
	               bufferbuilder.pos(matrix4f, (float)j3, (float)(j1 + 9), 0.0F).color(0, 0, 255, 255).endVertex();
	               bufferbuilder.pos(matrix4f, (float)j3, (float)j1, 0.0F).color(0, 0, 255, 255).endVertex();
	               bufferbuilder.pos(matrix4f, (float)i3, (float)j1, 0.0F).color(0, 0, 255, 255).endVertex();
	               bufferbuilder.finishDrawing();
	               WorldVertexBufferUploader.draw(bufferbuilder);
	               RenderSystem.disableColorLogicOp();
	               RenderSystem.enableTexture();
	            }
	         }
	      }
	      matrixstack.pop();
	      RenderHelper.setupGui3DDiffuseLighting();
	      super.render(p_render_1_, p_render_2_, p_render_3_);
	   }



		
	}



}
