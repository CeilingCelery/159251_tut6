package nz.ac.vuw.swen301.tuts.log4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;

/**
 * The purpose of this class is to read and merge financial transactions, and print a summary:
 * - total amount 
 * - highest/lowest amount
 * - number of transactions 
 * @author jens dietrich
 */
public class MergeTransactions {

	private static DateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");
	private static NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(Locale.getDefault());
	private static Logger loggerFile = Logger.getLogger("FILE");
	private static Logger loggerConsole = Logger.getLogger("TRANSACTIONS");
	private static final Level logLevel = Level.DEBUG; 

	public static void main(String[] args) {
		loggerConsole.addAppender(new ConsoleAppender(new SimpleLayout()));
		try {
			loggerFile.addAppender(new FileAppender(new SimpleLayout(), "logs.txt", false));
			loggerFile.addAppender(new ConsoleAppender(new SimpleLayout()));
		} catch (IOException e) {
			loggerConsole.fatal("Unable to initiate file logger, exiting.", e);
		}
		loggerConsole.setLevel(logLevel);
		loggerFile.setLevel(logLevel);
		List<Purchase> transactions = new ArrayList<Purchase>();
		
		// read data from 4 files
		readData("transactions1.csv",transactions);
		readData("transactions2.csv",transactions);
		readData("transactions3.csv",transactions);
		readData("transactions4.csv",transactions);
		
		// print some info for the user
		loggerConsole.info("" + transactions.size() + " transactions imported");
		loggerConsole.info("total value: " + CURRENCY_FORMAT.format(computeTotalValue(transactions)));
		loggerConsole.info("max value: " + CURRENCY_FORMAT.format(computeMaxValue(transactions)));

	}
	
	private static double computeTotalValue(List<Purchase> transactions) {
		double v = 0.0;
		for (Purchase p:transactions) {
			v = v + p.getAmount();
		}
		return v;
	}
	
	private static double computeMaxValue(List<Purchase> transactions) {
		double v = 0.0;
		for (Purchase p:transactions) {
			v = Math.max(v,p.getAmount());
		}
		return v;
	}

	// read transactions from a file, and add them to a list
	private static void readData(String fileName, List<Purchase> transactions) {
		
		File file = new File(fileName);
		String line = null;
		// print info for user
		loggerFile.info("import data from " + fileName);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			while ((line = reader.readLine())!=null) {
				String[] values = line.split(",");
				Purchase purchase = new Purchase(
						values[0],
						Double.parseDouble(values[1]),
						DATE_FORMAT.parse(values[2])
				);
				transactions.add(purchase);
				// this is for debugging only
				loggerFile.debug("imported transaction " + purchase);
			} 
		}
		catch (FileNotFoundException x) {
			// print warning
			loggerFile.warn("file " + fileName + " does not exist - skip", x);
		}
		catch (IOException x) {
			// print error message and details
			loggerFile.error("problem reading file " + fileName, x);
		}
		// happens if date parsing fails
		catch (ParseException x) { 
			// print error message and details
			loggerFile.error("cannot parse date from string - please check whether syntax is correct: " + line, x);
		}
		// happens if double parsing fails
		catch (NumberFormatException x) {
			// print error message and details
			loggerFile.error("cannot parse double from string - please check whether syntax is correct: " + line, x);
		}
		catch (Exception x) {
			// any other exception 
			// print error message and details
			loggerFile.error("exception reading data from file " + fileName + ", line: " + line, x);
		}
		finally {
			try {
				if (reader!=null) {
					reader.close();
				}
			} catch (IOException e) {
				// print error message and details
				loggerFile.error("cannot close reader used to access " + fileName, e);
			}
		}
	}

}
