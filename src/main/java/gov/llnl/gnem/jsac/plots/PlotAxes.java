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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import llnl.gnem.dftt.core.gui.waveform.plotPrefs.PlotPreferenceModel;
import llnl.gnem.dftt.core.gui.waveform.plotPrefs.PlotPresentationPrefs;

public class PlotAxes {

    private final Xlimits xlimits;
    private final Ylimits ylimits;
    private Scale xscale;
    private Scale yscale;
    private boolean showXlabel = false;
    private String xlabel = null;
    private int xlabelFontSize = 10;
    private boolean showTitle = false;
    private String title = null;
    private int titleFontSize = 10;

    private boolean showYlabel = false;
    private String ylabel = null;
    private int ylabelFontSize = 10;
    private final Map<String, Integer> nameSizeMap = new HashMap<>();
    private final Collection<CLabel> cLabels;
    private final List<PLabel> pLabels;
    private boolean showPLabels;
    private double subplotSpacing;
    private boolean showSublotBoxes;
    private final PlotPresentationPrefs prefs;
    private boolean drawBoxAroundPlots;

    private PlotAxes() {
        xlimits = new Xlimits();
        ylimits = new Ylimits();
        xscale = Scale.LINEAR;
        yscale = Scale.LINEAR;
        nameSizeMap.put("HUGE", 30);
        nameSizeMap.put("LARGE", 20);
        nameSizeMap.put("MEDIUM", 13);
        nameSizeMap.put("SMALL", 10);
        nameSizeMap.put("TINY", 8);
        nameSizeMap.put("H", 30);
        nameSizeMap.put("L", 20);
        nameSizeMap.put("M", 13);
        nameSizeMap.put("S", 10);
        nameSizeMap.put("T", 8);

        cLabels = new ArrayList<>();
        pLabels = new ArrayList<>();
        subplotSpacing = 2;
        showSublotBoxes = true;
        drawBoxAroundPlots = false;
        prefs = PlotPreferenceModel.getInstance().getPrefs();
        prefs.getPlotRegionPrefs().setDrawBox(false);
        prefs.getBorderPrefs().setDrawBox(false);
    }

    public static PlotAxes getInstance() {
        return PlotCharacteristicsHolder.INSTANCE;
    }

    public void setShowXlabel(boolean showLabel) {
        showXlabel = showLabel;
    }

    public void setXlabel(String label) {
        xlabel = label;
    }

    public void setXlabelFontSize(String value) {
        Integer tmp = nameSizeMap.get(value);
        if (tmp != null) {
            xlabelFontSize = tmp;
        }
    }

    public void setYlabelFontSize(String value) {
        Integer tmp = nameSizeMap.get(value);
        if (tmp != null) {
            ylabelFontSize = tmp;
        }
    }

    public void setShowYlabel(boolean showLabel) {
        showYlabel = showLabel;
    }

    public void setYlabel(String label) {
        ylabel = label;
    }

    public void setTitleFontSize(String titleSize) {
        Integer size = nameSizeMap.get(titleSize);
        if (size != null) {
            titleFontSize = size;
        }
    }

    public void setShowTitle(boolean value) {
        this.showTitle = value;
    }

    public void setTitle(String value) {
        this.title = value;
    }

    public int getSizeFromString(String size) {
        Integer tmp = nameSizeMap.get(size.toUpperCase());
        return (tmp == null) ? nameSizeMap.get("SMALL") : tmp;
    }

    public int getDefaultFontSize() {
        return nameSizeMap.get("SMALL");
    }

    public void clearPLabel(int index) {
        if (index >= 0 && index < pLabels.size()) {
            pLabels.remove(index);
        } else {
            throw new IllegalStateException("Index (" + (index + 1) + ") is out of bounds!");
        }
    }

    public void clearPLabels() {
        pLabels.clear();
    }

    public PLabel getLastPLabel() {
        if (pLabels.isEmpty()) {
            return null;
        } else {
            return pLabels.get(pLabels.size() - 1);
        }
    }

    public void addPLabel(PLabel label) {
        pLabels.add(label);
    }

    public void replacePLabel(int index, PLabel label) {
        if (index >= 0 && index < pLabels.size()) {
            pLabels.set(index, label);
        } else {
            throw new IllegalStateException("Index (" + (index + 1) + ") is out of bounds!");
        }
    }

    public PLabel getPLabel(int index) {
        if (index >= 0 && index < pLabels.size()) {
            return pLabels.get(index);
        } else {
            throw new IllegalStateException("Index (" + (index + 1) + ") is out of bounds!");
        }
    }

    public void setShowPLabels(boolean showLabel) {
        showPLabels = showLabel;
    }

    public boolean isShowPLabels() {
        return showPLabels;
    }

    public void setSubplotSpacing(Double spacing) {
        subplotSpacing = spacing;
    }

    public double getSubplotSpacing() {
        return subplotSpacing;
    }

    public boolean isShowSublotBoxes() {
        return showSublotBoxes;
    }

    public void setShowSublotBoxes(boolean showSublotBoxes) {
        this.showSublotBoxes = showSublotBoxes;
    }

    boolean isDrawBoxAroundPlots() {
        return drawBoxAroundPlots;
    }

    public void setDrawBoxAroundPlots(boolean drawBoxAroundPlots) {
        this.drawBoxAroundPlots = drawBoxAroundPlots;
    }

    private static class PlotCharacteristicsHolder {

        private static final PlotAxes INSTANCE = new PlotAxes();
    }

    public Xlimits getXlimits() {
        return xlimits;
    }

    public Scale getXscale() {
        return xscale;
    }

    public void setXscale(Scale xscale) {
        this.xscale = xscale;
    }

    public Ylimits getYlimits() {
        return ylimits;
    }

    public Scale getYscale() {
        return yscale;
    }

    public void setYscale(Scale yscale) {
        this.yscale = yscale;
    }

    public boolean isShowXlabel() {
        return showXlabel;
    }

    public String getXlabel() {
        return xlabel;
    }

    public int getXlabelFontSize() {
        return xlabelFontSize;
    }

    public boolean isShowYlabel() {
        return showYlabel;
    }

    public String getYlabel() {
        return ylabel;
    }

    public int getYlabelFontSize() {
        return ylabelFontSize;
    }

    public boolean isShowTitle() {
        return showTitle;
    }

    public String getTitle() {
        return title;
    }

    public int getTitleFontSize() {
        return titleFontSize;
    }

    public void addCLabel(CLabel label) {
        cLabels.add(label);
    }

    public Collection<CLabel> getCLabels() {
        return new ArrayList<>(cLabels);
    }

    public void clearCLabels() {
        cLabels.clear();
    }

    public List<PLabel> getpLabels() {
        return new ArrayList<>(pLabels);
    }

}
