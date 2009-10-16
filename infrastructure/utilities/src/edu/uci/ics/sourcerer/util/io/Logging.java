/* 
 * Sourcerer: an infrastructure for large-scale source code analysis.
 * Copyright (C) by contributors. See CONTRIBUTORS.txt for full list.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package edu.uci.ics.sourcerer.util.io;

import static edu.uci.ics.sourcerer.util.io.Properties.OUTPUT;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.util.io.properties.BooleanProperty;
import edu.uci.ics.sourcerer.util.io.properties.StringProperty;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
@SuppressWarnings("serial")
public final class Logging {
  protected static final Property<Boolean> SUPPRESS_FILE_LOGGING = new BooleanProperty("suppress-file-logging", false, "Logging", "Suppresses all logging to files.");
  protected static final Property<Boolean> REPORT_TO_CONSOLE = new BooleanProperty("report-to-console", false, "Logging", "Prints all the logging messages to the console.");
  
  protected static final Property<String> ERROR_LOG = new StringProperty("error-log", "error.log", "Logging", "Filename for error log.");
  
  protected static final Property<String> INFO_LOG = new StringProperty("info-log", "info.log", "Logging", "Filename for the info log.");
  
  protected static final Property<String> RESUME_LOG = new StringProperty("resume-log", "resume.log", "Logging", "Filename for the resume log.");
  protected static final Property<Boolean> CLEAR_RESUME_LOG = new BooleanProperty("clear-resume-log", false, "Logging", "Clears the resume log before beginning."); 
  
  private Logging() {}
  
  public static final Level RESUME = new Level("RESUME", 10000) {};
  private static boolean loggingInitialized = false;
  public static Logger logger;
  private static StreamHandler defaultHandler; 
  static {
    logger = Logger.getLogger("edu.uci.ics.sourcerer.util.io");
    logger.setUseParentHandlers(false);
    
    Formatter formatter = new Formatter() {
      @Override
      public String format(LogRecord record) {
        return Logging.formatError(record);
      }
    };
    defaultHandler = new StreamHandler(System.err, formatter);
    logger.addHandler(defaultHandler);
  }
    
  private static Set<String> getResumeSet(File resumeFile) {
    if (resumeFile.exists()) {
      Set<String> resumeSet = Helper.newHashSet();
      try {
        BufferedReader br = new BufferedReader(new FileReader(resumeFile));
        for (String line = br.readLine(); line != null; line = br.readLine()) {
          resumeSet.add(line);
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
      return resumeSet;
    } else {
      return Collections.emptySet();
    }
  }
  
  public synchronized static boolean loggingInitialized() {
    return loggingInitialized;
  }
    
  private static boolean resumeLoggingEnabled = false;
  public synchronized static Set<String> initializeResumeLogger() {
    if (resumeLoggingEnabled) {
      throw new IllegalStateException("Resume logging may only be initialized once");
    }
    File resumeFile = new File(OUTPUT.getValue(), RESUME_LOG.getValue());
    
    if (CLEAR_RESUME_LOG.getValue()) {
      if (resumeFile.exists()) {
        resumeFile.delete();
      }
    }
    
    Set<String> resumeSet = getResumeSet(resumeFile);
    
    if (!loggingInitialized) {
      initializeLogger();
    }
    
    try {
      FileHandler resumeHandler = new FileHandler(resumeFile.getPath(), true);
      resumeHandler.setLevel(RESUME);
      resumeHandler.setFormatter(new Formatter() {
        @Override
        public String format(LogRecord record) {
          return record.getMessage() + "\n";
        }
      });
      logger.addHandler(resumeHandler);
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
    
    resumeLoggingEnabled = true;
    return resumeSet;
  }
 
  public synchronized static void initializeLogger() {
    if (loggingInitialized) {
      throw new IllegalStateException("The logger may only be initialized once");
    }
   
    try {
      final boolean suppressFileLogging = SUPPRESS_FILE_LOGGING.getValue();
      final boolean reportToConsole = REPORT_TO_CONSOLE.getValue();
      
      if (suppressFileLogging && !reportToConsole) {
        return;
      }
      
      if (!suppressFileLogging) {
        OUTPUT.getValue().mkdirs();
      }
      
      Formatter errorFormatter = null;
      StreamHandler errorHandler = null;
      if (suppressFileLogging) {
        errorFormatter = new Formatter() {
          @Override
          public String format(LogRecord record) {
            if (record.getLevel() == RESUME) {
              return "";
            } else {
              return Logging.formatError(record);
            }
          }
        };
        errorHandler = new StreamHandler(System.err, errorFormatter);
      } else {
        errorFormatter = new Formatter() {
          @Override
          public String format(LogRecord record) {
            if (record.getLevel() == RESUME) {
              return "";
            } else {
              String msg = Logging.formatError(record);
              if (reportToConsole && record.getLevel() == Level.SEVERE) {
                System.err.print(msg);
              }
              return msg;
            }
          }
        };
        errorHandler = new FileHandler(new File(OUTPUT.getValue(), ERROR_LOG.getValue()).getPath());
        errorHandler.setFormatter(errorFormatter);
      }
      errorHandler.setLevel(Level.WARNING);
      
      Formatter infoFormatter = null;
      StreamHandler infoHandler = null;
      if (suppressFileLogging) {
        infoFormatter = new Formatter() {
          @Override
          public String format(LogRecord record) {
            return formatInfo(record);
          }
        };
        infoHandler = new StreamHandler(System.out, infoFormatter);
      } else {
        infoFormatter = new Formatter() {
          @Override
          public String format(LogRecord record) {
            if (record.getLevel() == Level.INFO) {
              String msg = formatInfo(record);
              if (reportToConsole) {
                System.out.print(msg);
              }
              return msg;
            } else {
              return "";
            }
          }
        };
        infoHandler = new FileHandler(new File(OUTPUT.getValue(), INFO_LOG.getValue()).getPath());
        infoHandler.setFormatter(infoFormatter);
      }
      infoHandler.setLevel(Level.INFO);
            
      logger.addHandler(errorHandler);
      logger.addHandler(infoHandler);
      
      logger.removeHandler(defaultHandler);
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }
  
  private static final DateFormat format = new SimpleDateFormat("HH:mm:ss.SSS");
  private static synchronized String formatError(LogRecord record) {
    StringWriter writer = new StringWriter();
    PrintWriter pw = new PrintWriter(writer);
    pw.print("[" + format.format(new Date(record.getMillis())) + " - " + record.getLevel() + "] ");
    pw.print(record.getMessage());
    if (record.getParameters() != null) {
      for (Object o : record.getParameters()) {
        pw.print(" " + o);
      }
    }
    if (record.getThrown() != null) {
      pw.println();
      record.getThrown().printStackTrace(pw);
    }
    pw.println();
    return writer.toString();
  }
  
  private static synchronized String formatInfo(LogRecord record) {
    return "[" + format.format(new Date(record.getMillis())) + "] " + record.getMessage() + "\n";
  }
}
