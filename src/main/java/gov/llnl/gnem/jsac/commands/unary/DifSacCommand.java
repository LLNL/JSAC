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

package gov.llnl.gnem.jsac.commands.unary;

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
 * @author dodge1 revised by D. Harris to make consistent with IRIS version and
 *         to add highly accurate equiripple (Parks-McClellan) differentiators
 */
public class DifSacCommand implements SacCommand {

    private static final List<AttributeDescriptor> descriptors = new ArrayList<>();

    static {
        descriptors.add(new AttributeDescriptor("TWO", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("THREE", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("FIVE", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("EQUIRIPPLE", ValuePossibilities.TWO_VALUES, Double.class));
        descriptors.add(new AttributeDescriptor("EQ", ValuePossibilities.TWO_VALUES, Double.class));
    }

    static DifferentiatorType difType = DifferentiatorType.TWO_POINT_OPERATOR;

    public DifSacCommand() {
    }

    @Override
    public void initialize(String[] tokens) {

        Map<String, List<Object>> parsedTokens = TokenListParser.parseTokens(descriptors, tokens);
        if (parsedTokens.isEmpty()) {
            return;
        }

        if (parsedTokens.containsKey("TWO")) {
            difType = DifferentiatorType.TWO_POINT_OPERATOR;
        } else if (parsedTokens.containsKey("THREE")) {
            difType = DifferentiatorType.THREE_POINT_OPERATOR;
        } else if (parsedTokens.containsKey("FIVE")) {
            difType = DifferentiatorType.FIVE_POINT_OPERATOR;
        } else if (parsedTokens.containsKey("EQUIRIPPLE") || parsedTokens.containsKey("EQ")) {
            difType = DifferentiatorType.EQUIRIPPLE;
        } else {
            difType = DifferentiatorType.TWO_POINT_OPERATOR;
        }

    }

    @Override
    public void execute() {
        SacDataModel.getInstance().differentiate(difType);
    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = { "DIF" };
        return new ArrayList<>(Arrays.asList(names));
    }

    @Override
    public String getHelpString() {
        return "SUMMARY\n"
                + "     Differentiates the data in memory.  A variety of differentiators are provided as options, including locally (around \n"
                + "       0 Hz) accurate approximations from a Taylor series expansion, and wideband differentiators designed with the Parks\n"
                + "       McClellan algorithm.\n"
                + "\n"
                + "SYNTAX\n"
                + "\n"
                + "	DIF { TWO | THREE | FOUR | [EQ]UIRIPPLE }\n"
                + "\n"
                + "INPUT\n"
                + "\n"
                + "TWO:         Apply a two-point difference operator.\n"
                + "THREE:       Apply a three-point difference operator.\n"
                + "FIVE:        Apply a four-point difference operator.\n"
                + "EQUIRIPPLE   Apply a two-sided, 81 point Parks-McClellan differentiator.\n"
                + "\n"
                + "DEFAULT VALUES\n"
                + "\n"
                + "DIF TWO\n"
                + "\n"
                + "DESCRIPTION\n"
                + "\n"
                + "The two-point algorithm is:\n"
                + "  OUT(J) = (DATA(J+1) - DATA(J)) / DELTA\n"
                + "  The last output point is not defined by this algorithm.  It also is not a centered algorithm.  SAC takes care of these\n"
                + "  problems by decreasing the number of points in the file (NPTS) by one and by increasing the begin time (B) by half the\n"
                + "  sampling interval (DELTA/2).\n"
                + "The three-point (centered 2-point) algorithm is:\n"
                + "  OUT(J) = 1/2 * (DATA(J+1) - DATA(J-1)) / DELTA\n"
                + "  The first and last output points are not defined by this algorithm.  SAC decreases NPTS by 2 and increases B by DELTA.\n"
                + "The five-point (centered 3-point) algorithm is:\n"
                + "  OUT(J) = 2/3 * (DATA(J+1) - DATA(J-1)) / DELTA  -  1/12 * (DATA(J+2) - DATA(J-2)) / DELTA\n"
                + "  The first two and last two output points are not defined by this algorithm.  SAC applies the three-point operator\n"
                + "  to the second points from each end, decreases NPTS by 2, and increases B by DELTA.\n"
                + "The applies a 81 point symmetric (centered) FIR diffentiator, designed with the Parks-McClellan algorithm, that has\n"
                + "  equiripple error as a fraction of the nominal response over 90% of the frequency band.  This implementation pads the \n"
                + "  data with zeros on either side, filters the data, then crops it back to the original length.  Consequently, there \n"
                + "  are no changes to NPTS, B and E with this option.\n"
                + "\n"
                + "HEADER CHANGES\n"
                + "\n"
                + "NPTS, B, E, DEPMIN, DEPMAX, DEPMEN\n";
    }
}
