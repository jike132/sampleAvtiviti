package com.hkust;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;

import org.springframework.stereotype.Service;
import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;

/**
 * Mock as Email Server
 * @author Vincent
 *
 */
@Service
public class EmailService {
	
	private Wiser wiser;

	public Wiser getWiser() {
		return wiser;
	}

	public void setWiser(Wiser wiser) {
		this.wiser = wiser;
	}

	public EmailService() {
		super();
		
		wiser = new Wiser();
        wiser.setPort(1025);
	}
	
	public void start() {
		wiser.start();
	}
	
	public List<String> getEmails(String user) {
		List<WiserMessage> messages = wiser.getMessages();
		
		List<String> ret = new ArrayList<String>();
		
		for(WiserMessage m : messages){
			try {
				ret.add("Sender: " + m.getEnvelopeSender() + "  Message:" + m.getMimeMessage().getContent().toString());
			} catch (MessagingException | IOException e) {
				e.printStackTrace();
			}
		}
		
		return ret;
	}
}
