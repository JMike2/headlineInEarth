package com.headline.kafka.sample;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

public class ProducerQuickstart {
    public static void main(String[] args) {
        //1.kafka连接配置信息
        Properties pro = new Properties();
        //kafka连接地址
        pro.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,"47.236.111.222:9092");

        //key和value的序列化
        pro.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringSerializer");
        pro.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringSerializer");
        //2.创建kafka生产者对象
        KafkaProducer<String,String> producer = new KafkaProducer<String,String>(pro);
        //3.发送消息
        /**
         * 第一个参数：topic
         * 第二个参数：消息得key
         * 第三个参数：消息的value
         */
        ProducerRecord<String,String> kvProducerRecord = new ProducerRecord<String,String>("topic-first","key-001","hello kafka");
        producer.send(kvProducerRecord);
        //4.关闭消息通道
        producer.close();
    }
}
