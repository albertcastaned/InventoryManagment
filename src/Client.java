import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.Socket;
import java.util.Scanner;

abstract class BaseClient {
    protected Socket socket;
    protected Scanner in;
    protected PrintWriter out;
    protected Inventory inventory;

    abstract void initializeGUI(Record[] records);

    abstract void refreshGUI(Record[] records);

    abstract void onOrderUnavailable();

    abstract void run();

    public void fetchRecords() {
        JSONObject object = new JSONObject();
        object.put("Request", Constants.LIST_ACTION);
        out.println(object);
        // Wait for fetch response
        String response = in.nextLine();
        processResponse(response);
    }

    public void orderRecord(int id, int quantity) {
        JSONObject object = new JSONObject();
        object.put("Request", Constants.ORDER_ITEM);
        JSONObject itemObj = new JSONObject();
        itemObj.put("id", id);
        itemObj.put("quantity", quantity);

        object.put("item", itemObj);
        out.println(object);
    }


    protected void processResponse(String response) {
        JSONObject deserialized = new JSONObject(response);
        String request = (String) deserialized.get("Request");
        switch (request) {
            case Constants.LIST_ACTION:
            case Constants.ORDER_ITEM: {
                JSONArray records = deserialized.getJSONArray("records");
                Record[] results = new Record[records.length()];
                for (int i = 0; i < records.length(); i++) {
                    JSONObject json = records.getJSONObject(i);
                    int id = (Integer) json.get("id");
                    String name = (String) json.get("name");
                    float price = ((BigDecimal) json.get("price")).floatValue();
                    int quantity = (Integer) json.get("quantity");

                    Item item = new Item(id, name, price);
                    results[i] = new Record(item, quantity);
                }
                inventory = new Inventory(false);
                inventory.setRecords(results);
                refreshGUI(inventory.getRecords());
                break;
            }
            case Constants.ITEM_UNAVAILABLE:{
                onOrderUnavailable();
            }
            default: {
                break;
            }
        }
    }


    public BaseClient(String address) {
        try {
            int PORT = 6000;
            socket = new Socket(address, PORT);
            System.out.println("Client - Connection established.");

            in = new Scanner(socket.getInputStream());
            out = new PrintWriter(socket.getOutputStream(), true);
            fetchRecords();
            initializeGUI(inventory.getRecords());

            run();

        } catch (IOException error) {
            System.out.println(error);
        }
    }
}

public class Client extends BaseClient {
    private ClientGUI gui;

    public Client(String address) {
        super(address);
    }

    @Override
    void initializeGUI(Record[] records) {
        gui = new ClientGUI(this);
        gui.init(records);
        gui.frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                System.exit(0);
            }
        });
    }

    @Override
    void refreshGUI(Record[] records) {
        if (gui != null) {
            gui.refresh(records);
        }
    }

    @Override
    void onOrderUnavailable() {
        gui.activateNoExistanceDialog();
    }

    @Override
    void run() {
        while (true){
            String response = in.nextLine();
            System.out.println("Response from server: " + response);
            processResponse(response);
        }
    }
}

class ClientMain {
    public static void main(String[] argvs) {
        Client client = new Client("localhost");
    }
}
