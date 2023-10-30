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
package gov.llnl.gnem.jsac.commands.filtering;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

import com.oregondsp.signalProcessing.filter.iir.PassbandType;

import gov.llnl.gnem.jsac.commands.AttributeDescriptor;
import gov.llnl.gnem.jsac.commands.SacCommand;
import gov.llnl.gnem.jsac.commands.TokenListParser;
import gov.llnl.gnem.jsac.commands.ValuePossibilities;
import gov.llnl.gnem.jsac.dataAccess.dataObjects.FilterDesignData;
import gov.llnl.gnem.jsac.io.FilterDesignDataPrinter;
import gov.llnl.gnem.jsac.io.FilterDesignDataWriter;
import gov.llnl.gnem.jsac.plots.plotFilterDesign.PlotFilterDesignFrame;
import gov.llnl.gnem.jsac.util.PathManager;
import llnl.gnem.dftt.core.signalprocessing.filter.FilterDesign;

public class FilterDesignSacCommand implements SacCommand {

    private static final List<AttributeDescriptor> descriptors = new ArrayList<>();

    static {
        descriptors.add(new AttributeDescriptor("PRINT", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("FILE", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("BUTTERWORTH", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("BU", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("C1", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("C2", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("LOWPASS", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("LP", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("HIGHPASS", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("HP", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("BANDPASS", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("BP", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("BANDREJECT", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("BR", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("CORNERS", ValuePossibilities.ONE_OR_MORE, Double.class));
        descriptors.add(new AttributeDescriptor("C", ValuePossibilities.ONE_OR_MORE, Double.class));
        descriptors.add(new AttributeDescriptor("NPOLES", ValuePossibilities.ONE_VALUE, Integer.class));
        descriptors.add(new AttributeDescriptor("N", ValuePossibilities.ONE_VALUE, Integer.class));
        descriptors.add(new AttributeDescriptor("PASSES", ValuePossibilities.ONE_VALUE, Integer.class));
        descriptors.add(new AttributeDescriptor("P", ValuePossibilities.ONE_VALUE, Integer.class));
        descriptors.add(new AttributeDescriptor("TRANBW", ValuePossibilities.ONE_VALUE, Double.class));
        descriptors.add(new AttributeDescriptor("T", ValuePossibilities.ONE_VALUE, Double.class));
        descriptors.add(new AttributeDescriptor("ATTEN", ValuePossibilities.ONE_VALUE, Double.class));
        descriptors.add(new AttributeDescriptor("A", ValuePossibilities.ONE_VALUE, Double.class));
    }

    private boolean printDesignData;
    private String printerName;
    private boolean writeDataToFiles;
    private String writePath;

    private PassbandType passbandType;
    private FilterDesign filterDesign;
    private int npoles;
    private int npasses;
    private double transitionBandwidth;
    private double attenuation;
    private double delta;
    private double f1;
    private double f2;

    private boolean valid;

    public FilterDesignSacCommand() {
    }

    @Override
    public void execute() {

        if (valid) {

            FilterDesignData fdd = new FilterDesignData(passbandType, filterDesign, npoles, npasses, transitionBandwidth, attenuation, delta, f1, f2);

            SwingUtilities.invokeLater(() -> {
                PlotFilterDesignFrame.getInstance().setVisible(true);
                PlotFilterDesignFrame.getInstance().plotDesignData(fdd);
            });

            if (writeDataToFiles) {
                FilterDesignDataWriter writer = new FilterDesignDataWriter(fdd);
                writer.write(writePath);
            }

            if (printDesignData) {
                FilterDesignDataPrinter printer = new FilterDesignDataPrinter(printerName);
                printer.print();
            }
        }

    }

    @Override
    public void initialize(String[] tokens) {
        printDesignData = false;
        writeDataToFiles = false;
        passbandType = PassbandType.LOWPASS;
        filterDesign = FilterDesign.Butterworth;
        npoles = 4;
        npasses = 1;
        transitionBandwidth = 0.5;
        attenuation = 100;
        delta = 0.025;
        f1 = 1.0;
        f2 = 5.0;
        valid = true;
        writePath = PathManager.getInstance().getCurrentDir().toString();
        
        Map<String, List<Object>> parsedTokens = TokenListParser.parseTokens(descriptors, tokens);
        if (parsedTokens.isEmpty()) {
            return;
        }

        if (parsedTokens.containsKey("PRINT")) {
            List<Object> parameters = parsedTokens.get("PRINT");
            printDesignData = true;
            if (!parameters.isEmpty()) {
                printerName = (String) parameters.get(0);
            } else {
                System.out.println("Error:  printer name missing");
                valid = false;
            }
        }

        if (parsedTokens.containsKey("FILE")) {
            List<Object> parameters = parsedTokens.get("FILE");
            writeDataToFiles = true;
            if (!parameters.isEmpty()) {
                writePath = (String) parameters.get(0);
                Path path = PathManager.getInstance().resolvePath(writePath);
                writePath = path.toString();

            } else {
                System.out.println("Error:  write path for files must be specified");
                valid = false;
            }
        }

        if (parsedTokens.containsKey("LOWPASS") || parsedTokens.containsKey("LP")) {
            passbandType = PassbandType.LOWPASS;
        } else if (parsedTokens.containsKey("HIGHPASS") || parsedTokens.containsKey("HP")) {
            passbandType = PassbandType.HIGHPASS;
        } else if (parsedTokens.containsKey("BANDPASS") || parsedTokens.containsKey("BP")) {
            passbandType = PassbandType.BANDPASS;
        } else if (parsedTokens.containsKey("BANDREJECT") || parsedTokens.containsKey("BR")) {
            passbandType = PassbandType.BANDREJECT;
        }

        if (parsedTokens.containsKey("BUTTERWORTH") || parsedTokens.containsKey("BU")) {
            filterDesign = FilterDesign.Butterworth;
        } else if (parsedTokens.containsKey("C1")) {
            filterDesign = FilterDesign.Chebyshev1;
        } else if (parsedTokens.containsKey("C2")) {
            filterDesign = FilterDesign.Chebyshev2;
        }

        if (parsedTokens.containsKey("CORNERS") || parsedTokens.containsKey("C")) {
            List<Object> parameters = parsedTokens.get("CORNERS");
            if (parameters == null) {
                parameters = parsedTokens.get("C");
            }
            if (parameters == null) {
                valid = false;
            } else if (parameters.isEmpty()) {
                valid = false;
            } else if (parameters.size() == 1) {
                double v = (double) parameters.get(0);
                switch (passbandType) {
                case LOWPASS:
                    f2 = v;
                    f1 = 0.0;
                    break;
                case HIGHPASS:
                    f1 = v;
                    f2 = 0.0;
                    break;
                case BANDPASS:
                case BANDREJECT:
                    valid = false;
                }

            } else if (parameters.size() > 1) {
                double v1 = (double) parameters.get(0);
                double v2 = (double) parameters.get(1);
                switch (passbandType) {
                case LOWPASS:
                    f2 = v1;
                    f1 = 0.0;
                    break;
                case HIGHPASS:
                    f1 = v1;
                    f2 = 0.0;
                    break;
                case BANDPASS:
                case BANDREJECT:
                    f1 = v1;
                    f2 = v2;
                    break;
                }
            }
        }

        if (parsedTokens.containsKey("NPOLES")) {
            List<Object> parameters = parsedTokens.get("NPOLES");
            if (!parameters.isEmpty()) {
                npoles = (int) parameters.get(0);
            } else {
                valid = false;
            }
        }

        if (parsedTokens.containsKey("N")) {
            List<Object> parameters = parsedTokens.get("N");
            if (!parameters.isEmpty()) {
                npoles = (int) parameters.get(0);
            } else {
                valid = false;
            }
        }

        if (parsedTokens.containsKey("PASSES")) {
            List<Object> parameters = parsedTokens.get("PASSES");
            if (!parameters.isEmpty()) {
                npasses = (int) parameters.get(0);
            } else {
                valid = false;
            }
        }

        if (parsedTokens.containsKey("P")) {
            List<Object> parameters = parsedTokens.get("P");
            if (!parameters.isEmpty()) {
                npasses = (int) parameters.get(0);
            } else {
                valid = false;
            }
        }

        if (parsedTokens.containsKey("ATTEN")) {
            List<Object> parameters = parsedTokens.get("ATTEN");
            if (!parameters.isEmpty()) {
                attenuation = (double) parameters.get(0);
            } else {
                valid = false;
            }
        }

        if (parsedTokens.containsKey("A")) {
            List<Object> parameters = parsedTokens.get("A");
            if (!parameters.isEmpty()) {
                attenuation = (double) parameters.get(0);
            } else {
                valid = false;
            }
        }

        if (parsedTokens.containsKey("TRANBW")) {
            List<Object> parameters = parsedTokens.get("TRANBW");
            if (!parameters.isEmpty()) {
                transitionBandwidth = (double) parameters.get(0);
            } else {
                valid = false;
            }
        }

        if (parsedTokens.containsKey("T")) {
            List<Object> parameters = parsedTokens.get("T");
            if (!parameters.isEmpty()) {
                transitionBandwidth = (double) parameters.get(0);
            } else {
                valid = false;
            }
        }

        List<Object> parameters = parsedTokens.get(TokenListParser.LEFT_OVER_TOKENS);

        if ((parameters != null) && !parameters.isEmpty()) {
            delta = Double.parseDouble((String) parameters.get(0));
        }

        System.out.println();

    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = { "FILTERDESIGN", "FD" };
        return new ArrayList<>(Arrays.asList(names));
    }

    @Override
    public String getHelpString() {

        return "SUMMARY\n"
                + "Produces a graphic display of an IIR filter's digital and analog prototype characteristics:  amplitude, phase, group delay\n"
                + "and impulse response.\n"
                + "\n"
                + "SYNTAX\n"
                + "\n"
                + "[F]ILTER[D]ESIGN [PRINT [pname]] [FILE [prefix]] [filter options] [delta]\n"
                + "  where 'filter options' are the same as those used in the LOWPASS, HIGHPASS, BANDPASS and BANDREJECT commands:\n"
                + "{[L]OW[P]ASS | [H]IGH[P]ASS] | [B]AND[P]ASS | [B]AND[R]EJECT]} {[BU]TTERWORTH | C1 | C2}\n"
                + "      {[C]ORNERS v1 v2}\n"
                + "      {[N]POLES n}\n"
                + "      {[P]ASSES n}\n"
                + "      {[T]RANBW v}\n"
                + "      {[A]TTEN v}\n"
                + "\n"
                + "INPUT\n"
                + " PRINT {pname}:     Prints the resulting plot to the printer named by pname, or to the default printer if pname is not specified.\n"
                + " FILE {prefix}      Writes three SAC files to disk.  These files contain the digital responses evaluated by this command:\n"
                + "  [prefix].spec      is of type IAMPH, and contains both the amplitude and phase information of the filter. \n"
                + "  [prefix].gd        is of type IGRP\n"
                + "  [prefix].imp       is of type ITIME and contains the impulse response.\n"
                + "                    In each of these SAC files, the user header fields are set as follows:\n"
                + " user0:             passband code\n"
                + "                     1: lowpass\n"
                + "                     2: highpass\n"
                + "                     3: bandpass\n"
                + "                     4: bandreject\n"
                + " user1:             filter type code\n"
                + "                     1: Butterworth\n"
                + "                     3: Chebyshev Type 1\n"
                + "                     4: Chebyshev Type 2\n"
                + " user2:             number of poles\n"
                + " user3:             number of passes\n"
                + " user4:             transition bandwidth (used for Chebyshev types 1 and 2 filters\n"
                + " user5:             attenuation (used for Chebyshev types 1 and 2 filters\n"
                + " user6:             sampling interval (delta) in seconds\n"
                + " user7:             first corner (UNDEFINED if lowpass)\n"
                + " user8:             second corner (UNDEFINED if highpass)\n"
                + " kuser0:            passband type (lowpass, highpass, bandpass, or bandrej)\n"
                + " kuser1:            prototype (Butter, Bessel, C1, C2)\n"
                + "\n"
                + "DEFAULT VALUES\n"
                + "\n"
                + " filter type        BUTTERWORTH\n"
                + " bandpass type      LP\n"
                + " CORNERS            1.0 5.0\n"
                + " NPOLES             4\n"
                + " PASSES             1\n"
                + " TRANBW             0.5\n"
                + " ATTEN              100.0\n"
                + " delta              0.025\n"
                + "\n"
                + "DESCRIPTION\n"
                + " The FILTERDESIGN command is implemented through the OregonDSP package.  OregonDSP implements the standard recursive\n"
                + " (IIR) filter designs through bilinear transformation of prototype analog filters.  These prototype filters are created\n"
                + " in a two-step process.  A lowpass analog filter of the specified type and order (number of poles) is generated, then\n"
                + " mapped to lowpass of specified cutoff, highpass, bandpass or bandreject analog filter using analog spectral transformations.\n"
                + " Finally, the mapped analog prototype is transformed to a digital filter via the bilinear transformation for a specified\n"
                + " sampling interval (rate).  FILTERDESIGN displays digital filter responses as solid lines and analog responses as dashed lines.\n"
                + " On color monitors, digital curves are blue, while analog curves are amber.\n"
                + "\n"
                + "HEADER CHANGES\n"
                + "\n"
                + "none\n";

    }

}
