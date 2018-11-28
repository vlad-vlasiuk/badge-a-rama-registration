package com.galvanize.badgearamaregistration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class RegistrationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegistrationService.class);

    @Autowired
    PersonRepository personRepository;
    @Autowired
    RabbitTemplate rabbitTemplate;

    @Value("${amqp.exchange.name}")
    private String appExchangeName;

    @Value("${amqp.routing.key}")
    private String appRoutingKey;

    @Transactional
    public Boolean register(ExtendedPersonFrontEnd personFrontEnd) {
        ExtendedPerson extendedPerson = transformFrontEndPerson(personFrontEnd);
        Person person = Person.builder()
                .phoneNumber(extendedPerson.getPhoneNumber())
                .firstName(extendedPerson.getFirstName())
                .lastName(extendedPerson.getLastName())
                .company(extendedPerson.getCompany())
                .build();
        personRepository.save(person);

        //sendMessage(appExchangeName, appRoutingKey, extendedPerson);

        return true;
    }

    public ExtendedPerson transformFrontEndPerson(ExtendedPersonFrontEnd personFE) {
        return ExtendedPerson.builder()
                .phoneNumber(Long.parseLong(personFE.getPhoneNumber().replaceAll("[^0-9]", "")))
                .firstName(personFE.getFirstName())
                .lastName(personFE.getLastName())
                .company(personFE.getCompany())
                .hostName(personFE.getHostName())
                .hostPhoneNumber(Long.parseLong(personFE.getHostPhone().replaceAll("[^0-9]", "")))
                .purposeOfVisit(personFE.getPurposeOfVisit())
                .reasonForDeletion(personFE.getReasonForDeletion())
                .badgeNumber(personFE.getBadgeNumber())
                .active(personFE.getActive())
                .status(personFE.getStatus())
                .build();
    }

    public void sendMessage(String exchange, String routingKey, Object data) {
        LOGGER.info("Sending message to the queue using routingKey {}. Message= {}", routingKey, data);
        rabbitTemplate.convertAndSend(exchange, routingKey, data);
        LOGGER.info("The message has been sent to the queue.");
    }

    public ExtendedPersonFrontEnd getPersonByPhoneNumber(Long phoneNumber) {
        Optional<Person> optPerson = personRepository.findById(phoneNumber);
        if (!optPerson.isPresent())
            throw new IllegalArgumentException();
        Person person = optPerson.get();

        return ExtendedPersonFrontEnd.builder()
                .phoneNumber(Long.toString(person.getPhoneNumber()))
                .firstName(person.getFirstName())
                .lastName(person.getLastName())
                .company(person.getCompany())
                .build();

    }
}
