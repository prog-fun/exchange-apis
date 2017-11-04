package main;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.progfun.orderbook.Listener;
import org.progfun.orderbook.Order;

/**
 *
 */
public class Gui extends Application implements Listener{
    private final TableView table = new TableView();
    public static void main(String[] args){
        launch(args);
    }
    
    @Override
    public void start(Stage stage) throws Exception {
        Scene scene = new Scene(new Group());
        stage.setTitle("Table");
        stage.setWidth(300);
        stage.setHeight(500);
        
        final Label label = new Label("Something");
        label.setFont(new Font("Arial", 20));
        
        table.setEditable(true);
        
        TableColumn bidPrice = new TableColumn("Bid Price");
        TableColumn bidAmount = new TableColumn("Bid Amount");
        TableColumn askPrice = new TableColumn("Ask Price");
        TableColumn askAmount = new TableColumn("Ask Amount");
        
        table.getColumns().addAll(bidPrice,bidAmount,askPrice,askAmount);
        
        final VBox vbox = new VBox();
        vbox.setSpacing(5);
        vbox.setPadding(new Insets(10,0,0,10));
        vbox.getChildren().addAll(label,table);
        
        ((Group) scene.getRoot()).getChildren().addAll(vbox);
        
        stage.setScene(scene);
        stage.show();
        
    }

    @Override
    public void bidAdded(Order bid) {
        
    }

    @Override
    public void askAdded(Order ask) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void bidUpdated(Order bid) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void askUpdated(Order ask) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void bidRemoved(double price) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void askRemoved(double price) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
    
}
