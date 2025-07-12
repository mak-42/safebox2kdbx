package mak.safebox.kdbxconverter.converter.impl;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Safebox database converter configuration.
 */
@Configuration
@EnableConfigurationProperties({ OrphanedConverterProperties.class, NameConverterProperties.class })
public class ConverterConfig {

}
