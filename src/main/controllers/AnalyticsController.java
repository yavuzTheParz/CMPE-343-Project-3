package main.controllers;

import main.dao.OrderDAO;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.collections.ObservableList;

public class AnalyticsController extends BaseController {

    @FXML private PieChart salesPieChart;

    @FXML
    public void initialize() {
        loadChartData();
    }

    private void loadChartData() {
        ObservableList<PieChart.Data> data = OrderDAO.getProductSalesStats();
        salesPieChart.setData(data);

        // İnteraktif Tıklama Özelliği
        for (PieChart.Data slice : data) {
            // Tıklayınca detay göster
            slice.getNode().setOnMouseClicked(e -> {
                showAlert("Sales Statistic", 
                          slice.getName() + "\nTotal Sold: " + slice.getPieValue() + " kg");
            });

            // Üzerine gelince ipucu göster
            Tooltip tooltip = new Tooltip(slice.getName() + ": " + slice.getPieValue() + " kg");
            Tooltip.install(slice.getNode(), tooltip);
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) salesPieChart.getScene().getWindow();
        stage.close();
    }
}