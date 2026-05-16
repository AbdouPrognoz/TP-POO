package Zone;

import Crops.Crops;
import java.util.ArrayList;
import java.util.List;

public class CropZone extends Zone {

    private List<Crops> crops = new ArrayList<>();

    public CropZone(String code, String name, String type) {
        super(code, name, type);
    }

    public void addCrop(Crops crop) {
        if (crop == null) throw new IllegalArgumentException("Crop cannot be null.");
        for (Crops c : crops) {
            if (c.getId().equals(crop.getId())) {
                throw new IllegalArgumentException("Crop already exists: " + crop.getId());
            }
        }
        crops.add(crop);
    }

    public List<Crops> getCrops() { return crops; }

    @Override
    public int getHostedCount() { return crops.size(); }

    @Override
    public String getType() { return "CROP"; }
}

