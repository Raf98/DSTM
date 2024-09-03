package DSTMBenchmark.GenericDSTM;

import DSTMBenchmark.*;

public interface ExecTransaction {
	
	public void execTransaction(RObject[] objects, int op, int contentionManager, boolean usesDynamicParams, 
								int maxAborts_minDelay_delay, int maxDelay_intervals) throws Exception;
}

