package DSTMBenchmark.GenericDSTM;

public interface SaveData {
    public void saveData(int clientid, ExecTransaction trans) throws Exception;
}
