package com.CommonUtils.ConfigTemplate.Config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.transaction.KafkaTransactionManager;

@Configuration
@EnableKafka
public class KafkaConfig 
{
	/**
	 * 尽量在第一次创建一个topic时就指定这两个参数，因为
如果Partition 数目在之后再次做调整，则会打乱key的顺序保证（同样的key会分布到不同的partition上）
如果Replication Factor在之后再次增加，则会给集群带来更大的压力，可能会导致性能下降





1. Partition 数目
一般来说，每个partition 能处理的吞吐为几MB/s（仍需要基于根据本地环境测试后获取准确指标），增加更多的partitions意味着：
更高的并行度与吞吐
可以扩展更多的（同一个consumer group中的）consumers
若是集群中有较多的brokers，则可更大程度上利用闲置的brokers
但是会造成Zookeeper的更多选举
也会在Kafka中打开更多的文件
 
调整准则：
一般来说，若是集群较小（小于6个brokers），则配置2 x broker数的partition数。在这里主要考虑的是之后的扩展。若是集群扩展了一倍（例如12个），则不用担心会有partition不足的现象发生
一般来说，若是集群较大（大于12个），则配置1 x broker 数的partition数。因为这里不需要再考虑集群的扩展情况，与broker数相同的partition数已经足够应付常规场景。若有必要，则再手动调整
考虑最高峰吞吐需要的并行consumer数，调整partition的数目。若是应用场景需要有20个（同一个consumer group中的）consumer并行消费，则据此设置为20个partition
考虑producer所需的吞吐，调整partition数目（如果producer的吞吐非常高，或是在接下来两年内都比较高，则增加partition的数目）
以上仅是几个基本准则，最重要的是：在本地集群做测试，以获取一个更合适的partition数目，不同的集群会有不同的性能。






2. Replication factor
此参数决定的是records复制的数目，建议至少 设置为2，一般是3，最高设置为4。更高的replication factor（假设数目为N）意味着：
系统更稳定（允许N-1个broker宕机）
更多的副本（如果acks=all，则会造成较高的延时）
系统磁盘的使用率会更高（一般若是RF为3，则相对于RF为2时，会占据更多50% 的磁盘空间）
 
调整准则：
以3为起始（当然至少需要有3个brokers，同时也不建议一个Kafka 集群中节点数少于3个节点）
如果replication 性能成为了瓶颈或是一个issue，则建议使用一个性能更好的broker，而不是降低RF的数目
永远不要在生产环境中设置RF为1





3. 集群调整建议
一个已被业界接受的准则是：
一个broker不应该承载超过2000 到 4000 个partitions（考虑此broker上所有来自不同topics的partitions）。同时，一个Kafka集群上brokers中所有的partitions总数最多不应超过20,000个。
此准则基于的原理是：在有broker宕机后，zookeeper需要重新做选举。若是partitions数目过多，则需要执行大量的leader elections。
另外几个常规原则有：
如果集群中需要更多的partitions，则优先考虑增加brokers
如果集群中需要20,000 个以上的partitions，则可以参考Netflix的模型，创建更多的Kafka 集群
最后需要注意的是：不要为一个topic创建超过1000个的partitions。我们也并不需要1000个partitions才能达到很高的吞吐。在开始的时候，选择一个更合理的partition数目，然后测试性能，根据测试结果再调整partitions 数目。
	 * */
	@Bean
	public NewTopic myTopic()
	{ return new NewTopic("myTopic", 12, (short)1); }
	
	@Bean
	public DefaultKafkaProducerFactory<String, byte[]> defaultKafkaProducerFactory()
	{ return com.CommonUtils.Config.Kafka.Config.BaseConfig.getDefaultKafkaProducerFactory("127.0.0.1:9092", new ByteArraySerializer(), "myTransactionId-"); }
	
	@Bean
	public KafkaTransactionManager<String, byte[]> kafkaTransactionManager(@Qualifier("defaultKafkaProducerFactory")DefaultKafkaProducerFactory<String, byte[]> defaultKafkaProducerFactory)
	{ return new KafkaTransactionManager<String, byte[]>(defaultKafkaProducerFactory); }
	
	/**
	 * 记录一下KafkaTemplate函数使用场景
	 * 1、send(String topic, @Nullable V data) 发送后，数据会由kafka自己放到分区，注意，不是同一个分区，而且随机分区，由于是跨分区存放，不能保证顺序性
	 * 2、send(String topic, K key, @Nullable V data) 发送后，数据会由kafka自己放到分区，由于指定了key，kafka会随机指定一个分区，专门存放这个key的数据，同一个分区可以保证顺序性
	 * 3、send(String topic, Integer partition, K key, @Nullable V data)，这个自己指定分区、key来存放数据，同一个分区可以保证顺序性
	 * */
	@Bean
	public KafkaTemplate<String, byte[]> kafkaTemplate(@Qualifier("defaultKafkaProducerFactory")DefaultKafkaProducerFactory<String, byte[]> defaultKafkaProducerFactory)
	{ return new KafkaTemplate<String, byte[]>(defaultKafkaProducerFactory); }
	
	@Bean
	public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, byte[]>> kafkaListenerContainerFactory()
	{ return com.CommonUtils.Config.Kafka.Config.BaseConfig.getKafkaListenerContainerFactory("127.0.0.1:9092", 12, "myTopicGroup1", false, new ByteArrayDeserializer()); }
}