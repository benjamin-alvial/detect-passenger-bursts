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
		
		// The dictionary is created as a HashMap
		// The original csv dictionary has lines of the form
		// micro,paradero,codigo_ts,codigo,comuna,nombre_paradero
		// 201,PB241,T-3-374-NS-5,,HUECHURABA,Parada / Mall Plaza Norte - Los Libertadores
		final HashMap<String, String> translationMap = new HashMap<>();
		BufferedReader reader = new BufferedReader(new InputStreamReader (new FileInputStream ("/data/2024/uhadoop/projects/group_22/diccionario_codigo_paradero_final.csv")));
		String line;
		while ((line = reader.readLine()) != null) {
			String[] parts = line.split(",");
			if (parts.length == 6) {
				String key = parts[2]; // example T-3-374-NS-5
				String value = parts[0] + "," + parts[1] + "," + parts[4] + "," + parts[5]; // example 201,PB241,,HUECHURABA,Parada / Mall Plaza Norte - Los Libertadores
				System.out.println(key);
				System.out.println(value);
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
					
					// here, record.value() is for example
					// L-26-38-5-PO##2023-04-17 00:05:40.000##BUS##T353 03I
					String[] boardingInfo = record.value().split("##");
					String stopCodeToLookUp = boardingInfo[0];
					String timeData = boardingInfo[1];
					String modeType = boardingInfo[2];
					String extraCode = boardingInfo[3];

					// and translationMap is...
					// key: T-3-374-NS-5
					// value: 201,PB241,,HUECHURABA,Parada / Mall Plaza Norte - Los Libertadores
					String dictionaryInfo = translationMap.get(stopCodeToLookUp);
					
					if (dictionaryInfo != null) {
						
						String[] infoParts = dictionaryInfo.split(",");
						String busRoute = infoParts[0];
						String stopNiceCode = infoParts[1];
						String stopCommune = infoParts[2];
						String stopLiteralName = infoParts[3];
						
						// Generate a record with all the useful information separated by ##
						String newRecord = record.value() + "##" + busRoute + "##" + stopNiceCode + "##" + stopCommune + "##" + stopLiteralName;

						if(busRoute.equals("506") || busRoute.equals("506v")) {
							System.out.println(newRecord);
							producer.send(new ProducerRecord<>(args[1], 0, record.timestamp(), record.key(), newRecord));
						}
					}
				}
			}
		} finally{
			consumer.close();
			producer.close();
		}
	}

}
