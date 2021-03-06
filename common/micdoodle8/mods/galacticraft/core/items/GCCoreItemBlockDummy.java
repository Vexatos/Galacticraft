package micdoodle8.mods.galacticraft.core.items;

import micdoodle8.mods.galacticraft.core.client.ClientProxyCore;
import net.minecraft.block.Block;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class GCCoreItemBlockDummy extends ItemBlock
{
    public GCCoreItemBlockDummy(int id)
    {
        super(id);
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Icon getIconFromDamage(int par1)
    {
        return Block.blocksList[this.getBlockID()].getIcon(0, par1);
    }

    @Override
    public int getMetadata(int damage)
    {
        return damage;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public EnumRarity getRarity(ItemStack par1ItemStack)
    {
        return ClientProxyCore.galacticraftItem;
    }

    @Override
    public String getUnlocalizedName(ItemStack itemstack)
    {
        int metadata = itemstack.getItemDamage();
        String blockName = "";

        switch (metadata)
        {
        case 1:
            blockName = "spaceStationBase";
            break;
        case 2:
            blockName = "launchPad";
            break;
        case 3:
            blockName = "nasaWorkbench";
            break;
        case 4:
            blockName = "solar";
            break;
        case 5:
            blockName = "cryogenicChamber";
            break;
        default:
            blockName = null;
            break;
        }

        return Block.blocksList[this.getBlockID()].getUnlocalizedName() + "." + blockName;
    }

    @Override
    public String getUnlocalizedName()
    {
        return Block.blocksList[this.getBlockID()].getUnlocalizedName() + ".0";
    }
}
