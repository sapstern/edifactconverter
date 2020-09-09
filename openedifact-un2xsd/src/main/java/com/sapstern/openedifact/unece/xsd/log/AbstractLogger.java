package com.sapstern.openedifact.unece.xsd.log;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.sapstern.openedifact.unece.xsd.XSDFileGenerator;

public abstract class AbstractLogger {

	protected static Logger LOGGER = null;
	protected static int theLogLevel = -1;

	static {
		Logger mainLogger = Logger.getLogger("com.sapstern.openedifact.unece.xsd");
		mainLogger.setUseParentHandlers(false);
		ConsoleHandler handler = new ConsoleHandler();
		setLogLevel(handler);
		handler.setFormatter(new SimpleFormatter() {
			private static final String format = "[%1$tF %1$tT] [%2$-7s] %3$s %n";

			@Override
			public synchronized String format(LogRecord lr) {
				return String.format(format, new Date(lr.getMillis()), lr.getLevel().getLocalizedName(),
						lr.getMessage());
			}
		});
		mainLogger.addHandler(handler);
		LOGGER = Logger.getLogger(XSDFileGenerator.class.getName());
	}

	/**
	 * Set logging from command line arg (if any given)
	 */
	private static void setLogLevel(Handler handler) {

		Properties props = new Properties();
		try {
			props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("generator.properties"));
			theLogLevel = Integer.parseInt(props.getProperty("logLevel"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			//System.exit(1);
		}
		
		switch (theLogLevel) {
		case Integer.MAX_VALUE:
			handler.setLevel(Level.OFF);
			break;
		case Integer.MIN_VALUE:
			handler.setLevel(Level.ALL);
			break;
		case 300:
			handler.setLevel(Level.FINEST);
			break;
		case 400:
			handler.setLevel(Level.FINER);
			break;
		case 500:
			handler.setLevel(Level.FINE);
			break;
		case 700:
			handler.setLevel(Level.CONFIG);
			break;
		case 800:
			handler.setLevel(Level.INFO);
			break;
		case 900:
			handler.setLevel(Level.WARNING);
			break;
		case 1000:
			handler.setLevel(Level.SEVERE);
			break;
		default:
			handler.setLevel(Level.OFF);
		}
		// LogManager.getLogManager().getLogger(Logger.GLOBAL_LOGGER_NAME).setLevel(LOGGER.getLevel());
	}

}
