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
package gov.llnl.gnem.jsac.io.css;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.llnl.gnem.jsac.dataAccess.dataObjects.SacTraceData;
import gov.llnl.gnem.jsac.io.Waveform;
import llnl.gnem.dftt.core.util.FileUtil.DriveMapper;
import llnl.gnem.dftt.core.waveform.Wfdisc;
import llnl.gnem.dftt.core.waveform.io.css.PathType;
import llnl.gnem.dftt.core.waveform.io.css.WfdiscReader;
import llnl.gnem.dftt.core.waveform.merge.NamedIntWaveform;

/**
 *
 * @author dodge1
 */
public class CssProcessor {

    private static final Logger log = LoggerFactory.getLogger(CssProcessor.class);

    public static List<SacTraceData> readCSS(String filename, PathType wfdiscPathType) throws Exception {
        Path parent = Paths.get(filename).getParent();
        String userPath = null;
        List<Wfdisc> wfdiscs = WfdiscReader.readSpaceDelimitedWfdiscFile(filename);

        return wfdiscs.parallelStream().map(t -> toSacTraceData(t, parent)).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private static SacTraceData toSacTraceData(Wfdisc row, Path parent) {
        try {
            String dir = DriveMapper.getInstance().maybeMapPath(row.getDir());
            String dfile = row.getDfile();
            File file = new File(dir, dfile);
            int[] data = Waveform.getSeismogramDataAsIntArray(file.getAbsolutePath(), row.getFoff(), row.getNsamp(), row.getDatatype());
            NamedIntWaveform niw = new NamedIntWaveform(-1,
                                                        row.getSta(),
                                                        row.getChan(),
                                                        row.getTime(),
                                                        row.getSamprate(),
                                                        data,
                                                        row.getCalib(),
                                                        row.getCalper(),
                                                        row.getClip(),
                                                        row.getSegtype(),
                                                        row.getInstype());
            return new SacTraceData(niw, parent);
        } catch (Exception ex) {
            log.warn("Failed reading " + row.getDir() + "/" + row.getDfile() + "!", ex);
            return null;
        }
    }
}
