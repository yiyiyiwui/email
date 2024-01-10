package com.example.email;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.List;
import java.util.Properties;

/**
 * 服务器宕机重启邮件发送
 */

@RestController
@RequestMapping("/bjdx")
public class CupController {

    @Value("${config.inspur.username}")
    private String username;

    @Value("${config.inspur.password}")
    private String password;

    @Value("${config.inspur.smtp}")
    private String smtp;

    @Value("${config.inspur.ip}")
    private String ip;

    @Value("${config.inspur.pushs}")
    private List<String> pushs;

    @Value("${config.bjdx.theme}")
    private String theme;

    //@Scheduled(cron = "0 0 22 * * ?") // 每天晚上22点执行
    @RequestMapping("/cup")
    public String cup(@RequestBody String cup) {
        System.out.println("接受到消息"+cup);

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", smtp);//发件人邮箱的 SMTP 服务器地址
        props.put("mail.smtp.port", ip); // 发件人邮箱的 SMTP 服务器端口

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username)); // 设置发件人
            for (String s : pushs) {
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(s)); // 设置收件人(可设置多个收件人)
            }
            message.setSubject(theme); // 设置邮件主题

            // 创建邮件内容部分
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText("cup当前使用率高！当前阈值-->"+cup);

            // 创建包含邮件内容和附件的多部分消息
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);

            // 将多部分消息设置为整个消息内容
            message.setContent(multipart);

            // 发送邮件
            Transport.send(message);

            System.out.println("Email 发送成功");
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
        return "Email 发送成功";
    }

}
