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

package gov.llnl.gnem.jsac.commands.graphicsEnvironment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import gov.llnl.gnem.jsac.commands.AttributeDescriptor;
import gov.llnl.gnem.jsac.commands.SacCommand;
import gov.llnl.gnem.jsac.commands.TokenListParser;
import gov.llnl.gnem.jsac.commands.ValuePossibilities;
import gov.llnl.gnem.jsac.plots.PlotColors;

/**
 *
 * @author dodge1
 */
public class ColorSacCommand implements SacCommand {

    private static final List<AttributeDescriptor> descriptors = new ArrayList<>();

    static {
        descriptors.add(new AttributeDescriptor("COLOR", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("SKELETON", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("BACKGROUND", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("INCREMENT", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("INC", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("LIST", ValuePossibilities.ONE_OR_MORE, String.class));
    }

    public ColorSacCommand() {

    }

    @Override
    public void initialize(String[] tokens) {

        Map<String, List<Object>> parsedTokens = TokenListParser.parseTokens(descriptors, tokens);
        if (parsedTokens.isEmpty()) {
            return;
        }

        List<Object> objects = parsedTokens.remove("COLOR");
        if (objects != null && objects.size() == 1) {
            Object tmp = objects.get(0);
            PlotColors.getInstance().setLineColor((String) tmp);
        }
        objects = parsedTokens.remove("SKELETON");
        if (objects != null && objects.size() == 1) {
            Object tmp = objects.get(0);
            PlotColors.getInstance().setSkeletonColor((String) tmp);
        }
        objects = parsedTokens.remove("BACKGROUND");
        if (objects != null && objects.size() == 1) {
            Object tmp = objects.get(0);
            PlotColors.getInstance().setBackGroundColor((String) tmp);
        }

        objects = parsedTokens.remove("INCREMENT");
        if (objects == null) {
            objects = parsedTokens.remove("INC");
        }
        if (objects != null) {
            if (objects.isEmpty()) {
                PlotColors.getInstance().setIncrementColors(true);
            } else if (objects.size() == 1) {
                String tmp = (String) objects.get(0);
                PlotColors.getInstance().setIncrementColors(tmp.equalsIgnoreCase("ON"));
            }
        }
        parseListOption(parsedTokens);

        objects = parsedTokens.get(TokenListParser.LEFT_OVER_TOKENS);
        if (objects != null && !objects.isEmpty()) {
            for (Object obj : objects) {
                String valueString = (String) obj;
                PlotColors.getInstance().setLineColor(valueString);
            }
        }

    }

    private void parseListOption(Map<String, List<Object>> parsedTokens) throws NumberFormatException {
        List<Object> objects;
        objects = parsedTokens.remove("LIST");
        if (objects != null) {
            List<String> incList = new ArrayList<>();
            for (Object obj : objects) {
                String tmp = (String) obj;
                if (tmp.equalsIgnoreCase("STANDARD")) {
                    PlotColors.getInstance().setListToStandard();
                    return;
                } else {
                    incList.add((tmp));
                }
            }
            PlotColors.getInstance().setColorList(incList);
        }
    }

    @Override
    public void execute() {

    }

    @Override
    public String getHelpString() {
        return "SUMMARY\n"
                + "	Controls color selection for color graphics devices.\n"
                + "\n"
                + "SYNTAX\n"
                + "	COLOR  {color} options\n"
                + "	where options are one or more of the following:\n"
                + "\n"
                + "		{INCREMENT {ON|OFF}}\n"
                + "		{SKELETON color}\n"
                + "		{BACKGROUND color}\n"
                + "		{LIST STANDARD|colorlist}\n"
                + "		and where color is one of the following:\n"
                + "			WHITE|RED|GREEN|YELLOW|BLUE|MAGENTA|CYAN|BLACK|DARK_GRAY|GRAY|LIGHT_GRAY|PINK\n"
                + "SPECIAL NOTE The LIST option must appear last in this command.\n"
                + "\n"
                + "INPUT\n"
                + "	COLOR color:	Change data color.\n"
                + "	INCREMENT {ON}:	Increment data color from color list after each data file is plotted.\n"
                + "	INCREMENT OFF:	Do not increment data color.\n"
                + "	SKELETON color:	Change color of skeleton to standard color name.\n"
                + "	BACKGROUND color:\n"
                + "		Change background color to standard color name.\n"
                + "	LIST colorlist:	Change the content of the color list. \n"
                + "		Enter list of standard color names. \n"
                + "		Sets data color to first color in list.\n"
                + "	LIST STANDARD:	Change to the standard color list. \n"
                + "			Sets data color to first color in list.\n"
                + "DEFAULT VALUES\n"
                + "	COLOR BLACK INCREMENT OFF SKELETON BLACK BACKGROUND WHITE LIST STANDARD";
    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = {"COLOR","COL"};
        return new ArrayList<>(Arrays.asList(names));
    }

}
