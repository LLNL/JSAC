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

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author dodge1
 */
public class PlotPanelTab extends JPanel {

    private final PlotToolbar toolbar;
    private final SharedPlotPanel plotPanel;

    public PlotPanelTab(JFrame frame, SharedPlotPanel panel) {
        super(new BorderLayout());

        plotPanel = panel;
        toolbar = new PlotToolbar(frame, plotPanel);
        add(toolbar, BorderLayout.NORTH);
        add(plotPanel, BorderLayout.CENTER);
    }

    public void exportPlot() {
        plotPanel.exportSVG();
    }

    public void printPlot() {
        plotPanel.print();
    }

    public void magnifyTraces() {
        plotPanel.magnifyTraces();
    }

    public void reduceTraces() {
        plotPanel.reduceTraces();
    }

    public void autoScaleTraces() {
        plotPanel.autoScaleTraces();
    }

    public void unzoomAll() {
        plotPanel.unzoomAll();
    }
}
