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
/*
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
