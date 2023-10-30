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

public class ConvolveSacCommand implements SacCommand {

    private static final float EPS = 1.0e-06f;
    private static final List<AttributeDescriptor> descriptors = new ArrayList<>();

    static {
        descriptors.add(new AttributeDescriptor("PULSE", ValuePossibilities.ONE_VALUE, Integer.class));
        descriptors.add(new AttributeDescriptor("TRI", ValuePossibilities.ONE_VALUE, Double.class));
        descriptors.add(new AttributeDescriptor("TRIANGLE", ValuePossibilities.ONE_VALUE, Double.class));
        descriptors.add(new AttributeDescriptor("GAUSS", ValuePossibilities.ONE_VALUE, Double.class));
        descriptors.add(new AttributeDescriptor("CENTERED", ValuePossibilities.ONE_VALUE, String.class));
    }

    private static int pulseIndex = 1;
    private static String filePath = null;
    private static double v = -1.0;
    private static boolean centered = false;
    private static float[] template = null;
    private static List<SacTraceData> traceData = null;
    private static double delta;

    public ConvolveSacCommand() {

    }

    @Override
    public void initialize(String[] tokens) {

        Map<String, List<Object>> parsedTokens = TokenListParser.parseTokens(descriptors, tokens);
        if (parsedTokens.isEmpty()) {
            throw new IllegalArgumentException("No arguments passed to convolve function. Convolve has mandatory arguments (see HELP CONVOLVE).");
        }

        traceData = SacDataModel.getInstance().getData();

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

        if (parsedTokens.containsKey("PULSE")) {
            List<Object> parameters = parsedTokens.get("PULSE");
            if (parameters == null || parameters.size() <= 0) {
                throw new IllegalArgumentException("PULSE was specified but missing required arguments.");
            }
            pulseIndex = (int) parameters.get(0);
            template = traceData.get(pulseIndex - 1).getData();
        } else if (parsedTokens.containsKey("TRI") || parsedTokens.containsKey("TRIANGLE")) {
            List<Object> parameters = parsedTokens.get("TRI");
            if (parameters == null) {
                parameters = parsedTokens.get("TRIANGLE");
            }
            if (parameters == null || parameters.size() <= 0) {
                throw new IllegalArgumentException("[TRI]ANGLE was specified but missing required arguments.");
            }
            v = (double) parameters.get(0);

            int n = (int) Math.round(v / delta);
            template = new float[2 * n + 1];
            template[n] = 1.0f;
            float sum = 1.0f;
            for (int i = 1; i <= n; i++) {
                template[n + i] = 1.0f - (float) i / (float) n;
                template[n - i] = template[n + i];
                sum += 2 * template[n + i];
            }
            for (int i = 0; i < template.length; i++) {
                template[i] /= sum;
            }

        } else if (parsedTokens.containsKey("GAUSS")) {
            List<Object> parameters = parsedTokens.get("GAUSS");
            if (parameters == null || parameters.size() <= 0) {
                throw new IllegalArgumentException("GAUSS was specified but missing required arguments.");
            }
            v = (double) parameters.get(0);

            int n = 6 * (int) Math.round(v / delta);
            template = new float[2 * n + 1];
            template[n] = 1.0f;
            float sum = 1.0f;
            for (int i = 1; i <= n; i++) {
                float x = (float) (i * delta);
                template[n + i] = (float) Math.exp(-0.5 * x * x / (v * v));
                template[n - i] = template[n + i];
                sum += 2 * template[n + i];
            }
            for (int i = 0; i < template.length; i++) {
                template[i] /= sum;
            }
        }

        if (parsedTokens.containsKey("CENTERED")) {
            List<Object> parameters = parsedTokens.get("CENTERED");
            if (parameters == null || parameters.size() <= 0) {
                throw new IllegalArgumentException("CENTERED was specified but missing required arguments.");
            }
            String offon = (String) parameters.get(0);
            if (offon.toUpperCase().equals("ON")) {
                centered = true;
            }
        }

        if (parsedTokens.containsKey(TokenListParser.LEFT_OVER_TOKENS)) {
            List<Object> parameters = parsedTokens.get(TokenListParser.LEFT_OVER_TOKENS);
            filePath = (String) parameters.get(0);

            try {
                SACFileReader reader = SACFileReader.fromStringPath(filePath);
                SACHeader header = reader.getHeader();

                if (Math.abs(header.getDelta() - delta) > EPS * delta) {
                    throw new IllegalStateException("Template sampling interval does not match data sampling interval: " + header.getDelta());
                }

                template = new float[header.getNpts()];
                reader.read(template);
                reader.close();
            } catch (IOException e) {
                throw new IllegalArgumentException("File access exception attempting to open template file " + filePath + ". Error msg: " + e.getMessage());
            }
        }

        //If template is still null by now we either got an unknown type of
        // template or couldn't read it so we should throw an error.
        if (template == null) {
            throw new IllegalArgumentException("Unable to parse a template function for arguments [" + tokens + "]");
        }
    }

    @Override
    public void execute() {
        SacDataModel.getInstance().convolve(template, centered);
    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = { "CONVOLVE" };
        return new ArrayList<>(Arrays.asList(names));
    }

    @Override
    public String getHelpString() {
        return "SUMMARY\n"
                + "     Compute the convolution of a pulse shape with all other time series in memory.\n"
                + "\n"
                + "SYNTAX\n"
                + "\n"
                + "         CONVOLVE {file} {PULSE n} {TRI v} {GAUSS v} \n"
                + "\n"
                + "INPUT\n"
                + "\n"
                + "PULSE n:        The PULSE in filenumber n in memory from 1-N.\n"
                + "file:           Convolve with data in file (not in memory)\n"
                + "[TRI]ANGLE v:          Convolve with a triangle of half width v seconds\n"
                + "GAUSS v:        Convolve with a Gaussian with standard deviation v seconds\n"
                + "CENTERED ON|OFF Place zero time on center [ON} or beginning [OFF] of the pulse\n"
                + "\n"
                + "CONVOLVE PULSE 1 CENTERED OFF\n"
                + "\n"
                + "DESCRIPTION\n"
                + "\n"
                + "In seismology, convolution is used in two ways:\n"
                + "\n"
                + "  1. convolving a pulse with a time-series, and\n"
                + "  2. convolving a resonse with a time series. (Or, more typically, deconvolving a response from a time series.)\n"
                + "\n"
                + "The equation for convolution takes a simple form in both the time and frequency domains: (1) is more easily described \n"
                + "in the time domain, while (2) in the frequency domain.  The CONVOLVE function here is oriented towards (1).  The\n"
                + "TRANSFER command is more appropriate for (2).\n"
                + "\n"
                + "There are three ways to run CONVOLVE: (1) PULSE: read in N files and convolve one of them with all the others; (2) file:\n"
                + "read in N files and convolve all of the waveforms with a waveform in a separate file; (3) using a PREDEFINED FUNCTION (see below).\n"
                + "Prior to SAC v102.0, the PULSE method was the only option, so we start there.\n"
                + "\n"
                + "The discrete-time waveform specified by PULSE is convolved with all other time-series in memory using the following equation:\n"
                + "\n"
                + "conv( g, f ) = SUM_m ( g[m] f[n-m] )\n"
                + "\n"
                + "where f is the pulse waveform, g is any from among the other signals in memory and SUM_m refers to summation over the discrete\n"
                + "time index m.  All signals in memory ust have the same DELTA.  The number of points in f, the PULSE waveform, cannot be greater\n"
                + "than the number of points in any other signal in memory.  If the numbers of points in f and g are nf and ng, then the number\n"
                + "of points in the resulting convolved waveform is nf + ng - 1.  The start time for each convolved waveform will be the same as\n"
                + "B(g).  Contering on the pulse waveform eliminates time shifts introduced by convolution.  If CENTERED is OFF, B(f) = 0.0. \n"
                + "CENTERED ON should be used only for time-symmetric pulses.  As an example, let synthetic.sac contain a synthetic time series\n"
                + "created by a program such as WKBJ, consisting of spikes with time offsets, amplitudes and polarities representing phase\n"
                + "arrivals.  To compare the synthetics with an observed seismogram, one could create a file p_arrival.sac containing a pulse \n"
                + "waveform and perform the convolution with the commands:\n"
                + "\n"
                + "SAC> r p_arrival.sac synthetic.sac"
                + "SAC> convolve PULSE 1 CENTERED OFF\n"
                + "\n"
                + "FILE OPTION\n"
                + "\n"
                + "Alternatively, given the same two files, one could obtain the same convolution result with the commands:\n"
                + "\n"
                + "SAC> r synthetic.sac\n"
                + "SAC> convolve p_arrival.sac CENTER OFF\n"
                + "\n"
                + "PREDEFINED FUNCTIONS\n"
                + "\n"
                + "JSAC also supports the option of convolution with one of two predefined waveforms, using the TRI and GAUSS options.  These\n"
                + "options automatically compute triangular and Gaussian functions with time samplings consistent with the waveforms in memory.\n"
                + "When the TRI or GAUSS options are used, no pulse waveform file is read in;  the predefined function takes the place of f[n]\n"
                + "in the convolution equation cited above.  Both functions are normalized such that the integrate to 1.0 and, thus, do\n"
                + "not change amplitude or moment in the convolved result.  Both functions are symmetric.  For the GAUSS option, the sampled\n"
                + "time history is e^(-0.5*(n*DELTA/v)^2), so that v is the standard deviation.\n"
                + "\n"
                + "Say one wants to look at a synthetic waveform in synthetic.sac simply as a spike series (no source pulse).  Most synthetics\n"
                + "suffer from the Gibbs phenomenon and will exhibit a train of oscillating pulses.  Convolving the synthetic with one of these\n"
                + "predefined functions will smooth out the oscillating pulses, resulting in a cleaner seismogram that is easier to interpret:\n"
                + "\n"
                + "SAC> r synthetic.sac\n"
                + "SAC> convolve GAUSS 0.04 CENTERED ON\n"
                + "or\n"
                + "SAC> r synthetic.sac\nn"
                + "SAC> convolve TRI 0.04 CENTERED ON\n"
                + "\n"
                + "HEADER CHANGES\n"
                + "\n"
                + "DEPMIN, DEPMAX, DEPMEN, NPTS\n";

    }

}
