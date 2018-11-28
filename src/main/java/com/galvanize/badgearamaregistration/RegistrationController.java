package com.galvanize.badgearamaregistration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/visitor")
public class RegistrationController {

    @Autowired
    RegistrationService registrationService;

    @PostMapping("/register")
    public Boolean register(@RequestBody ExtendedPersonFrontEnd person) {
        return registrationService.register(person);
    }

    @GetMapping("/{phoneNumber}")
    public ExtendedPersonFrontEnd getByPhone(@PathVariable Long phoneNumber) {
        return registrationService.getPersonByPhoneNumber(phoneNumber);
    }

}
