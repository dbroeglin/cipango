package org.cipango.diameter.util;

public interface Visitable 
{
	void accept(DiameterVisitor visitor);
}
