package org.cipango.server.transaction;

public interface TransactionListener 
{
	void transactionTerminated(Transaction transaction);
}
