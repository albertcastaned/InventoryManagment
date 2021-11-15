import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ServerThread extends Thread {
    protected final Socket socket;
    protected final BaseServer server;

    public ServerThread(BaseServer server, Socket socket) {
        this.socket = socket;
        this.server = server;
    }

    private String processRequest(String request) {
        JSONObject deserialized = new JSONObject(request);
        String requestType = (String) deserialized.get("Request");
        JSONObject response = new JSONObject();
        response.put("Request", requestType);
        switch (requestType) {
            case Constants.LIST_ACTION -> {
                Record[] records = server.getRecords();
                JSONArray array = new JSONArray();
                for (Record record : records) {
                    JSONObject obj = new JSONObject();
                    obj.put("id", record.item.id);
                    obj.put("name", record.item.name);
                    obj.put("price", record.item.price);
                    obj.put("quantity", record.quantity);
                    array.put(obj);
                }
                response.put("records", array);
                server.addLog("List request from: " + socket.getInetAddress().getHostName());
                return response.toString();
            }
            case Constants.ORDER_ITEM -> {
                JSONObject itemObj = (JSONObject) deserialized.get("item");
                int id = itemObj.getInt("id");
                int quantity = itemObj.getInt("quantity");
                server.addLog("Order request ID: " + id + " Quantity: " + quantity + " from: " + socket.getInetAddress().getHostName());

                boolean success = server.order(id, quantity);
                if (success) {
                    Record[] records = server.getRecords();
                    JSONArray array = new JSONArray();
                    for (Record record : records) {
                        JSONObject obj = new JSONObject();
                        obj.put("id", record.item.id);
                        obj.put("name", record.item.name);
                        obj.put("price", record.item.price);
                        obj.put("quantity", record.quantity);
                        array.put(obj);
                    }
                    response.put("records", array);
                    server.refresh(server.getRecords());
                    return response.toString();
                } else {
                    JSONObject error = new JSONObject();
                    error.put("Request" , Constants.ITEM_UNAVAILABLE);
                    return error.toString();
                }
            }
            default -> {
                System.out.println("Unknown request type");
                return "Unknown request";
            }
        }
    }

    public void run() {
        System.out.println("Thread started");
        Scanner in;

        PrintWriter out;
        try {
            in = new Scanner(socket.getInputStream());
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException error) {
            error.printStackTrace();
            return;
        }
        String message;
        while(in.hasNextLine()) {
            message = in.nextLine();

            System.out.println("Received request: " + message);
            String response = processRequest(message);
            out.println(response);
        }
    }
}
