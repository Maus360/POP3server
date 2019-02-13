import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Window {
    private StackPane root = new StackPane();
    private TextArea chat = new TextArea();
    Window(Stage stage){
        stage.setTitle("POP3server");
        chat.setEditable(false);
        root.getChildren().add(chat);
        stage.setScene(new Scene(root, 640, 480));
        stage.show();
    }

    void print(String string){
        chat.setEditable(true);
        chat.setText(chat.getText() + "\n" + string);
        chat.setEditable(false);
    }
}
