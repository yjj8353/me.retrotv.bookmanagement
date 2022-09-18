package me.retrotv.bookmanagement.util;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import me.retrotv.bookmanagement.exception.EmailSendErrorException;

public class MailUtil {
    static Properties mailServerProperties;
	static Session getMailSession;
	static MimeMessage generateMailMessage;

    private MailUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static void sendMail(String address, String subject, String text) {
        String user = "test@gmail.com";
        String password = "";

		mailServerProperties = System.getProperties();
        mailServerProperties.put("mail.smtp.host", "smtp.gmail.com");
		mailServerProperties.put("mail.smtp.port", "465");
		mailServerProperties.put("mail.smtp.auth", "true");
        mailServerProperties.put("mail.smtp.ssl.enable", "true"); 
        mailServerProperties.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        
		Session session = Session.getDefaultInstance(mailServerProperties, new Authenticator() {

            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            
            message.setFrom(new InternetAddress("no-reply@retrotv.me", "no-reply"));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(address)); 

            // 타이틀
            message.setSubject(subject);

            // 내용
            message.setContent(text, "text/html; charset=UTF-8");

            // 이메일 전송
            Transport.send(message);
        } catch(MessagingException | UnsupportedEncodingException exception) {
            throw new EmailSendErrorException(exception.getMessage());
        }
    }
}
