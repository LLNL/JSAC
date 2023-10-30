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

package gov.llnl.gnem.jsac.commands.signalCorrection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.llnl.gnem.jsac.SacDataModel;
import gov.llnl.gnem.jsac.commands.SacCommand;

/**
 * @author dodge1
 */
public class InterpolateSacCommand implements SacCommand {

    private static final Logger log = LoggerFactory.getLogger(InterpolateSacCommand.class);

    private static Double DELTA = null;
    private static Integer NPTS = null;

    private List<String> mytokens;

    @Override
    public void initialize(String[] tokens) {
        mytokens = new ArrayList<>();
        mytokens.addAll(Arrays.asList(tokens));
        mytokens.remove(0); // Don't need the command name anymore.
        parseConstants();
    }

    @Override
    public void execute() {
        if (DELTA != null || NPTS != null) {
            SacDataModel.getInstance().interpolate(DELTA, NPTS);
        } else {
            log.warn("Neither DELTA nor NPTS defined. Interpolation not performed.");
        }
    }

    private void parseConstants() {
        while (mytokens.size() > 1) {
            String token = mytokens.remove(0);
            if (token.toUpperCase().equals("DELTA")) {
                token = mytokens.remove(0);
                DELTA = Double.valueOf(token);
                NPTS = null;
            } else if (token.toUpperCase().equals("NPTS")) {
                token = mytokens.remove(0);
                NPTS = Integer.valueOf(token);
                DELTA = null;
            }
        }

    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = { "INTERP", "INTERPOLATE" };
        return new ArrayList<>(Arrays.asList(names));
    }

    @Override
    public String getHelpString() {
        return "SUMMARY\n"
                + "	Interpolates evenly-spaced data to a new sampling rate. Interpolate can also be used with unevenly-spaced data.\n"
                + " If DELTA and NPTS are both specified DELTA takes priority and NPTS will be ignored.\n"
                + "\n"
                + "SYNTAX\n"
                + "	INTERPOLATE {DELTA v} {NPTS n} {BEGIN v}\n"
                + "INPUT\n"
                + "	DELTA v:	Set new sampling rate to v. The time range (E-B) is not changed, so NPTS is changed. \n"
                + "				However, E will be changed so that it is a multiple of DELTA from b.\n"
                + "	NPTS n:		Force the number of points in interpolated file to be n. The time range (E-B) is not changed, \n"
                + "				so DELTA is changed. \n"
                + "	BEGIN v:	Start interpolation at v. This value becomes the begin time of the interpolated file. BEGIN can be used with either DELTA or NPTS.\n"
                + "DEFAULT VALUES\n"
                + "	The time series is unchanged.";
    }
}
