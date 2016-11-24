package de.haw.ttv2.main.network;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetworkInterfaceInfo {

	public static Map<String, NetworkInterfaceModell> getNetworkInterfacesWithInetAddresses() {
		Map<String, NetworkInterfaceModell> interfaceMap = new HashMap<>();
		try {
			Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
			for (NetworkInterface netint : Collections.list(nets)) {
				NetworkInterfaceModell nim = new NetworkInterfaceModell(netint.getName(), netint.getDisplayName(), netint.getInetAddresses());
				if (nim.hasNetworkInterfaceInetAddresses())
					interfaceMap.put(nim.getNetworkInterfaceName(), nim);
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return interfaceMap;
	}
	
	public static List<String> getNIIA(){
		List<String> inetList = new ArrayList<>();
		Map<String, NetworkInterfaceModell> interfaceMap = getNetworkInterfacesWithInetAddresses();
		for (String key : interfaceMap.keySet())
			inetList.addAll(interfaceMap.get(key).getNetworkInterfaceInetAddresses());
		return inetList;
	}
}
