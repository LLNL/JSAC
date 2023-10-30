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

import gov.llnl.gnem.jsac.SacDataModel;
import gov.llnl.gnem.jsac.commands.SacCommand;

/**
 *
 * @author dodge1
 */
public class EnvelopeSacCommand implements SacCommand {

    @Override
    public void initialize(String[] tokens) {
    }

    @Override
    public void execute() {
        SacDataModel.getInstance().computeEnvelope();
    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = {"ENVELOPE"};
        return new ArrayList<>(Arrays.asList(names));
    }

    @Override
    public String getHelpString() {
        return "SUMMARY\n"
                + "	Computes the envelope function using a Hilbert transform.\n"
                + "\n"
                + "SYNTAX\n"
                + "	ENVELOPE\n"
                + "DESCRIPTION\n"
                + "	This command computes the envelope function of the data in memory. \n"
                + "	The envelope is defined by the square root of x(n)^2 + y(n)^2, \n"
                + "	where x(n) is the original signal and y(n) its Hilbert transform \n"
                + "	(see HILBERT). As with HILBERT, very long period data should be \n"
                + "	decimated (see DECIMATE) prior to processing.";
    }
}
