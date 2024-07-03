package org.mdp.hadoop.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Objects;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

/**
 * Java class to sort the boarding times of buses
 * 
 * @author Aidan
 * @author Carlos Ruz
 */
public class SortTimes {

	/**
	 * This is the Mapper Class. This sends key-value pairs to different machines
	 * based on the key.
	 * <p>
	 * Remember that the generic is Mapper<InputKey, InputValue, MapKey, MapValue>
	 * <p>
	 * InputKey we don't care about (a LongWritable will be passed as the input
	 * file offset, but we don't care; we can also set as Object)
	 * <p>
	 * InputKey will be Text: a line of the file
	 * <p>
	 * MapKey will be Text: string with boarding time formatted like dd/mm/yyyy hh:mm
	 * <p>
	 * MapValue will be Text: string with boarding stop code
	 * 
	 * @author Aidan
	 * @author Carlos Ruz
	 */
	public static class SortTimesMapper extends
		Mapper<Object, Text, Text, Text>{

		/**
		 * @throws InterruptedException 
		 * 
		 * Each input line should be as follows:
		 * <p>
		 * word[\t]count
		 * <p>
		 * Parse this and map count as key, word as value.
		 * <p>
		 * Note DescendingIntWritable, which offers
		 * 	inverse sorting (largest first!)
		 * 
		 */
		@Override
		public void map(Object key, Text value, Context output)
						throws IOException, InterruptedException {
			String[] split = value.toString().split(";");
			output.write(new Text(split[7]), new Text(split[3]+"##"+split[20]+"##"+split[18]));
		}
	}

	/**
	 * This is the Reducer Class.
	 * <p>
	 * This collects sets of key-value pairs with the same key on one machine. 
	 * <p>
	 * Remember that the generic is Reducer<MapKey, MapValue, OutputKey, OutputValue>
	 * <p>
	 * MapKey will be Text: string with boarding time formatted like dd/mm/yyyy hh:mm
	 * <p>
	 * MapValue will be Text: string with boarding stop code
	 * <p>
	 * OutputKey will be Text: the boarding stop code
	 * <p>
	 * OutputValue will be Text: the time (sorted this time!)
	 * 
	 * @author Aidan
	 * @author Carlos Ruz
	 *
	 */
	public static class SortTimesReducer
	       extends Reducer<Text, Text, Text, Text> {

		/**
		 * @throws InterruptedException 
		 * 
		 * The keys (counts) are called in ascending order ...
		 * ... so for each value (word) of a key, we just write
		 * (value,key) pairs to the output and we're done.
		 * 
		 */
		@Override
		public void reduce(Text key, Iterable<Text> values,
				Context output) throws IOException, InterruptedException {

			for(Text value:values) {
				String[] split = value.toString().split("##");
				if (split.length > 2) {
					output.write(new Text(split[1]), new Text(key.toString()+"##"+split[0]+"##"+split[2]));
				}
			}
		}
	}

	/**
	 * Main method that sets up and runs the job
	 * 
	 * @param args First argument is input, second is output
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
		if (otherArgs.length != 2) {
			System.err.println("Usage: "+ SortTimes.class.getName()+" <in> <out>");
			System.exit(2);
		}
		String inputLocation = otherArgs[0];
		String outputLocation = otherArgs[1];

		Job job = Job.getInstance(new Configuration());
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		
		job.setMapperClass(SortTimesMapper.class); // no combiner this time!
		job.setReducerClass(SortTimesReducer.class);

		FileInputFormat.setInputPaths(job, new Path(inputLocation));
		FileOutputFormat.setOutputPath(job, new Path(outputLocation));

		job.setJarByClass(SortTimes.class);
		job.waitForCompletion(true);
	}
}
