import java.util.HashMap;
import java.util.Map;

interface InMemoryDB {
    int get(String key);

    void put(String key, int val);

    void begin_transaction();

    void commit();

    void rollback();
}

public class InMemoryDatabase implements InMemoryDB {
    private Map<String, Integer> database;
    private Map<String, Integer> transactionSnapshot;
    private boolean inTransaction;

    public InMemoryDatabase() {
        database = new HashMap<>();
        inTransaction = false;
    }

    @Override
    public int get(String key) {
        return database.getOrDefault(key, 0);
    }

    @Override
    public void put(String key, int val) {
        if (!inTransaction) {
            throw new IllegalStateException("No transaction in progress");
        }
        transactionSnapshot.put(key, val);
    }

    @Override
    public void begin_transaction() {
        if (inTransaction) {
            throw new IllegalStateException("Transaction already in progress");
        }
        inTransaction = true;
        transactionSnapshot = new HashMap<>(database);
    }

    @Override
    public void commit() {
        if (!inTransaction) {
            throw new IllegalStateException("No transaction in progress");
        }
        database.clear();
        database.putAll(transactionSnapshot);
        endTransaction();
    }

    @Override
    public void rollback() {
        if (!inTransaction) {
            throw new IllegalStateException("No transaction in progress");
        }
        endTransaction();
    }

    private void endTransaction() {
        inTransaction = false;
        transactionSnapshot = null;
    }

    public static void main(String[] args) {
        InMemoryDatabase inmemoryDB = new InMemoryDatabase();

        // should return null, because A doesn’t exist in the DB yet
        System.out.println(inmemoryDB.get("A"));

        // should throw an error because a transaction is not in progress
        try {
            inmemoryDB.put("A", 5);
        } catch (IllegalStateException e) {
            System.out.println(e.getMessage());
        }

        // starts a new transaction
        inmemoryDB.begin_transaction();

        // set’s value of A to 5, but its not committed yet
        inmemoryDB.put("A", 5);

        // should return null, because updates to A are not committed yet
        System.out.println(inmemoryDB.get("A"));

        // update A’s value to 6 within the transaction
        inmemoryDB.put("A", 6);

        // commits the open transaction
        inmemoryDB.commit();

        // should return 6, that was the last value of A to be committed
        System.out.println(inmemoryDB.get("A"));

        // throws an error, because there is no open transaction
        try {
            inmemoryDB.commit();
        } catch (IllegalStateException e) {
            System.out.println(e.getMessage());
        }

        // throws an error because there is no ongoing transaction
        try {
            inmemoryDB.rollback();
        } catch (IllegalStateException e) {
            System.out.println(e.getMessage());
        }

        // should return null because B does not exist in the database
        System.out.println(inmemoryDB.get("B"));

        // starts a new transaction
        inmemoryDB.begin_transaction();

        // Set key B’s value to 10 within the transaction
        inmemoryDB.put("B", 10);

        // Rollback the transaction - revert any changes made to B
        inmemoryDB.rollback();

        // Should return null because changes to B were rolled back
        System.out.println(inmemoryDB.get("B"));
    }
}
