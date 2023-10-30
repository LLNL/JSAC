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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.llnl.gnem.jsac.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 *
 * @author dodge1
 */
public class FlynnEngdahl {

    private final List<String> regionName = new ArrayList<>();
    private final int[] latValues;
    private final int[] lonValues;
    private final int[] grnValues;
    private final Map<Integer, Integer> grnSrnMap;
    private final boolean valid;

    private static FlynnEngdahl instance;

    public static FlynnEngdahl getInstance() {
        try {
            instance = new FlynnEngdahl();
        } catch (Exception ex) {
            instance = new FlynnEngdahl(false);
        }
        return instance;
    }

    private FlynnEngdahl() throws Exception {
        List<String> lines = new ArrayList<>();
        ClassLoader classLoader = getClass().getClassLoader();
        try (InputStream is = classLoader.getResourceAsStream("llnl/gnem/core/util/Geometry/region_lookup.txt")) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                String line;
                while ((line = br.readLine()) != null) {
                    lines.add(line);
                }
            }
        }

        latValues = new int[lines.size()];
        lonValues = new int[lines.size()];
        grnValues = new int[lines.size()];
        for (int j = 0; j < lines.size(); ++j) {
            StringTokenizer st = new StringTokenizer(lines.get(j));
            latValues[j] = Integer.parseInt(st.nextToken());
            lonValues[j] = Integer.parseInt(st.nextToken());
            grnValues[j] = Integer.parseInt(st.nextToken());
        }

        try (InputStream is = classLoader.getResourceAsStream("llnl/gnem/core/util/Geometry/region_name.txt")) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                String line;
                while ((line = br.readLine()) != null) {
                    regionName.add(line);
                }
            }
        }
        grnSrnMap = new HashMap<>();
        try (InputStream is = classLoader.getResourceAsStream("llnl/gnem/core/util/Geometry/grnSrnMap.txt")) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                String line;
                while ((line = br.readLine()) != null) {
                    StringTokenizer st = new StringTokenizer(line);
                    int grn = Integer.parseInt(st.nextToken());
                    int srn = Integer.parseInt(st.nextToken());
                    grnSrnMap.put(grn, srn);
                }
            }
        }
        valid = true;
    }

    private FlynnEngdahl(boolean b) {
        latValues = new int[0];
        lonValues = new int[0];
        grnValues = new int[0];
        grnSrnMap = new HashMap<>();
        valid = b;
    }

    public boolean isValid() {
        return valid;
    }

    private int getGrn(int vLat, int vLon, int nValue, int numQuad) {
        int minord = getMinord(nValue, numQuad, vLat);
        if (minord < numQuad) {
            int minord2 = getMinord2(minord, nValue, vLat, vLon, numQuad);
            return grnValues[minord2 + nValue];
        } else {
            return 0;
        }
    }

    private int getMinord2(int minord, int nValue, int vLat, int vLon, int numQuad) {
        int minord2 = 0;
        for (int j = minord + nValue; j < latValues.length; ++j) {
            if ((latValues[j] == vLat && lonValues[j] <= vLon && j < numQuad + nValue) && (j > minord2)) {
                minord2 = j;
            }
        }
        minord2 -= nValue;
        return minord2;
    }

    private int getMinord(int nValue, int numQuad, int vLat) {
        int minord = 0;
        int tmp = nValue + numQuad;
        for (int j = nValue; j < latValues.length; ++j) {
            if (latValues[j] >= vLat) {
                minord = Math.min(j, tmp);
                break;
            }
        }
        minord -= nValue;

        return minord;
    }

    public String getRegionName(int regionid) {
        return this.valid ? regionName.get(regionid) : "Unknown";
    }

    public String getRegionName(double lat, double lon) {
        if (!valid) {
            return "Unknown";
        } else {
            int grn = getGeoRegion(lat, lon);
            return getRegionName(grn);
        }
    }

    public int getGeoRegion(Double lat, Double lon) {
        if (!valid) {
            return 758;
        }
        int result = 758;
        if (lat >= 0 && lon >= 0) {
            result = getGrn(lat.intValue(), lon.intValue(), 0, 1914);
        } else if (lat >= 0 && lon < 0) {
            result = getGrn(lat.intValue(), Math.abs(lon.intValue()), 1919, 1579);
        } else if (lat < 0 && lon < 0) {
            result = getGrn(Math.abs(lat.intValue()), Math.abs(lon.intValue()), 3535, 1161);
        } else {
            result = getGrn(Math.abs(lat.intValue()), Math.abs(lon.intValue()), 4747, 1304);
        }
        return result <= 0 ? 758 : result;
    }

    public int getSrn(double lat, double lon) {
        return getSrn(getGeoRegion(lat, lon));
    }

    public int getSrn(int grn) {
        if (!valid) {
            return 0;
        }
        Integer result = grnSrnMap.get(grn);
        return result != null ? result : 0;
    }
}
