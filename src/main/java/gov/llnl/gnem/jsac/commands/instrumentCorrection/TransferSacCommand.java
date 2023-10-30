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

package gov.llnl.gnem.jsac.commands.instrumentCorrection;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.isti.jevalresp.ChannelMatchPolicy.Policy;

import gov.llnl.gnem.jsac.commands.AttributeDescriptor;
import gov.llnl.gnem.jsac.commands.Misc;
import gov.llnl.gnem.jsac.commands.SacCommand;
import gov.llnl.gnem.jsac.commands.TokenListParser;
import gov.llnl.gnem.jsac.commands.ValuePossibilities;
import gov.llnl.gnem.jsac.transfer.ResponseOptions;
import gov.llnl.gnem.jsac.transfer.ResponseType;
import gov.llnl.gnem.jsac.transfer.TransferProcessor;
import gov.llnl.gnem.jsac.transfer.TransferSubOptionParser;
import gov.llnl.gnem.jsac.util.PathManager;
import gov.llnl.gnem.response.ChannelMatchPolicyHolder;
import gov.llnl.gnem.response.FreqLimits;
import llnl.gnem.dftt.core.util.StreamKey;

/**
 *
 * @author dodge1
 */
public class TransferSacCommand implements SacCommand {

    private static final Logger log = LoggerFactory.getLogger(TransferSacCommand.class);

    private static FreqLimits freqLimits = null;
    private static ResponseType fromType = ResponseType.NONE;
    private static ResponseType toType = ResponseType.NONE;
    private Policy policy = Policy.AGENCY_NET_STA_CHAN_LOCID_EPOCH_MATCH;
    private ResponseOptions fromResponseOptions = null;
    private ResponseOptions toResponseOptions = null;
    private Path fromResponseFilePath = null;
    private Path toResponseFilePath = null;
    private StreamKey substituteKey = null;
    private Double substituteTime = null;

    public static void resetDefaults() {
        freqLimits = null;
        fromType = ResponseType.NONE;
        toType = ResponseType.NONE;
    }

    private static final List<AttributeDescriptor> descriptors = new ArrayList<>();

    static {
        descriptors.add(new AttributeDescriptor("FROM", ValuePossibilities.ONE_OR_MORE, String.class));
        descriptors.add(new AttributeDescriptor("TO", ValuePossibilities.ONE_OR_MORE, String.class));
        descriptors.add(new AttributeDescriptor("FREQLIMITS", ValuePossibilities.FOUR_VALUES, Double.class));
        descriptors.add(new AttributeDescriptor("FREQ", ValuePossibilities.FOUR_VALUES, Double.class));
        descriptors.add(new AttributeDescriptor("PREWHITENING", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("NETWORK", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("NET", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("STATION", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("STA", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("CHANNEL", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("CHAN", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("LOCID", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("TIME", ValuePossibilities.ONE_VALUE, Double.class));
        descriptors.add(new AttributeDescriptor("HTIME", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("POLICY", ValuePossibilities.ONE_VALUE, String.class));
    }

    public TransferSacCommand() {
    }

    @Override
    public void initialize(String[] tokens) {
        ChannelMatchPolicyHolder.getInstance().setPolicy(policy);
        Map<String, List<Object>> parsedTokens = TokenListParser.parseTokens(descriptors, tokens);
        policy = maybeGetPolicy(parsedTokens);
        substituteKey = maybeGetSubstituteKey(parsedTokens);
        substituteTime = maybeGetSubstituteTime(parsedTokens);
        freqLimits = TransferSubOptionParser.maybeGetFreqLimits(parsedTokens);
        List<Object> values = parsedTokens.get("TO");
        if (values != null && values.size() >= 1) {
            try {
                String tmp = (String) values.get(0);
                toType = ResponseType.valueOf(tmp.toUpperCase());
                if (toType != null) {
                    switch (toType) {
                    case POLEZERO:
                        try {
                            toResponseOptions = TransferSubOptionParser.getToOptions(values);
                        } catch (IllegalStateException e) {
                            System.out.println(e.getMessage());
                            throw (e);
                        }
                        break;
                    case CSS:
                        if (values.size() > 1) {
                            String tmpFileName = (String) values.get(1);
                            toResponseFilePath = PathManager.getInstance().resolveAndValidateFile(tmpFileName);
                        } else {
                            System.out.println("Option CSS must be followed by a file name!");
                            throw new IllegalStateException("File name was not specified!");
                        }
                        break;
                    case EVALRESP:
                        try {
                            fromResponseOptions = TransferSubOptionParser.getToOptions(values);
                        } catch (IllegalStateException e) {
                            System.out.println(e.getMessage());
                            throw (e);
                        }
                        break;
                    }
                }
            } catch (IllegalStateException ex) {
                throw new IllegalStateException("Invalid TO type: " + values.get(0));
            }
        }
        values = parsedTokens.get("FROM");
        if (values != null && values.size() >= 1) {
            try {
                String tmp = (String) values.get(0);
                fromType = ResponseType.valueOf(tmp.toUpperCase());
                if (null != fromType) {
                    switch (fromType) {
                    case POLEZERO:
                        try {
                            fromResponseOptions = TransferSubOptionParser.getOptions(values);
                        } catch (IllegalStateException e) {
                            System.out.println(e.getMessage());
                            throw (e);
                        }
                        break;
                    case CSS:
                        if (values.size() > 1) {
                            String tmpFileName = (String) values.get(1);
                            fromResponseFilePath = PathManager.getInstance().resolveAndValidateFile(tmpFileName);
                        } else {
                            System.out.println("Option CSS must be followed by a file name!");
                            throw new IllegalStateException("File name was not specified!");
                        }
                        break;
                    case EVALRESP:
                        try {
                            fromResponseOptions = TransferSubOptionParser.getOptions(values);
                        } catch (IllegalStateException e) {
                            System.out.println(e.getMessage());
                            throw (e);
                        }
                        break;
                    default:
                        break;
                    }
                } else {
                    fromType = ResponseType.NONE;
                }
            } catch (IllegalStateException ex) {
                throw ex;
            }
        }
    }

    @Override
    public void execute() {
        ChannelMatchPolicyHolder.getInstance().setPolicy(policy);
        try {
            TransferProcessor.getInstance().transfer(fromType, toType, freqLimits, fromResponseOptions, fromResponseFilePath, toResponseOptions, toResponseFilePath, substituteKey, substituteTime);
        } catch (FileNotFoundException ex) {
            log.error("Failed executing transfer command! {}", ex);
        }
    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = { "TRANS", "TRANSFER" };
        return new ArrayList<>(Arrays.asList(names));
    }

    @Override
    public String getHelpString() {
        return "SUMMARY\n"
                + "	Performs deconvolution to remove an instrument response and \n"
                + "	convolution to apply another instrument response.\n"
                + "\n"
                + "SYNTAX\n"
                + "\n"
                + "	[TRANS]FER {FROM type {options}} , {TO type {options}} ,\n"
                + "      {FREQlimits f1 f2 f3 f4} \n"
                + "INPUT\n"
                + "\n"
                + "FROM type:	\n"
                + "	Available types are: EVALRESP, POLEZERO, CSS, DBASE\n"
                + "		Type options are:\n"
                + "			Network (Net) Use this network code in finding appropriate transfer function				\n"
                + "			Station (Sta) Use this station code in finding appropriate transfer function				\n"
                + "			Channel (chan) Use this channel code in finding appropriate transfer function				\n"
                + "			Locid Use this location code (KHOLE) in finding appropriate transfer function				\n"
                + "\n"
                + "			TIME      	substitute reference Time specified as epoch time\n"
                + "			HTIME     	substitute reference Time specified as one of (YYYYDDD, YYYY/MM/DD, YYYY/DDD:HH:MM:SS, YYYY/MM/DD:HH:MM:SS)\n"
                + "			The CSS option requires the name of the response file to be specified immediately after the CSS keyword.\n"
                + "			The POLEZERO and EVALRESP options have a sub-option (SUBTYPE {S}) which should be followed by the name of the  file to use.\n"
                + "			If the SUBTYPE sub-option is not used the RESPONSE directory will be searched for files matching pattern \"SAC_PZs_NET_STA_CHAN_LOCID_*\" or \"RESP.<NET>.<STA>.<LOCID>.<CHAN>\";\n"
                + "			The RESPONSE is either the current directory or a directory specified using the DIR sub-option.\n"
                + "TO type:	\n"
                + "	Insert the instrument type by convolution using spectral multiplication.\n"
                + "\n"
                + "FREQLIMITS:	\n"
                + "	Default is it is not used.  \n"
                + "\n"
                + "POLICY:	\n"
                + "	Controls the specificity of the query for response metadata. Allowable values are:\n"
                + "		AGENCY_NET_STA_CHAN_LOCID_TIME (ANSCLT)\n"
                + "		NET_STA_CHAN_LOCID_TIME (NSCLT)\n"
                + "		STA_CHAN_TIME (SCT)\n"
                + "\n"
                + "EXAMPLES\n"
                + "	POLEZERO:\n"
                + "		Name the polezero file using the SUBTYPE (S) option:\n"
                + "			transfer from polezero subtype polezero-file.pz freqlimits 0.05 0.1 9 10\n"
                + "			\n"
                + "		search a named directory (sacpzf) for SAC Polezero files named like:SAC_PZs_NETWORK_STATION_CHANNEL_LOCID_*\n"
                + "			transfer from polezero dir sacpzf to none\n"
                + "			\n"
                + "		search the current directory for SAC Polezero files named like:SAC_PZs_NETWORK_STATION_CHANNEL_LOCID_*\n"
                + "		but override the net-sta-chan-locid values in the header(s)\n"
                + "			transfer from polezero  network IU station YAK channel BHZ locid 00 freq .01 .05 8 9  to none\n"
                + "	\n"
                + "	EVALRESP:\n"
                + "		Name the evalresp file using the SUBTYPE (S) option:\n"
                + "			transfer from evalresp s 2013.148.00.YAK.BHZ.00.6733538.resp freq 0.01 .05 8 9  to none\n"
                + "			\n"
                + "		search a named directory (evresp) for EVALRESP files named like:RESP.<NET>.<STA>.<LOCID>.<CHAN>\n"
                + "			transfer from evalresp dir evresp s RESP.IU.ANMO.00.BHZ freq .01 .05 8 9  to none\n"
                + "			\n"
                + "		search the current directory for evalresp files named like:RESP.<NET>.<STA>.<LOCID>.<CHAN>\n"
                + "		but override the net-sta-chan-locid and the reftime values in the header(s)\n"
                + "			transfer from evalresp network IU station ANMO channel BHZ locid 00    htime 2019001 freq .01 .05 8 9  to none\n"
                + "\n"
                + "	CSS: This option applies to response files from the USNDC such as (FAP,PAZ,PAZFIR)\n"
                + "		Transfer from a named CSS response 			\n"
                + "			transfer from css 1989.025.00.YKW2.be.--.1.resp  freq .01 .05 8 9\n"
                + "\n"
                + "		Transfer from a named CSS response to a named POLEZERO response\n"
                + "			transfer from css cmg3t+q330_lb100c@1 to polezero subtype polezero-file.pz\n"
                + "	DBASE: Looks up the correct response in the associated database table using metadata in the SAC header. \n"
                + "		This option can only be used in the FROM direction and requires\n"
                + "     a provider plugin installed and available for the DBASE processing type.\n"
                + "			transfer from dbase freqlimits 0.05 0.1 9 10\n"
                + "\n"
                + "DEFAULT VALUES:	\n"
                + "	TRANS FROM NONE TO NONE POLICY ANSCLT \n"
                + "	\n"
                + "DESCRIPTION\n"
                + "\n"
                + "	The default input and output \"instrument\" in TRANSFER is displacement, which in SAC is designated as NONE. \n"
                + "	Hence, if a FROM type or a TO type is not specified, SAC assumes it to be NONE. If the output instrument is \n"
                + "	NONE, IDEP in the SAC header is set to DISPLACEMENT (NM) - SAC's convention for displacement. If TRANSFER \n"
                + "	uses TO VEL or TO ACC, the header variable IDEP is changed accordingly for all waveforms in memory.";
    }

    private StreamKey maybeGetSubstituteKey(Map<String, List<Object>> parsedTokens) {

        String net = getStringValue(parsedTokens, "NETWORK", "NET");
        String sta = getStringValue(parsedTokens, "STATION", "STA");
        String chan = getStringValue(parsedTokens, "CHANNEL", "CHAN");
        String locid = getStringValue(parsedTokens, "LOCID", "LOCID");
        if (net != null || sta != null || chan != null || locid != null) {
            return new StreamKey(net, sta, chan, locid);
        } else {
            return null;
        }
    }

    private String getStringValue(Map<String, List<Object>> parsedTokens, String longString, String shortString) {
        String value = null;
        List<Object> values = parsedTokens.remove(longString);
        if (values == null) {
            values = parsedTokens.remove(shortString);
        }
        if (values != null && !values.isEmpty()) {
            value = (String) values.get(0);
        }
        return value;
    }

    private Double maybeGetSubstituteTime(Map<String, List<Object>> parsedTokens) {
        List<Object> values = parsedTokens.remove("TIME");
        if (values != null && !values.isEmpty()) {
            return (Double) values.get(0);
        }
        values = parsedTokens.remove("HTIME");
        if (values != null && !values.isEmpty()) {
            String tmp = (String) values.get(0);
            return Misc.parseTimeString(tmp);
        }
        return null;
    }

    private Policy maybeGetPolicy(Map<String, List<Object>> parsedTokens) {
        String p = getStringValue(parsedTokens, "POLICY", "POLICY");
        if (p != null && !p.isEmpty()) {
            switch (p.toUpperCase()) {
            case "NET_STA_CHAN_LOCID_TIME":
            case "NSCLT":
                return Policy.AGENCY_NET_STA_CHAN_LOCID_EPOCH_MATCH;
            case "AGENCY_NET_STA_CHAN_LOCID_TIME":
            case "ANSCLT":
                return Policy.AGENCY_NET_STA_CHAN_LOCID_EPOCH_MATCH;
            case "STA_CHAN_TIME":
            case "SCT":
                return Policy.STA_CHAN_EPOCH_MATCH;
            }
        }

        return Policy.AGENCY_NET_STA_CHAN_LOCID_EPOCH_MATCH;
    }

}
