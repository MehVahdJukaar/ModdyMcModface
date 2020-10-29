
package net.mcreator.moddymcmodface.gui;

import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.World;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.tileentity.CommandBlockTileEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.Minecraft;

import net.mcreator.moddymcmodface.ModdymcmodfaceMod;
import net.mcreator.moddymcmodface.block.SpeakerBlockBlock;
import net.mcreator.moddymcmodface.ModdymcmodfaceModElements;

import java.util.function.Supplier;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.tileentity.TileEntity;

@ModdymcmodfaceModElements.ModElement.Tag
public class EditSpeakerBlockGui extends ModdymcmodfaceModElements.ModElement {
	public EditSpeakerBlockGui(ModdymcmodfaceModElements instance) {
		super(instance, 178);
		elements.addNetworkMessage(PackedUpdateServerSpeakerBlock.class, PackedUpdateServerSpeakerBlock::buffer, PackedUpdateServerSpeakerBlock::new,
				PackedUpdateServerSpeakerBlock::handler);

				
		FMLJavaModLoadingContext.get().getModEventBus().register(this);
	}
	@OnlyIn(Dist.CLIENT)
	public static class GuiWindow extends Screen {
		private TextFieldWidget commandTextField;
		private SpeakerBlockBlock.CustomTileEntity tileSpeaker = null;
		private boolean narrator = false;
		private String message = "";
		private Button modeBtn;
		public GuiWindow(SpeakerBlockBlock.CustomTileEntity te) {
			super(new StringTextComponent("edit speaker message"));
			this.tileSpeaker = te;
			this.narrator = tileSpeaker.narrator;
			this.message = tileSpeaker.message;
		}

		public static void open(SpeakerBlockBlock.CustomTileEntity te) {
			Minecraft.getInstance().displayGuiScreen(new GuiWindow(te));
		}

		public void tick() {
			this.commandTextField.tick();
		}

		private void updateMode() {
			if (this.narrator) {
				this.modeBtn.setMessage("Narrator");
			} else {
				this.modeBtn.setMessage("Chat message");
			}
		}

		private void toggleMode() {
			this.narrator = !this.narrator;
		}

		@Override
		public void init() {
			this.minecraft.keyboardListener.enableRepeatEvents(true);
			this.addButton(new Button(this.width / 2 - 100, this.height / 4 + 120, 200, 20, I18n.format("gui.done"), (p_214266_1_) -> {
				this.close();
			}));
			this.modeBtn = this.addButton(new Button(this.width / 2 - 75, this.height / 4 + 50, 150, 20, "Narrator", (p_214186_1_) -> {
				this.toggleMode();
				this.updateMode();
			}));
			this.updateMode();
			this.commandTextField = new TextFieldWidget(this.font, this.width / 2 - 100, this.height / 4 + 10, 200, 20, "prova") {
				protected String getNarrationMessage() {
					return super.getNarrationMessage();
				}
			};
			this.commandTextField.setText(message);
			this.commandTextField.setMaxStringLength(32);
			this.children.add(this.commandTextField);
			this.setFocusedDefault(this.commandTextField);
			this.commandTextField.setFocused2(true);
		}

		protected void close() {
			// this.tileSpeaker.markDirty();
			this.minecraft.displayGuiScreen((Screen) null);
		}

		@Override
		public void removed() {
			this.minecraft.keyboardListener.enableRepeatEvents(false);
			//update client tile
			this.tileSpeaker.message = this.commandTextField.getText();
			this.tileSpeaker.narrator = this.narrator;
			//update server tile
			ModdymcmodfaceMod.PACKET_HANDLER.sendToServer(new PackedUpdateServerSpeakerBlock(this.tileSpeaker.getPos(), this.tileSpeaker.message, this.tileSpeaker.narrator));
			
		}

		@Override
		public void onClose() {
			this.close();
		}

		public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
			if (super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_)) {
				return true;
			} else if (p_keyPressed_1_ != 257 && p_keyPressed_1_ != 335) {
				return false;
			} else {
				this.close();
				return true;
			}
		}

		public void render(int p_render_1_, int p_render_2_, float p_render_3_) {
			this.renderBackground();
			this.drawCenteredString(this.font, "Set Speaker Block Message", this.width / 2, 40, 16777215);
			//this.drawString(this.font, I18n.format("advMode.command"), this.width / 2 - 150, 40, 10526880);
			this.commandTextField.render(p_render_1_, p_render_2_, p_render_3_);
			super.render(p_render_1_, p_render_2_, p_render_3_);
		}
	}


	public static class PackedUpdateServerSpeakerBlock{
		private BlockPos pos;
		private ITextComponent str;
		private boolean narrator;
		
		public PackedUpdateServerSpeakerBlock(PacketBuffer buf) {
			
			this.pos = buf.readBlockPos();
			this.str = buf.readTextComponent();
			this.narrator = buf.readBoolean();
		}

		public PackedUpdateServerSpeakerBlock(BlockPos pos, String str, boolean narrator) {
			this.pos = pos;
			this.str = new StringTextComponent(str);
			this.narrator = narrator;
		}

		public static void buffer(PackedUpdateServerSpeakerBlock message, PacketBuffer buf) {
			
			buf.writeBlockPos(message.pos);
			buf.writeTextComponent(message.str);
			buf.writeBoolean(message.narrator);
		}

		public static void handler(PackedUpdateServerSpeakerBlock message, Supplier<NetworkEvent.Context> ctx) {
			// server world
			World world = ctx.get().getSender().world;
			
			ctx.get().enqueueWork(() -> {
				if (world != null) {
					TileEntity tileentity = world.getTileEntity(message.pos);
					if (tileentity instanceof SpeakerBlockBlock.CustomTileEntity) {
						SpeakerBlockBlock.CustomTileEntity speaker = (SpeakerBlockBlock.CustomTileEntity) tileentity;
						speaker.message = message.str.getString();
						speaker.narrator = message.narrator;
					}
				}
			});
			ctx.get().setPacketHandled(true);
		}
	}
}
