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
package gov.llnl.gnem.jsac.util;

import llnl.gnem.dftt.core.waveform.merge.NamedIntWaveform;
import llnl.gnem.dftt.core.waveform.seismogram.BasicSeismogram;

public class WaveformUtils {

    public static String createName(BasicSeismogram waveform) {
        StringBuilder sb = new StringBuilder();

        if (waveform.getStreamKey().getNet() != null) {
            sb.append(waveform.getStreamKey().getNet()).append(".");
        }
        sb.append(waveform.getStreamKey().getSta()).append(".");
        sb.append(waveform.getStreamKey().getChan()).append(".");
        sb.append(waveform.getStreamKey().getLocationCode()).append(".");
        sb.append(waveform.getTimeAsDouble());
        return sb.toString();
    }

    public static String createName(NamedIntWaveform waveform) {
        StringBuilder sb = new StringBuilder();

        if (waveform.getKey().getNet() != null) {
            sb.append(waveform.getKey().getNet()).append(".");
        }
        sb.append(waveform.getKey().getSta()).append(".");
        sb.append(waveform.getKey().getChan()).append(".");
        sb.append(waveform.getKey().getLocationCode()).append(".");
        sb.append(waveform.getStart());
        return sb.toString();
    }
}
