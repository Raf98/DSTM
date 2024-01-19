package DHT;

public interface ExecuteTransaction {
    public void execTransaction(int nServers, int nObjectsServers, int nObjects, int hashTablesEntries, int op, int clientId) throws Exception;
}
