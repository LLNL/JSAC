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
package gov.llnl.gnem.jsac.dataAccess;

import java.util.ServiceLoader;

import gov.llnl.gnem.jsac.dataAccess.dataObjects.DataSource;
import gov.llnl.gnem.jsac.dataAccess.interfaces.WaveformRetriever;

public class JsacDAOFactory {

    private static ServiceLoader<WaveformRetrieverFactory> waveformRetrievers = ServiceLoader.load(WaveformRetrieverFactory.class);
    private String username;
    private String password;
    private String url;

    private JsacDAOFactory(String username, String password, String url) {
        this.username = username;
        this.password = password;
        this.url = url;
    }

    public WaveformRetriever getWaveformRetriever(DataSource source) {
        WaveformRetriever provider = null;
        for (WaveformRetrieverFactory retrievers : waveformRetrievers) {
            if (retrievers.available(source)) {
                if (username != null && password != null && url != null) {
                    provider = retrievers.getWaveformRetriever(source, username, password, url);
                } else {
                    provider = retrievers.getWaveformRetriever(source);
                }
                break;
            }
        }
        if (provider == null) {
            throw new IllegalArgumentException("Not implemented yet!");
        }
        return provider;
    }

    public static boolean isInitialized() {
        return instance != null;
    }

    private static JsacDAOFactory instance;

    public static JsacDAOFactory getInstance(String username, String password, String url) {
        if (instance != null) {
            return instance;
        } else {
            instance = new JsacDAOFactory(username, password, url);
            return instance;
        }
    }
}
