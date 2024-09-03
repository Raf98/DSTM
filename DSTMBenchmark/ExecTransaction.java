// Universidade Federal de Pelotas 2022
// This work is licensed under a Creative Commons
package DSTMBenchmark;

public interface ExecTransaction {

	public void execTransaction(RObject[] objects, int op, int contentionManager) throws Exception;
}
