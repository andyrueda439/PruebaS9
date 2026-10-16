module ni.edu.uam.proyectocorte2 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens ni.edu.uam.proyectocorte2 to javafx.fxml;
    opens ni.edu.uam.proyectocorte2.controller to javafx.fxml;
    opens ni.edu.uam.proyectocorte2.model to javafx.base;

    exports ni.edu.uam.proyectocorte2;
    exports ni.edu.uam.proyectocorte2.controller;
    exports ni.edu.uam.proyectocorte2.model;
}