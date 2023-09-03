package com.smart.service;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.stereotype.Service;

@Service
public class EmailService {
	
	public boolean sendEmail(String subject,String message,String to) {
		//Rest of the code
		boolean f=false;
		String from = "shivjimca2024@gmail.com";
		
		// Gmail Host
				String host = "smtp.gmail.com";

				// Get the system properties
				Properties properties = System.getProperties();
				System.out.println("Properties :" + properties);

				// Setting important information to propeties object

				// Host set
				properties.put("mail.smtp.host", host);
				properties.put("mail.smtp.port", "465");
				properties.put("mail.smtp.ssl.enable", "true");
				properties.put("mail.smtp.auth", "true");

				// Step 1 to get session object..
				Session session = Session.getInstance(properties, new Authenticator() {

					@Override
					protected PasswordAuthentication getPasswordAuthentication() {

						return new PasswordAuthentication("shivjimca2024@gmail.com", "sszuhznmrezehpxe");

					}

				});

				session.setDebug(true);
				// Step 2 compose the message [text, multi media]
				MimeMessage m = new MimeMessage(session);

				try {
					// From send to message
					m.setFrom(from);

					// Adding recipent to message

					m.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

					// Adding Subject to message
					m.setSubject(subject);

					// Adding Message Text to Message
					//m.setText(message);
					m.setContent(message,"text/html");

					// Send Message Step 3 using transport class
					Transport.send(m);

				//	System.out.println("Send Message Successfully......");

					f=true;
				} catch (MessagingException e) {

					e.printStackTrace();
				}
				return f;
			}
				

}
