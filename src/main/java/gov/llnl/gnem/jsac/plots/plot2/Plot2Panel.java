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

package gov.llnl.gnem.jsac.plots.plot2;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import gov.llnl.gnem.jsac.dataAccess.dataObjects.SacPlotData;
import gov.llnl.gnem.jsac.dataAccess.dataObjects.SacTraceData;
import gov.llnl.gnem.jsac.dataAccess.dataObjects.SpectralData.PresentationFormat;
import gov.llnl.gnem.jsac.plots.CLabel;
import gov.llnl.gnem.jsac.plots.PLabel;
import gov.llnl.gnem.jsac.plots.CLabel;
import gov.llnl.gnem.jsac.plots.PLabel;
import gov.llnl.gnem.jsac.plots.PlotAxes;
import gov.llnl.gnem.jsac.plots.PlotColors;
import gov.llnl.gnem.jsac.plots.PlotDrawingData;
import gov.llnl.gnem.jsac.plots.PlotUtils;
import gov.llnl.gnem.jsac.plots.SharedPlotPanel;
import gov.llnl.gnem.jsac.plots.TimeMode;
import gov.llnl.gnem.jsac.plots.Xlimits;
import gov.llnl.gnem.jsac.util.PartialDataWindow;
import llnl.gnem.dftt.core.gui.plotting.HorizAlignment;
import llnl.gnem.dftt.core.gui.plotting.HorizPinEdge;
import llnl.gnem.dftt.core.gui.plotting.MouseMode;
import llnl.gnem.dftt.core.gui.plotting.MouseOverPlotObject;
import llnl.gnem.dftt.core.gui.plotting.PickCreationInfo;
import llnl.gnem.dftt.core.gui.plotting.PlotObjectClicked;
import llnl.gnem.dftt.core.gui.plotting.VertAlignment;
import llnl.gnem.dftt.core.gui.plotting.VertPinEdge;
import llnl.gnem.dftt.core.gui.plotting.jmultiaxisplot.JMultiAxisPlot;
import llnl.gnem.dftt.core.gui.plotting.jmultiaxisplot.JPlotKeyMessage;
import llnl.gnem.dftt.core.gui.plotting.jmultiaxisplot.JSubplot;
import llnl.gnem.dftt.core.gui.plotting.jmultiaxisplot.PickMovedState;
import llnl.gnem.dftt.core.gui.plotting.jmultiaxisplot.VPickLine;
import llnl.gnem.dftt.core.gui.plotting.plotobject.DataText;
import llnl.gnem.dftt.core.gui.plotting.plotobject.FractionalPinnedText;
import llnl.gnem.dftt.core.gui.plotting.plotobject.Line;
import llnl.gnem.dftt.core.gui.plotting.plotobject.PlotObject;
import llnl.gnem.dftt.core.util.PairT;
import llnl.gnem.dftt.core.util.SeriesMath;

public class Plot2Panel extends SharedPlotPanel implements Observer {

    private static Plot2Panel INSTANCE = null;
    private final Map<Line, SacTraceData> lineTraceMap;
    private final Map<Line, JSubplot> lineSubplotMap;

    public static Plot2Panel getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Plot2Panel();
        }
        return INSTANCE;
    }

    private Plot2Panel() {
        super(JMultiAxisPlot.XAxisType.Standard);
        lineTraceMap = new HashMap<>();
        lineSubplotMap = new HashMap<>();
        addPlotObjectObserver(this);
    }

    public void plotData(List<SacTraceData> data, TimeMode timeMode) {
        clear();
        lineTraceMap.clear();
        lineSubplotMap.clear();
        JSubplot sp = addSubplot();
        sp.setGenerateLineSelectionRegion(false);

        double vOffset = 3;

        float minYval = Float.MAX_VALUE;
        float maxYval = -minYval;
        double minXval = Double.MAX_VALUE;
        double maxXval = -minXval;
        SacTraceData earliest = PlotUtils.getEarliestTrace(data); // This is the trace with the earliest begin time (epoch time)
        Double referenceTime = earliest.getReferenceTime();
        if (referenceTime == null) {
            referenceTime = 0.0;
        }
        for (int j = 0; j < data.size(); ++j) {

            SacTraceData td = data.get(j);
            SacPlotData spd = td.getPlotData();
            if (spd.hasNoData()) {
                continue;
            }
            spd.maybeApplyFloorToYvalues();
            spd.maybeApplyXLimits(td);
            if (spd.hasNoData()) {
                continue;
            }

            float[] data1;
            Color color;
            if (spd.isFrequencyDomainData()) {
                PresentationFormat fmt = spd.getPresentationFormat();
                if (fmt == PresentationFormat.AmplitudePhase) {
                    data1 = spd.getPositiveFreqAmplitudeArray();
                } else {
                    data1 = spd.getPositiveFreqRealArray();
                }
                float[] frequencies = spd.getPositiveFrequencies();
                color = plotSingleLine(j, sp, frequencies, data1, td);
                if (minXval > frequencies[0]) {
                    minXval = frequencies[0];
                }
                double v = frequencies[data1.length - 1];
                if (maxXval < v) {
                    maxXval = v;
                }
            } else {
                float[] xValues = spd.getMaybeShiftedXValues(timeMode, referenceTime);

                data1 = spd.getYValues();
                color = plotSingleLine(j, sp, xValues, data1, td);
                float t0 = xValues[0];
                if (minXval > t0) {
                    minXval = t0;
                }
                float v = xValues[xValues.length - 1];
                if (v > maxXval) {
                    maxXval = v;
                }
            }
            sp.getPlotRegion().setBackgroundColor(PlotColors.getInstance().getBackGroundColor());

            vOffset = PlotUtils.addTextToPlotVerticalOrientation(vOffset, td, earliest, sp, j, 0.0, color) + 1;
            PlotUtils.setYlabelAttributes(sp);
            PlotUtils.setSubplotAttributes(sp);
            float min = SeriesMath.getMin(data1);
            if (min < minYval) {
                minYval = min;
            }
            float max = SeriesMath.getMax(data1);
            if (max > maxYval) {
                maxYval = max;
            }
        }

        // First limits operates if ylim is off. The second when ylim is on.
        PlotUtils.setYAxisLimits(sp, minYval, maxYval, PlotAxes.getInstance().getYscale());
        PlotUtils.maybeSetYlimits(sp);
        PlotUtils.setXlimits(this, sp, minXval, maxXval, PlotAxes.getInstance().getXscale());
        PlotUtils.setXlabelAttributes(this);
        PlotUtils.setXscale(this);
        PlotUtils.setXaxisColor(this);
        PlotUtils.renderTitle(this);
        PlotUtils.renderBorder(this);
        for (CLabel label : PlotAxes.getInstance().getCLabels()) {
            DataText dt = new DataText(label.getxValue(), label.getyValue(),
                    label.getLabelText(), label.getFontName(),
                    label.getFontSize(), label.getTextColor(),
                    label.getHorizAlignment(), label.getVertAlignment());
            sp.AddPlotObject(dt);
        }
        if (PlotAxes.getInstance().isShowPLabels()) {
            for (PLabel label : PlotAxes.getInstance().getpLabels()) {
                FractionalPinnedText fpt = new FractionalPinnedText(label.getxValue(),
                        label.getyValue(),
                        label.getLabelText(),
                        HorizPinEdge.LEFT,
                        VertPinEdge.TOP,
                        label.getFontName(),
                        label.getFontSize(),
                        label.getTextColor(),
                        HorizAlignment.LEFT,
                        VertAlignment.BOTTOM);
                sp.AddPlotObject(fpt);
            }
        }

        repaint();
    }

    private Color plotSingleLine(int j, JSubplot sp, float[] xData, float[] yData, SacTraceData td) {
        Line line = null;
        if (j == 0) {
            line = (Line) sp.Plot(xData, yData);
        } else {
            line = new Line(xData, yData);
            sp.AddPlotObject(line);
        }
        line.setPenStyle(PlotDrawingData.getInstance().getPenStyle());
        PlotUtils.setYAxisScale(sp);
        lineTraceMap.put(line, td);
        lineSubplotMap.put(line, sp);
        Color color = PlotColors.getInstance().getLineColor();
        line.setColor(color);
        return color;
    }

    @Override
    public void update(Observable o, Object obj) {
        if (obj instanceof MouseOverPlotObject) {
            MouseOverPlotObject mopo = (MouseOverPlotObject) obj;
            PlotObject po = mopo.getPlotObject();
            if (po instanceof VPickLine) {
                VPickLine vpl = (VPickLine) po;
            }
        } else if (obj instanceof PlotObjectClicked && this.getMouseMode() != MouseMode.CREATE_PICK) {
            PlotObjectClicked poc = (PlotObjectClicked) obj;
            if (poc.po instanceof Line) {
            } else if (poc.po instanceof VPickLine) {
                VPickLine vpl = (VPickLine) poc.po;
            }

        } else if (obj instanceof MouseMode) {
            // System.out.println( "owner.setMouseModeMessage((MouseMode) obj);");
        } else if (obj instanceof JPlotKeyMessage) {
            JPlotKeyMessage msg = (JPlotKeyMessage) obj;
            KeyEvent e = msg.getKeyEvent();
            if (e.getKeyChar() == '+') {
                zoomInAroundMouse(msg);
                return;
            }
            //       ControlKeyMapper controlKeyMapper = msg.getControlKeyMapper();
            int keyCode = e.getKeyCode();

            PlotObject po = msg.getPlotObject();
            if (keyCode == 127 && po instanceof Line) {
            }

        } else if (obj instanceof PickMovedState) {
            PickMovedState pms = (PickMovedState) obj;
            double deltaT = pms.getDeltaT();
            VPickLine vpl = pms.getPickLine();
        } else if (obj instanceof PickCreationInfo) {
            PickCreationInfo pci = (PickCreationInfo) obj;
            PlotObject po = pci.getSelectedObject();
            if (po instanceof Line) {
                Line line = (Line) po;
            }
        }
    }

    @Override
    public void zoomInAroundMouse(JPlotKeyMessage msg) {
        PlotUtils.zoomInAroundMouse(this, msg);
    }

    @Override
    public void magnifyTraces() {
        scaleTraces(2.0);

    }

    private void scaleTraces(double factor) {
        Collection<Line> lines = lineTraceMap.keySet();
        PlotUtils.rescaleLines(lines, factor);
        this.repaint();
    }

    @Override
    public void reduceTraces() {
        scaleTraces(0.5);
    }

    @Override
    public void autoScaleTraces() {
        scaleAllTraces(false);
    }

    @Override
    public void exportPlot() {
        exportSVG();
    }

    public void printPlot() {
        print();
    }

    @Override
    public void scaleAllTraces(boolean resetYlimits) {
        double xmin = getXaxis().getMin();
        double xmax = getXaxis().getMax();
        PlotUtils.scaleAllTraces(xmin, xmax, lineSubplotMap);
        repaint();
    }

    public PairT<Double, Double> getTimeMinMax(Collection<SacTraceData> traces) {
        double minVal = Double.MAX_VALUE;
        double maxVal = -minVal;
        for (SacTraceData std : traces) {
            Double b = std.getSACHeader().getB();
            Double e = std.getSACHeader().getE();
            if (b != null && b < minVal) {
                minVal = b;
            }
            if (e != null && e > maxVal) {
                maxVal = e;
            }
        }

        Xlimits limits = PlotAxes.getInstance().getXlimits();
        if (limits.isLimitsOn()) {
            PartialDataWindow pdw = limits.getPdw();
            String startRef = pdw.getStartReference();
            String endRef = pdw.getEndReference();
            if (startRef == null && endRef == null) {
                minVal = pdw.getStartOffset();
                maxVal = pdw.getEndOffset();
            } else if (startRef != null && endRef != null) {
                Double minStart = null;
                Double maxEnd = null;
                boolean applyMinValue = true;
                boolean applyMaxValue = true;
                for (SacTraceData std : traces) {
                    Object obj = std.getSACHeader().getValue(startRef);
                    if (obj != null) {
                        Double tmp = (Double) obj + pdw.getStartOffset();
                        if (minStart == null || tmp < minStart) {
                            minStart = tmp;
                        }
                    } else {
                        applyMinValue = false;
                    }
                    obj = std.getSACHeader().getValue(endRef);
                    if (obj != null) {
                        Double tmp = (Double) obj + pdw.getEndOffset();
                        if (maxEnd == null || tmp > maxEnd) {
                            maxEnd = tmp;
                        }
                    } else {
                        applyMaxValue = false;
                    }
                }
                if (applyMinValue && minStart != null) {
                    minVal = minStart;
                }
                if (applyMaxValue && maxEnd != null) {
                    maxVal = maxEnd;
                }
            }
        }

        return new PairT<>(minVal, maxVal);
    }
}
