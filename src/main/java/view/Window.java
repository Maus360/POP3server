package view;

import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;

public class Window {

    private TextArea chat = new TextArea();
    private Scene scene;

    public Window(){
        scene = new Scene(this.getContent(), 640, 480);
    }

    public Scene getScene(){
        return scene;
    }

    public StackPane getContent(){
        StackPane root = new StackPane();
        root.getChildren().add(chat);
        return root;
    }

    public void print(String string){
        javafx.application.Platform.runLater(() -> chat.setText(chat.getText() + "\n" + string) );
    }
}
