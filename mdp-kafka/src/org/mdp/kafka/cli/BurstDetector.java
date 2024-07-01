/**
 * Lab 6.
 * Worked alone from home.
 * @author Benjamin Alvial
 */

package org.mdp.kafka.cli;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.mdp.kafka.def.KafkaConstants;

public class BurstDetector {
	public static final String[] EARTHQUAKE_SUBSTRINGS = new String[] { "terremoto", "temblor", "sismo", "quake" };
	
	public static void main(String[] args) throws FileNotFoundException, IOException{
		if(args.length!=1){
			System.err.println("Usage [inputTopic]");
			return;
		}
		
		Properties props = KafkaConstants.PROPS;

		// randomise consumer ID so messages cannot be read by another consumer
		//   (or at least it's more likely that a meteor wipes out life on Earth)
		props.put(ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString());
		
		KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(props);
		
		consumer.subscribe(Arrays.asList(args[0]));
		
		// three passengers in 60 seconds, end event after 120 seconds
		final int FIFO_SIZE = 3;
		final int EVENT_START_TIME_INTERVAL = 60 * 1000;	
		final int EVENT_END_TIME_INTERVAL = 2 * EVENT_START_TIME_INTERVAL;
		
		// Map of queues and map of events for each unique bus stop
		final Map<String, LinkedList<ConsumerRecord<String, String>>> queuesMap = new HashMap<>();
	    final Map<String, Boolean> inEventMap = new HashMap<>();
		
		int events = 0;
			
		try{
			while (true) {
				// every ten milliseconds get all records in a batch
				ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(10));
				
				// for all records in the batch
				for (ConsumerRecord<String, String> record : records) {
					
					// Extract the stopCode (example: PB241)
					String stopCode = record.value().split("##")[0];
					// We will want to extract associated queue and inEvent variable
					LinkedList<ConsumerRecord<String, String>> fifo;
					boolean inEvent;
					
					if(queuesMap.get(stopCode) == null) {
						// For each new unique stopCode not present in the maps, create a queue and inEvent variable
						fifo = new LinkedList<ConsumerRecord<String, String>>();
						inEvent = false;
						queuesMap.put(stopCode, fifo);
						inEventMap.put(stopCode, inEvent);
					}
					
					else {
						// If the stopCode already exists, extract its queue and inEvent variable
						fifo = queuesMap.get(stopCode);
						inEvent = inEventMap.get(stopCode);
					}
					
					fifo.add(record);
							
					if(fifo.size()>=FIFO_SIZE) {
								
						ConsumerRecord<String, String> oldest = fifo.removeFirst();
						long gap = record.timestamp() - oldest.timestamp();
								
						if(gap <= EVENT_START_TIME_INTERVAL && !inEvent) {
							inEventMap.put(stopCode,  true);
							String timeData = oldest.value().split("##")[1];
							String busRoute = oldest.value().split("##")[4];
							String stopNiceCode = oldest.value().split("##")[5];
							String stopLiteralName = oldest.value().split("##")[7];
							System.out.println("START EVENT:  id " + events + " at " + timeData + " on route " + busRoute + " on stop " + stopNiceCode + " (" + stopLiteralName + ")");
							events++;
						} else if(gap >= EVENT_END_TIME_INTERVAL && inEvent) {
							inEventMap.put(stopCode,  false);
							System.out.println("END EVENT: id " + events + " at rate " + FIFO_SIZE + " boardings in " + gap/60000 + " min");
						}

					}
				}
			}
		} finally{
			consumer.close();
		}
	}
}
