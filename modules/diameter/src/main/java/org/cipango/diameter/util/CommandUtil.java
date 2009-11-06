package org.cipango.diameter.util;

import org.cipango.diameter.DiameterCommand;
import org.cipango.diameter.Dictionary;
import org.cipango.diameter.Factory;

public class CommandUtil 
{
	public static DiameterCommand getAnswer(DiameterCommand request)
	{
		assert request.isRequest();
		
		DiameterCommand answer = Dictionary.getInstance().getAnswer(request.getCode());
		if (answer == null)
			answer = Factory.newAnswer(request.getCode(), "Unknown-Answer");
		return answer;
	}
}
