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

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.llnl.gnem.apps.coda.common.mapping.StaticHtmlLeafletMap;
import gov.llnl.gnem.apps.coda.common.mapping.WMSLayerDescriptor;
import gov.llnl.gnem.apps.coda.common.mapping.api.GeoBox;
import gov.llnl.gnem.apps.coda.common.mapping.api.Location;
import gov.llnl.gnem.jsac.SacDataModel;
import gov.llnl.gnem.jsac.commands.AttributeDescriptor;
import gov.llnl.gnem.jsac.commands.SacCommand;
import gov.llnl.gnem.jsac.commands.TokenListParser;
import gov.llnl.gnem.jsac.commands.ValuePossibilities;
import gov.llnl.gnem.jsac.dataAccess.dataObjects.SacTraceData;
import gov.llnl.gnem.jsac.plots.PlotAxes;
import gov.llnl.gnem.jsac.plots.map.IconFactory;
import gov.llnl.gnem.jsac.util.OnOffFlag;

public class MapCommand implements SacCommand {

    private static final Logger log = LoggerFactory.getLogger(MapCommand.class);

    private static final String PLOTLEGEND_OPT = "PLOTLEGEND";
    private static final String PLOTEVENTS_OPT = "PLOTEVENTS";
    private static final String PLOTSTATIONS_OPT = "PLOTSTATIONS";
    private static final String MAPSCALE_OPT = "MAPSCALE";
    private static final String STANAMES_OPT = "STANAMES";
    private static final String EVENTFILE_OPT = "EVENTFILE";
    private static final String RMEAN_RESIDUAL_OPT = "RMEAN_RESIDUAL";
    private static final String RESIDUAL_OPT = "RESIDUAL";
    private static final String MAGNITUDE_OPT = "MAGNITUDE";
    private static final String SOUTH_OPT = "SOUTH";
    private static final String NORTH_OPT = "NORTH";
    private static final String EAST_OPT = "EAST";
    private static final String WEST_OPT = "WEST";
    private static final String FILE_OPT = "FILE";

    private String requestedPath = "map.html";
    private Boolean loadExtraEvents = Boolean.FALSE;
    private String extraEventsPath = null;

    private Boolean plotStations = Boolean.TRUE;
    private Boolean plotEvents = Boolean.TRUE;
    private Boolean plotScale = Boolean.TRUE;

    private Boolean plotLegend = Boolean.FALSE;
    private Boolean plotStationNames = Boolean.FALSE;

    private Boolean plotRmean = Boolean.FALSE;
    private Boolean plotResiduals = Boolean.FALSE;
    private Boolean plotMagnitude = Boolean.FALSE;

    private Double west = null;
    private Double east = null;
    private Double north = null;
    private Double south = null;
    private Double pxScale = 2.0;

    private static final List<AttributeDescriptor> descriptors = new ArrayList<>();

    //Only needed to trigger the default click handlers that attach station or event IDs to the
    // map shapes
    private static final BiConsumer<Boolean, String> DEFAULT_CALLBACK = (b, s) -> {
    };

    static {
        descriptors.add(new AttributeDescriptor(WEST_OPT, ValuePossibilities.ONE_VALUE, Double.class));
        descriptors.add(new AttributeDescriptor(EAST_OPT, ValuePossibilities.ONE_VALUE, Double.class));
        descriptors.add(new AttributeDescriptor(NORTH_OPT, ValuePossibilities.ONE_VALUE, Double.class));
        descriptors.add(new AttributeDescriptor(SOUTH_OPT, ValuePossibilities.ONE_VALUE, Double.class));

        descriptors.add(new AttributeDescriptor(MAGNITUDE_OPT, ValuePossibilities.ONE_VALUE, OnOffFlag.class));
        descriptors.add(new AttributeDescriptor("MAG", ValuePossibilities.ONE_VALUE, OnOffFlag.class));

        descriptors.add(new AttributeDescriptor(RESIDUAL_OPT, ValuePossibilities.ONE_VALUE, OnOffFlag.class));
        descriptors.add(new AttributeDescriptor("RE", ValuePossibilities.ONE_VALUE, OnOffFlag.class));

        descriptors.add(new AttributeDescriptor(RMEAN_RESIDUAL_OPT, ValuePossibilities.ONE_VALUE, OnOffFlag.class));
        descriptors.add(new AttributeDescriptor("RM", ValuePossibilities.ONE_VALUE, OnOffFlag.class));

        descriptors.add(new AttributeDescriptor(EVENTFILE_OPT, ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("EV", ValuePossibilities.ONE_VALUE, String.class));

        descriptors.add(new AttributeDescriptor(STANAMES_OPT, ValuePossibilities.ONE_VALUE, OnOffFlag.class));
        descriptors.add(new AttributeDescriptor("STAN", ValuePossibilities.ONE_VALUE, OnOffFlag.class));

        descriptors.add(new AttributeDescriptor(MAPSCALE_OPT, ValuePossibilities.ONE_VALUE, OnOffFlag.class));
        descriptors.add(new AttributeDescriptor(PLOTSTATIONS_OPT, ValuePossibilities.ONE_VALUE, OnOffFlag.class));
        descriptors.add(new AttributeDescriptor(PLOTEVENTS_OPT, ValuePossibilities.ONE_VALUE, OnOffFlag.class));
        descriptors.add(new AttributeDescriptor(PLOTLEGEND_OPT, ValuePossibilities.ONE_VALUE, OnOffFlag.class));

        descriptors.add(new AttributeDescriptor(FILE_OPT, ValuePossibilities.ONE_VALUE, String.class));
    }

    @Override
    public void initialize(String[] tokens) {
        Map<String, List<Object>> parsedTokens = TokenListParser.parseTokens(descriptors, tokens).entrySet().stream().collect(Collectors.toMap(e -> e.getKey().toUpperCase(), Entry::getValue));

        if (parsedTokens.containsKey(WEST_OPT)) {
            west = (Double) parsedTokens.get(WEST_OPT).get(0);
        }
        if (parsedTokens.containsKey(EAST_OPT)) {
            east = (Double) parsedTokens.get(EAST_OPT).get(0);
        }
        if (parsedTokens.containsKey(NORTH_OPT)) {
            north = (Double) parsedTokens.get(NORTH_OPT).get(0);
        }
        if (parsedTokens.containsKey(SOUTH_OPT)) {
            south = (Double) parsedTokens.get(SOUTH_OPT).get(0);
        }

        if (parsedTokens.containsKey(MAGNITUDE_OPT)) {
            plotMagnitude = (Boolean) parsedTokens.get(MAGNITUDE_OPT).get(0);
        } else if (parsedTokens.containsKey("MAG")) {
            plotMagnitude = (Boolean) parsedTokens.get("MAG").get(0);
        }

        if (parsedTokens.containsKey(RESIDUAL_OPT)) {
            plotResiduals = (Boolean) parsedTokens.get(RESIDUAL_OPT).get(0);
        } else if (parsedTokens.containsKey("RE")) {
            plotResiduals = (Boolean) parsedTokens.get("RE").get(0);
        }

        if (parsedTokens.containsKey(RMEAN_RESIDUAL_OPT)) {
            plotRmean = (Boolean) parsedTokens.get(RMEAN_RESIDUAL_OPT).get(0);
        } else if (parsedTokens.containsKey("RM")) {
            plotRmean = (Boolean) parsedTokens.get("RM").get(0);
        }

        if (parsedTokens.containsKey(EVENTFILE_OPT)) {
            loadExtraEvents = Boolean.TRUE;
            extraEventsPath = (String) parsedTokens.get(EVENTFILE_OPT).get(0);
        } else if (parsedTokens.containsKey("EV")) {
            loadExtraEvents = Boolean.TRUE;
            extraEventsPath = (String) parsedTokens.get("EV").get(0);
        }
        if (extraEventsPath == null) {
            loadExtraEvents = Boolean.FALSE;
        }

        if (parsedTokens.containsKey(STANAMES_OPT)) {
            plotStationNames = (Boolean) parsedTokens.get(STANAMES_OPT).get(0);
        } else if (parsedTokens.containsKey("STAN")) {
            plotStationNames = (Boolean) parsedTokens.get("STAN").get(0);
        }

        if (parsedTokens.containsKey(PLOTLEGEND_OPT)) {
            plotLegend = (Boolean) parsedTokens.get(PLOTLEGEND_OPT).get(0);
        }

        if (parsedTokens.containsKey(PLOTEVENTS_OPT)) {
            plotEvents = (Boolean) parsedTokens.get(PLOTEVENTS_OPT).get(0);
        }

        if (parsedTokens.containsKey(PLOTSTATIONS_OPT)) {
            plotStations = (Boolean) parsedTokens.get(PLOTSTATIONS_OPT).get(0);
        }

        if (parsedTokens.containsKey(MAPSCALE_OPT)) {
            plotScale = (Boolean) parsedTokens.get(MAPSCALE_OPT).get(0);
        }

        if (parsedTokens.containsKey(FILE_OPT)) {
            requestedPath = (String) parsedTokens.get(FILE_OPT).get(0);
        }
    }

    @Override
    public void execute() {
        String mapHtml = createMapHTML();
        Path path = writeMapFile(mapHtml);
        if (path != null) {
            openBrowser(path);
        }
    }

    private void openBrowser(Path path) {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.browse(path.toUri());
            } catch (IOException e) {
                log.error(e.getLocalizedMessage(), e);
            }
        } else {
            System.out.println("OS reports that the desktop environment is unavailable, skipping displaying the map.");
        }
    }

    private Path writeMapFile(String mapHtml) {
        try {
            Path filePath = resolveFilePath();
            URI fileURI = filePath.toUri();
            return Files.write(Paths.get(fileURI), mapHtml.getBytes(), StandardOpenOption.CREATE);
        } catch (IOException e) {
            log.error(e.getLocalizedMessage(), e);
            return null;
        }
    }

    protected Path resolveFilePath() {
        Path filePath = Paths.get(requestedPath);
        if (filePath.toFile().isDirectory() || FilenameUtils.getExtension(filePath.toString()).isEmpty()) {
            filePath = filePath.resolve("map.html");
        } else if (!filePath.toString().toLowerCase().endsWith(".html")) {
            filePath = filePath.resolveSibling(filePath.getFileName() + ".html");
        }
        return filePath;
    }

    private String createMapHTML() {
        StaticHtmlLeafletMap map = createMapFromOptions();
        String mapHtml = map.getHtml();
        return mapHtml;
    }

    protected StaticHtmlLeafletMap createMapFromOptions() {
        List<SacTraceData> data = SacDataModel.getInstance().getData();
        String title = PlotAxes.getInstance().getTitle();

        StaticHtmlLeafletMap map = new StaticHtmlLeafletMap();
        Double mean = null;
        double min = 0.0;
        double max = 0.0;

        for (SacTraceData trace : data) {
            if (plotStations && trace.getSACHeader().getKstnm() != null && trace.hasStationInfo()) {
                String stationName = trace.getSACHeader().getKnetwk();
                if (stationName != null) {
                    stationName = stationName.trim() + " ";
                } else {
                    stationName = "";
                }
                stationName = stationName + trace.getSACHeader().getKstnm();
                map.addIcon(IconFactory.stationIcon(new Location(trace.getStla(), trace.getStlo()), stationName).setIconSelectionCallback(DEFAULT_CALLBACK).setShouldBeAnnotated(plotStationNames));
            }
            if (plotEvents && trace.hasEventInfo()) {
                Double size = null;
                if ((plotMagnitude || plotRmean || plotResiduals) && trace.getSACHeader().getKuser0() != null) {
                    size = Double.valueOf(trace.getSACHeader().getKuser0());
                    if (size == null) {
                        size = 1.0;
                    }
                    if (plotResiduals || plotRmean) {
                        size = Math.abs(size);
                    }
                    if (mean != null) {
                        mean = mean + size / 2.0;
                        if (size > max) {
                            max = size;
                        } else if (size < min) {
                            min = size;
                        }
                    } else {
                        mean = size;
                        min = size;
                        max = size;
                    }
                }
                String eventId = trace.getKevnm();
                if ((eventId == null || eventId.trim().isEmpty()) && (trace.getEvid() != null)) {
                    eventId = Long.toString(trace.getEvid());
                }
                if (size != null) {
                    size = size * pxScale;
                }
                map.addIcon(IconFactory.eventIcon(new Location(trace.getEvla(), trace.getEvlo()), eventId, size).setIconSelectionCallback(DEFAULT_CALLBACK));
            }
        }

        if (plotEvents && loadExtraEvents && extraEventsPath != null) {

            try {
                List<String> entries = Files.readAllLines(Paths.get(new URI(extraEventsPath)));
                for (String entry : entries) {
                    String[] entryValues = entry.split(" ");
                    if (entryValues.length > 1) {
                        double evLat = Double.parseDouble(entryValues[0]);
                        double evLon = Double.parseDouble(entryValues[1]);

                        Double size = null;
                        if (entryValues.length > 2) {
                            Double eventSize = Double.valueOf(entryValues[2]);

                            if ((plotMagnitude || plotRmean || plotResiduals) && eventSize != null) {
                                size = eventSize;
                                if (plotResiduals || plotRmean) {
                                    size = Math.abs(size);
                                }
                                if (mean != null) {
                                    mean = mean + size / 2.0;
                                    if (size > max) {
                                        max = size;
                                    } else if (size < min) {
                                        min = size;
                                    }
                                } else {
                                    mean = size;
                                    min = size;
                                    max = size;
                                }
                            }
                        }

                        if (size != null) {
                            size = size * pxScale;
                        }
                        map.addIcon(IconFactory.eventIcon(new Location(evLat, evLon), UUID.randomUUID().toString(), size));
                    }
                }
            } catch (NullPointerException | IOException | URISyntaxException e) {
                log.debug(e.getLocalizedMessage(), e);
            }
        }

        if (plotRmean) {
            map.subtractValueFromEventIconSize(mean);
        }

        if (title != null) {
            map.setTitle(title);
        }

        map.setPlotScale(plotScale);

        if (plotLegend) {
            double[] sizes = new double[5];
            double diff = max - min;
            if (diff > 0.0) {
                for (int i = 0; i < sizes.length; i++) {
                    sizes[i] = min + (diff * (i / (sizes.length - 1.0)));
                }
            } else {
                Arrays.fill(sizes, 5.0);
            }
            map.setEventLegendScaleValues(sizes);
        }

        boolean boundsExist = false;

        if (west != null) {
            boundsExist = true;
            if (east == null) {
                east = west + 1.0;
            }
        }

        if (east != null) {
            boundsExist = true;
            if (west == null) {
                west = east - 1.0;
            }
        }

        if (north != null) {
            boundsExist = true;
            if (south == null) {
                south = north - 1.0;
            }
        }

        if (south != null) {
            boundsExist = true;
            if (north == null) {
                north = south + 1.0;
            }
        }

        if (boundsExist) {
            map.setBounds(new GeoBox(east, south, west, north));
        }

        map.addLayer(new WMSLayerDescriptor("https://basemap.nationalmap.gov/arcgis/services/USGSTopo/MapServer/WMSServer?", "USGS Topo", Collections.singletonList("0")));
        map.activateWmsLayer("USGS Topo");

        map.addLayer(new WMSLayerDescriptor("https://worldwind27.arc.nasa.gov/wms/virtualearth?", "MSVE Hybrid", Collections.singletonList("ve-h")));
        map.addLayer(new WMSLayerDescriptor("https://worldwind27.arc.nasa.gov/wms/virtualearth?", "MSVE Aerial", Collections.singletonList("ve-a")));

        map.addLayerToMap(new WMSLayerDescriptor("http://ows.mundialis.de/services/service?", "Mundalis OSM", Collections.singletonList("TOPO-OSM-WMS")));

        return map;
    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = { "MAP" };
        return new ArrayList<>(Arrays.asList(names));
    }

    @Override
    public String getHelpString() {
        return "MAP\r\n"
                + "SUMMARY\r\n"
                + "Generate an HTML map which can include station/event symbols, topography,\r\n"
                + "and station names using all the files in SAC memory and an optional event file specified on the\r\n"
                + "command line. Event symbols can be scaled according to magnitude, residual, etc. \r\n"
                + "The result of this command is an html file and a screen display of that file.\r\n"
                + "\r\n"
                + "SYNTAX\r\n"
                + "MAP \r\n"
                + "{WEST minlon} {EAST maxlon} {NORTH maxlat} {SOUTH minlat}\r\n"
                + "{MAGnitude | REsidual | RMean_residual}\r\n"
                + "{EVentfile filename} {STANames}\r\n"
                + "{MAPSCALE on|off} {PLOTSTATIONS on|off} {PLOTEVENTS on|off}\r\n"
                + "{PLOTLEGEND on|off} {LEGENDXY x y} {FILE output-file}\r\n"
                + "Note Shorthand notations for keywords are in capital letters.\r\n"
                + "\r\n"
                + "INPUT OPTIONS\r\n"
                + "The following options allow the user to specify the map region. The default is to use the min and\r\n"
                + "max defined by the plotted stations and events.\r\n"
                + "WEST: Define minimum longitude for map window.\r\n"
                + "EAST: Define maximum longitude for map window.\r\n"
                + "NORTH: Define maximum latitude for map window.\r\n"
                + "SOUTH: Define minimum latitude for map window.\r\n"
                + "\r\n"
                + "The following options allow the user to add locations and annotations to the map.\r\n"
                + "STANames: on | off [ Off ]\r\n"
                + "MAPSCALE: on | off [ On ] Plot a Distance Scale on the Map\r\n"
                + "PLOTSTATIONS: on | off [ On ] Plot all the Stations from seismograms\r\n"
                + "PLOTEVENTS: on | off [ On ] Plot all the Events from eventfile and/or seismograms\r\n"
                + "\r\n"
                + "The following options allow the user to scale the event symbols. The default is a constant symbol\r\n"
                + "size.\r\n"
                + "MAGnitude: Scale event symbols linearly with user0. [ Off ]\r\n"
                + "REsidual: Scale event symbols linearly with abs(user0). [ Off ] Positive values are (+)\r\n"
                + "negatives are (-).\r\n"
                + "\r\n"
                + "RMean_residual: Same as residual except mean is removed [ Off ] from all the residuals.\r\n"
                + "\r\n"
                + "PLOTLEGEND: on | off [ Off ] Plot a legend for Earthquake Magnitudes and Residuals\r\n"
                + "\r\n"
                + "EVENTFILE: Specify an ASCII text file containing additional event data. Each\r\n"
                + "line in the file contains data for a single event. The first two columns of each line must\r\n"
                + "contain latitude and longitude (in degrees), respectively. The third column is optional\r\n"
                + "and contains symbol size information (e.g., magnitudes, depth, travel-time residual,\r\n"
                + "...). The following is an example of a few lines in an eventfile:\r\n"
                + "38.5 42.5 6.5\r\n"
                + "25.5 37.3 5.5\r\n"
                + "44.2 40.9 5.7\r\n"
                + "\r\n"
                + "A TITLE can be specified using the TITLE command.\r\n"
                + "\r\n"
                + "The default output file is map.html. An alternative file name can be specified using the FILE option.\r\n"
                + "\r\n"
                + "DEFAULT VALUES\r\n"
                + "MAP STAN off FILE map.html PLOTSTATIONS on PLOTEVENTS on\r\n"
                + "\r\n"
                + "HEADER DATA\r\n"
                + "Station latitudes (stla) and longitudes (stlo) must be set. If event latitudes (evla) and longitudes\r\n"
                + "(evlo) are set they will be included in the map.\r\n"
                + "\r\n"
                + "The results of each MAP command are written to a file in the current directory called map.html unless \r\n"
                + "otherwise specified using the FILE argument.\r\n"
                + "The results of each MAP command will automatically be displayed. \r\n"
                + "The default program used to create the display is the default handler for HTML files specified \r\n"
                + "by the host operating system.";
    }
}
