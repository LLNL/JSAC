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

public enum SyntheticsType {
	
	IRLDTA(  49, "Real data" ),
	IWPP(   110, "WPP synthetic" ),
	ISW4(   111, "SW4 synthetic" ),
	SPFMG(  112, "SpecFEM 3D Globe synthetic" ),
	SPFM3D( 113, "SpecFEM 3D Cartesian synthetic" );

	
    private final int    code;
    private final String description;
	
	
    private SyntheticsType( int code, String description ) {
        this.code = code;
        this.description = description;
    }
	

    public static SyntheticsType getSyntheticsTypeFromCode( int code ) {
        for ( SyntheticsType st : SyntheticsType.values()) {
            if ( st.code == code ) {
                return st;
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
