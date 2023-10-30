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

package gov.llnl.gnem.jsac.commands.dataFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import gov.llnl.gnem.jsac.commands.SacCommand;
import gov.llnl.gnem.jsac.util.PartialDataWindow;

/**
 *
 * @author dodge1
 */
public class CutSacCommand extends PDWSetterBase implements SacCommand {
    private static final PartialDataWindow pdw = new PartialDataWindow();

    public CutSacCommand() {
        super("CUT",pdw);
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
                + "	Defines the amount of a data file to be read. As discussed below, \n"
                + "	CUT can be preceded by CUTERR, which controls errors if the chosen \n"
                + "	amount includes times outside B to E. The syntax will then be \n"
                + "	CUTERR {CUTERR option} ; CUT {CUT options}. CUT does not act on data \n"
                + "	currently in memory: a call to CUT must be followed by a READ or FUNCGEN to take effect.\n"
                + "\n"
                + "SYNTAX\n"
                + "	CUT {ON|OFF|pdw|SIGNAL}\n"
                + "INPUT\n"
                + "	ON:	Turn cut option on but don't change pdw (see below).\n"
                + "	OFF:	Turn cut option off.\n"
                + "	pdw:	Turn cut option on and enter/change pdw. A pdw is a partial data window. \n"
                + "			It consists of a starting and a stopping value of the independent variable, \n"
                + "			usually time, which defines the segment of a file one wishes to read. The \n"
                + "			most general form of a pdw is ref offset ref offset, where ref is a number \n"
                + "			or a reference value that is one of the following: B|E|O|A|F|Tn, \n"
                + "			where n=0,1...9, and N, the number of points. The reference values are \n"
                + "			defined in SAC data file format and reviewed below.\n"
                + "	offset:	A positive or negative number that is added to the reference value.\n"
                + "	SIGNAL:	Equivalent to typing: A -1 F +1.\n"
                + "DEFAULT VALUES\n"
                + "	CUT OFF (equivalent to CUT b e)\n"
                + "	CUTERR FILLZ \n"
                + "DESCRIPTION\n"
                + "	The CUT command simply sets cut points and does not change the file in memory. \n"
                + "	For the command to take effect, CUT must be followed by a READ. This is in \n"
                + "	contrast with command CUTIM, which carries out cut (or cuts) on the data currently in memory.\n"
                + "\n"
                + "	If the start or stop offset is omitted it is assumed to be zero. If the start reference \n"
                + "	value is omitted it is assumed to be zero. If the stop reference value is omitted it is \n"
                + "	assumed to be the same as the start reference value.\n"
                + "\n"
                + "	With CUT off, the entire file is read. With CUT on, only that portion of the file \n"
                + "	between the starting and stopping cut values is read. These are values in terms of \n"
                + "	the independent variable in the data file, normally time.  The following header \n"
                + "	variables are used to represent certain values of the independent variable:\n"
                + "\n"
                + "	B:	Disk file beginning value;\n"
                + "	E:	Disk file ending value;\n"
                + "	O:	Event origin time;\n"
                + "	A:	First arrival time;\n"
                + "	F:	Signal end time;\n"
                + "	Tn:	User defined time picks (n = 0,1...9)";
    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = {"CUT"};
        return new ArrayList<>(Arrays.asList(names));
    }
}
