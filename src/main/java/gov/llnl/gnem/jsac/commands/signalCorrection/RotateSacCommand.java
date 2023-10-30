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
import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.llnl.gnem.jsac.SacDataModel;
import gov.llnl.gnem.jsac.commands.AttributeDescriptor;
import gov.llnl.gnem.jsac.commands.SacCommand;
import gov.llnl.gnem.jsac.commands.TokenListParser;
import gov.llnl.gnem.jsac.commands.ValuePossibilities;
import gov.llnl.gnem.jsac.dataAccess.dataObjects.SacTraceData;
import gov.llnl.gnem.jsac.util.TraceRotator;
import gov.llnl.gnem.jsac.util.TraceRotator.RotationStyle;
import gov.llnl.gnem.jsac.util.TraceRotator.RotationType;

/**
 *
 * @author dodge1
 */
public class RotateSacCommand implements SacCommand {

    private static final Logger log = LoggerFactory.getLogger(RotateSacCommand.class);

    private static RotationStyle rotationStyle = RotationStyle.NORMAL;
    private static RotationType rotationType = RotationType.TO_GCP;
    private double rotationAngle = 0.0;

    private static final List<AttributeDescriptor> descriptors = new ArrayList<>();

    static {
        descriptors.add(new AttributeDescriptor("TO", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("THROUGH", ValuePossibilities.ONE_VALUE, Double.class));
        descriptors.add(new AttributeDescriptor("BY", ValuePossibilities.ONE_VALUE, Double.class));
        descriptors.add(new AttributeDescriptor("NORMAL", ValuePossibilities.NO_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("REVERSED", ValuePossibilities.NO_VALUE, String.class));
    }

    public RotateSacCommand() {

    }

    @Override
    public void initialize(String[] tokens) {
        Map<String, List<Object>> parsedTokens = TokenListParser.parseTokens(descriptors, tokens, true);
        List<Object> tmp = parsedTokens.remove("TO");
        if (tmp != null && tmp.size() == 1) {
            String value = (String) tmp.get(0);
            if (value.equalsIgnoreCase("GCP")) {
                rotationType = RotationType.TO_GCP;
            } else if (NumberUtils.isParsable(value)) {
                rotationAngle = Double.parseDouble(value);
                rotationType = RotationType.TO_VALUE;
            }
        }

        tmp = parsedTokens.remove("THROUGH");
        if (tmp == null) {
            tmp = parsedTokens.remove("BY");
        }
        if (tmp != null && tmp.size() == 1) {
            rotationAngle = (Double) tmp.get(0);
            rotationType = RotationType.THROUGH_VALUE;
        }

        tmp = parsedTokens.remove("NORMAL");
        if (tmp != null) {
            rotationStyle = RotationStyle.NORMAL;
        }

        tmp = parsedTokens.remove("REVERSED");
        if (tmp != null) {
            rotationStyle = RotationStyle.REVERSED;
        }
        tmp = parsedTokens.remove(TokenListParser.LEFT_OVER_TOKENS);
        if (tmp != null && !tmp.isEmpty()) {
            StringBuilder sb = new StringBuilder("Unexpected tokens ( ");
            for (Object obj : tmp) {
                sb.append(obj).append(" ");
            }
            sb.append(")");
            throw new IllegalStateException(sb.toString());
        }
    }

    @Override
    public void execute() {
        List<SacTraceData> files = SacDataModel.getInstance().getData();
        if (files.isEmpty()) {
            log.warn("1301: No data files read in.");
        } else {
            TraceRotator.rotateTraces(files, rotationStyle, rotationType, rotationAngle);
        }
    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = { "ROTATE" };
        return new ArrayList<>(Arrays.asList(names));
    }

    @Override
    public String getHelpString() {
        return "SUMMARY\n"
                + "	Rotates a pair of data components through an angle.\n"
                + "\n"
                + "SYNTAX\n"
                + "	ROTATE {to GCP|TO v|THROUGH v|,{NORMAL|REVERSED}\n"
                + "INPUT\n"
                + "	TO GCP:	Rotate to the \"great circle path\". Both components must be horizontals. \n"
                + "	The station and event coordinates header fields must be defined.\n"
                + "	TO v:	Rotate to the angle v (degrees). Both components must be horizontals.\n"
                + "	THROUGH v:	Rotate through the angle v in degrees. One component may be vertical.\n"
                + "	NORMAL:	Output (horizontal) components with the second leading the first by 90 \n"
                + "	degrees (clockwise rotation looking down).\n"
                + "	REVERSED:	Output (horizontal) components with the second lagging the first by 90 \n"
                + "	degrees (counterclockwise rotation looking down).\n"
                + "DEFAULT VALUES\n"
                + "	ROTATE TO GCP NORMAL\n"
                + "DESCRIPTION\n"
                + "	Pairs of data components are rotated in this command. Each pair must have the same \n"
                + "	station and event header variables, NPTS, B, and DELTA\n"
                + "\n"
                + "	TO option: Both components must be horizontals: CMPAZ must be defined and CMPINC must be \n"
                + "	90 degrees. After the rotation is completed, the first component of each pair will be \n"
                + "	directed along the angle given after the TO keyword. If the TO GCP option is used, the \n"
                + "	station and header fields STLA, STLO, EVLA, and EVLO must be defined so that the \n"
                + "	backazimuth (BAZ) can be calculated. After the rotation, the first component will be \n"
                + "	directed along the angle given by the station-event backazimuth plus or minus 180 degrees \n"
                + "	(to keep the final angle between 0 and 360 degrees). This component therefore points \n"
                + "	from the event toward the station (the radial direction), and the second component is \n"
                + "	called 'transverse' or 'tangential'. (The (upward) vertical, radial, and transverse \n"
                + "	directions form a left-handed coordinate system.)\n"
                + "\n"
                + "	The NORMAL and REVERSED options apply only to horizontal rotations. If the NORMAL option \n"
                + "	is used, the second component leads the first by 90 degrees. If the REVERSED option is \n"
                + "	used, it lags the first by 90 degrees. ROTATE TO GCP REVERSED results in a transverse \n"
                + "	component in the opposite direction from ROTATE TO GCP NORMAL, a convention preferred \n"
                + "	by some researchers.\n"
                + "\n"
                + "EXAMPLES\n"
                + "	To rotate a pair of horizontals to a specified angle for the first component:\n"
                + "\n"
                + "		SAC> READ XYZ.N XYZ.E\n"
                + "		SAC> ROTATE TO 123.43\n"
                + "	To rotate two sets of horizontals so that the first component in each set along the \n"
                + "	great circle path and then write SAC files for the radial and transverse components:\n"
                + "\n"
                + "		SAC> READ ABC.N ABC.E DEF.N DEF.E\n"
                + "		SAC> ROTATE TO GCP\n"
                + "		SAC> W ABC.R ABC.T DEF.R DEF.T\n"
                + "HEADER CHANGES\n"
                + "	CMPAZ, CMPINC, KCMPNM, DEP*\n"
                + "ERROR MESSAGES\n"
                + "	1301: No data files read in.\n"
                + "	2004: Insufficient header information for rotation:\n"
                + "	STLA, STLO, EVLA, EVLO must be defined for GCP option.";
    }
}
