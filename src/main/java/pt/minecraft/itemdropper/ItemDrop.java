package pt.minecraft.itemdropper;

import org.bukkit.entity.Player;

class ItemDrop
{
    private int id, accountId, item, size;
    private short itemAux = 0;
    private long removeDate = 0;
    private Player player = null;


    public ItemDrop(int id, int acid, int item, int size)
    {
        this.id = id;
        this.accountId = acid;
        this.item = item;
        this.size = size;
        this.itemAux = 0;
    }

    public ItemDrop(int id, int acid, int item, int size, short itemAux)
    {
        this.id = id;
        this.accountId = acid;
        this.item = item;
        this.size = size;
        this.itemAux = itemAux;
    }

    public int getId()
    {
        return this.id;
    }

    public int getAccountId()
    {
        return this.accountId;
    }

    public int getItem()
    {
        return this.item;
    }

    public short getItemAux()
    {
        return this.itemAux;
    }

    public int getSize()
    {
        return this.size;
    }

	public long getRemoveDate()
	{
		return removeDate;
	}

	public void setRemoveDate(long removeDate) {
		this.removeDate = removeDate;
	}
	public void setRemoveDate()
	{
		this.removeDate = System.currentTimeMillis();
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}
}

