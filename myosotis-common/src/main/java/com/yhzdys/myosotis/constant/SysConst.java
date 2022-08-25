package com.yhzdys.myosotis.constant;

import com.yhzdys.myosotis.exception.MyosotisException;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public final class SysConst {

    public static final String local_host;
    public static final int processors = Runtime.getRuntime().availableProcessors();

    public static final String separator = System.getProperty("file.separator");
    public static final String myosotis_dir = System.getProperty("user.home") + separator + ".myosotis";

    static {
        try {
            List<String> net4Ips = new ArrayList<>();
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                final NetworkInterface networkInterface = interfaces.nextElement();
                final Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    final InetAddress address = inetAddresses.nextElement();
                    if (address.isLoopbackAddress()) {
                        continue;
                    }
                    if (address instanceof Inet4Address) {
                        net4Ips.add(address.getHostAddress());
                    }
                }
            }
            String net4Address = null;
            for (String ip : net4Ips) {
                if (ip.startsWith("127.0")) {
                    continue;
                }
                net4Address = ip;
                break;
            }
            if (net4Address == null) {
                throw new MyosotisException("Can not load local host");
            }
            local_host = net4Address;
        } catch (MyosotisException e) {
            throw e;
        } catch (Exception e) {
            throw new MyosotisException("Load local host failed.", e);
        }
    }
}
