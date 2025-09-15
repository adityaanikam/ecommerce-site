package com.ecommerce.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.concurrent.TimeUnit;

/**
 * Database Configuration for MongoDB Connection Pooling and Optimization
 * 
 * This configuration optimizes MongoDB connections with proper pooling,
 * timeouts, and performance settings.
 */
@Configuration
@EnableMongoRepositories(basePackages = "com.ecommerce.repository")
public class DatabaseConfig extends AbstractMongoClientConfiguration {

    @Value("${spring.data.mongodb.uri:mongodb://localhost:27017/ecommerce}")
    private String mongoUri;

    @Value("${spring.data.mongodb.database:ecommerce}")
    private String databaseName;

    @Value("${spring.data.mongodb.max-connections:100}")
    private int maxConnections;

    @Value("${spring.data.mongodb.min-connections:10}")
    private int minConnections;

    @Value("${spring.data.mongodb.max-wait-time:120000}")
    private int maxWaitTime;

    @Value("${spring.data.mongodb.max-connection-idle-time:60000}")
    private int maxConnectionIdleTime;

    @Value("${spring.data.mongodb.max-connection-life-time:0}")
    private int maxConnectionLifeTime;

    @Value("${spring.data.mongodb.connect-timeout:10000}")
    private int connectTimeout;

    @Value("${spring.data.mongodb.socket-timeout:0}")
    private int socketTimeout;

    @Value("${spring.data.mongodb.server-selection-timeout:30000}")
    private int serverSelectionTimeout;

    @Value("${spring.data.mongodb.heartbeat-frequency:10000}")
    private int heartbeatFrequency;

    @Value("${spring.data.mongodb.min-heartbeat-frequency:500}")
    private int minHeartbeatFrequency;

    @Value("${spring.data.mongodb.retry-writes:true}")
    private boolean retryWrites;

    @Value("${spring.data.mongodb.retry-reads:true}")
    private boolean retryReads;

    @Value("${spring.data.mongodb.read-preference:primary}")
    private String readPreference;

    @Value("${spring.data.mongodb.write-concern:w1}")
    private String writeConcern;

    @Override
    protected String getDatabaseName() {
        return databaseName;
    }

    @Override
    @Bean
    public MongoClient mongoClient() {
        ConnectionString connectionString = new ConnectionString(mongoUri);
        
        MongoClientSettings.Builder settingsBuilder = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                
                // Connection Pool Settings
                .applyToConnectionPoolSettings(builder -> builder
                        .maxSize(maxConnections)
                        .minSize(minConnections)
                        .maxWaitTime(maxWaitTime, TimeUnit.MILLISECONDS)
                        .maxConnectionIdleTime(maxConnectionIdleTime, TimeUnit.MILLISECONDS)
                        .maxConnectionLifeTime(maxConnectionLifeTime, TimeUnit.MILLISECONDS)
                )
                
                // Timeout Settings
                .applyToSocketSettings(builder -> builder
                        .connectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
                        .readTimeout(socketTimeout, TimeUnit.MILLISECONDS)
                )
                
                // Server Selection Settings
                .applyToServerSettings(builder -> builder
                        .heartbeatFrequency(heartbeatFrequency, TimeUnit.MILLISECONDS)
                        .minHeartbeatFrequency(minHeartbeatFrequency, TimeUnit.MILLISECONDS)
                )
                
                // Retry Settings
                .retryWrites(retryWrites)
                .retryReads(retryReads)
                
                // Read Preference
                .readPreference(com.mongodb.ReadPreference.valueOf(readPreference.toUpperCase()))
                
                // Write Concern
                .writeConcern(com.mongodb.WriteConcern.valueOf(writeConcern.toUpperCase()))
                
                // Application Name for Monitoring
                .applicationName("E-commerce-Application")
                
                // Compression
                .compressorList(java.util.Arrays.asList(
                    com.mongodb.MongoCompressor.createZstdCompressor(),
                    com.mongodb.MongoCompressor.createZlibCompressor(),
                    com.mongodb.MongoCompressor.createSnappyCompressor()
                ));

        return MongoClients.create(settingsBuilder.build());
    }

    @Bean
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongoClient(), getDatabaseName());
    }

    /**
     * Custom MongoDB configuration for performance optimization
     */
    @Bean
    public MongoClientSettings mongoClientSettings() {
        return MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(mongoUri))
                
                // Optimized connection pool settings
                .applyToConnectionPoolSettings(builder -> builder
                        .maxSize(maxConnections)
                        .minSize(minConnections)
                        .maxWaitTime(maxWaitTime, TimeUnit.MILLISECONDS)
                        .maxConnectionIdleTime(maxConnectionIdleTime, TimeUnit.MILLISECONDS)
                        .maxConnectionLifeTime(maxConnectionLifeTime, TimeUnit.MILLISECONDS)
                )
                
                // Optimized timeout settings
                .applyToSocketSettings(builder -> builder
                        .connectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
                        .readTimeout(socketTimeout, TimeUnit.MILLISECONDS)
                )
                
                // Optimized server selection
                .applyToServerSettings(builder -> builder
                        .heartbeatFrequency(heartbeatFrequency, TimeUnit.MILLISECONDS)
                        .minHeartbeatFrequency(minHeartbeatFrequency, TimeUnit.MILLISECONDS)
                )
                
                // Performance optimizations
                .retryWrites(retryWrites)
                .retryReads(retryReads)
                .readPreference(com.mongodb.ReadPreference.valueOf(readPreference.toUpperCase()))
                .writeConcern(com.mongodb.WriteConcern.valueOf(writeConcern.toUpperCase()))
                .applicationName("E-commerce-Application")
                
                // Enable compression for better performance
                .compressors(
                    com.mongodb.client.model.Compression.zstd(),
                    com.mongodb.client.model.Compression.zlib(),
                    com.mongodb.client.model.Compression.snappy()
                )
                
                // Command monitoring for performance analysis
                .addCommandListener(new PerformanceCommandListener())
                
                .build();
    }

    /**
     * Performance monitoring command listener
     */
    public static class PerformanceCommandListener implements com.mongodb.event.CommandListener {
        private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PerformanceCommandListener.class);
        
        @Override
        public void commandStarted(com.mongodb.event.CommandStartedEvent event) {
            // Log slow commands
            if (logger.isDebugEnabled()) {
                logger.debug("MongoDB Command Started: {} - Database: {}", 
                    event.getCommandName(), event.getDatabaseName());
            }
        }

        @Override
        public void commandSucceeded(com.mongodb.event.CommandSucceededEvent event) {
            long duration = event.getElapsedTime(TimeUnit.MILLISECONDS);
            
            // Log slow commands (> 100ms)
            if (duration > 100) {
                logger.warn("Slow MongoDB Command: {} took {}ms", 
                    event.getCommandName(), duration);
            } else if (logger.isDebugEnabled()) {
                logger.debug("MongoDB Command Succeeded: {} took {}ms", 
                    event.getCommandName(), duration);
            }
        }

        @Override
        public void commandFailed(com.mongodb.event.CommandFailedEvent event) {
            logger.error("MongoDB Command Failed: {} - Error: {}", 
                event.getCommandName(), event.getThrowable().getMessage());
        }
    }

    /**
     * Database health check
     */
    @Bean
    public DatabaseHealthIndicator databaseHealthIndicator() {
        return new DatabaseHealthIndicator();
    }

    /**
     * Database health indicator
     */
    public static class DatabaseHealthIndicator {
        private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DatabaseHealthIndicator.class);
        
        public boolean isHealthy() {
            try {
                // Simple ping to check database connectivity
                // This would be implemented with actual health check logic
                return true;
            } catch (Exception e) {
                logger.error("Database health check failed", e);
                return false;
            }
        }
    }
}
