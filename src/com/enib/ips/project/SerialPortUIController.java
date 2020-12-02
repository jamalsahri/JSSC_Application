/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.enib.ips.project;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.GaugeBuilder;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import jssc.SerialPort;
import static jssc.SerialPort.MASK_RXCHAR;
import jssc.SerialPortEvent;
import jssc.SerialPortException;
import jssc.SerialPortList;
import jssc.SerialPortTimeoutException;
import org.controlsfx.control.textfield.TextFields;

/**
 * FXML Controller class
 *
 * @author Jamal
 */
public class SerialPortUIController implements Initializable {
    @FXML
    private FontAwesomeIconView fai_connectionState;
    @FXML
    private JFXComboBox<String> cb_serialLine;
    @FXML
    private JFXTextField tf_speed;
    @FXML
    private JFXTextField tf_timeout;
    @FXML
    private JFXTextField tf_dataBits;
    @FXML
    private JFXTextField tf_stopBits;
    @FXML
    private JFXComboBox<String> cb_parity;
    @FXML
    private JFXButton btn_startCommunication;
    @FXML
    private JFXButton btn_stopCommunication;
    @FXML
    private AreaChart<String, Integer> areaChart;
    @FXML
    private Label lbl_receivedValue;
    @FXML
    private HBox hbox_gaugeZone;

    FadeTransition ft = new FadeTransition();
    public static SerialPortUIController controller = null;
    private String st;
    
    private HashMap<String, Integer> parityMap;
    private ObservableList<String> portsList;
    private SerialPort stmPort = null;
    private String port = "COM1";
    private int speed = SerialPort.BAUDRATE_9600;
    private int timeout = 1000;
    private int dataBits = SerialPort.DATABITS_8;
    private int stopBits = SerialPort.STOPBITS_1;
    private int parity = SerialPort.PARITY_NONE;
    
    
    XYChart.Series<String, Integer> series;
    
    
    
    private Gauge    steps;
    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        try{
            changeConnectionStateLabel(false);
            configAreaChart();
            configSerialLine();
            configSpeed();
            configTimeout();
            configDataBits();
            configStopBits();
            configParity();
            /* FUNCTION */
            
            //GaugeBuilder builder = GaugeBuilder.create().skinType(Gauge.SkinType.GAUGE);         
            //steps          = builder.decimals(0).maxValue(10000).unit("STEPS").build(); 
            //VBox stepsBox        = getTopicBox("STEPS", Color.rgb(77,208,225), steps);
            //hbox_gaugeZone.getChildren().add(stepsBox);
            /************/
        }catch(Exception ex){
            APPException.showErrorNotifiction(ex, (StackPane)IPS_Project.getRoot());
        }
        
    }   
    
    private VBox getTopicBox(final String TEXT, final Color COLOR, final Gauge GAUGE) {         
        Rectangle bar = new Rectangle(200, 3);       
        bar.setArcWidth(6);    
        bar.setArcHeight(6);      
        bar.setFill(COLOR);       
        Label label = new Label(TEXT);      
        label.setTextFill(COLOR);      
        label.setAlignment(Pos.CENTER);     
        label.setPadding(new Insets(0, 0, 10, 0));  
        GAUGE.setBarColor(COLOR);       
        GAUGE.setBarBackgroundColor(Color.rgb(39,44,50));    
        GAUGE.setAnimated(true);      
        VBox vBox = new VBox(bar, label, GAUGE);    
        vBox.setSpacing(3);     
        vBox.setAlignment(Pos.CENTER);         
        return vBox;     
    }
    
    public void stop(){
        stopCommunication(null);
    }
    
    int i=0;
    String tmp;
    @FXML
    private void startCommunication(ActionEvent event) {
        System.out.println("Port:"+port+" speed:"+speed+" timeout:"+timeout+" databits:"+dataBits+" stopbits:"+stopBits+" parity:"+parity);
        String messageError=null;
        if(stmPort != null) return;
        try{
            SerialPort serialPort = new SerialPort(port);
            messageError = "Cannot to open serial communication on port ["+port+"]";
            serialPort.openPort();
            serialPort.setParams(speed,dataBits,stopBits,parity);
            serialPort.setEventsMask(MASK_RXCHAR);
            
            serialPort.addEventListener((SerialPortEvent serialPortEvent) -> {
                
                if(serialPortEvent.isRXCHAR()){
                    try {  
                        st = serialPort.readString(1024, 1000);//serialPort.readString(8, 1000);
                        Platform.runLater(() -> {
                                System.out.println(">"+st);
                                lbl_receivedValue.setText(st);
                                tmp = st;
                                    Platform.runLater(() -> {
                                        if(!tmp.equals("")){
                                            int d = Integer.parseInt(tmp.trim());
                                            series.getData().add(new XYChart.Data<>(d+"", d));
                                        }
                                        
                                        //System.out.println("2Th:> "+st);
                                        //steps.setValue(Double.parseDouble(st));
                                    });
                                //series.getData().add(new XYChart.Data<>(i+"", i)); i++;
                        });
                        
                    } catch (SerialPortTimeoutException | SerialPortException ex) {
                        APPException.showErrorNotifiction(ex, (StackPane)IPS_Project.getRoot());
                    }
                    
                }
            });
            changeConnectionStateLabel(true);
            stmPort = serialPort;
        }catch(Exception ex){
            APPException.showErrorNotifiction(new Exception(messageError), (StackPane)IPS_Project.getRoot());
        }
                
    }

    
    @FXML
    private void stopCommunication(ActionEvent event) {
        try{
            APPException.test((StackPane)IPS_Project.getRoot());
        }catch(Exception e){
            APPException.showErrorNotifiction(e, (StackPane)IPS_Project.getRoot());
        }
        /*
        if(stmPort!=null){
            try {
                stmPort.removeEventListener();
                if(stmPort.isOpened()){
                    stmPort.closePort();
                }
                changeConnectionStateLabel(false);
                stmPort = null;
            } catch (SerialPortException ex) {
                APPException.showErrorNotifiction(ex, (StackPane)IPS_Project.getRoot());
            }
        }*/
    }
    
    private void changeConnectionStateLabel(final boolean state){
        if(state){
            fai_connectionState.setIcon(FontAwesomeIcon.LINK);
            fai_connectionState.setStyle("-fx-fill:#54A6FB;");
            notificationSign();
        }else{
            fai_connectionState.setIcon(FontAwesomeIcon.CHAIN_BROKEN);
            fai_connectionState.setStyle("-fx-fill:red;");
            ft.stop();
        }
    }
    
    public void  notificationSign(){
        ft.setNode(fai_connectionState);
        ft.setDuration(new Duration(500));
        ft.setFromValue(0.5);
        ft.setToValue(100.0);
        ft.setCycleCount(1000000);
        ft.setAutoReverse(true);
        //fai_connectionState.setStyle("-fx-fill:#3A01DF;");
        ft.play();
    }
    
    private void configAreaChart(){
        series = new XYChart.Series<>();
        series.setName("DATA GRAPHE IN REAL TIME");
        /*series.getData().add(new XYChart.Data<>("A", 10));
        series.getData().add(new XYChart.Data<>("B", 27));
        series.getData().add(new XYChart.Data<>("C", 32));
        series.getData().add(new XYChart.Data<>("D", 11));
        series.getData().add(new XYChart.Data<>("E", 2));
        series.getData().add(new XYChart.Data<>("F", 17));
        series.getData().add(new XYChart.Data<>("G", 18));
        series.getData().add(new XYChart.Data<>("H", 19));
        series.getData().add(new XYChart.Data<>("I", 20));
        series.getData().add(new XYChart.Data<>("J", 45));
        series.getData().add(new XYChart.Data<>("K", 69));
        series.getData().add(new XYChart.Data<>("L", 50));
        series.getData().add(new XYChart.Data<>("M", 49));*/
        
        areaChart.getData().setAll(series);
        areaChart.setCreateSymbols(true);
    }
    
    private void configSerialLine(){
        try{
            detectPort();
            cb_serialLine.setItems(portsList);
            cb_serialLine.valueProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                if(!newValue.equals(oldValue) && !newValue.isEmpty()){
                    System.out.println("Port selected: "+newValue);
                    port = newValue;
                }
            });
            cb_serialLine.getSelectionModel().selectFirst();
        }catch(Exception ex){
            APPException.showErrorNotifiction(ex, (StackPane)IPS_Project.getRoot());
        }
    }
    
    private void configSpeed(){
        try{
            ArrayList<Integer> sugg = new ArrayList<>();
            sugg.add(SerialPort.BAUDRATE_115200);
            sugg.add(SerialPort.BAUDRATE_256000);
            sugg.add(SerialPort.BAUDRATE_9600);
            sugg.add(SerialPort.BAUDRATE_19200);
            sugg.add(SerialPort.BAUDRATE_14400);
            TextFields.bindAutoCompletion(tf_speed, sugg);
            tf_speed.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue)->{
                if(!newValue.equals(oldValue) && !newValue.isEmpty()){
                    try{
                        Integer speedValue = Integer.parseInt(tf_speed.getText().trim());
                        System.err.println("Speed: "+speedValue);
                        speed = speedValue;
                    }catch(Exception ex){
                        Platform.runLater(tf_speed::clear);
                        APPException.showErrorNotifiction(ex, (StackPane)IPS_Project.getRoot());
                    }
                }
            });
        }catch(Exception ex){
            APPException.showErrorNotifiction(ex, (StackPane)IPS_Project.getRoot());
        }
    }
    
    private void configTimeout(){
        try{
            tf_timeout.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue)->{
                if(!newValue.equals(oldValue) && !newValue.isEmpty()){
                    try{
                        Integer timeoutValue = Integer.parseInt(tf_timeout.getText().trim());
                        System.err.println("Speed: "+timeoutValue);
                        timeout = timeoutValue;
                    }catch(Exception ex){
                        Platform.runLater(tf_timeout::clear);
                        APPException.showErrorNotifiction(ex, (StackPane)IPS_Project.getRoot());
                    }
                }
            });
        }catch(Exception ex){
            APPException.showErrorNotifiction(ex, (StackPane)IPS_Project.getRoot());
        }
    }
    
    private void configDataBits(){
        try{
            ArrayList<Integer> sugg = new ArrayList<>();
            sugg.add(SerialPort.DATABITS_5);
            sugg.add(SerialPort.DATABITS_6);
            sugg.add(SerialPort.DATABITS_7);
            sugg.add(SerialPort.DATABITS_8);
            TextFields.bindAutoCompletion(tf_dataBits, sugg);
            tf_dataBits.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue)->{
                if(!newValue.equals(oldValue) && !newValue.isEmpty()){
                    try{
                        Integer databitsValue = Integer.parseInt(tf_dataBits.getText().trim());
                        System.err.println("Speed: "+databitsValue);
                        dataBits = databitsValue;
                    }catch(Exception ex){
                        Platform.runLater(tf_dataBits::clear);
                        APPException.showErrorNotifiction(ex, (StackPane)IPS_Project.getRoot());
                    }
                }
            });
        }catch(Exception ex){
            APPException.showErrorNotifiction(ex, (StackPane)IPS_Project.getRoot());
        }
    }
    
    private void configStopBits(){
        try{
            ArrayList<Integer> sugg = new ArrayList<>();
            sugg.add(SerialPort.STOPBITS_1);
            sugg.add(SerialPort.STOPBITS_1_5);
            sugg.add(SerialPort.STOPBITS_2);
            TextFields.bindAutoCompletion(tf_stopBits, sugg);
            tf_stopBits.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue)->{
                if(!newValue.equals(oldValue) && !newValue.isEmpty()){
                    try{
                        Integer stopbitsValue = Integer.parseInt(tf_stopBits.getText().trim());
                        System.err.println("Speed: "+stopbitsValue);
                        stopBits = stopbitsValue;
                    }catch(Exception ex){
                        Platform.runLater(tf_stopBits::clear);
                        APPException.showErrorNotifiction(ex, (StackPane)IPS_Project.getRoot());
                    }
                }
            });
        }catch(Exception ex){
            APPException.showErrorNotifiction(ex, (StackPane)IPS_Project.getRoot());
        }
    }
    
    private void configParity(){
        try{
            parityMap = new HashMap<>();
            parityMap.put("PARITY_EVEN", SerialPort.PARITY_EVEN);
            parityMap.put("PARITY_MARK", SerialPort.PARITY_MARK);
            parityMap.put("PARITY_NONE", SerialPort.PARITY_NONE);
            parityMap.put("PARITY_ODD", SerialPort.PARITY_ODD);
            parityMap.put("PARITY_SPACE", SerialPort.PARITY_SPACE);
            ObservableList<String> parityList = FXCollections.observableArrayList(parityMap.keySet());
            cb_parity.setItems(parityList);
            cb_parity.valueProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                if(!newValue.equals(oldValue)){
                    System.out.println("Parity: "+newValue);
                    parity = parityMap.get(newValue);
                }
            });
        }catch(Exception ex){
            APPException.showErrorNotifiction(ex, (StackPane)IPS_Project.getRoot());
        }
    }
    
    private void detectPort(){
        portsList = FXCollections.observableArrayList();
        String[] serialPortNames = SerialPortList.getPortNames();
        for(String name : serialPortNames){
            System.out.println(name);
            portsList.add(name);
        }
    }
}
