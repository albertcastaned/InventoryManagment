import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ClientGUI {
    private JTable table;
    private final String[] columnNames = { "Name", "Price", "Quantity", "Quantity to order", "" };
    private final Client client;
    public JFrame frame;
    public ClientGUI(Client client) {
        this.client = client;
    }

    public JPanel content;
    private Action order;


    public void init(Record[] records) {
        frame = new JFrame("Inventory - Client");

        DefaultTableModel model = getTableModel(records);
        table = new JTable(model);

        table.setBounds(30,40,200,300);
        order = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                JTable table = (JTable)e.getSource();
                int modelRow = Integer.parseInt(e.getActionCommand());
                String quantityObj = (String) (table.getModel()).getValueAt(modelRow, 3);
                try {
                    int quantity = Integer.parseInt(quantityObj);
                    client.orderRecord(modelRow, quantity);
                } catch (NumberFormatException error) {
                    error.printStackTrace();
                }
            }
        };

        new ButtonColumn(table, order, 4);

        content = new JPanel();
        BoxLayout boxLayout = new BoxLayout(content, BoxLayout.Y_AXIS);
        content.setLayout(boxLayout);

        JScrollPane scrollPane = new JScrollPane(table);
        JButton button = new JButton("Refresh");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.fetchRecords();
            }
        });

        content.add(scrollPane);
        content.add(button);

        frame.add(content);
        frame.setSize(800, 800);
        frame.show();
    }

    public void activateNoExistanceDialog() {
        JOptionPane.showMessageDialog(frame,
                "Order was not able to be created. Verify that there the requested items are available",
                "Order error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private DefaultTableModel getTableModel(Record[] records) {
        final int recordsLength = records.length;
        final int columns = columnNames.length;
        Object[][] data = new Object[recordsLength][columns];

        for(int i = 0; i < recordsLength; i++) {
            data[i][0] = records[i].item.name;
            data[i][1] = Float.toString(records[i].item.price);
            data[i][2] = Integer.toString(records[i].quantity);
            data[i][3] = "0";
            data[i][4] = "Order " + records[i].item.name;
        }
        return new DefaultTableModel(data, columnNames);
    }

    public void refresh(Record[] records) {
        System.out.println("Refresh");
        DefaultTableModel model = getTableModel(records);
        table.setModel(model);
        new ButtonColumn(table, order, 4);
        model.fireTableDataChanged();
    }
}

