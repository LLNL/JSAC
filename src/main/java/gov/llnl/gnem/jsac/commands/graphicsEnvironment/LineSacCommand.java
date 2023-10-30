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

import org.apache.commons.lang3.math.NumberUtils;

import gov.llnl.gnem.jsac.commands.AttributeDescriptor;
import gov.llnl.gnem.jsac.commands.SacCommand;
import gov.llnl.gnem.jsac.commands.TokenListParser;
import gov.llnl.gnem.jsac.commands.ValuePossibilities;
import gov.llnl.gnem.jsac.plots.PlotDrawingData;
import llnl.gnem.dftt.core.gui.plotting.PenStyle;

/**
 *
 * @author dodge1
 */
public class LineSacCommand implements SacCommand {

    private static final List<AttributeDescriptor> descriptors = new ArrayList<>();

    static {
        descriptors.add(new AttributeDescriptor("SOLID", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("DOTTED", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("INCREMENT", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("LIST", ValuePossibilities.ONE_OR_MORE, String.class));
    }

    public LineSacCommand() {

    }

    @Override
    public void initialize(String[] tokens) {

        Map<String, List<Object>> parsedTokens = TokenListParser.parseTokens(descriptors, tokens);
        if (parsedTokens.isEmpty()) {
            return;
        }

        List<Object> objects = parsedTokens.remove("SOLID");
        if (objects != null) {
            PlotDrawingData.getInstance().setPenStyle(PenStyle.SOLID);
        }
        objects = parsedTokens.remove("DOTTED");
        if (objects != null) {
            PlotDrawingData.getInstance().setPenStyle(PenStyle.DOT);
        }

        objects = parsedTokens.remove("INCREMENT");
        if (objects != null) {
            if (objects.isEmpty()) {
                PlotDrawingData.getInstance().setIncrementPenStyles(true);
            } else if (objects.size() == 1) {
                String tmp = (String) objects.get(0);
                PlotDrawingData.getInstance().setIncrementPenStyles(tmp.equalsIgnoreCase("ON"));
            }
        }
        parseListOption(parsedTokens);

        objects = parsedTokens.get(TokenListParser.LEFT_OVER_TOKENS);
        if (objects != null && !objects.isEmpty()) {
            String valueString = (String) objects.get(0);
            if(valueString.equalsIgnoreCase("ON") || valueString.equalsIgnoreCase("OFF"))
            PlotDrawingData.getInstance().setDrawLines(valueString.equalsIgnoreCase("ON"));
            else if(NumberUtils.isParsable(valueString)) {
                int v = Integer.parseInt(valueString);
                PlotDrawingData.getInstance().setPenStyle(v);
            }
        }
    }

    private void parseListOption(Map<String, List<Object>> parsedTokens) throws NumberFormatException {
        List<Object> objects;
        objects = parsedTokens.remove("LIST");
        if (objects != null) {
            List<Integer> incList = new ArrayList<>();
            for (Object obj : objects) {
                String tmp = (String) obj;
                if (tmp.equalsIgnoreCase("STANDARD")) {
                    PlotDrawingData.getInstance().setLineIncrementList("STANDARD");
                    return;
                } else if (NumberUtils.isParsable(tmp)) {
                    incList.add(Integer.parseInt(tmp));
                }
            }
            PlotDrawingData.getInstance().setLineIncrementList(incList);
        }
    }

    @Override
    public void execute() {

    }

    @Override
    public String getHelpString() {
        return "SUMMARY\n"
                + "	Controls the linestyle selection in plots.\n"
                + "\n"
                + "SYNTAX\n"
                + "LINE {ON|OFF|SOLID|DOTTED|n} {INCREMENT {ON|OFF}}, {LIST STANDARD|nlist}\n"
                + "INPUT\n"
                + "	{ON}:			Turn line-drawing on. Don't change linestyle.\n"
                + "	OFF:			Turn line-drawing off.\n"
                + "	SOLID:			Change to solid linestyle and turn line-drawing on.\n"
                + "	DOTTED:			Change to dotted linestyle and turn line-drawing on.\n"
                + "	n:				Change to linestyle n and turn line-drawing on. \n"
                + "	INCREMENT {ON}:	For multiple data files in a plot, increment \n"
                + "					linestyle from linestyle list for each data file in the plot.\n"
                + "	INCREMENT OFF:	Do not increment linestyle for multiple data files.\n"
                + "	LIST STANDARD:	Change to the standard linestyle list (1 2 3 ..).\n"
                + "	LIST nlist:		Change the content of the linestyle list. \n"
                + "					Enter list of linestyle numbers (e.g., 3 1 2 ..).\n"
                + "DEFAULT VALUES\n"
                + "	LINE SOLID INCREMENT OFF LIST STANDARD ";
    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = {"LINE"};
        return new ArrayList<>(Arrays.asList(names));
    }

}
