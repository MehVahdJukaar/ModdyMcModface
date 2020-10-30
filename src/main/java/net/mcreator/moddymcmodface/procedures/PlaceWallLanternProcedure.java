package net.mcreator.moddymcmodface.procedures;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.common.MinecraftForge;

import net.minecraft.world.World;
import net.minecraft.entity.player.PlayerEntity;

import net.mcreator.moddymcmodface.ModdymcmodfaceModElements;
import net.mcreator.moddymcmodface.block.WallLanternBlock;


import java.util.Map;
import java.util.HashMap;
import net.minecraft.item.Items;
import net.java.games.input.DirectAndRawInputEnvironmentPlugin;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.util.Direction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.ActionResultType;
import org.omg.CORBA.CTX_RESTRICT_SCOPE;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.util.math.Vec3d;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.item.BlockItem;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.StringTextComponent;

@ModdymcmodfaceModElements.ModElement.Tag
public class PlaceWallLanternProcedure extends ModdymcmodfaceModElements.ModElement {
	public PlaceWallLanternProcedure(ModdymcmodfaceModElements instance) {
		super(instance, 182);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		PlayerEntity player = event.getPlayer();
		Hand hand = event.getHand();
		if (hand != player.getActiveHand())
			return;

		ItemStack stack = player.getHeldItem(hand);
		if(stack.getItem() == Items.LANTERN){
			Direction dir = event.getFace();

			if(dir != Direction.UP && dir != Direction.DOWN){
				BlockPos pos = event.getPos();
				World world = event.getWorld();

				Item item = WallLanternBlock.block.asItem();

				BlockItemUseContext ctx = new BlockItemUseContext(
					new ItemUseContext(player, hand, new BlockRayTraceResult(
						new Vec3d(pos.getX(),pos.getY(),pos.getZ()), dir, pos, false)));

				ActionResultType result = ((BlockItem)item).tryPlace(ctx);

				if(result.isSuccessOrConsume()){
					if(player.isCreative()) stack.grow(1);
					//event.setCancellationResult(result);
					//event.setCanceled(true);
				}
				//if(world.getBlockState(pos).isSolidSide(world, pos, dir) &&
					//world.isAirBlock(pos.offset(dir))){
					//world.setBlockState(pos.offset(dir), WallLanternBlock.block.getDefaultState().with(HorizontalBlock.HORIZONTAL_FACING, dir), 3);
		
					//if(player.isCreative()) stack.shrink(1);
					
					//event.setCancellationResult(ActionResultType.SUCCESS);

				

			}
			
		}
	}
}
