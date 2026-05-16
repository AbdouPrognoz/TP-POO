package Zone;

public abstract class Zone {

    private String code;
    private String name;
    private String type;
    private StatusZone status = StatusZone.ACTIVE;
    private double production = 0;

    public Zone(String code, String name, String type) {
        this.code = code;
        this.name = name;
        this.type = type;
    }

    public String getCode() { return code; }
    public String getName() { return name; }
    public String getType() { return type; }
    public StatusZone getStatus() { return status; }
    public double getProduction() { return production; }

    public void setName(String name) { this.name = name; }
    public void setType(String type) { this.type = type; }

    public void suspend() { this.status = StatusZone.SUSPENDED; }
    public void reactivate() { this.status = StatusZone.ACTIVE; }

    public double productionLevel() { return production; }
    public void setProduction(double production) { this.production = production; }

    public abstract int getHostedCount();
}