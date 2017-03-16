package com.hkust.bean;

import java.util.Set;

import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SignComplete {
	private final Logger logger = LoggerFactory.getLogger(SignComplete.class);
	
    public boolean isComplete(ActivityExecution execution) {

    	logger.debug("enter the SignComplete isComplete method...");
        
        //String nodeId=execution.getActivity().getId();
        //String actInstId=execution.getProcessInstanceId();
          
        //The number of votes to complete
        Integer completeCounter=(Integer)execution.getVariable("nrOfCompletedInstances");
        //Total number of cycles
        Integer instanceOfNumbers=(Integer)execution.getVariable("nrOfInstances");
        
        //If the number of completions is reached
        if(instanceOfNumbers - completeCounter <= 1) {
        	logger.debug("Reject, {} - {} <= 1",instanceOfNumbers,completeCounter);
        	execution.setVariableLocal("result", "N");
        	return true;
        }
        
        //Calculate the number of people who have been approved
        Set<String> vars = execution.getVariableNames();
        
        int approve = 0;
        for(String var : vars) {
        	if(var.startsWith("signResult_")) {
        		logger.debug("{}",var);
        		if("Y".equals((String)execution.getVariable(var))){
        			approve++;
        		}
        	}
        }
        
        //If the number is greater than or equal to 2, pass the approval
        if(approve>=2) {
        	logger.debug("Approve, sign = {}",approve);
        	execution.setVariableLocal("result", "Y");
        	return true;
        }
        
        //default is reject 
        return false;
    }
}
