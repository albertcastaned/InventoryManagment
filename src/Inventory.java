import java.util.Random;

public class Inventory {
    private Record[] records;
    private final String[] randomNames = {
            "Cheese",
            "Ham",
            "Sausage",
            "Milk",
            "Eggs"
    };

    private void initInventory() {
        final int itemsLength = randomNames.length;
        Random r = new Random();
        Item[] items = new Item[itemsLength];
        for(int i = 0; i < itemsLength; i++) {
            int minPrice = 10;
            int maxPrice = 100;
            float randomPrice = minPrice + r.nextFloat() * (maxPrice - minPrice);
            items[i] = new Item(i, randomNames[i], randomPrice);
        }
        records = new Record[itemsLength];
        for(int i = 0; i < itemsLength; i++) {
            int maxQuantity = 10;
            int minQuantity = 3;
            int randomQuantity = r.nextInt(maxQuantity - minQuantity) + minQuantity;
            records[i] = new Record(items[i], randomQuantity);
        }
    }

    public Inventory(boolean filLDummy) {
        if (filLDummy) {
            initInventory();
        }
    }

    public void setRecords(Record[] records) {
        this.records = records;
    }

    public Record[] getRecords() {
        return records;
    }

    public synchronized boolean orderRecord(int id, int quantity) {
        int originalQuantity = records[id].quantity;
        records[id].quantity = records[id].quantity - quantity;

        if (records[id].quantity < 0) {
            System.out.println("Not enough inventory for this item request");
            records[id].quantity = originalQuantity;
            return false;
        } else {
            System.out.println("Ordered succesfully");
            return true;
        }
    }

    public synchronized void addRecord(int id, int quantity) {
        for (Record record : records) {
            if (record.item.id == id) {
                record.quantity += quantity;
                System.out.println("Added item succesfully");
                return;
            }
        }
        System.out.println("Item not found");
    }

    public Record getRecord(int id) {
        return records[id];
    }
}
