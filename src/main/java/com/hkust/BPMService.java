package com.hkust;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.DelegationState;
import org.activiti.engine.task.Task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hkust.bean.User;
import com.hkust.dao.DeptRepository;
import com.hkust.domain.Approver;
import com.hkust.domain.Dept;

/**
 * The service layer encapsulates the functionality of the Activiti process
 * 
 */

@Service
public class BPMService {
	private final Logger logger = LoggerFactory.getLogger(BPMService.class);
	/**
	 * Activiti provides run-time services that can be used to manage process:
	 * send or listen sign/event/message
	 * manage user/group, process variables
	 */
	@Autowired
	private RuntimeService runtimeService;
	/**
	 * Activiti provides task services that related task:
	 * start new task, add comment, add attachment
	 * assign user, complete task
	 */
	@Autowired
	private TaskService taskService;
	/**
	 * Activiti provides form services that related form:
	 * get form data
	 * submit form data
	 */
	@Autowired
	private FormService formService;
	/**
	 * Activiti provides history services that can research task history:
	 */
	@Autowired
	private HistoryService historyService;
	
	@Autowired
	private DeptRepository deptRepository;
	
	/**
	 * start process
	 * 
	 * @param processName   process definition name 
	 * @param username		user name that set as process variable
	 * @return 				return process instance id
	 * @throws Exception
	 */
	public String startProcess(final String processName, final String username) throws Exception {
		Map<String, Object> vars = new HashMap<String, Object>();
		
		vars.put("starter", username);
		vars.put("donation", null);
		
		//start process and set variables
		ProcessInstance instance = runtimeService.startProcessInstanceByKey(processName, vars);
		if (instance == null) {
			throw new Exception("Can not start process " + processName);
		}
		return instance.getId();
	}

	/**
	 * stop process by process instance id
	 * @param taskId	process instance id
	 */
	public void deleteProcess(final String taskId) {
		//find process instance id by task id
		String processId = getProcessId(taskId);
		//stop process and remark a reason
		runtimeService.deleteProcessInstance(processId, "delete reason");
	}
	/**
	 * get task list by assignee and task name
	 * @param assignee  task assignee
	 * @param task		task name
	 * @return			return task list
	 */
	public List<String> getTasks(final String assignee,final String task) {
		//create task query
		List<Task> tasks = taskService.createTaskQuery().taskAssignee(assignee).taskDefinitionKey(task).list();
		List<String> ret = new ArrayList<String>();
		
		//iterate task list, append taskid to string list
		Iterator<Task> i = tasks.iterator();
		while(i.hasNext()) {
			ret.add(((Task)i.next()).getId());
		}
		
		return ret;
	}
	/**
	 * get process instance id by task id
	 * @param taskId  	task Id
	 * @return			return process instance Id
	 */
	public String getProcessId(final String taskId) throws ActivitiException {
		Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
		return task.getExecutionId();
	}
	/**
	 * complete task by task id with process variable map
	 * @param taskId task id
	 * @param vars	 process variable map
	 */
	public void completeTask(final String taskId,final Map<String,Object> vars) throws ActivitiObjectNotFoundException {
		
		Task t = taskService.createTaskQuery().taskId(taskId).singleResult();
		//if task is a delegation task, then resolve it  
		if(t.getDelegationState()==DelegationState.PENDING){
			taskService.resolveTask(taskId,vars);
		}else{
			taskService.complete(taskId,vars);
		}
	}
	/**
	 * get From data by task id
	 * @param taskId	task Id
	 * @return			return Form data map
	 */
	public Map<String,String> getFormData(final String taskId){
		//from fromService get the formData and convert to map
		Map<String, String> tf = createMap(formService.getTaskFormData(taskId).getFormProperties());
		return tf;
		
	}
	/**
	 * submit task form data by task id
	 * @param taskId	task Id
	 * @param data		form data map
	 * @throws ActivitiException
	 */
	public void setFormData(final String taskId,Map<String,String> data) throws ActivitiException{
		
		
		Task t = taskService.createTaskQuery().taskId(taskId).singleResult();
		//if task is a delegation task then resolve it
		if(t.getDelegationState()==DelegationState.PENDING){
			formService.saveFormData(taskId, data);
			taskService.resolveTask(taskId);
		}else{
			formService.submitTaskFormData(taskId, data);
		}
		
		
	}
	/**
	 * convert FromProoerty list to map<string,string>
	 * Avoid activiti objects to the business layer
	 * @param props From data
	 * @return		return Map
	 */
	private static Map<String, String> createMap(final List<FormProperty> props) {  
        Map<String, String> re = new HashMap<String, String>();
        //iterate list, convert id and value to key-value
        for (FormProperty p : props) {  
            re.put(p.getId(), p.getValue()==null?"NULL":p.getValue());  
        }  
        return re;  
    }  
	/**
	 * get user name list by dept code
	 * @param deptCode	dept code
	 * @return			return user name list
	 */
	public List<String> getDeptUsers(final String deptCode){
		Dept dept = deptRepository.findByCode(deptCode);
		List<Approver> appList = dept.getApproverList();
		
		List<String> users = new ArrayList<String>();
		for(Approver app : appList){
			users.add(app.getName());
		}
		
		return users;
	}
	/**
	 * get history task list by start name
	 * @param processName	process definition name
	 * @param starter		starter name
	 * @return				return string list
	 */
	public List<String> queryFinishedProcessInfo(final String processName,final String starter) {  
		//create query
	    HistoricProcessInstanceQuery finishedQuery = historyService.createHistoricProcessInstanceQuery()  
	            .processDefinitionKey(processName).variableValueEquals("starter", starter).finished();
	    
	    //only return last 10 records
	    Iterable<HistoricProcessInstance> list = finishedQuery.orderByProcessInstanceEndTime().desc().listPage(0, 10);  
	    
	    //convert to a string list
	    List<String> ret = new ArrayList<String>(); 
	    for(HistoricProcessInstance d : list) {
	    	//get history process variable from history task
	    	List<HistoricVariableInstance> hv = historyService.createHistoricVariableInstanceQuery().processInstanceId(d.getId()).variableName("result").list();
	    	String result="-";
	    	if(!hv.isEmpty()){
	    		result=(String)hv.get(0).getValue();
	    	}
	    	ret.add("Start Time :"+ d.getStartTime().toString()+", Result :" + result + ", Duration in millis:" + d.getDurationInMillis().toString());
	    }
	    return ret;  
	}  
	/**
	 * demo for send message to process
	 * @param user	get user object from message
	 */
	public void sendLoginMessage(final User user){
		logger.debug("send login message");
		
		Map<String, Object> processVariables = new HashMap<String,Object>();
		processVariables.put("user", user);
		
		//start message process with user object as parameter
		ProcessInstance ins = runtimeService.startProcessInstanceByMessage("login_message","donation", processVariables );
		
	}
	/**
	 * output message to log
	 * In the process listener definition event monitoring, you can monitor the operation of the process
	 * @param msg	output message
	 */
	public void log(final String msg) {
		logger.debug(msg);
	}
	/**
	 * get the process variable for the specified process
	 * @param processId		process instance Id
	 * @param variableName	process variable key
	 * @return				return process variable value. if not found then return null
	 */
	public Object getProcessVar(final String processId,final String variableName) throws ActivitiObjectNotFoundException {
		try{
			return runtimeService.getVariable(processId, variableName);
		}catch(Exception e){
			return null;
		}
	}
	/**
	 * Set the process variable for the specified process
	 * @param processId		process instance Id
	 * @param variableName	process variable key
	 * @param value			process variable value
	 */
	public void setProcessVar(final String processId, final String variableName,final Object value) throws ActivitiObjectNotFoundException {
		runtimeService.setVariable(processId, variableName, value);
	}
	
}
