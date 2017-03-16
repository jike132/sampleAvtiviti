package com.hkust.mvc;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

import com.hkust.BPMService;
import com.hkust.bean.User;
import com.hkust.dao.ApproverRepository;
import com.hkust.domain.Approver;

@Controller
@SessionAttributes({ "user", "taskid" })
@RequestMapping("/")
public class AdminController {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private ApproverRepository apprRepository;
	
	@Autowired
	private BPMService bpmService;
	
	@RequestMapping(value = "admin/login", method = RequestMethod.GET)
	public String formLogin(ModelMap modelMap) {
		modelMap.addAttribute("user", new User());
		return ("login");
	}

	@RequestMapping(value = "admin/processLogin", method = RequestMethod.POST)
	public String processLogin(final User user, final BindingResult bindingResult, final ModelMap modelMap) {

		if (bindingResult.hasErrors()) {
			return "login";
		}

		logger.info("{}", user.getName());
		
		bpmService.sendLoginMessage(user);

		modelMap.addAttribute("user", user);

		return "redirect:/mvc/list";

	}

	@RequestMapping("admin/logout")
	public String processLogout(SessionStatus sessionStatus) {

		//The setComplete method in sessionStatus can empty the contents of the session
		sessionStatus.setComplete();

		return "redirect:login";
	}
	
	@ModelAttribute("allApproveres")
	public List<Approver> populateApprovers() {
		return apprRepository.findAll();
	}

}