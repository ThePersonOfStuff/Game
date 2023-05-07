package comms;

import java.net.InetSocketAddress;

public class HostData {
    private InetSocketAddress address;
    private String name;

    public HostData(InetSocketAddress addr, String name) {
        this.name = name;
        address = addr;
    }

    public InetSocketAddress address() {
        return address;
    }

    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return name + ": " + address.toString();
    }

    @Override
    public boolean equals(Object other) {
        if(!(other instanceof HostData)) {
            return false;
        }

        HostData otherData = (HostData)other;

        return name.equals(otherData.name()) && address.equals(otherData.address());
    }
}
