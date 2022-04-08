module Main {
    requires javafx.controls;
    requires javafx.fxml;
    requires xstream;


    opens Main to javafx.fxml,xstream;
    exports Main;
}