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
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.llnl.gnem.jsac.SacDataModel;
import gov.llnl.gnem.jsac.commands.AttributeDescriptor;
import gov.llnl.gnem.jsac.commands.SacCommand;
import gov.llnl.gnem.jsac.commands.TokenListParser;
import gov.llnl.gnem.jsac.commands.ValuePossibilities;
import gov.llnl.gnem.jsac.dataAccess.dataObjects.SacTraceData;
import gov.llnl.gnem.jsac.util.PartialDataWindow;

public class MarkptpSacCommand implements SacCommand {

    private static final Logger log = LoggerFactory.getLogger(MarkptpSacCommand.class);

    private static String toMarker = "T0";
    private static double length = 5.0;
    private static final PartialDataWindow pdw = new PartialDataWindow();

    private static final List<AttributeDescriptor> descriptors = new ArrayList<>();
    private static final List<String> refs;

    static {
        String[] foo = { "T0", "T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8" };
        refs = new ArrayList<>(Arrays.asList(foo));
    }

    static {
        descriptors.add(new AttributeDescriptor("TO", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("LENGTH", ValuePossibilities.ONE_OR_MORE, Double.class));
    }

    public MarkptpSacCommand() {

    }

    @Override
    public void initialize(String[] tokens) {

        Map<String, List<Object>> parsedTokens = TokenListParser.parseTokens(descriptors, tokens);

        List<Object> values = parsedTokens.remove("TO");
        if (values != null && values.size() == 1) {
            String tmp = (String) values.get(0);
            if (refs.contains(tmp.toUpperCase())) {
                toMarker = tmp.toUpperCase();
            } else {
                throw new IllegalStateException("Invalid marker: " + tmp.toUpperCase() + " Only T0 - T8 are allowed.");
            }
        }

        values = parsedTokens.remove("LENGTH");
        if (values != null && values.size() == 1) {
            length = (double) values.get(0);
        }

    }

    @Override
    public void execute() {

        List<SacTraceData> data = SacDataModel.getInstance().getData();
        int idx = refs.indexOf(toMarker);
        for (SacTraceData std : data) {
            if (std.isSpectral()) {
                String msg = String.format("ERROR 1307: Illegal operation on spectral file");
                log.warn(msg);
                continue;
            }

            std.markptp(MTWSacCommand.getPartialDataWindow(), length, idx);

        }
    }

    @Override
    public String getHelpString() {
        return "SUMMARY\n"
                + "	Measures and marks the maximum peak to peak amplitude of each signal within the measurement time window.\n"
                + "\n"
                + "SYNTAX\n"
                + "	MARKPTP {LENGTH v},{TO marker}\n"
                + "INPUT\n"
                + "	LENGTH v:	Change the length of the sliding window to v seconds.\n"
                + "	TO marker:	Define the first time marker in the header to store results. \n"
                + "	The time of the minimum is stored in this marker. The time of the maximum \n"
                + "	is stored in the next marker.\n"
                + "		marker:	T0|T1|T2|T3|T4|T5|T6|T7|T8|T9\n"
                + "DEFAULT VALUES\n"
                + "	MARKPTP LENGTH 5.0 TO T0\n"
                + "DESCRIPTION\n"
                + "	This command measures the times and the amplitude of the maximum peak-to-peak \n"
                + "	excursion of the data within the current measurement time window (see MTW.) \n"
                + "	The results are written into the header. The time of the minimum value (valley) \n"
                + "	is written into the requested marker. The time of the maximum value (peak) is \n"
                + "	written into the next marker. The peak-to-peak amplitude is written into USER0. \n"
                + "	The results are also written into the alphanumeric pick file if it is open (see OAFP.)\n"
                + "EXAMPLES\n"
                + "	To set the measurement time window to be between the two header fields, T4 and T5, and the default sliding window length and marker:\n"
                + "\n"
                + "		SAC> MTW T4 T5\n"
                + "		SAC> MARKPTP\n"
                + "	To set the measurement time window to be the 30 seconds immediately after the \n"
                + "	first arrival, and the sliding window length to to 3 seconds, and the starting marker to T7:\n"
                + "\n"
                + "		SAC> MTW A 0 30\n"
                + "		SAC> MARKP L 3. TO T7\n"
                + "HEADER CHANGES\n"
                + "	Tn, USER0, KTn, KUSER0";
    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = { "MARKPTP", "MARKP" };
        return new ArrayList<>(Arrays.asList(names));
    }

}
