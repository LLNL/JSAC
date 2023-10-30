/*-
 * #%L
 * Java Seismic Analysis Code (JSAC)
 *  LLNL-CODE-855505
 *  This work was performed under the auspices of the U.S. Department of Energy
 *  by Lawrence Livermore National Laboratory under Contract DE-AC52-07NA27344.
 * %%
 * Copyright (C) 2022 - 2023 Lawrence Livermore National Laboratory
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package gov.llnl.gnem.jsac;

import java.util.ServiceLoader;

import javax.swing.SwingUtilities;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import gov.llnl.gnem.jsac.commands.SacCommandParser;
import gov.llnl.gnem.jsac.plots.plot.PlotFrame;
import llnl.gnem.dftt.core.gui.util.ExceptionDialog;
import llnl.gnem.dftt.core.util.FileUtil.DriveMapper;

/**
 *
 * @author dodge1
 */
public class MainClass {

    private static final Logger log = LoggerFactory.getLogger(MainClass.class);

    private static ServiceLoader<CommandParser> additionalParsers = ServiceLoader.load(CommandParser.class);
 static {
      System.setProperty("org.apache.commons.logging.Log",
                         "org.apache.commons.logging.impl.NoOpLog");
   }
    public static void main(String[] args) {
        getCommandLineInfo(args);
        DriveMapper.setupWindowsNFSDriveMap();

        SwingUtilities.invokeLater(() -> {
            try {
                PlotFrame.getInstance().setVisible(false);
            } catch (Exception e) {
                ExceptionDialog.displayError(e);
            }
        });
        try {
            SacCommandParser.getInstance().initialize();
        } catch (Exception ex) {
            log.error(ex.getLocalizedMessage(), ex);
            System.exit(1);
        }
        Runnable runnable = new Interpreter();

        Thread thread = new Thread(runnable);
        thread.start();

    }

    private static void getCommandLineInfo(String[] args) {
        Options options = new Options();

        Option help = new Option("h", "help", false, "Eventually show general help message...");
        Option logLevelOption = new Option("L", "LogLevel", true, "The logging level to use.");

        DefaultParser baseParser = new DefaultParser();
        options.addOption(help);
        options.addOption(logLevelOption);
        try {
            CommandLine cmd = baseParser.parse(options, args);

            if (cmd.hasOption(logLevelOption.getOpt())) {
                String level = cmd.getOptionValue(logLevelOption.getOpt());
                LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
                ch.qos.logback.classic.Logger logger = context.getLogger(Logger.ROOT_LOGGER_NAME);
                if (logger != null) {
                    logger.setLevel(Level.toLevel(level));
                }
            }
            additionalParsers.forEach(f -> f.apply(options, args));
        } catch (Exception ex) {
            log.error(ex.getLocalizedMessage(), ex);
            System.exit(2);
        }
    }

}
