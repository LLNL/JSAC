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

import java.util.NoSuchElementException;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.llnl.gnem.jsac.commands.SacCommand;
import gov.llnl.gnem.jsac.commands.SacCommandExecutor;
import gov.llnl.gnem.jsac.commands.SacCommandParser;

/**
 *
 * @author dodge1
 */
public class Interpreter implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(Interpreter.class);

    private final Scanner sc = new Scanner(System.in);

    @Override
    public void run() {
        displayPrompt();
        while (true) {
            try {
                String text = sc.nextLine().trim();
                log.trace("Got text: {}", text);
                String[] commands = text.split(";");
                for (String cmd : commands) {
                    log.info(cmd);
                    if (isCommand(cmd)) {
                        try {
                            SacCommand command = SacCommandParser.getInstance().parseCommandLine(cmd);
                            log.trace("Created command.");
                            if (command != null) {
                                try {
                                    SacCommandExecutor.getInstance().executeCommand(command);
                                    log.trace("Executed command.");
                                } catch (Exception ex) {
                                    log.warn("Failed executing command: {}", ex.getMessage());
                                }
                            } else {
                                log.warn("Unrecognized command:{}", cmd);
                            }
                        } catch (Exception ex) {
                            log.warn("Error parsing or preparing command: {}", ex.getMessage());
                        }
                    }
                    displayPrompt();
                }
            } catch (NoSuchElementException ex) {
                //Ctrl+C will evoke a runtime error on sc.next() that we want to catch here.
                log.trace(ex.getLocalizedMessage(), ex);
            }
        }
    }

    private static boolean isCommand(String text) {
        return !text.isEmpty() && !text.startsWith("*") && !text.startsWith("--");
    }

    private void displayPrompt() {
        System.out.print("SAC>");
        System.out.flush();
    }

}
