package com.zerobase.cms.application;

import com.zerobase.cms.client.MailgunClient;
import com.zerobase.cms.client.mailgun.SendMailForm;
import com.zerobase.cms.domain.SignUpForm;
import com.zerobase.cms.domain.model.Customer;
import com.zerobase.cms.excepition.CustomException;
import com.zerobase.cms.service.SignUpCustomerService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.zerobase.cms.excepition.ErrorCode.ALREADY_REGISTER_USER;

@Service
@RequiredArgsConstructor
public class SignUpApplication {
    private final MailgunClient mailgunClient;
    private final SignUpCustomerService signUpCustomerService;

    public void customerVerify(String email, String code) {
        signUpCustomerService.verifyEmail(email, code);
    }

    public String customerSignUp(SignUpForm form) {
        if (signUpCustomerService.isEmailExist(form.getEmail())) {
            throw new CustomException(ALREADY_REGISTER_USER);
        } else {
            Customer c = signUpCustomerService.signUp(form);
            LocalDateTime now = LocalDateTime.now();
            String code = getRandomCode();
            SendMailForm sendMailForm = SendMailForm.builder()
                    .from("kys1064@naver.com")
                    .to(form.getEmail())
                    .subject("이메일 인증")
                    .text(getVerificationEmailBody(c.getEmail(), c.getName(), code))
                    .build();

            mailgunClient.sendEmail(sendMailForm);
            signUpCustomerService.changeCustomerValidateEmail(c.getId(), code);
            return "회원가입 성공";
        }
    }
    private String getRandomCode() {return RandomStringUtils.random(10, true, true);}

    private String getVerificationEmailBody(String emil, String name, String code) {
        StringBuilder builder = new StringBuilder();
        return builder.append(name).append("인증을 위해 링크를 클릭해주세요.\n\n")
                .append("http://localhost:3306/signup/verify/customer?email=")
                .append(emil)
                .append("&code")
                .append(code).toString();
    }
}
