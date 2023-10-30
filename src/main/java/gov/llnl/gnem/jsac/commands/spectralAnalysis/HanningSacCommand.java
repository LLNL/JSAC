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
import llnl.gnem.dftt.core.util.TaperType;

/**
 *
 * @author dodge1
 */
public class HanningSacCommand implements SacCommand {

    private final TaperType taperType = TaperType.Hanning;
    private final double taperPercent = 50.0;

    @Override
    public void initialize(String[] tokens) {
    }

    @Override
    public void execute() {
        SacDataModel.getInstance().applyTaper(taperType, taperPercent);
    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = { "HAN", "HANNING" };
        return new ArrayList<>(Arrays.asList(names));
    }

    @Override
    public String getHelpString() {
        return "SUMMARY\n"
                + "	Applies a \"hanning\" window to each data file.\n"
                + "\n"
                + "SYNTAX\n"
                + "	HANNING\n"
                + "DESCRIPTION\n"
                + "	The \"hanning\" window is a recursive smoothing algorithm defined at each interior data point, j, as:\n"
                + "		Y(j) = 0.5 * [1 - cos(2πj/N)]";
    }
}
