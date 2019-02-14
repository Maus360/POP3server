import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;

public class Window {

    private TextArea chat = new TextArea();
    private Scene scene;

    Window(){
        scene = new Scene(this.getContent(), 640, 480);
    }

    public Scene getScene(){
        return scene;
    }

    public StackPane getContent(){
        StackPane root = new StackPane();
        chat.setEditable(false);
        root.getChildren().add(chat);
        return root;
    }

    void print(String string){
        chat.setEditable(true);
        chat.setText(chat.getText() + "\n" + string);
        chat.setEditable(false);
    }
}
