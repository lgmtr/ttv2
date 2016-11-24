package de.haw.ttv2.main.network;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NetworkInterfaceModell {

	private String networkInterfaceName;

	private String networkInterfaceDisplayName;

	private List<String> networkInterfaceInetAddresses;

	public static final String IPADDRESS_PATTERN = "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";

	public NetworkInterfaceModell(String networkInterfaceName, String networkInterfaceDisplayName, List<String> networkInterfaceInetAddresses) {
		this.networkInterfaceName = networkInterfaceName;
		this.networkInterfaceDisplayName = networkInterfaceDisplayName;
		this.networkInterfaceInetAddresses = networkInterfaceInetAddresses;
	}

	public NetworkInterfaceModell(String networkInterfaceName, String networkInterfaceDisplayName, String... networkInterfaceInetAddresses) {
		this(networkInterfaceName, networkInterfaceDisplayName, Arrays.asList(networkInterfaceInetAddresses));
	}

	public NetworkInterfaceModell(String networkInterfaceName, String networkInterfaceDisplayName,
			Enumeration<InetAddress> networkInterfaceInetAddressesEnumeration) {
		List<String> networkInterfaceInetAddresses = new ArrayList<>();
		for (InetAddress inetAddress : Collections.list(networkInterfaceInetAddressesEnumeration)) {
			Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);
			Matcher matcher = pattern.matcher(inetAddress.toString());
			if (matcher.find()) {
				final String group = matcher.group();
				if(!group.equals("127.0.0.1"))
					networkInterfaceInetAddresses.add(group);
			}
		}
		this.networkInterfaceName = networkInterfaceName;
		this.networkInterfaceDisplayName = networkInterfaceDisplayName;
		this.networkInterfaceInetAddresses = networkInterfaceInetAddresses;
	}

	public boolean hasNetworkInterfaceInetAddresses() {
		if (networkInterfaceInetAddresses != null)
			if (networkInterfaceInetAddresses.size() > 0)
				return true;
		return false;
	}

	@Override
	public String toString() {
		String returnValue = "";
		returnValue += this.networkInterfaceDisplayName + "\n";
		returnValue += this.networkInterfaceName + "\n";
		for (String inetAddresses : networkInterfaceInetAddresses) {
			returnValue += inetAddresses + "\n";
		}
		return returnValue;
	}

	public String getNetworkInterfaceName() {
		return networkInterfaceName;
	}

	public void setNetworkInterfaceName(String networkInterfaceName) {
		this.networkInterfaceName = networkInterfaceName;
	}

	public String getNetworkInterfaceDisplayName() {
		return networkInterfaceDisplayName;
	}

	public void setNetworkInterfaceDisplayName(String networkInterfaceDisplayName) {
		this.networkInterfaceDisplayName = networkInterfaceDisplayName;
	}

	public List<String> getNetworkInterfaceInetAddresses() {
		return networkInterfaceInetAddresses;
	}

	public void setNetworkInterfaceInetAddresses(List<String> networkInterfaceInetAddresses) {
		this.networkInterfaceInetAddresses = networkInterfaceInetAddresses;
	}
}
