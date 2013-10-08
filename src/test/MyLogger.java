package test;

/**
 * @author      Jiao Jing Ping
 * @CoAuthor	Steve Ng
 * @version     0.2                                   
 * @since       2010-10-30    
 */

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class MyLogger {
	private static MyLogger geekDoLogger;
	private FileHandler fh;
	private final static String fileName = "GeekdoLog.txt";
	private final static Logger logger = Logger.getLogger("GeekdoLog");

	public static MyLogger getInstance() {
		if (geekDoLogger == null) {
			geekDoLogger = new MyLogger();
		}
		return geekDoLogger;
	}

	private MyLogger() {
		try {
			fh = new FileHandler(fileName, false);
			logger.addHandler(fh);
		} catch (SecurityException e) {
			logger.severe("MyLogger securityException " +e.getMessage());
		} catch (IOException e) {
			logger.severe("MyLogger IOException "+e.getMessage());
		}
	}

	public String getFileName() {
		return fileName;
	}

	public void logException(String Exception) {

		MyFormatter formatter = new MyFormatter("Exception: ");
		fh.setFormatter(formatter);
		logger.severe(Exception);
	}
	
	public void logInfo(String input) {

		MyFormatter formatter = new MyFormatter("Info: ");
		fh.setFormatter(formatter);
		logger.info(input);
	}
}


class MyFormatter extends Formatter {
	private String callerInfo;

	public MyFormatter(String callerInfo) {
		// TODO Auto-generated constructor stub
		if (callerInfo == null) {
			this.callerInfo = "";
		}
		this.callerInfo = callerInfo;
	}

	@Override
	public String format(LogRecord record) {
		return callerInfo + record.getMessage() + "\n";
	}
}
