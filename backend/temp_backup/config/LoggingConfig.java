package com.ecommerce.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;

@Configuration
public class LoggingConfig {
    
    @Autowired
    private Environment environment;
    
    private static final String LOG_PATTERN = "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n";
    private static final String ERROR_LOG_PATTERN = "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n%ex";
    
    @PostConstruct
    public void configureLogging() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.reset();
        
        // Console Appender
        ConsoleAppender<ILoggingEvent> consoleAppender = createConsoleAppender(context);
        
        // File Appender
        RollingFileAppender<ILoggingEvent> fileAppender = createFileAppender(context);
        
        // Error File Appender
        RollingFileAppender<ILoggingEvent> errorFileAppender = createErrorFileAppender(context);
        
        // Security Log Appender
        RollingFileAppender<ILoggingEvent> securityAppender = createSecurityAppender(context);
        
        // Audit Log Appender
        RollingFileAppender<ILoggingEvent> auditAppender = createAuditAppender(context);
        
        // Root Logger
        Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.setLevel(getLogLevel());
        rootLogger.addAppender(consoleAppender);
        rootLogger.addAppender(fileAppender);
        rootLogger.addAppender(errorFileAppender);
        
        // Application Loggers
        configureApplicationLoggers(context, securityAppender, auditAppender);
        
        // Third-party Loggers
        configureThirdPartyLoggers(context);
    }
    
    private ConsoleAppender<ILoggingEvent> createConsoleAppender(LoggerContext context) {
        ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
        consoleAppender.setContext(context);
        consoleAppender.setName("CONSOLE");
        
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern(LOG_PATTERN);
        encoder.start();
        
        consoleAppender.setEncoder(encoder);
        consoleAppender.start();
        
        return consoleAppender;
    }
    
    private RollingFileAppender<ILoggingEvent> createFileAppender(LoggerContext context) {
        RollingFileAppender<ILoggingEvent> fileAppender = new RollingFileAppender<>();
        fileAppender.setContext(context);
        fileAppender.setName("FILE");
        fileAppender.setFile("logs/application.log");
        
        TimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new TimeBasedRollingPolicy<>();
        rollingPolicy.setContext(context);
        rollingPolicy.setParent(fileAppender);
        rollingPolicy.setFileNamePattern("logs/application.%d{yyyy-MM-dd}.%i.log");
        rollingPolicy.setMaxHistory(30);
        rollingPolicy.setTotalSizeCap("1GB");
        rollingPolicy.start();
        
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern(LOG_PATTERN);
        encoder.start();
        
        fileAppender.setRollingPolicy(rollingPolicy);
        fileAppender.setEncoder(encoder);
        fileAppender.start();
        
        return fileAppender;
    }
    
    private RollingFileAppender<ILoggingEvent> createErrorFileAppender(LoggerContext context) {
        RollingFileAppender<ILoggingEvent> errorAppender = new RollingFileAppender<>();
        errorAppender.setContext(context);
        errorAppender.setName("ERROR_FILE");
        errorAppender.setFile("logs/error.log");
        
        TimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new TimeBasedRollingPolicy<>();
        rollingPolicy.setContext(context);
        rollingPolicy.setParent(errorAppender);
        rollingPolicy.setFileNamePattern("logs/error.%d{yyyy-MM-dd}.%i.log");
        rollingPolicy.setMaxHistory(90);
        rollingPolicy.setTotalSizeCap("500MB");
        rollingPolicy.start();
        
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern(ERROR_LOG_PATTERN);
        encoder.start();
        
        errorAppender.setRollingPolicy(rollingPolicy);
        errorAppender.setEncoder(encoder);
        errorAppender.start();
        
        return errorAppender;
    }
    
    private RollingFileAppender<ILoggingEvent> createSecurityAppender(LoggerContext context) {
        RollingFileAppender<ILoggingEvent> securityAppender = new RollingFileAppender<>();
        securityAppender.setContext(context);
        securityAppender.setName("SECURITY");
        securityAppender.setFile("logs/security.log");
        
        TimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new TimeBasedRollingPolicy<>();
        rollingPolicy.setContext(context);
        rollingPolicy.setParent(securityAppender);
        rollingPolicy.setFileNamePattern("logs/security.%d{yyyy-MM-dd}.%i.log");
        rollingPolicy.setMaxHistory(365); // Keep security logs for 1 year
        rollingPolicy.setTotalSizeCap("2GB");
        rollingPolicy.start();
        
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern(LOG_PATTERN);
        encoder.start();
        
        securityAppender.setRollingPolicy(rollingPolicy);
        securityAppender.setEncoder(encoder);
        securityAppender.start();
        
        return securityAppender;
    }
    
    private RollingFileAppender<ILoggingEvent> createAuditAppender(LoggerContext context) {
        RollingFileAppender<ILoggingEvent> auditAppender = new RollingFileAppender<>();
        auditAppender.setContext(context);
        auditAppender.setName("AUDIT");
        auditAppender.setFile("logs/audit.log");
        
        TimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new TimeBasedRollingPolicy<>();
        rollingPolicy.setContext(context);
        rollingPolicy.setParent(auditAppender);
        rollingPolicy.setFileNamePattern("logs/audit.%d{yyyy-MM-dd}.%i.log");
        rollingPolicy.setMaxHistory(365); // Keep audit logs for 1 year
        rollingPolicy.setTotalSizeCap("2GB");
        rollingPolicy.start();
        
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern(LOG_PATTERN);
        encoder.start();
        
        auditAppender.setRollingPolicy(rollingPolicy);
        auditAppender.setEncoder(encoder);
        auditAppender.start();
        
        return auditAppender;
    }
    
    private void configureApplicationLoggers(LoggerContext context, 
                                           RollingFileAppender<ILoggingEvent> securityAppender,
                                           RollingFileAppender<ILoggingEvent> auditAppender) {
        
        // Security Logger
        Logger securityLogger = context.getLogger("com.ecommerce.security");
        securityLogger.setLevel(Level.INFO);
        securityLogger.setAdditive(false);
        securityLogger.addAppender(securityAppender);
        
        // Audit Logger
        Logger auditLogger = context.getLogger("com.ecommerce.audit");
        auditLogger.setLevel(Level.INFO);
        auditLogger.setAdditive(false);
        auditLogger.addAppender(auditAppender);
        
        // Application Logger
        Logger appLogger = context.getLogger("com.ecommerce");
        appLogger.setLevel(getLogLevel());
        appLogger.setAdditive(false);
        
        // Error Logger
        Logger errorLogger = context.getLogger("com.ecommerce.error");
        errorLogger.setLevel(Level.ERROR);
        errorLogger.setAdditive(false);
        errorLogger.addAppender(context.getLogger(Logger.ROOT_LOGGER_NAME).getAppender("ERROR_FILE"));
        
        // Performance Logger
        Logger perfLogger = context.getLogger("com.ecommerce.performance");
        perfLogger.setLevel(Level.INFO);
        perfLogger.setAdditive(false);
        
        // Validation Logger
        Logger validationLogger = context.getLogger("com.ecommerce.validation");
        validationLogger.setLevel(Level.WARN);
        validationLogger.setAdditive(false);
    }
    
    private void configureThirdPartyLoggers(LoggerContext context) {
        // Spring Framework Loggers
        Logger springLogger = context.getLogger("org.springframework");
        springLogger.setLevel(Level.WARN);
        
        Logger springSecurityLogger = context.getLogger("org.springframework.security");
        springSecurityLogger.setLevel(Level.INFO);
        
        Logger springDataLogger = context.getLogger("org.springframework.data");
        springDataLogger.setLevel(Level.WARN);
        
        // MongoDB Loggers
        Logger mongoLogger = context.getLogger("org.mongodb");
        mongoLogger.setLevel(Level.WARN);
        
        // Redis Loggers
        Logger redisLogger = context.getLogger("org.springframework.data.redis");
        redisLogger.setLevel(Level.WARN);
        
        // HTTP Client Loggers
        Logger httpClientLogger = context.getLogger("org.apache.http");
        httpClientLogger.setLevel(Level.WARN);
        
        // Jackson Loggers
        Logger jacksonLogger = context.getLogger("com.fasterxml.jackson");
        jacksonLogger.setLevel(Level.WARN);
        
        // Hibernate Validator Loggers
        Logger validatorLogger = context.getLogger("org.hibernate.validator");
        validatorLogger.setLevel(Level.WARN);
    }
    
    private Level getLogLevel() {
        String profile = environment.getActiveProfiles().length > 0 ? 
                        environment.getActiveProfiles()[0] : "default";
        
        switch (profile.toLowerCase()) {
            case "dev":
            case "development":
                return Level.DEBUG;
            case "test":
                return Level.INFO;
            case "prod":
            case "production":
                return Level.WARN;
            default:
                return Level.INFO;
        }
    }
    
    @PreDestroy
    public void shutdown() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.stop();
    }
}
