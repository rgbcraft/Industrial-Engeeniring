package com.rgbcraft.usefuladditions.tiles;

import java.util.HashMap;
import java.util.Map;

import com.rgbcraft.usefuladditions.UsefulAdditions;
import com.rgbcraft.usefuladditions.api.IDebuggable;
import com.rgbcraft.usefuladditions.utils.TileInventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.ISidedInventory;

public class TileSmartSafe extends TileInventory implements ISidedInventory, IDebuggable {
	
	private String passCode = "";
	private String owner = "";
	
	public float doorAngle;
	private int numUsingPlayers;
	public float prevDoorAngle;
	private int ticksSinceSync;


	public TileSmartSafe() {
		super("container.SmartSafe", 54);
	}
	
    private void syncNumUsingPlayers() {
    	super.worldObj.addBlockEvent(this.xCoord, this.yCoord, this.zCoord, this.worldObj.getBlockId(this.xCoord, this.yCoord, this.zCoord), 1, this.numUsingPlayers);
	}
	
	@Override
    public void updateEntity() {
        if (this.ticksSinceSync++ % 20 * 4 == 0 && worldObj.isRemote)
        	this.syncNumUsingPlayers();

        this.prevDoorAngle = this.doorAngle;
        final float var1 = 0.1f;

        if (this.numUsingPlayers > 0 && this.doorAngle == 0.0f) {
            final double var2 = this.xCoord + 0.5;
            final double var3 = this.zCoord + 0.5;
            this.worldObj.playSoundEffect(var2, this.yCoord + 0.5, var3, "random.chestopen", 0.5f, this.worldObj.rand.nextFloat() * 0.1f + 0.9f);
        }

        if ((this.numUsingPlayers == 0 && this.doorAngle > 0.0f) || (this.numUsingPlayers > 0 && this.doorAngle < 1.0f)) {
            final float var4 = this.doorAngle;
            if (this.numUsingPlayers > 0) {
                this.doorAngle += var1;
            } else {
                this.doorAngle -= var1;
            }
            
            if (this.doorAngle > 1.0f)
                this.doorAngle = 1.0f;
            
            final float var5 = 0.5f;
            if (this.doorAngle < var5 && var4 >= var5) {
                final double var3 = this.xCoord + 0.5;
                final double var6 = this.zCoord + 0.5;
                this.worldObj.playSoundEffect(var3, this.yCoord + 0.5, var6, "random.chestclosed", 0.5f, this.worldObj.rand.nextFloat() * 0.1f + 0.9f);
            }

            if (this.doorAngle < 0.0f)
                this.doorAngle = 0.0f;
        }
    }
	
	@Override
    public void openChest() {
        this.numUsingPlayers++;
        this.syncNumUsingPlayers();
    }

	@Override
    public void closeChest() {
        this.numUsingPlayers--;
        this.syncNumUsingPlayers();
    }
    
    @Override
    public void receiveClientEvent(int event, int data) {
        switch (event) {
        	case 1:
        		this.numUsingPlayers = data;
        		break;
        }
    }
	
	public String getPin() {
		return this.passCode;
	}
	
	public String getOwner() {
		return this.owner;
	}
	
	@Override
	public void onDataReceived(byte id, String value, EntityPlayer player) {
		switch (id) {
			case 0:
				this.owner = value;
				break;
			case 1:
				this.passCode = value;
				break;
			case 2:
				player.openGui(UsefulAdditions.instance, 1, this.worldObj, this.xCoord, this.yCoord, this.zCoord);
				return;
		}
		
		this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}
	
	@Override
	public Packet getDescriptionPacket() {
		NBTTagCompound tag = new NBTTagCompound();
		this.writeToNBT(tag);
	    return new Packet132TileEntityData(this.xCoord, this.yCoord, this.zCoord, 0, tag);
	}
	
	@Override
	public void onDataPacket(INetworkManager net, Packet132TileEntityData pkt) {
		this.readFromNBT(pkt.customParam1);
	}

	@Override
	public int getStartInventorySide(ForgeDirection side) {
		return 0;
	}

	@Override
	public int getSizeInventorySide(ForgeDirection side) {
		return 0;
	}
	
	@Override
    public void readFromNBT(final NBTTagCompound compound) {
        super.readFromNBT(compound);

        this.passCode = compound.getString("pin");
        this.owner = compound.getString("owner");
      
    }
    
    @Override
    public void writeToNBT(final NBTTagCompound compound) {
    	super.writeToNBT(compound);

        compound.setString("pin", this.passCode);
        compound.setString("owner", this.owner);
    }

	@Override
	public Map<String, Boolean> getRequirements(EntityPlayer player, HashMap<String, Boolean> requirements) {
		return null;
	}

	@Override
	public Map<String, String> getAdditionalAdvancedInfos(EntityPlayer player, HashMap<String, String> additionalInfos) {
		additionalInfos.put("Proprietario", this.owner.equals("") ? "Nessuno" : this.owner);
		return additionalInfos;
	}

}