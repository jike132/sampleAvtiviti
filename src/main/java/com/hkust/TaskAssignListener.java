package com.hkust;

import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.delegate.event.impl.ActivitiEntityEventImpl;
import org.activiti.engine.task.DelegationState;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Steps to Implement Automatic Agents:
 * 1. Configure the listener in the process scope to listen for Task events
 * 2. When the task created, to determine whether the recipient leave, if 
 * 	  it is agents to the agent
 * 3. When the agent completes the delegation task, the TASK ASSIGNED event 
 *    fires, and the task is RESOLVED automatically
 *
 */
public class TaskAssignListener implements ActivitiEventListener {
	final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Override
	public void onEvent(ActivitiEvent event) {

		Task t = (Task) ((ActivitiEntityEventImpl) event).getEntity();

		switch (event.getType()) {

		case TASK_ASSIGNED:
			/**
			 * Triggered when the agent completes
			 * (taskService.resolveTask(taskId))
			 * Now the state of the task is DelegationState.RESOLVED
			 * The task will be returned to the original assignee
			 * Here will demonstrate the automatic completion of the task
			 */
			if (t.getDelegationState() == DelegationState.RESOLVED) {
				logger.debug("auto complete");
				event.getEngineServices().getTaskService().complete(t.getId());
			}

			break;
		case TASK_CREATED:

			//implement 1:Assignee
			if("Ade".equals(t.getAssignee())){
				logger.debug("Ade delegete");
				t.setAssignee("Tony");
				break;
			}
						
			//implement 2：Delegate
			if ("Quoze".equals(t.getAssignee())) {
				logger.debug("Quoze delegete");
				t.delegate("Bobo");
				break;
			}
			break;
		default:
			System.out.println("Other Event received: " + event.getType());
		}
	}

	@Override
	public boolean isFailOnException() {
		// The logic in the onEvent method of this listener is not critical,
		// exceptions
		// can be ignored if logging fails...
		return false;
	}

}
