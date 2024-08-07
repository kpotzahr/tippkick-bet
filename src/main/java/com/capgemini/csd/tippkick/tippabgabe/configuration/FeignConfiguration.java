package com.capgemini.csd.tippkick.tippabgabe.configuration;

import com.capgemini.csd.tippkick.tippabgabe.TippabgabeApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackageClasses = TippabgabeApplication.class)
public class FeignConfiguration {
}

