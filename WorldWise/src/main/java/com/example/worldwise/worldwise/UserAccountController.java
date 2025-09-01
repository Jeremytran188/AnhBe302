package com.example.worldwise.worldwise;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty; // ✅ needed for the date column

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class UserAccountController {

    @FXML private TableView<GameResult> historyTable;
    @FXML private TableColumn<GameResult, String> modeColumn;
    @FXML private TableColumn<GameResult, String> topicColumn;
    @FXML private TableColumn<GameResult, Integer> scoreColumn;
    @FXML private TableColumn<GameResult, String> dateColumn;

    private final GameHistoryDAO historyDAO = GameHistoryDAO.getInstance();

    @FXML
    public void initialize() {
        if (modeColumn != null) {
            // Bind columns to GameResult fields
            modeColumn.setCellValueFactory(new PropertyValueFactory<>("mode"));
            topicColumn.setCellValueFactory(new PropertyValueFactory<>("topic"));
            scoreColumn.setCellValueFactory(new PropertyValueFactory<>("score"));

            // Format timestamp -> readable date string
            dateColumn.setCellValueFactory(cellData -> {
                long ts = cellData.getValue().timestampMs();
                String formatted = ts > 0
                        ? new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date(ts))
                        : "";
                return new SimpleStringProperty(formatted);
            });

            // Demo: load history for a hardcoded user (replace with logged-in user later)
            List<GameResult> results = historyDAO.listByUser("javabeans@test.com");
            historyTable.getItems().setAll(results);
        }
    }
}
