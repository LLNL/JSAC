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
package gov.llnl.gnem.jsac.commands.graphicsAction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.swing.SwingUtilities;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.llnl.gnem.jsac.SacDataModel;
import gov.llnl.gnem.jsac.commands.AttributeDescriptor;
import gov.llnl.gnem.jsac.commands.SacCommand;
import gov.llnl.gnem.jsac.commands.TokenListParser;
import gov.llnl.gnem.jsac.commands.ValuePossibilities;
import gov.llnl.gnem.jsac.dataAccess.dataObjects.SacTraceData;
import gov.llnl.gnem.jsac.plots.PlotAxes;
import gov.llnl.gnem.jsac.plots.PlotUtils;
import gov.llnl.gnem.jsac.plots.TimeMode;
import gov.llnl.gnem.jsac.plots.plot1.Plot1Frame;
import gov.llnl.gnem.jsac.plots.plot1.Plot1Panel;
import llnl.gnem.dftt.core.util.PairT;

/**
 *
 * @author dodge1
 */
public class Plot1SacCommand implements SacCommand {

    private static final Logger log = LoggerFactory.getLogger(Plot1SacCommand.class);

    private final Scanner sc = new Scanner(System.in);
    private static final List<AttributeDescriptor> descriptors = new ArrayList<>();
    private boolean print = false;

    public static TimeMode timeMode = TimeMode.ABSOLUTE;
    private static int blockSize = -1; // Plot all in single frame
    private static boolean perPlot = false;

    static {
        descriptors.add(new AttributeDescriptor("ABSOLUTE", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("ABS", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("RELATIVE", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("REL", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("EPOCH", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("PERPLOT", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("P", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("PRINT", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("SPACING", ValuePossibilities.ONE_VALUE, Double.class));
    }

    @Override
    public void initialize(String[] tokens) {
        print = false;
        Map<String, List<Object>> parsedTokens = TokenListParser.parseTokens(descriptors, tokens);
        List<Object> result = TokenListParser.getObjects(parsedTokens, "ABSOLUTE", "ABS");
        if (result != null) { //Parameter was specified
            timeMode = TimeMode.ABSOLUTE;
        }
        result = parsedTokens.remove("RELATIVE");
        if (result == null) {
            result = parsedTokens.remove("REL");
        }
        if (result != null) { //Parameter was specified
            timeMode = TimeMode.RELATIVE;
        }
        result = parsedTokens.remove("EPOCH");
        if (result != null) { //Parameter was specified
            timeMode = TimeMode.EPOCH;
        }
        result = TokenListParser.getObjects(parsedTokens, "PERPLOT", "P");
        if (result != null && !result.isEmpty()) { //Parameter was specified
            String value = (String) result.get(0);
            if (value.equalsIgnoreCase("ON")) {
                perPlot = true;
            } else if (value.equalsIgnoreCase("OFF") || value.equalsIgnoreCase("ALL")) {
                perPlot = false;
            } else if (NumberUtils.isParsable(value)) {
                blockSize = Integer.parseInt(value);
                perPlot = true;
            }
        }

        result = TokenListParser.getObjects(parsedTokens, "SPACING");
        if (result != null && !result.isEmpty()) {
            Double spacing = (Double) result.get(0);
            PlotAxes.getInstance().setSubplotSpacing(spacing);
        }
        result = parsedTokens.remove("PRINT");
        print = result != null;
    }

    private void displayPrompt() {
        System.out.print("PLOT (Q to quit plotting)>");
        System.out.flush();
    }

    @Override
    public void execute() {
        List<SacTraceData> data = SacDataModel.getInstance().getPlottableData();

        // Initialization for global ylimits if so requested.
        PairT<Double, Double> dataMinMax = PlotUtils.getGlobalLimits(data);
        PlotAxes.getInstance().getYlimits().setDataRange(dataMinMax);
        PlotAxes.getInstance().getYlimits().resetIndex(); //If multiple limits have been specified this sets the pointer at 1st limit.

        int totalFiles = data.size();
        if (totalFiles < 1) {
            log.warn("No data files to plot!");
            return;
        }
        if (!perPlot || blockSize < 1 || blockSize > totalFiles) {
            SwingUtilities.invokeLater(() -> {
                Plot1Panel.getInstance().plotDataBlock(data, timeMode, 0);
                Plot1Frame.getInstance().setVisible(true);
                if (print) {
                    Plot1Panel.getInstance().print(true);
                }
            });

            return;
        }
        List<SacTraceData> block = new ArrayList<>();
        int numBlocks = totalFiles / blockSize;
        int blockStart = 0;
        SwingUtilities.invokeLater(() -> {
            Plot1Frame.getInstance().setVisible(true);
        });
        for (int j = 0; j < numBlocks; ++j) {
            for (int k = 0; k < blockSize; ++k) {
                block.add(data.get(k + blockStart));
            }
            final int offset = blockStart;
            SwingUtilities.invokeLater(() -> {
                Plot1Panel.getInstance().plotDataBlock(block, timeMode, offset);
                if (print) {
                    Plot1Panel.getInstance().print(true);
                }
            });
            blockStart += blockSize;
            int numRemaining = data.size() - blockStart;
            if (numRemaining == 0) {
                return;
            }
            displayPrompt();
            String text = sc.nextLine().trim().toUpperCase();
            if (text.equals("Q") || text.equals("QUIT")) {
                System.out.println("");
                return;
            }

            block.clear();
        }
        int numRemaining = data.size() - blockStart;
        for (int k = 0; k < numRemaining; ++k) {
            block.add(data.get(k + blockStart));
        }
        final int offset = blockStart;
        SwingUtilities.invokeLater(() -> {
            Plot1Panel.getInstance().plotDataBlock(block, timeMode, offset);
            if (print) {
                Plot1Panel.getInstance().print(true);
            }
        });
    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = { "P1", "PLOT1" };
        return new ArrayList<>(Arrays.asList(names));
    }

    @Override
    public String getHelpString() {
        return "SUMMARY\n"
                + "Generates a multi-trace multi-window plot.\n"
                + "\n"
                + "SYNTAX\n"
                + "	[P]LOT[1] {ABSOLUTE|RELATIVE|EPOCH},{[P]ERPLOT {n|OFF|ON}} {SPACING v} {PRINT {pname} }\n"
                + "INPUT\n"
                + "	ABSOLUTE:	Plots files treating time as an absolute. Files with different begin times will be shifted relative to each other.\n"
                + "	RELATIVE:	Plots files relative to that file's begin time.\n"
                + "	EPOCH:          Plots files relative to earliest file's begin time as an epoch time.\n"
                + "	PERPLOT n:	Plots n files per frame.\n"
                + "	SPACING v:	Separate subplots by v mm.\n"
                + "	PERPLOT ON:	Plots n files per frame. Use last value for n.\n"
                + "	PERPLOT OFF:	Plots all files on one frame.\n"
                + "    PRINT:          Prints the resulting plot to the default printer.ALTERNATE FORMS\n"
                + "	PERPLOT ALL has the same meaning as PERPLOT OFF.\n"
                + "\n"
                + "DEFAULT VALUES\n"
                + "	PLOT1 ABSOLUTE PERPLOT OFF SPACING 2";
    }
}
