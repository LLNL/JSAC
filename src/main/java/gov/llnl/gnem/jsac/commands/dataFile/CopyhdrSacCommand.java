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
import java.util.Map;

import gov.llnl.gnem.jsac.SacDataModel;
import gov.llnl.gnem.jsac.commands.AttributeDescriptor;
import gov.llnl.gnem.jsac.commands.SacCommand;
import gov.llnl.gnem.jsac.commands.TokenListParser;
import gov.llnl.gnem.jsac.commands.ValuePossibilities;

/**
 *
 * @author dodge1
 */
public class CopyhdrSacCommand implements SacCommand {

    private static final List<AttributeDescriptor> descriptors = new ArrayList<>();

    static {
        descriptors.add(new AttributeDescriptor("FROM", ValuePossibilities.ONE_VALUE, String.class));
    }

    private String fromValue = null;
    private final List<String> headerValues;

    public CopyhdrSacCommand() {

        headerValues = new ArrayList<>();
    }

    @Override
    public void initialize(String[] tokens) {
        headerValues.clear();
        fromValue = null;
        Map<String, List<Object>> parsedTokens = TokenListParser.parseTokens(descriptors, tokens, true);
        if (parsedTokens.isEmpty()) {
            String msg = String.format("COPYHDR requires a FROM value and at least one header name!");
            throw new IllegalStateException(msg);
        }
        List<Object> junk = parsedTokens.remove(TokenListParser.LEFT_OVER_TOKENS);
        if (junk != null && !junk.isEmpty()) {
            StringBuilder sb = new StringBuilder("Unexpected tokens: ");
            for (Object obj : junk) {
                sb.append(obj).append(" ");
            }
            sb.append("!");
            throw new IllegalStateException(sb.toString());
        }

        List<Object> files = parsedTokens.remove("FROM");
        if (files == null || files.size() != 1) {
            String msg = String.format("COPYHDR requires a single FROM value!");
            throw new IllegalStateException(msg);
        }
        fromValue = (String) files.get(0);

        for (String fieldName : parsedTokens.keySet()) {
            headerValues.add(fieldName);
        }
    }

    @Override
    public void execute() {
        if (fromValue != null && !headerValues.isEmpty()) {
            SacDataModel.getInstance().copyHeaderValues(fromValue, headerValues);
        }
    }

    @Override
    public String getHelpString() {
        return "SUMMARY\n"
                + "	Copies header variables from one file in memory to all others.\n"
                + "\n"
                + "SYNTAX\n"
                + "	COPYHDR {FROM name|n} hdrlist\n"
                + "INPUT\n"
                + "	FROM name:	Copy header list from named file in memory.\n"
                + "	FROM n:	Copy header list from numbered file in memory.\n"
                + "	hdrlist:	Space delimited list of header variables to copy.\n"
                + "DEFAULT VALUES\n"
                + "	COPYHDR FROM 1\n"
                + "DESCRIPTION\n"
                + "	This command lets you copy the values of any SAC header variable from one file in memory to all of the remaining files in memory. You can select which file you want to copy from.\n"
                + "EXAMPLES\n"
                + "	Assume you are using PPK to mark several times in the header of a file called FILE1. \n"
                + "	You are using the header variables T3 and T4. To copy those same markers into files FILE2 and FILE3:\n"
                + "\n"
                + "	SAC> READ FILE1\n"
                + "	SAC> PPK\n"
                + "	SAC> ... use cursor to mark times T3 and T4.\n"
                + "	SAC> READ MORE FILE2 FILE3\n"
                + "	SAC> COPYHDR FROM 1 T3 T4\n"
                + "	In this next example, assume you have read in a large number of files and you want to \n"
                + "	copy the event location, EVLA and EVLO, from the file called ABC into all of the other \n"
                + "	headers. This can be easily done by referencing the file by name not number:\n"
                + "\n"
                + "	SAC> COPYHDR FROM ABC STLA STLO\n"
                + "HEADER CHANGES\n"
                + "	Potentially all.";
    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = {"COPYHDR"};
        return new ArrayList<>(Arrays.asList(names));
    }

}
