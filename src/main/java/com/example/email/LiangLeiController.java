package com.example.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Properties;

/**
 * 日报发送
 */

@RestController
@RequestMapping("/lianglei")
public class LiangLeiController {

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

    @Value("${config.inspur.theme}")
    private String theme;

    @Value("${config.inspur.filename}")
    private String filename;

    @Value("${config.inspur.name}")
    private String name;

    @Value("${config.inspur.filePath}")
    private String filePath;


    //@Scheduled(cron = "0 0 22 * * ?") // 每天晚上22点执行
    @RequestMapping("/mail")
    public String  mail() throws IOException {

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", smtp); // 发件人邮箱的 SMTP 服务器地址
        props.put("mail.smtp.port", ip); // 发件人邮箱的 SMTP 服务器端口

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username)); // 设置发件人
            for (String push : pushs) {
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(push)); // 设置收件人(可设置多个收件人)
            }

            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String time = now.format(formatter);
            message.setSubject(theme+ time); // 设置邮件主题


            // 创建附件部分
            MimeBodyPart attachmentPart = new MimeBodyPart();
//            String filename = "src/main/resources/北京中心-梁磊-工作日报-2023年11月13日.xlsx"; // 附件文件路径
            DataSource source = new FileDataSource(filename); //附件地址
            attachmentPart.setDataHandler(new DataHandler(source));
            attachmentPart.setFileName(name);//附件名称

            // 添加图片附件
//            BodyPart imagePart = new MimeBodyPart();
//            String imagePath = "src/main/resources/123.jpg";
//            DataSource imageSource = new FileDataSource(imagePath);
//            imagePart.setDataHandler(new DataHandler(imageSource));
//            imagePart.setFileName("123.jpg");
//            multipart.addBodyPart(imagePart);


            // 创建邮件内容部分
            String fileContent = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);

            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText(fileContent);

            MimeMultipart multipart = new MimeMultipart();
            // 创建包含邮件内容和附件的多部分消息
            multipart.addBodyPart(messageBodyPart);
            multipart.addBodyPart(attachmentPart);

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
