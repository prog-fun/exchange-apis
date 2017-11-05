package main;

import java.util.ArrayList;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.ListBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.progfun.BitFinexGDAXParser;
import org.progfun.BitFinexWSClient;
import org.progfun.InvalidFormatException;
import org.progfun.Market;
import org.progfun.orderbook.Listener;
import org.progfun.orderbook.Order;

/**
 *
 */
public class Gui extends Application implements Listener {

    private final TableView table = new TableView();

    TableView table1;
    TableView table2;

    private TableColumn bidPrice;
    private TableColumn bidAmount;
    private TableColumn askPrice;
    private TableColumn askAmount;

    private ArrayList bidList;
    private ArrayList askList;

    private static Gui gui;
    
    public Gui() {
        gui = this;
    }
    
    
    public static void main(String[] args) throws InvalidFormatException {
        
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        
        bidList = new ArrayList<>();
        askList = new ArrayList<>();

        final Label label = new Label("Something");
        label.setFont(new Font("Arial", 20));
        Button btn = new Button("Update");
        btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                table1.setItems(FXCollections.observableArrayList(bidList));
                table2.setItems(FXCollections.observableArrayList(askList));
            }
        });

        table1 = new TableView<>();
        table2 = new TableView<>();

        table1.setEditable(true);
        table2.setEditable(true);

        bidPrice = new TableColumn("Bid Price");
        bidPrice.setCellValueFactory(new PropertyValueFactory("price"));
        bidAmount = new TableColumn("Bid Amount");
        bidAmount.setCellValueFactory(new PropertyValueFactory("amount"));
        askPrice = new TableColumn("Ask Price");
        askPrice.setCellValueFactory(new PropertyValueFactory("price"));
        askAmount = new TableColumn("Ask Amount");
        askAmount.setCellValueFactory(new PropertyValueFactory("amount"));

        ObservableList<Object> ob1 = FXCollections.observableArrayList(bidList);
        ObservableList<Object> ob2 = FXCollections.observableArrayList(askList);

        table1.setItems(ob1);
        table2.setItems(ob2);

        table1.getColumns().addAll(bidPrice, bidAmount);
        table2.getColumns().addAll(askPrice, askAmount);

        HBox hbox = new HBox();
        hbox.getChildren().addAll(table1, table2);
        VBox vbox = new VBox();
        vbox.setSpacing(5);
        vbox.setPadding(new Insets(10, 0, 0, 10));
        vbox.getChildren().addAll(label, btn, hbox);

        Scene scene = new Scene(vbox);
        stage.setTitle("Table");
        stage.setWidth(600);
        stage.setHeight(500);
        stage.setScene(scene);
        stage.show();

        bidList.add(new Order(1000, 2, 5));
        askList.add(new Order(1000, 2, 5));
        
        BitFinexWSClient client = new BitFinexWSClient();
        Thread thread = new Thread(client);
        Market market = new Market("BTC", "USD");
        market.addListener(this);
        client.setMarket(market);
        thread.start();
    }

    @Override
    public void bidAdded(Market market, Order bid) {
        bidList.add(bid);
        table1.setItems(FXCollections.observableArrayList(bidList));
        
    }

    @Override
    public void askAdded(Market market, Order ask) {
        askList.add(ask);
        table2.setItems(FXCollections.observableArrayList(askList));
    }

    @Override
    public void bidUpdated(Market market, Order bid) {
        
    }

    @Override
    public void askUpdated(Market market, Order ask) {
    }

    @Override
    public void bidRemoved(Market market, double price) {
    }

    @Override
    public void askRemoved(Market market, double price) {
    }

}
