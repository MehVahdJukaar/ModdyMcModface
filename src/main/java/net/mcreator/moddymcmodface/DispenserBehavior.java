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

import net.mcreator.moddymcmodface.block.JarBlock;

import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraft.item.PotionItem;
import net.minecraft.dispenser.IDispenseItemBehavior;
import net.minecraft.util.IItemProvider;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.item.ItemStack;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.util.Direction;
import net.minecraft.block.DispenserBlock;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.item.Item;
import net.minecraft.item.BlockItem;
import net.minecraft.item.DirectionalPlaceContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.dispenser.OptionalDispenseBehavior;
import net.minecraft.dispenser.ShulkerBoxDispenseBehavior;

@ModdymcmodfaceModElements.ModElement.Tag
public class DispenserBehavior extends ModdymcmodfaceModElements.ModElement {
	/**
	 * Do not remove this constructor
	 */
	public DispenserBehavior(ModdymcmodfaceModElements instance) {
		super(instance, 163);
	}


	@Override
	public void initElements() {
	}

	@Override
	public void init(FMLCommonSetupEvent event) {
		registerBehaviors();
	}

	@Override
	public void serverLoad(FMLServerStartingEvent event) {
	}



	private static void register(IItemProvider provider, IDispenseItemBehavior behavior) {
		DispenserBlock.registerDispenseBehavior(provider, behavior);
	}



	private static void registerBehaviors() {
		for(Item item : ForgeRegistries.ITEMS) {
			if(item == new ItemStack(JarBlock.block).getItem()){
				register(item, new JarDispenseBehavior());
			}
		}
	}



	public static class JarDispenseBehavior extends OptionalDispenseBehavior {
	   /**
	    * Dispense the specified stack, play the dispense sound and spawn particles.
	    */
	   protected ItemStack dispenseStack(IBlockSource source, ItemStack stack) {
	      this.successful = false;
	      Item item = stack.getItem();
	      if (item instanceof BlockItem) {
	         Direction direction = source.getBlockState().get(DispenserBlock.FACING);
	         BlockPos blockpos = source.getBlockPos().offset(direction);
	         Direction direction1 = source.getWorld().isAirBlock(blockpos.down()) ? direction : Direction.UP;
	         this.successful = ((BlockItem)item).tryPlace(new DirectionalPlaceContext(source.getWorld(), blockpos, direction, stack, direction1)) == ActionResultType.SUCCESS;
	      }
	
	      return stack;
	   }
	}
	//PlayerEntity fakePlayer = FakePlayerFactory.getMinecraft((ServerWorld)world);






}
