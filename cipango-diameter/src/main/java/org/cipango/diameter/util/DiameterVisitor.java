package org.cipango.diameter.util;

import org.cipango.diameter.AVP;
import org.cipango.diameter.AVPList;
import org.cipango.diameter.node.DiameterMessage;

public interface DiameterVisitor 
{
	void visit(DiameterMessage message);
	void visit(AVP<?> avp);
	
	void visitEnter(AVP<AVPList> avp);
	void visitLeave(AVP<AVPList> avp);
}
