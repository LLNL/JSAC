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

import gov.llnl.gnem.jsac.SacDataModel;
import gov.llnl.gnem.jsac.commands.SacCommand;
import gov.llnl.gnem.jsac.util.PartialDataWindow;

/**
 *
 * @author dodge1
 */
public class CutImSacCommand extends PDWSetterBase implements SacCommand {

    private static final PartialDataWindow pdw = new PartialDataWindow();

    public CutImSacCommand() {
        super("CUTIM", pdw);
    }

    public static PartialDataWindow getPartialDataWindow()
    {
        return pdw;
    }
    
    @Override
    public void execute() {
        SacDataModel.getInstance().cutFilesInMemory();
    }

    @Override
    public void initialize(String[] tokens) {
        super.initialize(tokens);
    }

    @Override
    public String getHelpString() {
        return "SUMMARY\n"
                + "	Cuts files in memory. \n"
                + "\n"
                + "SYNTAX\n"
                + "	CUTIM pdw \n"
                + "INPUT\n"
                + "	pdw:	Partial Data Window. It consists of a starting and a stopping value of the \n"
                + "			independent variable (usually time), which defines which segment of a file \n"
                + "			(or files) one wishes to retain. The most general form of a pde is :\n"
                + "			ref offset ref offset:, where ref is a reference value that is one of the \n"
                + "			following: B|E|O|A|F|Tn, where n=0,1...9. \n"
                + "	offset:	A positive or negative number which is added to the reference value (optional).\n"
                + "DEFAULT VALUES\n"
                + "	Start and stop reference values are required. See examples below for an exception. \n"
                + "	If the start or stop offset is omitted, it is assumed to be zero.";
    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = {"CUTIM"};
        return new ArrayList<>(Arrays.asList(names));
    }
}
