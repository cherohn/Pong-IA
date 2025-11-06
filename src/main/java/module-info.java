module com.pongai.game.pongai {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.pongai.game.pongai to javafx.fxml;
    exports com.pongai.game.pongai;
}