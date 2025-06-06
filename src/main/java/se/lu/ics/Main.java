package se.lu.ics;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Main extends Application {

    private static Stage primaryStage;
    private static Map<String, Scene> sceneCache = new HashMap<>();

    @Override
    public void start(Stage primaryStage) {
        Main.primaryStage = primaryStage;
        loadAllScenes(); // Load all scenes method at startup
        setScene("MainView"); // Set MainView as the initial scene
    }

    // Load all scenes at startup

    private void loadAllScenes() {
        try {
            sceneCache.put("MainView", loadScene("/fxml/MainView.fxml"));
            sceneCache.put("EmployeeView", loadScene("/fxml/EmployeeView.fxml"));
            sceneCache.put("ProjectView", loadScene("/fxml/ProjectView.fxml"));
            sceneCache.put("MilestoneView", loadScene("/fxml/MilestoneView.fxml"));
            sceneCache.put("WorkView", loadScene("/fxml/WorkView.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error", "Could not load scenes. Please contact Arctic Byte Support.");
        }
    }

    // Load scene method

    private Scene loadScene(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        AnchorPane root = loader.load();
        return new Scene(root);
    }

    // Set scene method

    public static void setScene(String sceneName) {
        Scene scene = sceneCache.get(sceneName);
        if (scene != null) {
            primaryStage.setScene(scene);
            primaryStage.show();
        } else {
            showErrorAlert("Error", "Scene not found: " + sceneName);
        }
    }

    // Show error alert method

    private static void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title); // Set the title of the alert
        alert.setHeaderText(null); // No header text, keeping it simple
        alert.setContentText(message); // Set the content of the alert
        alert.initModality(Modality.APPLICATION_MODAL); // Set modality (blocks interaction with other windows until closed)
        alert.showAndWait(); // Show the alert and wait for the user to close it
    }

    // Main method to launch the application

    public static void main(String[] args) {
        launch(args);
    }
}
