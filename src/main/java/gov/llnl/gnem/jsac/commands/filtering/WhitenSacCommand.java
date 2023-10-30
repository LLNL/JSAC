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

public class WhitenSacCommand implements SacCommand {

    private static final int DEFAULT_ORDER = 6;

    private static final List<AttributeDescriptor> descriptors = new ArrayList<>();

    static {
        descriptors.add(new AttributeDescriptor("FILTERDESIGN", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("FD", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("COMMON", ValuePossibilities.NO_VALUE, null));
    }

    private static boolean common = false;
    private static boolean filterDesign = false;
    private static int order = DEFAULT_ORDER;

    public WhitenSacCommand() {

    }

    @Override
    public void execute() {
        SacDataModel.getInstance().whiten(common, filterDesign, order);
    }

    @Override
    public void initialize(String[] tokens) {

        Map<String, List<Object>> parsedTokens = TokenListParser.parseTokens(descriptors, tokens);

        if (parsedTokens.isEmpty()) {
            return;
        }

        if (parsedTokens.containsKey("COMMON")) {
            common = true;
        }

        if (parsedTokens.containsKey("FILTERDESIGN") || parsedTokens.containsKey("FD")) {
            filterDesign = true;
        }

        List<Object> parameters = parsedTokens.get(TokenListParser.LEFT_OVER_TOKENS);
        if (parameters != null && !parameters.isEmpty()) {
            order = Integer.parseInt((String) parameters.get(0));
        }

    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = { "WHITEN" };
        return new ArrayList<>(Arrays.asList(names));
    }

    @Override
    public String getHelpString() {

        return "SUMMARY\n"
                + "Flattens the spectrum of the time series in memory using a prediction error filter designed from the data in memory.\n"
                + "This implementation allows the filter coefficients to be saved in a SAC file on disk.\n"
                + "\n"
                + "SYNTAX\n"
                + "\n"
                + "WHITEN {N} {FILTERDESIGN|FD} {COMMON}\n"
                + "\n"
                + "INPUT\n"
                + " N:                 Order of the filter (number of zeros in the predictor's transfer function).\n"
                + " [F]ILTER[D]ESIGN:  Performs something akin to the filterdesign command.  Computes the impulse response, \n"
                + "                    frequency response and group delay of the prediction error filter and writes these to files\n"
                + "                    on disk in the current working directory with suffixes '.imp', '.spec' and '.gd' respectively.\n"
                + "	COMMON:    	       Uses a common prediction error filter for all sequences in memory.  This option is provided for\n"
                + "                    ambient noise Greens function estimation applications.\n"
                + "\n"
                + "DEFAULT VALUES\n"
                + "\n"
                + " WHITEN 6\n"
                + "\n"
                + "DESCRIPTION\n"
                + " Flattens the spectrum of the time series in memory with a prediction error filter of the specified order (default 6).\n"
                + " It estimates the coefficients of the prediction error filter from the autocorrelation function of the data, either\n"
                + " trace by trace (default) or for all traces simultaneously (COMMON option), then applies the resulting prediction error\n"
                + " filters to the data.  Prewhitening the data before spectral estimation or correlation reduces bias caused by spectral\n"
                + " leakage (wherein energy from large peaks of the spectrum bleed into surrounding frequency bands) caused by finite\n"
                + " windowing effects (see Jenkins and Watts, Spectral Analysis and Its Applications).  Prewhitening reduces the \n"
                + " dynamic range of the spectrum (which reduces leakage).  When prewhitening for spectral estimation, remember to divide\n"
                + " the spectral estimate by the squared magnitude of the frequency response of the prediction error filter.\n\n"
                + " Note that the first N values of the trace will be lost in this translation and the time markers updated accordingly."
                + "\n"
                + "HEADER CHANGES\n"
                + "\n"
                + "DEPMIN, DEPMAX, DEPMEN, NPTS\n";
    }

}
