package tconstruct.blocks;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import tconstruct.TConstruct;
import tconstruct.blocks.logic.CraftingStationLogic;
import tconstruct.blocks.logic.PartBuilderLogic;
import tconstruct.blocks.logic.PatternChestLogic;
import tconstruct.blocks.logic.StencilTableLogic;
import tconstruct.blocks.logic.ToolForgeLogic;
import tconstruct.blocks.logic.ToolStationLogic;
import tconstruct.common.TContent;
import tconstruct.common.TProxyCommon;
import tconstruct.library.TConstructRegistry;
import tconstruct.library.blocks.InventorySlab;
import tconstruct.util.config.PHConstruct;

public class CraftingSlab extends InventorySlab
{
    public CraftingSlab(int id, Material material)
    {
        super(id, material);
        this.setCreativeTab(TConstructRegistry.blockTab);
        this.setHardness(2f);
        this.setStepSound(Block.soundWoodFootstep);
    }

    /* Rendering */
    @Override
    public String[] getTextureNames ()
    {
        String[] textureNames = { "craftingstation_top", "craftingstation_slab_side", "craftingstation_bottom", "toolstation_top", "toolstation_slab_side", "toolstation_bottom",
                "partbuilder_oak_top", "partbuilder_slab_side", "partbuilder_oak_bottom", "stenciltable_oak_top", "stenciltable_slab_side", "stenciltable_oak_bottom", "patternchest_top",
                "patternchest_slab_side", "patternchest_bottom", "toolforge_top", "toolforge_slab_side", "toolforge_top" };

        return textureNames;
    }

    @Override
    public Icon getIcon (int side, int meta)
    {
        return icons[(meta % 8) * 3 + getTextureIndex(side)];
    }

    public int getTextureIndex (int side)
    {
        if (side == 0)
            return 2;
        if (side == 1)
            return 0;

        return 1;
    }

    public AxisAlignedBB getSelectedBoundingBoxFromPool (World world, int x, int y, int z)
    {
        int metadata = world.getBlockMetadata(x, y, z);
        if (metadata == 5)
            return AxisAlignedBB.getAABBPool().getAABB((double) x + this.minX, (double) y + this.minY, (double) z + this.minZ, (double) x + this.maxX, (double) y + this.maxY - 0.125,
                    (double) z + this.maxZ);
        return AxisAlignedBB.getAABBPool().getAABB((double) x + this.minX, (double) y + this.minY, (double) z + this.minZ, (double) x + this.maxX, (double) y + this.maxY, (double) z + this.maxZ);
    }

    @Override
    public TileEntity createTileEntity (World world, int metadata)
    {
        switch (metadata % 8)
        {
        case 0:
            return new CraftingStationLogic();
        case 1:
            return new ToolStationLogic();
        case 2:
            return new PartBuilderLogic();
        case 3:
            return new StencilTableLogic();
        case 4:
            return new PatternChestLogic();
        case 5:
            return new ToolForgeLogic();
        default:
            return null;
        }
    }

    @Override
    public Integer getGui (World world, int x, int y, int z, EntityPlayer entityplayer)
    {
        int meta = world.getBlockMetadata(x, y, z) % 8;
        switch (meta)
        {
        case 0:
            return TProxyCommon.craftingStationID;
        case 1:
            return TProxyCommon.toolStationID;
        case 2:
            return TProxyCommon.partBuilderID;
        case 3:
            return TProxyCommon.stencilTableID;
        case 4:
            return TProxyCommon.patternChestID;
        case 5:
            return TProxyCommon.toolForgeID;
        }

        return -1;
    }

    @Override
    public Object getModInstance ()
    {
        return TConstruct.instance;
    }

    @Override
    public void getSubBlocks (int id, CreativeTabs tab, List list)
    {
        for (int iter = 0; iter < 6; iter++)
        {
            list.add(new ItemStack(id, 1, iter));
        }
    }    

    /* Keep pattern chest inventory */
    @Override
    public void breakBlock (World par1World, int x, int y, int z, int blockID, int meta)
    {
        if (meta != 4)
            super.breakBlock(par1World, x, y, z, blockID, meta);
        else
        {
            par1World.removeBlockTileEntity(x, y, z);
        }
    }
    
    @Override
    public boolean removeBlockByPlayer (World world, EntityPlayer player, int x, int y, int z)
    {
        player.addExhaustion(0.025F);

        if (!world.isRemote && world.getGameRules().getGameRuleBooleanValue("doTileDrops"))
        {
            int meta = world.getBlockMetadata(x, y, z);
            if (meta == 4)
            {
                ItemStack chest = new ItemStack(this, 1, 4);
                NBTTagCompound inventory = new NBTTagCompound();
                PatternChestLogic logic = (PatternChestLogic) world.getBlockTileEntity(x, y, z);
                logic.writeInventoryToNBT(inventory);
                NBTTagCompound baseTag = new NBTTagCompound();
                baseTag.setCompoundTag("Inventory", inventory);
                chest.setTagCompound(baseTag);

                //Spawn item
                if (!player.capabilities.isCreativeMode || player.isSneaking())
                {
                    float f = 0.7F;
                    double d0 = (double) (world.rand.nextFloat() * f) + (double) (1.0F - f) * 0.5D;
                    double d1 = (double) (world.rand.nextFloat() * f) + (double) (1.0F - f) * 0.5D;
                    double d2 = (double) (world.rand.nextFloat() * f) + (double) (1.0F - f) * 0.5D;
                    EntityItem entityitem = new EntityItem(world, (double) x + d0, (double) y + d1, (double) z + d2, chest);
                    entityitem.delayBeforeCanPickup = 10;
                    world.spawnEntityInWorld(entityitem);
                }
            }
        }
        return world.setBlockToAir(x, y, z);
    }

    @Override
    public void harvestBlock (World world, EntityPlayer player, int x, int y, int z, int meta)
    {
        if (meta != 4)
        super.harvestBlock(world, player, x, y, z, meta);
        //Do nothing
    }

    @Override
    public void onBlockPlacedBy (World world, int x, int y, int z, EntityLivingBase living, ItemStack stack)
    {
        boolean keptInventory = false;
        if (stack.hasTagCompound())
        {
            NBTTagCompound inventory = stack.getTagCompound().getCompoundTag("Inventory");
            if (inventory != null)
            {
                PatternChestLogic logic = (PatternChestLogic) world.getBlockTileEntity(x, y, z);
                logic.readInventoryFromNBT(inventory);
                logic.xCoord = x;
                logic.yCoord = y;
                logic.zCoord = z;
                keptInventory = true;
            }
        }
        if (!keptInventory && PHConstruct.freePatterns)
        {
            int meta = world.getBlockMetadata(x, y, z);
            if (meta == 4)
            {
                PatternChestLogic logic = (PatternChestLogic) world.getBlockTileEntity(x, y, z);
                for (int i = 1; i <= 13; i++)
                {
                    logic.setInventorySlotContents(i - 1, new ItemStack(TContent.woodPattern, 1, i));
                }
                logic.setInventorySlotContents(13, new ItemStack(TContent.woodPattern, 1, 22));
            }
        }
        super.onBlockPlacedBy(world, x, y, z, living, stack);
    }
}