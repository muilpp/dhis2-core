/*
 * Copyright (c) 2004-2022, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.hisp.dhis.tracker.validation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.core.Every.everyItem;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.hisp.dhis.config.H2DhisConfigurationProvider;
import org.hisp.dhis.encryption.EncryptionStatus;
import org.hisp.dhis.external.conf.DhisConfigurationProvider;
import org.hisp.dhis.tracker.TrackerImportParams;
import org.hisp.dhis.tracker.TrackerImportService;
import org.hisp.dhis.tracker.TrackerImportStrategy;
import org.hisp.dhis.tracker.TrackerTest;
import org.hisp.dhis.tracker.report.TrackerErrorCode;
import org.hisp.dhis.tracker.report.TrackerImportReport;
import org.hisp.dhis.tracker.report.TrackerStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class TeTaEncryptionValidationTest extends TrackerTest
{

    @Autowired
    private DhisConfigurationProvider dhisConfigurationProvider;

    @Autowired
    private TrackerImportService trackerImportService;

    @Override
    protected void initTest()
        throws IOException
    {
        setUpMetadata( "tracker/validations/te-program_with_tea_encryption_metadata.json" );
        injectAdminUser();
    }

    @Test
    void testEncryptedAttrFail()
        throws IOException
    {
        TrackerImportParams trackerImportParams = fromJson(
            "tracker/validations/te-program_with_tea_encryption_data.json" );
        H2DhisConfigurationProvider dhisConfigurationProvider = (H2DhisConfigurationProvider) this.dhisConfigurationProvider;
        dhisConfigurationProvider.setEncryptionStatus( EncryptionStatus.MISSING_ENCRYPTION_PASSWORD );
        TrackerImportReport trackerImportReport = trackerImportService.importTracker( trackerImportParams );
        assertEquals( 1, trackerImportReport.getValidationReport().getErrors().size() );
        assertThat( trackerImportReport.getValidationReport().getErrors(),
            everyItem( hasProperty( "errorCode", equalTo( TrackerErrorCode.E1112 ) ) ) );
    }

    @Test
    void testUniqueFailInOrgUnit()
        throws IOException
    {
        TrackerImportParams trackerImportParams = fromJson(
            "tracker/validations/te-program_with_tea_unique_data_in_country.json" );
        TrackerImportReport trackerImportReport = trackerImportService.importTracker( trackerImportParams );
        assertEquals( TrackerStatus.OK, trackerImportReport.getStatus() );

        trackerImportParams = fromJson(
            "tracker/validations/te-program_with_tea_unique_data_in_country.json" );
        trackerImportParams.setImportStrategy( TrackerImportStrategy.CREATE_AND_UPDATE );
        trackerImportReport = trackerImportService.importTracker( trackerImportParams );
        assertEquals( TrackerStatus.OK, trackerImportReport.getStatus() );
        trackerImportParams = fromJson(
            "tracker/validations/te-program_with_tea_unique_data_in_region.json" );
        trackerImportReport = trackerImportService.importTracker( trackerImportParams );
        assertEquals( TrackerStatus.OK, trackerImportReport.getStatus() );
    }

    @Test
    void testUniqueFail()
        throws IOException
    {
        TrackerImportParams trackerImportParams = fromJson(
            "tracker/validations/te-program_with_tea_unique_data.json" );
        TrackerImportReport trackerImportReport = trackerImportService.importTracker( trackerImportParams );
        assertEquals( TrackerStatus.OK, trackerImportReport.getStatus() );

        trackerImportParams = fromJson( "tracker/validations/te-program_with_tea_unique_data2.json" );
        trackerImportReport = trackerImportService.importTracker( trackerImportParams );
        assertEquals( 1, trackerImportReport.getValidationReport().getErrors().size() );
        assertThat( trackerImportReport.getValidationReport().getErrors(),
            everyItem( hasProperty( "errorCode", equalTo( TrackerErrorCode.E1064 ) ) ) );
    }
}
