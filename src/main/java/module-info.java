module com.homoonme.algovisualizer {
    requires javafx.controls;
    requires javafx.fxml;
//    requires com.fasterxml.jackson.databind;, com.fasterxml.jackson.databind;


    opens com.homoonme.algovisualizer to javafx.fxml;
    exports com.homoonme.algovisualizer;
}