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

package gov.llnl.gnem.jsac.commands.signalMeasurement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import gov.llnl.gnem.jsac.commands.SacCommand;
import gov.llnl.gnem.jsac.commands.dataFile.PDWSetterBase;
import gov.llnl.gnem.jsac.util.PartialDataWindow;

/**
 *
 * @author dodge1
 */
public class MTWSacCommand extends PDWSetterBase implements SacCommand {

    private static final PartialDataWindow pdw = new PartialDataWindow();

    public MTWSacCommand() {
        super("MTW", pdw);
    }

    public static PartialDataWindow getPartialDataWindow()
    {
        return pdw;
    }
    
    @Override
    public void execute() {
        //Nothing to do. This command just sets its own state to be queried by other commands.
    }

    @Override
    public void initialize(String[] tokens) {
        super.initialize(tokens);
    }

    @Override
    public String getHelpString() {
        return "SUMMARY\n"
                + "	Determines the measurement time window for use in subsequent measurement commands.\n"
                + "\n"
                + "SYNTAX\n"
                + "	MTW {ON|OFF|pdw}\n"
                + "INPUT\n"
                + "	{ON}:	Turn measurement time window option on but don't change window values.\n"
                + "	OFF:	Turn measurement time window off. Measurements are done on the entire file.\n"
                + "	pdw:	Turn measurement time window on and set window values to a new \"partial data window.\" \n"
                + "	A pdw consists of a starting and a stopping value of the independent variable, usually time, \n"
                + "	which defines the desired window of data that you wish to make measurements on. \n"
                + "	See the CUT command for a complete explanation of how to define and use a pdw. \n"
                + "	Some examples are given below.\n"
                + "DEFAULT VALUES\n"
                + "	MTW OFF\n"
                + "DESCRIPTION\n"
                + "	When this option is on, measurements are made on the data within the window only. \n"
                + "	When this option is off, measurements are made on the entire file. This option \n"
                + "	currently applies to the MARKPTP and MARKVALUE commands only. \n"
                + "EXAMPLES\n"
                + "	Some examples of pdw are given below:\n"
                + "\n"
                + "		B 0 30:    First 30 secs of the file.\n"
                + "		A -10 30:  From 10 secs before to 30 secs after first arrival.\n"
                + "		T3 -1 T7:  From 1 sec before T3 time pick to T7 time pick.\n"
                + "		B N 2048:  First 2048 points of file.\n"
                + "		30.2 48:   30.2 to 48 secs relative to file zero.";
    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = {"MTW"};
        return new ArrayList<>(Arrays.asList(names));
    }
}
