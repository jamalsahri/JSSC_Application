/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.enib.ips.project;

import java.io.InputStream;
import java.util.Arrays;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 *
 * @author Jamal
 */
public class IPS_Project extends Application {
    
    private static StackPane root;
    
    public static StackPane getRoot(){
        return root;
    }
    
    @Override
    public void start(Stage stage) throws Exception {
        try {
            
            FXMLLoader loader = new FXMLLoader();
            InputStream in = IPS_Project.class.getResourceAsStream("SerialPortUI.fxml");
            loader.setBuilderFactory(new JavaFXBuilderFactory());
            loader.setLocation(IPS_Project.class.getResource("SerialPortUI.fxml"));
            StackPane page;
            try {
                page = (StackPane) loader.load(in);
                root = page;
            } finally {
                in.close();
            } 
            Scene scene = new Scene(page);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.setTitle("Serial Port Communication App");
            stage.getIcons().add(new Image("/com/enib/ips/project/icon2.png"));
            root.getStylesheets().add(getClass().getResource("/com/enib/ips/project/style.css").toExternalForm());
            stage.resizableProperty().setValue(Boolean.TRUE);
            SerialPortUIController app = (SerialPortUIController) loader.getController();
            SerialPortUIController.controller = app;
            stage.show();
            
        } catch (Exception ex) {
            APPException.showErrorNotifiction(ex, root);
        }
        
        
        
        /*
        root = FXMLLoader.load(getClass().getResource("SerialPortUI.fxml"));
        root.getStylesheets().add(getClass().getResource("/com/enib/ips/project/style.css").toExternalForm());
        Scene scene = new Scene(root);
        
        stage.setScene(scene);
        */
        
    }

    @Override
    public void stop() throws Exception {
        System.out.println("Closed");
        SerialPortUIController.controller.stop();
        super.stop();
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
