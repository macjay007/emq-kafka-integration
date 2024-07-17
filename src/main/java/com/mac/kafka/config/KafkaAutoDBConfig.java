package com.mac.kafka.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.mac.kafka.ClassMethodArgs;
import com.mac.kafka.KafkaConsumerProperties;
import com.mac.kafka.query.KafkaQueryConfigService;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListenerAnnotationBeanPostProcessor;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.config.MethodKafkaListenerEndpoint;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.messaging.handler.annotation.support.MessageHandlerMethodFactory;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The type Kafka config.
 *
 * @author mac
 */
@Configuration
@Slf4j
@EnableConfigurationProperties(KafkaConsumerProperties.class)
@ConditionalOnBean(value = KafkaConsumerProperties.class, name = "kafkaConsumerProperties")
public class KafkaAutoDBConfig implements ApplicationContextAware {

    private final KafkaConsumerProperties kafkaConsumerProperties;

    private final KafkaListenerEndpointRegistry registry;
    private final KafkaListenerAnnotationBeanPostProcessor<String, String> postProcessor;
    @Getter
    private Map<String, DefaultKafkaConsumerFactory<String, String>> consumerFactoryMap = new ConcurrentHashMap<>();

    public KafkaAutoDBConfig(KafkaConsumerProperties kafkaConsumerProperties, KafkaListenerEndpointRegistry registry, KafkaListenerAnnotationBeanPostProcessor<String, String> postProcessor) {
        this.kafkaConsumerProperties = kafkaConsumerProperties;
        this.registry = registry;
        this.postProcessor = postProcessor;
    }


    public Map<String, Object> consumerConfigs() {
        Map<String, Object> propsMap = new HashMap<>(16);
        propsMap.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConsumerProperties.getBootstrapServers());
        propsMap.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaConsumerProperties.getGroupId());
        //是否自动提交偏移量，默认值是true，为了避免出现重复数据和数据丢失，可以把它设置为false，然后手动提交偏移量
        propsMap.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, kafkaConsumerProperties.isEnableAutoCommit());
        //自动提交的时间间隔，自动提交开启时生效
        propsMap.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, kafkaConsumerProperties.getAutoCommitInterval());
        //该属性指定了消费者在读取一个没有偏移量的分区或者偏移量无效的情况下该作何处理：
        //earliest：当各分区下有已提交的offset时，从提交的offset开始消费；无提交的offset时，从头开始消费分区的记录
        //latest：当各分区下有已提交的offset时，从提交的offset开始消费；无提交的offset时，消费新产生的该分区下的数据（在消费者启动之后生成的记录）
        //none：当各分区都存在已提交的offset时，从提交的offset开始消费；只要有一个分区不存在已提交的offset，则抛出异常
        propsMap.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, kafkaConsumerProperties.getAutoOffsetReset());
        //两次poll之间的最大间隔，默认值为5分钟。如果超过这个间隔会触发reBalance
        propsMap.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, kafkaConsumerProperties.getMaxPollIntervalTime());
        //这个参数定义了poll方法最多可以拉取多少条消息，默认值为500。如果在拉取消息的时候新消息不足500条，那有多少返回多少；如果超过500条，每次只返回500。
        //这个默认值在有些场景下太大，有些场景很难保证能够在5min内处理完500条消息，
        //如果消费者无法在5分钟内处理完500条消息的话就会触发reBalance,
        //然后这批消息会被分配到另一个消费者中，还是会处理不完，这样这批消息就永远也处理不完。
        //要避免出现上述问题，提前评估好处理一条消息最长需要多少时间，然后覆盖默认的max.poll.records参数
        //注：需要开启BatchListener批量监听才会生效，如果不开启BatchListener则不会出现reBalance情况
        propsMap.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, kafkaConsumerProperties.getMaxPollRecords());
        //当broker多久没有收到consumer的心跳请求后就触发reBalance，默认值是10s
        propsMap.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, kafkaConsumerProperties.getSessionTimeout());
        //序列化（建议使用Json，这种序列化方式可以无需额外配置传输实体类）
        propsMap.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        propsMap.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return propsMap;
    }

    public ConsumerFactory<Object, Object> consumerFactory() {
//        //配置消费者的 Json 反序列化的可信赖包，反序列化实体类需要
//        try(JsonDeserializer<Object> deserializer = new JsonDeserializer<>()) {
//            deserializer.trustedPackages("*");
//            return new DefaultKafkaConsumerFactory<>(consumerConfigs(), new JsonDeserializer<>(), deserializer);
//        }
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<Object, Object>> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        //在侦听器容器中运行的线程数，一般设置为 机器数*分区数
        factory.setConcurrency(kafkaConsumerProperties.getConcurrency());
        //消费监听接口监听的主题不存在时，默认会报错，所以设置为false忽略错误
        factory.setMissingTopicsFatal(kafkaConsumerProperties.isMissingTopicsFatal());
        //自动提交关闭，需要设置手动消息确认
//        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.getContainerProperties().setPollTimeout(kafkaConsumerProperties.getPollTimeout());
        //设置为批量监听，需要用List接收
        //factory.setBatchListener(true);
        return factory;
    }
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        init();
    }

    /**
     * 此方式主要为db应用做组件支撑的
     */
    @SneakyThrows
    public void init() {
        if (!kafkaConsumerProperties.getDbEnabled()) {
            return;
        }
        KafkaQueryConfigService kafkaQueryConfigService = applicationContext.getBean(KafkaQueryConfigService.class);
        List<Map<String, Object>> kafkaConsumerConfigs = kafkaQueryConfigService.findConfigs();
        if(kafkaConsumerConfigs.size()==0){
            log.info("AutoDB-kafka数据未做配置");
            return;
        }
        MessageHandlerMethodFactory methodFactory = postProcessor.getMessageHandlerMethodFactory();
        List<String> list = CollUtil.newArrayList();
        for (Map<String, Object> map : kafkaConsumerConfigs) {
            String kafkaBroker = map.get("kafka_broker").toString();
            if (!consumerFactoryMap.containsKey(kafkaBroker)) {
                JSONObject props = StrUtil.isEmpty(String.valueOf(map.get("kafka_config"))) ? new JSONObject() : JSON.parseObject(String.valueOf(map.get("kafka_config")));
                props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBroker);
                props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
                props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
                props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 10);
                DefaultKafkaConsumerFactory<String, String> consumerFactory = new DefaultKafkaConsumerFactory<>(props);
                consumerFactoryMap.put(kafkaBroker, consumerFactory);
            }
            DefaultKafkaConsumerFactory<String, String> consumerFactory = consumerFactoryMap.get(kafkaBroker);
            ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
            factory.setConsumerFactory(consumerFactory);
            factory.setConcurrency(Integer.valueOf(String.valueOf(map.get("concurrency"))));

            MethodKafkaListenerEndpoint<String, String> endpoint = new MethodKafkaListenerEndpoint<>();
            ClassMethodArgs classMethodArgs = ClassMethodArgs.parseMethod(String.valueOf(map.get("bean_method")));
            Class<?> clazz = (Class<?>) Class.forName(classMethodArgs.getClassName());
            // 获取构造函数
            Constructor<?> constructor = clazz.getDeclaredConstructor(long.class);
            endpoint.setBean(constructor.newInstance(Long.valueOf(String.valueOf(map.get("id")))));
            Method method = ReflectionUtils.findMethod(clazz, classMethodArgs.getMethod(), classMethodArgs.getArgsClasses());
            endpoint.setMethod(method);
            endpoint.setMessageHandlerMethodFactory(methodFactory);
            endpoint.setId("ConsumerZoningProcessing-" + map.get("id"));
            endpoint.setGroupId(String.valueOf(map.get("kafka_group_id")));
            endpoint.setTopicPartitions();
            endpoint.setTopics(String.valueOf(map.get("kafka_topic")));
            endpoint.setConcurrency(Integer.valueOf(String.valueOf(map.get("concurrency"))));
            endpoint.setBatchListener(true);
            list.addAll(endpoint.getTopics());
            registry.registerListenerContainer(endpoint, factory, false);
        }
        registry.start();
         log.info("AutoDB-kafka启动完成,已订阅kafka-topic:{}",list);

    }

}