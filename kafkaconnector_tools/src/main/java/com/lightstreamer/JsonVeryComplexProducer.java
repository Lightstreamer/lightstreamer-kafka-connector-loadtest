package com.lightstreamer;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JsonVeryComplexProducer extends BaseProducer {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    String[] stringids = { "James", "John", "Robert", "Michael", "William",
            "David", "Richard", "Joseph", "Charles", "Thomas",
            "Daniel", "Matthew", "Christopher", "George", "Brian",
            "Edward", "Ronald", "Anthony", "Kevin", "Jason",
            "Gary", "Timothy", "Jose", "Larry", "Jeffrey",
            "Frank", "Scott", "Eric", "Stephen", "Andrew",
            "Raymond", "Gregory", "Joshua", "Jerry", "Dennis",
            "Walter", "Patrick", "Peter", "Harold", "Douglas" };

    String[] hobbies = { "Reading", "Writing", "Drawing", "Painting", "Sculpting", "Photography", "Gardening",
            "Cooking", "Baking", "Fishing", "Hiking", "Camping", "Traveling", "Playing music", "Singing", "Dancing",
            "Acting", "Watching movies", "Playing video games", "Playing sports", "Swimming", "Cycling", "Running",
            "Yoga", "Meditation", "Knitting", "Crocheting", "Embroidery", "Woodworking", "Metalworking", "Pottery",
            "Collecting stamps", "Collecting coins", "Collecting comics", "Collecting antiques", "Model building",
            "Numismatics", "Philately", "Gaming", "DIY projects" };

    private static final Logger logger = LogManager.getLogger(JsonProducer.class);

    private static ConcurrentHashMap<String, TestVeryComplexObj> messages = new ConcurrentHashMap<String, TestVeryComplexObj>();

    private Random random = new SecureRandom();

    private String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            char randomChar = CHARACTERS.charAt(randomIndex);
            sb.append(randomChar);
        }

        return sb.toString();
    }

    private int generateRndInt() {
        return random.nextInt();
    }

    private static String generateMillisTS() {
        long milliseconds = System.currentTimeMillis();

        Date date = new Date(milliseconds);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

        String formattedDate = sdf.format(date);

        return formattedDate;
    }

    private void generateMessage() {
        for (int i = 0; i < stringids.length; i++) {

            // choose 3 hobbies random
            List<String> hList = new LinkedList<>();
            for (int k = 0; k < 3; k++) {
                hList.add(hobbies[random.nextInt(hobbies.length)]);
            }

            messages.put(stringids[i],
                    new TestVeryComplexObj(stringids[i], generateRandomString(20), generateRandomString(256),
                            generateRandomString(256), generateRandomString(256), generateRandomString(256),
                            generateRndInt(), generateRndInt(), generateRndInt(), generateRndInt(), hList,
                            generateMillisTS()));

            logger.info("Generated first message for {} ok.", stringids[i]);
        }
    }

    public JsonVeryComplexProducer(String kafka_bootstrap_string, String pid, String topicname, int pause, int msgsize,
            boolean first) {
        super(kafka_bootstrap_string, pid, topicname, pause, msgsize);

        if (first) {
            generateMessage();
        }

        logger.info("Json complex producer {} ok.", pid);
    }

    @Override
    public void run() {
        Properties props = new Properties();
        props.put("bootstrap.servers", kafkabootstrapstring);
        props.put("linger.ms", 1);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                org.apache.kafka.common.serialization.StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                io.confluent.kafka.serializers.KafkaJsonSerializer.class);

        try {
            Future<RecordMetadata> futurek;
            Producer<String, TestVeryComplexObj> producer = new KafkaProducer<>(props);

            // Send a first message for each key in the stringids arrays
            for (Map.Entry<String, TestVeryComplexObj> entry : messages.entrySet()) {
                TestVeryComplexObj message = entry.getValue();
                futurek = producer
                        .send(new ProducerRecord<String, TestVeryComplexObj>(ktopicname, message.id, message));

                logger.debug("Sent message : {}", message.id);

                futurek.get();
            }

            while (goproduce) {
                int index = random.nextInt(stringids.length);
                String id = stringids[index];

                synchronized (messages) {
                    TestVeryComplexObj message = messages.get(id);

                    logger.debug("New Message for : {}", id);

                    index = random.nextInt(stringids.length);
                    if (index < 10) {
                        message.setChanges(generateRandomString(20));
                        message.setFourthNumber(generateRndInt());
                    } else if (index < 20) {
                        message.setChanges(generateRandomString(20));
                        message.setThirdNumber(generateRndInt());
                    } else if (index < 30) {
                        message.setChanges(generateRandomString(20));
                        message.setSecondNumber(generateRndInt());
                    } else {
                        message.setChanges(generateRandomString(20));
                        message.setFirstnumber(generateRndInt());
                    }
                    message.setTimestamp(generateMillisTS());

                    futurek = producer
                            .send(new ProducerRecord<String, TestVeryComplexObj>(ktopicname, message.id, message));
                }
                logger.debug("Sent message : {}", futurek.isDone());

                Thread.sleep(millisp);
                /*
                 * RecordMetadata rmtdta = futurek.get();
                 * 
                 * logger.debug("Partition : " + rmtdta.partition() + ", " + rmtdta.offset());
                 */
            }

            producer.close();

        } catch (Exception e) {
            logger.error("Error during producer loop: {}", e.getMessage());
        }
    }

}
