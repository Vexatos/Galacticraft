package micdoodle8.mods.galacticraft.core.blocks;

import java.util.List;
import micdoodle8.mods.galacticraft.core.GalacticraftCore;
import micdoodle8.mods.galacticraft.core.network.GCCorePacketHandlerClient.EnumPacketClient;
import micdoodle8.mods.galacticraft.core.tile.GCCoreTileEntityAluminumWire;
import micdoodle8.mods.galacticraft.core.util.PacketUtil;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import universalelectricity.core.vector.Vector3;
import universalelectricity.prefab.block.BlockConductor;
import universalelectricity.prefab.tile.TileEntityConductor;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class GCCoreBlockAluminumWire extends BlockConductor
{
    public static final String[] names = { "aluminumWire", "aluminumWireHeavy" };
    private static Icon[] blockIcons;

    public GCCoreBlockAluminumWire(int id, String assetName)
    {
        super(id, Material.cloth);
        this.setStepSound(Block.soundClothFootstep);
        this.setResistance(0.2F);
        this.setBlockBounds(0.4F, 0.4F, 0.4F, 0.6F, 0.6F, 0.6F);
        this.minVector = new Vector3(0.4, 0.4, 0.4);
        this.maxVector = new Vector3(0.6, 0.6, 0.6);
        Block.setBurnProperties(this.blockID, 30, 60);
        this.setHardness(0.075F);
        this.setUnlocalizedName(assetName);
    }

    @Override
    public CreativeTabs getCreativeTabToDisplayOn()
    {
        return GalacticraftCore.galacticraftTab;
    }

    @Override
    public void registerIcons(IconRegister par1IconRegister)
    {
        GCCoreBlockAluminumWire.blockIcons = new Icon[GCCoreBlockAluminumWire.names.length];

        for (int i = 0; i < GCCoreBlockAluminumWire.names.length; i++)
        {
            GCCoreBlockAluminumWire.blockIcons[i] = par1IconRegister.registerIcon(GalacticraftCore.ASSET_PREFIX + GCCoreBlockAluminumWire.names[i]);
        }
    }

    @Override
    public boolean isOpaqueCube()
    {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock()
    {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Icon getIcon(int side, int meta)
    {
        switch (meta)
        {
        case 0:
            return GCCoreBlockAluminumWire.blockIcons[0];
        case 1:
            return GCCoreBlockAluminumWire.blockIcons[1];
        default:
            return GCCoreBlockAluminumWire.blockIcons[0];
        }
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, int blockID)
    {
        super.onNeighborBlockChange(world, x, y, z, blockID);

        TileEntity tile = world.getBlockTileEntity(x, y, z);

        if (tile instanceof TileEntityConductor)
        {
            ((TileEntityConductor) tile).adjacentConnections = null;
            Block.blocksList[world.getBlockId(tile.xCoord, tile.yCoord, tile.zCoord)].setBlockBoundsBasedOnState(world, tile.xCoord, tile.yCoord, tile.zCoord);
            PacketDispatcher.sendPacketToAllAround(x, y, z, 10, world.provider.dimensionId, PacketUtil.createPacket(GalacticraftCore.CHANNEL, EnumPacketClient.UPDATE_WIRE_BOUNDS, new Object[] { x, y, z }));
        }
    }

    @Override
    public int getRenderType()
    {
        return -1;
    }

    @Override
    public int damageDropped(int metadata)
    {
        return metadata;
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata)
    {
        switch (metadata)
        {
        case 0:
            return new GCCoreTileEntityAluminumWire();
        case 1:
            return new GCCoreTileEntityAluminumWire(0.025F, 400.0F);
        default:
            return null;
        }
    }

    @Override
    public TileEntity createNewTileEntity(World var1)
    {
        return null;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    @SideOnly(Side.CLIENT)
    public void getSubBlocks(int par1, CreativeTabs par2CreativeTabs, List par3List)
    {
        par3List.add(new ItemStack(par1, 1, 0));
        par3List.add(new ItemStack(par1, 1, 1));
    }
}
