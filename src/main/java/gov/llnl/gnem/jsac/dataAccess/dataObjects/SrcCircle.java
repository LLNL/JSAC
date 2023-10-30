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

package gov.llnl.gnem.jsac.dataAccess.dataObjects;

/**
 *
 * @author dodge1
 */
public class SrcCircle {
    private final double cnterLat;
    private final double centerLon;
    private final double radiusdegrees;

    public SrcCircle(double cnterLat, double centerLon, double radiusdegrees) {
        this.cnterLat = cnterLat;
        this.centerLon = centerLon;
        this.radiusdegrees = radiusdegrees;
    }

    @Override
    public String toString() {
        return "SrcCircle{" + "cnterLat=" + cnterLat + ", centerLon=" + centerLon + ", radiusdegrees=" + radiusdegrees + '}';
    }

    public double getCnterLat() {
        return cnterLat;
    }

    public double getCenterLon() {
        return centerLon;
    }

    public double getRadiusdegrees() {
        return radiusdegrees;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + (int) (Double.doubleToLongBits(this.cnterLat) ^ (Double.doubleToLongBits(this.cnterLat) >>> 32));
        hash = 41 * hash + (int) (Double.doubleToLongBits(this.centerLon) ^ (Double.doubleToLongBits(this.centerLon) >>> 32));
        hash = 41 * hash + (int) (Double.doubleToLongBits(this.radiusdegrees) ^ (Double.doubleToLongBits(this.radiusdegrees) >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SrcCircle other = (SrcCircle) obj;
        if (Double.doubleToLongBits(this.cnterLat) != Double.doubleToLongBits(other.cnterLat)) {
            return false;
        }
        if (Double.doubleToLongBits(this.centerLon) != Double.doubleToLongBits(other.centerLon)) {
            return false;
        }
        if (Double.doubleToLongBits(this.radiusdegrees) != Double.doubleToLongBits(other.radiusdegrees)) {
            return false;
        }
        return true;
    }
    
    
}
