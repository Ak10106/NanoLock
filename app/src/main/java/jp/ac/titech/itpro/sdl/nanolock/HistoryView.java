package jp.ac.titech.itpro.sdl.nanolock;

public class HistoryView {
    private String name;
    private String mac_address;
    private String query;
    private String datetime;

    public HistoryView(String name, String mac_address, String query, String datetime) {
        this.name = name;
        this.mac_address = mac_address;
        this.query = query;
        this.datetime = datetime;
    }

    public String getName() {
        return name;
    }
    public String getMacAddress() {
        return mac_address;
    }
    public String getQuery() {return query;}
    public String getDatetime() {return datetime;}
}
