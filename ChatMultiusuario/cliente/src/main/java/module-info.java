module com.cliente {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.cliente to javafx.fxml;
    exports com.cliente;
}
