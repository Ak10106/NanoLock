package jp.ac.titech.itpro.sdl.nanolock;

public class LockDevicesView {
    private String name;
    private String ip;

    public LockDevicesView(String name, String ip) {
        this.name = name;
        this.ip = ip;
    }

    public String getName() {
        return name;
    }
    public String getIP() {
        return ip;
    }
}
