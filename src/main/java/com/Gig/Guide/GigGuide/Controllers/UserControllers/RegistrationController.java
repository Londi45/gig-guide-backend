package com.Gig.Guide.GigGuide.Controllers.UserControllers;

import com.Gig.Guide.GigGuide.Models.MyAppUser;
import com.Gig.Guide.GigGuide.Repositories.MyAppUserRepository;
import com.Gig.Guide.GigGuide.Service.EmailService;
import com.Gig.Guide.GigGuide.utils.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class RegistrationController {
    
    @Autowired
    private MyAppUserRepository myAppUserRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private EmailService emailService;
    
    
    @PostMapping(value = "/req/signup", consumes = "application/json")
    public ResponseEntity<String> createUser(@RequestBody MyAppUser user){
        
        MyAppUser existingAppUser = myAppUserRepository.findByEmail(user.getEmail());
        
        if(existingAppUser != null){
            if(existingAppUser.isVerified()){
                return new ResponseEntity<>("User Already exist and Verified.",HttpStatus.BAD_REQUEST);
            }else{
                String verificationToken = JwtTokenUtil.generateToken(existingAppUser.getEmail());
                existingAppUser.setVerficationToken(verificationToken);
                myAppUserRepository.save(existingAppUser);
                //Send Email Code
                emailService.sendVerificationEmail(existingAppUser.getEmail(), verificationToken);
                return new ResponseEntity<>("Verification Email resent. Check your inbox",HttpStatus.OK);
            }
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        String vericationToken =JwtTokenUtil.generateToken(user.getEmail());
        user.setVerficationToken(vericationToken);
        myAppUserRepository.save(user);
        //Send Email Code
        emailService.sendVerificationEmail(user.getEmail(), vericationToken);
        
        return new ResponseEntity<>("Registration successfull! Please Verify your Email", HttpStatus.OK);
    }
    
}
