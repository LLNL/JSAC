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
package gov.llnl.gnem.jsac.commands.filtering;

import java.io.IOException;
import java.nio.file.Path;
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
import gov.llnl.gnem.jsac.dataAccess.dataObjects.SacTraceData;
import gov.llnl.gnem.jsac.io.SACFileReader;
import gov.llnl.gnem.jsac.io.SACHeader;
import gov.llnl.gnem.jsac.util.PathManager;

public class CorrelateSacCommand implements SacCommand {

    private static final List<AttributeDescriptor> descriptors = new ArrayList<>();

    static {
        descriptors.add(new AttributeDescriptor("MASTER", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("NUMBER", ValuePossibilities.ONE_VALUE, Integer.class));
        descriptors.add(new AttributeDescriptor("FILE", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("NORMALIZED", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("LENGTH", ValuePossibilities.ONE_VALUE, String.class));
    }

    private static final float EPS = 1.0e-5f;

    private static boolean normalize = false;
    private static int numberOfWindows = 1;
    private static double windowLength = -1.0;
    private static boolean useSubwindows = false;
    private static float[] master;
    private final List<SacTraceData> traceData;

    private double delta;

    public CorrelateSacCommand() {

        traceData = new ArrayList<>();
    }

    private void populateTraceData() throws IllegalStateException {
        traceData.clear();
        traceData.addAll(SacDataModel.getInstance().getData());

        // check consistency of sampling intervals
        boolean first = true;

        delta = -1.0;

        for (SacTraceData std : traceData) {
            if (first) {
                delta = std.getDelta();
                first = false;
            } else if ((std.getDelta() - delta) > EPS * delta) {
                throw new IllegalStateException("A data file has an inconsistent sampling interval, delta: " + std.getDelta());
            }
        }
    }

    @Override
    public void initialize(String[] tokens) {
        populateTraceData();

        Map<String, List<Object>> parsedTokens = TokenListParser.parseTokens(descriptors, tokens);
        if (parsedTokens.isEmpty()) {
            return;
        }

        if (parsedTokens.containsKey("MASTER")) {

            List<Object> parameters = parsedTokens.get("MASTER");
            String token = (String) parameters.get(0);

            boolean masterFound = false;

            try {
                int filePtr = Integer.parseInt(token);
                master = traceData.get(filePtr).getData();
                masterFound = true;
            } catch (NumberFormatException nfe) {

                for (SacTraceData std : traceData) {
                    if (std.getFilename().endsWith(token)) {
                        master = std.getData();
                        masterFound = true;
                    }
                }

            }

            if (!masterFound) {
                throw new IllegalStateException("MASTER file not found");
            }

        } else if (parsedTokens.containsKey("FILE")) {

            List<Object> parameters = parsedTokens.get("FILE");
            Path filePath = PathManager.getInstance().resolvePath((String) parameters.get(0));

            try {

                SACFileReader reader = new SACFileReader(filePath);
                SACHeader header = reader.getHeader();

                if (Math.abs(header.getDelta() - delta) > EPS * delta) {
                    throw new IllegalStateException("File sampling interval does not match data sampling interval: " + header.getDelta());
                }

                master = new float[header.getNpts()];
                reader.read(master);
                reader.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        if (parsedTokens.containsKey("NORMALIZED")) {

            List<Object> parameters = parsedTokens.get("FILE");
            String token = (String) parameters.get(0);

            if (token.equals("ON")) {
                normalize = true;
            } else if (token.equals("OFF")) {
                normalize = false;
            } else {
                normalize = false;
            }
        }

        if (parsedTokens.containsKey("NUMBER")) {
            List<Object> parameters = parsedTokens.get("NUMBER");
            numberOfWindows = (int) parameters.get(0);
        }

        if (parsedTokens.containsKey("LENGTH")) {

            List<Object> parameters = parsedTokens.get("LENGTH");
            String token = (String) parameters.get(0);
            try {

                windowLength = Double.parseDouble(token);
                useSubwindows = true;

            } catch (NumberFormatException nfe) {

                if (token.equals("ON")) {
                    useSubwindows = true;
                    if (windowLength == -1.0) {
                        throw new IllegalStateException("No prior window length, use LENGTH v");
                    }
                } else if (token.equals("OFF")) {
                    useSubwindows = false;
                }

            }

        }

    }

    @Override
    public void execute() {

        int windowLengthInSamples = (int) Math.round(windowLength / delta);

        SacDataModel.getInstance().correlate(master, normalize, useSubwindows, windowLengthInSamples, numberOfWindows);

    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = { "CORRELATE" };
        return new ArrayList<>(Arrays.asList(names));
    }

    @Override
    public String getHelpString() {
        return "SUMMARY\n"
                + " Computes auto- and cross-correlation functions for three use cases: examining waveform similarity, detection of recurrent\n"
                + "   signals, and Green's function extraction from ambient noise.\n"
                + "\n"
                + "SYNTAX\n"
                + "\n"
                + "	CORRELATE {MASTER name|n}, {FILE path}, {NUMBER n}, {NORMALIZED ON|OFF}, {LENGTH ON|OFF|v}\n"
                //                + "   {TYPE RECTANGLE|HAMMING|HANNING|COSINE|TRIANGLE\n"
                + "\n"
                + "INPUT\n"
                + "\n"
                + "MASTER name|n:  Select master waveform in data file list by name or number.  All waveforms are correlated against this one.\n"
                + "FILE   path:    Select master waveform from a file on disk.\n"
                + "NUMBER n:       Set number of correlation windows.\n"
                + "NORMALIZED OFF: No normalization.\n"
                + "NORMALIZED ON:  Results are normalized between -1.0 and 1.0\n"
                + "LENGTH ON:      Turn fixed window length option on.\n"
                + "LENGTH OFF:     Turn fixed window length option off.\n"
                + "LENGTH v:       Turn fixed window length option on and change window length in seconds to v.\n"
                + "\n"
                + "DEFAULT VALUES\n"
                + "\n"
                + "CORRELATE MASTER 1 NORMALIZED OFF NUMBER 1 LENGTH OFF\n"
                + "\n"
                + "DESCRIPTION\n"
                + "\n\n"
                + "The JSAC implementation of this command is intended to support three principal use cases:\n"
                + "  (1) checking to see whether two signals (e.g. two relatively short transients) are closely related,\n"
                + "  (2) computing a trial stacked correlation on relatively long samples of original or prewhitened ambient noise to test the \n"
                + "      emergence of Green's functions, and\n"
                + "  (3) correlating a short template (e.g. an observation of a prior nuclear test) against a long stretch of data from the same\n"
                + "      station to look for new occurrences of the same waveform (i.e. a detection use).\n"
                + "\n"
                + "In the first case one might read in the two transients to be compared, then correlate with NORMALIZED on to get an assessment\n"
                + "of waveform similarity:\n"
                + "\n"
                + "SAC> r a.sac b.sac\n"
                + "SAC> correlate MASTER 1 NORMALIZED ON NUMBER 1 LENGTH OFF\n"
                + "SAC> plot\n"
                + "\n"
                + "This sequence of commands will result in an autocorrelation in the first trace in memory with a peak value of 1.0 and a\n"
                + "cross-correlation in the second trace with a peak value ranging between -1.0 and 1.0, depending on the degree of similarity\n"
                + "between the waveforms in a.sac and b.sac.\n"
                + "\n"
                + "In the second (ambient noise) case, one might read in long samples (e.g. a day) of clean background noise from two separate \n"
                + "stations, then correlate with LENGTH v and NUMBER n to stack auto- and cross-correlations of n windows of length v seconds.  A\n"
                + "processing sequence might look like:\n"
                + "\n"
                + "SAC> r bdm.bhz wenl.bhz mtos.bhz\n"
                + "SAC> CORRELATE MASTER 2 LENGTH 100.0 NUMBER 1001\n"
                + "SAC> plot\n"
                + "\n"
                + "This sequence of commands will result in stacked cross-correlations between 1001 100-second windows of data from station WENL \n"
                + "and similar windows of data from stations BDM and MTOS in the first and third traces in memory.  The second trace in memory will\n"
                + "contain the stacked autocorrelation of 1000 windows of WENL data.  Assuming the sac files contained 86,400 seconds of data (one day)\n"
                + "the 1001 windows of length 100 seconds are overlapped by (100,000-86,400)/1000 = 13.6 seconds.\n"
                + "\n"
                + "In the third (exploratory detection) case, one might read in a long stretch of data from a station, then correlate it against a\n"
                + "short template waveform from the same station contained in a separate SAC file, to search for more occurrences of the template\n"
                + "waveform.  For example:\n"
                + "\n"
                + "SAC> r BK.BKS.BHZ\n"
                + "SAC> CORRELATE FILE BKS_BHZ_template.sac LENGTH OFF\n"
                + "SAC> plot"
                + "\n"
                + "This sequence of commands will result in a single waveform in memory, consisting of the cross-correlation between the template\n"
                + "waveform in BKS_BHZ_template.sac (perhaps a ten-second streak repeater template) and the long record (for example, a day) in \n"
                + "BK.BKS.BHZ.  In this instance, the CORRELATE command will use the overlap-add method to compute the cross-correlation function.\n"
                + "\n"
                + "In all three cases, the correlations (whether auto- or cross-) are computed as:\n"
                + "corr( f, g, n ) = SUM_m ( f[m] g[m-n] )\n"
                + "where f is the master waveform and g is any waveform among the signals in memory.\n"
                + "\n"
                + "If nf is the number of samples in f (or subwindows of f) and ng is the number of samples in g (or its subwindows), then the \n"
                + "correlation has a total of nf+ng-1 samples.\n"
                + "\n"
                + "HEADER CHANGES\n"
                + "\n"
                + "DEPMIN, DEPMAX, DEPMEN, NPTS, B\n";

    }

}
