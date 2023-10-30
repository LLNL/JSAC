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
package gov.llnl.gnem.jsac.commands.binary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import gov.llnl.gnem.jsac.commands.AttributeDescriptor;
import gov.llnl.gnem.jsac.commands.Misc;
import gov.llnl.gnem.jsac.commands.SacCommand;
import gov.llnl.gnem.jsac.commands.TokenListParser;
import gov.llnl.gnem.jsac.commands.ValuePossibilities;
import gov.llnl.gnem.jsac.dataAccess.dataObjects.SacTraceData.BinOpType;
import gov.llnl.gnem.jsac.util.OnOffFlag;

/**
 *
 * @author dodge1
 */
public class MulfSacCommand implements SacCommand {

    private boolean replaceHeaders = false;
    private static final List<AttributeDescriptor> descriptors = new ArrayList<>();
    private final List<String> filenames;

    static {
        descriptors.add(new AttributeDescriptor("NEWHDR", ValuePossibilities.ONE_VALUE, OnOffFlag.class));
    }

    public MulfSacCommand() {
        filenames = new ArrayList<>();
    }

    @Override
    public void execute() {
        if (filenames.isEmpty()) {
            System.out.println("No file names were supplied!");
            return;
        }
        Misc.buildAndProcessList(filenames, BinOpType.MUL,replaceHeaders);
    }

    @Override
    public void initialize(String[] tokens) {
         replaceHeaders = false; 
        filenames.clear();
       Map<String, List<Object>> parsedTokens = TokenListParser.parseTokens(descriptors, tokens);

        if (parsedTokens.containsKey("NEWHDR")) {
            replaceHeaders = (Boolean) parsedTokens.get("NEWHDR").get(0);
        }

        List<Object> tmp = parsedTokens.remove(TokenListParser.LEFT_OVER_TOKENS);
        if (tmp != null) {
            for (Object obj : tmp) {
                filenames.add((String) obj);
            }
        }

    }

    @Override
    public String getHelpString() {
        return "SUMMARY\n"
                + "	Multiplies a set of data files by the data in memory.\n"
                + "\n"
                + "SYNTAX\n"
                + "	MULF {NEWHDR ON|OFF} filelist\n"
                + "INPUT\n"
                + "	NEWHDR ON|OFF:	By default, the resultant file will take its header field from the original file in memory. \n"
                + "	Turning NEWHDR ON, causes the header fields to be taken from the new file in the filelist.\n"
                + "	\n"
                + "	filelist:	A list of SAC binary data files. This list may contain simple filenames, \n"
                + "				full or relative pathnames.\n"
                + "DESCRIPTION\n"
                + "	This command can be used to multiply a single file by a set of files or to multiply one set of files by another set. \n"
                + "	An example of each case is presented below. The files must be evenly spaced and must have the same sampling \n"
                + "	interval and number of data points. If there are more data files in memory than in the filelist, \n"
                + "	then the last file in the filelist is used for the remainder of the data files in memory.\n"
                + "EXAMPLES\n"
                + "	To multiply three files by a single file :\n"
                + "\n"
                + "		SAC> READ FILE1 FILE2 FILE3\n"
                + "		SAC> MULF FILE4\n"
                + "		\n"
                + "	To multiply two files by two other files:\n"
                + "\n"
                + "		SAC> READ FILE1 FILE2\n"
                + "		SAC> MULF FILE3 FILE4\n"
                + "\n"
                + "HEADER CHANGES\n"
                + "	If NEWHDR is OFF (the default) the headers in memory are unchanged).\n"
                + "\n"
                + "	If NEWHDR is ON, the headers are replaced with the headers from the files in the filelist.\n"
                + "\n"
                + "DEPMIN, DEPMAX, DEPMEN";
    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = {"MULF"};
        return new ArrayList<>(Arrays.asList(names));
    }

}
