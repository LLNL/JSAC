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

package gov.llnl.gnem.jsac.plots.plot;

import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.HashMap;
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
import gov.llnl.gnem.jsac.plots.picking.KeyPhaseMapper;
import gov.llnl.gnem.jsac.plots.picking.KeyPhaseMapper.PhaseUsage;
import gov.llnl.gnem.jsac.plots.picking.PickConfigFrame;
import gov.llnl.gnem.jsac.plots.picking.PickData;
import gov.llnl.gnem.jsac.plots.picking.PickPhaseState;
import java.util.logging.Level;
import llnl.gnem.dftt.core.gui.plotting.HorizAlignment;
import llnl.gnem.dftt.core.gui.plotting.HorizPinEdge;
import llnl.gnem.dftt.core.gui.plotting.MouseMode;
import llnl.gnem.dftt.core.gui.plotting.MouseOverPlotObject;
import llnl.gnem.dftt.core.gui.plotting.PickCreationInfo;
import llnl.gnem.dftt.core.gui.plotting.VertAlignment;
import llnl.gnem.dftt.core.gui.plotting.VertPinEdge;
import llnl.gnem.dftt.core.gui.plotting.ZoomLimits;
import llnl.gnem.dftt.core.gui.plotting.jmultiaxisplot.JMultiAxisPlot;
import llnl.gnem.dftt.core.gui.plotting.jmultiaxisplot.JPlotKeyMessage;
import llnl.gnem.dftt.core.gui.plotting.jmultiaxisplot.JPlotKeyReleasedMessage;
import llnl.gnem.dftt.core.gui.plotting.jmultiaxisplot.JSubplot;
import llnl.gnem.dftt.core.gui.plotting.jmultiaxisplot.PickMovedState;
import llnl.gnem.dftt.core.gui.plotting.jmultiaxisplot.VPickLine;
import llnl.gnem.dftt.core.gui.plotting.jmultiaxisplot.XAxis;
import llnl.gnem.dftt.core.gui.plotting.jmultiaxisplot.YAxis;
import llnl.gnem.dftt.core.gui.plotting.jmultiaxisplot.ZoomInStateChange;
import llnl.gnem.dftt.core.gui.plotting.jmultiaxisplot.ZoomOutStateChange;
import llnl.gnem.dftt.core.gui.plotting.plotobject.DataText;
import llnl.gnem.dftt.core.gui.plotting.plotobject.FractionalPinnedText;
import llnl.gnem.dftt.core.gui.plotting.plotobject.Line;
import llnl.gnem.dftt.core.gui.plotting.plotobject.PlotObject;
import llnl.gnem.dftt.core.gui.plotting.plotobject.SymbolStyle;
import llnl.gnem.dftt.core.gui.plotting.transforms.Coordinate;
import llnl.gnem.dftt.core.util.ApplicationLogger;
import llnl.gnem.dftt.core.util.SeriesMath;


public class PlotPanel extends SharedPlotPanel implements Observer {

    private static PlotPanel INSTANCE = null;

    public static PlotPanel getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PlotPanel();
        }
        return INSTANCE;
    }

    private final JSubplot subplot;
    private final Map<Line, SacTraceData> lineDataMap;
    private final Map<VPickLine, PickData> vplDataMap;

    private PlotPanel() {
        super(JMultiAxisPlot.XAxisType.Standard);
        lineDataMap = new HashMap<>();
        vplDataMap = new HashMap<>();
        subplot = addSubplot();
        addPlotObjectObserver(this);

    }

    @Override
    public void clear() {

        subplot.Clear();
        lineDataMap.clear();
        vplDataMap.clear();
        repaint();
    }

    public void plotSeismogram(SacTraceData sacData, int fileNumber) {
        clear();
        setXaxisType(XAxisType.Standard);

        if (sacData == null) {
            return;
        }

        SacPlotData spd = sacData.getPlotData();
        if (spd.isFrequencyDomainData()) {
            ApplicationLogger.getInstance().log(Level.WARNING, "The PLOT command cannot be used on spectral data.");
            return;
        }
        spd.maybeApplyFloorToYvalues();

        spd.maybeTrimForLogDisplay();
        spd.maybeApplyXLimits(sacData);
        if (spd.hasNoData()) {
            return;
        }

        Line line = (Line) subplot.Plot(spd.getxValues(), spd.getYValues());
        PlotUtils.setYAxisScale(subplot);
        line.setPenStyle(PlotDrawingData.getInstance().getPenStyle());
        line.setSymbolStyle(SymbolStyle.CIRCLE);
        line.setPlotLineSymbols(true);
        line.setColor(PlotColors.getInstance().getLineColor());
        lineDataMap.put(line, sacData);
        Double markerCorrection = 0.0;
        PlotUtils.addTextToPlotVerticalOrientation(0.0, sacData, sacData, subplot, fileNumber, 0.0, line.getColor());
        PlotUtils.addOriginReferenceLine(sacData, subplot, markerCorrection);
        Collection<PickData> cpd = PlotUtils.addPicks(sacData, subplot, markerCorrection);
        for (PickData pd : cpd) {
            vplDataMap.put(pd.getVpl(), pd);
        }

        PlotUtils.maybeSetYlimits(subplot);

        setAllXlimits(spd.getB(), spd.getE());
        PlotUtils.setYlabelAttributes(subplot);
        PlotUtils.setXlabelAttributes(this);
        PlotUtils.setXscale(this);
        subplot.getPlotRegion().setBackgroundColor(PlotColors.getInstance().getBackGroundColor());

        PlotUtils.setXaxisColor(this);
        PlotUtils.setSubplotAttributes(subplot);

        PlotUtils.renderTitle(this);
        PlotUtils.renderBorder(this);

        for (CLabel label : PlotAxes.getInstance().getCLabels()) {
            DataText dt = new DataText(label.getxValue(), label.getyValue(),
                    label.getLabelText(), label.getFontName(),
                    label.getFontSize(), label.getTextColor(),
                    label.getHorizAlignment(), label.getVertAlignment());
            subplot.AddPlotObject(dt);
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
                subplot.AddPlotObject(fpt);
            }
        }
        repaint();
    }

    @Override
    public void update(Observable o, Object obj) {
        if (obj instanceof MouseOverPlotObject) {
            MouseOverPlotObject mopo = (MouseOverPlotObject) obj;
            PlotObject po = mopo.getPlotObject();
            if (po instanceof VPickLine) {
                VPickLine vpl = (VPickLine) po;
                PickData pd = vplDataMap.get(vpl);
                if (pd != null) {
                    setToolTipText(pd.toString());
                }
            }
        } else if (obj instanceof MouseMode) {
            // System.out.println( "owner.setMouseModeMessage((MouseMode) obj);");
        } else if (obj instanceof JPlotKeyMessage) {
            handleJPlotKeyMessage(obj);

        } else if (obj instanceof JPlotKeyReleasedMessage) {
            handleJPlotKeyReleased(obj);
        } else if (obj instanceof ZoomInStateChange) {
//            ZoomInStateChange zisc = (ZoomInStateChange) obj;
//            this.getSubplotManager().zoomToBox(zisc.getZoomBounds());
        } else if (obj instanceof ZoomOutStateChange) {
//            this.getSubplotManager().UnzoomAll();
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
            PlotUtils.handlePickCreationInfo(this, lineDataMap, vplDataMap, pci, 0);
        }
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
                PlotUtils.maybeDeletePick(this, subplot, vplDataMap, po);
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

                    this.zoomToNewLimits(zl);
                    this.repaint();
                }
            }
        }
    }

    @Override
    public void magnifyTraces() {
        scaleTraces(2.0);

    }

    private void scaleTraces(double factor) {

        Line[] lines = subplot.getLines();
        for (Line line : lines) {
            float[] data = line.getYdata();
            double mean = SeriesMath.getMean(data);
            SeriesMath.RemoveMean(data);
            SeriesMath.MultiplyScalar(data, factor);
            data = SeriesMath.Add(data, mean);
            line.replaceYarray(data);
        }

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
    public void scaleAllTraces(boolean resetYlimits) {
        double xmin = getXaxis().getMin();
        double xmax = getXaxis().getMax();

        Line[] lines = subplot.getLines();

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
                    subplot.setYlimits(ymin, ymax);

                }
            }
        }
        repaint();
    }

}
