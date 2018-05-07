import java.io.InputStream;
import java.sql.SQLException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;


public class InterfaceUsuario extends Application{
	 
	@Override
    public void start(Stage mainWindow) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("telaTrabalho.fxml"));
        mainWindow.setTitle("Calculator");
        mainWindow.setScene(new Scene(root, 800, 600));
        mainWindow.setResizable(false);
        mainWindow.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
