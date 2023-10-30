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
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gov.llnl.gnem.jsac.transfer;

import java.nio.file.Path;
import java.util.Objects;

/**
 *
 * @author dodge1
 */
public class ResponseOptions {

    private final String filename;
    private final Path searchDir;

    public ResponseOptions(String filename, Path searchDir) {
        this.filename = filename;
        this.searchDir = searchDir;
    }

    public String getFilename() {
        return filename;
    }

    public Path getSearchDir() {
        return searchDir;
    }

    @Override
    public String toString() {
        return "ResponseOptions{" + "filename=" + filename + ", searchDir=" + searchDir + '}';
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + Objects.hashCode(this.filename);
        hash = 37 * hash + Objects.hashCode(this.searchDir);
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
        final ResponseOptions other = (ResponseOptions) obj;
        if (!Objects.equals(this.filename, other.filename)) {
            return false;
        }
        return Objects.equals(this.searchDir, other.searchDir);
    }

}
