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

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import gov.llnl.gnem.jsac.commands.AttributeDescriptor;
import gov.llnl.gnem.jsac.commands.SacCommand;
import gov.llnl.gnem.jsac.commands.TokenListParser;
import gov.llnl.gnem.jsac.commands.ValuePossibilities;
import gov.llnl.gnem.jsac.plots.CLabel;
import gov.llnl.gnem.jsac.plots.PlotAxes;
import gov.llnl.gnem.jsac.plots.PlotColors;
import llnl.gnem.dftt.core.gui.plotting.HorizAlignment;
import llnl.gnem.dftt.core.gui.plotting.VertAlignment;

public class ClabelSacCommand implements SacCommand {

    private static final List<AttributeDescriptor> descriptors = new ArrayList<>();

    static {
        descriptors.add(new AttributeDescriptor("SIZE", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("POSITION", ValuePossibilities.TWO_VALUES, Double.class));
        descriptors.add(new AttributeDescriptor("CLEAR", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("HORIZ_ALIGN", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("VERT_ALIGN", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("FONT", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("COLOR", ValuePossibilities.ONE_VALUE, String.class));
    }

    private static int size = PlotAxes.getInstance().getDefaultFontSize();
    private static HorizAlignment horizAlignment = HorizAlignment.LEFT;
    private static VertAlignment vertAlignment = VertAlignment.BOTTOM;
    private static String fontName = "Arial";
    private static Color textColor = Color.black;

    public ClabelSacCommand() {

    }

    @Override
    public void initialize(String[] tokens) {

        Double x = null;
        Double y = null;
        String labelText = null;
        Map<String, List<Object>> parsedTokens = TokenListParser.parseTokens(descriptors, tokens);
        if (parsedTokens.isEmpty()) {
            return;
        }

        List<Object> objects = parsedTokens.remove("CLEAR");
        if (objects != null) {
            PlotAxes.getInstance().clearCLabels();
        }
        objects = parsedTokens.remove("SIZE");
        if (objects != null) {
            for (Object aSize : objects) {
                String tmp = (String) aSize;
                size = PlotAxes.getInstance().getSizeFromString(tmp);
                break;
            }
        }

        objects = parsedTokens.remove("FONT");
        if (objects != null && !objects.isEmpty()) {
            fontName = (String) objects.get(0);
        }
        objects = parsedTokens.remove("COLOR");
        if (objects != null && !objects.isEmpty()) {
            String tmp = (String) objects.get(0);
            textColor = PlotColors.getColorByName(tmp);
        }

        objects = parsedTokens.remove("POSITION");
        if (objects != null && objects.size() == 2) {
            x = (Double) objects.remove(0);
            y = (Double) objects.remove(0);
        }
        objects = parsedTokens.remove("HORIZ_ALIGN");
        if (objects != null && !objects.isEmpty()) {
            String label = (String) objects.get(0);
            horizAlignment = HorizAlignment.getHorizAlignment(label.toUpperCase());
        }
        objects = parsedTokens.remove("VERT_ALIGN");
        if (objects != null && !objects.isEmpty()) {
            String label = (String) objects.get(0);
            vertAlignment = VertAlignment.getVertAlignment(label.toUpperCase());
        }
        objects = parsedTokens.get(TokenListParser.LEFT_OVER_TOKENS);
        if (objects != null && !objects.isEmpty()) {
            labelText = (String) objects.get(0);
        }

        if (x == null || y == null) {
            throw new IllegalStateException("You must supply X and Y values to the CLABEL command!");
        }
        if (labelText == null || labelText.isEmpty()) {
            throw new IllegalStateException("You must a non-empty string to the CLABEL command!");
        }

        CLabel label = new CLabel(labelText, fontName, textColor, x, y, size, horizAlignment, vertAlignment);
        PlotAxes.getInstance().addCLabel(label);
    }

    @Override
    public void execute() {

    }

    @Override
    public String getHelpString() {
        return "SUMMARY\n"
                + "	Defines general plot labels and their attributes.\n"
                + "\n"
                + "SYNTAX\n"
                + "	CLABEL {CLEAR}, text,{SIZE size}, POSITION x y , {HORIZ_ALIGN value}, \n"
                + "	{VERT_ALIGN value}, {FONT value}, {COLOR value}\n"
                + "\n"
                + "where size is one of the following:\n"
                + "TINY|SMALL|MEDIUM|LARGEINPUT\n"
                + "	text:		The text of plot label. \n"
                + "	SIZE size:	Set the plot label size.\n"
                + "		TINY:		Tiny text size has 132 characters per line.\n"
                + "		SMALL:		Small text size has 100 characters per line.\n"
                + "		MEDIUM:		Medium text size has 80 characters per line.\n"
                + "		LARGE:		Large text size has 50 characters per line.\n"
                + "		HUGE:		Huge text size has 33 characters per line.\n"
                + "	POSITION 	x y Define a specific position for this label. \n"
                + "				The text is plotted at the point in the plot\n"
                + "				coordinate system given by (x,y).\n"
                + "	HORIZ_ALIGN LEFT|CENTER|RIGHT\n"
                + "	VERT_ALIGN	TOP|CENTER|BOTTOM\n"
                + "	FONT		The font to use for the label text. Multi-word font \n"
                + "				names must be enclsed with single- or double quotes.\n"
                + "	COLOR		The color of the label text. See the COLOR command \n"
                + "				for the list of available colors.\n"
                + "	CLEAR		removes all labels created using CLABEL\n"
                + "	             \n"
                + "DEFAULT VALUES\n"
                + "	Default size is small.\n"
                + "	Default HORIZ_ALIGN is LEFT\n"
                + "	Default VERT_ALIGN is BOTTOM\n"
                + "	Default font is Arial\n"
                + "	Default color is BLACK\n"
                + "\n"
                + "DESCRIPTION\n"
                + "	This command lets you define general purpose plot labels for subsequent \n"
                + "	plot commands. You can define the location and size of each label. \n"
                + "	You can also generate a title and axes labels using the TITLE, XLABEL, and YLABEL commands.\n"
                + "EXAMPLES\n"
                + "	The following command will generate a label at the position (14.37, 0.345):\n"
                + "\n"
                + "		u:  CLABEL 'Local maximum' POSITION 14.37 0.345 ";
    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = { "CLABEL" };
        return new ArrayList<>(Arrays.asList(names));
    }

}
