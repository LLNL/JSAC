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
import llnl.gnem.dftt.core.util.PairT;

/**
 *
 * @author dodge1
 */
public class ChnhdrSacCommand implements SacCommand {

    private static final List<AttributeDescriptor> descriptors = new ArrayList<>();

    static {
        descriptors.add(new AttributeDescriptor("FILE", ValuePossibilities.ONE_OR_MORE, Integer.class));
        descriptors.add(new AttributeDescriptor("ALLT", ValuePossibilities.ONE_VALUE, Double.class));
    }

    private final List<Integer> fileNumbers;
    private final List<PairT<String, Object>> newHeaderValues;
    private Double allTValue = null;

    public ChnhdrSacCommand() {

        fileNumbers = new ArrayList<>();
        newHeaderValues = new ArrayList<>();
    }

    @Override
    public void initialize(String[] tokens) {
        fileNumbers.clear();
        newHeaderValues.clear();
        allTValue = null;
        Map<String, List<Object>> parsedTokens = TokenListParser.parseTokens(descriptors, tokens, true);
        if (parsedTokens.isEmpty()) {
            return;
        }
        List<Object> files = parsedTokens.remove("FILE");
        if (files != null) {
            for (Object fileNumber : files) {
                fileNumbers.add((Integer) fileNumber);
            }
        }

        List<Object> allTValues = parsedTokens.remove("ALLT");
        if (allTValues != null && allTValues.size() == 1) {
            allTValue = (Double) allTValues.get(0);
        }
        for (String fieldName : parsedTokens.keySet()) {
            List<Object> values = parsedTokens.get(fieldName);
            if (values != null && values.size() == 1) {
                newHeaderValues.add(new PairT<>(fieldName, values.get(0)));
            }
        }
    }

    @Override
    public void execute() {
        SacDataModel.getInstance().changeHeaderValues(fileNumbers, newHeaderValues, allTValue);
    }

    @Override
    public String getHelpString() {
        return "SUMMARY:\n"
                + " Changes the values of selected header fields.\n"
                + "\n"
                + " SYNTAX:\n"
                + " CHNHDR  { file n1 n2 ... } field v  {field v ... }\n"
                + "\n"
                + " INPUT:\n"
                + "       file  :  This is an optional keyword that can be followed by\n"
                + "                a list of numbers indicating which file's headers\n"
                + "                are to be changed.\n"
                + "\n"
                + "       n1 n2 ...:  Integers indicating which file's headers to change.\n"
                + "\n"
                + "\n"
                + "       field  :  The name of a SAC header variable.  These variables are listed\n"
                + "           in an appendix to this manual.  Also, field may be the keyword ALLT\n"
                + "           as discussed below.  Note:  in order to maintain internal consistency,\n"
                + "           the following header variables cannot be changed with CHNHDR:\n"
                + "           NVHDR, NPTS, NWFID, NORID, and NEVID.\n"
                + "\n"
                + "       v  :  Set the value of that field to v.  The type of the field and its\n"
                + "           new value must match.  Use single quotes for alphanumeric fields\n"
                + "           with embedded blanks.  Use TRUE or FALSE for logical fields.  YES or\n"
                + "           NO are also acceptable for logical fields.  Use variable names (see\n"
                + "           appendix) for value fields.  For offset time fields (B, E, O, A, F,\n"
                + "           and Tn), v may also be of the form: GMT v1 v2 v3 v4 v5 v6 where v1,\n"
                + "           v2, v3, v4, v5, and v6 are the GMT year, julian day, hour, minute,\n"
                + "           second, and millisecond of the time.  If v1 is a two digit number,\n"
                + "           SAC will assume it is in the current century, unless that would mean\n"
                + "           that the year is in the future yet, in which case, SAC assumes the\n"
                + "           previous century.  To be certain you get what you want, use four\n"
                + "           digits.\n"
                + "\n"
                + "       UNDEF  :  Use this keyword instead of v to \"undefine\" a header field.\n"
                + "\n"
                + "       ALLT v  :  Add v seconds to all defined header times.  Subtract v\n"
                + "           seconds from the zero time.\n"
                + "\n"
                + " DESCRIPTION:\n"
                + "       This command lets you change any of SAC's header fields.\n"
                + " A specific file or list of files can be changed by specifying the\n"
                + " integer value(s) corresponding to the order in which the file(s)\n"
                + " were read in.  If no integer filelist is specified, all files in memory\n"
                + " will have their header fields changed.  To change the headers of the\n"
                + " files on disk follow this command with the WRITE or WRITEHDR command.\n"
                + " SAC does some validity checking on the new values but you may want to\n"
                + " verify the results using the LISTHDR command.\n"
                + "       There is a set of six variables in the header (NZYEAR, NZJDAY, NZHOUR,\n"
                + " NZMIN, NZSEC, and NZMSEC) which contain the reference or \"zero\" time of the\n"
                + " file.  This is the only GMT in the SAC header.  All other times in the header\n"
                + " (B, E, O, A, F, and Tn) are offsets in seconds relative to this reference\n"
                + " time.  You may change the reference time and all of the defined offset times\n"
                + " by using the \"ALLT v\" option.  That number of seconds are added to each\n"
                + " defined offset time.  That same number of seconds is also subtracted from the\n"
                + " reference time.  This preserves the actual GMT time of the data.  As a\n"
                + " convenience, you may enter a GMT time instead of a relative time when\n"
                + " changing the offset times.  When the GMT time is entered it is converted to\n"
                + " a relative time before storing it in the offset time field.\n"
                + "\n"
                + " EXAMPLES:\n"
                + " To define the event latitude, longitude and name in all the files in memory:\n"
                + "\n"
                + "       u:  CHNHDR EVLA 34.3 EVLO -118.5\n"
                + "\n"
                + "       u:  CHNHDR KEVNM 'LA goes under'\n"
                + "\n"
                + " To define the event latitude, longitude and name in files 2 and 4:\n"
                + "\n"
                + "       u:  CHNHDR file 2 4 EVLA 34.3 EVLO -118.5\n"
                + "\n"
                + "       u:  CHNHDR file 2 4 KEVNM 'LA goes under'\n"
                + "\n"
                + " To change the event type to earthquake:\n"
                + "\n"
                + "       u:  CHNHDR IEVTYP IQUAKE\n"
                + "\n"
                + " To set the first arrival time to its undefined state:\n"
                + "\n"
                + "       u:  CHNHDR A UNDEF\n"
                + "\n"
                + " Assume you know the GMT origin time of an event and that you want to quickly\n"
                + " change all the times in the header so that this origin time is the zero or\n"
                + " reference time and all other offset times are correct relative to this time.\n"
                + " First set the origin time using the GMT option:\n"
                + "\n"
                + "       u:  CHNHDR O GMT 1982 123 13 37 10 103\n"
                + "\n"
                + " If no reference time was set prior to executing this command then this command will\n"
                + " set the reference time to MAY 03 (123), 1982 13:37:10.103. Any other\n"
                + " offset times are left unchanged.\n"
                + " \n"
                + " If the reference time was set prior to executing the command then the existing \n"
                + " reference time is changed and all offset times except for O are changed by the\n"
                + " difference of the supplied GMT time and the existing reference time.\n"
                + " \n"
                + " In either case, O is set to zero\n"
                + " \n"
                + " HEADER CHANGES:\n"
                + " Potentially all header fields.";
    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = {"CH", "CHNHDR"};
        return new ArrayList<>(Arrays.asList(names));
    }

}
