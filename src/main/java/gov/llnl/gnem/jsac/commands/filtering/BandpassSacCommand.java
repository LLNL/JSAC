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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.oregondsp.signalProcessing.filter.iir.PassbandType;

import gov.llnl.gnem.jsac.SacDataModel;
import gov.llnl.gnem.jsac.commands.SacCommand;
import gov.llnl.gnem.jsac.commands.TokenListParser;

public class BandpassSacCommand extends IIRSacCommand implements SacCommand {

    @Override
    public void initialize(String[] tokens) {
        Map<String, List<Object>> parsedTokens = TokenListParser.parseTokens(descriptors, tokens);
        initialParsing(parsedTokens);

        Set<String> keys = new HashSet<>();
        for (String value : parsedTokens.keySet()) {
            keys.add(value.toUpperCase());
        }

        List<Object> parameters = maybeGetCorners(parsedTokens);
        if (parameters != null && parameters.size() == 2) {

            cutoff1 = (double) parameters.get(0);
            if (cutoff1 <= 0.0) {
                System.out.println("Corner frequency must be greater than 0.0, reset to " + NOMINAL_CUTOFF);
                cutoff1 = NOMINAL_CUTOFF;
            }
            cutoff2 = (double) parameters.get(1);
            if (cutoff2 <= 0.0) {
                cutoff2 = NOMINAL_HIGH_CUTOFF;
                System.out.println("Corner frequency must be greater than 0.0, reset to " + NOMINAL_HIGH_CUTOFF);
            }
        }

    }

    @Override
    public void execute() {
        SacDataModel.getInstance().applyFilter(design, order, PassbandType.BANDPASS, cutoff1, cutoff2, epsilon, transitionBW, twoPass);
    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = { "BP", "BANDPASS" };
        return new ArrayList<>(Arrays.asList(names));
    }

    @Override
    public String getHelpString() {
        return "SUMMARY\n"
                + "	Applies an IIR bandpass filter.\n"
                + "\n"
                + "SYNTAX\n"
                + "[B]AND[P]ASS {[BU]TTER|[BE]SSEL|C1|C2}\n"
                + "      {[C]ORNERS v1 v2}\n"
                + "      {[N]POLES n}\n"
                + "      {[P]ASSES n}\n"
                + "      {[T]RANBW v}\n"
                + "      {[A]TTEN v}\n"
                + "INPUT\n"
                + "	BUTTER:	Apply a Butterworth filter.\n"
                + "	C1:	Apply a Chebyshev Type I filter.\n"
                + "	C2:	Apply a Chebyshev Type II filter.\n"
                + "	CORNERS v1 v2:	Set corner frequencies to v1 and v2.\n"
                + "	NPOLES n:	Set number of poles {range: 1-10}.\n"
                + "	PASSES n:	Set number of passes {n=1: causal, n=2: zero-phase}.\n"
                + "	TRANBW v:	Set the Chebyshev transition bandwidth to v.\n"
                + "	ATTEN v:	Set the Chebyshev attenuation factor to v.\n"
                + "DEFAULT VALUES\n"
                + "     BANDPASS BUTTER CORNER 0.1 0.4 NPOLES 2 PASSES 1 TRANBW 0.3 ATTEN 30.";
    }

}
