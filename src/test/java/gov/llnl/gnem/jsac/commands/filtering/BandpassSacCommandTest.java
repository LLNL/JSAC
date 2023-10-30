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
package gov.llnl.gnem.jsac.commands.filtering;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gov.llnl.gnem.jsac.SacDataModel;
import gov.llnl.gnem.jsac.TestUtil;
import gov.llnl.gnem.jsac.dataAccess.dataObjects.SacTraceData;

public class BandpassSacCommandTest {

    private static final String TEST_FILE_DIRECTORY = "gov/llnl/gnem/jsac/commands/filtering/";

    @BeforeAll
    public static void setUpClass() {
        SacDataModel.getInstance().clear();
    }

    @AfterAll
    protected static void tearDownAfterClass() throws Exception {
        SacDataModel.getInstance().clear();
    }

    @BeforeEach
    protected void setUp() throws Exception {
        SacDataModel.getInstance().clear();
        BandpassSacCommand.resetDefaults();
    }

    @AfterEach
    protected void tearDown() throws Exception {
    }

    @Test
    public void testBP_N_4_C_12_14() {
        System.out.println("testBP_N_4_C_12_14");

        TestUtil.loadTestSacAlphaFile("seismogram.txt", TestUtil.SAC_TEST_FILES_DIRECTORY);

        List<SacTraceData> lstd = SacDataModel.getInstance().getData();
        SacTraceData test = lstd.get(0);

        TestUtil.runCommand("BP N 4 C 12 14", new BandpassSacCommand());

        SacTraceData truth = TestUtil.getTruthAlpha("seis.bp.n4.c.12_14.sacalpha", TestUtil.SAC_TEST_FILES_DIRECTORY);

        TestUtil.compareTwoSacfileArraysExact(truth, test, "BP N 4 C 12 14", 0.001f);
    }

    @Test
    public void testBP_C1_N_4_C_12_14_point5_100() {
        System.out.println("testBP_C1_N_4_C_12_14_point5_100");

        TestUtil.loadTestSacAlphaFile("seismogram.txt", TestUtil.SAC_TEST_FILES_DIRECTORY);

        List<SacTraceData> lstd = SacDataModel.getInstance().getData();
        SacTraceData test = lstd.get(0);

        TestUtil.runCommand("BP C1 N 4 C 12 14 TRANBW 0.5 ATTEN 100", new BandpassSacCommand());

        SacTraceData truth = TestUtil.getTruthAlpha("seis.bp.c1.n4.c.12_14.tr.5.atten_100.sacalpha", TestUtil.SAC_TEST_FILES_DIRECTORY);

        TestUtil.compareTwoSacfileArraysExact(truth, test, "BP C1 N 4 C 12 14 0.5 100", 0.001f);
    }

    @Test
    public void Chebyshev2() {
        TestUtil.loadTestSacFile("funcgenSeismogram.sac", TestUtil.SAC_TEST_FILES_DIRECTORY, true);

        List<SacTraceData> lstd = SacDataModel.getInstance().getData();
        SacTraceData test = lstd.get(0);

        TestUtil.runCommand("bp c2 n 2 p 1 t 0.3 a 30", new BandpassSacCommand());

        SacTraceData truth = TestUtil.getTruth("bpChebyshev2Truth.sac", TEST_FILE_DIRECTORY);

        TestUtil.compareArraysNormalizedMeanAbsoluteError(truth.getData(), test.getData());
    }
}
