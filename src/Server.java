import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

abstract class BaseServer {
    private Socket socket;
    private ServerSocket server;
    protected Inventory inventory;

    private final int PORT = 6000;

    abstract void addLog(String log);

    abstract void refresh(Record[] records);

    void init() {
        try {
            server = new ServerSocket(PORT);
        } catch (IOException error) {
            error.printStackTrace();
        }
        System.out.println("Server started");
        System.out.println("Waiting for clients...");

        while(true) {
            try {
                socket = server.accept();
                System.out.println("Server - Connection establilshed.");
            } catch (IOException error) {
                System.out.println(error);
            }
            new ServerThread(this, socket).start();
        }
    }

    public boolean order(int id, int quantity) {
        return inventory.orderRecord(id, quantity);
    }

    public Record[] getRecords() {
        return inventory.getRecords();
    }
}

public class Server extends BaseServer {
    private ServerGUI gui;

    void initializeGui() {
        ServerGUI gui = new ServerGUI(this);
        this.gui = gui;
        gui.init(inventory.getRecords());
        gui.frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }

    @Override
    void addLog(String log) {
        gui.addLog(log);
    }

    @Override
    void refresh(Record[] records) {
        gui.refresh(records);
    }

    public Server() {
        inventory = new Inventory(true);
        initializeGui();
        super.init();
    }
}

class ServerMain {
    public static void main(String argv[]) {
        Server server = new Server();
    }
}
