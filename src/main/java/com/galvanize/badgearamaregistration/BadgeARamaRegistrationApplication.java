package com.galvanize.badgearamaregistration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.net.URI;
import java.net.URISyntaxException;

import static java.lang.System.getenv;

@SpringBootApplication
public class BadgeARamaRegistrationApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(BadgeARamaRegistrationApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(BadgeARamaRegistrationApplication.class, args);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(final ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(producerJackson2MessageConverter());
        return rabbitTemplate;
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        final URI rabbitMqUrl;
        try {
            rabbitMqUrl = new URI(getEnvOrThrow("CLOUDAMQP_URL"));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        final CachingConnectionFactory factory = new CachingConnectionFactory();
        factory.setUsername(rabbitMqUrl.getUserInfo().split(":")[0]);
        factory.setPassword(rabbitMqUrl.getUserInfo().split(":")[1]);
        factory.setHost(rabbitMqUrl.getHost());
        factory.setPort(rabbitMqUrl.getPort());
        factory.setVirtualHost(rabbitMqUrl.getPath().substring(1));

        return factory;
    }


    @Bean
    public Jackson2JsonMessageConverter producerJackson2MessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    private static String getEnvOrThrow(String name) {
        final String env = getenv(name);
        LOGGER.info("try to find env: {}", name);
        if (env == null) {
            throw new IllegalStateException("Environment variable [" + name + "] is not set.");
        }
        return env;
    }


}
