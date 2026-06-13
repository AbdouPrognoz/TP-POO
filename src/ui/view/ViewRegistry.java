package ui.view;

import ui.model.FarmDataStore;

public class ViewRegistry {
    private final DashboardView dashboardView;
    private final ZoneView zoneView;
    private final CropView cropView;
    private final AnimalView animalView;
    private final SensorView sensorView;
    private final AlertView alertView;
    private final ChartView chartView;

    public ViewRegistry(FarmDataStore store) {
        this.dashboardView = new DashboardView(store);
        this.zoneView = new ZoneView(store);
        this.cropView = new CropView(store);
        this.animalView = new AnimalView(store);
        this.sensorView = new SensorView(store);
        this.alertView = new AlertView(store);
        this.chartView = new ChartView(store);
    }

    public DashboardView getDashboardView() { return dashboardView; }
    public ZoneView getZoneView() { return zoneView; }
    public CropView getCropView() { return cropView; }
    public AnimalView getAnimalView() { return animalView; }
    public SensorView getSensorView() { return sensorView; }
    public AlertView getAlertView() { return alertView; }
    public ChartView getChartView() { return chartView; }

    public void refreshAll() {
        dashboardView.refresh();
        zoneView.refresh();
        cropView.refresh();
        animalView.refresh();
        sensorView.refresh();
        alertView.refresh();
        chartView.refresh();
    }
}
