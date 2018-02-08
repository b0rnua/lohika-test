package name.aapanchenko.lohika;

import java.net.InetAddress;

public class InterfaceEntry implements Entry{
	
	boolean state; // State of interface: up or down.
	String name; // Name of interface.
	String macAddress; // MAC address of interface.
	InetAddress ipAddr; // IP address of interface.
	Integer speed; // Rate of speed in Kbytes. (in config can be represented in KB, MB, GB).
	int startLineNumber; // Number of line where interface section start in “config.txt” file.
	
	public boolean isState() {
		return state;
	}

	public void setState(boolean state) {
		this.state = state;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMacAddress() {
		return macAddress;
	}

	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}

	public InetAddress getIpAddr() {
		return ipAddr;
	}

	public void setIpAddr(InetAddress ipAddr) {
		this.ipAddr = ipAddr;
	}

	public Integer getSpeed() {
		return speed;
	}

	public void setSpeed(Integer speed) {
		this.speed = speed;
	}

	public int getStartLineNumber() {
		return startLineNumber;
	}

	public void setStartLineNumber(int startLineNumber) {
		this.startLineNumber = startLineNumber;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((macAddress == null) ? 0 : macAddress.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((speed == null) ? 0 : speed.hashCode());
		result = prime * result + startLineNumber;
		result = prime * result + (state ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		InterfaceEntry other = (InterfaceEntry) obj;
		if (macAddress == null) {
			if (other.macAddress != null)
				return false;
		} else if (!macAddress.equals(other.macAddress))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (speed == null) {
			if (other.speed != null)
				return false;
		} else if (!speed.equals(other.speed))
			return false;
		if (startLineNumber != other.startLineNumber)
			return false;
		if (state != other.state)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "InterfaceEntry [state=" + state + ", name=" + name + ", macAddress=" + macAddress + ", ipAddr=" + ipAddr
				+ ", speed=" + speed + ", startLineNumber=" + startLineNumber + "]";
	}

}
