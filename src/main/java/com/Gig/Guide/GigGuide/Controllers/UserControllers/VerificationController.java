package com.Gig.Guide.GigGuide.Controllers.UserControllers;

import com.Gig.Guide.GigGuide.Models.MyAppUser;
import com.Gig.Guide.GigGuide.Repositories.MyAppUserRepository;
import com.Gig.Guide.GigGuide.utils.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;



@RestController
public class VerificationController {
    
    @Autowired
    private MyAppUserRepository myAppUserRepository;
    
    @Autowired
    private JwtTokenUtil jwtUtil;
     
    @GetMapping("/req/signup/verify")
    public ResponseEntity verifyEmail(@RequestParam("token") String token) {
        String emailString = jwtUtil.extractEmail(token);
        MyAppUser user = myAppUserRepository.findByEmail(emailString);
        if (user == null || user.getVerficationToken() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Token Expired!");
        }
        
        if (!jwtUtil.validateToken(token) || !user.getVerficationToken().equals(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Token Expired!");
        }
        user.setVerficationToken(null);
        user.setVerified(true);  
        myAppUserRepository.save(user);
        
        return ResponseEntity.status(HttpStatus.CREATED).body("Email successfully verified!");
    }
    
    
}

