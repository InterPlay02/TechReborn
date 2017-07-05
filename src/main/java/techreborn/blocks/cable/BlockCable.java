/*
 * This file is part of TechReborn, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2017 TechReborn
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package techreborn.blocks.cable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import techreborn.client.TechRebornCreativeTab;
import techreborn.init.ModBlocks;
import techreborn.items.tools.ItemWrench;
import techreborn.tiles.cable.TileCable;

import javax.annotation.Nullable;
import java.security.InvalidParameterException;
import java.util.Random;

/**
 * Created by modmuss50 on 19/05/2017.
 */
public class BlockCable extends BlockContainer {

	public static final PropertyBool EAST = PropertyBool.create("east");
	public static final PropertyBool WEST = PropertyBool.create("west");
	public static final PropertyBool NORTH = PropertyBool.create("north");
	public static final PropertyBool SOUTH = PropertyBool.create("south");
	public static final PropertyBool UP = PropertyBool.create("up");
	public static final PropertyBool DOWN = PropertyBool.create("down");
	public static final IProperty<EnumCableType> TYPE = PropertyEnum.create("type", EnumCableType.class);

	public BlockCable() {
		super(Material.ROCK);
		setHardness(1F);
		setResistance(8F);
		setCreativeTab(TechRebornCreativeTab.instance);
		setDefaultState(getDefaultState().withProperty(EAST, false).withProperty(WEST, false).withProperty(NORTH, false).withProperty(SOUTH, false).withProperty(UP, false).withProperty(DOWN, false).withProperty(TYPE, EnumCableType.COPPER));
	}

	public static ItemStack getCableByName(String name, int count) {
		for (int i = 0; i < EnumCableType.values().length; i++) {
			if (EnumCableType.values()[i].getName().equalsIgnoreCase(name)) {
				return new ItemStack(ModBlocks.CABLE,
					count, i);
			}
		}
		throw new InvalidParameterException("The cable " + name + " could not be found.");
	}

	@Override
	public void neighborChanged(final IBlockState state, final World w, final BlockPos pos, final Block block,
	                            final BlockPos posNeighbor) {
		if (!w.isRemote)
			((TileCable) w.getTileEntity(pos)).scanHandlers(posNeighbor);
	}

	@Override
	public void breakBlock(final World w, final BlockPos pos, final IBlockState state) {
		((TileCable) w.getTileEntity(pos)).disconnectItself();

		super.breakBlock(w, pos, state);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {

		if (!world.isRemote)
			playerIn.sendMessage(new TextComponentString("Grid: " + ((TileCable) world.getTileEntity(pos)).getGrid()));
		ItemStack stack = playerIn.getHeldItem(hand);
		if (stack.getItem() instanceof ItemWrench && !world.isRemote) { //TODO use new api
			ItemStack itemStack = new ItemStack(this, 1, getMetaFromState(state));
			Random rand = new Random();

			float dX = rand.nextFloat() * 0.8F + 0.1F;
			float dY = rand.nextFloat() * 0.8F + 0.1F;
			float dZ = rand.nextFloat() * 0.8F + 0.1F;

			EntityItem entityItem = new EntityItem(world, pos.getX() + dX, pos.getY() + dY, pos.getZ() + dZ,
				itemStack.copy());

			float factor = 0.05F;
			entityItem.motionX = rand.nextGaussian() * factor;
			entityItem.motionY = rand.nextGaussian() * factor + 0.2F;
			entityItem.motionZ = rand.nextGaussian() * factor;
			world.spawnEntity(entityItem);
			world.setBlockToAir(pos);
		}
		return super.onBlockActivated(world, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
	}

	public static ItemStack getCableByName(String name) {
		return getCableByName(name, 1);
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		return state.getValue(TYPE).getStack();
	}

	@Override
	public int damageDropped(IBlockState state) {
		return getMetaFromState(state);
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
		if (blockState.getValue(TYPE) == EnumCableType.GLASSFIBER)
			return false;
		else
			return true;
	}

	@Override
	public boolean isFullBlock(IBlockState state) {
		return false;
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, EAST, WEST, NORTH, SOUTH, UP, DOWN, TYPE);
	}

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
		return getStateFromMeta(placer.getHeldItem(hand).getItemDamage());
	}

	@Override
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
		for (EnumCableType cableType : EnumCableType.values()) {
			list.add(new ItemStack(this, 1, cableType.ordinal()));
		}
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(TYPE).ordinal();
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(TYPE, EnumCableType.values()[meta]);
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		state = state.getActualState(source, pos);
		float minX = state.getValue(WEST) ? 0.0F : 0.3125F;
		float minY = state.getValue(DOWN) ? 0.0F : 0.3125F;
		float minZ = state.getValue(NORTH) ? 0.0F : 0.3125F;
		float maxX = state.getValue(EAST) ? 1.0F : 0.6875F;
		float maxY = state.getValue(UP) ? 1.0F : 0.6875F;
		float maxZ = state.getValue(SOUTH) ? 1.0F : 0.6875F;
		return new AxisAlignedBB((double) minX, (double) minY, (double) minZ, (double) maxX, (double) maxY, (double) maxZ);
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		IBlockState actualState = state;
		for (EnumFacing facing : EnumFacing.values()) {
			TileCable cable = (TileCable) getTileEntitySafely(worldIn, pos);
			if (cable != null)
				actualState = actualState.withProperty(getProperty(facing), cable.isConnected(facing));
		}
		return actualState;
	}

	//see for more info https://www.reddit.com/r/feedthebeast/comments/5mxwq9/psa_mod_devs_do_you_call_worldgettileentity_from/
	public TileEntity getTileEntitySafely(IBlockAccess blockAccess, BlockPos pos) {
		if (blockAccess instanceof ChunkCache) {
			return ((ChunkCache) blockAccess).getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);
		} else {
			return blockAccess.getTileEntity(pos);
		}
	}

	public IProperty<Boolean> getProperty(EnumFacing facing) {
		switch (facing) {
			case EAST:
				return EAST;
			case WEST:
				return WEST;
			case NORTH:
				return NORTH;
			case SOUTH:
				return SOUTH;
			case UP:
				return UP;
			case DOWN:
				return DOWN;
			default:
				return EAST;
		}
	}

	@Nullable
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileCable();
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

}
