package Zone;

public abstract class Zone {

    // attributes
    private String code;
    private String name;
    private String type;
    private StatusZone status = StatusZone.ACTIVE;
    private double production = 0;

    // constructor
    public Zone(String code, String name, String type) {
        this.code = code;
        this.name = name;
        this.type = type;
    }

    // getters
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getType() { return type; }
    public StatusZone getStatus() { return status; }
    public double getProduction() { return production; }

    // setters
    public void setName(String name) { this.name = name; }
    public void setType(String type) { this.type = type; }

    // suspend a zone
    public void suspend() {
        this.status = StatusZone.SUSPENDED;
    }
    // reactivate a zone
    public void reactivate() {
        this.status = StatusZone.ACTIVE;
    }
   // get production level
    public double productionLevel() {
        return production;
    }
    // update production level
    public void setProduction(double production) {
        this.production = production;
    }
    // to get the number of hosted entities
    public abstract int getHostedCount();
}