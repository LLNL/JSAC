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
package gov.llnl.gnem.jsac.io.enums;

public enum DepVarType {
	
	IUNKN(   5, "unknown" ),
	IDISP(   6, "displacement in nm" ),
	IVEL (   7, "velocity in nm/sec" ),
	IACC (   8, "acceleration in nm/sec/sec" ),
	IVOLTS( 50, "velocity in volts" );
	
	
	
    private final int    code;
    private final String description;

	
	
    private DepVarType( int code, String description ) {
        this.code        = code;
        this.description = description;
    }
	

    public static DepVarType getDepVarTypeFromCode( int code ) {
        for ( DepVarType dep : DepVarType.values()) {
            if ( dep.code == code ) {
                return dep;
            }
        }
        return null;
    }
    

    public int getCode() {
        return code;
    }
    

    public String getDescription() {
        return description;
    }


}
