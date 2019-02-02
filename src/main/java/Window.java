import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Window {
    Window(Stage stage){
        stage.setTitle("POP3server");
        TextArea chat = new TextArea();
        chat.setEditable(false);
        StackPane root = new StackPane();
        root.getChildren().add(chat);
        stage.setScene(new Scene(root, 640, 480));
        stage.show();
    }
}
