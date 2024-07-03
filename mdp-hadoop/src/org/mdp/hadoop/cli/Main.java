package org.mdp.hadoop.cli;

import java.lang.reflect.Method;

/**
 * Class for running one of many possible command line tasks
 * in the CLI package.
 * 
 * @author Aidan Hogan
 */
public class Main {
	
	private static final String PREFIX = "org.mdp.hadoop.cli.";
	private static final String USAGE = "usage: "+Main.class.getName();

	/**
	 * Main method
	 * @param args Command line args, first of which is the utility to run
	 */
	public static void main(String[] args) {
		try {
			if (args.length < 1) {
                String sb = "missing <utility> arg where <utility> one of" +
                        "\n\t" + SortTimes.class.getSimpleName() + ": Sort boarding times ascending";
				
				usage(sb);
			}


			Class<?> cls = Class.forName(PREFIX + args[0]);

			Method mainMethod = cls.getMethod("main", String[].class);

			String[] mainArgs = new String[args.length - 1];
			System.arraycopy(args, 1, mainArgs, 0, mainArgs.length);

			long time = System.currentTimeMillis();
			
			mainMethod.invoke(null, new Object[] { mainArgs });

			long time1 = System.currentTimeMillis();

			System.err.println("time elapsed " + (time1-time) + " ms");
		} catch (Throwable e) {
			e.printStackTrace();
			usage(e.toString());
		}
	}

	private static void usage(String msg) {
		System.err.println(USAGE);
		System.err.println(msg);
		System.exit(-1);
	}
}
