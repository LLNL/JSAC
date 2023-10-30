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
package gov.llnl.gnem.jsac.plots.plot1;

import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.llnl.gnem.jsac.dataAccess.dataObjects.SacPlotData;
import gov.llnl.gnem.jsac.dataAccess.dataObjects.SacTraceData;
import gov.llnl.gnem.jsac.plots.CLabel;
import gov.llnl.gnem.jsac.plots.PLabel;
import gov.llnl.gnem.jsac.plots.PlotAxes;
import gov.llnl.gnem.jsac.plots.PlotColors;
import gov.llnl.gnem.jsac.plots.PlotDrawingData;
import gov.llnl.gnem.jsac.plots.PlotUtils;
import gov.llnl.gnem.jsac.plots.SharedPlotPanel;
import gov.llnl.gnem.jsac.plots.TimeMode;
import gov.llnl.gnem.jsac.plots.Xlimits;
import gov.llnl.gnem.jsac.plots.picking.KeyPhaseMapper;
import gov.llnl.gnem.jsac.plots.picking.KeyPhaseMapper.PhaseUsage;
import gov.llnl.gnem.jsac.plots.picking.PickConfigFrame;
import gov.llnl.gnem.jsac.plots.picking.PickData;
import gov.llnl.gnem.jsac.plots.picking.PickPhaseState;
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
import llnl.gnem.dftt.core.gui.plotting.jmultiaxisplot.JPlotKeyReleasedMessage;
import llnl.gnem.dftt.core.gui.plotting.jmultiaxisplot.JSubplot;
import llnl.gnem.dftt.core.gui.plotting.jmultiaxisplot.PickMovedState;
import llnl.gnem.dftt.core.gui.plotting.jmultiaxisplot.VPickLine;
import llnl.gnem.dftt.core.gui.plotting.plotobject.DataText;
import llnl.gnem.dftt.core.gui.plotting.plotobject.FractionalPinnedText;
import llnl.gnem.dftt.core.gui.plotting.plotobject.Line;
import llnl.gnem.dftt.core.gui.plotting.plotobject.PlotObject;
import llnl.gnem.dftt.core.util.PairT;

public class Plot1Panel extends SharedPlotPanel implements Observer {

    private static final Logger log = LoggerFactory.getLogger(Plot1Panel.class);

    private static Plot1Panel INSTANCE = null;
    private final Map<Line, SacTraceData> lineTraceMap;
    private final Map<Line, JSubplot> lineSubplotMap;
    private final Map<VPickLine, PickData> vplDataMap;
    private final Map<SacTraceData, Double> traceTimeCorrectionMap;

    public static Plot1Panel getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Plot1Panel();
        }
        return INSTANCE;
    }

    private Plot1Panel() {
        super(JMultiAxisPlot.XAxisType.Standard);
        lineTraceMap = new HashMap<>();
        lineSubplotMap = new HashMap<>();
        vplDataMap = new HashMap<>();
        traceTimeCorrectionMap = new HashMap<>();
        addPlotObjectObserver(this);
        setplotSpacing(1);
    }

    public void plotDataBlock(List<SacTraceData> data, TimeMode timeMode, int offset) {
        configurePlotFromPrefs();
        clear();
        double spacing = PlotAxes.getInstance().getSubplotSpacing();
        if (data.size() >= 100) {
            spacing = 0;
        }
        setplotSpacing(spacing);
        lineTraceMap.clear();
        lineSubplotMap.clear();
        vplDataMap.clear();
        traceTimeCorrectionMap.clear();
        switch (timeMode) {
            case ABSOLUTE:
            case RELATIVE:
                this.setXaxisType(XAxisType.Standard);
                break;
            case EPOCH:
                this.setXaxisType(XAxisType.EpochTime);
        }
        SacTraceData earliest = PlotUtils.getEarliestTrace(data); // This is the trace with the earliest begin time (epoch time)
        Double referenceTime = earliest.getReferenceTime();
        if (referenceTime == null) {
            referenceTime = 0.0;
        }

        for (int j = 0; j < data.size(); ++j) {
            JSubplot sp = addSubplot();
            sp.setGenerateLineSelectionRegion(false);
            SacTraceData td = data.get(j);
            SacPlotData spd = td.getPlotData();
            if (spd.hasNoData()) {
                continue;
            }
            if (spd.isFrequencyDomainData()) {
                log.warn("The PLOT1 command cannot be used on spectral data.");
                continue;
            }
            spd.maybeApplyFloorToYvalues();
            spd.maybeApplyXLimits(td);
            if (spd.hasNoData()) {
                continue;
            }
            float[] data2 = spd.getYValues();
            Line line = null;
            double markerCorrection = 0;
            if (null != timeMode) {
                SacTraceData reference = td;
                switch (timeMode) {
                    case EPOCH: {
                        Double beginTime = spd.getBeginTimeAsEpochTime();
                        Double delta = spd.getDelta();
                        if (beginTime != null && delta != null) {
                            line = (Line) sp.Plot(beginTime, delta, data2);
                        } else {
                            log.warn("Unable to plot trace in Epoch time mode.");
                            continue;
                        }
                        PlotUtils.addTextToPlotVerticalOrientation(0, td, reference, sp, j + offset, 0.0, line.getColor());
                        markerCorrection = td.getSACHeader().getReferenceTime();
                        break;
                    }
                    case ABSOLUTE:
                        reference = earliest;
                    case RELATIVE: {
                        float[] xValues = spd.getMaybeShiftedXValues(timeMode, referenceTime);
                        markerCorrection = spd.getxShiftForRelativeMode();
                        line = (Line) sp.Plot(xValues, spd.getYValues());
                        line.setColor(PlotColors.getInstance().getLineColor());
                        switch (timeMode) {
                            case ABSOLUTE:
                            case RELATIVE:
                                PlotUtils.addTextToPlotVerticalOrientation(0, td, reference, sp, j + offset, 0.0, line.getColor());
                                break;
                        }

                        break;
                    }
                    default:
                        break;
                }
            }
            traceTimeCorrectionMap.put(td, markerCorrection);
            PlotUtils.addOriginReferenceLine(td, sp, markerCorrection);
            Collection<PickData> cpd = PlotUtils.addPicks(td, sp, markerCorrection);
            for (PickData pd : cpd) {
                vplDataMap.put(pd.getVpl(), pd);
            }
            if (line != null) {
                line.setPenStyle(PlotDrawingData.getInstance().getPenStyle());
                PlotUtils.setYAxisScale(sp);
                lineTraceMap.put(line, td);
                lineSubplotMap.put(line, sp);

                sp.getPlotRegion().setBackgroundColor(PlotColors.getInstance().getBackGroundColor());
                PlotUtils.maybeSetYlimits(sp);
                PlotUtils.setYlabelAttributes(sp);
                PlotUtils.setSubplotAttributes(sp);
            }
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

        }

        setAllXlimits();
        Xlimits limits = PlotAxes.getInstance().getXlimits();
        if (limits.isLimitsOn()) {
            PartialDataWindow pdw = limits.getPdw();
            setAllXlimits(pdw.getStartOffset(), pdw.getEndOffset());
        }
        PlotUtils.setXlabelAttributes(this);
        PlotUtils.setXscale(this);
        PlotUtils.setXaxisColor(this);
        PlotUtils.renderTitle(this);
        PlotUtils.renderBorder(this);
        repaint();
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
            handleJPlotKeyMessage(obj);

        } else if (obj instanceof JPlotKeyReleasedMessage) {
            handleJPlotKeyReleased(obj);
        } else if (obj instanceof PickMovedState) {
            PickMovedState pms = (PickMovedState) obj;

            double deltaT = pms.getDeltaT();
            VPickLine vpl = pms.getPickLine();
            PickData pd = vplDataMap.get(vpl);
            if (pd != null) {
                PlotUtils.updateHeaderForMovedPick(pd, deltaT);
            }
        } else if (obj instanceof PickCreationInfo) {
            PickCreationInfo pci = (PickCreationInfo) obj;
            Double markerCorrection = getMarkerCorrection(pci);
            PlotUtils.handlePickCreationInfo(this, lineTraceMap, vplDataMap, pci, markerCorrection);
        }
    }

    private double getMarkerCorrection(PickCreationInfo pms) {
        PlotObject po = pms.getSelectedObject();
        if (po instanceof Line) {
            Line line = (Line) po;
            SacTraceData sacData = lineTraceMap.get(line);
            if (sacData != null) {
                Double markerCorrection = traceTimeCorrectionMap.get(sacData);
                if (markerCorrection != null) {
                    return markerCorrection;
                }
            }
        }
        return 0;
    }

    private boolean handleJPlotKeyMessage(Object obj) {
        JPlotKeyMessage msg = (JPlotKeyMessage) obj;
        KeyEvent e = msg.getKeyEvent();
        if (e.getKeyChar() == '+') {
            zoomInAroundMouse(msg);
            return true;
        } else if (e.getKeyChar() == KeyEvent.VK_DELETE) {
            PlotObject po = msg.getPlotObject();
            if (po != null) {
                PlotUtils.maybeDeletePick(this, msg.getSubplot(), vplDataMap, po);
            }
        }

        if (e.getID() == KeyEvent.KEY_PRESSED) {
            PhaseUsage pu = KeyPhaseMapper.getInstance().getMappedPhase(e.getKeyChar());

            if (pu != null && pu.isEnable()) {
                PickPhaseState.getInstance().setCurrentPhase(pu.getPhase());
                PickConfigFrame.getInstance().setSelectedPhaseUsage(pu);
                setMouseMode(MouseMode.CREATE_PICK);
            }
        }
        return false;
    }

    private void handleJPlotKeyReleased(Object obj) {
        JPlotKeyReleasedMessage msg = (JPlotKeyReleasedMessage) obj;
        KeyEvent e = msg.getKeyEvent();
        if (e.getID() == KeyEvent.KEY_RELEASED) {
            PickConfigFrame.getInstance().unsetSelectedPhaseUsage();
            setMouseMode(MouseMode.SELECT_ZOOM);
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
