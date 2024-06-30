/**
 * Lab 6.
 * Worked alone from home.
 * @author Benjamin Alvial
 */


package org.mdp.kafka.cli;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.mdp.kafka.def.KafkaConstants;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

public class EarthquakeFilter {
	public static final String[] EARTHQUAKE_SUBSTRINGS = new String[] { "terremoto", "temblor", "sismo", "quake" };
	
	public static void main(String[] args) throws FileNotFoundException, IOException{
		if(args.length!=2){
			System.err.println("Usage [inputTopic] [outputTopic]");
			return;
		}
		
		Properties props = KafkaConstants.PROPS;
		
		
		
		final HashMap<String, String> translationMap = new HashMap<>();
		// Open the translation table CSV file
					//Path path = new Path("/data/2024/uhadoop/projects/group_22/diccionario_codigo_paradero_final.csv");
					//FileSystem fs = FileSystem.get(conf);
					//FSDataInputStream inputStream = fs.open(path);
					//BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
					BufferedReader reader = new BufferedReader(new InputStreamReader (new FileInputStream ("/data/2024/uhadoop/projects/group_22/diccionario_codigo_paradero_final.csv")));

					String line;
					while ((line = reader.readLine()) != null) {
						String[] parts = line.split(",");
						if (parts.length == 2) {
							String key = parts[2]; // example T-3-374-NS-5
							String value = parts[0] + "," + parts[1] + "," + parts[3] + "," + parts[4] + "," + parts[5]; // example 201,PB241,,HUECHURABA,Parada / Mall Plaza Norte - Los Libertadores
							translationMap.put(key, value);
						}
					}
					reader.close();
		
		
		
		
		

		// randomise consumer ID so messages cannot be read by another consumer
		//   (or at least it's more likely that a meteor wipes out life on Earth)
		props.put(ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString());
		
		KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(props);
		Producer<String, String> producer = new KafkaProducer<String, String>(props);
		
		consumer.subscribe(Arrays.asList(args[0]));
		
		try{
			while (true) {
				// every ten milliseconds get all records in a batch
				ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(10));
				
				// for all records in the batch
				for (ConsumerRecord<String, String> record : records) {
					String lowercase = record.value().toLowerCase();
					
					// check if record value contains keyword
					// (could be optimised a lot)
					//for(String ek: EARTHQUAKE_SUBSTRINGS){
						// if so print it out to the console
						//if(lowercase.contains(ek)){
					
					// here, record is for example
					// E-20-53-PO-70   2023-04-17 01:03:54.000##BUS##T301 00I
					// where record.key() is E-20-53-PO-70
					// and record.value() is 2023-04-17 01:03:54.000##BUS##T301 00I
					
					// and translationMap is...
					// T-3-374-NS-5
					// 201,PB241,,HUECHURABA,Parada / Mall Plaza Norte - Los Libertadores
					
					String dictionaryInfo = translationMap.get(record.key().toString());
					
					
					
					
					if (dictionaryInfo != null) {
						
						String[] infoParts = dictionaryInfo.split(",");
						String busRoute = infoParts[0];
						String stopCode = infoParts[1];
						String stopCommune = infoParts[3];
						String stopLiteralName = infoParts[4];
						
						String newRecord = record.value();
								
					
							System.out.println(record.value());
							
							producer.send(new ProducerRecord<>(args[1], 0, record.timestamp(), record.key(), record.value()));
							// prevents multiple prints of the same tweet with multiple keywords
							//break;
					}
						//}
					//}
				}
			}
		} finally{
			consumer.close();
			producer.close();
		}
	}

}
