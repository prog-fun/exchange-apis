package main;

import javafx.application.Application;
import javafx.beans.binding.ListBinding;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.progfun.Market;
import org.progfun.orderbook.Listener;
import org.progfun.orderbook.Order;

/**
 *
 */
public class Gui extends Application implements Listener{
    private final TableView table = new TableView();
    private TableColumn bidPrice;
    private TableColumn bidAmount;
    private TableColumn askPrice;
    private TableColumn askAmount;
    private ObservableList<Order> list;
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
        
        bidPrice = new TableColumn("Bid Price");
        bidAmount = new TableColumn("Bid Amount");
        askPrice = new TableColumn("Ask Price");
        askAmount = new TableColumn("Ask Amount");
        
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
    public void bidAdded(Market market, Order bid) {
        bidPrice.setCellValueFactory(
            new PropertyValueFactory("price"));
    }

    @Override
    public void askAdded(Market market, Order ask) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void bidUpdated(Market market, Order bid) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void askUpdated(Market market, Order ask) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void bidRemoved(Market market, double price) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void askRemoved(Market market, double price) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
}
