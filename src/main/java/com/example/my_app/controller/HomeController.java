package com.example.my_app.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.my_app.model.InquiryDTO;
import com.example.my_app.util.WebUtils;

@Controller
public class HomeController {
    private final Map<String, LocalDateTime> userSubmissionMap = new HashMap<>();

    @Autowired
    private JavaMailSender javaMailSender;

    @GetMapping("/")
    public String index(Model m) {
        m.addAttribute("obj", new InquiryDTO());
        return "home/index";
    }

    @PostMapping("/submit")
    public String submit(Model m, @ModelAttribute("obj") InquiryDTO inquiryDTO,
            final RedirectAttributes redirectAttributes) {
        // agar database system rahe ga to database me inquiryDTO ka data store kar le
        // ge magar abhi to list me kar rahe he

        if (hasRecentSubmission(inquiryDTO.getEmail())) {
            redirectAttributes.addFlashAttribute(WebUtils.MSG_SUCCESS,
                    WebUtils.getMessage(
                            "You have already submitted a message. Please wait 5 min before submitting again."));
            return "redirect:";
        }

        LocalDateTime currentTime = LocalDateTime.now();
        inquiryDTO.setCreatedAt(currentTime);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = currentTime.format(formatter);

        sendAuctionEmail(inquiryDTO, formattedDateTime);
        userSubmissionMap.put(inquiryDTO.getEmail(), currentTime);
        String successMessage = String.format("Success message sent by %s %s",
                inquiryDTO.getFirstName(), inquiryDTO.getLastName());

        redirectAttributes.addFlashAttribute(WebUtils.MSG_SUCCESS, WebUtils.getMessage(successMessage));
        redirectAttributes.addFlashAttribute("FORMATTED_DATE_TIME", formattedDateTime);
        return "redirect:";
    }

    private void sendAuctionEmail(InquiryDTO auctionInquiryDTO, String formattedDateTime) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo("luckymourya634@gmail.com"); // Replace with your email address
        mailMessage.setSubject("New Inquiry for Upcoming Auction at: " + formattedDateTime);
        mailMessage.setText(
                "Hello,\n\n" +
                        "We have received a new inquiry related to the upcoming auction scheduled for: "
                        + formattedDateTime + "\n\n" +
                        "Inquirer's Details:\n" +
                        "Name: " + auctionInquiryDTO.getFirstName() + " " + auctionInquiryDTO.getLastName() + "\n" +
                        "Email: " + auctionInquiryDTO.getEmail() + "\n" +
                        "Phone Number: " + auctionInquiryDTO.getPhoneNumber() + "\n" +
                        "Subject: " + auctionInquiryDTO.getSubject() + "\n" +
                        "Message: " + auctionInquiryDTO.getMessage() + "\n" +
                        "From Company: " + auctionInquiryDTO.getCompany() + "\n\n" +
                        "Please respond promptly to address their inquiries and provide any necessary information.\n\n"
                        +
                        "Thank you,\n" +
                        "Auction Management Team");
        javaMailSender.send(mailMessage);
    }

    private boolean hasRecentSubmission(String userEmail) {
        LocalDateTime lastSubmissionTime = userSubmissionMap.get(userEmail);
        return lastSubmissionTime != null && ChronoUnit.MINUTES.between(lastSubmissionTime, LocalDateTime.now()) < 5;
        // Allow one submission every 5
        // minutes
    }

    @GetMapping("/videoCall")
    public String indexVideoCall(Model m) {
        return "home/videoCall";
    }

}
