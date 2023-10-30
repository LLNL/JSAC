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
public class MinMax {

    private double minVal;
    private double maxVal;

    public MinMax(double val1, double val2) {
        this.minVal = Math.min(val1, val2);
        this.maxVal = Math.max(val1, val2);
    }

    public double getMinVal() {
        return minVal;
    }

    public double getMaxVal() {
        return maxVal;
    }

    public void multiplyBy(double value) {
        minVal *= value;
        maxVal *= value;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + (int) (Double.doubleToLongBits(this.minVal) ^ (Double.doubleToLongBits(this.minVal) >>> 32));
        hash = 59 * hash + (int) (Double.doubleToLongBits(this.maxVal) ^ (Double.doubleToLongBits(this.maxVal) >>> 32));
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
        final MinMax other = (MinMax) obj;
        if (Double.doubleToLongBits(this.minVal) != Double.doubleToLongBits(other.minVal)) {
            return false;
        }
        if (Double.doubleToLongBits(this.maxVal) != Double.doubleToLongBits(other.maxVal)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "MinMax{" + "minVal=" + minVal + ", maxVal=" + maxVal + '}';
    }

}
