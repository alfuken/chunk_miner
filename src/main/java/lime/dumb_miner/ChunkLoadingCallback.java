package lime.dumb_miner;

import lime.dumb_miner.tiles.DumbMinerTile;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;

import java.util.List;

public class ChunkLoadingCallback implements ForgeChunkManager.LoadingCallback
{
    @Override
    public void ticketsLoaded(List<ForgeChunkManager.Ticket> tickets, World world)
    {
        for (ForgeChunkManager.Ticket ticket : tickets)
        {
            int x = ticket.getModData().getInteger("x");
            int y = ticket.getModData().getInteger("y");
            int z = ticket.getModData().getInteger("z");
            TileEntity tile = world.getTileEntity(x, y, z);
            if (tile instanceof DumbMinerTile) {
                ((DumbMinerTile) tile).forceChunkLoading(ticket);
            }
        }
    }
}