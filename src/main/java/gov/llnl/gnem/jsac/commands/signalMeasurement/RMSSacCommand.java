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
import gov.llnl.gnem.jsac.commands.dataFile.PDWSetterBase;
import gov.llnl.gnem.jsac.dataAccess.dataObjects.SacTraceData;
import gov.llnl.gnem.jsac.util.PartialDataWindow;

public class RMSSacCommand implements SacCommand {

    private static final Logger log = LoggerFactory.getLogger(RMSSacCommand.class);

    private static String toMarker = "USER0";
    private static int index = 0;
    private static boolean useNoise = false;
    private static final PartialDataWindow pdw = new PartialDataWindow();

    private static final List<AttributeDescriptor> descriptors = new ArrayList<>();

    static {
        descriptors.add(new AttributeDescriptor("TO", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("NOISE", ValuePossibilities.ONE_OR_MORE, String.class));
    }

    public RMSSacCommand() {

    }

    @Override
    public void initialize(String[] tokens) {

        Map<String, List<Object>> parsedTokens = TokenListParser.parseTokens(descriptors, tokens);

        List<Object> values = parsedTokens.remove("TO");
        if (values != null && values.size() == 1) {
            String tmp = (String) values.get(0);
            toMarker = tmp.toUpperCase();
            if (toMarker.indexOf("USER") == 0 && toMarker.length() == 5) {
                {
                    index = Integer.parseInt(toMarker.substring(4));
                }
            }
        }

        values = parsedTokens.remove("NOISE");
        if (values != null && !values.isEmpty()) {
            String tmp = (String) values.get(0);
            if (tmp.equalsIgnoreCase("ON")) {
                useNoise = true;
                values.remove(0);
            } else if (tmp.equalsIgnoreCase("OFF")) {
                useNoise = false;
                values.remove(0);
            }
            if (values.size() >= 2) { // Window specification follows...
                String[] t = new String[values.size() + 1];
                t[0] = "BogusCommandName"; // PDWSetterBase expects first token to be command name
                for (int j = 0; j < values.size(); ++j) {
                    Object obj = values.get(j);
                    t[j + 1] = (String) obj;
                }
                PDWSetterBase pdws = new PDWSetterBase("RMS", pdw);
                pdws.initialize(t);
                useNoise = true;
            }
        }
    }

    @Override
    public void execute() {
        PartialDataWindow signalPdw = MTWSacCommand.getPartialDataWindow();
        if (!signalPdw.isValid()) {
            String msg = String.format("No valid signal window set using MTW!");
            log.warn(msg);
            return;
        }

        if (useNoise && !pdw.isValid()) {
            String msg = String.format("No valid noise window set!");
            log.warn(msg);
            return;
        }

        List<SacTraceData> data = SacDataModel.getInstance().getData();
        for (SacTraceData std : data) {
            if (std.isSpectral()) {
                String msg = String.format("ERROR 1307: Illegal operation on spectral file");
                log.warn(msg);
                continue;
            }
            float[] signal = std.getPartialDataWindow(signalPdw);
            if (signal == null || signal.length < 2) {
                String msg = String.format("Could not produce an float[] for PDW (%s) and file (%s)!", signalPdw.toString(), std.getFilename().toString());
                log.warn(msg);
                continue;
            }
            double sumSquares = 0;
            double sumSignal = 0;
            for (float element : signal) {
                sumSignal += (element * element);
            }
            sumSignal /= signal.length;

            if (useNoise) {
                float[] noise = std.getPartialDataWindow(pdw);
                double sumNoise = 0;
                for (float element : noise) {
                    sumNoise += (element * element);
                }
                sumNoise /= noise.length;
                sumSquares = sumSignal - sumNoise;
            } else {
                sumSquares = sumSignal;
            }
            double rms = Math.sqrt(sumSquares);
            std.getSACHeader().setUser(index, rms);
        }
    }

    @Override
    public String getHelpString() {
        return "SUMMARY\n"
                + "	Computes the root mean square of the data within the measurement time window.\n"
                + "\n"
                + "SYNTAX\n"
                + "	RMS {NOISE ON|OFF|pdw},{TO USERn}\n"
                + "INPUT\n"
                + "	NOISE ON:	Turn noise normalization option on.\n"
                + "	NOISE OFF:	Turn noise normalization option off.\n"
                + "	NOISE pdw:	Turn noise normalization option on and change noise 'partial data window.' A pdw consists of a starting and a stopping value of the independent variable, usually time, which defines the desired window of data that you wish to make measurements on. See the CUT command for a complete explanation of how to define and use a pdw. Some examples are given below.\n"
                + "	TO USERn:	Define the user header variable in which to store the result. n is an integer in the range 0 to 9.\n"
                + "DEFAULT VALUES\n"
                + "	RMS NOISE OFF TO USER0\n"
                + "DESCRIPTION\n"
                + "	This command computes the root mean square of the data within the current measurement time window (see MTW.) The result is written into one of the floating point user header variables. The result may be corrected for noise if desired by defining a noise window. The general form of the calculation is: where the first summation is over the signal window and the second is over the optional noise window.\n"
                + "EXAMPLES\n"
                + "	To compute the uncorrected root mean square of data between the two header fields, T1 and T2, and to store the result into the USER4 header field:\n"
                + "\n"
                + "		SAC> MTW T1 T2\n"
                + "		SAC> RMS TO USER4\n"
                + "	To compute the corrected root mean square using a noise window 5 seconds long ending at the header field T3:\n"
                + "\n"
                + "		SAC> MTW T1 T2\n"
                + "		SAC> RMS NOISE T3 -5.0 0.0\n"
                + "HEADER CHANGES\n"
                + "	USERn";
    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = { "RMS" };
        return new ArrayList<>(Arrays.asList(names));
    }

}
