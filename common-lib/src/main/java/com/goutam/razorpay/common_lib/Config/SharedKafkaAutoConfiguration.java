package com.goutam.razorpay.common_lib.Config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@AutoConfiguration
@EnableConfigurationProperties(KafkaProperties.class)
public class SharedKafkaAutoConfiguration {
}
