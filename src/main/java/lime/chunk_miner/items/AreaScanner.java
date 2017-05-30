package lime.chunk_miner.items;

import lime.chunk_miner.ChunkMiner;
import lime.chunk_miner.ChunkMinerHelpers;
import lime.chunk_miner.Config;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.world.World;

import java.util.List;


public class AreaScanner extends Item {
    public AreaScanner() {
        setCreativeTab(ChunkMiner.ctab);
        setUnlocalizedName(ChunkMiner.MODID + ".area_scanner");
        setTextureName(ChunkMiner.MODID + ".area_scanner");
    }

    public ItemStack onItemRightClick(ItemStack itemStack, World w, EntityPlayer p) {
        if (w.isRemote){return itemStack;}

        int range = Config.area_scan_radius;

        if (itemStack.getDisplayName().startsWith("range")){
            range = (int)itemStack.getDisplayName().charAt(itemStack.getDisplayName().length() - 1);
        }

        generateBook(p, (int)p.posX, (int)p.posZ, ChunkMinerHelpers.areaScanReportAsPages(w, p, range));

        if (!p.capabilities.isCreativeMode) --itemStack.stackSize;

        w.playSoundAtEntity(p, "IC2:tools.ODScanner", 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

        return itemStack;
    }

    public void generateBook(EntityPlayer player, int x, int z, List<String> pages){
        ItemStack book = new ItemStack(Items.written_book);

        NBTTagCompound tag = new NBTTagCompound();
        NBTTagList bookPages = new NBTTagList();
        for(String page : pages){
            bookPages.appendTag(new NBTTagString(page));
        }
        book.setTagInfo("pages", bookPages);
        book.setTagInfo("author", new NBTTagString(player.getDisplayName()));
        book.setTagInfo("title", new NBTTagString("Scan results of "+x+":"+z));

        // Give the player the book
        player.inventory.addItemStackToInventory(book);
    }

}
