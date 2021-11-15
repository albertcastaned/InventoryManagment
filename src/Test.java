import java.util.ArrayList;

class TestClient extends BaseClient {
    private Thread thread;
    public TestClient(String address, Thread thread) {
        super(address);
    }

    @Override
    void initializeGUI(Record[] records) {
        // Do nothing
    }

    @Override
    void refreshGUI(Record[] records) {
        // Do nothing
    }

    @Override
    void onOrderUnavailable() {
        // Do nothing
    }

    @Override
    void run() {
        while (true) {
            try {
                int randomID = (int) ((Math.random() * (4)));
                int randomQuantity = (int) ((Math.random() * (2 - 1)) + 1);

                System.out.println("Ordering " + randomID + " quantity " + randomQuantity);
                orderRecord(randomID, randomQuantity);
                String response = in.nextLine();
                processResponse(response);

                thread.sleep((int) ((Math.random() * (6000 - 3000)) + 3000));
            } catch (InterruptedException error) {
                error.printStackTrace();
            }
        }
    }
}

class TestClientThread extends Thread {
    public void run() {
        TestClient client = new TestClient("localhost", this);
    }
}

class TestServer extends BaseServer {
    @Override
    void addLog(String log) {
        System.out.println(log);
    }

    @Override
    void refresh(Record[] records) {
        // Do nothing
    }

    @Override
    public boolean order(int id, int quantity) {
        boolean success = inventory.orderRecord(id, quantity);
        Record[] records = inventory.getRecords();
        for(Record record : records){
            System.out.println(record.item.name + " - " + record.quantity);
        }
        if(inventory.getRecord(id).quantity < 0) {
            System.out.println("CONCURRENCY ERROR");
            System.exit(1);
        }
        System.out.println("\n");

        return success;
    }
}


class TestServerThread extends Thread {
    public void run() {
        TestServer server = new TestServer();
        server.inventory = new Inventory(true);
        TestServerAdderThread adder = new TestServerAdderThread(server);
        adder.start();
        server.init();
    }
}

class TestServerAdderThread extends Thread {
    private TestServer server;
    public TestServerAdderThread(TestServer server){
        this.server = server;
    }

    public void run() {
        while (true) {
            if (server.inventory == null) continue;
             int randomID = (int) ((Math.random() * (3)));
            int randomQuantity = (int) ((Math.random() * (5 - 1)) + 1);
            System.out.println("Adding product " + randomID + " Quantity " + randomQuantity);

            server.inventory.addRecord(randomID, randomQuantity);
            try {
                this.sleep((int) ((Math.random() * (5000 - 1000)) + 1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

public class Test {
    public static void main(String[] args)  {
        ArrayList<TestClientThread> clientThreads = new ArrayList();
        TestServerThread serverThread = new TestServerThread();
        int clientThreadsNum = 50;

        serverThread.start();

        for (int i = 0; i < clientThreadsNum; i++) {
            TestClientThread thread = new TestClientThread();
            clientThreads.add(thread);

            thread.start();
        }
    }
}
