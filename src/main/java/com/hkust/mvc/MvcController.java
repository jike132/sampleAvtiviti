package com.hkust.mvc;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

import com.hkust.BPMService;
import com.hkust.EmailService;
import com.hkust.bean.ApproveBean;
import com.hkust.bean.User;
import com.hkust.dao.ApproverRepository;
import com.hkust.dao.DeptRepository;
import com.hkust.dao.DonationRepository;
import com.hkust.domain.Approver;
import com.hkust.domain.Dept;
import com.hkust.domain.Donation;

/**
 * Presentation layer controller, view template using Thymeleaf
 * In order to facilitate docking with the page, use the following naming rules:
 * 		formXXX 	Returns the data and view of the presentation page, the corresponding 
 * 					page template is XXX.html
 * 		processXXX 	According to the page form submitted data, processing and return
 * 
 * 	Use the session to save the user and the current task id
 */

@Controller
@SessionAttributes({ "user", "taskid" })
@RequestMapping("/mvc")
public class MvcController {

	public static String TASK_DONATION = "Donation";
	public static String TASK_APPROVE = "Approve";
	public static String TASK_SECOND = "Second";

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private BPMService bpmService;

	@Autowired
	private DonationRepository donationDao;

	@Autowired
	private ApproverRepository apprRepository;

	@Autowired
	private DeptRepository deptRepository;
	
	@Autowired
	private EmailService emailWiser;

	/**
	 * The main page displays information about the user's 
	 * task list, task history, and incoming mail
	 * @param modelMap	model map object
	 * @return			which view will go to
	 */
	@RequestMapping(value = "list")
	public String list(ModelMap modelMap) {
		//from the session get user object
		User user = (User)modelMap.get("user");
		//if user is null then redirect to login view
		if(user == null || user.getName() == null) {
			return "redirect:admin/login";
		}
		//get special user's waiting list of donation, approve, second approve step 
		List<String> tasklist = bpmService.getTasks(user.getName(), TASK_DONATION);
		List<String> applist = bpmService.getTasks(user.getName(), TASK_APPROVE);
		List<String> secondlist = bpmService.getTasks(user.getName(), TASK_SECOND);
		//get task history list
		List<String> historyDonation = bpmService.queryFinishedProcessInfo("donation",user.getName());
		//get email list
		List<String> emails = emailWiser.getEmails(user.getName());
		
		//put into model, return to view
		modelMap.addAttribute("donations", tasklist);
		modelMap.addAttribute("approves", applist);
		modelMap.addAttribute("seconds", secondlist);
		modelMap.addAttribute("allHistory", historyDonation);
		modelMap.addAttribute("emails",emails);
		
		return ("list");
	}

	/**
	 * begin process when click start button
	 * @param user		from session get user object
	 * @param process	process definition name
	 * @param modelMap	model map object
	 * @return
	 */
	@RequestMapping(value = "start/{process}")
	public String startProcess(@ModelAttribute("user") User user, @PathVariable("process") String process,
			ModelMap modelMap) {

		try {

			bpmService.startProcess(process, user.getName());

			return "redirect:/mvc/list";
		} catch (Exception e) {
			//When the failure to throw an exception to the page
			throw new RuntimeException("Can not start process");
		}

	}
	/**
	 * Stop the process when you click stop
	 * @param process	process instance id
	 * @param modelMap	
	 * @return
	 */
	@RequestMapping(value = "stop/{process}")
	public String stopProcess(@PathVariable("process") String process,
			ModelMap modelMap) {
		bpmService.deleteProcess(process);
		
		return "redirect:/mvc/list"; 
	}
	/**
	 * return donation.html needed data
	 * @param user		from session get user object
	 * @param taskid	task Id
	 * @param modelMap	
	 * @return
	 */
	@RequestMapping(value = "donation/{taskid}", method = RequestMethod.GET)
	public String formDonation(@ModelAttribute("user") User user, @PathVariable("taskid") String taskid, ModelMap modelMap) {
		//Find the process id according to task
		String processId = bpmService.getProcessId(taskid);
		
		Donation donation;
		try{
			//Get the process variable - donation from the corresponding process 
			donation  = (Donation)bpmService.getProcessVar(processId,"donation");
		}catch(Exception e){
			donation=null;
		}
		//if donation is null then create new object
		if (donation == null) {
			donation = new Donation();

			donation.setId(processId);
			donation.setName(user.getName());
			donation.setStatus(Donation.OPEN);
			donation.setAmt(new BigDecimal(0));
			donation.setDate(new Date());
			donationDao.save(donation);
			
			bpmService.setProcessVar(processId,"donation",donation);
		}
		
		//put into model and return to page
		modelMap.addAttribute("taskid", taskid);
		modelMap.addAttribute("donation", donation);
		
		//returned view
		return "donation";
	}
	/**
	 * be called when donation page is submitting
	 * @param donation		page returned donation object
	 * @param bindingResult	page returned binding result
	 * @param modelMap		
	 * @param redirect		
	 * @return
	 */
	@RequestMapping(value = "/processDonation", method = RequestMethod.POST)
	public String processDonation(@Valid final Donation donation, final BindingResult bindingResult,
			final ModelMap modelMap,RedirectAttributes redirect) {
		//if input data has binding error then return 
		if (bindingResult.hasErrors()) {
			return "donation";
		}
		//get taskid from session
		String taskId = (String) modelMap.get("taskid");

		logger.info("process donation = {} , taskid = {}", donation, taskId);
		
		//donationDao.save(donation);

		Map<String, Object> vars = new HashMap<String, Object>();
		
		//put approver into process variable
		vars.put("approver", donation.getApprover());
		
		//complete task with variable
		bpmService.completeTask(taskId, vars);

		//redirect.addFlashAttribute("globalMessage", "Successfully created a new message");
		return "redirect:list";

	}
	/**
	 * return approve.html used data
	 * @param id		approve id
	 * @param user		get user from session
	 * @param modelMap	
	 * @return
	 */
	@RequestMapping(value = "approve/{id}", method = RequestMethod.GET)
	public String formApprove(@PathVariable("id") String id, @ModelAttribute("user") User user, ModelMap modelMap) {

		String processId = bpmService.getProcessId(id);

		Donation donation;
		donation = (Donation)bpmService.getProcessVar(processId, "donation");

		if (donation == null) {
			//if donation is null then throw exception to page
			throw new RuntimeException("Can not find donation");
		}
		
		//put these object into view model
		modelMap.addAttribute("donation", donation);
		modelMap.addAttribute("approveBean", new ApproveBean());
		modelMap.addAttribute("taskid", id);

		return "approve";
	}
	/**
	 * be called when approve page is submitting
	 * @param approveBean		page returned approveBean object
	 * @param bindingResult		page returned binding result
	 * @param modelMap
	 * @return
	 */
	@RequestMapping(value = "/processApprove", method = RequestMethod.POST)
	public String processApprove(@Valid final ApproveBean approveBean, final BindingResult bindingResult,
			final ModelMap modelMap) {
		//get taskid from session
		String taskId = (String) modelMap.get("taskid");
		
		logger.info("process approve : {} , {}", approveBean.getDept(), taskId);

		String processId = bpmService.getProcessId(taskId);

		Donation donation = (Donation)bpmService.getProcessVar(processId, "donation");	
		
		//return if binding has errors
		if (bindingResult.hasErrors()) {
			modelMap.addAttribute("donation", donation);
			return "approve";
		}

		/**
		 * to complete userTask there are two implements:
		 * 1. task complete  good point: direct complete with process variable
		 * 2. form submit    good point: focus on form data when input/output
		 */
//		//task complete implement
//		Map<String, Object> vars = new HashMap<String, Object>();
//		vars.put("dept", approveBean.getDept());
//		vars.put("result", approveBean.getResult());
//		bpmService.completeTask(taskId, vars);
		
		
		//form submit implements
		Map<String,String> data = bpmService.getFormData(taskId);
		
		logger.info("form vars = {}", data);
		
		data.put("result", approveBean.getResult());
		data.put("dept", approveBean.getDept());

		try {
				bpmService.setFormData(taskId, data);
			} catch (Exception e) {
				//return error to page
				bindingResult.addError(new ObjectError("err",e.getMessage()));
				modelMap.addAttribute("donation", donation);
				return "approve";
			}

		return "redirect:list";
	}
	/**
	 * return data to second.html
	 * @param user		get user from session
	 * @param id		current task Id
	 * @param modelMap	
	 * @return
	 */
	@RequestMapping(value = "second/{id}", method = RequestMethod.GET)
	public String formSecond(@ModelAttribute("user") User user, @PathVariable("id") String id, ModelMap modelMap) {

		String processId = bpmService.getProcessId(id);

		Donation donation;
		donation = (Donation)bpmService.getProcessVar(processId, "donation");
		if (donation == null) {
			throw new RuntimeException("Can not find donation");
		}

		modelMap.addAttribute("donation", donation);
		modelMap.addAttribute("approveBean", new ApproveBean());
		modelMap.addAttribute("taskid", id);

		return "second";
	}
	/**
	 * be called when second page is submitting
	 * @param taskId		from session get taskId
	 * @param approveBean	page returned approveBean object
	 * @param bindingResult	page returned binding result
	 * @param modelMap
	 * @return
	 */
	@RequestMapping(value = "/processSecond", method = RequestMethod.POST)
	public String processSecond(@ModelAttribute("taskid")String taskId,@Valid final ApproveBean approveBean, final BindingResult bindingResult,
			final ModelMap modelMap) {
		
		logger.info("process second : result = {} , taskid = {}", approveBean.getResult(), taskId);

		String processId = bpmService.getProcessId(taskId);

		Donation donation = (Donation)bpmService.getProcessVar(processId, "donation");	

		if (bindingResult.hasErrors()) {
			//if input has errors
			modelMap.addAttribute("donation", donation);
			return "second";
		}
	
		Map<String, Object> vars = new HashMap<String, Object>();
		
		//Set the result of the vote, each task set in a different var, 
		//used to calculate the voting results
		vars.put("signResult_" + taskId, approveBean.getResult());

		bpmService.completeTask(taskId, vars);

		return "redirect:list";

	}
	/**
	 * Page shared approve model
	 * @return
	 */
	@ModelAttribute("allApproveres")
	public List<Approver> populateApprovers() {
		return apprRepository.findAll();
	}
	/**
	 * Page shared dept model
	 * @return
	 */
	@ModelAttribute("allDepts")
	public List<Dept> populateallDepts() {
		return deptRepository.findAll();
	}
	/**
	 * Defines the Date binding format
	 * @param binder
	 */
	@InitBinder
	public void initBinder(WebDataBinder binder) {
		CustomDateEditor editor = new CustomDateEditor(new SimpleDateFormat("yyyy-MM-dd"), true);
		binder.registerCustomEditor(Date.class, editor);
	}
}
