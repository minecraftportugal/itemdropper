package pt.minecraft.itemdropper;

class ItemDrop
{
    private int id, accountId, item, size;
    private short itemAux;
    private long removeDate = 0;


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

	public long getRemoveDate() {
		return removeDate;
	}

	public void setRemoveDate(long removeDate) {
		this.removeDate = removeDate;
	}
	public void setRemoveDate()
	{
		this.removeDate = System.currentTimeMillis();
	}
}

// vim: et:ts=4:sw=4
