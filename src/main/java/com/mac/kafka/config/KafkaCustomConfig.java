package com.mac.kafka.config;

import cn.hutool.core.map.MapUtil;
import com.mac.kafka.KafkaConsumerProperties;
import com.mac.kafka.KafkaMessageHandler;
import com.mac.kafka.annotation.KafkaAnnotationConsumer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 自定义注册topic
 *
 * @author zj
 * @Date 2024/7/5 16:40
 **/
@Slf4j
@Component
public class KafkaCustomConfig implements ApplicationContextAware, Ordered {

    // 存储topic到消息处理器的映射
    private static ConcurrentMap<String, KafkaMessageHandler<?, ?>> topicHandlers = MapUtil.newConcurrentHashMap();
    @Autowired
    private KafkaConsumerProperties kafkaConsumerProperties;

    private final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    ;


    // 注册主题处理程序
    public <K, V> void registerTopicHandler(String topic, KafkaMessageHandler<K, V> handler) {
        topicHandlers.put(topic, handler);
    }


    public void startConsumer() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConsumerProperties.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaConsumerProperties.getGroupId());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        Consumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(topicHandlers.keySet());
        log.info("自定义消费端订阅完成");

        try {
            while (!Thread.currentThread().isInterrupted()) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records) {
                    executorService.submit(() -> {
                        try {
                            handleRecord(record);
                        } catch (Exception e) {
                            log.error("Error while handling record", e);
                        }
                    });
                }
            }
        } catch (Exception e) {
            log.error("Error while consuming Kafka messages", e);
        } finally {
            // 先关闭KafkaConsumer
            consumer.close();
            // 然后开始优雅地关闭ExecutorService
            executorService.shutdown(); // 禁用新任务的提交，并启动已提交任务的关闭序列
            try {
                // 等待所有任务完成（设置超时）
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    // 如果超时，则尝试停止所有正在执行的任务
                    executorService.shutdownNow();
                    // 可能还需要等待一小段时间以确保任务确实停止了
                    if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                        log.error("ExecutorService did not terminate");
                    }
                }
            } catch (InterruptedException ie) {
                // 如果当前线程在等待过程中被中断，则也取消等待的任务
                executorService.shutdownNow();
                // 保存中断状态
                Thread.currentThread().interrupt();
            }
            // 输出优雅关闭的信息
            log.info("Kafka consumer and executor service shut down gracefully.");
        }
    }

    private void handleRecord(ConsumerRecord<String, String> record) {
        KafkaMessageHandler<String, String> handler = (KafkaMessageHandler<String, String>) topicHandlers.get(record.topic());
        if (handler != null) {
            try {
                handler.handle(record);
            } catch (Exception e) {
                log.error("Error handling record from topic " + record.topic(), e);
            }
        } else {
            log.info("No handler registered for topic: " + record.topic());
        }
    }

    private ApplicationContext applicationContext;


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        try {
            if (kafkaConsumerProperties.getCustomEnabled()) {
                integrationRigisterRun();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getOrder() {
        return Integer.MAX_VALUE;
    }

    public boolean integrationRigisterRun() throws Exception {
        Map<String, KafkaMessageHandler> strategies = applicationContext.getBeansOfType(KafkaMessageHandler.class);
        List<String> topics = new ArrayList<>();
        for (Map.Entry<String, KafkaMessageHandler> entry : strategies.entrySet()) {
            KafkaMessageHandler strategy = entry.getValue();
            if (strategy.getClass().isAnnotationPresent(KafkaAnnotationConsumer.class)) {
                // 提取注解的值，并将策略添加到Map中
                KafkaAnnotationConsumer annotation = strategy.getClass().getAnnotation(KafkaAnnotationConsumer.class);
                topicHandlers.put(strategy.getTopic(), strategy);
//                Arrays.asList(annotation.topics());Arrays.toString(annotation.topics())
                topics.add(strategy.getTopic());
            }
        }
        log.info("自定义注册的 topic: {}", topics);
        if (topicHandlers.size() != strategies.size()) {
            throw new Exception("请检查topics，topics重复验证失败");
        } else {
            startConsumer();
            log.info("自定义kafka客户端启动成功！");
        }
        return true;
    }


}
