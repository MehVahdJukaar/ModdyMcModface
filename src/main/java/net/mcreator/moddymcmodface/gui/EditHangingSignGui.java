
package net.mcreator.moddymcmodface.gui;

import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.server.MinecraftServer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.fonts.TextInputUtil;
import net.minecraft.client.gui.RenderComponentsUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.block.BlockState;

import net.mcreator.moddymcmodface.ModdymcmodfaceMod;
import net.mcreator.moddymcmodface.block.HangingSignBlock;
import net.mcreator.moddymcmodface.Network;
import net.mcreator.moddymcmodface.ModdymcmodfaceModElements;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.network.PacketBuffer;
import java.util.function.Supplier;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraft.world.World;
import net.minecraft.tileentity.TileEntity;

@ModdymcmodfaceModElements.ModElement.Tag
public class EditHangingSignGui extends ModdymcmodfaceModElements.ModElement {
	public EditHangingSignGui(ModdymcmodfaceModElements instance) {
		super(instance, 169);

		elements.addNetworkMessage(PackedUpdateServerHangingSign.class, PackedUpdateServerHangingSign::buffer, PackedUpdateServerHangingSign::new,
				PackedUpdateServerHangingSign::handler);

		FMLJavaModLoadingContext.get().getModEventBus().register(this);
	}

	@OnlyIn(Dist.CLIENT)
	public void initElements() {
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class GuiWindow extends Screen {
		private PlayerEntity entity;
		private TextInputUtil textInputUtil;
		// The index of the line that is being edited.
		private int editLine = 0;
		//for ticking cusros
		private int updateCounter;
		private HangingSignBlock.CustomTileEntity tileSign = null;
		private static final int MAXLINES = 5;
		public GuiWindow(HangingSignBlock.CustomTileEntity teSign) {
			super(new TranslationTextComponent("sign.edit"));
			this.tileSign = teSign;
		}

		public static void open(HangingSignBlock.CustomTileEntity sign) {
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
			// up arrow
			if (p_keyPressed_1_ == 265) {
				this.editLine = Math.floorMod(this.editLine - 1, MAXLINES);
				this.textInputUtil.func_216899_b();
				return true;
			}
			// !down arrow, !enter, !enter, handles special keys
			else if (p_keyPressed_1_ != 264 && p_keyPressed_1_ != 257 && p_keyPressed_1_ != 335) {
				return this.textInputUtil.func_216897_a(p_keyPressed_1_) ? true : super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
			}
			// down arrow, enter
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
			// send new text to the server
			ModdymcmodfaceMod.PACKET_HANDLER.sendToServer(new PackedUpdateServerHangingSign(this.tileSign.getPos(), this.tileSign.getText(0), this.tileSign.getText(1),
				this.tileSign.getText(2), this.tileSign.getText(3), this.tileSign.getText(4)));
			
			this.tileSign.setEditable(true);
		}

		private void close() {
			this.tileSign.markDirty();
			this.minecraft.displayGuiScreen((Screen) null);
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
			matrixstack.translate((double) (this.width / 2), 0.0D, 50.0D);
			float f = 93.75F;
			matrixstack.scale(93.75F, -93.75F, 93.75F);
			matrixstack.translate(0.0D, -1.3125D, 0.0D);
			// renders sign
			matrixstack.push();
			// matrixstack.scale(0.6666667F, 0.6666667F, 0.6666667F);
			matrixstack.rotate(Vector3f.YP.rotationDegrees(90));
			matrixstack.translate(0, - 0.5 + 0.1875, -0.5);
			BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
			BlockState state = HangingSignBlock.block.getDefaultState().with(BlockStateProperties.INVERTED, true);
			blockRenderer.renderBlock(state, matrixstack, irendertypebuffer$impl, 15728880, OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE);
			matrixstack.pop();

			//renders text
			boolean flag1 = this.updateCounter / 6 % 2 == 0;

			matrixstack.translate(0, 0, 0.0625 + 0.005);
			matrixstack.scale(0.010416667F, -0.010416667F, 0.010416667F);
			
			int i = this.tileSign.getTextColor().getTextColor();
			String[] astring = new String[MAXLINES];
			for (int j = 0; j < astring.length; ++j) {
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
			for (int k1 = 0; k1 < astring.length; ++k1) {
				String s = astring[k1];
				if (s != null) {
					float f3 = (float) (-this.minecraft.fontRenderer.getStringWidth(s) / 2);
					this.minecraft.fontRenderer.renderString(s, f3, (float) (k1 * 10 - this.tileSign.signText.length * 5), i, false, matrix4f,
							irendertypebuffer$impl, false, 0, 15728880);
					if (k1 == this.editLine && k >= 0 && flag1) {
						int l1 = this.minecraft.fontRenderer.getStringWidth(s.substring(0, Math.max(Math.min(k, s.length()), 0)));
						int i2 = (l1 - this.minecraft.fontRenderer.getStringWidth(s) / 2) * i1;
						if (k >= s.length()) {
							this.minecraft.fontRenderer.renderString("_", (float) i2, (float) j1, i, false, matrix4f, irendertypebuffer$impl, false,
									0, 15728880);
						}
					}
				}
			}
			
			irendertypebuffer$impl.finish();
			for (int k3 = 0; k3 < astring.length; ++k3) {
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
						int k2 = (this.minecraft.fontRenderer.getStringWidth(s1.substring(0, j4))
								- this.minecraft.fontRenderer.getStringWidth(s1) / 2) * i1;
						int l2 = (this.minecraft.fontRenderer.getStringWidth(s1.substring(0, j2))
								- this.minecraft.fontRenderer.getStringWidth(s1) / 2) * i1;
						int i3 = Math.min(k2, l2);
						int j3 = Math.max(k2, l2);
						Tessellator tessellator = Tessellator.getInstance();
						BufferBuilder bufferbuilder = tessellator.getBuffer();
						RenderSystem.disableTexture();
						RenderSystem.enableColorLogicOp();
						RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
						bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
						bufferbuilder.pos(matrix4f, (float) i3, (float) (j1 + 9), 0.0F).color(0, 0, 255, 255).endVertex();
						bufferbuilder.pos(matrix4f, (float) j3, (float) (j1 + 9), 0.0F).color(0, 0, 255, 255).endVertex();
						bufferbuilder.pos(matrix4f, (float) j3, (float) j1, 0.0F).color(0, 0, 255, 255).endVertex();
						bufferbuilder.pos(matrix4f, (float) i3, (float) j1, 0.0F).color(0, 0, 255, 255).endVertex();
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


	public static class PackedUpdateServerHangingSign{
		private BlockPos pos;
		private ITextComponent t0;
		private ITextComponent t1;
		private ITextComponent t2;
		private ITextComponent t3;
		private ITextComponent t4;
		
		public PackedUpdateServerHangingSign(PacketBuffer buf) {
			
			this.pos = buf.readBlockPos();
			this.t0 = buf.readTextComponent();
			this.t1 = buf.readTextComponent();
			this.t2 = buf.readTextComponent();
			this.t3 = buf.readTextComponent();
			this.t4 = buf.readTextComponent();
		}

		public PackedUpdateServerHangingSign(BlockPos pos, ITextComponent t0, ITextComponent t1, ITextComponent t2, ITextComponent t3,
				ITextComponent t4) {
			this.pos = pos;
			this.t0 = t0;
			this.t1 = t1;
			this.t2 = t2;
			this.t3 = t3;
			this.t4 = t4;
		}

		public static void buffer(PackedUpdateServerHangingSign message, PacketBuffer buf) {
			
			buf.writeBlockPos(message.pos);
			buf.writeTextComponent(message.t0);
			buf.writeTextComponent(message.t1);
			buf.writeTextComponent(message.t2);
			buf.writeTextComponent(message.t3);
			buf.writeTextComponent(message.t4);		
		}

		public static void handler(PackedUpdateServerHangingSign message, Supplier<NetworkEvent.Context> ctx) {
			// server world
			World world = ctx.get().getSender().world;
			
			ctx.get().enqueueWork(() -> {
				if (world != null) {
					TileEntity tileentity = world.getTileEntity(message.pos);
					if (tileentity instanceof HangingSignBlock.CustomTileEntity) {
						HangingSignBlock.CustomTileEntity sign = (HangingSignBlock.CustomTileEntity) tileentity;
						sign.setText(0, message.t0);
						sign.setText(1, message.t1);
						sign.setText(2, message.t2);
						sign.setText(3, message.t3);
						sign.setText(4, message.t4);
					}
				}
			});
			ctx.get().setPacketHandled(true);
		}
	}

	
}
