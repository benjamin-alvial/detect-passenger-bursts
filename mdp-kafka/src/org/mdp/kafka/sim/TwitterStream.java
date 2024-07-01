package org.mdp.kafka.sim;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

public class TwitterStream implements Runnable {
	// cannot be static since not synchronised
	public final SimpleDateFormat TWITTER_DATE = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	
	BufferedReader br;
	long startSim = 0;
	long startData = 0;
	long lastData = 0;
	int speedup;
	int id; 
	Producer<String, String> producer;
	String topic;
	
	public TwitterStream(BufferedReader br, int id, Producer<String, String> producer, String topic, int speedup){
		this(br,id,System.currentTimeMillis(),producer,topic,speedup);
	}
	
	public TwitterStream(BufferedReader br, int id, long startSim, Producer<String, String> producer, String topic, int speedup){
		this.br = br;
		this.id = id;
		this.startSim = startSim;
		this.producer = producer;
		this.speedup = speedup;
		this.topic = topic;
	}

	@Override
	public void run() {
		String line;
		long wait = 0;
		int idd = 0;

		try{
			// Lines are of the type
			// L-17-24-30-SN   2023-04-17 00:05:27.000##BUS##T1103 00R
			while((line = br.readLine())!=null){

				String[] tabs = line.split("\t");

				if(tabs.length>0){
					try{
					
						long timeData = getUnixTime(tabs[1].split("##")[0]);

						if(startData == 0) // first element read
							startData = timeData;
						
						wait = calculateWait(timeData);
						
						//String idStr = tabs[id];
						String idStr = Integer.toString(idd);
						idd++;
						
						if(wait>0){
							Thread.sleep(wait);
						}
						
						String newLine = tabs[0] + "##" + tabs[1];
						
						// The newLine sent is of the type
						// L-26-38-5-PO##2023-04-17 00:05:40.000##BUS##T353 03I
						System.out.println(newLine);
						producer.send(new ProducerRecord<String,String>(topic, 0, timeData, idStr, newLine));
					} catch(ParseException | NumberFormatException pe){
						System.err.println("Cannot parse date "+tabs[0]);
					}
				}
				
				if (Thread.interrupted()) {
				    throw new InterruptedException();
				}
			}
		} catch(IOException ioe){
			System.err.println(ioe.getMessage());
		} catch(InterruptedException ie){
			System.err.println("Interrupted "+ie.getMessage());
		}
		
		System.err.println("Finished! Messages were "+wait+" ms from target speed-up times.");
	}
	
	private long calculateWait(long time) {
		long current = System.currentTimeMillis();
		
		// how long we have waited since start
		long delaySim = current - startSim;
		if(delaySim<0){
			// the first element ...
			// wait until startSim
			return delaySim*-1;
		}
		
		// calculate how long we should wait since start
		long delayData = time - startData;
		long shouldDelay = delayData / speedup;
		
		// if we've already waited long enough
		if(delaySim>=shouldDelay) return 0;
		// otherwise return wait time
		else return shouldDelay - delaySim;
	}

	// example 2017-09-19 00:07:03
	public long getUnixTime(String dateTime) throws ParseException{
		Date d = TWITTER_DATE.parse( dateTime );
		return d.getTime();
	}
}
