import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.sql.Timestamp;

public class ServerGUI {
    private JTable table;
    private JList logs;
    private final String[] columnNames = { "Nombre", "Precio", "Cantidad", "Quantity to add", "" };
    public JFrame frame;
    private Action order;
    private final Server server;

    public ServerGUI(Server server) {
        this.server = server;
    }

    public void init(Record[] records) {
        frame = new JFrame("Inventory - Server");

        DefaultTableModel model = getTableModel(records);
        table = new JTable(model);
        order = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                JTable table = (JTable)e.getSource();
                int modelRow = Integer.parseInt(e.getActionCommand());
                String quantityObj = (String) (table.getModel()).getValueAt(modelRow, 3);
                try {
                    int quantity = Integer.parseInt(quantityObj);
                    server.inventory.addRecord(modelRow, quantity);
                    refresh(server.inventory.getRecords());
                } catch (NumberFormatException error) {
                    error.printStackTrace();
                }
            }
        };
        new ButtonColumn(table, order, 4);

        JPanel content = new JPanel();
        BoxLayout boxLayout = new BoxLayout(content, BoxLayout.Y_AXIS);
        content.setLayout(boxLayout);

        JScrollPane scrollPane = new JScrollPane(table);
        DefaultListModel<String> list = new DefaultListModel<String>();
        logs = new JList(list);
        content.add(scrollPane);
        content.add(logs);
        frame.add(content);
        frame.setSize(1200, 1000);
        frame.show();
    }

    private DefaultTableModel getTableModel(Record[] records) {
        final int recordsLength = records.length;
        final int columns = columnNames.length;
        Object[][] data = new String[recordsLength][columns];

        for(int i = 0; i < recordsLength; i++) {
            data[i][0] = records[i].item.name;
            data[i][1] = Float.toString(records[i].item.price);
            data[i][2] = Integer.toString(records[i].quantity);
            data[i][3] = "0";
            data[i][4] = "Add " + records[i].item.name;
        }
        DefaultTableModel model = new DefaultTableModel(data, columnNames);
        return model;
    }

    public void addLog(String log) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        DefaultListModel list = (DefaultListModel) logs.getModel();
        list.addElement(log + " - " + timestamp);
        logs = new JList(list);
    }

    public void refresh(Record[] records) {
        System.out.println("Refresh");
        DefaultTableModel model = getTableModel(records);
        table.setModel(model);
        new ButtonColumn(table, order, 4);
        model.fireTableDataChanged();;
    }
}