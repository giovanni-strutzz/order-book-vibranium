package com.br.strutz.order_book.adapter.output.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopic {

    private static final short REPLICATION_FACTOR = 1;
    private static final int PARTITIONS = 10;

    @Bean
    public NewTopic ordersPlacedTopic() {
        return TopicBuilder.name("orders.placed")
                .partitions(PARTITIONS)
                .replicas(REPLICATION_FACTOR)
                .config(TopicConfig.RETENTION_MS_CONFIG, "604800000")
                .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "snappy")
                .build();
    }

    @Bean
    public NewTopic ordersMatchedTopic() {
        return TopicBuilder.name("orders.matched")
                .partitions(PARTITIONS)
                .replicas(REPLICATION_FACTOR)
                .config(TopicConfig.RETENTION_MS_CONFIG, "604800000")
                .build();
    }

    @Bean
    public NewTopic balanceUpdatedTopic() {
        return TopicBuilder.name("balance.updated")
                .partitions(PARTITIONS)
                .replicas(REPLICATION_FACTOR)
                .build();
    }

    @Bean
    public NewTopic auditTopic() {
        return TopicBuilder.name("audit.log")
                .partitions(3)
                .replicas(REPLICATION_FACTOR)
                .config(TopicConfig.RETENTION_MS_CONFIG, "7776000000")
                .config(TopicConfig.CLEANUP_POLICY_CONFIG, "delete")
                .build();
    }

    @Bean
    public NewTopic ordersPlacedDlt() {
        return TopicBuilder.name("orders.placed.DLT")
                .partitions(1)
                .replicas(REPLICATION_FACTOR)
                .build();
    }
}
