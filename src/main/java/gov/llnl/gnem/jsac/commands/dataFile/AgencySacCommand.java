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
import java.util.List;

import gov.llnl.gnem.jsac.SacDataModel;
import gov.llnl.gnem.jsac.commands.SacCommand;


public class AgencySacCommand implements SacCommand {

    private final List<String> mytokens;
    private final List< String> agencies;

    public AgencySacCommand() {
        mytokens = new ArrayList<>();
        agencies = new ArrayList<>();
        parseAgencies();
    }

    @Override
    public void initialize(String[] tokens) {
        mytokens.clear();
        agencies.clear();
        mytokens.addAll(Arrays.asList(tokens));
        mytokens.remove(0); // Don't need the command name anymore.
        parseAgencies();
    }

    @Override
    public void execute() {
        SacDataModel.getInstance().setAgency(agencies);
    }

    private void parseAgencies() {
        for (String token : mytokens) {
            agencies.add(token);
        }
    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = {"AGENCY"};
        return new ArrayList<>(Arrays.asList(names));
    }

    @Override
    public String getHelpString() {
        return "SUMMARY\n"
                + "	This command will set the agency of each data file in memory. \n"
                + "	The agency may be the same or different for each data file. \n"
                + "	If there are more data files in memory than agencies, then the \n"
                + "	last agency entered is used for the remainder of the data files in memory.\n"
                + "	To unset all agencies execute the command with no arguments. To\n"
                + "	unset the agency for a specific file enter the agency name as --.\n"
                + "\n"
                + "SYNTAX\n"
                + "	AGENCY {v1 {v2 ... vn} }\n"
                + "INPUT\n"
                + "	v1:	Agency for first file.\n"
                + "	v2:	Agency for second file.\n"
                + "	vn:	Agency for nth file.\n"
                + "\n"
                + "DEFAULT VALUES\n"
                + "	AGENCY NULL.";
    }
}
