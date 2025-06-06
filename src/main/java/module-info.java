module se.lu.ics {
    exports se.lu.ics;
    exports se.lu.ics.controllers;
    exports se.lu.ics.models;
    requires transitive javafx.controls;
    requires transitive javafx.graphics;
    requires javafx.fxml;
    requires transitive java.sql;
    requires java.desktop;
    requires javafx.base;


    opens se.lu.ics.controllers to javafx.fxml;
    opens se.lu.ics.models to javafx.base; 

}   