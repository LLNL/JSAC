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
package gov.llnl.gnem.jsac.plots;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import gov.llnl.gnem.jsac.dataAccess.dataObjects.SacPlotData;
import gov.llnl.gnem.jsac.dataAccess.dataObjects.SacTraceData;
import gov.llnl.gnem.jsac.dataAccess.dataObjects.SpectralData;
import gov.llnl.gnem.jsac.io.SACHeader;
import gov.llnl.gnem.jsac.plots.FileIDInfo.FieldLayout;
import gov.llnl.gnem.jsac.plots.FileIDInfo.Location;
import gov.llnl.gnem.jsac.plots.FileIDInfo.Type;
import gov.llnl.gnem.jsac.plots.picking.PhasePanel;
import gov.llnl.gnem.jsac.plots.picking.PickConfigFrame;
import gov.llnl.gnem.jsac.plots.picking.PickData;
import gov.llnl.gnem.jsac.plots.picking.PickPhaseState;
import gov.llnl.gnem.jsac.util.PartialDataWindow;
import llnl.gnem.dftt.core.gui.plotting.AxisScale;
import llnl.gnem.dftt.core.gui.plotting.HorizAlignment;
import llnl.gnem.dftt.core.gui.plotting.HorizPinEdge;
import llnl.gnem.dftt.core.gui.plotting.MouseMode;
import llnl.gnem.dftt.core.gui.plotting.PickCreationInfo;
import llnl.gnem.dftt.core.gui.plotting.TickMetrics;
import llnl.gnem.dftt.core.gui.plotting.VertAlignment;
import llnl.gnem.dftt.core.gui.plotting.VertPinEdge;
import llnl.gnem.dftt.core.gui.plotting.ZoomLimits;
import llnl.gnem.dftt.core.gui.plotting.jmultiaxisplot.JMultiAxisPlot;
import llnl.gnem.dftt.core.gui.plotting.jmultiaxisplot.JPlotKeyMessage;
import llnl.gnem.dftt.core.gui.plotting.jmultiaxisplot.JSubplot;
import llnl.gnem.dftt.core.gui.plotting.jmultiaxisplot.JWindowRegion;
import llnl.gnem.dftt.core.gui.plotting.jmultiaxisplot.PlotAxis;
import llnl.gnem.dftt.core.gui.plotting.jmultiaxisplot.VPickLine;
import llnl.gnem.dftt.core.gui.plotting.jmultiaxisplot.XAxis;
import llnl.gnem.dftt.core.gui.plotting.jmultiaxisplot.YAxis;
import llnl.gnem.dftt.core.gui.plotting.plotobject.Line;
import llnl.gnem.dftt.core.gui.plotting.plotobject.PinnedText;
import llnl.gnem.dftt.core.gui.plotting.plotobject.PlotObject;
import llnl.gnem.dftt.core.gui.plotting.transforms.Coordinate;
import llnl.gnem.dftt.core.util.PairT;
import llnl.gnem.dftt.core.util.SeriesMath;
import llnl.gnem.dftt.core.util.StreamKey;

public class PlotUtils {

    public static double addTextToPlotVerticalOrientation(double startOffset,
            SacTraceData sacData,
            SacTraceData referenceSacData,
            JSubplot subplot,
            int fileNumber,
            Double markerCorrection,
            Color color) {
        if (!FileIDInfo.getInstance().isOn()) {
            return 0;
        }
        Location location = FileIDInfo.getInstance().getLocation();
        Type type = FileIDInfo.getInstance().getType();
        List<String> result = createTextFields(type, sacData, referenceSacData, location);
        if (startOffset == 0) {
            startOffset = FileIDInfo.getInstance().getFontSize() / 4;
        }
        double vOffset = startOffset;
        double increment = FileIDInfo.getInstance().getFontSize() / 4 + FileIDInfo.getInstance().getSpacingAdjustment();
        HorizPinEdge horizPinEdge = HorizPinEdge.RIGHT;
        VertPinEdge vertPinEdge = VertPinEdge.TOP;
        HorizAlignment horizAlignment = HorizAlignment.RIGHT;
        switch (location) {
            case UR: {
                horizPinEdge = HorizPinEdge.RIGHT;
                vertPinEdge = VertPinEdge.TOP;
                horizAlignment = HorizAlignment.RIGHT;
                break;
            }

            case UL: {
                horizPinEdge = HorizPinEdge.LEFT;
                vertPinEdge = VertPinEdge.TOP;
                horizAlignment = HorizAlignment.LEFT;
                break;
            }

            case LR: {
                horizPinEdge = HorizPinEdge.RIGHT;
                vertPinEdge = VertPinEdge.BOTTOM;
                horizAlignment = HorizAlignment.RIGHT;
                break;
            }
            case LL: {
                horizPinEdge = HorizPinEdge.LEFT;
                vertPinEdge = VertPinEdge.BOTTOM;
                horizAlignment = HorizAlignment.LEFT;
                break;
            }
        }
        if (type == Type.NAME) {
            String text = createTextString("NAME", sacData.getFilename().getFileName().toString(), FileIDInfo.getInstance().getFormat());
            plotText(subplot, text, vOffset, horizPinEdge, vertPinEdge, horizAlignment, color);
            vOffset += increment;
        } else {
            FieldLayout layout = FileIDInfo.getInstance().getFieldLayout();
            if (layout == FieldLayout.VERTICAL) {
                for (String text : result) {
                    plotText(subplot, text, vOffset, horizPinEdge, vertPinEdge, horizAlignment, color);
                    vOffset += increment;
                }
            } else {
                StringBuilder sb = new StringBuilder();
                String delimiter = FileIDInfo.getInstance().getFieldDelimiter();
                ArrayList<String> items = new ArrayList<>(result);
                for (int j = 0; j < items.size(); ++j) {
                    String item = items.get(j);
                    sb.append(item);
                    if (j < items.size() - 1) {
                        sb.append(delimiter);
                    }
                }

                plotText(subplot, sb.toString(), vOffset, horizPinEdge, vertPinEdge, horizAlignment, color);
                vOffset += increment;
            }
        }
        return vOffset;
    }

    public static void addTextToPlot(SacTraceData sacData, JSubplot subplot, int fileNumber, Double markerCorrection, Color color) {
        addTextToPlotVerticalOrientation(0.0, sacData, sacData, subplot, fileNumber, markerCorrection, color);
    }

    public static PinnedText plotText(JSubplot subplot,
            String textString,
            double vOffset,
            HorizPinEdge horizPinEdge,
            VertPinEdge vertPinEdge,
            HorizAlignment horizalignment,
            Color color) {
        boolean textVisible = true;
        PinnedText text = new PinnedText(5.0,
                vOffset,
                textString,
                horizPinEdge,
                vertPinEdge,
                FileIDInfo.getInstance().getFontName(),
                FileIDInfo.getInstance().getFontSize(),
                color,
                horizalignment,
                VertAlignment.CENTER);

        text.setVisible(textVisible);
        subplot.AddPlotObject(text);
        return text;
    }

    public static String buildIdString(StreamKey key) {
        StringBuilder sb = new StringBuilder();
        if (key.getAgency() != null && !key.getAgency().isEmpty()) {
            sb.append(key.getAgency());
        }
        String net = key.getNet();
        if (net != null && !net.isEmpty()) {
            sb.append(" ");
            sb.append(net);
            Integer netDate = key.getNetJdate();
            if (netDate != null) {
                sb.append(" (").append(netDate).append(")");
            }
        }
        sb.append(" ");

        String sta = key.getSta();
        if (sta != null && !sta.isEmpty()) {
            sb.append(sta).append(" ");
        }
        String chan = key.getChan();
        if (chan != null && !chan.isEmpty()) {
            sb.append(chan).append(" ");
        }
        String locid = key.getLocationCode();
        if (locid != null && !locid.isEmpty()) {
            sb.append("(").append(locid).append(")");
        }
        return sb.toString().trim();
    }

    public static Collection<PickData> addPicks(SacTraceData sacData, JSubplot plot, double markerCorrection) {
        Collection<PickData> result = new ArrayList<>();
        SACHeader header = sacData.getSacFileHeader();

        for (int j = 0; j < header.getT().length; ++j) {
            Double t = header.getT()[j];
            if (t == null) {
                continue;
            }
            String k = header.getKt()[j];
            if (k == null) {
                k = "";
            }
            String headerVariable = "T" + j;
            VPickLine vpl = addSingleTimePick(t, k, plot, markerCorrection);
            PickData pd = new PickData(plot, vpl, sacData, headerVariable);
            result.add(pd);
        }
        Double a = header.getA();
        if (a != null) {
            String k = header.getKa();
            if (k == null) {
                k = "";
            }
            VPickLine vpl = addSingleTimePick(a, k, plot, markerCorrection);
            PickData pd = new PickData(plot, vpl, sacData, "A");
            result.add(pd);
        }

        Double f = header.getF();
        if (f != null) {
            VPickLine vpl = addSingleTimePick(f, "F", plot, markerCorrection);
            PickData pd = new PickData(plot, vpl, sacData, "F");
            result.add(pd);
        }

        return result;
    }

    public static VPickLine addSingleTimePick(double time, String label, JSubplot plot, double markerCorrection) {

        if (label == null) {
            label = "";
        }

        double yAxisFraction = 0.75;
        VPickLine vpl = new VPickLine(time + markerCorrection, yAxisFraction, label);
        vpl.setColor(Color.black);
        vpl.setSelectable(true);
        vpl.setDraggable(true);
        plot.AddPlotObject(vpl);
        return vpl;
    }

    public static void addOriginReferenceLine(SacTraceData sacData, JSubplot plot, double timeCorrection) {
        Double offset = sacData.getOmarkerTime();
        if (offset != null) {
            double markerTime = offset;
            String label = String.format("%s", "O");
            double yAxisFraction = 0.8;
            VPickLine vpl = new VPickLine(markerTime + timeCorrection, yAxisFraction, label);
            vpl.setColor(Color.black);
            vpl.setSelectable(false);
            plot.AddPlotObject(vpl);
        }
    }

    public static PairT<Double, Double> getTimeMinMax(SacTraceData std) {
        double minVal = Double.MAX_VALUE;
        double maxVal = -minVal;

        Double b = std.getSACHeader().getB();
        Double e = std.getSACHeader().getE();
        if (b != null && b < minVal) {
            minVal = b;
        }
        if (e != null && e > maxVal) {
            maxVal = e;
        }

        Xlimits limits = PlotAxes.getInstance().getXlimits();
        if (limits.isLimitsOn()) {
            PartialDataWindow pdw = limits.getPdw();
            minVal = maybeSetMinVal(pdw, std, minVal);

            maxVal = maybeSetMaxVal(pdw, std, maxVal);
        }
        if (PlotAxes.getInstance().getXscale() == Scale.LOG) {
            double floor = PlotDrawingData.getInstance().getFloorValue();
            if (minVal < floor) {
                minVal = floor;
            }
        }
        return new PairT<>(minVal, maxVal);
    }

    public static double maybeSetMaxVal(PartialDataWindow pdw, SacTraceData std, double maxVal) {
        String endRef = pdw.getEndReference();
        if (endRef != null) {

            Object obj = std.getSACHeader().getValue(endRef);
            if (obj != null) {
                maxVal = pdw.getEndOffset() + (Double) obj;
            }
        } else if (endRef == null) {
            maxVal = pdw.getEndOffset();
        }
        return maxVal;
    }

    public static double maybeSetMinVal(PartialDataWindow pdw, SacTraceData std, double minVal) {
        String startRef = pdw.getStartReference();
        if (startRef != null) {
            Object obj = std.getSACHeader().getValue(startRef);
            if (obj != null) {
                minVal = pdw.getStartOffset() + (Double) obj;
            }
        } else if (startRef == null) {
            minVal = pdw.getStartOffset();
        }
        return minVal;
    }

    public static void setXlabelAttributes(JMultiAxisPlot aplot) {
        aplot.getXaxis().setLabelFontSize(PlotAxes.getInstance().getXlabelFontSize());
        boolean visible = PlotAxes.getInstance().isShowXlabel();
        aplot.getXaxis().setLabelVisible(visible);
        if (visible) {
            String label = PlotAxes.getInstance().getXlabel();
            if (label != null) {
                aplot.getXaxis().setLabelText(label);
            }
        }
    }

    public static void setYlabelAttributes(JSubplot subplot) {
        subplot.getYaxis().setLabelOffset(14.0);
        subplot.getYaxis().setLabelFontSize(PlotAxes.getInstance().getYlabelFontSize());
        boolean visible = PlotAxes.getInstance().isShowYlabel();
        subplot.getYaxis().setLabelVisible(visible);
        if (visible) {
            String label = PlotAxes.getInstance().getYlabel();
            if (label != null) {
                subplot.getYaxis().setLabelText(label);
            }
        }
    }

    public static void maybeApplyFloorToYvalues(float[] data) {
        if (PlotAxes.getInstance().getYscale() == Scale.LOG) {
            float minVal = (float) PlotDrawingData.getInstance().getFloorValue();
            for (int j = 0; j < data.length; ++j) {
                data[j] = data[j] >= minVal ? data[j] : minVal;
            }

        }
    }

    public static void setYAxisScale(JSubplot subplot) {
        if (PlotAxes.getInstance().getYscale() == Scale.LOG) {
            subplot.getCoordinateTransform().setYScale(AxisScale.LOG);
        } else {
            subplot.getCoordinateTransform().setYScale(AxisScale.LINEAR);
        }
    }

    public static PairT<Double, float[]> maybeTrimForLogDisplay(double plotStartTime, float[] data, double deltaT) {
        if (PlotAxes.getInstance().getXscale() == Scale.LOG) {
            double floor = PlotDrawingData.getInstance().getFloorValue();
            if (plotStartTime < floor) {
                int numPointsToSkip = (int) Math.round((floor - plotStartTime) / deltaT);
                int remainingNpts = data.length - numPointsToSkip;
                float[] reduced = new float[remainingNpts];
                System.arraycopy(data, numPointsToSkip, reduced, 0, remainingNpts);
                data = reduced;
                plotStartTime = floor;
            }
        }
        return new PairT<>(plotStartTime, data);
    }

    public static void setXscale(JMultiAxisPlot aplot) {
        if (PlotAxes.getInstance().getXscale() == Scale.LOG) {
            aplot.getCoordinateTransform().setXScale(AxisScale.LOG);
        } else {
            aplot.getCoordinateTransform().setXScale(AxisScale.LINEAR);
        }
    }

    public static void renderBorder(JMultiAxisPlot aplot) {
        aplot.setShowBorder(PlotBorderData.getInstance().isDrawPlotBorder());
        aplot.getPlotBorder().setBackgroundColor(PlotBorderData.getInstance().getBorderColor());

        aplot.getPlotRegion().setDrawBox(PlotAxes.getInstance().isDrawBoxAroundPlots()); // This box is drawn around all the subplots
        aplot.getPlotRegion().setBackgroundColor(PlotColors.getInstance().getBackGroundColor()); // Plot interior below subplot drawing regions.
        aplot.setPlotBorderWidthMM(PlotBorderData.getInstance().getBorderWidthMM());
    }

    public static void setXaxisColor(JMultiAxisPlot aplot) {
        aplot.getXaxis().setAxisColor(PlotColors.getInstance().getSkeletonColor());
        aplot.getXaxis().setTickFontColor(PlotColors.getInstance().getSkeletonColor());

    }

    public static void setSubplotAttributes(JSubplot sp) {
        sp.getPlotRegion().setFillRegion(true);
        sp.getYaxis().setAxisColor(PlotColors.getInstance().getSkeletonColor());
        sp.getYaxis().setTickFontColor(PlotColors.getInstance().getSkeletonColor());
        sp.getPlotRegion().setDrawBox(PlotBorderData.getInstance().isDrawSubplotBorders());

    }

    public static void renderTitle(JMultiAxisPlot aplot) {
        String title = PlotAxes.getInstance().getTitle();
        if (title != null && PlotAxes.getInstance().isShowTitle()) {
            aplot.getTitle().setText(title);
            aplot.getTitle().setFontSize(PlotAxes.getInstance().getTitleFontSize());
        }

    }

    public static void rescaleLines(Collection<Line> lines, double factor) {
        for (Line line : lines) {
            float[] data = line.getYdata();
            double mean = SeriesMath.getMean(data);
            SeriesMath.RemoveMean(data);
            SeriesMath.MultiplyScalar(data, factor);
            data = SeriesMath.Add(data, mean);
            line.replaceYarray(data);
        }
    }

    public static void scaleAllTraces(double xmin, double xmax, Map<Line, JSubplot> lineSubplotMap) {

        Collection<Line> lines = lineSubplotMap.keySet();

        for (Line line : lines) {
            float[] data = line.getYdata();
            if (data != null && data.length > 1) {
                int idx1 = 0;
                int idx2 = data.length - 1;

                double delta = line.getIncrement();
                double lineStartTime = line.getXBegin();
                if (xmin > lineStartTime) {
                    idx1 = (int) (Math.round(xmin - lineStartTime) / delta);
                }
                if (xmax < line.getXEnd()) {
                    idx2 = Math.min((int) (Math.round(xmax - lineStartTime) / delta), data.length - 1);
                }
                if (idx2 > idx1 + 1) {
                    double ymin = Double.MAX_VALUE;
                    double ymax = -ymin;
                    for (int j = idx1; j <= idx2; ++j) {
                        if (data[j] < ymin) {
                            ymin = data[j];
                        }
                        if (data[j] > ymax) {
                            ymax = data[j];
                        }
                    }
                    JSubplot plot = lineSubplotMap.get(line);
                    if (plot != null) {
                        plot.setYlimits(ymin, ymax);
                    }

                }
            }
        }

    }

    public static void zoomInAroundMouse(JMultiAxisPlot aPlot, JPlotKeyMessage msg) {
        Coordinate coord = msg.getCurrentCoord();
        JSubplot plot = msg.getSubplot();
        if (plot != null) {
            YAxis yaxis = plot.getYaxis();
            if (yaxis != null) {
                double ymin = yaxis.getMin();
                double ymax = yaxis.getMax();
                double yrange4 = (ymax - ymin) / 4;
                XAxis xaxis = plot.getXaxis();
                if (xaxis != null) {
                    double xmin = xaxis.getMin();
                    double xmax = xaxis.getMax();
                    double xrange4 = (xmax - xmin) / 4;
                    ZoomLimits zl = new ZoomLimits(coord.getWorldC1() - xrange4, coord.getWorldC1() + xrange4, coord.getWorldC2() - yrange4, coord.getWorldC2() + yrange4);

                    aPlot.zoomToNewLimits(zl);
                    aPlot.repaint();
                }
            }
        }
    }

    public static void setYAxisLimits(JSubplot subplot, float min, float max, AxisScale scale) {
        Scale aScale = scale == AxisScale.LINEAR ? Scale.LINEAR : Scale.LOG;
        setYAxisLimits(aScale, min, max, subplot);
    }

    public static void setYAxisLimits(JSubplot subplot, float min, float max, Scale scale) {
        setYAxisLimits(scale, min, max, subplot);
    }

    public static void setYAxisLimits(JSubplot subplot, float[] data, Scale yScale) {
        float min = SeriesMath.getMin(data);
        float max = SeriesMath.getMax(data);
        setYAxisLimits(yScale, min, max, subplot);
    }

    private static void setYAxisLimits(Scale yScale, float min, float max, JSubplot subplot) {
        if (yScale == Scale.LOG) {
            if (min <= PlotDrawingData.getInstance().getFloorValue()) {
                min = (float) PlotDrawingData.getInstance().getFloorValue();
            }
            int v = (int) Math.floor(Math.log10(min));
            min = (float) Math.pow(10.0, v);
            v = (int) Math.round(Math.ceil(Math.log10(max)));
            max = (float) Math.pow(10.0, v);
        } else {
            TickMetrics ticks = PlotAxis.defineAxis(min, max);
            max = (float) ticks.getMax();
            min = (float) ticks.getMin();
        }
        subplot.getYaxis().setMin(min);
        subplot.getYaxis().setMax(max);
    }

    public static void setXlimits(JMultiAxisPlot aplot, JSubplot subplot, double min, double max, Scale xScale) {
        if (xScale == Scale.LOG) {
            if (min <= PlotDrawingData.getInstance().getFloorValue()) {
                min = (float) PlotDrawingData.getInstance().getFloorValue();
            }
            int v = (int) Math.floor(Math.log10(min));
            min = (float) Math.pow(10.0, v);
            v = (int) Math.round(Math.ceil(Math.log10(max)));
            max = (float) Math.pow(10.0, v);
        } else {
            TickMetrics ticks = PlotAxis.defineAxis(min, max);
            max = ticks.getMax();
            min = ticks.getMin();
        }
        subplot.getXaxis().setMin(min);
        subplot.getXaxis().setMax(max);
        aplot.getXaxis().setMin(min);
        aplot.getXaxis().setMax(max);
    }

    public static PairT<Double, Double> getGlobalLimits(Collection<SacTraceData> files) {
        double minVal = Double.MAX_VALUE;
        double maxVal = -minVal;
        List<PairT<Double, Double>> minMaxList = files.parallelStream().map((std) -> getMinMax(std)).filter(Objects::nonNull).collect(Collectors.toList());
        for (PairT<Double, Double> ptd : minMaxList) {
            double min = ptd.getFirst();
            double max = ptd.getSecond();
            if (min < minVal) {
                minVal = min;
            }
            if (max > maxVal) {
                maxVal = max;
            }
        }

        return new PairT<>(minVal, maxVal);
    }

    private static PairT<Double, Double> getMinMax(SacTraceData std) {
        if (!std.isPlottable()) {
            return null;
        }
        SacPlotData spd = std.getPlotData();
        float[] data = null;
        if (!spd.isFrequencyDomainData()) {
            data = std.getData();
        } else {
            SpectralData dft = std.getSpectralData();
            data = getSpectralData(dft);
        }
        double min = Double.MAX_VALUE;
        double max = -min;
        for (float v : data) {
            if (v < min) {
                min = v;
            }
            if (v > max) {
                max = v;
            }
        }
        return new PairT<>(min, max);
    }

    private static float[] getSpectralData(SpectralData dft) {
        SpectralData.PresentationFormat fmt = dft.getPresentationFormat();
        if (fmt == SpectralData.PresentationFormat.AmplitudePhase) {
            return dft.getPositiveFreqAmplitudeArray();
        } else {
            return dft.getPositiveFreqRealArray();
        }
    }

    public static void maybeSetYlimits(JSubplot subplot) {
        Ylimits ylimits = PlotAxes.getInstance().getYlimits();
        if (ylimits.isLimitsOn()) {
            PairT<Double, Double> range = ylimits.nextLimit();
            if (range != null) {
                subplot.getYaxis().setMin(range.getFirst());
                subplot.getYaxis().setMax(range.getSecond());
            }
        }
    }

    private static String createTextString(String field, Object obj, FileIDInfo.Format format) {
        String formattedObject = formatObject(obj);
        switch (format) {
            case EQUALS:
                return field + " = " + formattedObject;
            case COLON:
                return field + " : " + formattedObject;
            case NONAMES:
                return formattedObject;
        }
        return field;
    }

    private static String formatObject(Object obj) {
        String result = obj.toString();
        if (obj instanceof Integer) {
            return String.format("%d", (Integer) obj);
        } else if (obj instanceof Double) {
            return String.format("%8.4g", (Double) obj);
        } else if (obj instanceof Float) {
            return String.format("%8.4f", (Float) obj);
        }
        return result;
    }

    public static float[] trimData(float[] data, double startOffset, double lengthSeconds, double delta) {
        int idx1 = 0;
        if (startOffset >= delta) {
            idx1 = (int) Math.round(startOffset / delta);
        }
        int npts = (int) Math.round(lengthSeconds / delta) + 1;
        int idx2 = idx1 + npts - 1;
        if (idx2 > data.length - 1) {
            idx2 = data.length - 1;
        }
        npts = idx2 - idx1 + 1;
        float[] result = new float[npts];
        System.arraycopy(data, idx1, result, 0, npts);
        return result;
    }

    public static void updateHeaderForMovedPick(PickData pd, double deltaT) {
        SacTraceData std = pd.getStd();
        SACHeader sac = std.getSACHeader();
        String headerField = pd.getHeaderVariable();
        if (headerField.equals("A")) {
            Double v = sac.getA();
            if (v != null) {
                sac.setA(v + deltaT);
            }

        } else if (headerField.equals("F")) {
            Double v = sac.getF();
            if (v != null) {
                sac.setF(v + deltaT);
            }
        } else if (headerField.startsWith("T") && headerField.length() == 2) {
            String tmp = headerField.substring(1);
            int idx = Integer.parseInt(tmp);
            Double v = sac.getT(idx);
            if (v != null) {
                sac.setT(idx, v + deltaT);
            }
        }
    }

    public static void deleteHeaderPick(PickData pd) {
        SacTraceData std = pd.getStd();
        SACHeader sac = std.getSACHeader();
        String headerField = pd.getHeaderVariable();
        if (headerField.equals("A")) {
            sac.setA(null);
            sac.setKa(null);
        } else if (headerField.equals("F")) {
            sac.setF(null);
        } else if (headerField.startsWith("T") && headerField.length() == 2) {
            String tmp = headerField.substring(1);
            int idx = Integer.parseInt(tmp);
            sac.setT(idx, null);
        }
    }

    public static void handlePickCreationInfo(JMultiAxisPlot aPlot,
            Map<Line, SacTraceData> lineDataMap,
            Map<VPickLine, PickData> vplDataMap,
            PickCreationInfo pci,
            double markerCorrection) {
        JSubplot subplot = pci.getOwningPlot();
        PlotObject po = pci.getSelectedObject();
        if (po instanceof Line) {
            Line line = (Line) po;
            SacTraceData sacData = lineDataMap.get(line);
            if (sacData != null) {
                Coordinate coord = pci.getCoordinate();
                double pointerXvalue = coord.getWorldC1();
                String phase = PickPhaseState.getInstance().getCurrentPhase();
                if (phase != null) {
                    PhasePanel.PhaseInfo phaseInfo = PickConfigFrame.getInstance().getSelectedPhaseInfo();
                    if (phaseInfo != null) {
                        int value = phaseInfo.getTIndex();
                        Double v = sacData.getSACHeader().getT(value);
                        if (v == null) { // no existing pick so we have to create a VPickLine object
                            sacData.getSACHeader().setT(value, pointerXvalue - markerCorrection);
                            sacData.getSACHeader().setKt(value, phase);
                            VPickLine vpl = PlotUtils.addSingleTimePick(pointerXvalue, phase, subplot, 0);
                            PickData pd = new PickData(subplot, vpl, sacData, "T" + value);
                            vplDataMap.put(vpl, pd);
                            aPlot.repaint();
                        } else {
                            PickData pd = getExistingPickData(vplDataMap, "T" + value, sacData);
                            if (pd != null) {
                                VPickLine vpl = pd.getVpl();
                                vpl.setXval(pointerXvalue);
                                sacData.getSACHeader().setT(value, pointerXvalue - markerCorrection);
                                sacData.getSACHeader().setKt(value, phase);
                                vplDataMap.remove(vpl);
                                vplDataMap.put(vpl, new PickData(subplot, vpl, sacData, "T" + value));
                                aPlot.repaint();
                            }
                        }
                    }
                }
                aPlot.setMouseMode(MouseMode.SELECT_ZOOM);
            }
        }
    }

    private static PickData getExistingPickData(Map<VPickLine, PickData> vplDataMap, String string, SacTraceData sacData) {
        for (PickData pd : vplDataMap.values()) {
            if (pd.getStd() == sacData && pd.getHeaderVariable().equals(string)) {
                return pd;
            }
        }
        return null;
    }

    public static void maybeDeletePick(JMultiAxisPlot aPlot, JSubplot subplot, Map<VPickLine, PickData> vplDataMap, PlotObject po) {
        if (po instanceof JWindowRegion) {
            JWindowRegion jwr = (JWindowRegion) po;
            VPickLine vpl = jwr.getAssociatedPick();
            PickData pd = vplDataMap.remove(vpl);
            if (pd != null) {

                PlotUtils.deleteHeaderPick(pd);
                subplot.DeletePlotObject(vpl);
            }
        }
        aPlot.repaint();
    }

    public static SacTraceData getEarliestTrace(List<SacTraceData> data) {
        double min = Double.MAX_VALUE;
        SacTraceData result = null;
        for (SacTraceData std : data) {
            double v = std.getTime().getEpochTime();
            if (v < min) {
                min = v;
                result = std;
            }
        }
        return result;
    }

    private static List<String> createTextFields(Type type, SacTraceData sacData, SacTraceData referenceSacData, Location location) {
        List<String> fields = FileIDInfo.getInstance().getHdrlist();
        if (type == Type.DEFAULT) {
            fields = FileIDInfo.getDefaultHdrlist();
        } else if (type == Type.NAME) {
            fields = new ArrayList<>();
            fields.add(sacData.getFilename().getFileName().toString());
        }
        if (location == Location.LL || location == Location.LR) {
            Collections.reverse(fields);
        }

        List<String> result = new ArrayList<>();
        for (String field : fields) {

            if (sacData.getSACHeader().isHeaderField(field)) {
                Object obj = sacData.getSACHeader().getValue(field);
                if (field.equalsIgnoreCase("KZDATE") && referenceSacData != null) {
                    obj = referenceSacData.getSACHeader().getValue(field);
                } else if (field.equalsIgnoreCase("KZTIME") && referenceSacData != null) {
                    obj = referenceSacData.getSACHeader().getValue(field);
                }
                if (obj != null) {
                    String text = createTextString(field, obj, FileIDInfo.getInstance().getFormat());
                    result.add(text);
                }
            } else {
                result.add(field);
            }

        }
        return result;
    }

}
