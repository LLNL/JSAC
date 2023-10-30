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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.print.PrintService;
import javax.swing.SwingUtilities;

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
import gov.llnl.gnem.jsac.plots.plot.PlotFrame;
import gov.llnl.gnem.jsac.plots.plot.PlotPanel;
import llnl.gnem.dftt.core.gui.plotting.PlotPrinter;
import llnl.gnem.dftt.core.util.PairT;

/**
 *
 * @author dodge1
 */
public class PlotSacCommand implements SacCommand {

    private static final Logger log = LoggerFactory.getLogger(PlotSacCommand.class);

    private final Scanner sc = new Scanner(System.in);
    private static final List<AttributeDescriptor> descriptors = new ArrayList<>();
    private static final List<String> allowableFormats = new ArrayList<>(Arrays.asList("PNG", "GIF", "SVG"));
    private static final String DEFAULT_FORMAT = "PNG";
    private static final List<Integer> ALLOWABLE_DPI = new ArrayList<>(Arrays.asList(50, 100, 200, 300, 600, 1000));
    private static final int DEFAULT_DPI = 300;
    private boolean print = false;
    private String selectedFormat;
    private String fileName;
    private int selectedDPI;
    private boolean listPrintersOnly;
    private String printerName;

    public PlotSacCommand() {
        print = false;
        selectedFormat = null;
        fileName = null;
        selectedDPI = DEFAULT_DPI;
        listPrintersOnly = false;
        printerName = null;
    }

    static {
        descriptors.add(new AttributeDescriptor("PRINT", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("FORMAT", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("FILENAME", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("DPI", ValuePossibilities.ONE_VALUE, Integer.class));
        descriptors.add(new AttributeDescriptor("LIST_PRINTERS", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("LIST", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("PRINTERNAME", ValuePossibilities.ONE_VALUE, String.class));
    }

    @Override
    public void initialize(String[] tokens) {
        print = false;
        selectedFormat = null;
        fileName = null;
        selectedDPI = DEFAULT_DPI;
        listPrintersOnly = false;
        printerName = null;

        Map<String, List<Object>> parsedTokens = TokenListParser.parseTokens(descriptors, tokens);
        List<Object> result = TokenListParser.getObjects(parsedTokens, "PRINT");
        print = result != null;

        result = TokenListParser.getObjects(parsedTokens, "FORMAT");
        if (result != null && !result.isEmpty()) {
            String tmp = (String) result.get(0);
            if (!allowableFormats.contains(tmp.toUpperCase())) {
                throw new IllegalStateException("Unsupported format: " + tmp + "!");
            } else {
                selectedFormat = tmp.toUpperCase();
            }
        }
        result = TokenListParser.getObjects(parsedTokens, "FILENAME");
        if (result != null && !result.isEmpty()) {
            String tmp = ((String) result.get(0)).trim();
            if (!tmp.isEmpty()) {
                if (selectedFormat == null) {
                    selectedFormat = DEFAULT_FORMAT;
                }
                if (tmp.toUpperCase().endsWith("." + DEFAULT_FORMAT.toUpperCase())) {
                    int index = tmp.lastIndexOf(".");
                    fileName = tmp.substring(0, index);
                } else {
                    fileName = tmp;
                }

            }
        }

        result = TokenListParser.getObjects(parsedTokens, "DPI");
        if (result != null && !result.isEmpty()) {
            Integer tmp = (Integer) result.get(0);
            if (!ALLOWABLE_DPI.contains(tmp)) {
                throw new IllegalStateException("Unsupported resolution: " + tmp + "!");
            } else {
                selectedDPI = tmp;
            }
        }

        result = TokenListParser.getObjects(parsedTokens, "LIST_PRINTERS", "LIST");
        listPrintersOnly = result != null;

        result = TokenListParser.getObjects(parsedTokens, "PRINTERNAME");
        if (result != null && !result.isEmpty()) {
            String tmp = ((String) result.get(0)).trim();
            if (!tmp.isEmpty()) {
                printerName = tmp;
            }
        }

    }

    private void displayPrompt() {
        System.out.print("PLOT (Q to quit plotting)>");
        System.out.flush();
    }

    @Override
    public void execute() {
        List<SacTraceData> data = SacDataModel.getInstance().getPlottableData();
        if (print && listPrintersOnly) {
            PlotPrinter.getInstance().listPrinters();
            return;
        }
        if (data.size() < 1) {
            log.warn("No data files to plot!");
            return;
        }

        PairT<Double, Double> dataMinMax = PlotUtils.getGlobalLimits(data);
        PlotAxes.getInstance().getYlimits().setDataRange(dataMinMax);
        PlotAxes.getInstance().getYlimits().resetIndex(); //If multiple limits have been specified this sets the pointer at 1st limit.
        int traceCount = SacDataModel.getInstance().getTraceCount();
        for (int j = 0; j < data.size(); ++j) {
            SacTraceData sacData = data.get(j);
            if (!sacData.isPlottable()) {
                continue;
            }
            SwingUtilities.invokeLater(() -> {
                PlotFrame.getInstance().setVisible(true);
            });
            final int m = j;
            SwingUtilities.invokeLater(() -> {
                PlotPanel.getInstance().plotSeismogram(sacData, m);
                if (print) {
                    try {
                        if (fileName == null) {
                            if (printerName != null) {
                                PrintService service = PlotPrinter.getInstance().getServiceByName(printerName);
                                if (service != null) {
                                    PlotPrinter.getInstance().printCurrentPlot(PlotPanel.getInstance(), true, service);
                                } else {
                                    throw new IllegalStateException("No such printer: " + printerName + "!");
                                }
                            } else {
                                PlotPrinter.getInstance().printCurrentPlot(PlotPanel.getInstance(), true);
                            }
                        } else {
                            int width = PlotFrame.getInstance().getWidth();
                            int height = PlotFrame.getInstance().getHeight();
                            String printName = constructFileName(data, m);
                            PlotPrinter.getInstance().exportPlot(PlotPanel.getInstance(), width, height, selectedDPI, selectedFormat, printName);
                        }
                    } catch (IOException ex) {
                        log.error(ex.getLocalizedMessage(), ex);
                    }
                }
            });

            if (j >= traceCount - 1) {
                return;
            }
            if (isPauseAfterPlotting()) {
                String text = sc.nextLine().trim().toUpperCase();
                if (text.equals("Q") || text.equals("QUIT")) {
                    System.out.println("");
                    return;
                }
                displayPrompt();
            }

        }

    }

    private boolean isPauseAfterPlotting() {
        return !print;
    }

    private String constructFileName(List<SacTraceData> data, final int m) {
        StringBuilder printName = new StringBuilder().append(fileName);
        if (data.size() == 1) {
            printName.append(".").append(selectedFormat.toLowerCase());
        } else {
            printName.append(".").append(m + 1).append(".").append(selectedFormat.toLowerCase());
        }
        return printName.toString();
    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = { "P", "PLOT" };
        return new ArrayList<>(Arrays.asList(names));
    }

    @Override
    public String getHelpString() {
        return "SUMMARY\n"
                + "	Generates a single-trace single-window plot.\n"
                + "\n"
                + "SYNTAX\n"
                + "	PLOT {PRINT}\n"
                + "INPUT\n"
                + "     PRINT :			Prints the resulting plot to a printer or to a file.\n"
                + "	 \n"
                + "		FORMAT			When printing to a file, FORMAT controls the output format.\n"
                + "						Allowable formats are PNG,GIF,SVG. Default format is PNG\n"
                + "						\n"
                + "		FILENAME		The name of the output file. If multiple SAC files are in memory\n"
                + "						then the file number is appended to the file name, \n"
                + "						e.g., filename.1.png, filename.2.png, etc.\n"
                + "						\n"
                + "		PRINTERNAME		If no filename is specified then output is sent to a printer. If no\n"
                + "						printer name is specified, then output is sent to the default printer.\n"
                + "						Printer names must be specified as shown by the LIST_PRINTERS option.\n"
                + "						Names with spaces must be enclosed in quotes.\n"
                + "		{LIST}_PRINTERS	If the LIST sub-option is specified then JSAC will simply list the\n"
                + "						available printers.\n"
                + "						\n"
                + "		DPI				For raster formats (PNG,GIF) This specifies the resolution in dots-per-inch.\n"
                + "						The default is 300 dpi.\n"
                + "						\n"
                + "DESCRIPTION\n"
                + "	Each data file is displayed one at a time in a single plot window. \n"
                + "	After each file is plotted the prompt PLOT (Q to quit plotting) \n"
                + "	is displayed. To advance to the next plot press the Enter key. \n"
                + "	To quit plotting press the Q key.\n"
                + "	Note: If output was requested all plots are processed without interaction.\n"
                + "\n"
                + "EXAMPLES\n"
                + "	To list printers only:\n"
                + "		plot print list\n"
                + "		\n"
                + "	To send plots to the default printer:\n"
                + "		fg seis\n"
                + "		plot print\n"
                + "	\n"
                + "	To send plots to a named printer:\n"
                + "		fg seis\n"
                + "		plot print printername \"Adobe PDF\"\n"
                + "	\n"
                + "	To export the plot to an SVG-format file named example.svg\n"
                + "		fg seis\n"
                + "		plot print filename example format svg";
    }
}
