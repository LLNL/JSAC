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

package gov.llnl.gnem.jsac.commands.spectralAnalysis;

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
public class HilbertSacCommand implements SacCommand {

    private static final List<AttributeDescriptor> descriptors = new ArrayList<>();

    static {
        descriptors.add(new AttributeDescriptor("SPECTRAL", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("SP", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("FIR", ValuePossibilities.TWO_VALUES, Double.class));
    }

    static HilbertTransformerType type = HilbertTransformerType.SPECTRAL;

    @Override
    public void initialize(String[] tokens) {

        Map<String, List<Object>> parsedTokens = TokenListParser.parseTokens(descriptors, tokens);

        if (parsedTokens.containsKey("SPECTRAL") || parsedTokens.containsKey("SP")) {
            type = HilbertTransformerType.SPECTRAL;
        } else if (parsedTokens.containsKey("FIR")) {
            type = HilbertTransformerType.FIR;
        } else {
            type = HilbertTransformerType.SPECTRAL;
        }

    }

    @Override
    public void execute() {
        SacDataModel.getInstance().applyHilbert(type);
    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = { "HILBERT" };
        return new ArrayList<>(Arrays.asList(names));
    }

    @Override
    public String getHelpString() {
        return "SUMMARY\n"
                + "	Applies a Hilbert transform to each data file in memory.\n"
                + "\n"
                + "SYNTAX\n"
                + "\n"
                + "	HILBERT { [SP]ECTRAL | FIR }\n"
                + "\n"
                + "INPUT\n"
                + "\n"
                + "[SP]ECTRAL:    Use the spectral Hilbert transform method.\n"
                + "FIR:           Apply a Hilbert transform by convolution with an FIR operator.\n"
                + "\n"
                + "DEFAULT VALUES\n"
                + "\n"
                + "HILBERT SPECTRAL\n"
                + "\n"
                + "DESCRIPTION\n"
                + "	Each trace in the data file list is replaced by its Hilbert transform, x(n).  Two transform\n"
                + " implementations are available.  The first is the spectral method, which computes an FFT of \n"
                + " the data, multiplies the transform by the (sampled) theoretical Hilbert response, then computes\n"
                + " an inverse FFT to obtain a time-domain result.  The second is a time domain method which convolves\n"
                + " the trace data with a 201-point FIR filter designed by the Parks-McClellan algorithm to closely \n"
                + " approximate the Hilbert transform in the frequency domain.\n"
                + "\n"
                + "  Both methods have their plusses and minuses.  The spectral method is closer to the nominal response\n"
                + " around 0 Hz and the folding frequency, but its implied response near 0 Hz and the folding frequency\n"
                + " rings due to the Gibbs phenomenon.  This non-intuitive characteristic of the method occurs due to \n"
                + " the fact that the method operates on a sampled version of the data spectrum rather than the continuous\n"
                + " spectrum.  The method is exact on the sampled spectrum but not on the continuous spectrum.  By contrast\n"
                + " the FIR method is not exact on the sampled spectrum (deviating significantly in the lowest 2.5% and \n"
                + " highest 2.5% of the discrete-time spectrum).  But its continuous spectrum is well-behaved, without\n"
                + " overshoot.  If the loss of the lowest 2.5% of the spectrum is a problem, one can decimate the data to\n"
                + " move its spectrum higher into the mid-band where the FIR operator is highly accurate.";

    }

}
